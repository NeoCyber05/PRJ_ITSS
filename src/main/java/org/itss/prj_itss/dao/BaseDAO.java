package org.itss.prj_itss.dao;

import org.itss.prj_itss.db.DatabaseConnection;

import java.sql.Connection;
import java.sql.SQLException;


public abstract class BaseDAO {

    protected Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }
}
