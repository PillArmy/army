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
import javax.lang.model.util.Types;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

abstract class AbstractFieldTypeParser extends FieldHandlerSupport implements FieldTypeParser {

    final Types types;

    final Consumer<String> errorMsgConsumer;

    final IntSupplier errorCountSupplier;

    final Set<String> compositeClassNameSet = new HashSet<>();

    final Map<String, CompositeMeta> compositeMetaMap = new HashMap<>();

    private Set<String> defaultUnderlyingComponentClassNameSet;

    AbstractFieldTypeParser(Context context) {
        super(context.tempBuilder);
        this.types = context.env.getTypeUtils();

        this.errorMsgConsumer = context.errorMsgConsumer;
        this.errorCountSupplier = context.errorCountSupplier;
    }


    @Override
    public final List<Pair> compositeSourceList(StringBuilder builder) {
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
    final void addErrorMsg(String msg) {
        this.errorMsgConsumer.accept(msg);
    }


    final boolean isDefaultArray(VariableElement field) {
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

    @Nullable
    final String getMappingValue(String className, VariableElement field, Mapping mapping) {
        String value = mapping.value();

        if (!MetaUtils.hasText(value)) {
            return null;
        }

        if (value.startsWith("${") && value.endsWith("}")) {
            final String key;
            key = getAndClearTempBuilder()
                    .append(className)
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

        return value;
    }


    final List<VariableElement> createCompositeFieldList(final TypeElement typeElement, final String className, final Predicate<VariableElement> func) {

        final List<VariableElement> fieldList;
        fieldList = findFields(typeElement, true, func);

        final DefinedType definedType;
        definedType = getDefinedTypeAndValidate(typeElement, className);

        if (fieldList.isEmpty()) {
            String m = String.format("Composite type %s has no fields.", className);
            this.errorMsgConsumer.accept(m);
            return List.of();
        }


        int errorCount = this.errorCountSupplier.getAsInt(), newErrorCount;

        final Map<String, Integer> orderMap;
        orderMap = createOrderMap(className, definedType);

        newErrorCount = this.errorCountSupplier.getAsInt();
        if (errorCount != newErrorCount) {
            return List.of();
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

        final List<VariableElement> finalList;
        if (this.errorCountSupplier.getAsInt() == 0) {
            finalList = List.copyOf(fieldList);
        } else {
            finalList = List.of();
        }
        return finalList;
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


    private DefinedType getDefinedTypeAndValidate(TypeElement typeElement, String className) {
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
        return definedType;
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
