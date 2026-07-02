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

import io.army.criteria.CriteriaException;
import io.army.criteria.SQLToken;
import io.army.criteria.impl._SQLConsultant;
import io.army.criteria.impl._UnionType;
import io.army.criteria.impl.inner.*;
import io.army.criteria.standard.StandardStatement;
import io.army.env.EscapeMode;
import io.army.executor.ExecutorSupport;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.meta.ChildTableMeta;
import io.army.meta.ParentTableMeta;
import io.army.meta.ServerMeta;
import io.army.modelgen._MetaBridge;
import io.army.session.Visible;
import io.army.sqltype.DataType;
import io.army.sqltype.SQLiteType;
import io.army.util._Exceptions;

import java.util.List;
import java.util.Set;

abstract class SQLiteParser extends _ArmyDialectParser {

    static SQLiteParser standard(DialectEnv dialectEnv, SQLiteDialect dialect) {
        return new Standard(dialectEnv, dialect);
    }


    SQLiteParser(DialectEnv dialectEnv, SQLiteDialect dialect) {
        super(dialectEnv, dialect);
        if (this.literalEscapeMode != EscapeMode.DEFAULT) {
            throw _Exceptions.literalEscapeModeError(this.literalEscapeMode, this.dialect);
        } else if (this.identifierEscapeMode != EscapeMode.DEFAULT) {
            throw _Exceptions.identifierEscapeModeError(this.identifierEscapeMode, this.dialect);
        }

    }

    @Override
    public final void typeName(MappingType type, StringBuilder sqlBuilder) {
        final DataType dataType;
        dataType = type.map(this.serverMeta);
        if (!(dataType instanceof SQLiteType)) {
            unrecognizedTypeName(type, dataType, false, sqlBuilder);
        } else switch ((SQLiteType) dataType) {
            case UNKNOWN:
            case NULL:
                throw ExecutorSupport.mapMethodError(type, dataType);
            default:
                sqlBuilder.append(dataType.typeName());
        }
    }


    @Override
    final Set<String> createKeyWordSet(ServerMeta serverMeta) {
        return _SQLiteDialectUtils.createKeyWordSet();
    }

    @Override
    final IdentifierHandler createIdentifierHandler(ServerMeta serverMeta) {
        return _SQLiteDialectUtils::handleIdentifier;
    }

    @Override
    final LiteralHandler createLiteralHandler() {
        return new SQLiteLiteralHandler(this);
    }

    @Override
    final int capabilitiesBitSet(ServerMeta serverMeta) {
        // SQLite don't support SET clause table alias
        // https://www.sqlite.org/lang_update.html , https://www.sqlite.org/lang_insert.html

        final int capabilities = 0; // must be int type
        return capabilities
                | SUPPORT_ZONE
                | SUPPORT_TABLE_ALIAS_AFTER_AS      // https://www.sqlite.org/lang_select.html
                | SUPPORT_WITH_CLAUSE               // https://www.sqlite.org/lang_select.html
                | SUPPORT_WITH_CLAUSE_IN_INSERT     // https://www.sqlite.org/lang_insert.html
                | SUPPORT_WINDOW_CLAUSE             // https://www.sqlite.org/lang_select.html
                | SUPPORT_UPDATE_ROW                // https://www.sqlite.org/lang_update.html
                | SUPPORT_JOINABLE_SINGLE_UPDATE    // https://www.sqlite.org/lang_update.html
                | SUPPORT_RETURNING_CLAUSE          // support
                | SUPPORT_SINGLE_UPDATE_ALIAS       // https://www.sqlite.org/lang_update.html
                | SUPPORT_SINGLE_DELETE_ALIAS       // https://www.sqlite.org/lang_delete.html
                ;
    }

    /// @see <a href="https://sqlite.org/lang_naming.html">Database Object Name Resolution</a>
    /// @see <a href="https://www.sqlite.org/lang_keywords.html">SQLite Keywords</a>
    @Override
    protected final char identifierDelimitedQuote() {
        return _Constant.DOUBLE_QUOTE;
    }


    /// @see <a href="https://www.sqlite.org/lang_update.html">UPDATE statement</a>
    /// @see <a href="https://www.sqlite.org/lang_delete.html">DELETE statement</a>
    @Override
    protected final ChildUpdateMode childUpdateMode() {
        return ChildUpdateMode.WITH_ID;
    }


    /// @see #SUPPORT_VALIDATE_UNION_TYPE
    @Override
    protected final void validateUnionType(_UnionType unionType) {
        // no bug,never here
        throw new UnsupportedOperationException();
    }


