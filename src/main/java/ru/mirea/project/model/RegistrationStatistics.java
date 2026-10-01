package ru.mirea.project.model;

public class RegistrationStatistics{
    private final long totalUsers;
    private final long totalTournaments;
    private final long totalRegistrations;
    private final long pending;
    private final long confirmed;
    private final long rejected;
    private final long cancelled;

    public RegistrationStatistics(
        long totalUsers,
        long totalTournaments,
        long totalRegistrations,
        long pending,
        long confirmed,
        long rejected,
        long cancelled
    ) {
        this.totalUsers = totalUsers;
        this.totalRegistrations = totalRegistrations;
        this.totalTournaments = totalTournaments;
        this.pending = pending;
        this.confirmed = confirmed;
        this.rejected = rejected;
        this.cancelled = cancelled;

    }
    public long getTotalUsers() {
        return totalUsers;
    }

    public long getTotalTournaments() {
        return totalTournaments;
    }

    public long getTotalRegistrations() {
        return totalRegistrations;
    }

    public long getPending() {
        return pending;
    }

    public long getConfirmed() {
        return confirmed;
    }

    public long getRejected() {
        return rejected;
    }

    public long getCancelled() {
        return cancelled;
    }
}
