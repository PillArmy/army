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

package io.army.criteria;

import io.army.criteria.impl.SQLs;
import io.army.meta.ComplexTableMeta;
import io.army.meta.ParentTableMeta;
import io.army.meta.TableMeta;

public interface SelectionConsumer extends Statement._DeferContextSpec {

    SelectionConsumer selection(Selection selection);

    SelectionConsumer selection(Selection selection1, Selection selection2);

    SelectionConsumer selection(TableField field1, TableField field2, TableField field3);

    SelectionConsumer selection(TableField field1, TableField field2, TableField field3, TableField field4);

    SelectionConsumer selection(String tableAlias, SQLs.SymbolDot period, TableMeta<?> table);

    <P> SelectionConsumer selection(String parenAlias, SQLs.SymbolDot period1, ParentTableMeta<P> parent,
                                    String childAlias, SQLs.SymbolDot period2, ComplexTableMeta<P, ?> child);

    SelectionConsumer selection(String derivedAlias, SQLs.SymbolDot period, SQLs.SymbolAsterisk star);


}
