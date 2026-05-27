package org.itss.prj_itss.model.shared.database;

import java.sql.Connection;
import java.sql.SQLException;

public final class WarehouseTransactionManager implements TransactionRunner {

    private final ThreadLocal<Connection> activeConnection = new ThreadLocal<>();

    Connection currentConnection() {
        return activeConnection.get();
    }

    @Override
    public void execute(TransactionCallback callback) throws TransactionException {
        Connection existing = activeConnection.get();
        if (existing != null) {
            callback.execute();
            return;
        }

        Connection connection = null;
        boolean originalAutoCommit = true;
        try {
            connection = WarehouseDatabaseConnection.getInstance().getConnection();
            originalAutoCommit = connection.getAutoCommit();
            activeConnection.set(connection);
            connection.setAutoCommit(false);
            callback.execute();
            connection.commit();
        } catch (TransactionException exception) {
            rollbackQuietly(connection, exception);
            throw exception;
        } catch (SQLException exception) {
            rollbackQuietly(connection, exception);
            throw new TransactionException("Transaction failed.", exception);
        } catch (RuntimeException exception) {
            rollbackQuietly(connection, exception);
            throw exception;
        } finally {
            restoreAutoCommit(connection, originalAutoCommit);
            activeConnection.remove();
        }
    }

    private void rollbackQuietly(Connection connection, Throwable failure) {
        if (connection == null) {
            return;
        }
        try {
            connection.rollback();
        } catch (SQLException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }

    private void restoreAutoCommit(Connection connection, boolean originalAutoCommit) throws TransactionException {
        if (connection == null) {
            return;
        }
        try {
            connection.setAutoCommit(originalAutoCommit);
        } catch (SQLException exception) {
            throw new TransactionException("Transaction cleanup failed.", exception);
        }
    }
}
