package org.itss.prj_itss.common.config;

import java.sql.Connection;
import java.sql.SQLException;

public interface IConnectionProvider {
    Connection getConnection() throws SQLException;
}
