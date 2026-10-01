package ru.mirea.project.service;

import ru.mirea.project.model.Registration;
import ru.mirea.project.model.RegistrationStatistics;
import ru.mirea.project.model.RegistrationStatus;
import ru.mirea.project.model.Tournament;
import ru.mirea.project.model.User;
import ru.mirea.project.repository.CrudRepository;

import java.sql.SQLException;
import java.util.List;

public class StatisticsService{
    private final CrudRepository<User> userRepository;
    private final CrudRepository<Tournament> tournamentRepository;
    private final CrudRepository<Registration> registrationRepository;

    public StatisticsService(
        CrudRepository<User> userRepository,
        CrudRepository<Tournament> tournamentRepository,
        CrudRepository<Registration> registrationRepository
    ) {
        this.userRepository = userRepository;
        this.tournamentRepository = tournamentRepository;
        this.registrationRepository = registrationRepository;
    }
    public RegistrationStatistics calculate() throws SQLException{
        List<User> users = userRepository.findAll();
        List<Tournament> tournaments = tournamentRepository.findAll();
        List<Registration> registrations = registrationRepository.findAll();

        long pending = registrations.stream().filter(registration -> registration.getStatus()==RegistrationStatus.PENDING).count();
        long confirmed = registrations.stream().filter(registration -> registration.getStatus()==RegistrationStatus.CONFIRMED).count();
        long rejected = registrations.stream().filter(registration -> registration.getStatus()==RegistrationStatus.REJECTED).count();
        long cancelled = registrations.stream().filter(registration -> registration.getStatus()==RegistrationStatus.CANCELLED).count();
        return new RegistrationStatistics(
                users.size(),
                tournaments.size(),
                registrations.size(),
                pending,
                confirmed,
                rejected,
                cancelled
        );

    }

}
