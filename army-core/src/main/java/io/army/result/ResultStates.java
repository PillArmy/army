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

package io.army.result;

import io.army.executor.DataAccessException;
import io.army.lang.Nullable;
import io.army.option.Option;
import io.army.session.StmtOption;
import io.army.spec.OptionSpec;

import java.util.function.Consumer;

public interface ResultStates extends ResultItem, OptionSpec {

    Consumer<ResultStates> IGNORE_STATES = states -> {
    };

/// Get batch statement batch size.
/// **NOTE**: if {@link StmtOption#isParseBatchAsMultiStmt()} is true , then always return 0.
/// @return 0 or batch size.
    int batchSize();

/// Get batch No (based 1)
/// @return 
/// - If {@link #batchSize()} is 0, then 0
/// - Else batch No (based 1)
/// 
    int batchNo();

    /// Whether support {@link #lastInsertedId()} method or not.
/// If false ,then database usually support RETURNING clause,it better than lastInsertedId ,for example : PostgreSQL
/// @return true : support
    boolean isSupportInsertId();

/// the last inserted id, a unsigned long .
/// **NOTE**:
/// 
/// - when {@link #isSupportInsertId()} is false,throw {@link DataAccessException} . now database usually support RETURNING clause,it better than lastInsertedId ,for example : PostgreSQL
/// - when use multi-row insert syntax ,the last inserted id is the first row id.
/// - If you use multi-row insert syntax and exists conflict clause(e.g : MySQL ON DUPLICATE KEY UPDATE),then database never return correct lastInsertedId
/// - Due to army auto append RETURNING id for INSERT statement if database support(eg: postgre),so database server don't return last inserted id
/// 
/// @return the last inserted id
/// @throws DataAccessException throw when {@link #isSupportInsertId()} return false
    long lastInsertedId() throws DataAccessException;

    boolean inTransaction();

    long affectedRows();

    /// @return empty or  success info(maybe contain warning info)
    String message();


/// Whether exists more result after this result or not .
/// 
/// - simple single statement always false
/// - multi-result statement,for example stored procedure ,multi-statement ,last result is false,before last result is true
/// - batch statement,for example,batch update/query  ,last batch item is false,before last batch item is true
/// 
/// @return true : exists more result after this result
    boolean hasMoreResult();

    /// @return true : this {@link ResultStates} is the last {@link ResultStates} of current stream.
    boolean isLastStates();


    /// @return true representing exists server cursor and the last row don't send.
/// @see #hasColumn()
    boolean hasMoreFetch();

/// Test result whether is query result or not.
/// **NOTE** : Army auto append RETURNING id clause for INSERT statement that primary key is auto increment.
/// @return 
/// - true : this instance representing the terminator of query result (eg: SELECT command)
/// - false : this instance representing the update result (eg: INSERT/UPDATE/DELETE command)
/// 
/// @see #rowCount()
    boolean hasColumn();

/// @return the row count.
/// 
/// - If {@link #hasColumn()} is false, then 0
/// - Else if use fetch (eg: {@code  Statement#setFetchSize(int)} , {@code  RefCursor}) , then the row count representing only the row count of current fetch result.
/// - Else then the row count representing the total row count of query result.
/// 
/// @see #hasColumn()
    long rowCount();


    @Nullable
    Warning warning();


/// Just for following methods of {@link io.army.session.Session}:
/// 
/// - query()
/// - queryObject()
/// - queryRecord()
/// 
/// <pre>
/// <code>
/// &#64;Transactional
/// &#64;Test
/// public void returningDomainInsertChildWithTowStmtQueryMode(final SyncLocalSession session) {
/// final List<ChinaProvince> provinceList;
/// provinceList = createProvinceListWithCount(3);
/// final ReturningInsert stmt;
/// stmt = Postgres.singleInsert()
/// .insertInto(ChinaRegion_.T)
/// .values(provinceList)
/// .returningAll()
/// .asReturningInsert()
/// .child()
/// .insertInto(ChinaProvince_.T)
/// .values(provinceList)
/// .returningAll()
/// .asReturningInsert();
/// final int[] flagHolder = new int[]{0};
/// final Consumer<ResultStates> statesConsumer;
/// statesConsumer = states -> {
/// flagHolder[0] ++;
/// if (flagHolder[0] == 1) {
/// Assert.assertTrue(secondDmlStates == null || !secondDmlStates);
/// } else {
/// Assert.assertEquals(secondDmlStates, Boolean.TRUE);
/// }
/// Assert.assertEquals(states.affectedRows(), provinceList.size());
/// if (states.isSupportInsertId()) {
/// Assert.assertEquals(states.lastInsertedId(), provinceList.get(0).getId());
/// }
/// Assert.assertEquals(states.batchSize(), 0);
/// Assert.assertEquals(states.batchNo(), 0);
/// Assert.assertEquals(states.resultNo(), 1);
/// Assert.assertFalse(states.hasMoreResult());
/// Assert.assertFalse(states.hasMoreFetch());
/// Assert.assertTrue(states.inTransaction());
/// Assert.assertTrue(states.hasColumn());
/// Assert.assertEquals(states.rowCount(), states.affectedRows());
/// };
/// final List<ChinaProvince> resultList;
/// resultList = session.queryList(stmt, ChinaProvince.class, ArrayList::new,SyncStmtOption.stateConsumer(statesConsumer));
/// Assert.assertEquals(resultList.size(), provinceList.size());
/// Assert.assertEquals(flagHolder[0],2);
/// }
/// </code>
/// </pre>
/// @return true : child dml query with tow statement mode,for example : postgre insert child with RETURNING clause.
/// @see Option#FIRST_DML_STATES
/// @see Option#SECOND_DML_QUERY_STATES
    @Nullable
    @Override
    <T> T valueOf(Option<T> option);


}
