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
import io.army.meta.ServerMeta;
import io.army.util._Exceptions;

final class MySQLIdentifierHandler implements IdentifierHandler {

    static final char BACKTICK = '`';


    private final boolean asOf80;

    MySQLIdentifierHandler(ServerMeta serverMeta) {
        // Prior to / as of
        this.asOf80 = serverMeta.usedDialect().compareWith(MySQLDialect.MySQL80) >= 0;
    }

    ///
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/identifiers.html"> Schema Object Names</a>
    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/identifier-case-sensitivity.html">Identifier Case Sensitivity</a>
    @Override
    public void handle(@Nullable DatabaseObject object, String effectiveName, StringBuilder sqlBuilder) {
        // 1. 列名   : MySQL列名不区分大小写,即使使用引用也不区分,故此算法正确
        // 2. 列别名 : 虽然MySQL列别名不区分大小写,但MySQL原样返回了列别名,故此算法正确
        // 3. 表名   : MySQL 的表名的大小写规则依赖服务器的操作系统,若必要,开发者可通过 ArmyKey.TABLE_NAME_MODE 控制大小写
        // 4. 表别名 : MySQL 的表别名的大小写规则依赖服务器的操作系统,但由于表别名只是语句内引用,并不返回客户端,故此算法正确

        final int length, startIndex;
        length = effectiveName.length();
        startIndex = sqlBuilder.length();

        int lastWritten = 0, writtenIndex = startIndex;
        char ch;
        for (int i = 0; i < length; i++) {
            ch = effectiveName.charAt(i);
            if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') {
                continue;
            } else if (ch >= '0' && ch <= '9') {
                if (i == 0) {
                    // Identifiers may begin with a digit but unless quoted may not consist solely of digits.
                    sqlBuilder.append(BACKTICK);
                    writtenIndex++;
                }
                continue;
            } else if (ch == '$') {
                if (i > 0 || this.asOf80) {
                    continue;
                }
                sqlBuilder.append(BACKTICK);
                writtenIndex++;
                continue;
            }

            if (ch == _Constant.NUL_CHAR) {
                if (object == null) {
                    throw _Exceptions.identifierError(effectiveName, Database.MySQL);
                } else {
                    throw _Exceptions.objectNameError(object, Database.MySQL);
                }
            }

            if (writtenIndex == startIndex) {
                sqlBuilder.append(BACKTICK);
                writtenIndex++;
            }

            if (ch != BACKTICK) {
                continue;
            }

            if (i > lastWritten) {
                sqlBuilder.append(effectiveName, lastWritten, i);
            }
            sqlBuilder.append(BACKTICK);
            lastWritten = i; // not i + 1 as current char wasn't written


        } // for loop

        if (lastWritten < length) {
            sqlBuilder.append(effectiveName, lastWritten, length);
        }

        if (writtenIndex > startIndex) {
            sqlBuilder.append(BACKTICK);
        }


    }


}
