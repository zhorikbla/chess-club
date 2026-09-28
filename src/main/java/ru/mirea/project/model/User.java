package ru.mirea.project.model;

public class User {
    private final long id;
    private final String fullName;
    private final String email;
    private final UserRole role;
    private final int rating;

    public User(
        long id,
        String fullName,
        String email,
        UserRole role,
        int rating
    ) {
        if (id <= 0) {
            throw new IllegalArgumentException(
                "ID должен быть положительным"
            );
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException(
                "ФИО не может быть пустым"
            );
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                "Email не может быть пустым"
            );
        }
        if (role == null) {
            throw new IllegalArgumentException(
                "Роль обязательна"
            );
        }
        if (rating < 0 || rating > 3500) {
            throw new IllegalArgumentException(
                "Рейтинг должен быть от 0 до 3500"
            );
        }
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.rating = rating;
    }
    public long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public int getRating() {
        return rating;
    }

    @Override
    public String toString() {
        return "#" + id
            + " " + fullName
            + " <" + email + ">"
            + ", роль: " + role
            + ", рейтинг: " + rating;
    }
}
