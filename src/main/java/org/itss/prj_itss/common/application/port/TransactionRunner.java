package org.itss.prj_itss.common.application.port;

public interface TransactionRunner {

    void execute(TransactionCallback callback) throws TransactionException;

    @FunctionalInterface
    interface TransactionCallback {
        void execute() throws TransactionException;
    }
}
