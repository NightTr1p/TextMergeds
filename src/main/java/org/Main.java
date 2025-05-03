package org;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Main extends Application {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private String directoryPath = ""; // Путь к папке с транзакциями

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Ритейл Сервис 24");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(10);
        grid.setHgap(10);

        // Поле для выбора папки с транзакциями
        Label directoryLabel = new Label("Путь к папке с транзакциями:");
        TextField directoryField = new TextField();
        directoryField.setEditable(false);
        Button chooseDirectoryButton = new Button("Выбрать папку");
        grid.add(directoryLabel, 0, 0);
        grid.add(directoryField, 1, 0);
        grid.add(chooseDirectoryButton, 2, 0);

        // Обработчик для кнопки выбора папки
        chooseDirectoryButton.setOnAction(event -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Выберите папку с транзакциями");
            File selectedDirectory = directoryChooser.showDialog(primaryStage);
            if (selectedDirectory != null) {
                directoryPath = selectedDirectory.getAbsolutePath();
                directoryField.setText(directoryPath);
            }
        });

        // Поле для начальной даты
        Label startDateLabel = new Label("Начальная дата:");
        TextField startDateField = new TextField();
        grid.add(startDateLabel, 0, 1);
        grid.add(startDateField, 1, 1);

        // Поле для конечной даты
        Label endDateLabel = new Label("Конечная дата:");
        TextField endDateField = new TextField();
        grid.add(endDateLabel, 0, 2);
        grid.add(endDateField, 1, 2);

        // Кнопка для запуска процесса
        Button mergeButton = new Button("Объединить файлы");
        grid.add(mergeButton, 1, 3);

        // Обработчик нажатия на кнопку
        mergeButton.setOnAction(event -> {
            if (directoryPath.isEmpty()) {
                new Alert(Alert.AlertType.ERROR, "Выберите папку с транзакциями!").showAndWait();
                return;
            }

            try {
                LocalDate startDate = LocalDate.parse(startDateField.getText(), DATE_FORMAT);
                LocalDate endDate = LocalDate.parse(endDateField.getText(), DATE_FORMAT);
                mergeFiles(startDate, endDate);
                new Alert(Alert.AlertType.INFORMATION, "Файлы успешно объединены!").showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Ошибка: " + e.getMessage()).showAndWait();
            }
        });

        // Создаем сцену и отображаем окно
        Scene scene = new Scene(grid, 500, 250);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void mergeFiles(LocalDate startDate, LocalDate endDate) {
        if (directoryPath.isEmpty()) {
            System.err.println("Путь к папке с транзакциями не указан.");
            return;
        }

        List<File> filesToMerge = getFilesInRange(startDate, endDate);
        File outputFile = new File(directoryPath, startDate + " - " + endDate + ".log");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (File file : filesToMerge) {
                Files.lines(file.toPath()).forEach(line -> {
                    try {
                        writer.write(line);
                        writer.newLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }
            System.out.println("Файлы успешно объединены в: " + outputFile.getAbsolutePath());
        } catch (IOException e) {
            System.err.println("Ошибка при объединении файлов: " + e.getMessage());
        }
    }

    private List<File> getFilesInRange(LocalDate startDate, LocalDate endDate) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("Указанная директория не существует или не является папкой.");
            return Collections.emptyList();
        }

        return Arrays.stream(Objects.requireNonNull(directory.listFiles((dir, name) -> name.endsWith(".log"))))
                .filter(file -> {
                    try {
                        LocalDate fileDate = LocalDate.parse(file.getName().replace(".log", ""), DATE_FORMAT);
                        return !fileDate.isBefore(startDate) && !fileDate.isAfter(endDate);
                    } catch (Exception e) {
                        return false; // Пропускаем файлы с некорректными именами
                    }
                })
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
    }
}