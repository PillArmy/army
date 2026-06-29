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
import io.army.struct.DefinedType;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

final class TableAnnotationHandlerImpl implements TableAnnotationHandler {

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


    private final Elements elements;

    private final Types types;

    private final StringBuilder tempBuilder;

    private final Options options;

    private final TypeMirror jsonbType;

    private final TypeMirror jsonType;

    private final TypeMirror compositeType;

    private final TypeMirror arrayType;

    private final DeclaredType buildInCompositeType;


    private final DiscriminatorHandler discriminatorHandler;

    private final TableSourceCodeGen tableSourceCodeGen;

    private final Map<String, MappingMode> mappingModeMap = new HashMap<>();

    private final Map<String, Set<String>> startTimeToDomain = new HashMap<>();

    private final Map<String, Set<String>> fieldNamesMap = new HashMap<>();

    private final Set<String> compositeClassNameSet = new HashSet<>();

    private final Map<String, CompositeMeta> compositeMetaMap = new HashMap<>();

    private final TypeElement[] parentHolder = new TypeElement[1];

    private final TypeElement[] mappedHolder = new TypeElement[1];

    private final MappingMode[] modeHolder = new MappingMode[1];

    private List<String> errorMsgList;

    private Properties tableMetaProperties;

    private Set<String> defaultUnderlyingComponentClassNameSet;


