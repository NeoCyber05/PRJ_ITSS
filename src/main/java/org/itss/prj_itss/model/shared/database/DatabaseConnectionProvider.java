package org.itss.prj_itss.model.shared.database;

import java.sql.Connection;
import java.sql.SQLException;

public final class DatabaseConnectionProvider implements ConnectionProvider {

    private final TransactionManager transactionManager;

    public DatabaseConnectionProvider(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection transactionalConnection = transactionManager.currentConnection();
        if (transactionalConnection != null) {
            return transactionalConnection;
        }
        return DatabaseConnection.getInstance().getConnection();
    }
}
