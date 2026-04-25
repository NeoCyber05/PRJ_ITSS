package org.itss.prj_itss.repository;

import org.itss.prj_itss.common.config.IConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

abstract class RepositorySupport {

    private final IConnectionProvider connectionProvider;

    RepositorySupport(IConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    protected Connection getConnection() throws SQLException {
        return connectionProvider.getConnection();
    }
}
