package org.itss.prj_itss.model.shared.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection {

    private static final String PROPERTIES_FILE = "/db.properties";

    private static DatabaseConnection instance;
    private Connection connection;

    private final String url;
    private final String user;
    private final String password;

    private DatabaseConnection() {
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream(PROPERTIES_FILE)) {
            if (input == null) {
                throw new RuntimeException(
                        "Không tìm thấy file " + PROPERTIES_FILE + "!\n"
                        + "Hãy copy db.properties.example thành db.properties và điền thông tin kết nối."
                );
            }
            props.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi đọc file " + PROPERTIES_FILE, e);
        }

        String host = props.getProperty("db.host");
        String port = props.getProperty("db.port", "6543");
        String dbName = props.getProperty("db.name", "postgres");
        this.user = props.getProperty("db.user");
        this.password = props.getProperty("db.password");

        this.url = "jdbc:postgresql://" + host + ":" + port + "/" + dbName
                + "?prepareThreshold=0";
    }


    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }


    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Kết nối Supabase thành công!");
            } catch (ClassNotFoundException e) {
                throw new SQLException("PostgreSQL JDBC Driver không tìm thấy.", e);
            } catch (SQLException e) {
                System.err.println("Lỗi kết nối Supabase: " + e.getMessage());
                throw e;
            }
        }
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Đã đóng kết nối Supabase.");
            } catch (SQLException e) {
                System.err.println("Lỗi khi đóng kết nối: " + e.getMessage());
            }
        }
    }


    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
