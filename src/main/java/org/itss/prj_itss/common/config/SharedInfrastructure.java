package org.itss.prj_itss.common.config;

public final class SharedInfrastructure {

    private final TransactionManager transactionManager = new TransactionManager();
    private final IConnectionProvider connectionProvider = new DatabaseConnectionProvider(transactionManager);

    private final WarehouseTransactionManager warehouseTransactionManager = new WarehouseTransactionManager();
    private final IConnectionProvider warehouseConnectionProvider = new WarehouseConnectionProvider(warehouseTransactionManager);

    public TransactionManager transactionManager() {
        return transactionManager;
    }

    public IConnectionProvider connectionProvider() {
        return connectionProvider;
    }

    public WarehouseTransactionManager warehouseTransactionManager() {
        return warehouseTransactionManager;
    }

    public IConnectionProvider warehouseConnectionProvider() {
        return warehouseConnectionProvider;
    }

    public void warmUpDatabaseConnection() {
        try {
            connectionProvider.getConnection();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to initialize database connection", exception);
        }
    }
}
