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

import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Locale;

import static java.time.temporal.ChronoField.*;

abstract class CodeGenSupport {

    static final String IMPORT = "import";

    static final char LF = '\n';

    static final char SEMICOLON = ';';

    static final char SPACE = ' ';

    static final char DOUBLE_QUOTE = '"';

    static final char COMMA = ',';


    static void importClass(StringBuilder builder, String className) {
        builder.append(IMPORT)
                .append(SPACE)
                .append(className)
                .append(SEMICOLON)
                .append(LF);
    }

    static void packageDeclaration(StringBuilder builder, TypeElement element) {
        builder.append("package")
                .append(SPACE)
                .append(((PackageElement) element.getEnclosingElement()).getQualifiedName())
                .append(SEMICOLON)
                .repeat(LF, 2);
    }


    static void staticMetaModelClassDeclaration(String simpleClassName, StringBuilder builder) {
        builder.append("public abstract class ")
                .append(simpleClassName)
                .append(_MetaBridge.META_CLASS_NAME_SUFFIX)
                .append(SPACE)
                .append('{');
    }


    static StringBuilder fieldPrefix(StringBuilder builder) {
        return builder.repeat(SPACE, 4)
                .append("public static final ");
    }


    static void annotationGenerated(String codeGenTime, String comment, StringBuilder builder) {

        builder.repeat(LF, 2)

                .append("// Army static metamodel class")
                .repeat(LF, 2)

                .append("@Generated(value = ")
                .append(DOUBLE_QUOTE)
                .append(ArmyMetaModelDomainProcessor.class.getName())
                .append(DOUBLE_QUOTE)
                .append(COMMA)
                .append(LF)

                .repeat(SPACE, 8)
                .append("date = ")
                .append(DOUBLE_QUOTE)
                .append(codeGenTime)
                .append(DOUBLE_QUOTE)
                .append(COMMA)
                .append(LF)

                .repeat(SPACE, 8)
                .append("comments = ")
                .append(DOUBLE_QUOTE)
                .append(comment)
                .append(DOUBLE_QUOTE)
                .append(')')
                .append(LF);
    }


    static void defaultConstructor(final String simpleClassName, StringBuilder builder) {
        builder.repeat(LF, 2)
                .repeat(SPACE, 4)
                .append("private")
                .append(SPACE)
                .append(simpleClassName)
                .append(_MetaBridge.META_CLASS_NAME_SUFFIX)
                .append("()")
                .append('{')
                .append(LF)
                .repeat(SPACE, 8)
                .append("throw new UnsupportedOperationException()")
                .append(SEMICOLON)
                .append(LF)
                .repeat(SPACE, 4)
                .append('}')
                .repeat(LF, 2);
    }


    static void staticModelClassEnd(StringBuilder builder) {
        builder.repeat(LF, 2)
                .append('}')
                .repeat(LF, 2);
    }

    static void fieldMetaSeparator(StringBuilder builder) {
        builder.repeat(LF, 2)
                .repeat(SPACE, 4)
                .append("/*-------------------following table filed metas-------------------*/")
                .repeat(LF, 2);
    }

    static void appendParamWildcard(final StringBuilder builder, final int paramSize) {
        builder.append('<');
        for (int i = 0; i < paramSize; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append('?');
        }
        builder.append('>');
    }

    static boolean isSameClassName(TypeElement element1, TypeElement element2) {
        return MetaUtils.getSimpleClassName(element1).equals(MetaUtils.getSimpleClassName(element2));
    }

    static boolean isSamePackage(TypeElement element1, TypeElement element2) {
        final Name name1, name2;
        name1 = ((PackageElement) element1.getEnclosingElement()).getQualifiedName();
        name2 = ((PackageElement) element2.getEnclosingElement()).getQualifiedName();
        return name1.equals(name2);
    }

    static String createCodeCreateTime() {
        return OffsetDateTime.now().format(createTimeFormatter());
    }

    private static DateTimeFormatter createTimeFormatter() {
        return new DateTimeFormatterBuilder()
                .append(DateTimeFormatter.ISO_LOCAL_DATE)
                .appendLiteral(' ')

                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .optionalStart()
                .appendLiteral(':')
                .appendValue(SECOND_OF_MINUTE, 2)

                .optionalStart()
                .appendFraction(MICRO_OF_SECOND, 0, 6, true)
                .optionalEnd()

                .appendOffset("+HH:MM:ss", "+00:00")
                .toFormatter(Locale.ENGLISH);
    }

}
