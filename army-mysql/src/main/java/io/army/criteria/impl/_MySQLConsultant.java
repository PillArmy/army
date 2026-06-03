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

package io.army.criteria.impl;

import io.army.criteria.*;
import io.army.criteria.dialect.Hint;
import io.army.criteria.dialect.Window;
import io.army.criteria.impl.inner._Insert;
import io.army.criteria.impl.inner._NestedItems;
import io.army.criteria.impl.inner._SingleDelete;
import io.army.criteria.impl.inner._SingleUpdate;
import io.army.criteria.mysql.MySQLLoadData;
import io.army.criteria.mysql.MySQLReplace;
import io.army.criteria.mysql.MySQLSet;
import io.army.dialect.Database;

/// MySQL-specific SQL statement validation consultant.
public abstract class _MySQLConsultant extends _SQLConsultant {

    /// Constructs a MySQL consultant for validating MySQL dialect statements.
    protected _MySQLConsultant() {
    }


    /// Asserts the given insert is a valid MySQL dialect INSERT statement.
    ///
    /// @param insert the insert statement to validate
    public static void assertInsert(final InsertStatement insert) {
        if (insert instanceof _Insert._DomainInsert || insert instanceof _Insert._ValuesInsert) {
            if (!(insert instanceof MySQLInserts.MySQLValueSyntaxStatement)) {
                throw nonArmyStatement(insert);
            }
        } else if (insert instanceof _Insert._AssignmentInsert) {
            if (!(insert instanceof InsertSupports.AssignmentInsertStatement)) {
                throw nonArmyStatement(insert);
            }
        } else if (insert instanceof _Insert._QueryInsert) {
            if (!(insert instanceof InsertSupports.QuerySyntaxInsertStatement)) {
                throw nonArmyStatement(insert);
            }
        } else {
            throw nonArmyStatement(insert);
        }

    }


    /// Asserts the given replace is a valid MySQL dialect REPLACE statement.
    /// @param replace the replace statement to validate
    public static void assertReplace(final MySQLReplace replace) {
        if (replace instanceof _Insert._DomainInsert) {
            if (!(replace instanceof MySQLReplaces.DomainReplaceStatement)) {
                throw nonArmyStatement(replace);
            }
        } else if (replace instanceof _Insert._ValuesInsert) {
            if (!(replace instanceof MySQLReplaces.ValueReplaceStatement)) {
                throw nonArmyStatement(replace);
            }
        } else if (replace instanceof _Insert._AssignmentInsert) {
            if (!(replace instanceof MySQLReplaces.AssignmentReplaceStatement)) {
                throw nonArmyStatement(replace);
            }
        } else if (replace instanceof _Insert._QueryInsert) {
            if (!(replace instanceof MySQLReplaces.QueryReplaceStatement)) {
                throw nonArmyStatement(replace);
            }
        } else {
            throw nonArmyStatement(replace);
        }

    }


    /// Asserts the given update is a valid MySQL dialect UPDATE statement.
    public static void assertUpdate(final UpdateStatement update) {
        if (update instanceof _SingleUpdate) {
            if (!(update instanceof MySQLSingleUpdates)) {
                throw instanceNotMatch(update, MySQLSingleUpdates.class);
            }
        } else if (!(update instanceof MySQLMultiUpdates)) {
            throw instanceNotMatch(update, MySQLMultiUpdates.class);
        }

    }

    /// Asserts the given delete is a valid MySQL dialect DELETE statement.
    public static void assertDelete(final DeleteStatement delete) {
        if (delete instanceof _SingleDelete) {
            if (!(delete instanceof MySQLSingleDeletes)) {
                throw instanceNotMatch(delete, MySQLSingleDeletes.class);
            }
        } else if (!(delete instanceof MySQLMultiDeletes)) {
            throw instanceNotMatch(delete, MySQLMultiDeletes.class);
        }
    }

    /// Asserts the given query is a valid MySQL dialect SELECT statement.
    public static void assertQuery(final Query query) {
        if (query instanceof Select) {
            if (!(query instanceof MySQLQueries
                    || query instanceof MySQLQueries.MySQLBracketQuery
                    || query instanceof SimpleQueries.UnionSelect)) {
                throw nonArmyStatement(query);
            }
        } else if (query instanceof SubQuery) {
            if (!(query instanceof MySQLQueries
                    || query instanceof MySQLQueries.MySQLBracketQuery
                    || query instanceof SimpleQueries.UnionSubQuery
                    || query instanceof StandardQueries
                    || query instanceof StandardQueries.StandardBracketQuery)) {
                throw nonArmyStatement(query);
            }
        } else {
            throw nonArmyStatement(query);
        }

    }


    /// Asserts the given values query is a valid MySQL dialect VALUES statement.
    public static void assertValues(final ValuesQuery values) {
        if (!(values instanceof MySQLSimpleValues
                || values instanceof MySQLSimpleValues.MySQLBracketValues
                || values instanceof SimpleValues.UnionValues
                || values instanceof SimpleValues.UnionSubValues)) {
            throw nonArmyStatement(values);
        }
    }

    /// Asserts the given load data is a valid MySQL LOAD DATA statement.
    public static void assertMySQLLoad(final MySQLLoadData load) {
        if (!(load instanceof MySQLLoads.MySQLLoadDataStatement)) {
            throw nonArmyStatement(load);
        }
    }

    /// Asserts the given hint is a valid MySQL hint.
    public static void assertHint(Hint hint) {
        if (!(hint instanceof MySQLHints)) {
            throw MySQLUtils.illegalHint(hint);
        }
    }

    /// Asserts the given window is a valid MySQL window specification.
    public static void assertWindow(final Window window) {
        if (!(window instanceof MySQLSupports.MySQLWindowImpl || window instanceof SQLWindow.SimpleWindow)) {
            throw illegalWindow(window);
        }
    }


    /// Asserts the given nested items are valid MySQL nested joins.
    public static void assertNestedItems(final _NestedItems nestedItems) {
        if (!(nestedItems instanceof MySQLNestedJoins)) {
            throw illegalNestedItems(nestedItems, Database.MySQL);
        }

    }

    /// Asserts the given SET statement is a valid MySQL SET statement.
    public static void assertSetStmt(final MySQLSet stmt) {
        if (!(stmt instanceof MySQLSets)) {
            throw nonArmyStatement(stmt);
        }
    }



}
