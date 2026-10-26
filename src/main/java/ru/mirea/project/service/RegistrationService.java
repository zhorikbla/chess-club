package ru.mirea.project.service;

import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.exception.EntityNotFoundException;
import ru.mirea.project.model.Registration;
import ru.mirea.project.model.RegistrationStatus;
import ru.mirea.project.model.Tournament;
import ru.mirea.project.model.TournamentStatus;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;
import ru.mirea.project.repository.RegistrationRepository;
import ru.mirea.project.repository.TournamentRepository;
import ru.mirea.project.repository.UserRepository;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final UserRepository userRepository;
    private final TournamentRepository tournamentRepository;

    public RegistrationService(
        RegistrationRepository registrationRepository,
        UserRepository userRepository,
        TournamentRepository tournamentRepository
    ) {
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.tournamentRepository = tournamentRepository;
    }

    public List<Registration> findAll() throws SQLException {
        return registrationRepository.findAll();
    }

    public Registration findById(long id) throws SQLException {
        Registration registration = registrationRepository.findById(id);

        if (registration == null) {
            throw new EntityNotFoundException("Заявка", id);
        }

        return registration;
    }

    public List<Registration> findByUserId(long userId)
        throws SQLException {
        return registrationRepository.findByUserId(userId);
    }

    public long countActiveByTournamentId(long tournamentId)
        throws SQLException {
        return registrationRepository.countActiveByTournamentId(
            tournamentId
        );
    }

    public long create(long userId, long tournamentId)
        throws SQLException {
        User user = userRepository.findById(userId);
        if (user == null) {
            throw new EntityNotFoundException("Пользователь", userId);
        }

        Tournament tournament = tournamentRepository.findById(tournamentId);
        if (tournament == null) {
            throw new EntityNotFoundException("Турнир", tournamentId);
        }

        if (user.getRole() != UserRole.PLAYER) {
            throw new BusinessException("Только игрок может подать заявку");
        }
        if (tournament.getStatus() != TournamentStatus.REGISTRATION_OPEN) {
            throw new BusinessException("Регистрация на турнир закрыта");
        }
        if (!tournament.getStartDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Турнир уже начался");
        }
        if (!acceptsRating(tournament, user.getRating())) {
            throw new BusinessException(
                "Рейтинг игрока не подходит для турнира"
            );
        }
        if (registrationRepository.hasActiveRegistration(
            userId,
            tournamentId
        )) {
            throw new BusinessException(
                "Активная заявка на этот турнир уже существует"
            );
        }
        if (registrationRepository.countActiveByTournamentId(tournamentId)
            >= tournament.getMaxParticipants()) {
            throw new BusinessException("В турнире закончились места");
        }

        return registrationRepository.create(userId, tournamentId);
    }

    public void changeStatus(
        long id,
        RegistrationStatus newStatus
    ) throws SQLException {
        Registration registration = findById(id);

        if (!isAllowedTransition(registration.getStatus(), newStatus)) {
            throw new BusinessException(
                "Запрещён переход "
                    + registration.getStatus()
                    + " -> "
                    + newStatus
            );
        }

        if (!registrationRepository.updateStatus(id, newStatus)) {
            throw new EntityNotFoundException("Заявка", id);
        }
    }

    public List<RegistrationStatus> allowedStatusesFor(
        RegistrationStatus currentStatus
    ) {
        if (currentStatus == null) {
            throw new BusinessException("Текущий статус обязателен");
        }

        return switch (currentStatus) {
            case PENDING -> List.of(
                RegistrationStatus.CONFIRMED,
                RegistrationStatus.REJECTED,
                RegistrationStatus.CANCELLED
            );
            case CONFIRMED -> List.of(
                RegistrationStatus.CANCELLED
            );
            case REJECTED, CANCELLED -> List.of();
        };
    }

    public void delete(long id) throws SQLException {
        Registration registration = findById(id);

        if (!canDelete(registration.getStatus())) {
            throw new BusinessException(
                "Удалить можно только активную заявку"
            );
        }

        if (!registrationRepository.deleteById(id)) {
            throw new BusinessException(
                "Не удалось удалить заявку"
            );
        }
    }

    public boolean canDelete(RegistrationStatus status) {
        return status == RegistrationStatus.PENDING
            || status == RegistrationStatus.CONFIRMED;
    }

    public void cancelByUser(long id, long userId)
        throws SQLException {
        Registration registration = findById(id);

        if (registration.getUser().getId() != userId) {
            throw new BusinessException("Нельзя отменить чужую заявку");
        }
        if (!registrationRepository.cancelByUser(id, userId)) {
            throw new BusinessException("Заявка уже завершена");
        }
    }

    public List<Registration> searchByPlayerName(String query)
        throws SQLException {
        requireQuery(query);
        String normalized = query.trim().toLowerCase(Locale.ROOT);

        return findAll().stream()
            .filter(registration -> registration.getUser()
                .getFullName()
                .toLowerCase(Locale.ROOT)
                .contains(normalized))
            .toList();
    }

    public List<Registration> searchByTournamentTitle(String query)
        throws SQLException {
        requireQuery(query);
        String normalized = query.trim().toLowerCase(Locale.ROOT);

        return findAll().stream()
            .filter(registration -> registration.getTournament()
                .getTitle()
                .toLowerCase(Locale.ROOT)
                .contains(normalized))
            .toList();
    }

    public List<Registration> filterByStatus(RegistrationStatus status)
        throws SQLException {
        if (status == null) {
            throw new BusinessException("Статус обязателен");
        }

        return findAll().stream()
            .filter(registration -> registration.getStatus() == status)
            .toList();
    }

    public List<Registration> filterByDateRange(
        LocalDate from,
        LocalDate to
    ) throws SQLException {
        if (from == null || to == null || to.isBefore(from)) {
            throw new BusinessException("Некорректный диапазон дат");
        }

        return findAll().stream()
            .filter(registration -> {
                LocalDate date = registration.getRegisteredAt().toLocalDate();
                return !date.isBefore(from) && !date.isAfter(to);
            })
            .toList();
    }

    public List<Registration> sortByRegisteredAt()
        throws SQLException {
        return findAll().stream()
            .sorted(Comparator.comparing(Registration::getRegisteredAt))
            .toList();
    }

    public List<Registration> sortByPlayerRatingDescending()
        throws SQLException {
        return findAll().stream()
            .sorted(Comparator.comparingInt(
                (Registration registration) ->
                    registration.getUser().getRating()
            ).reversed())
            .toList();
    }

    private boolean acceptsRating(Tournament tournament, int rating) {
        Integer minRating = tournament.getMinRating();
        Integer maxRating = tournament.getMaxRating();

        return (minRating == null || rating >= minRating)
            && (maxRating == null || rating <= maxRating);
    }

    private void requireQuery(String query) {
        if (query == null || query.isBlank()) {
            throw new BusinessException(
                "Поисковая строка не может быть пустой"
            );
        }
    }

    private boolean isAllowedTransition(
        RegistrationStatus current,
        RegistrationStatus target
    ) {
        return target != null
            && allowedStatusesFor(current).contains(target);
    }
}
