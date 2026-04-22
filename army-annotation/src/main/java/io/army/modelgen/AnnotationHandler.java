/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.army.modelgen;

import io.army.annotation.*;
import io.army.lang.Nullable;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

final class AnnotationHandler {

    private final ProcessingEnvironment env;

    private final boolean snowflakeStartTimeWarning;

    final List<String> errorMsgList = new ArrayList<>();

    private final Map<String, Map<String, VariableElement>> parentFieldCache = new HashMap<>();

    private final Map<String, Map<String, TypeElement>> discriminatorValueCache = new HashMap<>();

    private final Map<String, MappingMode> mappingModeMap = new HashMap<>();

    private final Map<String, Set<String>> startTimeToDomain = new HashMap<>();

    private final Map<String, Set<String>> enumConstMap = new HashMap<>();

    private final Map<String, String> domainToEnumMap = new HashMap<>();

    private final StringBuilder tempBuilder = new StringBuilder();


    AnnotationHandler(ProcessingEnvironment env, boolean snowflakeStartTimeWarning) {
        this.env = env;
        this.snowflakeStartTimeWarning = snowflakeStartTimeWarning;
    }


    void createSourceFiles(final Set<? extends Element> domainElementSet) throws IOException {
        final SourceCodeCreator codeCreator = new SourceCodeCreator(this.env.getSourceVersion(), this.env.getFiler(), this.tempBuilder);
        final List<String> errorMsgList = this.errorMsgList;
        final TypeElement[] outParent = new TypeElement[1];

        TypeElement domain, parentDomain;
        Map<String, VariableElement> fieldMap;
        List<TypeElement> mappedList;
        Map<String, IndexMode> indexModeMap;
        MappingMode mode;
        String customeTableName, domainName;
        Table table;
        Inheritance inheritance;
        for (Element element : domainElementSet) {
            domain = (TypeElement) element;
            if (domain.getNestingKind() != NestingKind.TOP_LEVEL) {
                continue;
            }

            domainName = MetaUtils.getClassName(domain);

            if ((inheritance = domain.getAnnotation(Inheritance.class)) != null) {
                parentDomain = null;
                mappedList = null;
            } else {
                outParent[0] = null; // clear
                mappedList = getMappedList(domain, outParent);
                parentDomain = outParent[0];
            }

            mode = validateMode(domain, parentDomain);

            if (mode == null) {
                continue;  // occur error
            }

            this.mappingModeMap.put(domainName, mode);

            if (inheritance != null) {
                fieldMap = getParentFieldMap(domain);
            } else if (parentDomain == null) {
                fieldMap = getFieldSet(mappedList, null);
            } else {
                fieldMap = getFieldSet(mappedList, getParentFieldMap(parentDomain));
            }

            if (mode != MappingMode.SIMPLE) {
                validateDiscriminatorValue(domain, domainName);
            }

            // validate table name
            table = domain.getAnnotation(Table.class);
            assert table != null;
            customeTableName = table.name();
            if (_MetaBridge.isCamelCase(customeTableName)) {  // army don't allow camel
                String m = String.format("%s table name is CamelCase.", domainName);
                errorMsgList.add(m);
            }
            if (!customeTableName.trim().equals(customeTableName)) {
                String m = String.format("please trim %s table name .", domainName);
                errorMsgList.add(m);
            }
            if (!errorMsgList.isEmpty()) {
                continue;
            }
            indexModeMap = createFieldToIndexModeMap(domain);
            for (String fieldName : indexModeMap.keySet()) {
                if (!fieldMap.containsKey(fieldName)) {
                    String m = String.format("Not found index field[%s] in %s."
                            , fieldName, MetaUtils.getClassName(domain));
                    errorMsgList.add(m);
                }
            }
            if (!errorMsgList.isEmpty()) {
                continue;
            }

            codeCreator.create(domain, fieldMap, parentDomain, mode, indexModeMap);

        } // class element loop

        if (errorMsgList.isEmpty()) {
            codeCreator.flush(); // finally flush
        }

        printSnowflakeStartTimeWarning();

    }


