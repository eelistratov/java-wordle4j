package ru.yandex.practicum;
import java.io.PrintWriter;
import java.util.*;
/*
в этом классе хранится словарь и состояние игры
    текущий шаг
    всё что пользователь вводил
    правильный ответ

в этом классе нужны методы, которые
    проанализируют совпадение слова с ответом
    предложат слово-подсказку с учётом всего, что вводил пользователь ранее

не забудьте про специальные типы исключений для игровых и неигровых ошибок
 */
public class WordleGame {
    private final String answer;                    // загаданное слово
    private int steps;                               // количество сделанных шагов
    private final WordleDictionary dictionary;       // словарь
    private final List<String> guesses;              // всё что пользователь вводил
    private final Set<String> hintsGiven;             // уже предложенные подсказки
    private final PrintWriter logger;                 // логгер

    // Информация о буквах (храним прямо в классе)
    private final Set<Character> absentLetters;           // буквы, которых нет в слове
    private final Map<Integer, Character> exactPositions; // точные позиции букв
    private final Map<Character, Set<Integer>> possiblePositions; // возможные позиции для букв

    private static final int MAX_STEPS = 6;          // максимальное количество шагов
    private static final int WORD_LENGTH = 5;

    // Конструктор игры
    public WordleGame(WordleDictionary dictionary, PrintWriter logger) {
        this.dictionary = dictionary;
        this.logger = logger;
        this.answer = dictionary.getRandomWord();
        this.steps = 0;
        this.guesses = new ArrayList<>();
        this.hintsGiven = new HashSet<>();
        this.absentLetters = new HashSet<>();
        this.exactPositions = new HashMap<>();
        this.possiblePositions = new HashMap<>();

        logger.println("Новая игра. Загадано слово из 5 букв");
        logger.println("Загаданное слово (для отладки): " + answer);
    }

    // Ввести слово
    public String makeGuess(String word) {
        // Проверка состояния игры
        if (isGameOver()) {
            throw new RuntimeException("Игра уже завершена");
        }

        // Нормализация слова
        String normalizedWord = WordleDictionary.normalizeWord(word);

        // Проверка длины
        if (normalizedWord.length() != WORD_LENGTH) {
            throw new RuntimeException("Слово должно быть из 5 букв");
        }

        // Проверка наличия в словаре
        if (!dictionary.contains(normalizedWord)) {
            throw new RuntimeException("Слово " + normalizedWord + " отсутствует в словаре");
        }

        // Сохраняем попытку
        guesses.add(normalizedWord);
        steps++;

        // Проверяем слово
        String result = analyzeGuess(normalizedWord);

        // Обновляем информацию о буквах
        updateLetterKnowledge(normalizedWord, result);

        logger.println("Ход " + steps + ": " + normalizedWord + " -> " + result);
        return result;
    }

    // Проверка введенного слова на совпадение с ответом
    private String analyzeGuess(String guess) {
        char[] result = new char[5];
        boolean[] answerUsed = new boolean[5];
        boolean[] guessUsed = new boolean[5];

        // Точные совпадения +
        for (int i = 0; i < 5; i++) {
            if (guess.charAt(i) == answer.charAt(i)) {
                result[i] = '+';
                answerUsed[i] = true;
                guessUsed[i] = true;
            }
        }

        // Проверяем буквы в других позициях ^
        for (int i = 0; i < 5; i++) {
            if (guessUsed[i]) continue;

            char guessChar = guess.charAt(i);
            boolean found = false;

            for (int j = 0; j < 5; j++) {
                if (!answerUsed[j] && guessChar == answer.charAt(j)) {
                    result[i] = '^';
                    answerUsed[j] = true;
                    found = true;
                    break;
                }
            }

            if (!found) {
                result[i] = '-';
            }
        }

        return new String(result);
    }

    // Обновление информации о буквах на основе попытки
    private void updateLetterKnowledge(String guess, String result) {
        for (int i = 0; i < 5; i++) {
            char c = guess.charAt(i);
            char res = result.charAt(i);

            if (res == '+') {
                exactPositions.put(i, c);
            } else if (res == '^') {
                possiblePositions.computeIfAbsent(c, k -> new HashSet<>()).add(i);
            } else if (res == '-') {
                if (answer.indexOf(c) < 0) {
                    absentLetters.add(c);
                } else {
                    // Буква есть в ответе, но не на этой позиции
                    possiblePositions.computeIfAbsent(c, k -> new HashSet<>()).add(i);
                }
            }
        }
    }

    // Получить подсказку - подходящее слово из словаря
    public String getHint() {
        List<String> possibleWords = dictionary.findPossibleWords(
                absentLetters, exactPositions, possiblePositions);

        possibleWords.removeAll(hintsGiven);

        if (possibleWords.isEmpty()) {
            logger.println("Слов для подсказки не найдено");
            return null;
        }

        Random random = new Random();
        String hint = possibleWords.get(random.nextInt(possibleWords.size()));
        hintsGiven.add(hint);

        logger.println("Подсказка: " + hint);
        return hint;
    }

    // Проверка, закончена ли игра
    public boolean isGameOver() {
        return steps >= MAX_STEPS || isWin();
    }

    // Проверка, угадано ли слово
    public boolean isWin() {
        return !guesses.isEmpty() && guesses.get(guesses.size() - 1).equals(answer);
    }

    // Получить загаданное слово (для отладки и вывода в конце игры)
    public String getAnswer() {
        return answer;
    }

    // Получить количество сделанных попыток
    public int getCurrentStep() {
        return steps;
    }

    // Получить максимальное количество шагов
    public int getMaxSteps() {
        return MAX_STEPS;
    }

    // Получить оставшиеся попытки
    public int getAttemptsLeft() {
        return MAX_STEPS - steps;
    }

    // Получить историю попыток
    public List<String> getGuesses() {
        return Collections.unmodifiableList(guesses);
    }
}
