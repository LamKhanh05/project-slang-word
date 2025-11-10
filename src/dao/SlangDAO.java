package dao;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class SlangDAO implements Serializable {
    private static final long serialVersionUID = 1L;

    private HashMap<String, List<String>> slangDictionary;
    private HashMap<String, List<String>> invertedIndex;
    private HashMap<String, List<String>> originalDictionary;
    private List<String> searchHistory;

    public SlangDAO() {
        this.slangDictionary = new HashMap<>();
        this.invertedIndex = new HashMap<>();
        this.originalDictionary = new HashMap<>();
        this.searchHistory = new ArrayList<>();
    }

    public HashMap<String, List<String>> getSlangDictionary() { return slangDictionary; }
    public HashMap<String, List<String>> getInvertedIndex() { return invertedIndex; }
    public List<String> getSearchHistory() { return searchHistory; }

    public void loadDataFromTextFile(String textFile) throws IOException {
        slangDictionary.clear();
        originalDictionary.clear();

        List<String> lines = Files.readAllLines(Paths.get(textFile));
        for (String line : lines.subList(1, lines.size())) {
            if (!line.contains("`")) continue;
            String[] parts = line.split("`");
            if (parts.length < 2) continue;

            String slang = parts[0].trim().toUpperCase();
            String[] definitions = parts[1].split("\\|");

            List<String> existingDefs = slangDictionary.computeIfAbsent(slang, k -> new ArrayList<>());

            // check the duplicate definitions
            for (String def : definitions) {
                String trimmedDef = def.trim();
                if (!existingDefs.contains(trimmedDef)) { // Chỉ thêm nếu chưa tồn tại
                    existingDefs.add(trimmedDef);
                }
            }
        }

        // deep copy for the originalDictionary
        for (Map.Entry<String, List<String>> entry : slangDictionary.entrySet()) {
            originalDictionary.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        buildInvertedIndex();
        System.out.println("Loaded " + slangDictionary.size() + " slang words from " + textFile);
    }

    public void buildInvertedIndex() {
        invertedIndex.clear();
        for (Map.Entry<String, List<String>> entry : slangDictionary.entrySet()) {
            String slang = entry.getKey();
            for (String definition : entry.getValue()) {
                String[] words = definition.toLowerCase().split("\\s+");
                for (String word : words) {
                    String cleanedWord = word.replaceAll("[^a-zA-Z0-9]", "");
                    if (cleanedWord.isEmpty()) continue;
                    List<String> slangList = invertedIndex.computeIfAbsent(cleanedWord, k -> new ArrayList<>());
                    if (!slangList.contains(slang)) {
                        slangList.add(slang);
                    }
                }
            }
        }
    }

    /**
     * Reset lại slangDictionary về trạng thái gốc (lấy từ originalDictionary).
     * Nhanh và an toàn hơn việc đọc lại file.
     */
    public void resetToOriginal() {
        slangDictionary.clear();
        // Copy lại từ bản gốc
        for (Map.Entry<String, List<String>> entry : originalDictionary.entrySet()) {
            slangDictionary.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        buildInvertedIndex(); // Xây dựng lại inverted index
        searchHistory.clear(); // Xóa lịch sử tìm kiếm
    }

    public void saveIndexedData(String indexFile) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFile))) {
            oos.writeObject(this);
            System.out.println("Indexed data saved to " + indexFile);
        }
    }

    public static SlangDAO loadIndexedData(String indexFile) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFile))) {
            SlangDAO dao = (SlangDAO) ois.readObject();
            System.out.println("Indexed data loaded from " + indexFile);
            return dao;
        }
    }

    public void saveDataToTextFile(String textFile) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(textFile))) {
            List<String> sortedSlangs = new ArrayList<>(slangDictionary.keySet());
            Collections.sort(sortedSlangs);

            for (String slang : sortedSlangs) {
                List<String> definitions = slangDictionary.get(slang);
                String definitionString = String.join("| ", definitions);
                writer.write(slang + "`" + definitionString);
                writer.newLine();
            }
        }
    }
}