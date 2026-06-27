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

import io.army.annotation.Column;
import io.army.annotation.Table;
import io.army.lang.Nullable;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.Objects;

final class TableSourceCodeGenImpl extends CodeGenSupport implements TableSourceCodeGen {

    static TableSourceCodeGenImpl create(StringBuilder tempBuilder) {
        return new TableSourceCodeGenImpl(tempBuilder);
    }


    private final StringBuilder tempBuilder;

    final StringBuilder sourceCodeBuilder = new StringBuilder(1400 + 20 * 280);

    private final StringBuilder fieldBuilder = new StringBuilder(20 * 150);

    private final String codeGenTime;

    private TableSourceCodeGenImpl(StringBuilder tempBuilder) {
        this.tempBuilder = tempBuilder;
        this.codeGenTime = createCodeCreateTime();
    }

    @Override
    public Pair generateSource(TypeElement domainElement, @Nullable TypeElement parentElement, MappingMode mode,
                               List<FieldMeta> fieldMetaList) {
        final StringBuilder sourceCodeBuilder;
        sourceCodeBuilder = this.sourceCodeBuilder;

        sourceCodeBuilder.setLength(0); // clear

        appendCommonImports(domainElement, parentElement, mode, fieldMetaList, sourceCodeBuilder);

        final String simpleClassName, domainName;
        simpleClassName = MetaUtils.getSimpleClassName(domainElement);

        appendClassDefinition(domainElement, simpleClassName, sourceCodeBuilder);

        domainName = appendStaticBlock(domainElement, simpleClassName, parentElement, fieldMetaList.size(), mode, sourceCodeBuilder);

        appendFields(domainName, simpleClassName, fieldMetaList, sourceCodeBuilder);

        staticModelClassEnd(sourceCodeBuilder);

        return Pair.of(MetaUtils.getClassName(domainElement) + _MetaBridge.META_CLASS_NAME_SUFFIX, sourceCodeBuilder.toString());
    }


    private void appendCommonImports(TypeElement domainElement, @Nullable TypeElement parentElement, MappingMode mode,
                                     List<FieldMeta> fieldMetaList, StringBuilder builder) {

        packageDeclaration(builder, domainElement);

        importClass(builder, "javax.annotation.processing.Generated");
        importClass(builder, "io.army.criteria.impl._TableMetaFactory");
        builder.append(LF);
        importClass(builder, "io.army.meta.FieldMeta");
        importClass(builder, "io.army.meta.PrimaryFieldMeta");


        boolean hasJsonbField, hasJsonField, hasArrayField, hasCompositeField;
        hasJsonbField = hasJsonField = hasArrayField = hasCompositeField = false;

        for (FieldMeta fieldMeta : fieldMetaList) {

            switch (fieldMeta.type) {
                case JSONB:
                    hasJsonbField = true;
                    break;
                case JSON:
                    hasJsonField = true;
                    break;
                case ARRAY:
                    hasArrayField = true;
                    break;
                case COMPOSITE:
                    hasCompositeField = true;
                    break;
            } // switch


        } // loop

        if (hasJsonbField) {
            importClass(builder, "io.army.meta.JsonbFieldMeta");
        }

        if (hasJsonField) {
            importClass(builder, "io.army.meta.JsonFieldMeta");
        }

        if (hasArrayField) {
            importClass(builder, "io.army.meta.ArrayFieldMeta");
        }

        if (hasCompositeField) {
            importClass(builder, "io.army.meta.CompositeFieldMeta");
        }

        builder.append(LF);
        switch (mode) {
            case SIMPLE:
                importClass(builder, "io.army.meta.SimpleTableMeta");
                break;
            case CHILD:
                importClass(builder, "io.army.meta.ComplexTableMeta");
                break;
            case PARENT:
                importClass(builder, "io.army.meta.ParentTableMeta");
                break;
            default:
                //no-op
        } // switch


        if (parentElement != null
                && !isSameClassName(domainElement, parentElement)
                && !isSamePackage(domainElement, parentElement)) {

            final String parentClassName = MetaUtils.getClassName(parentElement);

            builder.append(LF);
            importClass(builder, parentClassName);
            importClass(builder, parentClassName + _MetaBridge.META_CLASS_NAME_SUFFIX);

        }


    }


    private void appendClassDefinition(final TypeElement element, final String simpleClassName, final StringBuilder builder) {
        final Table table = Objects.requireNonNull(element.getAnnotation(Table.class));

        annotationGenerated(this.codeGenTime, table.comment(), builder);

        if (!element.getTypeParameters().isEmpty()) {
            builder.append("@SuppressWarnings(\"unchecked\")")
                    .append(LF);
        }

        staticMetaModelClassDeclaration(simpleClassName, builder);
        defaultConstructor(simpleClassName, builder);

    }

