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
import io.army.criteria.impl.inner.*;
import io.army.criteria.impl.inner.postgre._PostgreCommand;
import io.army.criteria.postgre.PostgreCursor;
import io.army.criteria.postgre.PostgreMerge;
import io.army.dialect.Database;
import io.army.lang.Nullable;

/// Type consultant for PostgreSQL dialect statements, verifying that criteria objects are valid Army-generated PostgreSQL implementations.
public abstract class _PostgreConsultant extends _SQLConsultant {


    /// Private constructor, prevent instantiation.
    private _PostgreConsultant() {
    }


    /// Assert that the given INSERT statement is a valid PostgreSQL Army insert.
    /// @param insert the INSERT statement to validate
    public static void assertInsert(final InsertStatement insert) {
        if (insert instanceof _Insert._DomainInsert || insert instanceof _Insert._ValuesInsert) {
            if (!(insert instanceof PostgreInserts.PostgreValueSyntaxInsertStatement)) {
                throw nonArmyStatement(insert);
            }
        } else if (insert instanceof _Insert._QueryInsert) {
            if (!(insert instanceof PostgreInserts.PostgreQueryInsertStatement)) {
                throw nonArmyStatement(insert);
            }
        } else {
            throw nonArmyStatement(insert);
        }
    }

    /// Assert that the given sub INSERT statement is a valid PostgreSQL Army sub-insert.
    /// @param insert the sub INSERT statement to validate
    public static void assertSubInsert(final SubStatement insert) {
        if (insert instanceof _Insert._DomainInsert || insert instanceof _Insert._ValuesInsert) {
            if (!(insert instanceof PostgreInserts.PostgreValueSyntaxInsertStatement || insert instanceof PostgreMerges.MergeValuesSyntaxSubInsert)) {
                throw nonArmyStatement(insert);
            }
        } else if (insert instanceof _Insert._QueryInsert) {
            if (!(insert instanceof PostgreInserts.PostgreQueryInsertStatement)) {
                throw nonArmyStatement(insert);
            }
        } else {
            throw nonArmyStatement(insert);
        }
    }

    /// Assert that the given UPDATE statement is a valid PostgreSQL Army update.
    /// @param update the UPDATE statement to validate
    public static void assertUpdate(final UpdateStatement update) {
        if (update instanceof _ReturningDml) {
            if (!(update instanceof PostgreUpdates.PostgreUpdateWrapper)) {
                throw nonArmyStatement(update);
            }
        } else if (!(update instanceof PostgreUpdates)) {
            throw nonArmyStatement(update);
        }

    }

    /// Assert that the given sub UPDATE statement is a valid PostgreSQL Army sub-update.
    /// @param update the sub UPDATE statement to validate
    public static void assertSubUpdate(final SubStatement update) {
        if (update instanceof _ReturningDml) {
            if (!(update instanceof PostgreUpdates.PostgreSubReturningUpdate)) {
                throw nonArmyStatement(update);
            }
        } else if (!(update instanceof PostgreUpdates.PostgreSubUpdate)) {
            throw nonArmyStatement(update);
        }
    }

    /// Assert that the given DELETE statement is a valid PostgreSQL Army delete.
    /// @param stmt the DELETE statement to validate
    public static void assertDelete(final DeleteStatement stmt) {
        if (stmt instanceof _ReturningDml) {
            if (!(stmt instanceof PostgreDeletes.PostgreReturningDeleteWrapper)) {
                throw nonArmyStatement(stmt);
            }
        } else if (!(stmt instanceof PostgreDeletes)) {
            throw nonArmyStatement(stmt);
        }
    }

    /// Assert that the given sub DELETE statement is a valid PostgreSQL Army sub-delete.
    /// @param stmt the sub DELETE statement to validate
    public static void assertSubDelete(final SubStatement stmt) {
        if (stmt instanceof _ReturningDml) {
            if (!(stmt instanceof PostgreDeletes.PostgreSubReturningDelete)) {
                throw nonArmyStatement(stmt);
            }
        } else if (!(stmt instanceof PostgreDeletes.SubSimpleDelete)) {
            throw nonArmyStatement(stmt);
        }
    }

    /// Assert that the given MERGE statement is a valid PostgreSQL Army merge.
    /// @param stmt the MERGE statement to validate
    public static void assertMerge(final PostgreMerge stmt) {
        if (!(stmt instanceof PostgreMerges.MergeInsertStatement)) {
            throw nonArmyStatement(stmt);
        }
    }

