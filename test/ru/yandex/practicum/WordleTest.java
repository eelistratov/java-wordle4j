package ru.yandex.practicum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class WordleTest {
    private WordleDictionary dictionary;
    private WordleGame game;
    private PrintWriter testLogger;

    @BeforeEach
    void setUp() throws Exception {
        testLogger = new PrintWriter(System.out, true);
        WordleDictionaryLoader loader = new WordleDictionaryLoader(testLogger);
        dictionary = loader.loadDictionary("words_ru.txt", 5);
    }

    // Тесты словаря

    @Test
    @DisplayName("Проверка наличия слова в словаре")
    void testDictionaryContains() {
        // Проверяем, что реальные слова из словаря находятся
        assertTrue(dictionary.contains("стол"));
        assertTrue(dictionary.contains("ручка"));
        assertTrue(dictionary.contains("бокал"));
        assertTrue(dictionary.contains("вилка"));
        assertTrue(dictionary.contains("мышь"));

        // Проверяем, что несуществующие слова не находятся
        assertFalse(dictionary.contains("ераув"));
        assertFalse(dictionary.contains(""));
        assertFalse(dictionary.contains("гульванить"));
    }

    @Test
    @DisplayName("Проверка фильтрации слов по длине")
    void testFilterByLength() {
        // Проверяем, что все слова в словаре имеют длину 5
        List<String> allWords = dictionary.getAllWords();
        for (String word : allWords) {
            assertEquals(5, word.length(), "Слово " + word + " должно быть длиной 5 символов");
        }
        // Проверяем, что размер словаря > 0 (должно быть много слов)
        assertTrue(dictionary.size() > 1000, "Словарь должен содержать более 1000 слов");
    }

    //  Вспомогательный метод для тестов
    private WordleGame createGameWithAnswer(String targetAnswer) {
        WordleGame testGame = new WordleGame(dictionary, testLogger);

        try {
            java.lang.reflect.Field answerField = WordleGame.class.getDeclaredField("answer");
            answerField.setAccessible(true);
            answerField.set(testGame, targetAnswer);
        } catch (Exception e) {
            fail("Не удалось установить тестовый ответ: " + e.getMessage());
        }
        return testGame;
    }

    // Тесты игровой логики

    @Test
    @DisplayName("Проверка анализа слова при полном совпадении")
    void testAnalyzeGuessFullMatch() throws Exception {
        WordleGame testGame = createGameWithAnswer("рукав");
        String result = testGame.makeGuess("рукав");
        assertEquals("+++++", result);
        assertTrue(testGame.isWin());
        assertEquals(1, testGame.getCurrentStep());
    }

    @Test
    @DisplayName("Проверка анализа при частичном совпадении")
    void testAnalyzeGuessPartialMatch() throws Exception {
        // Тест 1: слово "ру" + "ка" - буквы на разных позициях
        WordleGame testGame1 = createGameWithAnswer("рукав");
        String result1 = testGame1.makeGuess("ручка");
        assertEquals("++-^^", result1);

        // Тест 2: слово "песок"
        WordleGame testGame2 = createGameWithAnswer("песок");
        String result2 = testGame2.makeGuess("покос");
        assertEquals("+^^-^", result2);

        // Тест 3: слово "молот"
        WordleGame testGame3 = createGameWithAnswer("молот");
        String result3 = testGame3.makeGuess("мотор");
        assertEquals("++^--", result3);
    }

    @Test
    @DisplayName("Проверка анализа при отсутствии совпадений")
    void testAnalyzeGuessNoMatch() throws Exception {
        WordleGame testGame = createGameWithAnswer("полка");
        // Слово, не содержащее ни одной буквы из ответа
        String result = testGame.makeGuess("бугор");
        assertEquals("-----", result);
        assertFalse(testGame.isWin());
        assertEquals(1, testGame.getCurrentStep());
    }

    @Test
    @DisplayName("Проверка анализа с повторяющимися буквами")
    void testAnalyzeGuessWithDuplicateLetters() throws Exception {
        WordleGame testGame = createGameWithAnswer("молот");
        String result = testGame.makeGuess("ооооо");
        assertEquals("^+^+-", result);
        assertFalse(testGame.isWin());
    }

    @Test
    @DisplayName("Проверка условия победы")
    void testWinCondition() throws Exception {
        WordleGame testGame = createGameWithAnswer("ручка");
        assertFalse(testGame.isWin());
        assertFalse(testGame.isGameOver());
        // Делаем правильный ход
        testGame.makeGuess("ручка");
        assertTrue(testGame.isWin());
        assertTrue(testGame.isGameOver());
        assertEquals(1, testGame.getCurrentStep());
    }

    @Test
    @DisplayName("Проверка условия проигрыша")
    void testLoseCondition() throws Exception {
        WordleGame testGame = createGameWithAnswer("ручка");
        for (int i = 0; i < 5; i++) {
            testGame.makeGuess("молот");
            assertFalse(testGame.isWin());
            assertFalse(testGame.isGameOver());
            assertEquals(i + 1, testGame.getCurrentStep());
        }

        // Шестая попытка
        testGame.makeGuess("почка");
        assertFalse(testGame.isWin());
        assertTrue(testGame.isGameOver());
        assertEquals(6, testGame.getCurrentStep());
        assertEquals(0, testGame.getAttemptsLeft());
    }

    @Test
    @DisplayName("Проверка некорректного ввода (не из словаря)")
    void testInvalidWordNotInDictionary() {
        WordleGame testGame = new WordleGame(dictionary, testLogger);
        // Пытаемся ввести слово не из словаря
        Exception exception = assertThrows(RuntimeException.class, () -> {
            testGame.makeGuess("абвгд");
        });
        assertTrue(exception.getMessage().contains("отсутствует в словаре"));
        assertEquals(0, testGame.getCurrentStep()); // Ход не засчитан
    }

    @Test
    @DisplayName("Проверка некорректного ввода (неправильная длина)")
    void testInvalidWordWrongLength() {
        WordleGame testGame = new WordleGame(dictionary, testLogger);
        // Слово слишком короткое
        Exception exception1 = assertThrows(RuntimeException.class, () -> {
            testGame.makeGuess("кот");
        });
        assertTrue(exception1.getMessage().contains("должно быть из 5 букв"));

        // Слово слишком длинное
        Exception exception2 = assertThrows(RuntimeException.class, () -> {
            testGame.makeGuess("котопес");
        });
        assertTrue(exception2.getMessage().contains("должно быть из 5 букв"));
        assertEquals(0, testGame.getCurrentStep()); // Ходы не засчитаны
    }

    @Test
    @DisplayName("Проверка работы подсказок")
    void testHints() throws Exception {
        WordleGame testGame = createGameWithAnswer("книга");

        // Первая подсказка - должно быть какое-то слово
        String hint1 = testGame.getHint();
        assertNotNull(hint1);
        assertEquals(5, hint1.length());

        // Вводим слово, чтобы обновить знания
        testGame.makeGuess("почта"); // +-^--

        // Вторая подсказка должна учитывать новые знания
        String hint2 = testGame.getHint();
        assertNotNull(hint2);

        // Подсказки не должны повторяться
        assertNotEquals(hint1, hint2);

        // Проверяем, что подсказка содержит 'к' на первой позиции (из предыдущего хода)
        // и не содержит 'н', 'г', 'а'
        if (hint2 != null && hint2.length() > 0) {
            assertEquals('к', hint2.charAt(0));
            assertFalse(hint2.contains("н"));
            assertFalse(hint2.contains("г"));
            assertFalse(hint2.contains("а"));
        }
    }

    @Test
    @DisplayName("Проверка, что подсказки не повторяются")
    void testHintsNoDuplicates() throws Exception {
        WordleGame testGame = createGameWithAnswer("почта");

        // Собираем несколько подсказок
        String hint1 = testGame.getHint();
        String hint2 = testGame.getHint();
        String hint3 = testGame.getHint();

        // Проверяем, что они разные (если словарь позволяет)
        assertNotEquals(hint1, hint2);
        assertNotEquals(hint1, hint3);
        assertNotEquals(hint2, hint3);
    }
}