    private String appendStaticBlock(final TypeElement element, final String simpleClassName,
                                     @Nullable final TypeElement parentElement, int fieldCount,
                                     final MappingMode mode, final StringBuilder builder) {

        final String parentClassName, methodName, tableMetaName;
        switch (mode) {
            case SIMPLE:
                methodName = "getSimpleTableMeta";
                tableMetaName = "SimpleTableMeta";
                parentClassName = null;
                break;
            case CHILD:
                methodName = "getChildTableMeta";
                tableMetaName = "ComplexTableMeta";
                if (isSameClassName(element, Objects.requireNonNull(parentElement))) {
                    parentClassName = MetaUtils.getClassName(parentElement);
                } else {
                    parentClassName = MetaUtils.getSimpleClassName(parentElement);
                }
                break;
            case PARENT:
                methodName = "getParentTableMeta";
                tableMetaName = "ParentTableMeta";
                parentClassName = null;
                break;
            default:
                throw new IllegalArgumentException("bug");
        }


        final int paramSize = element.getTypeParameters().size(), parentParamSize;
        if (parentElement == null) {
            parentParamSize = 0;
        } else {
            parentParamSize = parentElement.getTypeParameters().size();
        }

        fieldPrefix(builder)
                .append(tableMetaName)
                .append('<');

        final int complexStart, domainStart;
        complexStart = builder.length();
        if (parentClassName != null) {
            builder.append(parentClassName);
            if (parentParamSize > 0) {
                appendParamWildcard(builder, parentParamSize);
            }
            builder.append(COMMA);
        }
        domainStart = builder.length();
        builder.append(simpleClassName);

        if (paramSize > 0) {
            appendParamWildcard(builder, paramSize);
        }

        final String complexName, domainName;
        if (paramSize == 0 && parentParamSize == 0 && parentClassName == null) {
            domainName = simpleClassName;
            complexName = simpleClassName;
        } else {
            final int length = builder.length();
            domainName = builder.substring(domainStart, length);
            complexName = builder.substring(complexStart, length);
        }
        builder.append('>')
                .append(SPACE)
                .append(_MetaBridge.TABLE_META)
                .append(SEMICOLON)
                .repeat(LF, 2);

        final String safeXmlDomainName;
        if (paramSize > 0) {
            safeXmlDomainName = domainName.replace("<", "&lt;");
            builder.repeat(SPACE, 4) // member prefix
                    .append("/// Due to ")
                    .append(safeXmlDomainName)
                    .append(" contains type parameter(s) , army generate static CLASS for army session query api.\n");

            fieldPrefix(builder)
                    .append("Class<")
                    .append(domainName)
                    .append("> CLASS = (Class<")
                    .append(domainName)
                    .append(">)((Class<?>)")
                    .append(simpleClassName)
                    .append(".class)")
                    .append(SEMICOLON)
                    .repeat(LF, 2);
        } else {
            safeXmlDomainName = null;
        }

        builder.repeat(SPACE, 4) // member prefix
                .append("static")
                .append(SPACE)
                .append('{')
                .append(LF);

        if (paramSize > 0) {
            builder.repeat(SPACE, 8)
                    .append("final ")
                    .append(tableMetaName)
                    .append("<?> temp")
                    .append(SEMICOLON)
                    .append(LF)

                    .repeat(SPACE, 8)
                    .append("temp = _TableMetaFactory.")
                    .append(methodName)
                    .append('(');

            if (parentClassName != null) {
                builder.append(parentClassName)
                        .append(_MetaBridge.META_CLASS_NAME_SUFFIX)
                        .append('.')
                        .append(_MetaBridge.TABLE_META)
                        .append(',');
            }
            builder.append(simpleClassName)
                    .append(".class)")
                    .append(SEMICOLON)
                    .append(LF)

                    .repeat(SPACE, 8)
                    .append(_MetaBridge.TABLE_META)
                    .append(" = (")
                    .append(tableMetaName)
                    .append('<')
                    .append(complexName)
                    .append(">)temp")
                    .append(SEMICOLON)
                    .repeat(LF, 2);

        } else {
            builder.repeat(SPACE, 8)
                    .append(_MetaBridge.TABLE_META)
                    .append(" = _TableMetaFactory.")
                    .append(methodName)
                    .append('(');

            if (parentClassName != null) {
                builder.append(parentClassName)
                        .append(_MetaBridge.META_CLASS_NAME_SUFFIX)
                        .append('.')
                        .append(_MetaBridge.TABLE_META)
                        .append(',');
            }
            builder.append(simpleClassName)
                    .append(".class)")
                    .append(SEMICOLON)
                    .repeat(LF, 2);
        }

        final String varFieldSize = "fieldSize";

        builder.repeat(SPACE, 8)
                .append("final int ")
                .append(varFieldSize)
                .append(" = ")
                .append(_MetaBridge.TABLE_META)
                .append('.')
                .append("fieldList")
                .append("()")
                .append('.')
                .append("size")
                .append("()")
                .append(SEMICOLON)
                .append(LF)

                .repeat(SPACE, 8)
                .append("if(")
                .append(varFieldSize)
                .append(" != ")
                .append(fieldCount)
                .append("){")
                .append(LF)

                .repeat(SPACE, 12)
                .append("throw")
                .append(SPACE)
                .append("_TableMetaFactory")
                .append('.')
                .append("tableFiledSizeError")
                .append('(')
                .append(simpleClassName)
                .append('.')
                .append("class")
                .append(COMMA)
                .append(varFieldSize)
                .append(')')
                .append(SEMICOLON)
                .append(LF)

                .repeat(SPACE, 8)
                .append("}")
                .append(LF)
                .repeat(SPACE, 4)
                .append("}")
                .repeat(LF, 2);


        if (paramSize > 0) {
            // generate static constructor method.
            builder.repeat(SPACE, 4)
                    .append("/// Due to ")
                    .append(safeXmlDomainName)
                    .append(" contains type parameter(s) , army generate static constructor method for army session query api.\n")
                    .repeat(SPACE, 4)
                    .append("public static ")
                    .append(domainName)
                    .append(" constructor(){")
                    .append(LF)

                    .repeat(SPACE, 8)
                    .append("return new ")
                    .append(simpleClassName)
                    .append("<>")
                    .append("()")
                    .append(SEMICOLON)
                    .append(LF)
                    .repeat(SPACE, 4)
                    .append('}')
                    .repeat(LF, 2);
        }
        return domainName;
    }


