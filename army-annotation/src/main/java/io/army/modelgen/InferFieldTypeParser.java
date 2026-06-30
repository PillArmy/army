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

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.function.Predicate;

final class InferFieldTypeParser extends AbstractFieldTypeParser implements FieldTypeParser {

    static InferFieldTypeParser create(Context context) {
        return new InferFieldTypeParser(context);
    }

    static final String COMPOSITE_TYPE = "io.army.mapping.CompositeType";


    private InferFieldTypeParser(Context context) {
        super(context);
    }


    @Override
    public FieldType parseType(final String domainName, final VariableElement field) {
        final Mapping mapping = field.getAnnotation(Mapping.class);
        final String value;
        final int length;

        final FieldType fieldType;
        if (mapping == null) {
            if (isDefaultArray(field)) {
                fieldType = FieldType.ARRAY;
            } else if (MetaUtils.isCompositeField(field)) {
                fieldType = FieldType.COMPOSITE;
                validateCompositeType((TypeElement) this.types.asElement(field.asType()), COMPOSITE_TYPE);
            } else {
                fieldType = FieldType.DEFAULT;
            }
        } else if ((value = getMappingValue(domainName, field, mapping)) == null) {
            fieldType = FieldType.DEFAULT;
        } else if (value.equals("io.army.mapping.JsonbType")
                || value.equals("io.army.mapping.PreferredJsonbType")
                || (length = value.length()) > 9 && value.endsWith("JsonbType")) {
            fieldType = FieldType.JSONB;
        } else if (value.equals("io.army.mapping.JsonType")
                || (length > 8 && value.endsWith("JsonType"))) {
            fieldType = FieldType.JSON;
        } else if (length > 9 && value.endsWith("ArrayType")) {
            fieldType = FieldType.ARRAY;
        } else if (value.equals(COMPOSITE_TYPE)
                || (length > COMPOSITE_TYPE.length() && value.endsWith(COMPOSITE_TYPE))) {

            if (MetaUtils.isCompositeField(field)) {
                fieldType = FieldType.COMPOSITE;
                validateCompositeType((TypeElement) this.types.asElement(field.asType()), value);
            } else {
                String m = String.format("Field %s#%s is not a composite field.", domainName, field.getSimpleName());
                this.errorMsgConsumer.accept(m);
                fieldType = FieldType.DEFAULT;
            }
        } else if (value.equals("io.army.mapping.XmlType")
                || (length > 7 && value.endsWith("XmlType"))) {
            fieldType = FieldType.XML;
        } else {
            fieldType = FieldType.DEFAULT;
        }
        return fieldType;
    }


    private void validateCompositeType(final TypeElement typeElement, final String mappingTypeName) {
        final String className = MetaUtils.getClassName(typeElement);

        if (!this.compositeClassNameSet.add(className)) {
            return;
        }


        final Predicate<VariableElement> func;
        func = field -> {

            final String mappingType;
            mappingType = obtainCompositeMapping(className, field);

            if (mappingType != null) {
                final TypeElement compositeElement;
                compositeElement = (TypeElement) this.types.asElement(field.asType());
                validateCompositeType(compositeElement, mappingType);
            }

            return false;
        };

        final List<VariableElement> fieldList;
        fieldList = createCompositeFieldList(typeElement, className, func);

        if (this.errorCountSupplier.getAsInt() == 0) {
            this.compositeMetaMap.put(className, CompositeMeta.of(typeElement, mappingTypeName, fieldList));
        }

    }


    @Nullable
    private String obtainCompositeMapping(final String className, final VariableElement field) {
        final Mapping mapping = field.getAnnotation(Mapping.class);

        final String mappingValue;
        final String finalValue;
        if (mapping == null) {
            if (MetaUtils.isCompositeField(field)) {
                finalValue = COMPOSITE_TYPE;
            } else {
                finalValue = null;
            }
        } else if (((mappingValue = getMappingValue(className, field, mapping))) == null) {
            finalValue = null;
        } else if (mappingValue.equals(COMPOSITE_TYPE)
                || (mappingValue.length() > COMPOSITE_TYPE.length() && mappingValue.endsWith(COMPOSITE_TYPE))) {
            if (MetaUtils.isCompositeField(field)) {
                finalValue = mappingValue;
            } else {
                finalValue = null;
                String m = String.format("Field %s#%s is not a composite field.", className, field.getSimpleName());
                this.errorMsgConsumer.accept(m);
            }
        } else {
            finalValue = null;
        }
        return finalValue;
    }


}
