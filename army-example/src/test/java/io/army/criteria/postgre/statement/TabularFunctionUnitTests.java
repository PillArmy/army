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

package io.army.criteria.postgre.statement;

import io.army.criteria.Expression;
import io.army.criteria.Select;
import io.army.criteria.TypedExpression;
import io.army.criteria.impl.Postgres;
import io.army.criteria.impl.SQLs;
import io.army.example.bank.domain.user.ChinaRegion_;
import io.army.mapping.array.IntegerArrayType;
import io.army.mapping.array.LongArrayType;
import io.army.mapping.postgre.PgVectorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.List;

import static io.army.criteria.impl.Postgres.array;
import static io.army.criteria.impl.Postgres.unnest;
import static io.army.criteria.impl.SQLs.*;

public class TabularFunctionUnitTests extends PostgreUnitTests {

    private static final Logger LOG = LoggerFactory.getLogger(TabularFunctionUnitTests.class);

    /**
     * @see Postgres#unnest(TypedExpression)
     */
    @Test
    public void unnestFunc() {
        final Select stmt;
        stmt = Postgres.query()
                .select(s -> s.space("a", DOT, ASTERISK))
                .from(unnest(SQLs.literal(PgVectorType.INSTANCE, "cat:3 fat:2,4 rat:5A"))
                        ::withOrdinality
                )
                .as("a")
                .where(SQLs.refField("a", "lexeme").equal(SQLs.literalValue("cat")))
                .asQuery();

        printStmt(LOG, stmt);

    }

    /**
     * @see Postgres#unnest(TypedExpression)
     */
    @Test
    public void unnestArray() {
        final Select stmt;
        stmt = Postgres.query()
                .select(s -> s.space("a", DOT, ASTERISK)
                        .comma("b", DOT, ASTERISK)
                )
                .from(unnest(SQLs.literal(IntegerArrayType.LINEAR, new int[]{1, 2}))
                        ::withOrdinality
                )
                .as("a").parens("value", "ordinal")
                .crossJoin(unnest(array(List.of(1, 2, 3)).mapTo(IntegerArrayType.LINEAR))::withOrdinality)
                .as("b").parens("value", "ordinal")
                .asQuery();

        printStmt(LOG, stmt);
    }

    /**
     * @see Postgres#unnest(Expression, Expression)
     */
    @Test
    public void unnestMultiArray() {
        final Select stmt;
        stmt = Postgres.query()
                .select(s -> s.space("a", DOT, ASTERISK))
                .from(unnest(array(List.of(1, 2, 3)), array(List.of(1, 2, 3)))::withOrdinality)
                .as("a").parens("value1", "value2", "original")
                .asQuery();

        printStmt(LOG, stmt);
    }


    @Test
    public void unnestQueryArray() {
        final Select stmt;
        stmt = Postgres.query()
                .select(s -> s.space("a", DOT, ASTERISK))
                .from(unnest(array(Postgres.subQuery()
                                .select(ChinaRegion_.id)
                                .from(ChinaRegion_.T, AS, "c")
                                .limit(SQLs::literal, 10)
                                .asQuery()
                        ).mapTo(LongArrayType.LINEAR)
                )::withOrdinality)
                .as("a").parens("value", "original")
                .asQuery();

        printStmt(LOG, stmt);
    }


}
