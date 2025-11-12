package service;

import dao.SlangDAO;
import model.QuizQuestion;
import model.SlangWord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Lớp Service (Business Logic Layer).
 * Xử lý tất cả logic nghiệp vụ, không quan tâm đến I/O file hay View.
 */
public class    SlangService {
    private SlangDAO slangDAO;
    private Random random = new Random();

    // File text data gốc
    private final String TEXT_FILE = "data/slang.txt";

    public SlangService(SlangDAO dao) {
        this.slangDAO = dao;
    }

    // Chức năng 1: Tìm theo slang word
    public List<String> findBySlang(String slang) {
        String upperSlang = slang.toUpperCase();
        slangDAO.getSearchHistory().add(slang);
        return slangDAO.getSlangDictionary().get(upperSlang);
    }

    // Chức năng 2: Tìm theo definition
    public List<String> findByDefinition(String keyword) {
        String lowerKeyword = keyword.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        slangDAO.getSearchHistory().add(keyword);
        return slangDAO.getInvertedIndex().get(lowerKeyword);
    }

    // Chức năng 3: Hiển thị history
    public List<String> getSearchHistory() {
        return slangDAO.getSearchHistory();
    }

    // Chức năng 4: Add slang word
    public boolean addSlang(String slang, String definition, boolean overwrite) throws IOException {
        String upperSlang = slang.toUpperCase();

        if (slangDAO.getSlangDictionary().containsKey(upperSlang)) {
            if (overwrite) {
                List<String> definitions = slangDAO.getSlangDictionary().get(upperSlang);
                definitions.clear();
                definitions.add(definition);
            } else {
                slangDAO.getSlangDictionary().get(upperSlang).add(definition);
            }
        } else {
            List<String> definitions = new ArrayList<>();
            definitions.add(definition);
            slangDAO.getSlangDictionary().put(upperSlang, definitions);
        }

        slangDAO.buildInvertedIndex();
        slangDAO.saveDataToTextFile(TEXT_FILE);
        return true;
    }

    // Chức năng 5: Edit slang word
    public boolean editSlang(String oldSlang, String newSlang, String newDefinition) throws IOException {
        String upperOldSlang = oldSlang.toUpperCase();
        String upperNewSlang = newSlang.toUpperCase();

        if (!slangDAO.getSlangDictionary().containsKey(upperOldSlang)) {
            return false; // Slang cũ không tồn tại
        }

        List<String> definitions = slangDAO.getSlangDictionary().remove(upperOldSlang);

        if (upperOldSlang.equals(upperNewSlang)) {
            definitions.clear();
            definitions.add(newDefinition);
            slangDAO.getSlangDictionary().put(upperNewSlang, definitions);
        } else {
            List<String> newDefinitions = new ArrayList<>();
            newDefinitions.add(newDefinition);
            slangDAO.getSlangDictionary().put(upperNewSlang, newDefinitions);
        }

        slangDAO.buildInvertedIndex();
        slangDAO.saveDataToTextFile(TEXT_FILE);
        return true;
    }

    // Chức năng 6: Delete slang word
    public boolean deleteSlang(String slang) throws IOException {
        String upperSlang = slang.toUpperCase();
        if (!slangDAO.getSlangDictionary().containsKey(upperSlang)) {
            return false; // Không tồn tại
        }

        slangDAO.getSlangDictionary().remove(upperSlang);

        slangDAO.buildInvertedIndex();
        slangDAO.saveDataToTextFile(TEXT_FILE);
        return true;
    }

    // Chức năng 7: Reset danh sách gốc
    public void resetDictionary() throws IOException {
        slangDAO.loadDataFromTextFile(TEXT_FILE);
        slangDAO.saveDataToTextFile(TEXT_FILE);
        System.out.println("Dictionary has been reset to original state.");
    }

    // Chức năng 8: Random 1 slang word
    public SlangWord getRandomSlang() {
        List<String> slangKeys = new ArrayList<>(slangDAO.getSlangDictionary().keySet());
        if (slangKeys.isEmpty()) {
            return null;
        }
        String randomSlang = slangKeys.get(random.nextInt(slangKeys.size()));
        List<String> definitions = slangDAO.getSlangDictionary().get(randomSlang);

        return new SlangWord(randomSlang, definitions);
    }

    // Chức năng 9 & 10: Đố vui
    public QuizQuestion generateQuiz(boolean slangAsQuestion) {
        List<SlangWord> randomSlangs = new ArrayList<>();

        while (randomSlangs.size() < 4) {
            SlangWord slang = getRandomSlang();
            if (slang != null && !randomSlangs.contains(slang)) {
                randomSlangs.add(slang);
            }
        }

        int correctIndex = random.nextInt(4);
        SlangWord correctSlang = randomSlangs.get(correctIndex);

        String question;
        List<String> options = new ArrayList<>();

        if (slangAsQuestion) {
            question = correctSlang.getSlang();
            for (SlangWord sw : randomSlangs) {
                options.add(sw.getDefinitions().get(0));
            }
        } else {
            question = correctSlang.getDefinitions().get(0);
            for (SlangWord sw : randomSlangs) {
                options.add(sw.getSlang());
            }
        }

        return new QuizQuestion(question, options, correctIndex);
    }
}