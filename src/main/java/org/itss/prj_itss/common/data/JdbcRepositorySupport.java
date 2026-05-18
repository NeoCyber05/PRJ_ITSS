package org.itss.prj_itss.common.data;

import org.itss.prj_itss.common.config.IConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class JdbcRepositorySupport {

    private final IConnectionProvider connectionProvider;

    protected JdbcRepositorySupport(IConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    protected Connection getConnection() throws SQLException {
        return connectionProvider.getConnection();
    }
}
