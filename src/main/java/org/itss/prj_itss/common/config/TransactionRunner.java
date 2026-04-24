package org.itss.prj_itss.common.config;

import java.sql.SQLException;

public interface TransactionRunner {
    void execute(TransactionCallback callback) throws SQLException;

    @FunctionalInterface
    interface TransactionCallback {
        void execute() throws SQLException;
    }
}
