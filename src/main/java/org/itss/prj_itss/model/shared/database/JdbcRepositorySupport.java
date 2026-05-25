package org.itss.prj_itss.model.shared.database;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class JdbcRepositorySupport {

    private final ConnectionProvider connectionProvider;

    protected JdbcRepositorySupport(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    protected Connection getConnection() throws SQLException {
        return connectionProvider.getConnection();
    }
}
