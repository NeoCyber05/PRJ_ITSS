package org.itss.prj_itss.model.shared.database;

public final class TransactionException extends Exception {

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}
