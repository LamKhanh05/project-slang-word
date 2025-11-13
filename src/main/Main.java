package main;

import dao.SlangDAO;
import service.SlangService;
import view.ConsoleView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Lớp Main (Entry Point).
 * Khởi tạo ứng dụng.
 * Xử lý logic tải/lưu file index.
 */
public class Main {
    private static final String INDEX_FILE = "data/slang.dat"; // File lưu index
    private static final String TEXT_FILE = "data/slang.txt"; // File data gốc

    public static void main(String[] args) {
        SlangDAO dao;
        File indexFile = new File(INDEX_FILE);

        try {
            if (indexFile.exists() && !indexFile.isDirectory()) {
                dao = SlangDAO.loadIndexedData(INDEX_FILE);
            } else {
                // Lần chạy đầu tiên, xây dựng index từ file text
                System.out.println("Index file not found. Building index from " + TEXT_FILE + "...");
                dao = new SlangDAO();
                dao.loadDataFromTextFile(TEXT_FILE);
                // Lưu lại file index cho lần sau
                dao.saveIndexedData(INDEX_FILE);
            }

            SlangService service = new SlangService(dao);
            ConsoleView view = new ConsoleView(service);

            view.showMenu(); // Bắt đầu vòng lặp chính

            // Khi vòng lặp kết thúc (người dùng thoát)
            System.out.println("Saving data before exiting...");
            dao.saveIndexedData(INDEX_FILE);
            System.out.println("Goodbye!");

        } catch (FileNotFoundException e) {
            System.err.println("ERROR: Data file not found: " + TEXT_FILE);
            System.err.println("Please make sure " + TEXT_FILE + " is in the same directory.");
        } catch (IOException e) {
            System.err.println("An I/O error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("An error occurred loading indexed data. File might be corrupt.");
            e.printStackTrace();
        }
    }
}