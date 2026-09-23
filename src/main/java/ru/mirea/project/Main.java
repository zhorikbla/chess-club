package ru.mirea.project;

import ru.mirea.project.model.Player;
import ru.mirea.project.model.Registration;
import ru.mirea.project.model.RegistrationStatus;
import ru.mirea.project.model.Tournament;

import java.time.LocalDate;

public class Main {
    public static void main (String[] args){
        Player player = new Player(
        1L,
        "Иван Петров",
        "ivan@example.com",
        1450
        );
        Tournament tournament = new Tournament(
            1L,
            "Осенний кап",
            LocalDate.now().plusDays(7),
            16
        );
        Registration registration = new Registration (1L, player, tournament);

        System.out.println(player);
        System.out.println(tournament);
        System.out.println(registration);

        registration.changeStatus(RegistrationStatus.CONFIRMED);

        System.out.println("После подтверждения: ");
        System.out.println(registration);

    }


}
