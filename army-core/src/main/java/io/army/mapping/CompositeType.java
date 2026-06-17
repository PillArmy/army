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


package io.army.mapping;

import io.army.criteria.CriteriaException;
import io.army.criteria.impl.CompositeFieldFactory;
import io.army.dialect.LiteralHandler;
import io.army.dialect.UnsupportedDialectException;
import io.army.dialect._Constant;
import io.army.executor.DataAccessException;
import io.army.function.DecodeLiteralFunc;
import io.army.lang.Nullable;
import io.army.mapping.array.CompositeArrayType;
import io.army.meta.CompositeField;
import io.army.meta.ServerMeta;
import io.army.pojo.ObjectAccessor;
import io.army.pojo.ObjectAccessorFactory;
import io.army.serialize.RecordDeserializer;
import io.army.sqltype.ArmyType;
import io.army.sqltype.CustomType;
import io.army.sqltype.DataType;
import io.army.struct.DefinedType;
import io.army.struct.TypeCategory;
import io.army.util.*;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/// Mapping the pojo annotated by {@link DefinedType} to sql composite type
///
/// @see <a href="https://www.postgresql.org/docs/current/rowtypes.html">Composite Types</a>
public final class CompositeType extends _ArmyBuildInType implements MappingType.SqlComposite {

    public static CompositeType from(final Class<?> javaType) {
        final DefinedType definedType = javaType.getAnnotation(DefinedType.class);
        if (definedType == null
                || definedType.category() != TypeCategory.COMPOSITE
                || definedType.fieldOrder().length == 0) {
            throw errorJavaType(CompositeType.class, javaType);
        } else if (javaType.getEnclosingClass() != null) {
            throw errorJavaType(CompositeType.class, javaType);
        }
        return CLASS_VALUE.get(javaType);
    }


    public static final RecordDeserializer PG_DESERIALIZER = RecordDeserializer.builder()
            .name("PostgreSQL Composite")
            .leftBoundary(_Constant.LEFT_PAREN)
            .delim(_Constant.COMMA)
            .rightBoundary(_Constant.RIGHT_PAREN)

            .backSlashEscapeOn(true)
            .quoteEscapeOn(true)  // postgre composite input/output syntax support quote escape
            .quoteChar(_Constant.DOUBLE_QUOTE)

            .allowQuote(true)
            .allowNothing(true)  // nothing representing null
            .allowWhitespace(true) // whitespace is a part of field value
            .nullAsNull(false)

            .build();

    private static final ClassValue<CompositeType> CLASS_VALUE = FuncClassValue.create(CompositeType::new);


    private final Class<?> javaType;

    private final CustomType customType;

    private final List<CompositeField> fieldList;


    private CompositeType(Class<?> javaType) {
        this.javaType = javaType;
        this.customType = CustomType.builder()
                .typeName(Objects.requireNonNull(AnnotationUtils.definedTypeNameOf(javaType)))
                .componentType(ArmyType.COMPOSITE)
                .javaType(javaType)
                .componentCreateDdl(true)
                .build();
        this.fieldList = List.copyOf(CompositeFieldFactory.forType(this));
    }

    @Override
    public Class<?> javaType() {
        return this.javaType;
    }


