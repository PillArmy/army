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

package io.army.dialect;

import io.army.lang.Nullable;
import io.army.meta.DatabaseObject;
import io.army.sqltype.PgType;
import io.army.util._Collections;
import io.army.util._Exceptions;

import java.util.*;
import java.util.function.Function;

public abstract class _PostgreDialectUtils {

    static final Map<String, PgType> ALIAS_TO_TYPE_MAP = createAliasToTypeMap();

    private _PostgreDialectUtils() {
        throw new UnsupportedOperationException();
    }


    /// Reflection call
    ///
    /// see {@code io.army.schema.PostgreComparer}
    @SuppressWarnings("unused")
    public static Map<String, PgType> getAliasToTypeMap() {
        return ALIAS_TO_TYPE_MAP;
    }

    /// Reflection call
    ///
    /// see {@code io.army.schema.PostgreComparer}
    @SuppressWarnings("unused")
    public static Function<String, List<String>> decodeIdentifierFunc() {
        return _PostgreDialectUtils::decodeIdentifier;
    }


    /// @see <a href="https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS">Identifiers and Keywords</a>
    public static List<String> decodeIdentifier(final String text) {
        final int length = text.length();

        final StringBuilder builder = new StringBuilder(length);
        final List<String> list = new ArrayList<>(2);

        for (int i = 0; i < length; i++) {
            if (text.charAt(i) == _Constant.DOUBLE_QUOTE) {
                i = decodeQuotedIdentifier(text, i, length, builder, list);
            } else {
                i = decodeUnquotedIdentifier(text, i, length, builder, list);
            }
        } // loop


        if (list.isEmpty()) {
            throw new IllegalStateException("bug");
        }
        return list;
    }


    ///
    /// @see <a href="https://www.postgresql.org/docs/current/sql-syntax-lexical.html#SQL-SYNTAX-IDENTIFIERS">Identifiers and Keywords</a>
    static void handleIdentifier(@Nullable DatabaseObject object, String effectiveName, StringBuilder sqlBuilder) {
        final int length, startIndex;
        length = effectiveName.length();
        startIndex = sqlBuilder.length();

        int lastWritten = 0, writtenIndex = startIndex;
        char ch;

        for (int i = 0; i < length; i++) {
            ch = effectiveName.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || ch == '_') {
                continue;
            }

            if (ch >= 'A' && ch <= 'Z') {
                // Quoting an identifier also makes it case-sensitive, whereas unquoted names are always folded to lower case.
                if (writtenIndex == startIndex && object == null) {
                    sqlBuilder.append(_Constant.DOUBLE_QUOTE);
                    writtenIndex++;
                }
                continue;
            }

            if ((ch >= '0' && ch <= '9') || ch == '$') {
                if (i == 0) {
                    sqlBuilder.append(_Constant.DOUBLE_QUOTE);
                    writtenIndex++;
                }
                continue;
            }

            if (ch == _Constant.NUL_CHAR) {
                if (object == null) {
                    throw _Exceptions.identifierError(effectiveName, Database.PostgreSQL);
                } else {
                    throw _Exceptions.objectNameError(object, Database.PostgreSQL);
                }
            }

            if (writtenIndex == startIndex) {
                sqlBuilder.append(_Constant.DOUBLE_QUOTE);
                writtenIndex++;
            }

            if (ch != _Constant.DOUBLE_QUOTE) {
                continue;
            }

            if (i > lastWritten) {
                sqlBuilder.append(effectiveName, lastWritten, i);
            }
            sqlBuilder.append(_Constant.DOUBLE_QUOTE);
            lastWritten = i; // not i + 1 as current char wasn't written

        } // for loop

        if (lastWritten < length) {
            sqlBuilder.append(effectiveName, lastWritten, length);
        }

