package view;

import model.QuizQuestion;
import model.SlangWord;
import service.SlangService;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

/**
 * Lớp View (Presentation Layer).
 * Chỉ chịu trách nhiệm hiển thị UI, nhận input,
 * và gọi Service để xử lý.
 */
public class ConsoleView {
    private SlangService slangService;
    private Scanner scanner;

    public ConsoleView(SlangService service) {
        this.slangService = service;
        this.scanner = new Scanner(System.in);
    }

    public void showMenu() {
        boolean running = true;
        while (running) {
            System.out.println("\n========= SLANG WORD DICTIONARY (DAMH#1) =========");
            System.out.println("1. Find by Slang");
            System.out.println("2. Find by Definition");
            System.out.println("3. Show Search History");
            System.out.println("4. Add Slang");
            System.out.println("5. Edit Slang");
            System.out.println("6. Delete Slang");
            System.out.println("7. Reset Original Dictionary");
            System.out.println("8. Random a Slang Word ('On this day')");
            System.out.println("9. Quiz (Guess Definition)");
            System.out.println("10. Quiz (Guess Slang)");
            System.out.println("0. Exit");
            System.out.print("Your choice: ");

            try {
                String line = scanner.nextLine();
                int choice = Integer.parseInt(line);
                switch (choice) {
                    case 1: handleFindBySlang(); break;
                    case 2: handleFindByDefinition(); break;
                    case 3: handleShowHistory(); break;
                    case 4: handleAddSlang(); break;
                    case 5: handleEditSlang(); break;
                    case 6: handleDeleteSlang(); break;
                    case 7: handleResetDictionary(); break;
                    case 8: handleRandomSlang(); break;
                    case 9: handleQuiz(true); break;
                    case 10: handleQuiz(false); break;
                    case 0:
                        running = false;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            } catch (Exception e) {
                System.out.println("An error occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void handleFindBySlang() {
        System.out.print("Enter slang to find: ");
        String slang = scanner.nextLine();
        List<String> definitions = slangService.findBySlang(slang);

        if (definitions == null || definitions.isEmpty()) {
            System.out.println("Slang '" + slang + "' not found.");
        } else {
            System.out.println("Definitions for '" + slang + "':");
            for (String def : definitions) {
                System.out.println("- " + def);
            }
        }
    }

    private void handleFindByDefinition() {
        System.out.print("Enter keyword in definition to find: ");
        String keyword = scanner.nextLine();
        List<String> slangs = slangService.findByDefinition(keyword);

        if (slangs == null || slangs.isEmpty()) {
            System.out.println("No slang found containing keyword '" + keyword + "'.");
        } else {
            System.out.println("Slangs containing '" + keyword + "':");
            System.out.println(String.join(", ", slangs));
        }
    }

    private void handleShowHistory() {
        List<String> history = slangService.getSearchHistory();
        if (history.isEmpty()) {
            System.out.println("No search history.");
        } else {
            System.out.println("Search History:");
            for (String term : history) {
                System.out.println("- " + term);
            }
        }
    }

    private void handleAddSlang() throws IOException {
        System.out.print("Enter new slang: ");
        String slang = scanner.nextLine();
        System.out.print("Enter definition: ");
        String definition = scanner.nextLine();

        boolean overwrite = false;

        if (slangService.findBySlang(slang) != null) {
            System.out.print("Slang already exists. (O)verwrite or (D)uplicate definition? [O/D]: ");
            String choice = scanner.nextLine();
            if ("O".equalsIgnoreCase(choice)) {
                overwrite = true;
            }
        }

        slangService.addSlang(slang, definition, overwrite);
        System.out.println("Slang added successfully.");
    }

    private void handleEditSlang() throws IOException {
        System.out.print("Enter slang to edit: ");
        String oldSlang = scanner.nextLine();

        if (slangService.findBySlang(oldSlang) == null) {
            System.out.println("Slang not found.");
            return;
        }

        System.out.print("Enter new slang name (or press Enter to keep old): ");
        String newSlang = scanner.nextLine();
        if (newSlang.isEmpty()) {
            newSlang = oldSlang;
        }

        System.out.print("Enter new definition: ");
        String newDefinition = scanner.nextLine();

        slangService.editSlang(oldSlang, newSlang, newDefinition);
        System.out.println("Slang edited successfully.");
    }

    private void handleDeleteSlang() throws IOException {
        System.out.print("Enter slang to delete: ");
        String slang = scanner.nextLine();

        if (slangService.findBySlang(slang) == null) {
            System.out.println("Slang not found.");
            return;
        }

        System.out.print("Are you sure you want to delete '" + slang + "'? [y/N]: ");
        String confirm = scanner.nextLine();

        if ("y".equalsIgnoreCase(confirm)) {
            if (slangService.deleteSlang(slang)) {
                System.out.println("Slang deleted successfully.");
            }
        } else {
            System.out.println("Delete cancelled.");
        }
    }

    private void handleResetDictionary() throws IOException {
        System.out.print("Are you sure to reset the dictionary? All changes will be lost. [y/N]: ");
        String confirm = scanner.nextLine();
        if ("y".equalsIgnoreCase(confirm)) {
            slangService.resetDictionary();
        } else {
            System.out.println("Reset cancelled.");
        }
    }

    private void handleRandomSlang() {
        SlangWord slang = slangService.getRandomSlang();
        if (slang != null) {
            System.out.println("On this day Slang Word:");
            System.out.println(slang);
        } else {
            System.out.println("No slang words available.");
        }
    }

    private void handleQuiz(boolean slangAsQuestion) {
        QuizQuestion quiz = slangService.generateQuiz(slangAsQuestion);

        System.out.println("\n--- QUIZ TIME ---");
        if (slangAsQuestion) {
            System.out.println("What is the definition of: " + quiz.getQuestion());
        } else {
            System.out.println("What slang has the definition: " + quiz.getQuestion());
        }

        List<String> options = quiz.getOptions();
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }

        System.out.print("Your answer (1-4): ");
        try {
            int answer = Integer.parseInt(scanner.nextLine());
            if (answer - 1 == quiz.getCorrectAnswerIndex()) {
                System.out.println("Correct! Good job!");
            } else {
                System.out.println("Wrong! The correct answer was: " + (quiz.getCorrectAnswerIndex() + 1) + ". " + options.get(quiz.getCorrectAnswerIndex()));
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid answer.");
        }
    }
}