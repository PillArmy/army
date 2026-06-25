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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;

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

    private final ProcessingEnvironment env;

    private final StringBuilder tempBuilder;

    private final Options options;

    private final DiscriminatorHandler discriminatorHandler;


    private final Map<String, Map<String, VariableElement>> parentFieldCache = new HashMap<>();

    private final Map<String, Map<String, TypeElement>> discriminatorValueCache = new HashMap<>();

    private final Map<String, MappingMode> mappingModeMap = new HashMap<>();

    private final Map<String, Set<String>> startTimeToDomain = new HashMap<>();


    private TypeElement currentDomainElement;

    private String currentDomainName;

    private TypeElement currentParent;

    private List<String> errorMsgList;


    private TableAnnotationHandlerImpl(ProcessingEnvironment env, Options options, StringBuilder tempBuilder) {
        this.env = env;
        this.options = options;
        this.tempBuilder = tempBuilder;

        this.discriminatorHandler = DiscriminatorHandlerImpl.create(this::addErrorMsg);
    }

    @Override
    public Pair handle(final TypeElement domainElement) {
        this.currentDomainElement = domainElement;
        this.currentDomainName = MetaUtils.getClassName(domainElement);

        this.currentParent = null;  // clear
        final List<TypeElement> mappedList;
        mappedList = createMappedList(domainElement);

        return null;
    }


    private List<FieldMeta> createFieldList(final TypeElement targetElement, final TypeElement[] holder) {
        TypeElement superElement = targetElement;

        final List<FieldMeta> fieldList = new ArrayList<>(20), tempFieldList = new ArrayList<>(20);

        final Inheritance inheritance;
        inheritance = targetElement.getAnnotation(Inheritance.class);
        final String discriminatorFieldName = inheritance == null ? null : inheritance.value();
        final String domainName = MetaUtils.getClassName(targetElement);

        Column column;
        VariableElement field;
        String className, fieldName, columnName;
        boolean discriminatorField, foundDiscriminatorColumn = false;

        final Set<String> columnNamSet = ArmyCollections.hashSetForSize(20);

        for (TypeMirror superMirror = superElement.asType(); !(superMirror instanceof NoType); superMirror = superElement.getSuperclass()) {

            superElement = (TypeElement) ((DeclaredType) superMirror).asElement();
            if (superElement.getNestingKind() != NestingKind.TOP_LEVEL) {
                String m = String.format("Nested class %s is not supported.", MetaUtils.getClassName(superElement));
                addErrorMsg(m);
                break;
            }
            if (superElement.getAnnotation(Inheritance.class) != null) {
                if (targetElement.getAnnotation(Inheritance.class) != null) {
                    addErrorInheritance(targetElement, superElement);
                }
                field = findIdFieldFromParent(superElement);
                if (field != null) {
                    fieldList.add(FieldMeta.of(field, null));
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

                // get column name
                columnName = getColumnName(className, fieldName, column);
                if (!columnName.startsWith("${") && !columnNamSet.add(columnName)) {
                    String m = String.format("Field %s.%s column[%s] duplication.", className, fieldName, columnName);
                    addErrorMsg(m);
                }

                discriminatorField = discriminatorFieldName != null && discriminatorFieldName.equals(fieldName);
                if (discriminatorField) {
                    foundDiscriminatorColumn = true;
                    this.discriminatorHandler.handle(domainName, field);
                }

                validateField(domainName, className, fieldName, field, column, discriminatorField);

                tempFieldList.add(FieldMeta.of(field, null));

            }// for getEnclosedElements

            Collections.reverse(tempFieldList);
            fieldList.addAll(tempFieldList);
            tempFieldList.clear();

        } // superElement loop

        if (inheritance != null && !foundDiscriminatorColumn) {
            String m = String.format("Domain %s discriminator field[%s] not found.",
                    domainName, discriminatorFieldName);
            addErrorMsg(m);
        }

        columnNamSet.clear();


        return List.copyOf(fieldList);
    }


    @Nullable
    private VariableElement findIdFieldFromParent(final TypeElement targetElement) {
        TypeElement superElement = targetElement;

        VariableElement field = null;

        topLoop:
        for (TypeMirror superMirror = superElement.asType(); !(superMirror instanceof NoType); superMirror = superElement.getSuperclass()) {

            superElement = (TypeElement) ((DeclaredType) superMirror).asElement();

            if (superElement.getNestingKind() != NestingKind.TOP_LEVEL) {
                String m = String.format("Nested class %s is not supported.", MetaUtils.getClassName(superElement));
                addErrorMsg(m);
                break;
            }

            if (superElement.getAnnotation(MappedSuperclass.class) == null
                    && superElement.getAnnotation(Table.class) == null) {
                break;
            }

            for (Element element : superElement.getEnclosedElements()) {

                if (element.getKind() != ElementKind.FIELD
                        || element.getModifiers().contains(Modifier.STATIC)
                        || element.getAnnotation(Column.class) == null) {
                    continue;
                }

                field = (VariableElement) element;
                if (field.getSimpleName().toString().equals(_MetaBridge.ID)) {
                    break topLoop;
                }

            } // field loop

        } // loop

        if (field == null) {
            String m = String.format("Domain %s don't definite field %s.", MetaUtils.getClassName(targetElement), _MetaBridge.ID);
            addErrorMsg(m);
        }
        return field;
    }


    private Map<String, VariableElement> createFieldMap(final TypeElement tableElement, final List<VariableElement> fieldList,
                                                        final @Nullable Map<String, VariableElement> parentFieldMap) {

        final String domainName = MetaUtils.getClassName(tableElement);

        final Map<String, VariableElement> map = ArmyCollections.hashMapForSize(fieldList.size());
        String fieldName;
        VariableElement oldValue;
        boolean fieldOverridden;
        for (VariableElement field : fieldList) {

            fieldName = field.getSimpleName().toString();

            oldValue = map.put(fieldName, field);

            fieldOverridden = oldValue != null;
            if (!fieldOverridden
                    && !fieldName.equals(_MetaBridge.ID)
                    && parentFieldMap != null
                    && parentFieldMap.containsKey(fieldName)) {
                fieldOverridden = true;
            }

            if (fieldOverridden) {
                addErrorMsg(String.format("Field %s.%s is overridden.", domainName, fieldName));
            }

        } // loop

        if (parentFieldMap == null) {
            String m;
            if (!map.containsKey(_MetaBridge.ID)) {
                m = String.format("Domain %s don't definite field %s."
                        , domainName, _MetaBridge.ID);
                addErrorMsg(m);
            }
            if (!map.containsKey(_MetaBridge.CREATE_TIME)) {
                m = String.format("Domain %s don't definite field %s."
                        , domainName, _MetaBridge.CREATE_TIME);
                addErrorMsg(m);
            }
            final Table table = tableElement.getAnnotation(Table.class);
            assert table != null;
            if (!table.immutable() && !map.containsKey(_MetaBridge.UPDATE_TIME)) {
                m = String.format("Domain %s don't definite field %s."
                        , domainName, _MetaBridge.UPDATE_TIME);
                addErrorMsg(m);
            }
        }


        final OverrideParams overrideParams = tableElement.getAnnotation(OverrideParams.class);
        if (overrideParams != null) {
            createOverrideParamMap(overrideParams, domainName, map::get);
        }
        return Map.copyOf(map);
    }

    private Map<String, VariableElement> createFieldMap(final List<TypeElement> mappedList,
                                                        final @Nullable Map<String, VariableElement> parentFieldMap) {

        final TypeElement tableElement;
        tableElement = mappedList.getFirst();
        Asserts.isTrue(tableElement == this.currentDomainElement, "");
        final String domainName = this.currentDomainName;


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

        final Map<String, VariableElement> fieldMap = ArmyCollections.hashMapForSize(20);
        final Set<String> columnNamSet = ArmyCollections.hashSetForSize(20);
        final List<VariableElement> fieldList = new ArrayList<>(20);

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
                    addErrorMsg(String.format("Field %s.%s is overridden.", className, fieldName));
                }
                // get column name
                columnName = getColumnName(className, fieldName, column);
                if (!columnName.startsWith("${") && !columnNamSet.add(columnName)) {
                    String m = String.format("Field %s.%s column[%s] duplication.", className, fieldName, columnName);
                    addErrorMsg(m);
                }
                if (discriminatorField != null && discriminatorField.equals(fieldName)) {
                    foundDiscriminatorColumn = true;
                    assertDiscriminatorEnum(domainName, className, field);
                    validateField(domainName, className, fieldName, field, column, true);
                } else {
                    validateField(domainName, className, fieldName, field, column, false);
                }

                fieldList.add(field);

            }// for getEnclosedElements

        } // class loop


        if (inheritance != null && !foundDiscriminatorColumn) {
            String m = String.format("Domain %s discriminator field[%s] not found.",
                    domainName, discriminatorField);
            addErrorMsg(m);
        }

        columnNamSet.clear();

        if (parentFieldMap == null) {
            String m;
            if (!fieldMap.containsKey(_MetaBridge.ID)) {
                m = String.format("Domain %s don't definite field %s."
                        , domainName, _MetaBridge.ID);
                addErrorMsg(m);
            }
            if (!fieldMap.containsKey(_MetaBridge.CREATE_TIME)) {
                m = String.format("Domain %s don't definite field %s."
                        , domainName, _MetaBridge.CREATE_TIME);
                addErrorMsg(m);
            }
            if (!table.immutable() && !fieldMap.containsKey(_MetaBridge.UPDATE_TIME)) {
                m = String.format("Domain %s don't definite field %s."
                        , domainName, _MetaBridge.UPDATE_TIME);
                addErrorMsg(m);
            }
        } else {
            field = parentFieldMap.get(_MetaBridge.ID);
            Asserts.notNull(field, "");
            fieldMap.put(_MetaBridge.ID, field);
        }

        final OverrideParams overrideParams = tableElement.getAnnotation(OverrideParams.class);
        if (overrideParams != null) {
            createOverrideParamMap(overrideParams, domainName, fieldMap::get);
        }

        return Collections.unmodifiableMap(fieldMap);
    }


    private List<TypeElement> createMappedList(final TypeElement tableElement) {
        TypeElement superElement = tableElement;
        List<TypeElement> mappedList = null;
        for (TypeMirror superMirror = superElement.getSuperclass(); !(superMirror instanceof NoType); superMirror = superElement.getSuperclass()) {

            superElement = (TypeElement) ((DeclaredType) superMirror).asElement();
            if (superElement.getNestingKind() != NestingKind.TOP_LEVEL) {
                break;
            }
            if (superElement.getAnnotation(Inheritance.class) != null) {
                if (tableElement.getAnnotation(Inheritance.class) != null) {
                    addErrorInheritance(tableElement, superElement);
                }
                this.currentParent = superElement;
                break;
            }
            if (superElement.getAnnotation(MappedSuperclass.class) == null
                    && superElement.getAnnotation(Table.class) == null) {
                break;
            }
            if (mappedList == null) {
                mappedList = new ArrayList<>();
                mappedList.add(tableElement);
            }
            mappedList.add(superElement);
        } // loop

        if (mappedList == null) {
            mappedList = List.of(tableElement);
        } else {
            mappedList = Collections.unmodifiableList(mappedList);
        }

        return mappedList;
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


    private void validateField(final String domainName, final String className, final String fieldName,
                               final VariableElement field, final Column column, final boolean discriminatorField) {
        switch (fieldName) {
            case _MetaBridge.ID:
                validateSnowflakeStartTime(domainName, fieldName, field);
                validateCompositeTypeMapping(className, fieldName, field);
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
                    validateCompositeTypeMapping(className, fieldName, field);
                }
            } // default
        } //switch
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


    private void validateCompositeTypeMapping(final String className, final String fieldName,
                                              final VariableElement field) {
        final String compositeType = "io.army.mapping.CompositeType";
        final Mapping mapping = field.getAnnotation(Mapping.class);
        if (mapping == null) {
            return;
        }
        final String mappingTypeName = mappingTypeValue(field);
        if (mappingTypeName.equals(void.class.getName())) {
            if (!compositeType.equals(mapping.value())) {
                return;
            }
        } else if (!compositeType.equals(mappingTypeName)) {
            return;
        }

        final TypeMirror fieldTypeMirror = field.asType();
        final Element beanElement;
        if (!(fieldTypeMirror instanceof DeclaredType)
                || (beanElement = ((DeclaredType) fieldTypeMirror).asElement()).getKind() != ElementKind.CLASS) {
            String m = String.format("Field %s.%s is annotated with @Mapping(\"%s\")," +
                            " but its Java type is not a class.",
                    className, fieldName, compositeType);
            addErrorMsg(m);
            return;
        }

        final TypeElement pojoTypeElement = (TypeElement) beanElement;
        final String pojoTypeName = MetaUtils.getClassName(pojoTypeElement);

        final DefinedType definedType = beanElement.getAnnotation(DefinedType.class);
        if (definedType == null) {
            String m = String.format("Field %s.%s is annotated with @Mapping(\"%s\")," +
                            " but its Java type[%s] isn't annotated with @%s.",
                    className, fieldName, compositeType, MetaUtils.getClassName(pojoTypeElement),
                    DefinedType.class.getName());
            addErrorMsg(m);
            return;
        }
        final String definedTypeName = definedType.name();
        if (_MetaBridge.isCamelCase(definedTypeName)) {
            String m = String.format("Pojo[%s] : @%s name()[%s] must not be camelCase.",
                    pojoTypeName, DefinedType.class.getSimpleName(), definedTypeName);
            addErrorMsg(m);
        }


        final Map<String, Boolean> fieldMap = new HashMap<>();
        TypeElement superElement;
        for (TypeMirror superMirror = fieldTypeMirror; ; superMirror = superElement.getSuperclass()) {
            if (superMirror.getKind() == TypeKind.NONE) {
                break;
            }

            superElement = (TypeElement) ((DeclaredType) superMirror).asElement();

            if (superMirror != fieldTypeMirror && superElement.getAnnotation(MappedSuperclass.class) == null) {
                break;
            }

            for (Element e : superElement.getEnclosedElements()) {
                if (e.getKind() != ElementKind.FIELD || e.getModifiers().contains(Modifier.STATIC)) {
                    continue;
                }
                if (fieldMap.putIfAbsent(e.getSimpleName().toString(), Boolean.TRUE) != null) {
                    String m = String.format("%s.%s duplicate", pojoTypeName, e.getSimpleName());
                    addErrorMsg(m);
                }

            } // bean type field  loop

        } // super class loop

        final int fieldCount = fieldMap.size();
        final String[] orderArray;
        if (fieldCount == 0) {
            String m = String.format("Pojo[%s] is annotated with @%s,but it has no @%s field.",
                    pojoTypeName, DefinedType.class.getSimpleName(), Column.class.getSimpleName());
            addErrorMsg(m);
        } else if (fieldCount != (orderArray = definedType.fieldOrder()).length) {
            String m = String.format("Pojo[%s] is annotated with @%s and is mapped to %s, but fieldOrder().lentth[%s] and field count[%s] not match.",
                    pojoTypeName, DefinedType.class.getSimpleName(), compositeType, orderArray.length, fieldCount);
            addErrorMsg(m);
        } else for (String s : orderArray) {
            if (fieldMap.containsKey(s)) {
                continue;
            }
            String m = String.format("Pojo[%s] is annotated with @%s and is mapped to %s, but fieldOrder()'s element[%s] isn't field",
                    pojoTypeName, DefinedType.class.getSimpleName(), compositeType, s);
            addErrorMsg(m);
        }


    }

    private String mappingTypeValue(final VariableElement field) {

        TypeElement annoElement;
        String typeValue = void.class.getName();
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
                if (value instanceof Class<?>) {
                    typeValue = ((Class<?>) value).getName();
                } else if (value instanceof DeclaredType && ((DeclaredType) value).getKind() == TypeKind.DECLARED) {
                    typeValue = MetaUtils.getClassName(((TypeElement) ((DeclaredType) value).asElement()));
                }
                break topLoop;
            } // annotation value loop

        } // AnnotationMirror loop
        return typeValue;
    }

    private void discriminatorNonCodeNum(final String className, final VariableElement field) {
        String m = String.format("Discriminator field %s.%s isn't enum."
                , className, field.getSimpleName());
        addErrorMsg(m);
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


}
