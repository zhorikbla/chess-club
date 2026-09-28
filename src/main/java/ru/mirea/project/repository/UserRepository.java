package ru.mirea.project.repository;

import ru.mirea.project.database.DatabaseManager;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private final DatabaseManager databaseManager;

    public UserRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public User findByEmail(String email) throws SQLException {
        String sql = """
        SELECT id, full_name, email, role, rating
        FROM users
        WHERE lower(email) = lower(?)
        """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }
                return mapUser(result);
            }
        }
    }
    public List<User> findAll() throws SQLException {
        String sql = """
        SELECT id, full_name, email, role, rating
        FROM users
        ORDER BY id
        """;
        List<User> users = new ArrayList<>();
        try (Connection connection = databaseManager.getConnection();   
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                users.add(mapUser(result));
            }
        }
        return users;
    }

    public User create(
        String fullName,
        String email,
        UserRole role,
        int rating
    ) throws SQLException {
        String sql = """
            INSERT INTO users (full_name, email, role, rating)
            VALUES (?, lower(?), ?, ?)
            RETURNING id, full_name, email, role, rating
            """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fullName.trim());
            statement.setString(2, email.trim());
            statement.setString(3, role.name());
            statement.setInt(4, rating);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                        "База не вернула созданного пользователя"
                    );
                }
                return mapUser(result);
            }
        }
    }

    public boolean update(
        long id,
        String fullName,
        String email,
        UserRole role,
        int rating
    ) throws SQLException {
        String sql = """
            UPDATE users
            SET full_name = ?,
                email = lower(?),
                role = ?,
                rating = ?
            WHERE id = ?
            """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, fullName.trim());
            statement.setString(2, email.trim());
            statement.setString(3, role.name());
            statement.setInt(4, rating);
            statement.setLong(5, id);

            int changedRows = statement.executeUpdate();
            return changedRows == 1;
        }
    }

    public boolean deleteById(long id) throws SQLException {
        String sql = """
            DELETE FROM users
            WHERE id = ?
            """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            int deletedRows = statement.executeUpdate();
            return deletedRows == 1;
        }
    }

    private User mapUser(ResultSet result) throws SQLException {
        return new User(
            result.getLong("id"),
            result.getString("full_name"),
            result.getString("email"),
            UserRole.valueOf(result.getString("role")),
            result.getInt("rating")
        );
    }
}
