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
import io.army.criteria.impl._LiteralExpression;
import io.army.criteria.impl._SQLConsultant;
import io.army.criteria.impl._UnionType;
import io.army.criteria.impl.inner.*;
import io.army.criteria.standard.StandardStatement;
import io.army.executor.ExecutorSupport;
import io.army.lang.Nullable;
import io.army.mapping.*;
import io.army.meta.ChildTableMeta;
import io.army.meta.ParentTableMeta;
import io.army.meta.ServerMeta;
import io.army.modelgen._MetaBridge;
import io.army.session.Visible;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLType;
import io.army.stmt.SimpleStmt;
import io.army.stmt.Stmts;
import io.army.struct.TypeCategory;
import io.army.util.ArrayUtils;
import io.army.util._Exceptions;

import java.util.List;
import java.util.Objects;
import java.util.Set;

abstract class PostgreParser extends _ArmyDialectParser {


    PostgreParser(DialectEnv environment, PostgreDialect dialect) {
        super(environment, dialect);
        switch (this.literalEscapeMode) {
            case DEFAULT:
            case BACK_SLASH:
            case UNICODE:
                break;
            default:
                throw _Exceptions.literalEscapeModeError(this.literalEscapeMode, this.dialect);
        }

        switch (this.identifierEscapeMode) {
            case DEFAULT:
            case UNICODE:
                break;
            default:
                throw _Exceptions.literalEscapeModeError(this.identifierEscapeMode, this.dialect);
        }

    }


    @Override
    protected final MappingHandler createTypeMappingHandler(DialectEnv env) {
        return new PgMappingHandler(env);
    }

