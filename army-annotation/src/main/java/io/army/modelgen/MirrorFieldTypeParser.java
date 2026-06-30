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

import io.army.annotation.Mapping;
import io.army.lang.Nullable;
import io.army.struct.DefinedType;

import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

final class MirrorFieldTypeParser extends FieldHandlerSupport implements FieldTypeParser {

    static FieldTypeParser create(final Context context) {
        return new MirrorFieldTypeParser(context);
    }


    private final Elements elements;

    private final Types types;

    private final Consumer<String> errorMsgConsumer;

    private final IntSupplier errorCountSupplier;

    private final DeclaredType jsonbType;

    private final DeclaredType jsonType;

    private final DeclaredType compositeType;

    private final DeclaredType arrayType;

    private final DeclaredType xmlType;

    private final DeclaredType buildInCompositeType;

    private final Set<String> compositeClassNameSet = new HashSet<>();

    private final Map<String, CompositeMeta> compositeMetaMap = new HashMap<>();

    private Set<String> defaultUnderlyingComponentClassNameSet;

    private MirrorFieldTypeParser(Context context) {
        super(context.tempBuilder);

        this.elements = context.env.getElementUtils();
        this.types = context.env.getTypeUtils();
        this.errorMsgConsumer = context.errorMsgConsumer;
        this.errorCountSupplier = context.errorCountSupplier;

        this.jsonbType = getDeclaredType(this.elements, "io.army.mapping.JsonbMappingType");
        this.jsonType = getDeclaredType(this.elements, "io.army.mapping.JsonMappingType");
        this.compositeType = getDeclaredType(this.elements, "io.army.mapping.CompositeMappingType");
        this.arrayType = getDeclaredType(this.elements, "io.army.mapping.ArrayMappingType");

        this.xmlType = getDeclaredType(this.elements, "io.army.mapping.XmlMappingType");
        this.buildInCompositeType = getDeclaredType(this.elements, "io.army.mapping.CompositeType");
    }


    @Override
    public FieldType parseType(final String domainName, final VariableElement field) {
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
                this.errorMsgConsumer.accept(m);
                fieldType = FieldType.DEFAULT;
            }
        } else if (this.types.isAssignable(typeMirror, this.arrayType)) {
            fieldType = FieldType.ARRAY;
        } else if (this.types.isAssignable(typeMirror, this.xmlType)) {
            fieldType = FieldType.XML;
        } else {
            fieldType = FieldType.DEFAULT;
        }

        return fieldType;
    }

    @Override
    public List<Pair> compositeSourceList(final StringBuilder builder) {
        final Map<String, CompositeMeta> map = this.compositeMetaMap;
        if (map.isEmpty()) {
            return List.of();
        }

        final CompositeSourceCodeGen codeGen;
        codeGen = CompositeSourceCodeGenImpl.create(builder);

        final List<Pair> pairList = new ArrayList<>(map.size());
        for (CompositeMeta meta : map.values()) {
            pairList.add(codeGen.generateSource(meta));
        }
        return pairList;
    }

    @Override
    void addErrorMsg(String msg) {
        this.errorMsgConsumer.accept(msg);
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
            this.errorMsgConsumer.accept(m);
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
            this.errorMsgConsumer.accept(m);
        } else if (!typeName.equals(typeName.trim())) {
            String m = String.format("Composite type %s name[%s] has leading or trailing spaces.", className, typeName);
            this.errorMsgConsumer.accept(m);
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
            this.errorMsgConsumer.accept(m);
            return;
        }


        int errorCount = this.errorCountSupplier.getAsInt(), newErrorCount;

        final Map<String, Integer> orderMap;

        orderMap = createOrderMap(className, definedType);

        newErrorCount = this.errorCountSupplier.getAsInt();
        if (errorCount != newErrorCount) {
            return;
        }

        if (orderMap.size() != fieldList.size()) {
            String m = String.format("%s fieldOrder size %s not equal field size %s", className, orderMap.size(), fieldList.size());
            this.errorMsgConsumer.accept(m);
        }

        fieldList.sort(Comparator.comparingInt(field -> {
            final String fieldName = field.getSimpleName().toString();
            final Integer order;
            order = orderMap.get(fieldName);
            if (order == null) {
                String m = String.format("%s.%s not in fieldOrder", className, fieldName);
                this.errorMsgConsumer.accept(m);
            }
            return order == null ? 0 : order;
        }));

        if (newErrorCount == this.errorCountSupplier.getAsInt()) {
            this.compositeMetaMap.put(className, CompositeMeta.of(typeElement, (TypeElement) mappingType.asElement(), fieldList));
        }

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

            value = getTableMetaMap().get(key);

            if (!MetaUtils.hasText(value)) {
                return null;
            }
            value = value.trim();

        }

        TypeMirror typeMirror;
        final String msgFormat = "Field %s.%s mapping value %s not found.%s%s";
        try {
            final Element element;
            element = this.elements.getTypeElement(value);
            if (element == null) {
                String m = String.format(msgFormat,
                        domainName, field.getSimpleName(), value, "", "");
                this.errorMsgConsumer.accept(m);
                typeMirror = null;
            } else {
                typeMirror = element.asType();
            }

        } catch (Exception e) {
            typeMirror = null;
            String m = String.format(msgFormat,
                    domainName, field.getSimpleName(), value, "error message:\n", e.getMessage());
            this.errorMsgConsumer.accept(m);
        }
        return typeMirror;
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


    private static DeclaredType getDeclaredType(Elements elements, String className) {
        final Element element;
        element = elements.getTypeElement(className);
        if (element == null) {
            String m = String.format("%s not in classpath", className);
            throw new IllegalArgumentException(m);
        }
        return (DeclaredType) element.asType();
    }


}
