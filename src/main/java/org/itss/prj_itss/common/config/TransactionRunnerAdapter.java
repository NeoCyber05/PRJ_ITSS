package org.itss.prj_itss.common.config;

import org.itss.prj_itss.common.application.port.TransactionException;
import org.itss.prj_itss.common.application.port.TransactionRunner;

import java.sql.SQLException;

public final class TransactionRunnerAdapter implements TransactionRunner {

    private final ITransactionRunner transactionRunner;

    public TransactionRunnerAdapter(ITransactionRunner transactionRunner) {
        this.transactionRunner = transactionRunner;
    }

    @Override
    public void execute(TransactionCallback callback) throws TransactionException {
        try {
            transactionRunner.execute(() -> {
                try {
                    callback.execute();
                } catch (TransactionException exception) {
                    throw new TransactionCallbackFailure(exception);
                }
            });
        } catch (TransactionCallbackFailure exception) {
            throw exception.transactionException();
        } catch (SQLException exception) {
            throw new TransactionException("Transaction failed.", exception);
        }
    }

    private static final class TransactionCallbackFailure extends RuntimeException {
        private final TransactionException transactionException;

        private TransactionCallbackFailure(TransactionException transactionException) {
            super(transactionException);
            this.transactionException = transactionException;
        }

        private TransactionException transactionException() {
            return transactionException;
        }
    }
}
