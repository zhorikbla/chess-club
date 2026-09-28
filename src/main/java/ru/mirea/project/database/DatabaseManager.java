package ru.mirea.project.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private final String url;
    private final String user;
    private final String password;

    public DatabaseManager(
        String url,
        String user,
        String password
    ) {
        this.url = url;
        this.user = user;
        this.password = password;
    }
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
            url,
            user,
            password
        );
    }
}
