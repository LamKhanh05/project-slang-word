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

public class MainApp extends Application {

    private static final String INDEX_FILE = "data/slang.dat";
    private static final String TEXT_FILE = "data/slang.txt";

    private SlangService slangService;
    private SlangDAO slangDAO;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        // Phần init của bạn đã đúng, giữ nguyên
        File indexFile = new File(INDEX_FILE);
        try {
            if (indexFile.exists() && !indexFile.isDirectory()) {
                System.out.println("Loading indexed data from " + INDEX_FILE);
                this.slangDAO = SlangDAO.loadIndexedData(INDEX_FILE);
            } else {
                System.out.println("Index file not found. Building index from " + TEXT_FILE + "...");
                this.slangDAO = new SlangDAO();
                this.slangDAO.loadDataFromTextFile(TEXT_FILE);
            }
            this.slangService = new SlangService(this.slangDAO);
        } catch (Exception e) {
            System.err.println("FATAL ERROR: Could not load data.");
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Hàm start() xây dựng giao diện.
     * THAY THẾ TOÀN BỘ HÀM NÀY
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Tải FXML (Giống của bạn)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MainView.fxml"));
        Parent root = loader.load();

        // 2. Lấy Controller (Giống của bạn)
        MainViewController controller = loader.getController();
        controller.setSlangService(slangService);

        // 3. **THAY ĐỔI QUAN TRỌNG Ở ĐÂY**
        // Tăng kích thước cửa sổ và tải file CSS
        Scene scene = new Scene(root, 1000, 700); // Tăng kích thước

        // 4. **THÊM DÒNG NÀY:** Lấy đường dẫn đến file CSS
        // (Giả sử file style.css nằm trong /resources/view/ giống MainView.fxml)
        String cssPath = getClass().getResource("/view/style.css").toExternalForm();

        // 5. **THÊM DÒNG NÀY:** Áp dụng CSS cho Scene
        scene.getStylesheets().add(cssPath);

        // 6. Hiển thị cửa sổ
        primaryStage.setTitle("Slang Word Dictionary");
        primaryStage.setScene(scene); // Dùng scene đã có CSS
        primaryStage.setMinWidth(900); // Đặt kích thước tối thiểu
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        // Phần stop của bạn đã đúng, giữ nguyên
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