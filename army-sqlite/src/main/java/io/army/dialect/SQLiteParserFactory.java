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

@SuppressWarnings("unused")
public class SQLiteParserFactory implements ParserFactory {

    public static SQLiteParserFactory create() {
        return new SQLiteParserFactory();
    }

    private SQLiteParserFactory() {
    }


    @Override
    public PreBootstrapParser createPreBootstrapParser(ServerMeta serverMeta) {
        throw ParserFactories.extensionUnsupported(Database.SQLite);
    }

    @Override
    public DialectParser createDialectParser(DialectEnv environment) {
        final SQLiteDialect dialect;
        dialect = (SQLiteDialect) ParserFactories.targetDialect(environment, Database.SQLite);
        return SQLiteParser.standard(environment, dialect);
    }
}
