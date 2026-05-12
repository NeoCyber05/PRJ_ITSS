package org.itss.prj_itss.db;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class WarehouseDatabaseConnection {

    private static final String PROPERTIES_FILE = "/warehouse-db.properties";

    private static WarehouseDatabaseConnection instance;
    private Connection connection;

    private final String url;
    private final String user;
    private final String password;

    private WarehouseDatabaseConnection() {
        Properties props = loadProperties();

        String host = firstNonBlank(
            System.getenv("WAREHOUSE_DB_HOST"),
            props.getProperty("warehouse.db.host")
        );
        String port = firstNonBlank(
            System.getenv("WAREHOUSE_DB_PORT"),
            props.getProperty("warehouse.db.port"),
            "5432"
        );
        String dbName = firstNonBlank(
            System.getenv("WAREHOUSE_DB_NAME"),
            props.getProperty("warehouse.db.name"),
            "postgres"
        );
        this.user = firstNonBlank(
            System.getenv("WAREHOUSE_DB_USER"),
            props.getProperty("warehouse.db.user")
        );
        this.password = firstNonBlank(
            System.getenv("WAREHOUSE_DB_PASSWORD"),
            props.getProperty("warehouse.db.password")
        );

        if (isBlank(host) || isBlank(port) || isBlank(dbName) || isBlank(user) || isBlank(password)) {
            throw new IllegalStateException(
                "Warehouse database config is missing. Set WAREHOUSE_DB_HOST, WAREHOUSE_DB_PORT, " +
                "WAREHOUSE_DB_NAME, WAREHOUSE_DB_USER, WAREHOUSE_DB_PASSWORD or provide warehouse-db.properties locally."
            );
        }

        this.url = "jdbc:postgresql://" + host.trim() + ":" + port.trim() + "/" + dbName.trim() + "?sslmode=require";
    }

    public static synchronized WarehouseDatabaseConnection getInstance() {
        if (instance == null) {
            instance = new WarehouseDatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("Warehouse DB connected.");
            } catch (ClassNotFoundException exception) {
                throw new SQLException("PostgreSQL JDBC Driver not found for warehouse DB.", exception);
            } catch (SQLException exception) {
                System.err.println("Warehouse DB connection error: " + exception.getMessage());
                throw exception;
            }
        }
        return connection;
    }

    private Properties loadProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream(PROPERTIES_FILE)) {
            if (input != null) {
                props.load(input);
            }
        } catch (IOException exception) {
            throw new RuntimeException("Cannot read " + PROPERTIES_FILE, exception);
        }
        return props;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
