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

import io.army.meta.ServerMeta;
import io.army.util.ReflectionUtils;
import io.army.util._Exceptions;

import java.lang.reflect.Method;

abstract class ParserFactoryUtils {

    private ParserFactoryUtils() {
        throw new UnsupportedOperationException();
    }


    static ParserFactory createFactory(ServerMeta serverMeta) {
        final Database database;
        database = serverMeta.serverDatabase();
        final String className;
        switch (database) {
            case MySQL:
                className = "io.army.dialect.MySQLParserFactory";
                break;
            case PostgreSQL:
                className = "io.army.dialect.PostgreParserFactory";
                break;
            case SQLite:
                className = "io.army.dialect.sqlite.SQLiteParserFactory";
                break;
            default:
                throw _Exceptions.unexpectedEnum(database);
        }

        final Method method;
        method = ReflectionUtils.getStaticFactoryMethod(className, ParserFactory.class, "create");

        return (ParserFactory) ReflectionUtils.invokeStaticFactoryMethod(method);
    }


    static Dialect targetDialect(final DialectEnv environment, final Database database) {
        final ServerMeta meta = environment.serverMeta();
        if (meta.serverDatabase() != database) {
            String m = String.format("%s database isn't %s", meta, database);
            throw new IllegalArgumentException(m);
        }
        final Dialect serverDialect;
        serverDialect = Database.from(meta);
        final Dialect targetDialect;
        targetDialect = meta.usedDialect();
        if (!targetDialect.isFamily(serverDialect)) {
            throw _Exceptions.dialectDatabaseNotMatch(targetDialect, meta);
        } else if (targetDialect.compareWith(serverDialect) > 0) {
            throw _Exceptions.dialectVersionNotCompatibility(targetDialect, meta);
        }
        return targetDialect;
    }

    static UnsupportedOperationException extensionUnsupported(Database database) {
        String m = String.format("%s is not supported by %s", "Extension", database);
        return new UnsupportedOperationException(m);
    }


}
