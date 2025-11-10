package main;

import dao.SlangDAO;
import service.SlangService;
import view.ConsoleView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class Main {
    private static final String INDEX_FILE = "data/slang.dat";
    private static final String TEXT_FILE = "data/slang.txt";

    public static void main(String[] args) {
        SlangDAO dao = null;
        File indexFile = new File(INDEX_FILE);

        // Cố gắng tải file index trước
        if (indexFile.exists() && !indexFile.isDirectory()) {
            try {
                dao = SlangDAO.loadIndexedData(INDEX_FILE);
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Warning: Indexed file '" + INDEX_FILE + "' is corrupt or incompatible.");
                System.err.println("Deleting corrupt file and rebuilding from " + TEXT_FILE + "...");
                indexFile.delete(); // Xóa file hỏng
                dao = null; // Đặt lại dao để chắc chắn tải lại
            }
        }

        // Nếu dao chưa được tải (lần chạy đầu hoặc file index bị hỏng)
        if (dao == null) {
            try {
                System.out.println("Building index from " + TEXT_FILE + "...");
                dao = new SlangDAO();
                dao.loadDataFromTextFile(TEXT_FILE);
                dao.saveIndexedData(INDEX_FILE); // Lưu lại file index mới
            } catch (FileNotFoundException e) {
                System.err.println("ERROR: Data file not found: " + TEXT_FILE);
                System.err.println("Please make sure " + TEXT_FILE + " is in the same directory. Exiting.");
                return; // Không thể chạy nếu không có file .txt
            } catch (IOException e) {
                System.err.println("An I/O error occurred while building index: " + e.getMessage());
                e.printStackTrace();
                return;
            }
        }

        // Từ đây, chương trình chạy bình thường với 'dao' đã được đảm bảo
        SlangService service = new SlangService(dao);
        ConsoleView view = new ConsoleView(service);

        view.showMenu();

        try {
            System.out.println("Saving data before exiting...");
            dao.saveIndexedData(INDEX_FILE);
            System.out.println("Goodbye!");
        } catch (IOException e) {
            System.err.println("An I/O error occurred while saving index: " + e.getMessage());
            e.printStackTrace();
        }
    }
}