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

public abstract class _Constant {

    private _Constant() {
        throw new UnsupportedOperationException();
    }

    public static final String TRUE = "TRUE";

    public static final String FALSE = "FALSE";

    public static final String SPACE_NULL = " NULL";

    public static final String NULL = "NULL";

    public static final String SPACE_DEFAULT = " DEFAULT";

    public static final String SPACE_NOT_NULL = " NOT NULL";

    public static final String FORBID_ALIAS = "_army_";

    public static final String WITH = "WITH";

    public static final String SPACE_RECURSIVE = " RECURSIVE";

    public static final String SELECT = "SELECT";

    public static final String SPACE_SELECT_SPACE = " SELECT ";

    public static final String UPDATE = "UPDATE";

    public static final String UPDATE_SPACE = "UPDATE ";

    public static final String DELETE = "DELETE";

    public static final String DELETE_SPACE = "DELETE ";

    public static final String DELETE_FROM = "DELETE FROM";
    public static final String DELETE_FROM_SPACE = "DELETE FROM ";

    public static final String SPACE_FROM = " FROM";

    public static final String SPACE_FROM_SPACE = " FROM ";

    public static final String SPACE_LATERAL = " LATERAL";

    public static final String SPACE_USING = " USING";

    public static final String SPACE_JOIN_SPACE = " JOIN ";

    public static final String LEFT_JOIN = "LEFT JOIN";

    public static final String RIGHT_JOIN = "RIGHT JOIN";

    public static final String FULL_JOIN = "FULL JOIN";

    public static final String SPACE_ON_SPACE = " ON ";

    public static final String SPACE_ON = " ON";

    public static final String SPACE_WHERE = " WHERE";

    public static final String SPACE_AND = " AND";

    public static final String SPACE_AND_SPACE = " AND ";

    public static final String SPACE_NOT = " NOT";

    public static final String SPACE_EXISTS = " EXISTS";

    public static final String SPACE_OR = " OR";

    public static final String SPACE_GROUP_BY = " GROUP BY";

    public static final String SPACE_HAVING = " HAVING";

    public static final String SPACE_ORDER_BY = " ORDER BY";

    public static final String SPACE_WINDOW = " WINDOW";

    public static final String SPACE_EQUAL = " =";

    public static final String SPACE_EQUAL_SPACE = " = ";

    public static final String SPACE_AS = " AS";

    public static final String SPACE_AS_SPACE = " AS ";

    public static final String INSERT = "INSERT";

    public static final String SPACE_INTO_SPACE = " INTO ";

    public static final String INSERT_INTO_SPACE = "INSERT INTO ";

    public static final String VALUES = "VALUES";

    public static final String SPACE_VALUES = " VALUES";

    public static final String SPACE_BETWEEN = " BETWEEN";

    public static final String DEFAULT = "DEFAULT";

    public static final String SPACE_SET = " SET";

    public static final String SPACE_SET_SPACE = " SET ";


    public static final String SPACE_LIMIT = " LIMIT";

    public static final String SPACE_LIMIT_SPACE = " LIMIT ";

    public static final String SPACE_FETCH = " FETCH";


    public static final String SPACE_OFFSET = " OFFSET";

    public static final String SPACE_OFFSET_SPACE = " OFFSET ";

    public static final String SPACE_ROW = " ROW";

    public static final String SPACE_ROWS = " ROWS";

    public static final String SPACE_OVER = " OVER";

    public static final String SPACE_INTERVAL = " INTERVAL";

    public static final String SPACE_PARTITION_BY = " PARTITION BY";

    public static final String SPACE_IS_NULL = " IS NULL";

    public static final String SPACE_RETURNING = " RETURNING";

    public static final String SPACE_ONLY = " ONLY";

    public static final String SPACE_COMMA = " ,";

    public static final String SPACE_SEPARATOR = " SEPARATOR";

    public static final String SPACE_CHARACTER_SET_SPACE = " CHARACTER SET ";

    public static final String SPACE_COLLATE_SPACE = " COLLATE ";


    public static final String UNDERSCORE_ARRAY = "_ARRAY";

    public static final String SPACE_SEMICOLON = " ;";

    public static final String SPACE_SEMICOLON_TWO_LINE = " ;\n\n";

    public static final String SPACE_COMMA_SPACE = " , ";

    public static final String SPACE_SEMICOLON_SPACE = " ; ";

    public static final String SPACE_LEFT_PAREN = " (";

    public static final String SPACE_RIGHT_PAREN = " )";

    public static final String SPACE_LEFT_BRACE = " {";

    public static final String SPACE_RIGHT_BRACE = " }";

    public static final String SPACE_RIGHT_SQUARE_BRACKET = " ]";

    public static final String PARENS = "()";

    public static final String SPACE_FOR_UPDATE = " FOR UPDATE";

    public static final String SPACE_FOR_SHARE = " FOR SHARE";

    public static final String SPACE_OF_SPACE = " OF ";

    public static final String SPACE_LOCK_IN_SHARE_MODE = " LOCK IN SHARE MODE";

    public static final String SPACE_ASC = " ASC";

    public static final String SPACE_DESC = " DESC";

    public static final String SPACE_QUOTE = " '";

    public static final String SPACE_AT = " @";

    public static final String SPACE_ZERO = " 0";

    public static final String DOUBLE_COLON = "::";

    public static final char LEFT_PAREN = '(';

    public static final char RIGHT_PAREN = ')';

    public static final char LEFT_SQUARE_BRACKET = '[';

    public static final char RIGHT_SQUARE_BRACKET = ']';

    public static final char LEFT_BRACE = '{';

    public static final char RIGHT_BRACE = '}';

    public static final char SPACE = ' ';

    public static final char COMMA = ',';

    public static final char DOT = '.';

    public static final char NUL_CHAR = '\0';
    public static final char BACK_SLASH = '\\';

    public static final char QUOTE = '\'';

    public static final char DOUBLE_QUOTE = '"';

    public static final char AT = '@';

    public static final char ASTERISK = '*';

    public static final char SLASH = '/';

    public static final char EQUAL = '=';

    public static final char SEMICOLON = ';';


}
