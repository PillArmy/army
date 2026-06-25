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
import io.army.struct.CodeEnum;
import io.army.struct.DefinedType;
import io.army.struct.TypeCategory;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import java.lang.annotation.Annotation;
import java.util.Map;

abstract class MetaUtils {

    private MetaUtils() {
        throw new UnsupportedOperationException();
    }

    static String getClassName(final TypeElement domain) {
        String className;
        className = domain.getQualifiedName().toString();
        if (className.lastIndexOf('>') > 0) {
            className = className.substring(0, className.indexOf('<'));
        }
        return className;
    }

    static String getSimpleClassName(final TypeElement domain) {
        String className;
        className = domain.getSimpleName().toString();
        if (className.lastIndexOf('>') > 0) {
            className = className.substring(0, className.indexOf('<'));
        }
        return className;
    }


    static boolean hasText(@Nullable String str) {
        boolean match = false;
        final int strLen;
        if (str != null && (strLen = str.length()) > 0) {
            for (int i = 0; i < strLen; i++) {
                if (!Character.isWhitespace(str.charAt(i))) {
                    match = true;
                    break;
                }
            }
        }
        return match;
    }


    static boolean isEnum(VariableElement field) {
        final TypeMirror typeMirror = field.asType();
        return typeMirror instanceof DeclaredType
                && ((DeclaredType) typeMirror).asElement().getKind() == ElementKind.ENUM;
    }

    static boolean isComposite(VariableElement field) {
        final DefinedType definedType = field.getAnnotation(DefinedType.class);
        return definedType != null && definedType.category() == TypeCategory.COMPOSITE;
    }

    static boolean isCompositeMapping(VariableElement field) {

        Mapping mapping = field.getAnnotation(Mapping.class);


        return false;
    }

    @Nullable
    static TypeMirror typeMirror(final Element element, final Class<? extends Annotation> annoClass,
                                 final String methodName, final Elements elements) {


        final String annoClassName = annoClass.getName();

        TypeMirror typeMirror = null;

        TypeElement annoElement;

        topLoop:
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            annoElement = (TypeElement) mirror.getAnnotationType().asElement();
            if (!MetaUtils.getClassName(annoElement).equals(annoClassName)) {
                continue;
            }
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : mirror.getElementValues().entrySet()) {
                if (!entry.getKey().getSimpleName().toString().equals(methodName)) {
                    continue;
                }

                final Object value;
                value = entry.getValue().getValue();
                if (value instanceof Class<?>) {
                    typeMirror = elements.getTypeElement(((Class<?>) value).getName()).asType();
                } else if (value instanceof DeclaredType && ((DeclaredType) value).getKind() == TypeKind.DECLARED) {
                    typeMirror = (DeclaredType) value;
                }
                break topLoop;
            } // annotation value loop

        } // AnnotationMirror loop
        return typeMirror;
    }


    static boolean isCodeEnumType(final TypeElement typeElement) {
        final String codeEnum = CodeEnum.class.getName();
        boolean match = false;
        Element element;
        for (TypeMirror mirror : typeElement.getInterfaces()) {
            if (codeEnum.equals(mirror.toString())) {
                match = true;
                break;
            }
            if (!(mirror instanceof DeclaredType)) {
                continue;
            }
            element = ((DeclaredType) mirror).asElement();
            if (element.getKind() != ElementKind.INTERFACE) {
                continue;
            }
            if (isCodeEnumType((TypeElement) element)) {
                match = true;
                break;
            }
        }
        return match;
    }


}
