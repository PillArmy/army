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

import javax.lang.model.element.VariableElement;
import java.util.List;

final class CompositeSourceCodeGenImpl extends CodeGenSupport implements CompositeSourceCodeGen {

    static CompositeSourceCodeGen create(StringBuilder sourceCodeBuilder) {
        return new CompositeSourceCodeGenImpl(sourceCodeBuilder);
    }


    private final StringBuilder sourceCodeBuilder;

    private final String codeGenTime;


    private CompositeSourceCodeGenImpl(StringBuilder sourceCodeBuilder) {
        this.sourceCodeBuilder = sourceCodeBuilder;
        ;
        this.codeGenTime = CodeGenSupport.createCodeCreateTime();
    }

    @Override
    public Pair generateSource(final CompositeMeta meta) {
        final StringBuilder builder = this.sourceCodeBuilder;
        builder.setLength(0); // clear

        appendCommonImports(meta, builder);

        final String simpleClassName;
        simpleClassName = MetaUtils.getSimpleClassName(meta.typeElement);

        appendClassDefinition(simpleClassName, builder);

        appendStaticBlock(meta, simpleClassName, meta.fieldList.size(), builder);

        appendFields(simpleClassName, meta.fieldList, builder);

        staticModelClassEnd(builder);

        return Pair.of(MetaUtils.getClassName(meta.typeElement) + _MetaBridge.META_CLASS_NAME_SUFFIX, builder.toString());
    }


    private void appendCommonImports(CompositeMeta meta, StringBuilder builder) {
        packageDeclaration(builder, meta.typeElement);

        importClass(builder, "javax.annotation.processing.Generated");
        importClass(builder, "io.army.criteria.impl._TableMetaFactory");
        importClass(builder, meta.mappingType);
        builder.append(LF);
        importClass(builder, "io.army.meta.CompositeField");

    }

    private void appendClassDefinition(final String simpleClassName, final StringBuilder builder) {

        annotationGenerated(this.codeGenTime, "", builder);

        staticMetaModelClassDeclaration(simpleClassName, builder);

        defaultConstructor(simpleClassName, builder);

    }

    private void appendStaticBlock(final CompositeMeta meta, final String simpleClassName, final int fieldCount,
                                   final StringBuilder builder) {
        final String simpleClassNameOfMapping = MetaUtils.getSimpleClassNameFromClassName(meta.mappingType);

        final String varFieldSize = "fieldSize";

        fieldPrefix(builder)
                .append(simpleClassNameOfMapping)
                .append(SPACE)
                .append(_MetaBridge.TABLE_META)
                .append(SEMICOLON)
                .repeat(LF, 2)

                .repeat(SPACE, 4) // member prefix
                .append("static")
                .append(SPACE)
                .append('{')
                .append(LF)

                .repeat(SPACE, 8) // member prefix
                .append(_MetaBridge.TABLE_META)
                .append(SPACE)
                .append('=')
                .append(SPACE)

                .append(simpleClassNameOfMapping)
                .append('.')
                .append("from")
                .append('(')
                .append(simpleClassName)
                .append('.')
                .append("class")
                .append(')')
                .append(SEMICOLON)
                .repeat(LF, 2)

                .repeat(SPACE, 8)
                .append("final int ")
                .append(varFieldSize)
                .append(SPACE)
                .append('=')
                .append(SPACE)
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
                .append("if")
                .append('(')
                .append(varFieldSize)
                .append(SPACE)
                .append("!=")
                .append(SPACE)
                .append(fieldCount)
                .append(')')
                .append(SPACE)
                .append('{')
                .append(LF)

                .repeat(SPACE, 12)
                .append("throw")
                .append(SPACE)
                .append("_TableMetaFactory")
                .append('.')
                .append("compositeFieldSizeError")
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
                .append('}')
                .append(LF)

                .repeat(SPACE, 4) // member prefix
                .append('}');


    }


    private void appendFields(String simpleClassName, List<VariableElement> fieldList, StringBuilder builder) {
        final String fieldTypeName = "CompositeField";


        int count = 0;
        String fieldName;
        Column column;

        fieldMetaSeparator(builder);

        for (VariableElement field : fieldList) {
            fieldName = field.getSimpleName().toString();

            column = field.getAnnotation(Column.class);
            assert column != null;

            builder.repeat(SPACE, 4)
                    .append("/// {@link ")
                    .append(simpleClassName)
                    .append('#')
                    .append(fieldName)
                    .append(SPACE)
                    .append('}')
                    .append(SPACE)
                    .append(column.comment())
                    .append(LF);

            fieldPrefix(builder)
                    .append(fieldTypeName)
                    .append(SPACE)
                    .append(fieldName)
                    .append(SPACE)
                    .append('=')
                    .append(SPACE)

                    .append(_MetaBridge.TABLE_META)
                    .append('.')
                    .append("field")
                    .append('(')
                    .append(DOUBLE_QUOTE)
                    .append(fieldName)
                    .append(DOUBLE_QUOTE)
                    .append(')')
                    .append(SEMICOLON)
                    .repeat(LF, 2);

            count++;

            if ((count & 3) == 0) {
                builder.append(LF);
            }


        } // loop


    }


}
