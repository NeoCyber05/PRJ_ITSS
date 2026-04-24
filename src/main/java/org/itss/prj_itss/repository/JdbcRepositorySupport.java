package org.itss.prj_itss.repository;

import org.itss.prj_itss.common.config.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

abstract class JdbcRepositorySupport {

    private final ConnectionProvider connectionProvider;

    JdbcRepositorySupport(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    protected Connection getConnection() throws SQLException {
        return connectionProvider.getConnection();
    }
}
