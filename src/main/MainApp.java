package main;

import dao.SlangDAO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import service.SlangService;
import view.MainViewController;

import java.io.File;
import java.io.IOException;

/**
 * Lớp khởi động chính cho JavaFX.
 * Thay thế cho Main.java cũ.
 */
public class MainApp extends Application {

    private static final String INDEX_FILE = "data/slang.dat";
    private static final String TEXT_FILE = "data/slang.txt";

    private SlangService slangService;
    private SlangDAO slangDAO; // Giữ lại tham chiếu DAO

    // Điểm khởi đầu của ứng dụng
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Hàm init() chạy trước khi giao diện hiển thị.
     * Chúng ta tải toàn bộ dữ liệu ở đây, giống hệt Main.java cũ.
     */
    @Override
    public void init() throws Exception {
        File indexFile = new File(INDEX_FILE);

        try {
            if (indexFile.exists() && !indexFile.isDirectory()) {
                // Tải file index đã tối ưu
                System.out.println("Loading indexed data from " + INDEX_FILE);
                this.slangDAO = SlangDAO.loadIndexedData(INDEX_FILE);
            } else {
                // Lần chạy đầu tiên, xây dựng index từ file text
                System.out.println("Index file not found. Building index from " + TEXT_FILE + "...");
                this.slangDAO = new SlangDAO();
                this.slangDAO.loadDataFromTextFile(TEXT_FILE);
            }
            this.slangService = new SlangService(this.slangDAO);

        } catch (Exception e) {
            System.err.println("FATAL ERROR: Could not load data.");
            e.printStackTrace();
            throw e; // Ném lỗi để dừng ứng dụng
        }
    }

    /**
     * Hàm start() xây dựng giao diện.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // trong thư mục "resources"
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
        Parent root = loader.load();

        // 2. Lấy Controller ra
        MainViewController controller = loader.getController();

        // 3. "Tiêm" service vào cho Controller
        controller.setSlangService(slangService);

        // 4. Hiển thị cửa sổ
        primaryStage.setTitle("Slang Word Dictionary (DAMH#1)");
        primaryStage.setScene(new Scene(root, 800, 600)); // Set kích thước
        primaryStage.show();
    }

    /**
     * Hàm stop() chạy khi người dùng đóng cửa sổ.
     * Chúng ta lưu file index tại đây.
     */
    @Override
    public void stop() throws Exception {
        try {
            if (this.slangDAO != null) {
                System.out.println("Saving indexed data to " + INDEX_FILE + "...");
                this.slangDAO.saveIndexedData(INDEX_FILE);
                System.out.println("Data saved. Goodbye!");
            }
        } catch (IOException e) {
            System.err.println("Could not save data on exit.");
            e.printStackTrace();
        }
    }
}