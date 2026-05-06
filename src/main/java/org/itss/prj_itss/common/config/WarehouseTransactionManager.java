package org.itss.prj_itss.common.config;

import org.itss.prj_itss.db.WarehouseDatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;

public final class WarehouseTransactionManager implements ITransactionRunner {

    private final ThreadLocal<Connection> activeConnection = new ThreadLocal<>();

    Connection currentConnection() {
        return activeConnection.get();
    }

    @Override
    public void execute(ITransactionCallback callback) throws SQLException {
        Connection existing = activeConnection.get();
        if (existing != null) {
            callback.execute();
            return;
        }

        Connection connection = WarehouseDatabaseConnection.getInstance().getConnection();
        boolean originalAutoCommit = connection.getAutoCommit();
        activeConnection.set(connection);

        try {
            connection.setAutoCommit(false);
            callback.execute();
            connection.commit();
        } catch (SQLException | RuntimeException exception) {
            connection.rollback();
            throw exception;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
            activeConnection.remove();
        }
    }
}
