package dao;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Lớp DAO (Data Access Object)
 * Quản lý toàn bộ việc truy cập, lưu trữ, và tối ưu dữ liệu.
 * Implement Serializable để lưu toàn bộ các cấu trúc index đã xây dựng.
 */
public class SlangDAO implements Serializable {
    // Đảm bảo tương thích khi serialize
    private static final long serialVersionUID = 1L;

    // Cấu trúc 1: Tối ưu Chức năng 1 (Tìm theo Slang)
    private HashMap<String, List<String>> slangDictionary;

    // Cấu trúc 2: Tối ưu Chức năng 2 (Tìm theo Definition) - "Inverted Index"
    private HashMap<String, List<String>> invertedIndex;

    // Cấu trúc 3: Lịch sử tìm kiếm (Chức năng 3)
    private List<String> searchHistory;

    public SlangDAO() {
        this.slangDictionary = new HashMap<>();
        this.invertedIndex = new HashMap<>();
        this.searchHistory = new ArrayList<>();
    }

    /**
     * Tải dữ liệu (đã index) từ file binary.
     */
    public static SlangDAO loadIndexedData(String indexFile) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(indexFile))) {
            SlangDAO dao = (SlangDAO) ois.readObject();
            System.out.println("Indexed data loaded from " + indexFile);
            return dao;
        }
    }

    // --- Getters cho Service ---
    public HashMap<String, List<String>> getSlangDictionary() {
        return slangDictionary;
    }

    public HashMap<String, List<String>> getInvertedIndex() {
        return invertedIndex;
    }

    public void clearHistory() {
        searchHistory.clear();
    }

    public List<String> getSearchHistory() {
        return searchHistory;
    }

    /**
     * Tải dữ liệu từ file text (slang.txt)
     */
    public void loadDataFromTextFile(String textFile) throws IOException {
        slangDictionary.clear();

        List<String> lines = Files.readAllLines(Paths.get(textFile));
        for (String line : lines.subList(1, lines.size())) {
            if (!line.contains("`")) continue;

            String[] parts = line.split("`");
            if (parts.length < 2) continue;

            String slang = parts[0].trim().toUpperCase(); // Chuẩn hóa Slang về UpperCase
            String[] definitions = parts[1].split("\\|");

            List<String> existingDefs = slangDictionary.computeIfAbsent(slang, k -> new ArrayList<>());

            for (String def : definitions) {
                existingDefs.add(def.trim());
            }
        }

        // Xây dựng Inverted Index
        buildInvertedIndex();
        System.out.println("Loaded " + slangDictionary.size() + " slang words from " + textFile);
    }

    /**
     * Xây dựng Inverted Index từ Slang Dictionary.
     */
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
     * Lưu dữ liệu (đã index) xuống file binary.
     */
    public void saveIndexedData(String indexFile) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(indexFile))) {
            oos.writeObject(this);
            System.out.println("Indexed data saved to " + indexFile);
        }
    }
}