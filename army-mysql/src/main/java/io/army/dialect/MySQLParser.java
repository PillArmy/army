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
import io.army.criteria.SQLElement;
import io.army.criteria.SQLToken;
import io.army.criteria.UpdateStatement;
import io.army.criteria.impl._SQLConsultant;
import io.army.criteria.impl._UnionType;
import io.army.criteria.impl.inner.*;
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
import io.army.sqltype.MySQLType;
import io.army.util._Exceptions;

import java.util.List;
import java.util.Set;

abstract class MySQLParser extends _ArmyDialectParser {


    static final char BACKTICK = '`';

    final boolean asOf80;


    MySQLParser(DialectEnv environment, MySQLDialect dialect) {
        super(environment, dialect);

        if (this.identifierEscapeMode != EscapeMode.DEFAULT) {
            throw _Exceptions.identifierEscapeModeError(this.identifierEscapeMode, this.dialect);
        } else switch (this.literalEscapeMode) {
            case DEFAULT:
            case BACK_SLASH:
                break;
            default:
                throw _Exceptions.literalEscapeModeError(this.literalEscapeMode, this.dialect);

        }

        // Prior to / as of
        this.asOf80 = this.dialect.compareWith(MySQLDialect.MySQL80) >= 0;
    }


    @Override
    public final String sqlElement(SQLElement element) {
        throw _Exceptions.castCriteriaApi();
    }

    @Override
    public final void typeName(final MappingType type, final StringBuilder sqlBuilder) {
        final DataType dataType;
        dataType = type.map(this.serverMeta);
        if (!(dataType instanceof MySQLType)) {
            unrecognizedTypeName(type, dataType, false, sqlBuilder);
        } else switch ((MySQLType) dataType) {
            case UNKNOWN:
            case NULL:
                throw ExecutorSupport.mapMethodError(type, dataType);
            default:
                sqlBuilder.append(dataType.typeName());
        }

    }



    /*-------------------below protected methods -------------------*/

    @Override
    protected final MappingHandler createTypeMappingHandler(DialectEnv env) {
        return new MySQLMappingHandler(env);
    }

    @Override
    protected final void parseWithClause(final _Statement._WithClauseSpec spec, final _SqlContext context) {
        final List<_Cte> cteList;
        cteList = spec.cteList();
        if (cteList.isEmpty()) {
            return;
        }
        if (!this.asOf80) {
            throw _Exceptions.dontSupportWithClause(this.dialect);
        }
        withSubQuery(spec.isRecursive(), cteList, context, _SQLConsultant::assertStandardCte);
    }

    @Override
    protected final MySQLDdlParser createDdlDialect() {
        return MySQLDdlParser.create(this);
    }

    @Override
    protected final CriteriaException supportChildInsert(final _Insert._ChildInsert childStmt, final Visible visible) {

        return null;
    }


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

    @Override
    protected final void standardLockClause(final SQLToken lockMode, final _SqlContext context) {
        if (!_Constant.SPACE_FOR_UPDATE.equals(lockMode.spaceRender())) {
            throw _Exceptions.castCriteriaApi();
        }
        context.sqlBuilder().append(_Constant.SPACE_FOR_UPDATE);
    }

    /// @see #update(UpdateStatement, boolean, io.army.session.SessionSpec)
    @Override
    protected final void parseDomainChildUpdate(final _SingleUpdate update, final _UpdateContext ctx) {
        final _MultiUpdateContext context = (_MultiUpdateContext) ctx;


        final ChildTableMeta<?> childTable = (ChildTableMeta<?>) update.table();
        final ParentTableMeta<?> parentTable = childTable.parentMeta();
        final String safeParentTableAlias = context.saTableAliasOf(parentTable);

        // 1. UPDATE clause
        final StringBuilder sqlBuilder = context.sqlBuilder();
        if (!sqlBuilder.isEmpty()) {
            sqlBuilder.append(_Constant.SPACE);
        }
        sqlBuilder.append(_Constant.UPDATE);

        //2. child join parent
        this.appendChildJoinParent(safeParentTableAlias, sqlBuilder, context.saTableAliasOf(childTable), childTable);

        //3. set clause
        this.multiTableChildSetClause(update, context);

        //4. where clause
        this.dmlWhereClause(update.wherePredicateList(), context);


        //4.1 append discriminator for child
        this.discriminator(childTable, safeParentTableAlias, context);

        //4.2 append condition fields
        context.appendConditionFields();

        //4.3 append visible
        if (parentTable.containField(_MetaBridge.VISIBLE)) {
            this.visiblePredicate(parentTable, safeParentTableAlias, context, false);
        }
    }


    /// @see <a href="https://dev.mysql.com/doc/refman/8.0/en/delete.html">DELETE Statement</a>
    @Override
    protected final void parseDomainChildDelete(final _SingleDelete delete, final _DeleteContext ctx) {
        final _MultiDeleteContext context = (_MultiDeleteContext) ctx;

        final ChildTableMeta<?> childTable = (ChildTableMeta<?>) delete.table();
        final ParentTableMeta<?> parentTable = childTable.parentMeta();

        // 1. delete clause
        final StringBuilder sqlBuilder;
        if (!(sqlBuilder = context.sqlBuilder()).isEmpty()) {
            sqlBuilder.append(_Constant.SPACE);
        }
        sqlBuilder.append(_Constant.DELETE_SPACE);

        final String safeParentTableAlias, safeChildTableAlias;
        safeChildTableAlias = context.saTableAliasOf(childTable);
        safeParentTableAlias = context.saTableAliasOf(parentTable);

        sqlBuilder.append(safeChildTableAlias)// child table alias
                .append(_Constant.SPACE_COMMA_SPACE)
                .append(safeParentTableAlias)// parent table name
                .append(_Constant.SPACE_FROM);

        //2. child join parent
        this.appendChildJoinParent(safeParentTableAlias, sqlBuilder, safeChildTableAlias, childTable);

        //3. where clause
        this.dmlWhereClause(delete.wherePredicateList(), context);

        //3.1 append discriminator for child
        this.discriminator(childTable, safeParentTableAlias, context);

        //3.2 append visible
        if (parentTable.containField(_MetaBridge.VISIBLE)) {
            this.visiblePredicate(parentTable, safeParentTableAlias, context, false);
        }
    }



