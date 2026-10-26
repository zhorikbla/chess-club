package ru.mirea.project.util;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.project.model.Registration;
import ru.mirea.project.model.Tournament;
import ru.mirea.project.model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter{
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    public Path export(
        Path outputDirectory,
        List<User> users,
        List<Tournament> tournaments,
        List<Registration> registrations
    ) throws IOException {
        Files.createDirectories(outputDirectory);

        String fileName = "chess_club_"+LocalDateTime.now().format(FILE_FORMAT)+".xlsx";

        Path outputFile = outputDirectory.resolve(fileName);

        try(Workbook workbook = new XSSFWorkbook()){
            createUsersSheet(workbook, users);
            createTournamentsSheet(workbook, tournaments);
            createRegistrationsSheet(workbook, registrations);

            try(OutputStream outputStream = Files.newOutputStream(outputFile)){
                workbook.write(outputStream);
            }
        }
        return outputFile.toAbsolutePath();
    }
    public Path exportRegistrations(
        Path outputFile,
        List<Registration> registrations
    ) throws IOException {
        try(Workbook workbook = new XSSFWorkbook()){
            createRegistrationsSheet(
                workbook,
                registrations
            );
            try(
                OutputStream outputStream = Files.newOutputStream(outputFile);
            ) {
                workbook.write(outputStream);
            }
        }
        return outputFile.toAbsolutePath();
    }

    private void createUsersSheet(
        Workbook workbook,
        List<User> users
    ) {
        Sheet sheet = workbook.createSheet("Пользователи");

        writeHeader(
            sheet,
            "ID",
            "ФИО",
            "Email",
            "Роль",
            "Рейтинг"
        );
        int rowIndex = 1;

        for (User user : users){
            Row row = sheet.createRow(rowIndex++);
            set(row, 0, user.getId());
            set(row, 1, user.getFullName());
            set(row, 2, user.getEmail());
            set(row, 3, user.getRole().name());
            set(row, 4, user.getRating());

        }
        autoSize(sheet, 5);
    }
    private void createTournamentsSheet(
        Workbook workbook,
        List<Tournament> tournaments
    ) {
        Sheet sheet = workbook.createSheet("Турниры");
        writeHeader(
            sheet,
            "ID",
            "Название",
            "Контроль времени",
            "Начало",
            "Окончание",
            "Количество мест",
            "Минимальный рейтинг",
            "Максимальный рейтинг",
            "Статус"
        );
        int rowIndex=1;
        for (Tournament tournament : tournaments){
            Row row = sheet.createRow(rowIndex++);
            set(row, 0, tournament.getId());
            set(row, 1, tournament.getTitle());
            set(row, 2, tournament.getTimeControl().name());
            set(row, 3, tournament.getStartDate().toString());
            set(row, 4, tournament.getEndDate().toString());
            set(row, 5, tournament.getMaxParticipants());
            setNullableNumber(
                row,
                6,
                tournament.getMinRating()
            );
            setNullableNumber(
                row,
                7,
                tournament.getMaxRating()
            );
            set(row, 8, tournament.getStatus().name());

        }
        autoSize(sheet, 9);
    }
    private void createRegistrationsSheet(
        Workbook workbook,
        List<Registration> registrations
    ) {
        Sheet sheet = workbook.createSheet("Заявки");
        writeHeader(
            sheet,
            "ID",
            "Игрок",
            "Email",
            "Рейтинг",
            "Турнир",
            "Статус",
            "Дата подачи"
        );
        int rowIndex = 1;
        for (Registration registration : registrations){
            Row row = sheet.createRow(rowIndex++);
            set(row, 0, registration.getId());
            set(
                row,
                1,
                registration.getUser().getFullName()
            );
            set(
                row,
                2,
                registration.getUser().getEmail()
            );
            set(
                row,
                3,
                registration.getUser().getRating()
            );
            set(
                row,
                4,
                registration.getTournament().getTitle()
            );
            set(
                row,
                5,
                registration.getStatus().name()
            );
            set(
                row,
                6,
                registration.getRegisteredAt().format(DATE_FORMAT)
            );
        }
        autoSize(sheet,7);
    }
    private void writeHeader(
        Sheet sheet,
        String... values
    ) {
        Row header = sheet.createRow(0);
        for (int index = 0; index<values.length; index++){
            set(header, index, values[index]);
        }
        sheet.createFreezePane(0,1);
    }
    private void set(
        Row row,
        int column,
        String value
    ) {
        row.createCell(column).setCellValue(value);
    }
    private void set(
        Row row,
        int column,
        long value
    ){
        row.createCell(column).setCellValue(value);
    }
    private void setNullableNumber(
        Row row,
        int column,
        Integer value
    ) {
        if (value == null){
            set(row, column, "Без ограничения");
        } else {
            set(row, column, value.longValue());
        }
    }
    private void autoSize(
        Sheet sheet,
        int columnCount
    ) {
        for (int column = 0; column< columnCount; column++){
            sheet.autoSizeColumn(column);
        }
    }
}
