package org.itss.prj_itss.common.config;

import java.sql.SQLException;

public interface ITransactionRunner {
    void execute(ITransactionCallback callback) throws SQLException;

    @FunctionalInterface
    interface ITransactionCallback {
        void execute() throws SQLException;
    }
}