    private void appendFields(String domainName, String simpleClassName, List<FieldMeta> fieldMetaList, StringBuilder builder) {

        final StringBuilder tempBuilder = this.tempBuilder, fieldBuilder = this.fieldBuilder;
        fieldBuilder.setLength(0); // clear


        VariableElement field;
        String fieldName, upperCaseFieldName, methodName, metaTypeName;

        int count = 0, commentStart, commentEnd;
        boolean primary = false;

        builder.repeat(SPACE, 4)
                .append("/*-------------------following table filed names-------------------*/")
                .repeat(LF, 2);

        for (FieldMeta fieldMeta : fieldMetaList) {

            field = fieldMeta.field;
            fieldName = field.getSimpleName().toString();

            commentStart = builder.length();

            builder.repeat(SPACE, 4)
                    .append("/// {@link ")
                    .append(simpleClassName)
                    .append('#')
                    .append(fieldName)
                    .append(SPACE)
                    .append('}')
                    .append(SPACE);

            appendFieldComment(field, builder)
                    .append(LF);

            commentEnd = builder.length();

            upperCaseFieldName = _MetaBridge.camelToUpperCase(fieldName, tempBuilder);

            fieldPrefix(builder)
                    .append("String ")
                    .append(upperCaseFieldName)
                    .append(SPACE)
                    .append('=')
                    .append(SPACE)
                    .append(DOUBLE_QUOTE)
                    .append(fieldName)
                    .append(DOUBLE_QUOTE)
                    .append(SEMICOLON)
                    .repeat(LF, 2);

            count++;
            if ((count & 3) == 0) {
                builder.append(LF);
            }

            // field definitions
            fieldBuilder.append(builder, commentStart, commentEnd);
            fieldPrefix(fieldBuilder);

            switch (fieldMeta.type) {
                case JSONB:
                case JSON:
                case ARRAY:
                case COMPOSITE:
                default: {
                    if (fieldName.equals(_MetaBridge.ID)) {
                        methodName = "id";
                        metaTypeName = "PrimaryFieldMeta";
                        primary = true;
                    } else {
                        methodName = "field";
                        metaTypeName = "FieldMeta";
                    }
                } // default
            }  // switch

            fieldBuilder
                    .append(metaTypeName)
                    .append('<')
                    .append(domainName)
                    .append('>')
                    .append(SPACE)
                    .append(fieldName)
                    .append(SPACE)
                    .append('=')
                    .append(SPACE)
                    .append(_MetaBridge.TABLE_META)
                    .append('.')
                    .append(methodName)
                    .append('(');

            if (primary) {
                primary = false;
            } else {
                fieldBuilder.append(upperCaseFieldName);
            }

            fieldBuilder.append(')')
                    .append(SEMICOLON)
                    .repeat(LF, 2);

            if ((count & 3) == 0) {
                fieldBuilder.append(LF);
            }


        } // field loop

        fieldMetaSeparator(builder);
        builder.append(fieldBuilder);

    }


    private static StringBuilder appendFieldComment(final VariableElement field, StringBuilder builder) {
        final Column column;
        column = field.getAnnotation(Column.class);
        assert column != null;

        final String comment;
        comment = column.comment();
        if (!MetaUtils.hasText(comment)) {
            switch (field.getSimpleName().toString()) {
                case _MetaBridge.ID:
                    builder.append("primary key");
                    break;
                case _MetaBridge.CREATE_TIME:
                    builder.append("create time");
                    break;
                case _MetaBridge.UPDATE_TIME:
                    builder.append("update time");
                    break;
                case _MetaBridge.VERSION:
                    builder.append("version for optimistic lock");
                    break;
                case _MetaBridge.VISIBLE:
                    builder.append("visible for soft delete");
                    break;
                default: {
                    if (MetaUtils.isEnum(field)) {
                        builder.append("@see ")
                                .append(field.asType());
                    }
                }

            }
        }
        return builder;
    }


}
