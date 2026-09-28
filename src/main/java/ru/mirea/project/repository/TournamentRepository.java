package ru.mirea.project.repository;

import ru.mirea.project.database.DatabaseManager;
import ru.mirea.project.model.TimeControl;
import ru.mirea.project.model.Tournament;
import ru.mirea.project.model.TournamentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TournamentRepository {
    private final DatabaseManager databaseManager;

    public TournamentRepository(
        DatabaseManager databaseManager
    ) {
        this.databaseManager = databaseManager;
    }
    public List<Tournament> findAll() throws SQLException {
        String sql = """
        SELECT id,
               title,
               time_control,
               start_date,
               end_date,
               max_participants,
               min_rating,
               max_rating,
               status
        FROM tournaments
        ORDER BY start_date
        """;

        List<Tournament> tournaments = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                tournaments.add(mapTournament(result));
            }
        }
        return tournaments;

    }
    public Tournament create(
        String title,
        TimeControl timeControl,
        LocalDate startDate,
        LocalDate endDate,
        int maxParticipants,
        Integer minRating,
        Integer maxRating
    ) throws SQLException {
        String sql = """
        INSERT INTO tournaments (
            title,
            time_control,
            start_date,
            end_date,
            max_participants,
            min_rating,
            max_rating,
            status
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, 'REGISTRATION_OPEN')
        RETURNING id,
                  title,
                  time_control,
                  start_date,
                  end_date,
                  max_participants,
                  min_rating,
                  max_rating, 
                  status
        """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title.trim());
            statement.setString(2, timeControl.name());
            statement.setObject(3, startDate);
            statement.setObject(4, endDate);
            statement.setInt(5, maxParticipants);
            statement.setObject(6, minRating);
            statement.setObject(7, maxRating);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException(
                        "База не вернула созданный турнир"
                    );
                }
                return mapTournament(result);
            }
        }
    }
    public boolean update(
        long id,
        String title,
        TimeControl timeControl,
        LocalDate startDate,
        LocalDate endDate,
        int maxParticipants,
        Integer minRating,
        Integer maxRating,
        TournamentStatus status
    ) throws SQLException {
        String sql = """
        UPDATE tournaments 
        SET title = ?,
            time_control = ?,
            start_date = ?,
            end_date = ?,
            max_participants = ?,
            min_rating = ?,
            max_rating = ?,
            status = ?
        
        WHERE id = ?
        """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, title.trim());
            statement.setString(2, timeControl.name());
            statement.setObject(3, startDate);
            statement.setObject(4, endDate);
            statement.setInt(5, maxParticipants);
            statement.setObject(6, minRating);
            statement.setObject(7, maxRating);
            statement.setString(8, status.name());
            statement.setLong(9, id);

            int changedRows = statement.executeUpdate();
            return changedRows == 1;
        }
    }

    public boolean deleteById(long id) throws SQLException {
        String sql = """
        DELETE FROM tournaments
        WHERE id = ?
        """;
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            int deletedRows = statement.executeUpdate();
            return deletedRows == 1;
        }
    }

    private Tournament mapTournament(
        ResultSet result
    ) throws SQLException {
        return new Tournament(
            result.getLong("id"),
            result.getString("title"),
            TimeControl.valueOf(
                result.getString("time_control")
            ),
            result.getObject("start_date", LocalDate.class),
            result.getObject("end_date", LocalDate.class),
            result.getInt("max_participants"),
            result.getObject("min_rating", Integer.class),
            result.getObject("max_rating", Integer.class),
            TournamentStatus.valueOf(
                result.getString("status")
            )
        );
    }

    public Tournament findById(long id) throws SQLException {
        String sql = """
        SELECT id,
               title,
               time_control,
               start_date,
               end_date,
               max_participants,
               min_rating,
               max_rating,
               status
        FROM tournaments
        WHERE id = ?
        """;

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return null;
                }

                return mapTournament(result);
            }
        }
    }
}
