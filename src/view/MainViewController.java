package view;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import model.QuizQuestion;
import model.SlangWord;
import service.SlangService;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MainViewController {

    private SlangService slangService;
    private List<Node> allPanes;
    private List<Button> allNavButtons;


    // --- Navigation ---
    @FXML
    private Button navSearchButton;
    @FXML
    private Button navManageButton;
    @FXML
    private Button navPlayButton;
    @FXML
    private Button navHistoryButton;

    // --- Panes (Màn hình) ---
    @FXML
    private AnchorPane searchPane;
    @FXML
    private AnchorPane managePane;
    @FXML
    private AnchorPane playPane;
    @FXML
    private AnchorPane historyPane;

    // --- Search Pane ---
    @FXML
    private TextField searchField;
    @FXML
    private RadioButton radioFindSlang;
    @FXML
    private ListView<String> searchResultList;

    // --- History Pane ---
    @FXML
    private ListView<String> historyResultList;


    @FXML
    private void initialize() {
        allPanes = Arrays.asList(searchPane, managePane, playPane, historyPane);
        allNavButtons = Arrays.asList(navSearchButton, navManageButton, navPlayButton, navHistoryButton);

        // Mặc định chọn màn hình Search
        selectNavButton(navSearchButton);
        showPane(searchPane);
    }

    public void setSlangService(SlangService service) {
        this.slangService = service;
    }


    @FXML
    private void handleShowSearchView() {
        showPane(searchPane);
        selectNavButton(navSearchButton);
    }

    @FXML
    private void handleShowManageView() {
        showPane(managePane);
        selectNavButton(navManageButton);
    }

    @FXML
    private void handleShowPlayView() {
        showPane(playPane);
        selectNavButton(navPlayButton);
    }

    @FXML
    private void handleShowHistoryView() {
        showPane(historyPane);
        selectNavButton(navHistoryButton);
        loadHistoryList(); // Tải lịch sử khi nhấn vào
    }

    private void showPane(Node paneToShow) {
        allPanes.forEach(p -> p.setVisible(p == paneToShow));
    }

    private void selectNavButton(Button buttonToSelect) {
        allNavButtons.forEach(b -> b.getStyleClass().remove("nav-button-selected"));
        buttonToSelect.getStyleClass().add("nav-button-selected");
    }


    // --- Search Pane (Chức năng 1 & 2) ---
    @FXML
    private void handleSearch() {
        String keyword = searchField.getText();
        if (isInvalid(keyword, "Please enter a keyword.")) return;

        searchResultList.getItems().clear();

        if (radioFindSlang.isSelected()) {
            List<String> definitions = slangService.findBySlang(keyword);
            if (definitions == null || definitions.isEmpty()) {
                searchResultList.getItems().add("Slang '" + keyword + "' not found.");
            } else {
                searchResultList.getItems().add("Definitions for '" + keyword + "':");
                searchResultList.getItems().addAll(definitions);
            }
        } else {
            List<String> slangs = slangService.findByDefinition(keyword);
            if (slangs == null || slangs.isEmpty()) {
                searchResultList.getItems().add("No slangs found containing '" + keyword + "'.");
            } else {
                searchResultList.getItems().add("Slangs containing '" + keyword + "':");
                searchResultList.getItems().addAll(slangs);
            }
        }
    }

    // --- History Pane (Chức năng 3) ---
    private void loadHistoryList() {
        historyResultList.getItems().clear();
        List<String> history = slangService.getSearchHistory();
        if (history.isEmpty()) {
            historyResultList.getItems().add("No search history.");
        } else {
            historyResultList.getItems().addAll(history);
        }
    }

    @FXML
    private void handleClearHistory() {
        Optional<ButtonType> result = showConfirmation("Confirm Clear", "Are you sure you want to clear all search history?", "", ButtonType.OK, ButtonType.CANCEL);
        if (result.isPresent() && result.get() == ButtonType.OK) {
            slangService.getSearchHistory().clear(); // Xóa history
            loadHistoryList(); // Tải lại danh sách (đã rỗng)
        }
    }

    // --- Manage Pane (Chức năng 4, 5, 6, 7) ---

    @FXML
    private void handleAddSlang() {
        // 1. Tạo Dialog
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add New Slang");
        dialog.setHeaderText("Enter the new slang word and its definition.");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // 2. Tạo layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField slangField = new TextField();
        slangField.setPromptText("New Slang Word");
        TextField definitionField = new TextField();
        definitionField.setPromptText("Definition");
        grid.add(new Label("Slang:"), 0, 0);
        grid.add(slangField, 1, 0);
        grid.add(new Label("Definition:"), 0, 1);
        grid.add(definitionField, 1, 1);
        dialog.getDialogPane().setContent(grid);

        // 3. Lấy kết quả
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Pair<>(slangField.getText(), definitionField.getText());
            }
            return null;
        });
        Optional<Pair<String, String>> result = dialog.showAndWait();

        // 4. Xử lý
        if (result.isPresent()) {
            String slang = result.get().getKey();
            String definition = result.get().getValue();
            if (isInvalid(slang, "Slang field cannot be empty.") || isInvalid(definition, "Definition field cannot be empty."))
                return;

            // Xử lý trùng lặp (như code cũ)
            boolean overwrite = false;
            if (slangService.findBySlang(slang) != null) {
                Optional<ButtonType> confirmResult = showConfirmation("Slang Exists", "Slang '" + slang + "' already exists.", "Overwrite or Duplicate (add definition)?", new ButtonType("Overwrite"), new ButtonType("Duplicate"), ButtonType.CANCEL);
                if (confirmResult.isPresent()) {
                    if (confirmResult.get().getText().equals("Overwrite")) overwrite = true;
                    else if (confirmResult.get() == ButtonType.CANCEL) return;
                }
            }
            try {
                slangService.addSlang(slang, definition, overwrite);
                showInfo("Success", "Slang added successfully.", "");
            } catch (IOException e) {
                showError("Error", "Could not save data: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleEditSlang() {
        // 1. Tạo Dialog
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Edit Slang");
        dialog.setHeaderText("Enter the slang to edit and its new details.");
        ButtonType editButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(editButtonType, ButtonType.CANCEL);

        // 2. Tạo layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        TextField oldSlangField = new TextField();
        oldSlangField.setPromptText("Old Slang to Edit");
        TextField newSlangField = new TextField();
        newSlangField.setPromptText("New Slang Name (blank to keep old)");
        TextField newDefField = new TextField();
        newDefField.setPromptText("New Definition");
        grid.add(new Label("Old Slang:"), 0, 0);
        grid.add(oldSlangField, 1, 0);
        grid.add(new Label("New Slang:"), 0, 1);
        grid.add(newSlangField, 1, 1);
        grid.add(new Label("New Definition:"), 0, 2);
        grid.add(newDefField, 1, 2);
        dialog.getDialogPane().setContent(grid);

        // 3. Lấy kết quả
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == editButtonType) {
                return new String[]{oldSlangField.getText(), newSlangField.getText(), newDefField.getText()};
            }
            return null;
        });
        Optional<String[]> result = dialog.showAndWait();

        // 4. Xử lý
        if (result.isPresent()) {
            String oldSlang = result.get()[0];
            String newSlang = result.get()[1];
            String newDefinition = result.get()[2];

            if (isInvalid(oldSlang, "Old Slang field cannot be empty.") || isInvalid(newDefinition, "New Definition field cannot be empty."))
                return;
            if (newSlang == null || newSlang.isEmpty()) newSlang = oldSlang;

            try {
                if (!slangService.editSlang(oldSlang, newSlang, newDefinition)) {
                    showError("Error", "Slang '" + oldSlang + "' not found.");
                } else {
                    showInfo("Success", "Slang edited successfully.", "");
                }
            } catch (IOException e) {
                showError("Error", "Could not save data: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleDeleteSlang() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Delete Slang");
        dialog.setHeaderText("Enter the slang word you want to delete.");
        dialog.setContentText("Slang:");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && !result.get().isEmpty()) {
            String slang = result.get();
            Optional<ButtonType> confirmResult = showConfirmation("Confirm Delete", "Are you sure you want to delete '" + slang + "'?", "This action cannot be undone.", ButtonType.OK, ButtonType.CANCEL);

            if (confirmResult.isPresent() && confirmResult.get() == ButtonType.OK) {
                try {
                    if (!slangService.deleteSlang(slang)) {
                        showError("Error", "Slang '" + slang + "' not found.");
                    } else {
                        showInfo("Success", "Slang deleted successfully.", "");
                    }
                } catch (IOException e) {
                    showError("Error", "Could not save data: " + e.getMessage());
                }
            }
        }
    }

    @FXML
    private void handleResetDictionary() {
        Optional<ButtonType> result = showConfirmation("Confirm Reset", "Are you sure you want to reset the dictionary?", "All your changes and history will be lost!", ButtonType.OK, ButtonType.CANCEL);
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                slangService.resetDictionary();
                showInfo("Success", "Dictionary has been reset to original state.", "");
            } catch (IOException e) {
                showError("Error", "Could not reset dictionary: " + e.getMessage());
            }
        }
    }

    // --- Play Pane (Chức năng 8, 9, 10) ---

    @FXML
    private void handleRandomSlang() {
        SlangWord slang = slangService.getRandomSlang();
        if (slang != null) {
            showInfo("On This Day Slang Word",
                    "Slang: " + slang.getSlang(),
                    "Definitions: \n- " + String.join("\n- ", slang.getDefinitions()));
        } else {
            showError("Error", "No slang words available in the dictionary.");
        }
    }

    @FXML
    private void handleQuizGuessDefinition() {
        QuizQuestion quiz = slangService.generateQuiz(true);
        if (quiz == null) {
            showError("Quiz Error", "Not enough slangs to generate a quiz.");
            return;
        }
        showQuizDialog(quiz, "What is the definition of: " + quiz.getQuestion());
    }

    @FXML
    private void handleQuizGuessSlang() {
        QuizQuestion quiz = slangService.generateQuiz(false);
        if (quiz == null) {
            showError("Quiz Error", "Not enough slangs to generate a quiz.");
            return;
        }
        showQuizDialog(quiz, "Which slang has the definition: " + quiz.getQuestion());
    }

    private void showQuizDialog(QuizQuestion quiz, String headerText) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Quiz Time!");
        dialog.setHeaderText(headerText);

        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(20, 150, 10, 10));
        ToggleGroup group = new ToggleGroup();
        List<String> options = quiz.getOptions();

        for (String option : options) {
            RadioButton rb = new RadioButton(option);
            rb.setToggleGroup(group);
            rb.setUserData(option);
            rb.setMaxWidth(Double.MAX_VALUE);
            rb.setWrapText(true);
            rb.getStyleClass().add("quiz-radio-button");
            vbox.getChildren().add(rb);
        }
        if (!options.isEmpty()) {
            group.selectToggle(group.getToggles().getFirst());
        }
        dialog.getDialogPane().setContent(vbox);

        ButtonType okButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return (String) group.getSelectedToggle().getUserData();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();

        if (result.isPresent()) {
            String selectedAnswer = result.get();
            String correctAnswer = quiz.getOptions().get(quiz.getCorrectAnswerIndex());
            if (selectedAnswer.equals(correctAnswer)) {
                showInfo("Quiz Result", "CORRECT!", "Your answer was correct.");
            } else {
                showError("Quiz Result", "WRONG! The correct answer was: \n" + correctAnswer);
            }
        }
    }

    // ========== 3. HÀM HỖ TRỢ CHUNG ==========

    private boolean isInvalid(String text, String errorMessage) {
        if (text == null || text.trim().isEmpty()) {
            showError("Invalid Input", errorMessage);
            return true;
        }
        return false;
    }

    private void showInfo(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirmation(String title, String header, String content, ButtonType... buttonTypes) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getButtonTypes().setAll(buttonTypes);
        return alert.showAndWait();
    }
}