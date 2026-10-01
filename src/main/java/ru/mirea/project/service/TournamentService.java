package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.TimeControl;
import ru.mirea.project.model.Tournament;
import ru.mirea.project.model.TournamentStatus;
import ru.mirea.project.repository.TournamentRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TournamentService {
    private final TournamentRepository tournamentRepository;

    public TournamentService(
        TournamentRepository tournamentRepository
    ) {
        this.tournamentRepository = tournamentRepository;
    }

    public List<Tournament> findAll() throws SQLException {
        return tournamentRepository.findAll();
    }

    public Tournament findById(long id) throws SQLException {
        Tournament tournament = tournamentRepository.findById(id);

        if (tournament == null) {
            throw new EntityNotFoundException("Турнир", id);
        }

        return tournament;
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
        validate(
            title,
            timeControl,
            startDate,
            endDate,
            maxParticipants,
            minRating,
            maxRating
        );

        return tournamentRepository.create(
            title,
            timeControl,
            startDate,
            endDate,
            maxParticipants,
            minRating,
            maxRating
        );
    }

    public void update(
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
        findById(id);
        validate(
            title,
            timeControl,
            startDate,
            endDate,
            maxParticipants,
            minRating,
            maxRating
        );

        if (status == null) {
            throw new BusinessException("Статус турнира обязателен");
        }

        boolean updated = tournamentRepository.update(
            id,
            title,
            timeControl,
            startDate,
            endDate,
            maxParticipants,
            minRating,
            maxRating,
            status
        );

        if (!updated) {
            throw new EntityNotFoundException("Турнир", id);
        }
    }

    public void delete(long id) throws SQLException {
        findById(id);

        if (!tournamentRepository.deleteById(id)) {
            throw new EntityNotFoundException("Турнир", id);
        }
    }

    private void validate(
        String title,
        TimeControl timeControl,
        LocalDate startDate,
        LocalDate endDate,
        int maxParticipants,
        Integer minRating,
        Integer maxRating
    ) {
        if (title == null || title.isBlank()) {
            throw new BusinessException("Название турнира обязательно");
        }
        if (timeControl == null || startDate == null || endDate == null) {
            throw new BusinessException("Контроль времени и даты обязательны");
        }
        if (endDate.isBefore(startDate)) {
            throw new BusinessException("Дата окончания раньше даты начала");
        }
        if (maxParticipants < 2 || maxParticipants > 500) {
            throw new BusinessException(
                "Количество участников должно быть от 2 до 500"
            );
        }

        validateRating(minRating);
        validateRating(maxRating);

        if (minRating != null
            && maxRating != null
            && minRating > maxRating) {
            throw new BusinessException(
                "Минимальный рейтинг выше максимального"
            );
        }
    }

    private void validateRating(Integer rating) {
        if (rating != null && (rating < 0 || rating > 3500)) {
            throw new BusinessException("Рейтинг должен быть от 0 до 3500");
        }
    }
}
