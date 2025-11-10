package service;

import dao.SlangDAO;
import model.QuizQuestion;
import model.SlangWord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SlangService {
    private SlangDAO slangDAO;
    private Random random = new Random();
    private final String TEXT_FILE = "slang.txt";

    public SlangService(SlangDAO dao) {
        this.slangDAO = dao;
    }

    public List<String> findBySlang(String slang) {
        String upperSlang = slang.toUpperCase();
        slangDAO.getSearchHistory().add(slang);
        return slangDAO.getSlangDictionary().get(upperSlang);
    }
    public List<String> findByDefinition(String keyword) {
        String lowerKeyword = keyword.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
        slangDAO.getSearchHistory().add(keyword);
        return slangDAO.getInvertedIndex().get(lowerKeyword);
    }
    public List<String> getSearchHistory() {
        return slangDAO.getSearchHistory();
    }


    public boolean addSlang(String slang, String definition, boolean overwrite) throws IOException {
        // Validate
        if (slang == null || slang.trim().isEmpty() || definition == null || definition.trim().isEmpty()) {
            return false; // Báo lỗi nếu input rỗng
        }

        String upperSlang = slang.trim().toUpperCase();
        String trimmedDef = definition.trim();

        if (slangDAO.getSlangDictionary().containsKey(upperSlang)) {
            if (overwrite) {
                List<String> definitions = slangDAO.getSlangDictionary().get(upperSlang);
                definitions.clear();
                definitions.add(trimmedDef);
            } else {
                // Khi duplicate, cũng kiểm tra xem definition mới này đã tồn tại chưa
                List<String> definitions = slangDAO.getSlangDictionary().get(upperSlang);
                if (!definitions.contains(trimmedDef)) {
                    definitions.add(trimmedDef);
                }
            }
        } else {
            List<String> definitions = new ArrayList<>();
            definitions.add(trimmedDef);
            slangDAO.getSlangDictionary().put(upperSlang, definitions);
        }

        slangDAO.buildInvertedIndex();
        slangDAO.saveDataToTextFile(TEXT_FILE);
        return true;
    }

    public boolean editSlang(String oldSlang, String newSlang, String newDefinition) throws IOException {
        // Validate
        if (newSlang == null || newSlang.trim().isEmpty() || newDefinition == null || newDefinition.trim().isEmpty()) {
            return false; // Báo lỗi nếu input mới rỗng
        }

        String upperOldSlang = oldSlang.toUpperCase();
        String upperNewSlang = newSlang.trim().toUpperCase();
        String trimmedNewDef = newDefinition.trim();

        // Lấy định nghĩa cũ ra (và xóa entry cũ)
        List<String> definitions = slangDAO.getSlangDictionary().remove(upperOldSlang);
        if (definitions == null) {
            // Trường hợp này gần như không xảy ra vì View đã kiểm tra
            return false;
        }

        if (upperOldSlang.equals(upperNewSlang)) {
            // Nếu không đổi tên, chỉ cập nhật definition
            definitions.clear();
            definitions.add(trimmedNewDef);
            slangDAO.getSlangDictionary().put(upperNewSlang, definitions);
        } else {
            // Nếu đổi tên, tạo entry mới
            List<String> newDefinitions = new ArrayList<>();
            newDefinitions.add(trimmedNewDef);
            slangDAO.getSlangDictionary().put(upperNewSlang, newDefinitions);
        }

        slangDAO.buildInvertedIndex();
        slangDAO.saveDataToTextFile(TEXT_FILE);
        return true;
    }

    public boolean deleteSlang(String slang) throws IOException {
        String upperSlang = slang.toUpperCase();
        if (slangDAO.getSlangDictionary().remove(upperSlang) == null) {
            return false; // Không tồn tại
        }
        slangDAO.buildInvertedIndex();
        slangDAO.saveDataToTextFile(TEXT_FILE);
        return true;
    }

    public void resetDictionary() throws IOException {
        slangDAO.resetToOriginal(); // Gọi hàm reset từ bộ nhớ
        slangDAO.saveDataToTextFile(TEXT_FILE); // Lưu trạng thái đã reset ra file .txt
        System.out.println("Dictionary has been reset to original state.");
    }

    public SlangWord getRandomSlang() {
        List<String> slangKeys = new ArrayList<>(slangDAO.getSlangDictionary().keySet());
        if (slangKeys.isEmpty()) {
            return null;
        }
        String randomSlang = slangKeys.get(random.nextInt(slangKeys.size()));
        List<String> definitions = slangDAO.getSlangDictionary().get(randomSlang);

        return new SlangWord(randomSlang, definitions);
    }

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