    private void validateDiscriminatorValue(final TypeElement domain, final String domainName) {
        final DiscriminatorValue discriminatorValue = domain.getAnnotation(DiscriminatorValue.class);
        final String enumName = this.domainToEnumMap.get(domainName);
        final Set<String> enumSet;
        if (discriminatorValue == null
                || enumName == null
                || (enumSet = this.enumConstMap.get(enumName)) == null) {
            //occur error
            return;
        }
        if (!enumSet.contains(discriminatorValue.value())) {
            String m = String.format("Domain[%s] %s value[%s] not found in enum %s.",
                    domainName, DiscriminatorValue.class.getSimpleName(), discriminatorValue.value(), enumName);
            this.errorMsgList.add(m);
        }
    }


    private void printSnowflakeStartTimeWarning() {
        if (!this.snowflakeStartTimeWarning) {
            return;
        }
        StringBuilder builder = null;
        Set<String> set;
        for (Map.Entry<String, Set<String>> e : this.startTimeToDomain.entrySet()) {
            set = e.getValue();
            if (set.size() < 2) {
                continue;
            }
            if (builder == null) {
                builder = new StringBuilder();
            } else {
                builder.append('\n');
            }
            builder.append("snowflake startTime")
                    .append('[')
                    .append(e.getKey())
                    .append(']')
                    .append(" is used by ")
                    .append(set)
            ;

        }

        if (builder == null) {
            return;
        }
        builder.append('\n');
        final String text = """
                This may reduce concurrency during high - traffic concurrent situations.
                You can use the following code to turn off this warning.
                     <configuration>
                           <annotationProcessors>io.army.modelgen.ArmyMetaModelDomainProcessor</annotationProcessors>
                           <compilerArgs>
                                 <arg>-Aarmy.snowflakeStartTimeWarning=false</arg>
                           </compilerArgs>
                     </configuration>
                """;
        builder.append(text);
        System.out.printf("[%sWARNING%s] %s%n", "\u001B[33m", "\u001B[0m", builder);
    }


    private void addErrorInheritance(final TypeElement tableElement, final TypeElement superElement) {
        String m = String.format("Domain %s couldn't be annotated by %s,because %s is annotated by %s."
                , tableElement.getQualifiedName(), Inheritance.class.getName()
                , superElement.getQualifiedName(), Inheritance.class.getName());
        this.errorMsgList.add(m);
    }

    private List<TypeElement> getMappedList(final TypeElement tableElement, final TypeElement[] outParent) {
        TypeElement superElement = tableElement, parentElement = null;
        List<TypeElement> mappedList = null;
        for (TypeMirror superMirror = superElement.getSuperclass(); ; superMirror = superElement.getSuperclass()) {
            if (superMirror instanceof NoType) {
                break;
            }
            superElement = (TypeElement) ((DeclaredType) superMirror).asElement();
            if (superElement.getNestingKind() != NestingKind.TOP_LEVEL) {
                break;
            }
            if (superElement.getAnnotation(Inheritance.class) != null) {
                if (tableElement.getAnnotation(Inheritance.class) != null) {
                    addErrorInheritance(tableElement, superElement);
                }
                parentElement = superElement;
                break;
            }
            if (superElement.getAnnotation(MappedSuperclass.class) == null
                    && superElement.getAnnotation(Table.class) == null) {
                break;
            }
            if (mappedList == null) {
                mappedList = ArmyCollections.arrayList();
                mappedList.add(tableElement);
            }
            mappedList.add(superElement);
        }//for

        if (mappedList == null) {
            mappedList = Collections.singletonList(tableElement);
        } else {
            mappedList = Collections.unmodifiableList(mappedList);
        }

        outParent[0] = parentElement;
        return mappedList;

    }

