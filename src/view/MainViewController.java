package view;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.QuizQuestion;
import model.SlangWord;
import service.SlangService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainViewController {

    // Service để gọi logic nghiệp vụ
    private SlangService slangService;

    // == KHAI BÁO BIẾN FXML (Tự động liên kết) ==

    // Vùng trung tâm
    @FXML
    private StackPane centerStackedPane;
    @FXML
    private VBox resultPane; // View kết quả
    @FXML
    private VBox quizPane;   // View Quiz
    @FXML
    private ListView<String> resultListView;

    // Vùng Quiz
    @FXML
    private Label quizTitleLabel;
    @FXML
    private Label quizQuestionLabel;
    @FXML
    private VBox quizOptionsBox; // Nơi chứa các RadioButton
    @FXML
    private Button quizSubmitButton;
    @FXML
    private Button quizCancelButton;

    // Biến nội bộ để quản lý quiz
    private QuizQuestion currentQuiz;
    private ToggleGroup quizToggleGroup;

    // Chức ... (các khai báo FXML khác) ...
    @FXML
    private TextField slangSearchField;
    @FXML
    private Button slangSearchButton;
    @FXML
    private TextField defSearchField;
    @FXML
    private Button defSearchButton;
    @FXML
    private Button historyButton;
    @FXML
    private TextField addSlangField;
    @FXML
    private TextField addDefinitionField;
    @FXML
    private Button addButton;
    @FXML
    private TextField editOldSlangField;
    @FXML
    private TextField editNewSlangField;
    @FXML
    private TextField editNewDefinitionField;
    @FXML
    private Button editButton;
    @FXML
    private TextField deleteSlangField;
    @FXML
    private Button deleteButton;
    @FXML
    private Button resetButton;
    @FXML
    private Button randomButton;
    @FXML
    private Button quizSlangButton;
    @FXML
    private Button quizDefButton;

    /**
     * Hàm này được MainApp gọi để "tiêm" service vào.
     */
    public void setSlangService(SlangService service) {
        this.slangService = service;
    }

    // ========== CÁC HÀM XỬ LÝ (onAction) ==========

    // ... (Các hàm 1-8: Find, Add, Edit, Delete, Reset, Random, History - Giữ nguyên) ...

    /**
     * Chức năng 1: Tìm theo Slang
     */
    @FXML
    private void handleFindBySlang() {
        showResultView(); // Đảm bảo đang ở view kết quả
        String slang = slangSearchField.getText();
        if (isInvalid(slang, "Please enter a slang word.")) return;

        List<String> definitions = slangService.findBySlang(slang);
        clearResults();

        if (definitions == null || definitions.isEmpty()) {
            addResult("Slang '" + slang + "' not found.");
        } else {
            addResult("Definitions for '" + slang + "':");
            resultListView.getItems().addAll(definitions);
        }
    }

    /**
     * Chức năng 2: Tìm theo Definition
     */
    @FXML
    private void handleFindByDefinition() {
        showResultView();
        String keyword = defSearchField.getText();
        if (isInvalid(keyword, "Please enter a definition keyword.")) return;

        List<String> slangs = slangService.findByDefinition(keyword);
        clearResults();

        if (slangs == null || slangs.isEmpty()) {
            addResult("No slangs found containing '" + keyword + "'.");
        } else {
            addResult("Slangs containing '" + keyword + "':");
            resultListView.getItems().addAll(slangs);
        }
    }

    /**
     * Chức năng 3: Hiển thị History
     */
    @FXML
    private void handleShowHistory() {
        showResultView();
        List<String> history = slangService.getSearchHistory();
        clearResults();

        if (history.isEmpty()) {
            addResult("No search history.");
        } else {
            addResult("Search History:");
            resultListView.getItems().addAll(history);
        }
    }

    /**
     * Chức năng 4: Add Slang
     */
    @FXML
    private void handleAddSlang() {
        showResultView();
        String slang = addSlangField.getText();
        String definition = addDefinitionField.getText();

        if (isInvalid(slang, "Slang field cannot be empty.") || isInvalid(definition, "Definition field cannot be empty.")) {
            return;
        }

        boolean overwrite = false;
        if (slangService.findBySlang(slang) != null) {
            Optional<ButtonType> result = showConfirmation("Slang Exists",
                    "Slang '" + slang + "' already exists.",
                    "Choose an option:",
                    new ButtonType("Overwrite", ButtonBar.ButtonData.OK_DONE),
                    new ButtonType("Duplicate (Add Definition)", ButtonBar.ButtonData.YES),
                    new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE)
            );

            if (result.isPresent()) {
                if (result.get().getButtonData() == ButtonBar.ButtonData.OK_DONE) {
                    overwrite = true;
                } else if (result.get().getButtonData() == ButtonBar.ButtonData.CANCEL_CLOSE) {
                    clearResults();
                    addResult("Add cancelled.");
                    return;
                }
            }
        }

        try {
            slangService.addSlang(slang, definition, overwrite);
            clearResults();
            addResult("Slang '" + slang + "' added successfully.");
            addSlangField.clear();
            addDefinitionField.clear();
        } catch (IOException e) {
            showError("Error Adding Slang", "Could not save data: " + e.getMessage());
        }
    }

    /**
     * Chức năng 5: Edit Slang
     */
    @FXML
    private void handleEditSlang() {
        showResultView();
        String oldSlang = editOldSlangField.getText();
        String newSlang = editNewSlangField.getText();
        String newDefinition = editNewDefinitionField.getText();

        if (isInvalid(oldSlang, "Old Slang field cannot be empty.") || isInvalid(newDefinition, "New Definition field cannot be empty.")) {
            return;
        }

        if (newSlang == null || newSlang.isEmpty()) {
            newSlang = oldSlang;
        }

        try {
            boolean success = slangService.editSlang(oldSlang, newSlang, newDefinition);
            clearResults();
            if (success) {
                addResult("Slang '" + oldSlang + "' edited successfully.");
                editOldSlangField.clear();
                editNewSlangField.clear();
                editNewDefinitionField.clear();
            } else {
                addResult("Error: Slang '" + oldSlang + "' not found.");
            }
        } catch (IOException e) {
            showError("Error Editing Slang", "Could not save data: " + e.getMessage());
        }
    }

    /**
     * Chức năng 6: Delete Slang
     */
    @FXML
    private void handleDeleteSlang() {
        showResultView();
        String slang = deleteSlangField.getText();
        if (isInvalid(slang, "Slang field cannot be empty.")) return;

        Optional<ButtonType> result = showConfirmation("Confirm Delete",
                "Are you sure you want to delete '" + slang + "'?",
                "This action cannot be undone.",
                ButtonType.OK, ButtonType.CANCEL
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                boolean success = slangService.deleteSlang(slang);
                clearResults();
                if (success) {
                    addResult("Slang '" + slang + "' deleted successfully.");
                    deleteSlangField.clear();
                } else {
                    addResult("Error: Slang '" + slang + "' not found.");
                }
            } catch (IOException e) {
                showError("Error Deleting Slang", "Could not save data: " + e.getMessage());
            }
        } else {
            clearResults();
            addResult("Delete cancelled.");
        }
    }

    /**
     * Chức năng 7: Reset Dictionary
     */
    @FXML
    private void handleResetDictionary() {
        showResultView();
        Optional<ButtonType> result = showConfirmation("Confirm Reset",
                "Are you sure you want to reset the dictionary?",
                "All your changes (add, edit, delete) will be lost!",
                ButtonType.OK, ButtonType.CANCEL
        );

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                slangService.resetDictionary();
                clearResults();
                addResult("Dictionary has been reset to its original state.");
            } catch (IOException e) {
                showError("Error Resetting", "Could not load/save original data: " + e.getMessage());
            }
        }
    }

    /**
     * Chức năng 8: Random Slang
     */
    @FXML
    private void handleRandomSlang() {
        showResultView();
        SlangWord slang = slangService.getRandomSlang();
        clearResults();
        if (slang != null) {
            addResult("On this day Slang Word:");
            addResult("Slang: " + slang.getSlang());
            addResult("Definitions:");
            resultListView.getItems().addAll(slang.getDefinitions());
        } else {
            addResult("No slang words available in the dictionary.");
        }
    }


    /**
     * Chức năng 9: Quiz (Guess Definition)
     */
    @FXML
    private void handleQuizGuessDefinition() {
        QuizQuestion quiz = slangService.generateQuiz(true);
        if (quiz == null) {
            showError("Quiz Error", "Not enough slangs to generate a quiz.");
            return;
        }
        startQuiz(quiz, "What is the definition of: " + quiz.getQuestion());
    }

    /**
     * Chức năng 10: Quiz (Guess Slang)
     */
    @FXML
    private void handleQuizGuessSlang() {
        QuizQuestion quiz = slangService.generateQuiz(false);
        if (quiz == null) {
            showError("Quiz Error", "Not enough slangs to generate a quiz.");
            return;
        }
        startQuiz(quiz, "Which slang has the definition: " + quiz.getQuestion());
    }

    /**
     * HÀM MỚI: Xử lý khi nhấn nút "Submit Answer"
     */
    @FXML
    private void handleQuizSubmit() {
        if (currentQuiz == null) return;

        Toggle selectedToggle = quizToggleGroup.getSelectedToggle();
        if (selectedToggle == null) {
            showError("No Selection", "Please select an answer.");
            return;
        }

        String selectedAnswer = (String) selectedToggle.getUserData();
        String correctAnswer = currentQuiz.getOptions().get(currentQuiz.getCorrectAnswerIndex());

        // Vô hiệu hóa các lựa chọn
        quizToggleGroup.getToggles().forEach(toggle -> ((RadioButton) toggle).setDisable(true));
        quizSubmitButton.setDisable(true); // Vô hiệu hóa nút Submit

        // Đổi nút "Cancel" thành "Close"
        quizCancelButton.setText("Close");

        // Hiển thị kết quả ngay bên dưới
        Label resultLabel = new Label();
        resultLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        resultLabel.setWrapText(true);
        resultLabel.setPadding(new Insets(10, 0, 0, 0));

        if (selectedAnswer.equals(correctAnswer)) {
            resultLabel.setTextFill(Color.GREEN);
            resultLabel.setText("CORRECT! Good job!");
        } else {
            resultLabel.setTextFill(Color.RED);
            resultLabel.setText("WRONG! \nThe correct answer was: " + correctAnswer);
        }

        // Thêm label kết quả vào VBox
        quizPane.getChildren().add(resultLabel);
    }

    /**
     * HÀM MỚI: Xử lý khi nhấn nút "Cancel" hoặc "Close" Quiz
     */
    @FXML
    private void handleQuizCancel() {
        // Chuyển về view results
        showResultView();

        // Dọn dẹp quiz hiện tại (nếu có)
        if (currentQuiz != null) {
            clearResults(); // Xóa ListView để hiển thị kết quả quiz

            // Chỉ hiển thị kết quả trên ListView nếu người dùng đã nộp bài
            if (quizSubmitButton.isDisabled()) {
                Toggle selectedToggle = quizToggleGroup.getSelectedToggle();
                String selectedAnswer = (String) selectedToggle.getUserData();
                String correctAnswer = currentQuiz.getOptions().get(currentQuiz.getCorrectAnswerIndex());

                addResult("--- QUIZ RESULT ---");
                addResult("Question: " + quizQuestionLabel.getText());
                addResult("Your answer: " + selectedAnswer);

                if (selectedAnswer.equals(correctAnswer)) {
                    addResult("===> CORRECT!");
                } else {
                    addResult("===> WRONG!");
                    addResult("Correct answer was: " + correctAnswer);
                }
            } else {
                // Người dùng nhấn "Cancel" trước khi nộp
                addResult("Quiz cancelled.");
            }
        }

        currentQuiz = null; // Reset
    }

    /**
     * HÀM HELPER MỚI: Hiển thị View Quiz
     */
    private void startQuiz(QuizQuestion quiz, String headerText) {
        this.currentQuiz = quiz; // Lưu quiz hiện tại

        // Cập nhật tiêu đề và câu hỏi
        quizTitleLabel.setText("QUIZ TIME!");
        quizQuestionLabel.setText(headerText);

        // Xóa các radio button cũ (chỉ xóa khỏi VBox chứa options)
        quizOptionsBox.getChildren().clear();

        // XÓA CÁC LABEL KẾT QUẢ CŨ (Sửa lỗi ClassCastException ở đây)
        // Ta chỉ xóa các Label được thêm vào dynamic sau khi submit
        quizPane.getChildren().removeIf(node ->
                node instanceof Label && ((Label) node).getFont().getSize() == 14
        );
        // Lưu ý: Dùng Font.getSize() là cách để xác định Label được thêm vào dynamic
        // vì Label cố định (quizTitleLabel) có Font size lớn hơn.

        quizToggleGroup = new ToggleGroup();

        List<String> options = quiz.getOptions();

        // Tạo RadioButton mới cho mỗi lựa chọn
        for (String option : options) {
            RadioButton rb = new RadioButton(option);
            rb.setToggleGroup(quizToggleGroup);
            rb.setUserData(option); // Lưu trữ câu trả lời (String)
            rb.setMaxWidth(Double.MAX_VALUE);
            rb.setWrapText(true);
            rb.setDisable(false); // Đảm bảo nút được bật
            quizOptionsBox.getChildren().add(rb);
        }

        // Cài đặt lại các nút
        quizSubmitButton.setDisable(false);
        quizCancelButton.setText("Cancel");

        // Chuyển view
        resultPane.setVisible(false);
        quizPane.setVisible(true);
    }

    /**
     * HÀM HELPER MỚI: Hiển thị View Kết quả
     */
    private void showResultView() {
        quizPane.setVisible(false);
        resultPane.setVisible(true);
    }

    // ========== CÁC HÀM HỖ TRỢ (Giữ nguyên) ==========

    // Xóa sạch ListView kết quả
    private void clearResults() {
        resultListView.getItems().clear();
    }

    // Thêm 1 dòng vào ListView kết quả
    private void addResult(String text) {
        resultListView.getItems().add(text);
    }

    // Kiểm tra text field có rỗng không
    private boolean isInvalid(String text, String errorMessage) {
        if (text == null || text.trim().isEmpty()) {
            showError("Invalid Input", errorMessage);
            return true;
        }
        return false;
    }

    // Hiển thị hộp thoại thông báo
    private void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Hiển thị hộp thoại lỗi
    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Hiển thị hộp thoại xác nhận (cho Delete, Reset, Add)
    private Optional<ButtonType> showConfirmation(String title, String header, String content, ButtonType... buttonTypes) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getButtonTypes().setAll(buttonTypes);
        return alert.showAndWait();
    }
}