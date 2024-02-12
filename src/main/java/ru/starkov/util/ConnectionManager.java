package ru.starkov.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionManager {

    private static final String URL = "db.url";
    private static final String USER = "db.username";
    private static final String PASSWORD = "db.password";

    private ConnectionManager() {
    }

    public static Connection getConnection() {
        var url = PropertiesLoader.get(URL);
        var user = PropertiesLoader.get(USER);
        var password = PropertiesLoader.get(PASSWORD);
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            return connection;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