    @Nullable
    private MappingMode validateMode(final TypeElement domain, final @Nullable TypeElement parent) {

        final Inheritance inheritance;
        inheritance = domain.getAnnotation(Inheritance.class);
        final DiscriminatorValue discriminatorValue;
        discriminatorValue = domain.getAnnotation(DiscriminatorValue.class);

        final MappingMode mode;
        if (parent == null && inheritance == null) {
            mode = MappingMode.SIMPLE;
            if (discriminatorValue != null) {
                String m = String.format("Domain %s no parent,couldn't be annotated by %s."
                        , MetaUtils.getClassName(domain), DiscriminatorValue.class.getName());
                this.errorMsgList.add(m);
            }
        } else if (parent == null) {
            mode = MappingMode.PARENT;
            if (discriminatorValue == null) {
                String m;
                m = String.format("Domain %s discriminator absent.", MetaUtils.getClassName(domain));
                this.errorMsgList.add(m);
            } else {
                storeDiscriminatorValue(domain, discriminatorValue.value(), domain);
            }
        } else if (discriminatorValue != null) {
            mode = MappingMode.CHILD;
            storeDiscriminatorValue(parent, discriminatorValue.value(), domain);
        } else {
            mode = null;
            String m = String.format("Domain %s no parent,couldn't be annotated by %s."
                    , MetaUtils.getClassName(domain), DiscriminatorValue.class.getName());
            this.errorMsgList.add(m);
        }
        return mode;
    }

    private void storeDiscriminatorValue(final TypeElement parent, final String value, final TypeElement domain) {
        final TypeElement oldDomain;
        oldDomain = this.discriminatorValueCache.computeIfAbsent(MetaUtils.getClassName(parent), _ -> new HashMap<>())
                .putIfAbsent(value, domain);
        if (oldDomain != null && oldDomain != domain) {
            String m = String.format("Domain %s discriminator value[%s] duplication."
                    , MetaUtils.getClassName(domain), value);
            this.errorMsgList.add(m);
        }
    }


