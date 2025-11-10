//package src.model;
//
//
//import java.io.Serializable;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.HashMap;
//
///**
// * Lớp chứa toàn bộ cấu trúc dữ liệu cho từ điển Slang.
// * Cần implements Serializable để có thể lưu/tải (Persistence).
// */
//public class SlangDictionary implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//
//    // Cấu trúc dữ liệu chính: Slang -> List of Definitions (để xử lý Duplicate)
//    private Map<String, ArrayList<String>> slangMap;
//
//    // Cấu trúc cho chức năng 2 (Tìm theo Definition) - Inverted Index
//    private Map<String, ArrayList<String>> invertedIndex;
//
//    // Cấu trúc cho chức năng 3 (Lịch sử)
//    private List<String> searchHistory;
//
//    // Dữ liệu gốc (dùng cho Reset Chức năng 7).
//    // Dùng transient để không lưu vào file .dat, mà chỉ load lại từ slang.txt.
//    private transient Map<String, ArrayList<String>> originalSlangMap;
//
//    public SlangDictionary(Map<String, ArrayList<String>> slangMap,
//                           Map<String, ArrayList<String>> invertedIndex,
//                           List<String> searchHistory) {
//        this.slangMap = slangMap;
//        this.invertedIndex = invertedIndex;
//        this.searchHistory = searchHistory;
//    }
//
//    // Constructor mặc định cho lần chạy đầu tiên
//    public SlangDictionary() {
//        this(new HashMap<>(), new HashMap<>(), new ArrayList<>());
//    }
//
//    // ======== GETTERS VÀ SETTERS ========
//
//    // (Thêm các getters/setters cho 4 thuộc tính trên)
//    // ...
//    // ...
//
//    public Map<String, ArrayList<String>> getSlangMap() {
//        return slangMap;
//    }
//    // ... (các getters và setters khác)
//    public void setOriginalSlangMap(Map<String, ArrayList<String>> originalSlangMap) {
//        this.originalSlangMap = originalSlangMap;
//    }
//    public Map<String, ArrayList<String>> getOriginalSlangMap() {
//        return originalSlangMap;
//    }
//
//    public void setInvertedIndex(Map<String, ArrayList<String>> invertedIndex) {
//    }
//
//    // Lưu ý: Không đặt logic nghiệp vụ vào đây.
//}