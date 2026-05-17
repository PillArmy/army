package io.army.spring;

import org.jspecify.annotations.Nullable;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionOperations;

import java.util.function.Consumer;

public interface ArmyTransactionTemplate extends TransactionOperations {


    <T extends @Nullable Object> T execute(Isolation isolation, boolean readOnly, TransactionCallback<T> action)
            throws TransactionException;


    <T extends @Nullable Object> T execute(TransactionDefinition definition, TransactionCallback<T> action)
            throws TransactionException;


    void executeWithoutResult(Isolation isolation, boolean readOnly, Consumer<TransactionStatus> action)
            throws TransactionException;


    void executeWithoutResult(TransactionDefinition definition, Consumer<TransactionStatus> action)
            throws TransactionException;


    TransactionDefinition of(Propagation propagation, Isolation isolation, boolean readOnly);

    TransactionDefinition of(Propagation propagation, Isolation isolation, boolean readOnly, int timeout);

}