    /*################################## blow properties template method ##################################*/


    @Override
    final Set<String> createKeyWordSet(ServerMeta serverMeta) {
        final Set<String> keyWordSet;
        switch ((MySQLDialect) serverMeta.usedDialect()) {
            case MySQL55:
            case MySQL56:
            case MySQL57:
                keyWordSet = _MySQLDialectUtils.create57KeywordsSet();
                break;
            case MySQL80:
                keyWordSet = _MySQLDialectUtils.create80KeywordsSet();
                break;
            default:
                throw _Exceptions.unexpectedEnum((Enum<?>) dialect);
        }
        return keyWordSet;
    }


    @Override
    final IdentifierHandler createIdentifierHandler(ServerMeta serverMeta) {
        return new MySQLIdentifierHandler(serverMeta);
    }

    @Override
    final LiteralHandler createLiteralHandler() {
        return new MySQLLiteralHandler(this);
    }

    @Override
    final int capabilitiesBitSet(final ServerMeta serverMeta) {

        final int supportRowAlias, supportSingleDeleteAlas, supportWithClause, supportWindowClause;
        final int supportValidateUnionType;

        final boolean asOf80 = serverMeta.usedDialect().compareWith(MySQLDialect.MySQL80) >= 0;
        if (asOf80) {
            supportRowAlias = SUPPORT_ROW_ALIAS;
            supportSingleDeleteAlas = SUPPORT_SINGLE_DELETE_ALIAS;
            supportWithClause = SUPPORT_WITH_CLAUSE;
            supportWindowClause = SUPPORT_WINDOW_CLAUSE;
        } else {
            supportRowAlias = 0;
            supportSingleDeleteAlas = 0;
            supportWithClause = 0;
            supportWindowClause = 0;

        }

        if (asOf80) {
            // MySQL 8.0 add INTERSECT and EXCEPT
            supportValidateUnionType = 0;
        } else {
            supportValidateUnionType = SUPPORT_VALIDATE_UNION_TYPE;
        }
        return supportRowAlias
                | supportSingleDeleteAlas
                | supportWithClause
                | supportWindowClause
                | supportValidateUnionType
                | SUPPORT_TABLE_ALIAS_AFTER_AS
                | SUPPORT_ONLY_DEFAULT
                | SUPPORT_SINGLE_UPDATE_ALIAS
                | SUPPORT_SET_CLAUSE_TABLE_ALIAS
                ;
    }

    @Override
    protected final char identifierDelimitedQuote() {
        return BACKTICK;
    }

    @Override
    protected final ChildUpdateMode childUpdateMode() {
        return ChildUpdateMode.MULTI_TABLE;
    }


    @Override
    protected final void validateUnionType(final _UnionType unionType) {
        switch (unionType) {
            case UNION:
            case UNION_ALL:
            case UNION_DISTINCT:
                break;
            case EXCEPT:
            case EXCEPT_ALL:
            case EXCEPT_DISTINCT:
            case INTERSECT:
            case INTERSECT_ALL:
            case INTERSECT_DISTINCT: {
                String m = String.format("%s don't support %s", this.dialect, unionType.name());
                throw new CriteriaException(m);
            }
            default:
                //no bug,never here
                throw _Exceptions.unexpectedEnum(unionType);
        }
    }


    /*################################## blow private method ##################################*/

//    private static void re(){
//        switch ((MySqlType) sqlType) {
//            case INT:
//            case BIGINT:
//            case DECIMAL:
//            case BOOLEAN:
//            case DATETIME:
//            case DATE:
//            case TIME:
//            case YEAR:
//
//            case CHAR:
//            case VARCHAR:
//            case ENUM:
//            case JSON:
//            case SET:
//            case TINYTEXT:
//            case TEXT:
//            case MEDIUMTEXT:
//            case LONGTEXT:
//
//            case BINARY:
//            case VARBINARY:
//            case TINYBLOB:
//            case BLOB:
//            case MEDIUMBLOB:
//            case LONGBLOB:
//
//            case BIT:
//            case FLOAT:
//            case DOUBLE:
//
//            case TINYINT:
//            case TINYINT_UNSIGNED:
//            case SMALLINT:
//            case SMALLINT_UNSIGNED:
//            case MEDIUMINT:
//            case MEDIUMINT_UNSIGNED:
//            case INT_UNSIGNED:
//            case BIGINT_UNSIGNED:
//            case DECIMAL_UNSIGNED:
//
//            case POINT:
//            case LINESTRING:
//            case POLYGON:
//            case MULTIPOINT:
//            case MULTIPOLYGON:
//            case MULTILINESTRING:
//            case GEOMETRYCOLLECTION:
//            break;
//            default:
//                throw _Exceptions.unexpectedEnum((MySqlType) sqlType);
//        }
//    }


    private static final class Standard extends MySQLParser {

        private Standard(DialectEnv environment, MySQLDialect dialect) {
            super(environment, dialect);
        }

    }//Standard


}