    @Override
    protected final MappingHandler createTypeMappingHandler(DialectEnv env) {
        return new SQLiteMappingHandler(env);
    }


    @Override
    protected final SQLiteDdlParser createDdlDialect() {
        return SQLiteDdlParser.create(this);
    }


    @Nullable
    @Override
    protected final CriteriaException supportChildInsert(_Insert._ChildInsert childStmt, Visible visible) {
        return null;
    }


    /// @see <a href="https://www.sqlite.org/lang_select.html">SELECT statement</a>
    @Override
    protected final void parseWithClause(_Statement._WithClauseSpec spec, _SqlContext context) {
        final List<_Cte> cteList;
        cteList = spec.cteList();
        if (cteList.isEmpty()) {
            return;
        }
        if (spec instanceof StandardStatement) {
            withSubQuery(spec.isRecursive(), cteList, context, _SQLConsultant::assertStandardCte);
        } else {
            sqliteWithClause(cteList, spec.isRecursive(), context);
        }
    }


    protected void sqliteWithClause(final List<_Cte> cteList, final boolean recursive, final _SqlContext mainContext) {
        throw _Exceptions.dontSupportWithClause(this.dialect);
    }

    /// @see <a href="https://www.sqlite.org/lang_select.html">SELECT statement</a>
    @Override
    protected final void standardLimitClause(final @Nullable _Expression offset, final @Nullable _Expression rowCount,
                                             _SqlContext context) {

        final StringBuilder sqlBuilder;
        if (offset != null && rowCount != null) {
            sqlBuilder = context.sqlBuilder().append(_Constant.SPACE_LIMIT);
            offset.appendSql(sqlBuilder, context);
            sqlBuilder.append(_Constant.SPACE_COMMA);
            rowCount.appendSql(sqlBuilder, context);
        } else if (rowCount != null) {
            sqlBuilder = context.sqlBuilder()
                    .append(_Constant.SPACE_LIMIT);
            rowCount.appendSql(sqlBuilder, context);
        }

    }

    /// @see <a href="https://www.sqlite.org/lang_select.html">SELECT statement</a>
    @Override
    protected final void standardLockClause(final SQLToken lockMode, final _SqlContext context) {
        throw _Exceptions.dontSupportForUpdateClause(this.dialect);
    }


    /// @see <a href="https://www.sqlite.org/lang_update.html">UPDATE statement</a>
    @Override
    protected final void parseDomainChildUpdate(final _SingleUpdate update, final _UpdateContext context) {
        final _DomainUpdate stmt = (_DomainUpdate) update;

        final ChildTableMeta<?> child;
        child = (ChildTableMeta<?>) stmt.table();

        final ParentTableMeta<?> parent = child.parentMeta();

        final _SingleUpdateContext childContext, parentContext;
        childContext = (_SingleUpdateContext) context;

        assert childContext.targetTable() == child;


        final StringBuilder childBuilder, parentBuilder;
        childBuilder = childContext.sqlBuilder();

        if (!childBuilder.isEmpty()) {
            childBuilder.append(_Constant.SPACE);
        }

        // 1. child UPDATE kew word
        childBuilder.append(_Constant.UPDATE)
                .append(_Constant.SPACE);

        // 2. child table name
        safeObjectName(child, childBuilder);

        childBuilder.append(_Constant.SPACE_AS_SPACE);

        // 3. child table alias
        childBuilder.append(childContext.safeTargetTableAlias());

        // 4. child SET clause
        singleTableSetClause(stmt.childItemPairList(), childContext);

        // 5. child from clause

        // appendChildJoinParent(childContext.ta);

        final List<_Predicate> whereList;
        whereList = stmt.wherePredicateList();

        final _Predicate idPredicate;
        idPredicate = findIdPredicate(whereList);

        dmlWhereClause(whereList, childContext);

        childContext.appendConditionFields();

        parentContext = (_SingleUpdateContext) childContext.parentContext();
        assert parentContext != null;
        if (parent.containField(_MetaBridge.VISIBLE)) {
            this.visiblePredicate(parent, parentContext.safeTargetTableAlias(), childContext, false); // note : here use childContext not parentContext
        }

        // following parsing parent statement


    }


    private static class Standard extends SQLiteParser {

        private Standard(DialectEnv dialectEnv, SQLiteDialect dialect) {
            super(dialectEnv, dialect);
        }


    } // Standard


}
