package ru.mirea.project.model;

import java.time.LocalDate;

public class Tournament {
    private final long id;
    private final String title;
    private final TimeControl timeControl;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final int maxParticipants;
    private final Integer minRating;
    private final Integer maxRating;
    private final TournamentStatus status;

    public Tournament(
        long id,
        String title,
        TimeControl timeControl,
        LocalDate startDate,
        LocalDate endDate,
        int maxParticipants,
        Integer minRating,
        Integer maxRating,
        TournamentStatus status
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным");
        }
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Название турнира обязательно");
        }
        if (timeControl == null) {
            throw new IllegalArgumentException(
                "Контроль времени обязателен"
            );
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                "Даты турнира обязательны"
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                "Дата окончания не может быть раньше начала"
            );
        }

        if (maxParticipants < 2 || maxParticipants > 500) {
            throw new IllegalArgumentException(
                "Количество участников должно быть от 2 до 500"
            );
        }

        if (minRating != null
            && (minRating < 0 || minRating > 3500)) {
            throw new IllegalArgumentException(
                "Минимальный рейтинг должен быть от 0 до 3500"
            );
        }

        if (maxRating != null
            && (maxRating < 0 || maxRating > 3500)) {
            throw new IllegalArgumentException(
                "Максимальный рейтинг должен быть от 0 до 3500"
            );
        }
        if (minRating != null
            && maxRating != null
            && minRating > maxRating) {
            throw new IllegalArgumentException(
                "Минимальный рейтинг не может быть больше максимального"
            );
        }

        if (status == null) {
            throw new IllegalArgumentException(
                "Статус турнира обязателен"
            );
        }
        this.id = id;
        this.title = title;
        this.timeControl = timeControl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxParticipants = maxParticipants;
        this.minRating = minRating;
        this.maxRating = maxRating;
        this.status = status;
    }
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public TimeControl getTimeControl() {
        return timeControl;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getMaxParticipants() {
        return maxParticipants;
    }

    public Integer getMinRating() {
        return minRating;
    }

    public Integer getMaxRating() {
        return maxRating;
    }

    public TournamentStatus getStatus() {
        return status;
    }

    @Override
    public String toString() {
        String ratingRange = minRating == null && maxRating == null
            ? "без ограничений"
            : (minRating == null ? "до " + maxRating
                : (maxRating == null ? "от " + minRating
                    : minRating + " — " + maxRating));

        return "#" + id
            + " " + title
            + ", контроль: " + timeControl
            + ", даты: " + startDate + " — " + endDate
            + ", мест: " + maxParticipants
            + ", рейтинг: " + ratingRange
            + ", статус: " + status;
    }
}
