package ru.mirea.project.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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

    public static DatabaseManager fromProperties() throws IOException {
        Properties properties = new Properties();

        try (InputStream input = DatabaseManager.class
            .getResourceAsStream("/database.properties")) {
            if (input == null) {
                throw new IOException(
                    "Файл database.properties не найден"
                );
            }

            properties.load(input);
        }

        return new DatabaseManager(
            properties.getProperty("db.url"),
            properties.getProperty("db.user"),
            properties.getProperty("db.password")
        );
    }
}