    private Map<String, VariableElement> getFieldSet(final List<TypeElement> mappedList,
                                                     final @Nullable Map<String, VariableElement> parentFieldMap) {

        final TypeElement tableElement;
        tableElement = mappedList.getFirst();
        final String domainName = MetaUtils.getClassName(tableElement);


        final Inheritance inheritance;
        inheritance = tableElement.getAnnotation(Inheritance.class);
        final String discriminatorField = inheritance == null ? null : inheritance.value();
        final Table table;
        table = tableElement.getAnnotation(Table.class);
        assert table != null;


        VariableElement field;
        Column column;
        boolean foundDiscriminatorColumn = false;
        String className, fieldName, columnName;
        final Map<String, VariableElement> fieldMap = new HashMap<>();
        final Map<String, Boolean> columnNameMap = new HashMap<>();

        for (TypeElement mapped : mappedList) {
            className = MetaUtils.getClassName(mapped);

            for (Element element : mapped.getEnclosedElements()) {
                if (element.getKind() != ElementKind.FIELD
                        || element.getModifiers().contains(Modifier.STATIC)
                        || (column = element.getAnnotation(Column.class)) == null) {
                    continue;
                }
                field = (VariableElement) element;
                fieldName = field.getSimpleName().toString();

                if ((parentFieldMap != null && parentFieldMap.containsKey(fieldName))
                        || fieldMap.putIfAbsent(fieldName, field) != null) {
                    this.errorMsgList.add(String.format("Field %s.%s is overridden.", className, fieldName));
                }
                // get column name
                columnName = getColumnName(className, fieldName, column);
                if (columnNameMap.putIfAbsent(columnName, Boolean.TRUE) != null) {
                    String m = String.format("Field %s.%s column[%s] duplication.", className, fieldName, columnName);
                    this.errorMsgList.add(m);
                }
                if (discriminatorField != null && discriminatorField.equals(fieldName)) {
                    foundDiscriminatorColumn = true;
                    assertDiscriminatorEnum(domainName, className, field);
                    validateField(domainName, className, fieldName, field, column, true);
                } else {
                    validateField(domainName, className, fieldName, field, column, false);
                }


            }// for getEnclosedElements

        } // class loop

        final OverrideParams overrideParams = tableElement.getAnnotation(OverrideParams.class);
        if (overrideParams != null) {
            createOverrideParamMap(overrideParams, domainName, fieldMap::get);
        }


        if (inheritance != null && !foundDiscriminatorColumn) {
            this.errorMsgList.add(String.format("Domain %s discriminator field[%s] not found."
                    , MetaUtils.getClassName(mappedList.getFirst()), discriminatorField));
        }
        columnNameMap.clear();

        if (parentFieldMap == null) {
            String m;
            if (!fieldMap.containsKey(_MetaBridge.ID)) {
                m = String.format("Domain %s don't definite field %s."
                        , tableElement.getQualifiedName(), _MetaBridge.ID);
                this.errorMsgList.add(m);
            }
            if (!fieldMap.containsKey(_MetaBridge.CREATE_TIME)) {
                m = String.format("Domain %s don't definite field %s."
                        , tableElement.getQualifiedName(), _MetaBridge.CREATE_TIME);
                this.errorMsgList.add(m);
            }
            if (!table.immutable() && !fieldMap.containsKey(_MetaBridge.UPDATE_TIME)) {
                m = String.format("Domain %s don't definite field %s."
                        , tableElement.getQualifiedName(), _MetaBridge.UPDATE_TIME);
                this.errorMsgList.add(m);
            }
        } else {
            field = parentFieldMap.get(_MetaBridge.ID);
            assert field != null;
            fieldMap.put(_MetaBridge.ID, field);
        }
        return Collections.unmodifiableMap(fieldMap);
    }


    private Map<String, VariableElement> getParentFieldMap(final TypeElement parent) {
        final String className;
        className = MetaUtils.getClassName(parent);

        Map<String, VariableElement> fieldMap;
        fieldMap = this.parentFieldCache.get(className);
        if (fieldMap == null) {
            final TypeElement[] outParent = new TypeElement[1];
            List<TypeElement> mappedList;
            mappedList = getMappedList(parent, outParent);
            if (outParent[0] != null) {
                addErrorInheritance(parent, outParent[0]);
            }
            fieldMap = getFieldSet(mappedList, null);
            this.parentFieldCache.put(className, fieldMap);
        }
        return fieldMap;
    }


    private void createOverrideParamMap(final OverrideParams overrideParams, final String domainName, final Function<String, VariableElement> func) {

        String fieldName, paramName;
        VariableElement fieldElement;
        Generator generator;
        topLoop:
        for (FieldParam field : overrideParams.fields()) {
            fieldName = field.name();

            if (_MetaBridge.ID.equals(fieldName)
                    && this.mappingModeMap.get(domainName) == MappingMode.CHILD) {
                String m = String.format("%s is child table, couldn't override id field.", domainName);
                this.errorMsgList.add(m);
                continue;
            }

            fieldElement = func.apply(fieldName);
            if (fieldElement == null) {
                String m = String.format("%s override field[%s] not exists.", domainName, fieldName);
                this.errorMsgList.add(m);
                continue;
            }

            for (Param param : field.params()) {
                paramName = param.name();
                if (!paramName.equals("startTime")) {
                    continue;
                }
                if ((generator = fieldElement.getAnnotation(Generator.class)) == null
                        || generator.type() == GeneratorType.POST
                        || !generator.value().equals("io.army.generator.snowflake.SnowflakeGenerator")) {
                    continue;
                }
                this.startTimeToDomain.computeIfAbsent(param.value(), _ -> new HashSet<>())
                        .add(domainName);
                break topLoop;
            } // for param loop

        } // for field loop
    }


