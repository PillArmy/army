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

package io.army.modelgen;

import javax.annotation.processing.ProcessingEnvironment;


import java.util.function.Consumer;
import java.util.function.IntSupplier;

final class Context {

    final ProcessingEnvironment env;

    final Consumer<String> errorMsgConsumer;

    final IntSupplier errorCountSupplier;

    final StringBuilder tempBuilder;


    Context(ProcessingEnvironment env, StringBuilder tempBuilder, Consumer<String> errorMsgConsumer, IntSupplier errorCountSupplier) {
        this.env = env;
        this.tempBuilder = tempBuilder;
        this.errorMsgConsumer = errorMsgConsumer;
        this.errorCountSupplier = errorCountSupplier;
    }

//    StringBuilder getAndClearTempBuilder() {
//        final StringBuilder builder = this.tempBuilder;
//        builder.setLength(0);
//        return builder;
//    }


}
