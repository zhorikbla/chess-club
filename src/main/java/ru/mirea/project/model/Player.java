package ru.mirea.project.model;

public class Player {
    private final long id;
    private final String fullName;
    private final String email;
    private final int rating;

    public Player(long id, String fullName, String email, int rating){
        if (id<=0){
            throw new IllegalArgumentException("ID должен быть положительным");
        }
        if (fullName==null || fullName.isBlank()){
            throw new IllegalArgumentException("ФИО не может быть пустым");
        }
        if (email==null || email.isBlank()){
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        if (rating < 0 || rating >3500){
            throw new IllegalArgumentException("Рейтинг может быть от 0 до 3500");
        }
        this.id=id;
        this.fullName=fullName;
        this.email=email;
        this.rating=rating;
    }
    public long getId(){
        return id;
    }
    public String getFullName(){
        return fullName;
    }
    public String getEmail(){
        return email;
    }
    public int getRating(){
        return rating;
    }

    @Override
    public String toString(){
        return "#"+id+" "+fullName+", rating: "+rating;
    }
}