package org.itss.prj_itss.common.config;

import org.itss.prj_itss.model.shared.database.WarehouseDatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public final class WarehouseConnectionProvider implements IConnectionProvider {

    private final WarehouseTransactionManager transactionManager;

    public WarehouseConnectionProvider(WarehouseTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection transactionalConnection = transactionManager.currentConnection();
        if (transactionalConnection != null) {
            return transactionalConnection;
        }
        return WarehouseDatabaseConnection.getInstance().getConnection();
    }
}
