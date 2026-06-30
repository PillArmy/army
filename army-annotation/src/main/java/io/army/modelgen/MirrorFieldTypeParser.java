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

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

final class MirrorFieldTypeParser extends AbstractFieldTypeParser implements FieldTypeParser {

    static MirrorFieldTypeParser create(final Context context) {
        return new MirrorFieldTypeParser(context);
    }


    private final Elements elements;

    private final DeclaredType jsonbType;

    private final DeclaredType jsonType;

    private final DeclaredType compositeType;

    private final DeclaredType arrayType;

    private final DeclaredType xmlType;

    private final DeclaredType buildInCompositeType;


    private MirrorFieldTypeParser(Context context) {
        super(context);

        this.elements = context.env.getElementUtils();

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
        fieldList = createCompositeFieldList(typeElement, className, func);

        if (this.errorCountSupplier.getAsInt() == 0) {
            final String mappingTypeName;
            mappingTypeName = MetaUtils.getClassName((TypeElement) mappingType.asElement());

            this.compositeMetaMap.put(className, CompositeMeta.of(typeElement, mappingTypeName, fieldList));
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
        final String value;
        value = getMappingValue(domainName, field, mapping);
        if (value == null) {
            return null;
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
