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
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

final class TableAnnotationHandlerImpl extends FieldHandlerSupport implements TableAnnotationHandler {

    static TableAnnotationHandler create(ProcessingEnvironment env, StringBuilder tempBuilder) {
        final Map<String, String> map;
        map = env.getOptions();

        final Options options;
        if (map != null && "false".equals(map.get("army.snowflakeStartTimeWarning"))) {
            options = new Options(false);
        } else {
            options = new Options(true);
        }
        return new TableAnnotationHandlerImpl(env, options, tempBuilder);
    }


    private final FieldTypeParser fieldTypeParser;

    private final Options options;

    private final DiscriminatorHandler discriminatorHandler;

    private final TableSourceCodeGen tableSourceCodeGen;

    private final Map<String, Set<String>> startTimeToDomain = new HashMap<>();

    private final Map<String, Set<String>> fieldNamesMap = new HashMap<>();

    private final TypeElement[] parentHolder = new TypeElement[1];

    private final TypeElement[] mappedHolder = new TypeElement[1];

    private final MappingMode[] modeHolder = new MappingMode[1];

    private List<String> errorMsgList;


    private TableAnnotationHandlerImpl(ProcessingEnvironment env, Options options, StringBuilder tempBuilder) {
        super(tempBuilder);
        this.options = options;

        final Context context = new Context(env, tempBuilder, this::addErrorMsg, this::getErrorCount);

        if (env.getElementUtils().getTypeElement(InferFieldTypeParser.COMPOSITE_TYPE) == null) {
            this.fieldTypeParser = InferFieldTypeParser.create(context);
        } else {
            this.fieldTypeParser = MirrorFieldTypeParser.create(context);
        }

        this.discriminatorHandler = DiscriminatorHandlerImpl.create(this::addErrorMsg);

        this.tableSourceCodeGen = TableSourceCodeGenImpl.create(this.tempBuilder);
    }

    @Nullable
    @Override
    public Pair handle(final TypeElement domainElement) {

        final TypeElement[] holder = this.parentHolder;
        final MappingMode[] modeHolder = this.modeHolder;
        final List<FieldMeta> fieldMetaList;
        fieldMetaList = createFieldMetaList(domainElement, holder, modeHolder);

        final TypeElement parentElement = holder[0];

        final MappingMode mode;
        mode = modeHolder[0];

        if (mode == MappingMode.CHILD || mode == MappingMode.PARENT) {
            this.discriminatorHandler.validateDiscriminatorValue(domainElement);
        }

        validateTable(domainElement, mode);

        if (mode == null || hasError()) {
            return null;
        }
        return this.tableSourceCodeGen.generateSource(domainElement, parentElement, mode, fieldMetaList);
    }

    @Override
    public List<String> getErrorMessages() {
        List<String> list = this.errorMsgList;
        if (list == null) {
            list = List.of();
        } else {
            list = Collections.unmodifiableList(list);
        }
        return list;
    }

    @Override
    public List<Pair> compositeSourceList() {
        return this.fieldTypeParser.compositeSourceList(((TableSourceCodeGenImpl) this.tableSourceCodeGen).sourceCodeBuilder);
    }

