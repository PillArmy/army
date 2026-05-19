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
import java.util.function.Function;
import java.util.function.ToLongFunction;

public interface TransactionTemplate extends TransactionOperations {

    <T extends @Nullable Object> T execute(boolean readOnly, TransactionCallback<T> action)
            throws TransactionException;

    <T extends @Nullable Object> T execute(Isolation isolation, boolean readOnly, TransactionCallback<T> action)
            throws TransactionException;


    <T extends @Nullable Object> T execute(TransactionDefinition definition, TransactionCallback<T> action)
            throws TransactionException;

    <T> T executeNoNull(boolean readOnly, Function<TransactionStatus, T> action)
            throws TransactionException;

    <T> T executeNoNull(Isolation isolation, boolean readOnly, Function<TransactionStatus, T> action)
            throws TransactionException;

    <T> T executeNoNull(TransactionDefinition definition, Function<TransactionStatus, T> action)
            throws TransactionException;


    long executeLong(boolean readOnly, ToLongFunction<TransactionStatus> action)
            throws TransactionException;

    long executeLong(Isolation isolation, boolean readOnly, ToLongFunction<TransactionStatus> action)
            throws TransactionException;

    long executeLong(TransactionDefinition definition, ToLongFunction<TransactionStatus> action)
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
