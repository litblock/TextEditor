package com.thelitblock.texteditor;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.*;
import java.util.Objects;

import static com.thelitblock.texteditor.SyntaxHighlighting.computeHighlighting;

public class TextEditor extends Application {
    private final TabPane tabPane = new TabPane();
    public static CodeArea codeArea;
    static Stage primaryStage;
    static File currentFile = null;
    static boolean isChanged = false;
    static Scene scene;
    static HBox searchBar = new HBox();
    private TextField searchText;
    static MenuBar menuBar;

    @Override
    public void start(Stage primaryStage) {
        TextEditor.primaryStage = primaryStage;
        setupEditor();
        setupMenuBar();
        setupSearchBar();
        setupScene();
        primaryStage.show();
    }

    private void setupEditor() {
        Tab defaultTab = createNewTab("Untitled");
        tabPane.getTabs().add(defaultTab);
    }

    private Tab createNewTab(String title) {
        CodeArea codeArea = new CodeArea();
        codeArea.setId("codeArea");
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.richChanges().subscribe(change -> codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText())));
        codeArea.setStyle("-fx-font-family: 'Menlo'; -fx-font-size: 10pt");

        VirtualizedScrollPane<CodeArea> virtualizedScrollPane = new VirtualizedScrollPane<>(codeArea);
        Tab tab = new Tab(title, virtualizedScrollPane);
        tab.setUserData(codeArea);
        return tab;
    }

    private void setupMenuBar() {
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(new MenuItem("New"), new MenuItem("Open"), new MenuItem("Save"), new MenuItem("Save As"), new MenuItem("Exit"));
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(new MenuItem("Cut"), new MenuItem("Copy"), new MenuItem("Paste"), new MenuItem("Select All"));
        Menu themeMenu = new Menu("Theme");
        themeMenu.getItems().addAll(new MenuItem("Dark Theme"), new MenuItem("Light Theme"));
        menuBar.getMenus().addAll(fileMenu, editMenu, themeMenu);
    }

    private void setupSearchBar() {
        searchText = new TextField();
        Button findNext = new Button("Next");
        Button findPrevious = new Button("Previous");
        searchBar.getChildren().addAll(new Label("Find:"), searchText, findNext, findPrevious);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setVisible(false);
    }

    private void setupScene() {
        VBox vBox = new VBox();
        Scene scene = new Scene(vBox, 800, 600);
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN).match(event)) {
                searchBar.setVisible(!searchBar.isVisible());
                if (searchBar.isVisible()) {
                    searchText.requestFocus();
                }
                event.consume();
            }
        });
        vBox.getChildren().addAll(new MenuBar(), tabPane);
        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);
        TextEditor.scene = scene;
        scene.getStylesheets().add(Objects.requireNonNull(TextEditor.class.getResource("DarkTheme.css")).toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void displayFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            currentFile = file;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                codeArea.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    codeArea.appendText(line + "\n");
                }
                primaryStage.setTitle(file.getName());
            }
            catch (IOException e) {
                System.out.println("Error reading file");
            }
        }
        else {
            System.out.println("No file selected");
        }
    }

    public static void saveFile() {
        if (currentFile == null || currentFile.getName().equals("Text Editor") || currentFile.getName().equals("Untitled")) {
            saveAsFile();
        }
        else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                writer.write(codeArea.getText());
                primaryStage.setTitle(currentFile.getName());
            }
            catch (IOException e) {
                System.out.println("Error writing file");
            }
        }
    }

    public static void saveAsFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        File file = fileChooser.showSaveDialog(new Stage());

        if (file != null) {
            System.out.println("File saved");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                writer.write(codeArea.getText());
                primaryStage.setTitle(file.getName());
            }
            catch (IOException e) {
                System.out.println("Error writing file");
            }
        }
        else {
            System.out.println("No file selected");
        }
    }

    public static void cut() {
        copy();
        codeArea.deleteText(codeArea.getSelection());
    }

    public static void copy() {
        String selectedText = codeArea.getSelectedText();
            if (!selectedText.isEmpty()) {
            ClipboardContent content = new ClipboardContent();
            content.putString(selectedText);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }

    public static void paste() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            codeArea.insertText(codeArea.getCaretPosition(), clipboard.getString());
        }
    }

    public static void selectAll() {
        codeArea.selectAll();
    }

    public static void changeLightTheme() {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(TextEditor.class.getResource("LightTheme.css")).toExternalForm());
    }

    public static void changeDarkTheme() {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(TextEditor.class.getResource("DarkTheme.css")).toExternalForm());
    }

    public static void main(String[] args) {
        launch(args);
    }
}