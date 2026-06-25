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

import javax.lang.model.element.VariableElement;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

final class FieldHandlerImpl implements FieldHandler {

    private final Function<String, MappingMode> mappingModeFunc;

    private final Consumer<String> errorMsgConsumer;

    private final Map<String, Set<String>> startTimeToDomain = new HashMap<>();

    private FieldHandlerImpl(Function<String, MappingMode> mappingModeFunc, Consumer<String> errorMsgConsumer) {
        this.mappingModeFunc = mappingModeFunc;
        this.errorMsgConsumer = errorMsgConsumer;
    }

    @Override
    public FieldMeta handle(final String domainName, final VariableElement field) {
        final String fieldName = field.getSimpleName().toString();

        final FieldType fieldType;
        switch (fieldName) {
            case _MetaBridge.ID:
                validateSnowflakeStartTime(domainName, fieldName, field);
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
                final Column column = field.getAnnotation(Column.class);
                assert column != null;

                final boolean enumType;
                enumType = MetaUtils.isEnum(field);
                if (!enumType && !MetaUtils.hasText(column.comment())) {
                    noCommentError(domainName, field);
                }
                if (!enumType) {
                    validateSnowflakeStartTime(domainName, fieldName, field);
                }

                final Mapping mapping = field.getAnnotation(Mapping.class);


            } // default
        } //switch

        return null;
    }

    private void validateSnowflakeStartTime(final String domainName, final String fieldName, final VariableElement field) {
        final Generator generator;
        if ((generator = field.getAnnotation(Generator.class)) == null
                || generator.type() == GeneratorType.POST
                || !generator.value().equals("io.army.generator.snowflake.Snowflake8Generator")) {
            return;
        }
        if (_MetaBridge.ID.equals(fieldName)
                && this.mappingModeFunc.apply(domainName) == MappingMode.CHILD) {
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

    private void assertDateTime(final String domainName, final VariableElement field) {
        final String fieldJavaClassName;
        fieldJavaClassName = field.asType().toString();
        if (!(fieldJavaClassName.equals(LocalDateTime.class.getName())
                || fieldJavaClassName.equals(OffsetDateTime.class.getName())
                || fieldJavaClassName.equals(ZonedDateTime.class.getName()))) {
            String m;
            m = String.format("Field %s.%s support only below java type:\n%s\n%s\n%s.",
                    domainName, field.getSimpleName(),
                    LocalDateTime.class.getName(),
                    OffsetDateTime.class.getName(),
                    ZonedDateTime.class.getName()
            );
            this.errorMsgConsumer.accept(m);
        }

    }

    private void assertVersionField(final String domainName, final VariableElement field) {
        final String fieldJavaClassName;
        fieldJavaClassName = field.asType().toString();
        if (!(fieldJavaClassName.equals(Integer.class.getName())
                || fieldJavaClassName.equals(int.class.getName())
                || fieldJavaClassName.equals(Long.class.getName())
                || fieldJavaClassName.equals(long.class.getName())
                || fieldJavaClassName.equals(BigInteger.class.getName()))) {
            String m;
            m = String.format("Field %s.%s support only below java type:\n%s\n%s\n%s.",
                    domainName, field.getSimpleName(),
                    Integer.class.getName(),
                    Long.class.getName(),
                    BigInteger.class.getName()
            );
            this.errorMsgConsumer.accept(m);
        }
    }

    private void assertVisibleField(final String domainName, final VariableElement field) {
        final String fieldJavaClassName;
        fieldJavaClassName = field.asType().toString();
        if (!(fieldJavaClassName.equals(Boolean.class.getName())
                || fieldJavaClassName.equals(boolean.class.getName()))) {
            String m;
            m = String.format("Field %s.%s support only %s.",
                    domainName, field.getSimpleName(),
                    Boolean.class.getName()
            );
            this.errorMsgConsumer.accept(m);
        }
    }

    private void noCommentError(final String className, final VariableElement field) {
        String m = String.format("Field %s.%s isn't reserved field or discriminator field,so comment must have text."
                , className, field.getSimpleName());
        this.errorMsgConsumer.accept(m);
    }


}
