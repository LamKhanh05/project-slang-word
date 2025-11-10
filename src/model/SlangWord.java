package model;

import java.io.Serializable;
import java.util.List;

/**
 * Lớp Model đại diện cho một từ lóng.
 * Implement Serializable để có thể lưu đối tượng xuống file.
 */
public class SlangWord implements Serializable {
    private String slang;
    private List<String> definitions;

    public SlangWord(String slang, List<String> definitions) {
        this.slang = slang;
        this.definitions = definitions;
    }

    public String getSlang() { return slang; }
    public List<String> getDefinitions() { return definitions; }

    @Override
    public String toString() {
        return "Slang: " + slang + "\nDefinitions: " + String.join(", ", definitions);
    }
}