    private void validateField(final String domainName, final String className, final String fieldName,
                               final VariableElement field, final Column column, final boolean discriminatorField) {
        switch (fieldName) {
            case _MetaBridge.ID:
                validateSnowflakeStartTime(domainName, fieldName, field);
                break;
            case _MetaBridge.CREATE_TIME:
            case _MetaBridge.UPDATE_TIME:
                assertDateTime(className, field);
                break;
            case _MetaBridge.VERSION:
                assertVersionField(className, field);
                break;
            case _MetaBridge.VISIBLE:
                assertVisibleField(className, field);
                break;
            default: {
                if (!discriminatorField && !MetaUtils.hasText(column.comment())) {
                    noCommentError(className, field);
                }
                if (!discriminatorField) {
                    validateSnowflakeStartTime(domainName, fieldName, field);
                }
            } // default
        } //switch
    }


    private void validateSnowflakeStartTime(final String domainName, final String fieldName, final VariableElement field) {
        final Generator generator;
        if ((generator = field.getAnnotation(Generator.class)) == null
                || generator.type() == GeneratorType.POST
                || !generator.value().equals("io.army.generator.snowflake.SnowflakeGenerator")) {
            return;
        }
        if (_MetaBridge.ID.equals(fieldName)
                && this.mappingModeMap.get(domainName) == MappingMode.CHILD) {
            return;
        }

        String paramName;
        for (Param param : generator.params()) {
            paramName = param.name();
            if (!paramName.equals("startTime")) {
                continue;
            }
            this.startTimeToDomain.computeIfAbsent(param.value(), _ -> new HashSet<>())
                    .add(domainName);
            break;
        }

    }


    /**
     * @return lower case column
     */
    private String getColumnName(final String className, final String fieldName, final Column column) {
        final String customColumnName, lowerCaseColumnName;
        customColumnName = column.name();
        if (customColumnName.isEmpty()) {
            lowerCaseColumnName = _MetaBridge.camelToLowerCase(fieldName, this.tempBuilder);
        } else {
            if (_MetaBridge.isCamelCase(customColumnName)) { // army don't allow camel
                String m = String.format("Field %s.%s column name[%s] is camel.", className, fieldName, customColumnName);
                this.errorMsgList.add(m);
            }
            if (!customColumnName.trim().equals(customColumnName)) {
                String m = String.format("please trim Field %s.%s column name[%s].", className, fieldName, customColumnName);
                this.errorMsgList.add(m);
            }
            lowerCaseColumnName = customColumnName.toLowerCase(Locale.ROOT);
        }
        return lowerCaseColumnName;
    }


    private void assertDateTime(final String className, final VariableElement field) {
        final String fieldJavaClassName;
        fieldJavaClassName = field.asType().toString();
        if (!(fieldJavaClassName.equals(LocalDateTime.class.getName())
                || fieldJavaClassName.equals(OffsetDateTime.class.getName())
                || fieldJavaClassName.equals(ZonedDateTime.class.getName()))) {
            String m;
            m = String.format("Field %s.%s support only below java type:\n%s\n%s\n%s."
                    , className, field.getSimpleName()
                    , LocalDateTime.class.getName()
                    , OffsetDateTime.class.getName()
                    , ZonedDateTime.class.getName()
            );
            this.errorMsgList.add(m);
        }

    }

    private void assertVersionField(final String className, final VariableElement field) {
        final String fieldJavaClassName;
        fieldJavaClassName = field.asType().toString();
        if (!(fieldJavaClassName.equals(Integer.class.getName())
                || fieldJavaClassName.equals(int.class.getName())
                || fieldJavaClassName.equals(Long.class.getName())
                || fieldJavaClassName.equals(long.class.getName())
                || fieldJavaClassName.equals(BigInteger.class.getName()))) {
            String m;
            m = String.format("Field %s.%s support only below java type:\n%s\n%s\n%s."
                    , className, field.getSimpleName()
                    , Integer.class.getName()
                    , Long.class.getName()
                    , BigInteger.class.getName()
            );
            this.errorMsgList.add(m);
        }
    }

