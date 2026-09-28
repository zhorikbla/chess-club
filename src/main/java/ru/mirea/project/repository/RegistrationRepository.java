package ru.mirea.project.repository;

import ru.mirea.project.database.DatabaseManager;
import ru.mirea.project.model.Registration;
import ru.mirea.project.model.RegistrationStatus;
import ru.mirea.project.model.TimeControl;
import ru.mirea.project.model.Tournament;
import ru.mirea.project.model.TournamentStatus;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RegistrationRepository {
    private final DatabaseManager databaseManager;

    public RegistrationRepository(
        DatabaseManager databaseManager
    ) {
        this.databaseManager = databaseManager;
    }
    public List<Registration> findAll() throws SQLException {
        String sql = """
            SELECT
                r.id AS registration_id,
                r.status AS registration_status,
                r.registered_at,

                u.id AS user_id,
                u.full_name AS user_full_name,
                u.email AS user_email,
                u.role AS user_role,
                u.rating AS user_rating,

                t.id AS tournament_id,
                t.title AS tournament_title,
                t.time_control,
                t.start_date,
                t.end_date,
                t.max_participants,
                t.min_rating,
                t.max_rating,
                t.status AS tournament_status
            FROM registrations r
            JOIN users u
                ON u.id = r.user_id
            JOIN tournaments t
                ON t.id = r.tournament_id
            ORDER BY r.id
            """;
        List<Registration> registrations = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                registrations.add(mapRegistration(result));
            }
        }
        return registrations;
    }
    public long create(
        long userId,
        long tournamentId
    ) throws SQLException {

        String sql = """
            INSERT INTO registrations (
                user_id,
                tournament_id,
                status
            )
            VALUES (?, ?, 'PENDING')
            RETURNING id
            """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, userId);
            statement.setLong(2, tournamentId);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    throw new SQLException(
                        "База не вернула ID созданной заявки"
                    );
                }

                return result.getLong("id");
            }
        }
    }
    public List<Registration> findByUserId(
        long userId
    ) throws SQLException {

        return findAll()
            .stream()
            .filter(registration ->
                registration.getUser().getId() == userId
            )
            .toList();
    }
    public Registration findById(
        long id
    ) throws SQLException {

        return findAll()
            .stream()
            .filter(registration ->
                registration.getId() == id
            )
            .findFirst()
            .orElse(null);
    }
    public boolean updateStatus(
        long id,
        RegistrationStatus status
    ) throws SQLException {

        String sql = """
            UPDATE registrations
            SET status = ?
            WHERE id = ?
            """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setLong(2, id);

            int changedRows = statement.executeUpdate();

            return changedRows == 1;
        }
    }
    public boolean cancelByUser(
        long registrationId,
        long userId
    ) throws SQLException {

        String sql = """
            UPDATE registrations
            SET status = 'CANCELLED'
            WHERE id = ?
              AND user_id = ?
              AND status IN ('PENDING', 'CONFIRMED')
            """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, registrationId);
            statement.setLong(2, userId);

            int changedRows = statement.executeUpdate();

            return changedRows == 1;
        }
    }
    private Registration mapRegistration(
        ResultSet result
    ) throws SQLException {
        User user = new User(
            result.getLong("user_id"),
            result.getString("user_full_name"),
            result.getString("user_email"),
            UserRole.valueOf(
                result.getString("user_role")
            ),
            result.getInt("user_rating")
        );
        Tournament tournament = new Tournament(
            result.getLong("tournament_id"),
            result.getString("tournament_title"),
            TimeControl.valueOf(
                result.getString("time_control")
            ),
            result.getObject(
                "start_date",
                LocalDate.class
            ),
            result.getObject(
                "end_date",
                LocalDate.class
            ),
            result.getInt("max_participants"),
            result.getObject(
                "min_rating",
                Integer.class
            ),
            result.getObject(
                "max_rating",
                Integer.class
            ),
            TournamentStatus.valueOf(
                result.getString("tournament_status")
            )
        );
        return new Registration(
            result.getLong("registration_id"),
            user,
            tournament,
            RegistrationStatus.valueOf(
                result.getString("registration_status")
            ),
            result.getObject(
                "registered_at",
                LocalDateTime.class
            )
        );
    }
}
