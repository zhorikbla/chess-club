package ru.mirea.project.model;

import java.time.LocalDateTime;

public class Registration{

    private final long id;
    private final Player player;
    private final Tournament tournament;
    private RegistrationStatus status;
    private final LocalDateTime createdAt;

    public Registration(long id, Player player, Tournament tournament){
        if (id<=0){
            throw new IllegalArgumentException("ID должен быть положительным");
        }
        if (player==null){
            throw new IllegalArgumentException("Игрок обязателен");
        }
        if (tournament==null){
            throw new IllegalArgumentException("Турнир обязателен");
        }

        this.id = id;
        this.player = player;
        this.tournament = tournament;
        this.status = RegistrationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public long getId(){
        return id;
    }
    public Player getPlayer(){
        return player;
    }
    public Tournament getTournament(){
        return tournament;
    }
    public RegistrationStatus getStatus(){
        return status;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public void changeStatus(RegistrationStatus newStatus){
        if (newStatus == null){
            throw new IllegalArgumentException("Статус обязателен");
        }
        this.status = newStatus;
    }

    @Override
    public String toString(){
        return "Заявка #" + id
        +": " + player.getFullName()
        +" -> " + tournament.getTitle()
        +", статус: "+status;
    }
}