    private TableAnnotationHandlerImpl(ProcessingEnvironment env, Options options, StringBuilder tempBuilder) {
        this.elements = env.getElementUtils();
        this.types = env.getTypeUtils();
        this.options = options;
        this.tempBuilder = tempBuilder;

        this.buildInCompositeType = (DeclaredType) this.elements.getTypeElement("io.army.mapping.CompositeType").asType();

        final TypeElement mappingTypeElement = this.elements.getTypeElement("io.army.mapping.MappingType");

        TypeMirror jsonbType, jsonType, compositeType, arrayType;
        jsonbType = jsonType = compositeType = arrayType = null;
        for (Element element : mappingTypeElement.getEnclosedElements()) {
            if (element.getKind() != ElementKind.INTERFACE) {
                continue;
            }
            switch (element.getSimpleName().toString()) {
                case "SqlJsonb":
                    jsonbType = element.asType();
                    break;
                case "SqlJson":
                    jsonType = element.asType();
                    break;
                case "SqlComposite":
                    compositeType = element.asType();
                    break;
                case "SqlArray":
                    arrayType = element.asType();
                    break;
            }
        }

        assert jsonbType != null && jsonType != null && compositeType != null && arrayType != null;

        this.jsonbType = jsonbType;
        this.jsonType = jsonType;
        this.compositeType = compositeType;
        this.arrayType = arrayType;

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
        final Map<String, CompositeMeta> map = this.compositeMetaMap;
        if (map.isEmpty()) {
            return List.of();
        }

        final CompositeSourceCodeGen codeGen;
        codeGen = CompositeSourceCodeGenImpl.create(((TableSourceCodeGenImpl) this.tableSourceCodeGen).sourceCodeBuilder);

        final List<Pair> pairList = new ArrayList<>(map.size());
        for (CompositeMeta meta : map.values()) {
            pairList.add(codeGen.generateSource(meta));
        }
        return pairList;
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

    private void validateTable(final TypeElement domainElement, final @Nullable MappingMode mode) {
        final String className = MetaUtils.getClassName(domainElement);

        final Table table = domainElement.getAnnotation(Table.class);
        assert table != null;

        final String tableName = table.name();
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

            className = MetaUtils.getClassName(superElement);


            for (Element element : superElement.getEnclosedElements()) {
                if (element.getKind() != ElementKind.FIELD
                        || element.getModifiers().contains(Modifier.STATIC)
                        || (column = element.getAnnotation(Column.class)) == null) {
                    continue;
                }

                field = (VariableElement) element;
                fieldName = field.getSimpleName().toString();

                if (fieldMap.put(fieldName, field) != null) {
                    String m = String.format("Field %s#%s is overridden.", className, fieldName);
                    addErrorMsg(m);
                }

                // get column name
                columnName = getColumnName(className, fieldName, column);
                if (!columnName.startsWith("${") && !columnNamSet.add(columnName)) {
                    String m = String.format("Field %s#%s column[%s] duplication.", className, fieldName, columnName);
                    addErrorMsg(m);
                }

                discriminatorField = discriminatorFieldName != null && discriminatorFieldName.equals(fieldName);
                if (discriminatorField) {
                    foundDiscriminatorColumn = true;
                    this.discriminatorHandler.handle(domainName, field);
                }

                fieldType = validateField(domainName, className, fieldName, field, column, discriminatorField);

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
            createOverrideParamMap(overrideParams, domainName, fieldMap::get);
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


    /// @return lower case column
    private String getColumnName(final String className, final String fieldName, final Column column) {
        final String customColumnName, lowerCaseColumnName;
        customColumnName = column.name();
        if (customColumnName.isEmpty()) {
            lowerCaseColumnName = _MetaBridge.camelToLowerCase(fieldName, this.tempBuilder);
        } else {
            if (_MetaBridge.isCamelCase(customColumnName)) { // army don't allow camel
                String m = String.format("Field %s.%s column name[%s] is camel.", className, fieldName, customColumnName);
                addErrorMsg(m);
            }
            if (!customColumnName.trim().equals(customColumnName)) {
                String m = String.format("please trim Field %s.%s column name[%s].", className, fieldName, customColumnName);
                addErrorMsg(m);
            }
            lowerCaseColumnName = customColumnName.toLowerCase(Locale.ROOT);
        }
        return lowerCaseColumnName;
    }


    private FieldType validateField(final String domainName, final String className, final String fieldName,
                                    final VariableElement field, final Column column, final boolean discriminatorField) {

        final FieldType fieldType;
        switch (fieldName) {
            case _MetaBridge.ID:
                validateSnowflakeStartTime(domainName, fieldName, field);
                fieldType = FieldType.DEFAULT;
                break;
            case _MetaBridge.CREATE_TIME:
            case _MetaBridge.UPDATE_TIME:
                assertDateTime(className, field);
                fieldType = FieldType.DEFAULT;
                break;
            case _MetaBridge.VERSION:
                assertVersionField(className, field);
                fieldType = FieldType.DEFAULT;
                break;
            case _MetaBridge.VISIBLE:
                assertVisibleField(className, field);
                fieldType = FieldType.DEFAULT;
                break;
            default: {
                if (!discriminatorField && !MetaUtils.hasText(column.comment())) {
                    noCommentError(className, field);
                }
                if (discriminatorField) {
                    fieldType = FieldType.DEFAULT;
                } else {
                    validateSnowflakeStartTime(domainName, fieldName, field);
                    fieldType = parseFieldType(domainName, field);
                }
            } // default
        } //switch
        return fieldType;
    }


    private FieldType parseFieldType(final String domainName, final VariableElement field) {
        final Mapping mapping = field.getAnnotation(Mapping.class);

        final FieldType fieldType;
        TypeMirror typeMirror;
        if (mapping == null) {
            if (isDefaultArray(field)) {
                fieldType = FieldType.ARRAY;
            } else if (MetaUtils.isCompositeField(field)) {
                fieldType = FieldType.COMPOSITE;
                validateCompositeType((TypeElement) this.types.asElement(field.asType()), this.buildInCompositeType);
            } else {
                fieldType = FieldType.DEFAULT;
            }
        } else if ((typeMirror = mappingTypeMirror(domainName, field, mapping)) == null) {
            fieldType = FieldType.DEFAULT;
        } else if (this.types.isAssignable(typeMirror, this.jsonbType)) {
            fieldType = FieldType.JSONB;
        } else if (this.types.isAssignable(typeMirror, this.jsonType)) {
            fieldType = FieldType.JSON;
        } else if (this.types.isAssignable(typeMirror, this.compositeType)) {
            if (MetaUtils.isCompositeField(field)) {
                fieldType = FieldType.COMPOSITE;
                validateCompositeType((TypeElement) this.types.asElement(field.asType()), (DeclaredType) typeMirror);
            } else {
                String m = String.format("Field %s#%s is not a composite field.", domainName, field.getSimpleName());
                addErrorMsg(m);
                fieldType = FieldType.DEFAULT;
            }
        } else if (this.types.isAssignable(typeMirror, this.arrayType)) {
            fieldType = FieldType.ARRAY;
        } else {
            fieldType = FieldType.DEFAULT;
        }

        return fieldType;
    }

    private boolean isDefaultArray(VariableElement field) {
        TypeMirror typeMirror = field.asType();

        if (!(typeMirror instanceof ArrayType)) {
            return false;
        }


        int dimension = 0;
        while (typeMirror instanceof ArrayType) {
            typeMirror = ((ArrayType) typeMirror).getComponentType();
            dimension++;
        }

        final Element element;
        if (typeMirror.getKind() == TypeKind.DECLARED) {
            element = this.types.asElement(typeMirror);
        } else {
            element = null;
        }


        final String name;
        if (element == null) {
            name = typeMirror.toString();
        } else if (element instanceof QualifiedNameable) {
            name = ((QualifiedNameable) element).getQualifiedName().toString();
        } else {
            name = element.getSimpleName().toString();
        }

        final boolean match;
        if (dimension == 1 && name.equals(byte.class.getName())) {
            match = false;  // binary
        } else if (typeMirror instanceof DeclaredType
                && ((DeclaredType) typeMirror).asElement().getKind() == ElementKind.ENUM) {
            match = true;
        } else if (MetaUtils.isCompositeType(typeMirror)) {
            match = true;
        } else {
            final Set<String> nameSet;
            nameSet = obtainDefaultUnderlyingComponentClassNameSet();
            match = nameSet.contains(name);
        }
        return match;
    }

    /// @see #validateCompositeType(TypeElement, DeclaredType)
    @Nullable
    private DeclaredType obtainCompositeMapping(final String className, final VariableElement field) {
        final Mapping mapping = field.getAnnotation(Mapping.class);

        TypeMirror typeMirror;
        if (mapping == null) {
            if (MetaUtils.isCompositeField(field)) {
                typeMirror = this.buildInCompositeType;
            } else {
                typeMirror = null;
            }
        } else if ((typeMirror = mappingTypeMirror(className, field, mapping)) == null) {
            // no-op
        } else if (!this.types.isAssignable(typeMirror, this.compositeType)) {
            typeMirror = null;
        } else if (!MetaUtils.isCompositeField(field)) {
            String m = String.format("Field %s#%s is not a composite field.", className, field.getSimpleName());
            addErrorMsg(m);
            typeMirror = null;
        }
        return (DeclaredType) typeMirror;
    }


    private void validateCompositeType(final TypeElement typeElement, final DeclaredType mappingType) {
        final String className = MetaUtils.getClassName(typeElement);

        if (!this.compositeClassNameSet.add(className)) {
            return;
        }

        final DefinedType definedType = typeElement.getAnnotation(DefinedType.class);
        assert definedType != null;

        final String typeName;
        typeName = definedType.name();


        if (_MetaBridge.isCamelCase(typeName)) {
            String m = String.format("Composite type %s name[%s] is camel.", className, typeName);
            addErrorMsg(m);
        } else if (!typeName.equals(typeName.trim())) {
            String m = String.format("Composite type %s name[%s] has leading or trailing spaces.", className, typeName);
            addErrorMsg(m);
        }

        final Predicate<VariableElement> func;
        func = field -> {

            final DeclaredType typeMirror;
            typeMirror = obtainCompositeMapping(className, field);

            if (typeMirror != null) {
                final TypeElement compositeElement;
                compositeElement = (TypeElement) this.types.asElement(field.asType());
                validateCompositeType(compositeElement, typeMirror);
            }

            return false;
        };

        final List<VariableElement> fieldList;
        fieldList = findFields(typeElement, true, func);

        if (fieldList.isEmpty()) {
            String m = String.format("Composite type %s has no fields.", className);
            addErrorMsg(m);
            return;
        }


        int errorCount = getErrorCount(), newErrorCount;

        final Map<String, Integer> orderMap;

        orderMap = createOrderMap(className, definedType);

        newErrorCount = getErrorCount();
        if (errorCount != newErrorCount) {
            return;
        }

        if (orderMap.size() != fieldList.size()) {
            String m = String.format("%s fieldOrder size %s not equal field size %s", className, orderMap.size(), fieldList.size());
            addErrorMsg(m);
        }

        fieldList.sort(Comparator.comparingInt(field -> {
            final String fieldName = field.getSimpleName().toString();
            final Integer order;
            order = orderMap.get(fieldName);
            if (order == null) {
                String m = String.format("%s.%s not in fieldOrder", className, fieldName);
                addErrorMsg(m);
            }
            return order == null ? 0 : order;
        }));

        if (newErrorCount == getErrorCount()) {
            this.compositeMetaMap.put(className, CompositeMeta.of(typeElement, (TypeElement) mappingType.asElement(), fieldList));
        }

    }

    /// @return an unmodified map
    private Map<String, Integer> createOrderMap(final String className, final DefinedType definedType) {
        final String[] array = definedType.fieldOrder();

        final Map<String, Integer> map;
        switch (array.length) {
            case 0:
                map = Map.of();
                break;
            case 1:
                map = Map.of(array[0], 0);
                break;
            default: {
                final Map<String, Integer> tempMap = ArmyCollections.hashMapForSize(array.length);
                for (int i = 0; i < array.length; i++) {
                    if (tempMap.putIfAbsent(array[i], i) != null) {
                        String m = String.format("%s fieldOrder[%s] %s duplication", className, i, array[i]);
                        addErrorMsg(m);
                    }
                }
                map = Map.copyOf(tempMap);
            } // default

        } // switch

        return map;
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


    /// @param endFunc true : end loop
    private List<VariableElement> findFields(final TypeElement targetElement, final boolean createList,
                                             final Predicate<VariableElement> endFunc) {
        TypeElement superElement = targetElement;


        final List<VariableElement> fieldList;
        final Set<String> fieldNameSet, columnNameSet;

        if (createList) {
            fieldList = new ArrayList<>(10);
            fieldNameSet = ArmyCollections.hashSetForSize(10);
            columnNameSet = ArmyCollections.hashSetForSize(10);
        } else {
            fieldList = List.of();
            fieldNameSet = Set.of();
            columnNameSet = fieldNameSet;
        }

        VariableElement field;
        String fieldName, columnName;
        Column column;

        topLoop:
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

            if (superElement != targetElement
                    && superElement.getAnnotation(MappedSuperclass.class) == null
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

                if (createList) {
                    fieldName = field.getSimpleName().toString();
                    if (fieldNameSet.add(fieldName)) {
                        fieldList.add(field);
                        columnName = getColumnName(MetaUtils.getClassName(superElement), fieldName, column);
                        if (!columnNameSet.add(columnName)) {
                            String m = String.format("%s.%s column[%s] duplication", MetaUtils.getClassName(superElement), fieldName, columnName);
                            addErrorMsg(m);
                        }
                    } else {
                        String m = String.format("Field %s.%s is overridden", MetaUtils.getClassName(superElement), fieldName);
                        addErrorMsg(m);
                    }
                }

                if (endFunc.test(field)) {
                    break topLoop;
                }

            } // field loop

        } // TypeMirror  loop

        return fieldList;
    }


    @Nullable
    private TypeMirror mappingTypeMirror(final String domainName, final VariableElement field, final Mapping mapping) {
        TypeMirror typeMirror;
        typeMirror = valueOfMappingType(field);
        if (typeMirror == null) {
            typeMirror = valueOfMappingValue(domainName, field, mapping);
        }
        return typeMirror;
    }


    @Nullable
    private TypeMirror valueOfMappingType(final VariableElement field) {

        TypeMirror typeMirror = null;

        TypeElement annoElement;

        topLoop:
        for (AnnotationMirror mirror : field.getAnnotationMirrors()) {
            annoElement = (TypeElement) mirror.getAnnotationType().asElement();
            if (!MetaUtils.getClassName(annoElement).equals(Mapping.class.getName())) {
                continue;
            }

            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {

                if (!entry.getKey().getSimpleName().toString().equals("type")) {
                    continue;
                }

                final Object value;
                value = entry.getValue().getValue();
                if (value instanceof TypeMirror) {
                    typeMirror = (DeclaredType) value;
                } else if (value == void.class) {
                    break topLoop;
                } else if (value instanceof Class<?>) {
                    typeMirror = this.elements.getTypeElement(((Class<?>) value).getName()).asType();
                }

                break topLoop;

            } // annotation value loop

        } // AnnotationMirror loop
        return typeMirror;
    }


    @Nullable
    private TypeMirror valueOfMappingValue(final String domainName, final VariableElement field, final Mapping mapping) {
        String value = mapping.value();

        if (!MetaUtils.hasText(value)) {
            return null;
        }

        if (value.startsWith("${") && value.endsWith("}")) {
            final String key;
            key = getAndClearTempBuilder()
                    .append(domainName)
                    .append('.')
                    .append(field.getSimpleName())
                    .append('.')
                    .append("Mapping")
                    .append('.')
                    .append("value")
                    .toString();

            value = getTableMetaProperties().getProperty(key);

            if (!MetaUtils.hasText(value)) {
                return null;
            }

        }

        TypeMirror typeMirror;
        final String msgFormat = "Field %s.%s mapping value %s not found.%s%s";
        try {
            final Element element;
            element = this.elements.getTypeElement(value);
            if (element == null) {
                String m = String.format(msgFormat,
                        domainName, field.getSimpleName(), value, "", "");
                addErrorMsg(m);
                typeMirror = null;
            } else {
                typeMirror = element.asType();
            }

        } catch (Exception e) {
            typeMirror = null;
            String m = String.format(msgFormat,
                    domainName, field.getSimpleName(), value, "error message:\n", e.getMessage());
            addErrorMsg(m);
        }
        return typeMirror;
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


    private void addErrorMsg(String msg) {
        List<String> list = this.errorMsgList;
        if (list == null) {
            this.errorMsgList = list = new ArrayList<>();
        }
        list.add(msg);
    }

    private boolean hasError() {
        final List<String> list = this.errorMsgList;
        return list != null && !list.isEmpty();
    }

    private int getErrorCount() {
        final List<String> list = this.errorMsgList;
        return list == null ? 0 : list.size();
    }


    private Properties getTableMetaProperties() {
        Properties properties;
        properties = this.tableMetaProperties;
        if (properties == null) {
            this.tableMetaProperties = properties = _MetaBridge.loadArmyProperties("TableMeta");
        }
        return properties;
    }

    private StringBuilder getAndClearTempBuilder() {
        final StringBuilder builder;
        builder = this.tempBuilder;
        builder.setLength(0); // clear
        return builder;
    }


    private Set<String> obtainDefaultUnderlyingComponentClassNameSet() {
        Set<String> set = this.defaultUnderlyingComponentClassNameSet;
        if (set == null) {
            this.defaultUnderlyingComponentClassNameSet = set = createDefaultUnderlyingComponentClassNameSet();
        }
        return set;
    }


    private static Set<String> createDefaultUnderlyingComponentClassNameSet() {

        final Set<String> set = ArmyCollections.hashSetForSize(20 * 4);

        set.add(String.class.getName());

        set.add(boolean.class.getName());
        set.add(Boolean.class.getName());
        set.add(int.class.getName());
        set.add(Integer.class.getName());

        set.add(long.class.getName());
        set.add(Long.class.getName());
        set.add(float.class.getName());
        set.add(Float.class.getName());

        set.add(double.class.getName());
        set.add(Double.class.getName());
        set.add(short.class.getName());
        set.add(Short.class.getName());

        set.add(byte.class.getName());
        set.add(Byte.class.getName());
        set.add(char.class.getName());
        set.add(Character.class.getName());

        set.add(java.math.BigInteger.class.getName());
        set.add(java.math.BigDecimal.class.getName());
        set.add(java.time.LocalDateTime.class.getName());
        set.add(java.time.OffsetDateTime.class.getName());

        set.add(java.time.ZonedDateTime.class.getName());
        set.add(java.time.LocalDate.class.getName());
        set.add(java.time.LocalTime.class.getName());
        set.add(java.time.OffsetTime.class.getName());

        set.add(java.time.Instant.class.getName());
        set.add(java.time.Year.class.getName());
        set.add(java.time.YearMonth.class.getName());
        set.add(java.time.MonthDay.class.getName());

        set.add(java.time.ZoneId.class.getName());
        set.add(java.util.BitSet.class.getName());
        set.add(java.util.UUID.class.getName());

        return Set.copyOf(set);
    }


}
