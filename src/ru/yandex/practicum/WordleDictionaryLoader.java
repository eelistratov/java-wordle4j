package ru.yandex.practicum;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
/*
этот класс содержит в себе всю рутину по работе с файлами словарей и с кодировками
    ему нужны методы по загрузке списка слов из файла по имени файла
    на выходе должен быть класс WordleDictionary
 */
public class WordleDictionaryLoader {
    private final PrintWriter logger;

    // Конструктор загрузчика
    public WordleDictionaryLoader(PrintWriter logger) {
        this.logger = logger;
    }

    // Загрузка словаря из файла
    public WordleDictionary loadDictionary(String filePath) throws IOException {
        logger.println("Загружаем словарь из файла: " + filePath);

        List<String> allWords = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                String normalizedWord = WordleDictionary.normalizeWord(line);

                // Добавляем только слова длиной 5 букв
                if (normalizedWord.length() == 5) {
                    allWords.add(normalizedWord);
                }
            }
        }

        // Проверка, что словарь не пуст
        if (allWords.isEmpty()) {
            throw new IOException("Словарь пуст или не содержит слов длиной 5 букв");
        }

        logger.println("Загружено слов: " + allWords.size());
        return new WordleDictionary(allWords);
    }

    // Загрузка словаря с дополнительной фильтрацией
    public WordleDictionary loadDictionary(String filePath, int requiredLength) throws IOException {
        WordleDictionary dictionary = loadDictionary(filePath);
        List<String> filteredWords = WordleDictionary.filterByLength(dictionary.getAllWords(), requiredLength);

        if (filteredWords.isEmpty()) {
            throw new IOException("Нет слов длины " + requiredLength);
        }

        return new WordleDictionary(filteredWords);
    }
}
