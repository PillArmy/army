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

package io.army.criteria.impl.inner.postgre;

import io.army.criteria.Selection;
import io.army.criteria.impl.SQLs;
import io.army.criteria.impl.inner._Statement;

import java.util.List;

/// Internal representation of a PostgreSQL SET or SHOW command.
public interface _PostgreCommand extends _Statement {


    /// SET command for modifying runtime parameters.
    interface _SetCommand extends _PostgreCommand {

        /// The parameter-value pair.
        _ParamValue paramValuePair();

    }

    /// A parameter name and its value list for SET commands.
    interface _ParamValue {

        /// The variable scope (SESSION, LOCAL, etc.).
        SQLs.VarScope scope();

        /// The parameter name.
        String name();

        /// The value word (e.g. TO, =).
        Object word();

        /// The list of values.
        List<Object> valueList();

    }


    /// SHOW command for displaying runtime parameters.
    interface _ShowCommand extends _PostgreCommand {

        /// The selection list.
        List<? extends Selection> selectionList();


        /// The parameter to show.
        Object parameter();

    }


}
