package ru.mirea.project.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Registration {
    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final long id;
    private final User user;
    private final Tournament tournament;
    private RegistrationStatus status;
    private final LocalDateTime registeredAt;
    public Registration(
        long id,
        User user,
        Tournament tournament,
        RegistrationStatus status,
        LocalDateTime registeredAt
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException("ID должен быть положительным");
        }
        if (user == null) {
            throw new IllegalArgumentException("Пользователь обязателен");
        }
        if (user.getRole() != UserRole.PLAYER) {
            throw new IllegalArgumentException("Подавать заявку может только игрок");
        }
        if (tournament == null) {
            throw new IllegalArgumentException("Турнир обязателен");
        }
        if (status == null) {
            throw new IllegalArgumentException("Статус заявки обязателен");
        }
        if (registeredAt == null) {
            throw new IllegalArgumentException("Дата подачи заявки обязательна");
        }

        this.id = id;
        this.user = user;
        this.tournament = tournament;
        this.status = status;
        this.registeredAt = registeredAt;
    }

    public long getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public Tournament getTournament() {
        return tournament;
    }
    public RegistrationStatus getStatus() {
        return status;
    }
    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
    public void changeStatus(RegistrationStatus newStatus) {
        if (newStatus == null) {
            throw new IllegalArgumentException("Новый статус обязателен");
        }
        if (status == RegistrationStatus.CANCELLED
            || status == RegistrationStatus.REJECTED) {
            throw new IllegalStateException("Нельзя изменить завершённую заявку");
        }
        status = newStatus;
    }

    @Override
    public String toString() {
        return "Заявка #" + id
            + ": " + user.getFullName()
            + " -> " + tournament.getTitle()
            + ", статус: " + status
            + ", подана: " + registeredAt.format(DATE_FORMAT);
    }
}
