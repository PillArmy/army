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

import io.army.criteria.*;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.meta.ServerMeta;
import io.army.schema.SchemaResult;
import io.army.session.SessionSpec;
import io.army.stmt.SimpleStmt;
import io.army.stmt.Stmt;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/// A common interface to all dialect of dialect.
public sealed interface DialectParser permits ArmyParser {


    /// Parse an INSERT statement into an executable Stmt.
    ///
    /// @param insert      the insert statement to parse
    /// @param sessionSpec session specification
    /// @return one of :
    ///
    /// - {@link SimpleStmt}
    /// - {@link io.army.stmt.GeneratedKeyStmt}
    /// - {@link io.army.stmt.PairStmt}
    ///
    Stmt insert(InsertStatement insert, SessionSpec sessionSpec);

    /// Parse an UPDATE statement into an executable Stmt.
    ///
    /// @param update       the update statement to parse
    /// @param useMultiStmt whether to use multi-statement mode
    /// @param sessionSpec  session specification
    /// @return one of
    /// - {@link SimpleStmt}
    /// - {@link io.army.stmt.BatchStmt}
    ///
    Stmt update(UpdateStatement update, boolean useMultiStmt, SessionSpec sessionSpec);

    /// Parse a DELETE statement into an executable Stmt.
    ///
    /// @param delete       the delete statement to parse
    /// @param useMultiStmt whether to use multi-statement mode
    /// @param sessionSpec  session specification
    /// @return one of
    /// - {@link SimpleStmt}
    /// - {@link io.army.stmt.BatchStmt}
    ///
    Stmt delete(DeleteStatement delete, boolean useMultiStmt, SessionSpec sessionSpec);

    /// Parse a SELECT statement into an executable Stmt.
    ///
    /// @param select       the select statement to parse
    /// @param useMultiStmt whether to use multi-statement mode
    /// @param sessionSpec  session specification
    /// @return one of
    /// - {@link SimpleStmt}
    /// - {@link io.army.stmt.BatchStmt}
    ///
    Stmt select(SelectStatement select, boolean useMultiStmt, SessionSpec sessionSpec);

    Stmt values(Values values, SessionSpec sessionSpec);


    default Stmt dialectDml(DmlStatement statement, SessionSpec sessionSpec) {
        throw new UnsupportedOperationException();
    }

    default Stmt dialectDql(DqlStatement statement, SessionSpec sessionSpec) {
        throw new UnsupportedOperationException();
    }

    default List<SimpleStmt> queryDefinedTypeStmts(Set<MappingType> definedTypeSet) {
        throw new UnsupportedOperationException();
    }


    default List<String> schemaDdl(SchemaResult schemaResult) {
        throw new UnsupportedOperationException();
    }


    boolean isKeyWords(String words);

    ServerMeta serverMeta();

    MappingEnv mappingEnv();

    StringBuilder identifier(String identifier, StringBuilder builder);


    String identifier(String identifier);

    void typeName(MappingType type, StringBuilder sqlBuilder);

    Dialect dialect();


    String printStmt(Stmt stmt, boolean beautify);

    void printStmt(Stmt stmt, boolean beautify, Consumer<String> appender);

    String sqlElement(SQLElement element);


}
