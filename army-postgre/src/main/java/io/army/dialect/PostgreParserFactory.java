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

/// This class is used by {@link ParserFactory#createFactory(ServerMeta)} by reflection.
@SuppressWarnings("unused")
public final class PostgreParserFactory implements ParserFactory {

    public static PostgreParserFactory create() {
        return new PostgreParserFactory();
    }

    private PostgreParserFactory() {
    }

    @Override
    public PreBootstrapParser createPreBootstrapParser(ServerMeta serverMeta) {
        return new PostgrePreBootstrapDialectParser(serverMeta);
    }

    @Override
    public DialectParser createDialectParser(DialectEnv environment) {
        final PostgreDialect dialect;
        dialect = (PostgreDialect) ParserFactoryUtils.targetDialect(environment, Database.PostgreSQL);
        return PostgreDialectParser.create(environment, dialect);
    }


}