    ///
    /// Original Query defined type stmts,This statement is not used to unify processing logic for all dialects.
    /// 1. Identify composite type: pg_type.typcategory is 'C' and pg_class.relkind is 'c'
    /// 2. Identify domain type: pg_type.typbasetype > 0 ,If this is a domain (see typtype),
    /// then typbasetype identifies the type that this one is based on. Zero if this type is not a domain.
    ///
    /// 1. [pg_type](https://www.postgresql.org/docs/current/catalog-pg-type.html)
    /// 2. [pg_class.relkind](https://www.postgresql.org/docs/current/catalog-pg-class.html)
    ///
    /// ```postgresql
    /// SELECT t.typname AS "definedType", t.typcategory AS "type", et."enumLabelArray", at.*
    /// FROM pg_namespace AS n
    /// JOIN pg_type AS t ON t.typnamespace = n.oid
    /// LEFT JOIN LATERAL (
    /// SELECT e.enumtypid, array_agg(e.enumlabel) AS "enumLabelArray"
    /// FROM pg_enum AS e
    /// WHERE e.enumtypid = t.oid
    /// GROUP BY e.enumtypid
    /// ) AS et ON et.enumtypid = t.oid
    /// LEFT JOIN pg_class AS c ON c.oid = t.typrelid
    /// LEFT JOIN LATERAL (
    /// SELECT a.attrelid,
    /// array_agg(a.attnum)   AS "columnNumArray",
    /// array_agg(a.attname)  AS "columnNameArray",
    /// array_agg(st.typname) AS "columnTypeArray"
    /// FROM pg_attribute AS a
    /// JOIN pg_type AS st ON st.oid = a.atttypid
    /// WHERE a.attrelid = c.oid
    /// GROUP BY a.attrelid
    /// ) AS at ON at.attrelid = c.oid
    /// WHERE n.nspname NOT IN ('pg_catalog', 'information_schema')
    /// AND (t.typcategory IN ('E', 'U', 'R') OR t.typbasetype > 0 OR (t.typcategory = 'C' AND c.relkind = 'c'))
    /// ```
    @Override
    public final List<SimpleStmt> queryDefinedTypeStmts(Set<MappingType> definedTypeSet) {
        final String sql;
        sql = """
                SELECT t.typname AS "typeName",
                       CASE(t.typcategory)
                           WHEN 'C' THEN 'COMPOSITE'
                           WHEN 'E' THEN 'ENUM'
                           WHEN 'R' THEN 'RANGE'
                           ELSE CASE
                                    WHEN t.typbasetype > 0 THEN 'DOMAIN'
                                    ELSE 'UNKNOWN'
                               END
                           END AS "category" ,
                       et.enumlabel  AS "enumLabel",
                       et."enumOrder",
                       at.attnum AS "comFieldOrder",
                       at.attname AS "comFieldName",
                       at.typname AS "comFieldType",
                       at.collname AS "comFieldCollation",
                       pg_catalog.format_type(t.typbasetype, t.typtypmod) AS "baseType",
                       co.collname AS "collation",
                       t.typnotnull AS "notNull",
                       t.typdefault AS "default",
                       CASE WHEN cs.conname IS NOT NULL THEN cs.conname  ELSE NULL END AS "constraintName",
                       CASE WHEN cs.conname IS NOT NULL THEN  pg_get_constraintdef(cs.oid, true) ELSE NULL END AS "check",
                       (SELECT rt.typname FROM pg_type AS rt WHERE rt.oid = r.rngsubtype) AS "rangeSubType",
                       (SELECT rc.collname FROM pg_collation AS rc WHERE rc.oid = r.rngcollation) AS "rangeCollation",
                       (SELECT rt.typname FROM pg_type AS rt WHERE rt.oid = r.rngmultitypid) AS "rangeMulti",
                       (SELECT ro.opcname FROM pg_opclass AS ro WHERE ro.oid = r.rngsubopc) AS "rangeSubOpc",
                       (SELECT ro.proname FROM pg_proc AS ro WHERE ro.oid = r.rngcanonical) AS "rangeCanonical",
                       (SELECT ro.proname FROM pg_proc AS ro WHERE ro.oid = r.rngsubdiff) AS "rangeSubDiff"
                FROM pg_namespace AS n
                         JOIN pg_type AS t ON t.typnamespace = n.oid
                         LEFT JOIN LATERAL (
                    SELECT e.enumtypid,e.enumlabel,row_number() OVER (ORDER by e.enumsortorder) AS "enumOrder"
                    FROM pg_enum AS e
                    WHERE e.enumtypid = t.oid
                    ORDER BY e.enumsortorder
                    ) AS et ON et.enumtypid = t.oid
                         LEFT JOIN pg_class AS c ON c.oid = t.typrelid
                         LEFT JOIN LATERAL (
                    SELECT a.attrelid,a.attnum ,a.attname,st.typname,sc.collname
                    FROM pg_attribute AS a
                             JOIN pg_type AS st ON st.oid = a.atttypid
                             LEFT JOIN pg_collation as sc ON sc.oid = a.attcollation
                    WHERE a.attrelid = c.oid
                    order by  a.attnum
                    ) AS at ON at.attrelid = c.oid
                         LEFT JOIN pg_collation AS co  ON co.oid = t.typcollation
                         LEFT JOIN pg_constraint cs ON cs.contypid = t.oid AND cs.contype = 'c'
                         LEFT JOIN pg_range AS r ON r.rngtypid = t.oid
                WHERE n.nspname NOT IN ('pg_catalog', 'information_schema')
                  AND (t.typcategory IN ('E', 'R') OR t.typbasetype > 0 OR (t.typcategory = 'C' AND c.relkind = 'c'))
                ORDER BY t.typname ,at.attnum,et."enumOrder"
                """;

        final List<Selection> selectionList;
        selectionList = List.of(
                _SQLConsultant.forName("typeName", StringType.INSTANCE),
                _SQLConsultant.forName("category", NameEnumType.from(TypeCategory.class)),

                _SQLConsultant.forName("enumLabel", StringType.INSTANCE),
                _SQLConsultant.forName("enumOrder", IntegerType.INSTANCE),

                _SQLConsultant.forName("comFieldOrder", IntegerType.INSTANCE),
                _SQLConsultant.forName("comFieldName", StringType.INSTANCE),
                _SQLConsultant.forName("comFieldType", MappingTypeType.INSTANCE),
                _SQLConsultant.forName("comFieldCollation", StringType.INSTANCE),

                _SQLConsultant.forName("baseType", StringType.INSTANCE),
                _SQLConsultant.forName("collation", StringType.INSTANCE),
                _SQLConsultant.forName("notNull", BooleanType.INSTANCE),
                _SQLConsultant.forName("default", StringType.INSTANCE),
                _SQLConsultant.forName("constraintName", StringType.INSTANCE),
                _SQLConsultant.forName("check", StringType.INSTANCE),

                _SQLConsultant.forName("rangeSubType", StringType.INSTANCE),
                _SQLConsultant.forName("rangeCollation", StringType.INSTANCE),
                _SQLConsultant.forName("rangeMulti", StringType.INSTANCE),
                _SQLConsultant.forName("rangeSubOpc", StringType.INSTANCE),
                _SQLConsultant.forName("rangeCanonical", StringType.INSTANCE),
                _SQLConsultant.forName("rangeSubDiff", StringType.INSTANCE)
        );
        return List.of(Stmts.simpleRead(sql, List.of(), selectionList));
    }