    @Override
    public void endHandle() {
        if (!this.options.snowflakeStartTimeWarning) {
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

    @Override
    void addErrorMsg(String msg) {
        List<String> list = this.errorMsgList;
        if (list == null) {
            this.errorMsgList = list = new ArrayList<>();
        }
        list.add(msg);
    }


    private void validateTable(final TypeElement domainElement, final @Nullable MappingMode mode) {
        final String className = MetaUtils.getClassName(domainElement);

        final Table table = domainElement.getAnnotation(Table.class);
        assert table != null;

        final String tableName;
        tableName = getTableName(className, MetaUtils.getSimpleClassName(domainElement), table);
        if (!MetaUtils.hasText(tableName)) {
            String m = String.format("%s.Table name is empty", className);
            addErrorMsg(m);
        }

        if (_MetaBridge.isCamelCase(tableName)) {  // army don't allow camel
            String m = String.format("%s table name is CamelCase.", className);
            addErrorMsg(m);
        }

        if (!tableName.equals(tableName.trim())) {
            String m = String.format("%s table name has leading or trailing spaces.", className);
            addErrorMsg(m);
        }


        final Set<String> fieldNameSet;
        if (mode == null || mode == MappingMode.PARENT) {
            fieldNameSet = this.fieldNamesMap.get(className);
        } else {
            fieldNameSet = this.fieldNamesMap.remove(className);
        }

        if (fieldNameSet == null) {
            return;
        }

        final Index[] indexArray = table.indexes();

        IndexField[] indexFields;
        String[] fieldList;
        Index index;
        for (int i = 0; i < indexArray.length; i++) {
            index = indexArray[i];
            indexFields = index.fields();
            if (indexFields.length > 0) {
                for (IndexField field : indexFields) {
                    if (!fieldNameSet.contains(field.name())) {
                        String m = String.format("%s index[%s] field %s not found.", className, i, field.name());
                        addErrorMsg(m);
                    }
                }
            } else if ((fieldList = index.fieldList()) == null) {
                String m = String.format("%s index[%s] no any field.", className, i);
                addErrorMsg(m);
            } else {
                for (String fieldName : fieldList) {
                    if (!fieldNameSet.contains(fieldName)) {
                        String m = String.format("%s index[%s] field %s not found.", className, i, fieldName);
                        addErrorMsg(m);
                    }
                }
            }
        } // index loop


    }


    private String getTableName(final String domainName, final String simpleName, final Table table) {
        final String value = table.name();
        final String finalValue;
        switch (value) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configValue;
                key = getAndClearTempBuilder()
                        .append(domainName)
                        .append('.')
                        .append("Table")
                        .append('.')
                        .append("name")
                        .toString();

                configValue = getTableMetaMap().get(key);
                if (MetaUtils.hasText(configValue)) {
                    finalValue = configValue.trim();
                } else switch (value) {
                    case DEFAULT_EXP:
                        finalValue = _MetaBridge.camelToLowerCase(simpleName, this.tempBuilder);
                        break;
                    case RUNTIME_EXP:
                        finalValue = value;
                        addErrorMsg(String.format("%s no config", key));
                        break;
                    default:
                        finalValue = value;
                }
            }
            break;
            case OPTIONAL_EXP: {
                finalValue = value;
                String m = String.format("%s in %s %s.%s is unsupported", value, domainName,
                        "Table", "name");
                addErrorMsg(m);
            }
            break;
            default: {
                finalValue = value;
            }
        }
        return finalValue;
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
                addErrorMsg(m);
            }
        } else if (parent == null) {
            mode = MappingMode.PARENT;
            if (discriminatorValue == null) {
                String m;
                m = String.format("Domain %s discriminator absent.", MetaUtils.getClassName(domain));
                addErrorMsg(m);
            } else {
                this.discriminatorHandler.storeDiscriminatorValue(domain, discriminatorValue.value(), domain);
            }
        } else if (discriminatorValue != null) {
            mode = MappingMode.CHILD;
            this.discriminatorHandler.storeDiscriminatorValue(domain, discriminatorValue.value(), domain);
        } else {
            mode = null;
            String m = String.format("Domain %s no parent,couldn't be annotated by %s."
                    , MetaUtils.getClassName(domain), DiscriminatorValue.class.getName());
            addErrorMsg(m);
        }
        return mode;
    }


    private List<FieldMeta> createFieldMetaList(final TypeElement targetElement, final TypeElement[] holder,
                                                final @Nullable MappingMode[] modeHolder) {
        holder[0] = null; //clear

        TypeElement superElement = targetElement;

        final List<FieldMeta> fieldList = new ArrayList<>(20), tempFieldList = new ArrayList<>(20);

        final Inheritance inheritance;
        inheritance = targetElement.getAnnotation(Inheritance.class);
        final String discriminatorFieldName = inheritance == null ? null : inheritance.value();
        final String domainName = MetaUtils.getClassName(targetElement);

        final boolean childTable = inheritance == null && targetElement.getAnnotation(DiscriminatorValue.class) != null;

        final Set<String> columnNamSet = ArmyCollections.hashSetForSize(20);
        final Map<String, VariableElement> fieldMap = ArmyCollections.hashMapForSize(20);


        Column column;
        VariableElement field;
        String className, fieldName, columnName;
        boolean discriminatorField, foundDiscriminatorColumn = false;
        FieldType fieldType;


        for (TypeMirror superMirror = superElement.asType(); ; superMirror = superElement.getSuperclass()) {
            if (superMirror instanceof NoType) {
                break;
            }

            superElement = (TypeElement) ((DeclaredType) superMirror).asElement();
            if (superElement.getNestingKind() != NestingKind.TOP_LEVEL) {
                String m = String.format("Nested class %s is not supported.", MetaUtils.getClassName(superElement));
                addErrorMsg(m);
                break;
            }
            if (superElement != targetElement && superElement.getAnnotation(Inheritance.class) != null) {
                if (inheritance != null) {
                    addErrorInheritance(targetElement, superElement);
                }
                field = findIdFieldFromParent(superElement);
                if (field != null) {
                    fieldName = field.getSimpleName().toString();
                    if (fieldMap.put(fieldName, field) != null) {
                        String m = String.format("Field %s#%s is overridden.", MetaUtils.getClassName(superElement), fieldName);
                        addErrorMsg(m);
                    }
                    fieldList.add(FieldMeta.of(field, FieldType.DEFAULT));
                }
                holder[0] = superElement;
                break;
            }


            if (superElement.getAnnotation(MappedSuperclass.class) == null
                    && superElement.getAnnotation(Table.class) == null) {
                break;
            }


            for (Element element : superElement.getEnclosedElements()) {
                if (element.getKind() != ElementKind.FIELD
                        || element.getModifiers().contains(Modifier.STATIC)
                        || (column = element.getAnnotation(Column.class)) == null) {
                    continue;
                }

                field = (VariableElement) element;
                fieldName = field.getSimpleName().toString();

                if (fieldMap.put(fieldName, field) != null) {
                    String m = String.format("Field %s#%s is overridden.", MetaUtils.getClassName(superElement), fieldName);
                    addErrorMsg(m);
                }

                // get column name
                columnName = getColumnName(domainName, fieldName, column);
                if (!columnName.startsWith("${") && !columnNamSet.add(columnName)) {
                    String m = String.format("Field %s#%s column[%s] duplication.", domainName, fieldName, columnName);
                    addErrorMsg(m);
                }

                discriminatorField = discriminatorFieldName != null && discriminatorFieldName.equals(fieldName);
                if (discriminatorField) {
                    foundDiscriminatorColumn = true;
                    this.discriminatorHandler.handle(domainName, field);
                }

                fieldType = validateField(domainName, childTable, fieldName, field, column, discriminatorField);

                tempFieldList.add(FieldMeta.of(field, fieldType));

            }// for getEnclosedElements

            Collections.reverse(tempFieldList);
            fieldList.addAll(tempFieldList);
            tempFieldList.clear();

        } // superElement loop

        final TypeElement parentElement = holder[0];

        this.fieldNamesMap.put(domainName, Set.copyOf(fieldMap.keySet()));

        if (inheritance != null && !foundDiscriminatorColumn) {
            String m = String.format("Domain %s discriminator field[%s] not found.",
                    domainName, discriminatorFieldName);
            addErrorMsg(m);
        }

        String m;
        if (!fieldMap.containsKey(_MetaBridge.ID)) {
            m = String.format("Domain %s don't definite field %s."
                    , domainName, _MetaBridge.ID);
            addErrorMsg(m);
        }


        if (parentElement == null) {
            if (!fieldMap.containsKey(_MetaBridge.CREATE_TIME)) {
                m = String.format("Domain %s don't definite field %s."
                        , domainName, _MetaBridge.CREATE_TIME);
                addErrorMsg(m);
            }
            final Table table = targetElement.getAnnotation(Table.class);
            assert table != null;
            if (!table.immutable() && !fieldMap.containsKey(_MetaBridge.UPDATE_TIME)) {
                m = String.format("Domain %s don't definite field %s."
                        , domainName, _MetaBridge.UPDATE_TIME);
                addErrorMsg(m);
            }
        } else {
            final Set<String> parentFieldSet = obtainParentFieldSet(parentElement);
            for (String name : fieldMap.keySet()) {
                if (name.equals(_MetaBridge.ID)) {
                    continue;
                }
                if (parentFieldSet.contains(name)) {
                    m = String.format("Field %s.%s is overridden by parent %s."
                            , domainName, name, MetaUtils.getClassName(parentElement));
                    addErrorMsg(m);
                }
            } // filed name loop
        }


        final MappingMode mode;
        mode = validateMode(targetElement, parentElement);
        if (modeHolder != null) {
            modeHolder[0] = mode;
        }


        final OverrideParams overrideParams = targetElement.getAnnotation(OverrideParams.class);
        if (overrideParams != null) {
            createOverrideParamMap(overrideParams, domainName, mode, fieldMap::get);
        }

        Asserts.isTrue(fieldMap.size() == fieldList.size(), "bug");

        fieldMap.clear();
        columnNamSet.clear();

        holder[0] = parentElement; // finally write back, avoid bug

        Collections.reverse(fieldList);
        return List.copyOf(fieldList);
    }


    @Nullable
    private VariableElement findIdFieldFromParent(final TypeElement targetElement) {
        final VariableElement[] holder = new VariableElement[1];

        final Predicate<VariableElement> func;
        func = field -> {
            final boolean found;
            found = field.getSimpleName().toString().equals(_MetaBridge.ID);
            if (found) {
                holder[0] = field;
            }
            return found;
        };

        findFields(targetElement, false, func);

        final VariableElement field = holder[0];
        if (field == null) {
            String m = String.format("Domain %s don't definite field %s.", MetaUtils.getClassName(targetElement), _MetaBridge.ID);
            addErrorMsg(m);
        }
        return field;
    }


    private Set<String> obtainParentFieldSet(final TypeElement parentElement) {

        Asserts.isTrue(parentElement.getAnnotation(Inheritance.class) != null, "bug");

        final String domainName = MetaUtils.getClassName(parentElement);

        Set<String> set;
        set = this.fieldNamesMap.get(domainName);

        if (set == null) {
            createFieldMetaList(parentElement, this.mappedHolder, null);
            set = this.fieldNamesMap.get(domainName);
        }

        if (set == null) {
            throw new IllegalStateException("bug");
        }
        return set;
    }


    private FieldType validateField(final String domainName, final boolean childTable,
                                    final String fieldName, final VariableElement field, final Column column,
                                    final boolean discriminatorField) {

        final FieldType fieldType;
        switch (fieldName) {
            case _MetaBridge.ID:
                validateSnowflakeStartTime(domainName, childTable, fieldName, field);
                fieldType = FieldType.DEFAULT;
                break;
            case _MetaBridge.CREATE_TIME:
            case _MetaBridge.UPDATE_TIME:
                assertDateTime(domainName, field);
                fieldType = FieldType.DEFAULT;
                break;
            case _MetaBridge.VERSION:
                assertVersionField(domainName, field);
                fieldType = FieldType.DEFAULT;
                break;
            case _MetaBridge.VISIBLE:
                assertVisibleField(domainName, field);
                fieldType = FieldType.DEFAULT;
                break;
            default: {
                if (!discriminatorField && !MetaUtils.hasText(column.comment())) {
                    noCommentError(domainName, field);
                }
                if (discriminatorField) {
                    fieldType = FieldType.DEFAULT;
                } else {
                    validateSnowflakeStartTime(domainName, childTable, fieldName, field);
                    fieldType = this.fieldTypeParser.parseType(domainName, field);
                }
            } // default
        } //switch
        return fieldType;
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
            addErrorMsg(m);
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
            addErrorMsg(m);
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
            addErrorMsg(m);
        }
    }


    private void createOverrideParamMap(final OverrideParams overrideParams, final String domainName,
                                        final @Nullable MappingMode mode, final Function<String, VariableElement> func) {

        String fieldName, paramName;
        VariableElement fieldElement;
        Generator generator;
        topLoop:
        for (FieldParam field : overrideParams.fields()) {
            fieldName = field.name();

            if (_MetaBridge.ID.equals(fieldName) && mode == MappingMode.CHILD) {
                String m = String.format("%s is child table, couldn't override id field.", domainName);
                addErrorMsg(m);
                continue;
            }

            fieldElement = func.apply(fieldName);
            if (fieldElement == null) {
                String m = String.format("%s override field[%s] not exists.", domainName, fieldName);
                addErrorMsg(m);
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

    private void validateSnowflakeStartTime(final String domainName, final boolean childTable, final String fieldName, final VariableElement field) {
        final Generator generator;
        if ((generator = field.getAnnotation(Generator.class)) == null
                || generator.type() == GeneratorType.POST
                || !generator.value().equals("io.army.generator.snowflake.SnowflakeGenerator")) {
            return;
        }
        if (childTable && _MetaBridge.ID.equals(fieldName)) {
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


    private void addErrorInheritance(final TypeElement tableElement, final TypeElement superElement) {
        String m = String.format("Domain %s couldn't be annotated by %s,because %s is annotated by %s.",
                tableElement.getQualifiedName(), Inheritance.class.getName(),
                superElement.getQualifiedName(), Inheritance.class.getName()
        );
        addErrorMsg(m);
    }

    private void noCommentError(final String className, final VariableElement field) {
        String m = String.format("Field %s.%s isn't reserved field or discriminator field,so comment must have text."
                , className, field.getSimpleName());
        addErrorMsg(m);
    }


    private boolean hasError() {
        final List<String> list = this.errorMsgList;
        return list != null && !list.isEmpty();
    }

    private int getErrorCount() {
        final List<String> list = this.errorMsgList;
        return list == null ? 0 : list.size();
    }


}