    /// Assert that the given SET statement is a valid PostgreSQL Army SET command.
    /// @param stmt the SET command to validate
    public static void assertSetStmt(final _PostgreCommand._SetCommand stmt) {
        if (!(stmt instanceof PostgreSets)) {
            throw nonArmyStatement(stmt);
        }
    }

    /// Assert that the given SHOW statement is a valid PostgreSQL Army SHOW command.
    /// @param stmt the SHOW command to validate
    public static void assertShowStmt(final _PostgreCommand._ShowCommand stmt) {
        if (!(stmt instanceof PostgreCommands.ShowCommand)) {
            throw nonArmyStatement(stmt);
        }
    }

    /// Check whether the given insert statement is NOT a merge sub-insert.
    /// @param stmt the INSERT statement to check
    /// @return {@code true} if not a merge sub-insert, {@code false} otherwise
    public static boolean isNotMergeSubInsert(final _Insert stmt) {
        return !(stmt instanceof PostgreMerges.MergeValuesSyntaxSubInsert);
    }


    /// Assert that the given RowSet is a valid PostgreSQL Army row set.
    /// @param rowSet the RowSet to validate
    public static void assertRowSet(final RowSet rowSet) {
        if (!(rowSet instanceof Query)) {
            if (!(rowSet instanceof PostgreSimpleValues
                    || rowSet instanceof PostgreSimpleValues.PostgreBracketValues
                    || rowSet instanceof SimpleValues.UnionValues
                    || rowSet instanceof SimpleValues.UnionSubValues)) {
                throw nonArmyStatement(rowSet);
            }
        } else if (rowSet instanceof Select) {
            if (!(rowSet instanceof PostgreQueries
                    || rowSet instanceof PostgreQueries.PostgreBracketQuery
                    || rowSet instanceof SimpleQueries.UnionSelect)) {
                throw nonArmyStatement(rowSet);
            }
        } else if (rowSet instanceof SubQuery) {
            if (!(rowSet instanceof PostgreQueries
                    || rowSet instanceof PostgreQueries.PostgreBracketQuery
                    || rowSet instanceof SimpleQueries.UnionSubQuery
                    || rowSet instanceof StandardQueries
                    || rowSet instanceof StandardQueries.StandardBracketQuery)) {
                throw nonArmyStatement(rowSet);
            }
        } else {
            throw nonArmyStatement(rowSet);
        }
    }

    /// Assert that the given DECLARE CURSOR statement is a valid PostgreSQL Army cursor declaration.
    /// @param stmt the cursor declaration to validate
    public static void assertDeclareCursor(final PostgreCursor stmt) {
        if (!(stmt instanceof PostgreDeclareCursors)) {
            throw nonArmyStatement(stmt);
        }
    }

    /// Assert that the given CLOSE CURSOR statement is a valid PostgreSQL Army close cursor.
    /// @param stmt the close cursor statement to validate
    public static void assertCloseCursor(final _CloseCursor stmt) {
        if (!(stmt instanceof PostgreSupports.CloseCursorStatement)) {
            throw nonArmyStatement(stmt);
        }
    }

    /// Determine the query modifier level for DISTINCT/ALL.
    public static int queryModifier(final SQLToken modifier) {
        final int level;
        if (modifier == Postgres.DISTINCT || modifier == Postgres.ALL) {
            level = 0;
        } else {
            level = -1;
        }
        return level;
    }

    /// Assert that the given WINDOW clause is a valid PostgreSQL Army window implementation.
    public static void assertWindow(final @Nullable _Window window) {
        if (!(window instanceof PostgreSupports.PostgreWindowImpl || window instanceof SQLWindow.SimpleWindow)) {
            throw illegalWindow(window);
        }
    }

    /// Assert that the given nested items are a valid PostgreSQL Army nested join.
    public static void assertNestedItems(final @Nullable _NestedItems nestedItems) {
        if (!(nestedItems instanceof PostgreNestedJoins)) {
            throw illegalNestedItems(nestedItems, Database.PostgreSQL);
        }
    }

    /// Assert that the given CTE is a valid PostgreSQL Army CTE implementation.
    public static void assertPostgreCte(final @Nullable _Cte cte) {
        if (!(cte instanceof PostgreSupports.PostgreCte || cte instanceof CriteriaContexts.RecursiveCte)) {
            throw illegalCteImpl(cte);
        }
    }

    /// Assert that the given SQL element is a valid PostgreSQL Army element.
    public static void assertSqlElement(final SQLElement element) {
        if (element instanceof _TableNameElement) {
            if (!(element instanceof PostgreFunctionUtils.TableNameExpression)) {
                throw illegalSqlElement(element);
            }
        } else {
            throw illegalSqlElement(element);
        }
    }


}