    @Override
    public final void typeName(final MappingType type, final StringBuilder sqlBuilder) {
        final DataType dataType;
        dataType = type.map(this.serverMeta);

        if (!(dataType instanceof PgType)) { // user defined type or unrecognized type
            unrecognizedTypeName(type, dataType, true, sqlBuilder);
        } else if (dataType.isArray()) {
            final SQLType elementType;
            elementType = ((PgType) dataType).elementType();
            Objects.requireNonNull(elementType);
            safeObjectName(elementType, sqlBuilder);
            arrayTypeName(ArrayUtils.dimensionOfType(type), sqlBuilder);
        } else switch ((PgType) dataType) {
            case REF_CURSOR:
            case UNKNOWN:
                throw ExecutorSupport.mapMethodError(type, dataType);
            default:
                sqlBuilder.append(dataType.typeName());

        } // switch

    }


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
            postgreWithClause(cteList, spec.isRecursive(), context);
        }
    }

    protected void postgreWithClause(List<_Cte> cteList, boolean recursive, _SqlContext mainContext) {
        throw _Exceptions.dontSupportWithClause(this.dialect);
    }




    @Override
    protected final void arrayTypeName(final int dimension, final StringBuilder sqlBuilder) {
        assert dimension > 0;
        for (int i = 0; i < dimension; i++) {
            sqlBuilder.append("[]");
        }

    }


    @Override
    protected final PostgreDdlParser createDdlDialect() {
        return PostgreDdlParser.create(this);
    }

    @Override
    protected final CriteriaException supportChildInsert(_Insert._ChildInsert childStmt, Visible visible) {
        return null;
    }


    @Override
    final Set<String> createKeyWordSet(ServerMeta serverMeta) {
        return _PostgreDialectUtils.createKeywordsSet();
    }


    @Override
    final IdentifierHandler createIdentifierHandler(ServerMeta serverMeta) {
        return _PostgreDialectUtils::handleIdentifier;
    }

    @Override
    final LiteralHandler createLiteralHandler() {
        return new PostgreLiteralHandler(this);
    }

    @Override
    final int capabilitiesBitSet(ServerMeta serverMeta) {
        final int capabilities = 0; // must be int type
        // Postgre don't support table alias in SET clause
        // Postgre don't support update derived field
        // Postgre donot validate union type
        return capabilities
                | SUPPORT_ZONE  // Postgre support zone
                | SUPPORT_TABLE_ALIAS_AFTER_AS      // Postgre support AS key word
                | SUPPORT_WITH_CLAUSE               // Postgre support WITH clause
                | SUPPORT_WITH_CLAUSE_IN_INSERT     // Postgre support WITH clause in INSERT statement
                | SUPPORT_WINDOW_CLAUSE             // Postgre support WINDOW clause
                | SUPPORT_UPDATE_ROW                // Postgre support update row
                | SUPPORT_JOINABLE_SINGLE_UPDATE    // true ,Postgre support single-table joinable update https://www.postgresql.org/docs/current/sql-update.html
                | SUPPORT_RETURNING_CLAUSE           // Postgre support RETURNING clause
                | SUPPORT_SINGLE_UPDATE_ALIAS        // Postgre support single table update alias
                | SUPPORT_SINGLE_DELETE_ALIAS       // Postgre support single table DELETE alias
                | SUPPORT_ROW_ALIAS
                | SUPPORT_TABLE_ONLY
                ;
    }

    @Override
    protected final char identifierDelimitedQuote() {
        return _Constant.DOUBLE_QUOTE;
    }


    @Override
    protected final ChildUpdateMode childUpdateMode() {
        // Postgre support DML in cte.
        return ChildUpdateMode.CTE;
    }


    @Override
    protected final void validateUnionType(_UnionType unionType) {
        //no-op, no bug never here
    }

    @Override
    protected final void parseAssignmentInsert(_AssignmentInsertContext context, _Insert._AssignmentInsert insert) {
        throw _Exceptions.dontSupportAssignmentInsert(this.dialect);
    }

    @Override
    protected final void parseDomainChildUpdate(final _SingleUpdate stmt, final _UpdateContext context) {

        final _SingleUpdateContext childContext = (_SingleUpdateContext) context;
        final _SingleUpdateContext parentContext = (_SingleUpdateContext) childContext.parentContext();
        assert parentContext != null;

        final String safeParentTableName, safeChildTableName, safeParentAlias, safeChildTableAlias;

        // child table part
        final ChildTableMeta<?> domainTable = (ChildTableMeta<?>) childContext.domainTable();
        assert domainTable == stmt.table() && domainTable == childContext.targetTable();
        safeChildTableName = this.safeObjectName(domainTable);
        safeChildTableAlias = childContext.safeTargetTableAlias();

        // parent table part
        final ParentTableMeta<?> parentTable = (ParentTableMeta<?>) parentContext.targetTable();
        assert domainTable.parentMeta() == parentTable;
        safeParentTableName = this.safeObjectName(parentTable);
        safeParentAlias = parentContext.safeTargetTableAlias();


        final StringBuilder sqlBuilder;
        sqlBuilder = childContext.sqlBuilder();
        assert parentContext.sqlBuilder() == sqlBuilder; // must assert


        if (sqlBuilder.length() > 0) {
            sqlBuilder.append(_Constant.SPACE);
        }


        // append child table update cte statement
        final String childCte;
        childCte = this.identifier(childContext.targetTableAlias() + "_update_cte");
        sqlBuilder.append(_Constant.WITH)
                .append(_Constant.SPACE)
                .append(childCte)
                .append(_Constant.SPACE_AS)
                .append(_Constant.SPACE_LEFT_PAREN)
                .append(_Constant.SPACE)
                .append(_Constant.UPDATE)
                .append(_Constant.SPACE_ONLY)
                .append(_Constant.SPACE)
                .append(safeChildTableName) // child table name.
                .append(_Constant.SPACE_AS_SPACE)
                .append(safeChildTableAlias);

        this.singleTableSetClause(((_DomainUpdate) stmt).childItemPairList(), childContext); // child SET clause

        sqlBuilder.append(_Constant.SPACE_FROM_SPACE)
                .append(safeParentTableName)
                .append(_Constant.SPACE_AS_SPACE)
                .append(safeParentAlias);

        // child cte WHERE clause
        this.childDomainCteWhereClause(stmt.wherePredicateList(), childContext);
        this.discriminator(domainTable, safeParentAlias, childContext);
        childContext.appendConditionFields();
        if (parentTable.containField(_MetaBridge.VISIBLE)) {
            this.visiblePredicate(parentTable, safeParentAlias, childContext, false);
        }

        final String safeIdColumnName;
        safeIdColumnName = safeObjectName(domainTable.id());

        // RETURNING clause
        sqlBuilder.append(_Constant.SPACE_RETURNING)
                .append(_Constant.SPACE)
                .append(safeChildTableAlias)
                .append(_Constant.PERIOD)
                .append(safeIdColumnName);

        sqlBuilder.append(_Constant.SPACE_AS_SPACE)
                .append(_MetaBridge.ID)
                .append(_Constant.SPACE_RIGHT_PAREN);

        // child cte end

        // below primary UPDATE statement part, parent table.
        sqlBuilder.append(_Constant.SPACE)
                .append(_Constant.UPDATE)
                .append(_Constant.SPACE_ONLY)
                .append(_Constant.SPACE)
                .append(safeParentTableName) // parent table name.
                .append(_Constant.SPACE_AS_SPACE)
                .append(safeParentAlias);

        this.singleTableSetClause(stmt.itemPairList(), parentContext); // parent SET clause

        // parent part FROM clause
        sqlBuilder.append(_Constant.SPACE_FROM_SPACE)
                .append(childCte);

        if (((_DmlContext._DomainUpdateSpec) parentContext).isExistsChildFiledInSetClause()) { // after SET clause
            // append join child table
            sqlBuilder.append(_Constant.SPACE_JOIN_SPACE)
                    .append(safeChildTableName)
                    .append(_Constant.SPACE_AS_SPACE)
                    .append(safeChildTableAlias)
                    .append(_Constant.SPACE_ON_SPACE)
                    .append(safeChildTableAlias)
                    .append(_Constant.PERIOD)
                    .append(safeIdColumnName)
                    .append(_Constant.SPACE_EQUAL_SPACE)
                    .append(childCte)
                    .append(_Constant.PERIOD)
                    .append(_MetaBridge.ID);

        }

        sqlBuilder.append(_Constant.SPACE_WHERE)
                .append(_Constant.SPACE)
                .append(safeParentAlias)
                .append(_Constant.PERIOD)
                .append(safeIdColumnName)
                .append(_Constant.SPACE_EQUAL_SPACE)
                .append(childCte)
                .append(_Constant.PERIOD)
                .append(_MetaBridge.ID);


    }

    /// @see <a href="https://www.postgresql.org/docs/current/sql-delete.html">Postgre DELETE syntax</a>
    @Override
    protected final void parseDomainChildDelete(final _SingleDelete stmt, final _DeleteContext context) {

        final _SingleDeleteContext childContext = (_SingleDeleteContext) context;
        final _SingleDeleteContext parentContext = (_SingleDeleteContext) childContext.parentContext();
        assert parentContext != null;

        final String safeParentTableName, safeChildTableName, safeParentAlias, safeChildTableAlias;

        // child table part
        final ChildTableMeta<?> domainTable = (ChildTableMeta<?>) childContext.domainTable();
        assert domainTable == stmt.table() && domainTable == childContext.targetTable();
        safeChildTableName = this.safeObjectName(domainTable);
        safeChildTableAlias = childContext.safeTargetTableAlias();

        // parent table part
        final ParentTableMeta<?> parentTable = (ParentTableMeta<?>) parentContext.targetTable();
        assert domainTable.parentMeta() == parentTable;
        safeParentTableName = this.safeObjectName(parentTable);
        safeParentAlias = parentContext.safeTargetTableAlias();


        final StringBuilder sqlBuilder;
        sqlBuilder = childContext.sqlBuilder();
        assert parentContext.sqlBuilder() == sqlBuilder; // must assert

        if (sqlBuilder.length() > 0) {
            sqlBuilder.append(_Constant.SPACE);
        }

        // append child table DELETE cte statement
        final String deleteCte;
        deleteCte = this.identifier(childContext.targetTableAlias() + "_delete_cte");
        sqlBuilder.append(_Constant.WITH)
                .append(_Constant.SPACE)
                .append(deleteCte)
                .append(_Constant.SPACE_AS)
                .append(_Constant.SPACE_LEFT_PAREN)
                .append(_Constant.SPACE)
                .append(_Constant.DELETE_FROM)
                .append(_Constant.SPACE_ONLY)
                .append(_Constant.SPACE)
                .append(safeChildTableName)// child table name.
                .append(_Constant.SPACE_AS_SPACE)
                .append(safeChildTableAlias)
                .append(_Constant.SPACE_USING)
                .append(_Constant.SPACE)
                .append(safeParentTableName)
                .append(_Constant.SPACE_AS_SPACE)
                .append(safeParentAlias);

        // child cte WHERE clause
        this.childDomainCteWhereClause(stmt.wherePredicateList(), childContext);
        this.discriminator(domainTable, safeParentAlias, context);
        if (parentTable.containField(_MetaBridge.VISIBLE)) {
            this.visiblePredicate(parentTable, safeParentAlias, childContext, false);
        }

        final String safeIdColumnName;
        safeIdColumnName = safeObjectName(domainTable.id());

        // RETURNING clause
        sqlBuilder.append(_Constant.SPACE_RETURNING)
                .append(_Constant.SPACE)
                .append(safeChildTableAlias)
                .append(_Constant.PERIOD)
                .append(safeIdColumnName);

        sqlBuilder.append(_Constant.SPACE_AS_SPACE)
                .append(_MetaBridge.ID)
                .append(_Constant.SPACE_RIGHT_PAREN);

        // child cte end


        // below primary DELETE statement part, parent table.
        sqlBuilder.append(_Constant.SPACE)
                .append(_Constant.DELETE_FROM)
                .append(_Constant.SPACE_ONLY)
                .append(_Constant.SPACE)
                .append(safeParentTableName)// parent table name.
                .append(_Constant.SPACE_AS_SPACE)
                .append(safeParentAlias)
                .append(_Constant.SPACE_USING)   // parent part USING clause
                .append(_Constant.SPACE)
                .append(deleteCte)
                .append(_Constant.SPACE_WHERE)
                .append(_Constant.SPACE)
                .append(safeParentAlias)
                .append(_Constant.PERIOD)
                .append(safeIdColumnName)
                .append(_Constant.SPACE_EQUAL_SPACE)
                .append(deleteCte)
                .append(_Constant.PERIOD)
                .append(_MetaBridge.ID);
    }

    @Override
    protected final void standardLimitClause(final @Nullable _Expression offset, final @Nullable _Expression rowCount,
                                             final _SqlContext context) {

        final StringBuilder sqlBuilder;
        sqlBuilder = context.sqlBuilder();
        if (rowCount != null) {
            sqlBuilder.append(_Constant.SPACE_LIMIT);
            if (rowCount instanceof LiteralExpression) {
                ((_LiteralExpression) rowCount).appendSqlWithoutType(sqlBuilder, context);
            } else {
                rowCount.appendSql(sqlBuilder, context);
            }

        }
        if (offset != null) {
            sqlBuilder.append(_Constant.SPACE_OFFSET);
            if (rowCount instanceof LiteralExpression) {
                ((_LiteralExpression) offset).appendSqlWithoutType(sqlBuilder, context);
            } else {
                offset.appendSql(sqlBuilder, context);
            }
        }

    }

    @Override
    protected final void standardLockClause(SQLToken lockMode, _SqlContext context) {
        if (!_Constant.SPACE_FOR_UPDATE.equals(lockMode.spaceRender())) {
            throw _Exceptions.castCriteriaApi();
        }
        context.sqlBuilder().append(_Constant.SPACE_FOR_UPDATE);
    }

    @Override
    protected final void parseMultiUpdate(_MultiUpdate update, _MultiUpdateContext context) {
        // Postgre don't support multi-table UPDATE syntax
        throw _Exceptions.unexpectedStatement((Statement) update);
    }

    @Override
    protected final void parseMultiDelete(_MultiDelete delete, _MultiDeleteContext context) {
        // Postgre don't support multi-table DELETE syntax
        throw _Exceptions.unexpectedStatement(delete);
    }

    /*-------------------below private methods -------------------*/






}
