package ru.yandex.practicum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/*
этот класс содержит в себе список слов List<String>
    его методы похожи на методы списка, но учитывают особенности игры
    также этот класс может содержать рутинные функции по сравнению слов, букв и т.д.
 */
public class WordleDictionary {

    private List<String> words;

    // Конструктор словаря
    public WordleDictionary(List<String> words) {
        this.words = new ArrayList<>(words);
    }

    // Проверка наличия слова в словаре
    public boolean contains(String word) {
        return words.contains(word);
    }

    // Получение случайного слова из словаря
    public String getRandomWord() {
        if (words.isEmpty()) {
            throw new IllegalStateException("Словарь пуст");
        }
        Random random = new Random();
        return words.get(random.nextInt(words.size()));
    }

    // Поиск слов, подходящих под текущие знания о буквах
    public List<String> findPossibleWords(Set<Character> absentLetters,
                                          Map<Integer, Character> exactPositions,
                                          Map<Character, Set<Integer>> possiblePositions) {
        return words.stream()
                .filter(word -> matchesKnowledge(word, absentLetters, exactPositions, possiblePositions))
                .collect(Collectors.toList());
    }

    // Проверка, соответствует ли слово текущим знаниям о буквах
    private boolean matchesKnowledge(String word,
                                     Set<Character> absentLetters,
                                     Map<Integer, Character> exactPositions,
                                     Map<Character, Set<Integer>> possiblePositions) {
        // Проверка отсутствующих букв
        for (char absent : absentLetters) {
            if (word.indexOf(absent) >= 0) {
                return false;
            }
        }

        // Проверка позиций
        for (Map.Entry<Integer, Character> entry : exactPositions.entrySet()) {
            if (word.charAt(entry.getKey()) != entry.getValue()) {
                return false;
            }
        }

        // Проверка букв которые есть в слове, но не на тех позициях
        for (Map.Entry<Character, Set<Integer>> entry : possiblePositions.entrySet()) {
            char c = entry.getKey();
            if (word.indexOf(c) < 0) {
                return false;
            }

            // Буква не должна стоять на позициях, где она точно не может быть
            for (int forbiddenPos : entry.getValue()) {
                if (word.charAt(forbiddenPos) == c) {
                    return false;
                }
            }
        }

        return true;
    }

    // Нормализация слова приведение к единому нижнему регистру
    public static String normalizeWord(String word) {
        if (word == null) {
            return "";
        }
        return word.toLowerCase().replace('ё', 'е').trim();
    }

    // Фильтрация слов по длине
    public static List<String> filterByLength(List<String> words, int length) {
        return words.stream()
                .filter(word -> word.length() == length)
                .collect(Collectors.toList());
    }

    // Получить все слова словаря (неизменяемый список)
    public List<String> getAllWords() {
        return Collections.unmodifiableList(words);
    }

    // Получить размер словаря
    public int size() {
        return words.size();
    }
}
