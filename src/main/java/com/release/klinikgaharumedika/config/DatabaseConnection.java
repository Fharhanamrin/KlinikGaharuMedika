package com.release.klinikgaharumedika.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class DatabaseConnection {
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    private static final String DEFAULT_HOST = "localhost";
    private static final String DEFAULT_PORT = "3306";
    private static final String DEFAULT_DB_NAME = "db_gaharu_medika";
    private static final String DEFAULT_USERNAME = "root";
    private static final String DEFAULT_PASSWORD = "";

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                    "MySQL JDBC driver belum tersedia. Pastikan dependency mysql-connector-j terpasang.",
                    e
            );
        }

        return DriverManager.getConnection(buildJdbcUrl(), getUsername(), getPassword());
    }

    public static boolean testConnection() {
        String connectionTarget = buildConnectionTarget();

        try (Connection connection = getConnection()) {
            if (connection != null && !connection.isClosed()) {
                LOGGER.info(() -> "Database connection success: " + connectionTarget);
                return true;
            }

            LOGGER.severe(() -> "Database connection failed: koneksi tertutup setelah dibuka ke " + connectionTarget);
            return false;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database connection failed: " + connectionTarget + " - " + e.getMessage(), e);
            return false;
        }
    }

    public static void main(String[] args) {
        testConnection();
    }

    public static String getDatabaseName() {
        return readSetting("app.db.name", "APP_DB_NAME", DEFAULT_DB_NAME);
    }

    private static String buildJdbcUrl() {
        return "jdbc:mysql://"
                + getHost()
                + ":"
                + getPort()
                + "/"
                + getDatabaseName()
                + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta";
    }

    private static String buildConnectionTarget() {
        return getHost() + ":" + getPort() + "/" + getDatabaseName();
    }

    private static String getHost() {
        return readSetting("app.db.host", "APP_DB_HOST", DEFAULT_HOST);
    }

    private static String getPort() {
        return readSetting("app.db.port", "APP_DB_PORT", DEFAULT_PORT);
    }

    private static String getUsername() {
        return readSetting("app.db.user", "APP_DB_USER", DEFAULT_USERNAME);
    }

    private static String getPassword() {
        return readSetting("app.db.password", "APP_DB_PASSWORD", DEFAULT_PASSWORD);
    }

    private static String readSetting(String systemPropertyKey, String envKey, String defaultValue) {
        String systemPropertyValue = System.getProperty(systemPropertyKey);
        if (systemPropertyValue != null && !systemPropertyValue.isBlank()) {
            return systemPropertyValue.trim();
        }

        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }

        return defaultValue;
    }
}
