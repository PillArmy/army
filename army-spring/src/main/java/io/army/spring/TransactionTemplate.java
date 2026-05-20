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

package io.army.spring;

import org.jspecify.annotations.Nullable;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import java.util.function.Consumer;

public interface TransactionTemplate extends TransactionOperations {

    <T extends @Nullable Object> T execute(boolean readOnly, TransactionCallback<T> action)
            throws TransactionException;

    <T extends @Nullable Object> T execute(Isolation isolation, boolean readOnly, TransactionCallback<T> action)
            throws TransactionException;


    <T extends @Nullable Object> T execute(TransactionDefinition definition, TransactionCallback<T> action)
            throws TransactionException;

    <T> T executeNoNull(boolean readOnly, TransactionCallback<T> action)
            throws TransactionException;

    <T> T executeNoNull(Isolation isolation, boolean readOnly, TransactionCallback<T> action)
            throws TransactionException;

    <T> T executeNoNull(TransactionDefinition definition, TransactionCallback<T> action)
            throws TransactionException;


    void executeWithoutResult(boolean readOnly, Consumer<TransactionStatus> action)
            throws TransactionException;

    void executeWithoutResult(Isolation isolation, boolean readOnly, Consumer<TransactionStatus> action)
            throws TransactionException;


    void executeWithoutResult(TransactionDefinition definition, Consumer<TransactionStatus> action)
            throws TransactionException;


    // below static method
    //
    //


    static TransactionDefinition of(Isolation isolation, boolean readOnly) {
        final DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setIsolationLevel(isolation.value());
        definition.setReadOnly(readOnly);
        return definition;
    }

    static TransactionDefinition of(Propagation propagation, Isolation isolation, boolean readOnly) {
        return of(propagation, isolation, readOnly, TransactionDefinition.TIMEOUT_DEFAULT);
    }

    static TransactionDefinition of(Propagation propagation, Isolation isolation, boolean readOnly, int timeout) {
        final DefaultTransactionDefinition definition = new DefaultTransactionDefinition();

        definition.setPropagationBehavior(propagation.value());
        definition.setIsolationLevel(isolation.value());
        definition.setReadOnly(readOnly);
        definition.setTimeout(timeout);

        return definition;
    }


}
