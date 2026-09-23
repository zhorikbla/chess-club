package ru.mirea.project.model;

import java.time.LocalDate;

public class Tournament{

    private final long id;
    private final String title;
    private final LocalDate startDate;
    private final int maxParticipants;

    public Tournament(
        long id,
        String title,
        LocalDate startDate,
        int maxParticipants
    ) {
        if (id<=0){
            throw new IllegalArgumentException("ID должен быть положительным");
        }
        if (title==null || title.isBlank()){
            throw new IllegalArgumentException("Название не может быть пустым");
        }
        if (startDate==null){
            throw new IllegalArgumentException("Дата обязательна");
        }
        if (maxParticipants<2){
            throw new IllegalArgumentException("В турнире должно быть минимум 2 места");
        }
        this.id = id;
        this.title = title;
        this.startDate = startDate;
        this.maxParticipants = maxParticipants;
    }
    public long getId(){
        return id;
    }
    public String getTitle(){
        return title;
    }
    public LocalDate getStartDate(){
        return startDate;
    }
    public int getMaxParticipants(){
        return maxParticipants;
    }

    @Override
    public String toString(){
        return "#"+id+" "+title
        +", date: "+startDate
        +", maxParticipants: "+maxParticipants;
    }
}