    private void assertVisibleField(final String className, final VariableElement field) {
        final String fieldJavaClassName;
        fieldJavaClassName = field.asType().toString();
        if (!(fieldJavaClassName.equals(Boolean.class.getName())
                || fieldJavaClassName.equals(boolean.class.getName()))) {
            String m;
            m = String.format("Field %s.%s support only %s."
                    , className, field.getSimpleName()
                    , Boolean.class.getName()
            );
            this.errorMsgList.add(m);
        }
    }

    private void assertDiscriminatorEnum(final String domainName, final String className, final VariableElement field) {
        final TypeMirror typeMirror = field.asType();
        final Element element;
        if (!(typeMirror instanceof DeclaredType)) {
            discriminatorNonCodeNum(className, field);
        } else if ((element = ((DeclaredType) typeMirror).asElement()).getKind() == ElementKind.ENUM) {
            storeEnumConsts(domainName, (TypeElement) element);
        } else {
            discriminatorNonCodeNum(className, field);
        }

    }


    private void storeEnumConsts(final String domainName, final TypeElement enumElement) {
        final String className = MetaUtils.getClassName(enumElement);
        if (this.enumConstMap.get(className) != null) {
            this.domainToEnumMap.put(domainName, className);
            return;
        }
        final Set<String> enumConstSet = new HashSet<>();
        for (Element e : enumElement.getEnclosedElements()) {
            if (e.getKind() != ElementKind.ENUM_CONSTANT) {
                continue;
            }
            enumConstSet.add(e.getSimpleName().toString());
        }
        this.domainToEnumMap.put(domainName, className);
        this.enumConstMap.put(className, Set.copyOf(enumConstSet));
    }

    private void discriminatorNonCodeNum(final String className, final VariableElement field) {
        String m = String.format("Discriminator field %s.%s isn't enum."
                , className, field.getSimpleName());
        this.errorMsgList.add(m);
    }

    private void noCommentError(final String className, final VariableElement field) {
        String m = String.format("Field %s.%s isn't reserved field or discriminator field,so comment must have text."
                , className, field.getSimpleName());
        this.errorMsgList.add(m);
    }


    private Map<String, IndexMode> createFieldToIndexModeMap(final TypeElement domain) {
        final Table table = domain.getAnnotation(Table.class);
        assert table != null;
        final Index[] indexArray = table.indexes();
        if (indexArray.length == 0) {
            return Collections.emptyMap();
        }
        final Map<String, IndexMode> indexMetaMap = ArmyCollections.hashMap();

        final Map<String, Boolean> indexNameMap = ArmyCollections.hashMap(indexArray.length + 3);
        String[] fieldArray;
        String indexName;
        IndexMode indexMode;
        for (Index index : indexArray) {
            // make index name lower case
            indexName = index.name();
            if (_MetaBridge.isCamelCase(indexName)) {
                String m = String.format("Domain %s index name[%s] is CamelCase.", MetaUtils.getClassName(domain), indexName);
                this.errorMsgList.add(m);
            }
            indexName = indexName.toLowerCase(Locale.ROOT);
            if (indexNameMap.putIfAbsent(indexName, Boolean.TRUE) != null) {
                String m = String.format("Domain %s index name[%s] duplication",
                        domain.getQualifiedName(), indexName);
                this.errorMsgList.add(m);
            }
            indexMode = IndexMode.resolve(index);
            for (String fieldName : index.fieldList()) {
                fieldArray = fieldName.split(" ");
                indexMetaMap.put(fieldArray[0], indexMode);
            }
        }
        indexMetaMap.put(_MetaBridge.ID, IndexMode.PRIMARY);
        return Collections.unmodifiableMap(indexMetaMap);
    }


}