        if (writtenIndex > startIndex) {
            sqlBuilder.append(_Constant.DOUBLE_QUOTE);
        }
    }

    /// @return a modifiable set
    /// @see <a href="https://www.postgresql.org/docs/current/sql-keywords-appendix.html">SQL Key Words</a>
    static Set<String> createKeywordsSet() {
        final Set<String> keywords = new HashSet<>((int) (282 / 0.75f));
        // below postgre  reserved key words,today 2023-03-31

        keywords.add("ABS");
        keywords.add("ACOS");
        keywords.add("ALL");
        keywords.add("ALLOCATE");
        keywords.add("ANALYSE");
        keywords.add("ANALYZE");
        keywords.add("AND");
        keywords.add("ANY");
        keywords.add("ARE");
        keywords.add("ARRAY");
        keywords.add("ARRAY_AGG");
        keywords.add("ARRAY_MAX_CARDINALITY");
        keywords.add("AS");
        keywords.add("ASIN");
        keywords.add("ASYMMETRIC");
        keywords.add("ATAN");
        keywords.add("AUTHORIZATION");
        keywords.add("AVG");
        keywords.add("BEGIN_FRAME");
        keywords.add("BEGIN_PARTITION");
        keywords.add("BINARY");
        keywords.add("BIT_LENGTH");
        keywords.add("BLOB");
        keywords.add("BOTH");
        keywords.add("CARDINALITY");
        keywords.add("CASE");
        keywords.add("CAST");
        keywords.add("CEIL");
        keywords.add("CEILING");
        keywords.add("CHARACTER_LENGTH");
        keywords.add("CHAR_LENGTH");
        keywords.add("CHECK");
        keywords.add("CLASSIFIER");
        keywords.add("CLOB");
        keywords.add("COLLATE");
        keywords.add("COLLECT");
        keywords.add("COLUMN");
        keywords.add("CONCURRENTLY");
        keywords.add("CONDITION");
        keywords.add("CONNECT");
        keywords.add("CONSTRAINT");
        keywords.add("CONTAINS");
        keywords.add("CONVERT");
        keywords.add("CORR");
        keywords.add("CORRESPONDING");
        keywords.add("COS");
        keywords.add("COSH");
        keywords.add("COUNT");
        keywords.add("COVAR_POP");
        keywords.add("COVAR_SAMP");
        keywords.add("CREATE");
        keywords.add("CROSS");
        keywords.add("CUME_DIST");
        keywords.add("CURRENT_CATALOG");
        keywords.add("CURRENT_DATE");
        keywords.add("CURRENT_DEFAULT_TRANSFORM_GROUP");
        keywords.add("CURRENT_PATH");
        keywords.add("CURRENT_ROLE");
        keywords.add("CURRENT_ROW");
        keywords.add("CURRENT_SCHEMA");
        keywords.add("CURRENT_TIME");
        keywords.add("CURRENT_TIMESTAMP");
        keywords.add("CURRENT_TRANSFORM_GROUP_FOR_TYPE");
        keywords.add("CURRENT_USER");
        keywords.add("DATALINK");
        keywords.add("DATE");
        keywords.add("DECFLOAT");
        keywords.add("DEFAULT");
        keywords.add("DEFINE");
        keywords.add("DENSE_RANK");
        keywords.add("DEREF");
        keywords.add("DESCRIBE");
        keywords.add("DETERMINISTIC");
        keywords.add("DISCONNECT");
        keywords.add("DISTINCT");
        keywords.add("DLNEWCOPY");
        keywords.add("DLPREVIOUSCOPY");
        keywords.add("DLURLCOMPLETE");
        keywords.add("DLURLCOMPLETEONLY");
        keywords.add("DLURLCOMPLETEWRITE");
        keywords.add("DLURLPATH");
        keywords.add("DLURLPATHONLY");
        keywords.add("DLURLPATHWRITE");
        keywords.add("DLURLSCHEME");
        keywords.add("DLURLSERVER");
        keywords.add("DLVALUE");
        keywords.add("DO");
        keywords.add("DYNAMIC");
        keywords.add("ELEMENT");
        keywords.add("ELSE");
        keywords.add("END");
        keywords.add("END-EXEC");
        keywords.add("END_FRAME");
        keywords.add("END_PARTITION");
        keywords.add("EQUALS");
        keywords.add("EVERY");
        keywords.add("EXCEPT");
        keywords.add("EXCEPTION");
        keywords.add("EXEC");
        keywords.add("EXP");
        keywords.add("FALSE");
        keywords.add("FETCH");
        keywords.add("FIRST_VALUE");
        keywords.add("FLOOR");
        keywords.add("FOR");
        keywords.add("FOREIGN");
        keywords.add("FRAME_ROW");
        keywords.add("FREE");
        keywords.add("FREEZE");
        keywords.add("FROM");
        keywords.add("FULL");
        keywords.add("FUSION");
        keywords.add("GET");
        keywords.add("GRANT");
        keywords.add("GROUP");
        keywords.add("HAVING");
        keywords.add("ILIKE");
        keywords.add("IN");
        keywords.add("INDICATOR");
        keywords.add("INITIAL");
        keywords.add("INNER");
        keywords.add("INTERSECT");
        keywords.add("INTERSECTION");
        keywords.add("INTO");
        keywords.add("IS");
        keywords.add("ISNULL");
        keywords.add("JOIN");
        keywords.add("JSON_ARRAY");
        keywords.add("JSON_ARRAYAGG");
        keywords.add("JSON_EXISTS");
        keywords.add("JSON_OBJECT");
        keywords.add("JSON_OBJECTAGG");
        keywords.add("JSON_QUERY");
        keywords.add("JSON_TABLE");
        keywords.add("JSON_TABLE_PRIMITIVE");
        keywords.add("JSON_VALUE");
        keywords.add("LAG");
        keywords.add("LAST_VALUE");
        keywords.add("LATERAL");
        keywords.add("LEAD");
        keywords.add("LEADING");
        keywords.add("LEFT");
        keywords.add("LIKE");
        keywords.add("LIKE_REGEX");
        keywords.add("LISTAGG");
        keywords.add("LN");
        keywords.add("LOCALTIME");
        keywords.add("LOCALTIMESTAMP");
        keywords.add("LOG");
        keywords.add("LOG10");
        keywords.add("LOWER");
        keywords.add("MATCHES");
        keywords.add("MATCH_NUMBER");
        keywords.add("MATCH_RECOGNIZE");
        keywords.add("MAX");
        keywords.add("MEASURES");
        keywords.add("MEMBER");
        keywords.add("MIN");
        keywords.add("MOD");
        keywords.add("MODIFIES");
        keywords.add("MODULE");
        keywords.add("MULTISET");
        keywords.add("NATURAL");
        keywords.add("NCLOB");
        keywords.add("NOT");
        keywords.add("NOTNULL");
        keywords.add("NTH_VALUE");
        keywords.add("NTILE");
        keywords.add("NULL");
        keywords.add("OCCURRENCES_REGEX");
        keywords.add("OCTET_LENGTH");
        keywords.add("OFFSET");
        keywords.add("OMIT");
        keywords.add("ON");
        keywords.add("ONE");
        keywords.add("ONLY");
        keywords.add("OPEN");
        keywords.add("OR");
        keywords.add("ORDER");
        keywords.add("OUTER");
        keywords.add("OVERLAPS");
        keywords.add("PATTERN");
        keywords.add("PER");
        keywords.add("PERCENT");
        keywords.add("PERCENTILE_CONT");
        keywords.add("PERCENTILE_DISC");
        keywords.add("PERCENT_RANK");
        keywords.add("PERIOD");
        keywords.add("PERMUTE");
        keywords.add("PORTION");
        keywords.add("POSITION_REGEX");
        keywords.add("POWER");
        keywords.add("PRECEDES");
        keywords.add("PRIMARY");
        keywords.add("PTF");
        keywords.add("RANK");
        keywords.add("READS");
        keywords.add("REFERENCES");
        keywords.add("REGR_AVGX");
        keywords.add("REGR_AVGY");
        keywords.add("REGR_COUNT");
        keywords.add("REGR_INTERCEPT");
        keywords.add("REGR_R2");
        keywords.add("REGR_SLOPE");
        keywords.add("REGR_SXX");
        keywords.add("REGR_SXY");
        keywords.add("REGR_SYY");
        keywords.add("RESULT");
        keywords.add("RIGHT");
        keywords.add("ROW_NUMBER");
        keywords.add("RUNNING");
        keywords.add("SCOPE");
        keywords.add("SEEK");
        keywords.add("SELECT");
        keywords.add("SENSITIVE");
        keywords.add("SESSION_USER");
        keywords.add("SIMILAR");
        keywords.add("SIN");
        keywords.add("SINH");
        keywords.add("SOME");
        keywords.add("SPECIFIC");
        keywords.add("SPECIFICTYPE");
        keywords.add("SQLCODE");
        keywords.add("SQLERROR");
        keywords.add("SQLEXCEPTION");
        keywords.add("SQLSTATE");
        keywords.add("SQLWARNING");
        keywords.add("SQRT");
        keywords.add("STATIC");
        keywords.add("STDDEV_POP");
        keywords.add("STDDEV_SAMP");
        keywords.add("SUBMULTISET");
        keywords.add("SUBSET");
        keywords.add("SUBSTRING_REGEX");
        keywords.add("SUCCEEDS");
        keywords.add("SUM");
        keywords.add("SYMMETRIC");
        keywords.add("SYSTEM_TIME");
        keywords.add("SYSTEM_USER");
        keywords.add("TABLE");
        keywords.add("TABLESAMPLE");
        keywords.add("TAN");
        keywords.add("TANH");
        keywords.add("THEN");
        keywords.add("TIMEZONE_HOUR");
        keywords.add("TIMEZONE_MINUTE");
        keywords.add("TO");
        keywords.add("TRAILING");
        keywords.add("TRANSLATE");
        keywords.add("TRANSLATE_REGEX");
        keywords.add("TRANSLATION");
        keywords.add("TRIM_ARRAY");
        keywords.add("TRUE");
        keywords.add("UNION");
        keywords.add("UNIQUE");
        keywords.add("UNMATCHED");
        keywords.add("UNNEST");
        keywords.add("UPPER");
        keywords.add("USER");
        keywords.add("USING");
        keywords.add("VALUE_OF");
        keywords.add("VARBINARY");
        keywords.add("VARIADIC");
        keywords.add("VAR_POP");
        keywords.add("VAR_SAMP");
        keywords.add("VERBOSE");
        keywords.add("VERSIONING");
        keywords.add("WHEN");
        keywords.add("WHENEVER");
        keywords.add("WHERE");
        keywords.add("WIDTH_BUCKET");
        keywords.add("WINDOW");
        keywords.add("WITH");
        keywords.add("XMLAGG");
        keywords.add("XMLBINARY");
        keywords.add("XMLCAST");
        keywords.add("XMLCOMMENT");
        keywords.add("XMLDOCUMENT");
        keywords.add("XMLITERATE");
        keywords.add("XMLQUERY");
        keywords.add("XMLTEXT");
        keywords.add("XMLVALIDATE");

        return keywords;
    }


    /// an unmodifiable map
    /// @see <a href="https://www.postgresql.org/docs/current/datatype.html">Data Types</a>
    private static Map<String, PgType> createAliasToTypeMap() {
        final PgType[] values = PgType.values();

        final Map<PgType, List<String>> listMap = _Collections.hashMapForSize(20);

        listMap.put(PgType.BOOLEAN, List.of("BOOLEAN", "BOOL"));
        listMap.put(PgType.SMALLINT, List.of("INT2", "SMALLINT", "SMALLSERIAL"));
        listMap.put(PgType.INTEGER, List.of("INT", "INT4", "INTEGER", "SERIAL", "XID", "CID"));   // https://www.postgresql.org/docs/current/datatype-oid.html
        listMap.put(PgType.BIGINT, List.of("INT8", "BIGINT", "BIGSERIAL", "SERIAL8", "XID8")); // https://www.postgresql.org/docs/current/datatype-oid.html  TODO what's tid ?

        listMap.put(PgType.DECIMAL, List.of("NUMERIC", "DECIMAL"));
        listMap.put(PgType.DOUBLE, List.of("FLOAT8", "DOUBLE PRECISION", "FLOAT"));
        listMap.put(PgType.REAL, List.of("FLOAT4", "REAL"));
        listMap.put(PgType.CHAR, List.of("CHAR", "BPCHAR", "CHARACTER"));

        listMap.put(PgType.VARCHAR, List.of("VARCHAR", "CHARACTER VARYING"));
        listMap.put(PgType.TEXT, List.of("TEXT", "TXID_SNAPSHOT"));
        listMap.put(PgType.TIME, List.of("TIME", "TIME WITHOUT TIME ZONE"));
        listMap.put(PgType.TIMETZ, List.of("TIMETZ", "TIME WITH TIME ZONE"));

        listMap.put(PgType.TIMESTAMP, List.of("TIMESTAMP", "TIMESTAMP WITHOUT TIME ZONE"));
        listMap.put(PgType.TIMESTAMPTZ, List.of("TIMESTAMPTZ", "TIMESTAMP WITH TIME ZONE"));
        listMap.put(PgType.VARBIT, List.of("VARBIT", "BIT VARYING"));


        final Map<String, PgType> map = _Collections.hashMapForSize(values.length * 3);

        PgType elementType;
        for (Map.Entry<PgType, List<String>> e : listMap.entrySet()) {
            elementType = e.getKey();
            for (String alias : e.getValue()) {
                map.put(alias, elementType);
            }
        }

        for (PgType value : values) {
            if (!value.isArray()) {
                map.put(value.typeName(), value);
            }

        }

        final StringBuilder builder = new StringBuilder(15);

        List<String> aliasList;

        final List<String> singletonList = new ArrayList<>(1);
        for (PgType value : values) {
            if (!value.isArray()) {
                continue;
            }
            elementType = Objects.requireNonNull(value.elementType());
            aliasList = listMap.get(elementType);

            if (aliasList == null) {
                singletonList.clear();
                singletonList.add(elementType.typeName());
                aliasList = singletonList;
            }

            for (String alias : aliasList) {

                builder.setLength(0);
                builder.append(alias)
                        .append("[]");
                map.put(builder.toString(), value);

                builder.setLength(0);
                builder.append('_')
                        .append(alias);
                map.put(builder.toString(), value);
            }  // aliasList loop

        } // values loop

        return Map.copyOf(map);
    }


    private static IllegalArgumentException notIdentifierError() {
        return new IllegalArgumentException("not postgre identifier");
    }

    /// @see #decodeIdentifier(String)
    private static int decodeQuotedIdentifier(final String text, int offest, final int length,
                                              StringBuilder builder, List<String> list) {
        final int originalSize = list.size();

        boolean inQuote = false;
        char ch, nextChar;
        int lastWritten = offest;
        for (int nextIndex; offest < length; offest++) {
            ch = text.charAt(offest);

            if (ch != _Constant.DOUBLE_QUOTE) {
                continue;
            }

            if (!inQuote) {
                lastWritten = offest + 1;
                builder.setLength(0);  // clear
                inQuote = true;
                continue;
            }

            nextIndex = offest + 1;
            if (nextIndex == length) {
                builder.append(text, lastWritten, offest);
                list.add(builder.toString());
                inQuote = false;
                continue;
            }

            if (offest <= lastWritten) {
                throw new IllegalStateException("bug");
            }

            builder.append(text, lastWritten, offest);
            offest++;  // skip next char
            lastWritten = offest;

            nextChar = text.charAt(nextIndex);
            if (nextChar == _Constant.DOUBLE_QUOTE) {
                continue;
            }

            if (nextChar == _Constant.PERIOD) {
                list.add(builder.toString());
                inQuote = false;
                break;
            }

        } // loop

        if (inQuote) {
            throw notIdentifierError();
        }
        if (originalSize + 1 != list.size()) {
            throw new IllegalStateException("bug");
        }
        return offest;
    }


    /// @see #decodeIdentifier(String)
    private static int decodeUnquotedIdentifier(final String text, int offest, final int length,
                                                StringBuilder builder, List<String> list) {
        final int originalSize = list.size();

        boolean enclose = false;

        char ch;
        int lastWritten = offest;
        for (int nextIndex; offest < length; offest++) {
            ch = text.charAt(offest);

            if (ch == _Constant.DOUBLE_QUOTE || ch == _Constant.PERIOD || Character.isWhitespace(ch)) {
                throw notIdentifierError();
            }

            if (!enclose) {
                lastWritten = offest;
                builder.setLength(0);  // clear
                enclose = true;
                continue;
            }

            if (offest <= lastWritten) {
                throw new IllegalStateException("bug");
            }

            nextIndex = offest + 1;
            if (nextIndex == length) {
                builder.append(text, lastWritten, length);
                list.add(builder.toString());
                enclose = false;
                continue;
            }

            if (text.charAt(nextIndex) == _Constant.PERIOD) {
                offest++;  // firstly skip next char
                builder.append(text, lastWritten, offest); // use offest after ++
                list.add(builder.toString());
                enclose = false;
                break;
            }

        } // loop

        if (enclose) {
            throw notIdentifierError();
        }
        if (originalSize + 1 != list.size()) {
            throw new IllegalStateException("bug");
        }
        return offest;
    }


}