    @Override
    public DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case PostgreSQL:
                dataType = this.customType;
                break;
            case SQLite:
            case MySQL:
            default:
                throw mapError(this, meta);
        }
        return dataType;
    }


    @Override
    public String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        _Assert.isTrue(dataType == this.customType, "");
        return serialize(this, env, source, createStringBuilder())
                .toString();
    }

    @Override
    public Object afterGet(DataType dataType, MappingEnv env, final Object source) throws DataAccessException {
        final Object pojo;
        if (this.javaType.isInstance(source)) {
            pojo = source;
        } else if (source instanceof String text) {
            text = text.trim();
            pojo = deserialize(this, dataType, env, text, 0, text.length(), null);
        } else {
            throw dataAccessError(this, dataType, source, null);
        }
        return pojo;
    }

    @Override
    public List<CompositeField> fieldList() {
        return this.fieldList;
    }


    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return CompositeArrayType.from(ArrayUtils.arrayClassOf(this.javaType));
    }

    @SuppressWarnings("unused")
    public CustomType getCustomType() {
        return this.customType;
    }


    public StringBuilder createStringBuilder() {
        final List<CompositeField> fieldList = this.fieldList;
        final int size = fieldList.size();
        return new StringBuilder(2 + size + (size * 10));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.javaType);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof CompositeType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }


    public static StringBuilder serialize(final CompositeType instance, final MappingEnv env,
                                          final Object source, final StringBuilder sqlBuilder) {
        if (!instance.javaType.isInstance(source)) {
            String m = String.format("%s is instance of %s", ClassUtils.safeClassName(source), instance.javaType.getName());
            throw paramError(instance, instance.customType, source, new IllegalArgumentException(m));
        }

        final LiteralHandler literalHandler = env.literalHandler();
        final ObjectAccessor accessor = ObjectAccessorFactory.forPojo(instance.javaType);
        final List<CompositeField> fieldList = instance.fieldList;

        final int size = fieldList.size();

        final CustomType container = Objects.requireNonNull(instance.customType);


        CompositeField field;
        Object value;
        sqlBuilder.append(_Constant.LEFT_PAREN);
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sqlBuilder.append(_Constant.COMMA);
            }
            field = fieldList.get(i);
            value = accessor.get(source, field.fieldName());
            if (value == null) {
                // null output nothing , https://www.postgresql.org/docs/current/rowtypes.html
                continue;
            }

            literalHandler.safeLiteral(field.mappingType(), value, false, sqlBuilder, container);
        }
        sqlBuilder.append(_Constant.RIGHT_PAREN);
        return sqlBuilder;
    }


    public static Object deserialize(final CompositeType instance, final DataType dataType, final MappingEnv env,
                                     final String source, final int offset, final int endIndex, @Nullable StringBuilder builder) {
        if (!_StringUtils.hasText(source)
                || source.charAt(offset) != _Constant.LEFT_PAREN
                || source.charAt(endIndex - 1) != _Constant.RIGHT_PAREN) {
            throw dataAccessError(instance, dataType, source, null);
        }

        final ObjectAccessor accessor = ObjectAccessorFactory.forPojo(instance.javaType);
        final Supplier<?> constructor = ObjectAccessorFactory.pojoConstructor(instance.javaType);
        final FieldParser fieldParser = new FieldParser(accessor, constructor.get(), instance.fieldList, env);

        try {

            PG_DESERIALIZER.deserialize(source, offset, endIndex, fieldParser::parseField, null, null, builder);

            if (fieldParser.fieldIndex != fieldParser.fieldCount) {
                throw dataAccessError(instance, dataType, source, null);
            }
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw dataAccessError(instance, dataType, source, e);
        }

        return fieldParser.object;
    }


    private static final class FieldParser {

        private final ObjectAccessor accessor;

        private final Object object;

        private final List<CompositeField> fieldList;

        private final MappingEnv env;

        private final DecodeLiteralFunc decodeFunc;

        private final ServerMeta serverMeta;

        private final int fieldCount;

        private int fieldIndex = 0;

        private FieldParser(ObjectAccessor accessor, Object object, List<CompositeField> fieldList, MappingEnv env) {
            this.accessor = accessor;
            this.object = object;
            this.fieldList = fieldList;
            this.env = env;
            this.decodeFunc = env.decodeLiteralFunc();
            this.serverMeta = env.serverMeta();
            this.fieldCount = fieldList.size();
        }

        Object parseField(final String text, final int offest, final int endIndex) {
            final int fieldIndex = this.fieldIndex;
            if (fieldIndex >= this.fieldCount) {
                throw new IllegalArgumentException("composite filed count error");
            }
            final CompositeField field = this.fieldList.get(fieldIndex);
            final MappingType type = field.mappingType();

            final String literal;
            if (offest == 0 && endIndex == offest && !text.isEmpty()) { // representing nothing
                literal = null;
            } else {
                literal = text.substring(offest, endIndex);
            }

            //TODO add decode literal
            // literal = this.decodeFunc.decodeLiteral(type, literal);

            Object value;
            if (literal == null) {
                value = null;
            } else {
                value = type.afterGet(type.map(this.serverMeta), this.env, literal);
                if (value == MappingType.DOCUMENT_NULL_VALUE) {
                    value = null;
                }
            }

            this.accessor.set(this.object, field.fieldName(), value);
            this.fieldIndex++;
            return Boolean.TRUE;
        }

    } // FieldParser


}
