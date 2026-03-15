package ru.yandex.practicum;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/*
в главном классе нам нужно:
    создать лог-файл (он должен передаваться во все классы)
    создать загрузчик словарей WordleDictionaryLoader
    загрузить словарь WordleDictionary с помощью класса WordleDictionaryLoader
    затем создать игру WordleGame и передать ей словарь
    вызвать игровой метод в котором в цикле опрашивать пользователя и передавать информацию в игру
    вывести состояние игры и конечный результат
 */
public class Wordle {
    private static PrintWriter logger;
    private static final String DICTIONARY_FILE = "words_ru.txt";
    private static final String LOG_FILE_PREFIX = "wordle_log_";

    public static void main(String[] args) {
        try {
            // Инициализация логгера
            initializeLogger();

            logger.println("=== Игра Wordle ===");
            logger.println("Запуск: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            // Загрузка словаря
            WordleDictionaryLoader loader = new WordleDictionaryLoader(logger);
            WordleDictionary dictionary = loadDictionary(loader);

            // Создание и запуск игры
            playGame(dictionary);

        } catch (Exception e) {
            // Любая ошибка
            logAndPrintError("Ошибка", e);
            System.exit(1);

        } finally {
            // Закрываем логгер
            if (logger != null) {
                logger.println("Завершение работы: " + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                logger.close();
            }
        }
    }

    // Инициализация логгера
    private static void initializeLogger() throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String logFileName = LOG_FILE_PREFIX + timestamp + ".log";

        FileOutputStream fos = new FileOutputStream(logFileName);
        OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
        BufferedWriter bw = new BufferedWriter(osw);
        logger = new PrintWriter(bw, true);

        System.out.println("Лог-файл: " + logFileName);
    }

    // Загрузка словаря из файла
    private static WordleDictionary loadDictionary(WordleDictionaryLoader loader) {
        try {
            WordleDictionary dictionary = loader.loadDictionary(DICTIONARY_FILE, 5);
            System.out.println("Словарь успешно загружен. Количество слов: " + dictionary.size());
            return dictionary;
        } catch (Exception e) {
            System.err.println("Критическая ошибка: Не удалось загрузить словарь - " + e.getMessage());
            System.exit(1);
            return null; // никогда не выполнится
        }
    }

    // Основной игровой цикл
    private static void playGame(WordleDictionary dictionary) {
        try (Scanner scanner = new Scanner(System.in)) {

            WordleGame game = new WordleGame(dictionary, logger);

            System.out.println("\nWordle");
            System.out.println("Загадано слово из 5 букв. У вас 6 попыток.");
            System.out.println("Введите слово (или пустую строку для подсказки):");

            boolean gameActive = true;

            while (gameActive) {
                System.out.print("\nПопытка " + (game.getCurrentStep() + 1) + "/" + game.getMaxSteps() + ": ");

                String input = scanner.nextLine().trim().toLowerCase();

                // Обработка пустой строки - подсказка
                if (input.isEmpty()) {
                    String hint = game.getHint();
                    if (hint != null) {
                        System.out.println("Подсказка: " + hint);
                    } else {
                        System.out.println("Подходящих подсказок больше нет");
                    }
                    continue;
                }

                // Обработка команды выхода
                if (input.equals("exit") || input.equals("выход")) {
                    System.out.println("Игра прервана. Загаданное слово: " + game.getAnswer());
                    logger.println("Игра прервана пользователем");
                    break;
                }

                try {
                    String result = game.makeGuess(input);
                    System.out.println("Результат: " + result);

                    if (game.isWin()) {
                        System.out.println("\nВы угадали слово \"" + game.getAnswer() + "\"!");
                        System.out.println("Количество попыток: " + game.getCurrentStep());
                        gameActive = false;
                    } else if (game.isGameOver()) {
                        System.out.println("\nВы не угадали слово. Загаданное слово: " + game.getAnswer());
                        System.out.println("Использовано попыток: " + game.getCurrentStep());
                        gameActive = false;
                    }
                } catch (RuntimeException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                    logger.println("Ошибка ввода: " + e.getMessage());
                }
            }
        }
    }

    // Логирование ошибки и вывод в консоль
    private static void logAndPrintError(String message, Exception e) {
        String fullMessage = message + ": " + e.getMessage();
        System.err.println(fullMessage);
        if (logger != null) {
            logger.println("ОШИБКА: " + fullMessage);
            e.printStackTrace(logger);
        }
    }
}

