package com.thelitblock.texteditor;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import java.io.*;
import java.util.*;
import javafx.geometry.Orientation;
import static com.thelitblock.texteditor.SyntaxHighlighting.computeHighlighting;

public class TextEditor extends Application {
    static final TabPane tabPane = new TabPane();
    static Stage primaryStage;
    static Scene scene;
    static HBox searchBar = new HBox();
    private TextField searchText;
    static MenuBar menuBar;
    static File currentFile = null;

    //search bar
    private SearchBarSetup searchBarSetup;
    // Terminal stuff
    private static TextArea terminalOutput;
    private static TextField commandInput;

    //find and replace
    private Label searchResultCount;
    private int currentSearchIndex = -1;
    private List<Integer> searchIndices = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        try {
            TextEditor.primaryStage = primaryStage;
            terminalOutput = new TextArea();
            commandInput = new TextField();

            setupMenuBar();

            VBox vBox = new VBox();
            scene = new Scene(vBox, 800, 600);
            vBox.getChildren().addAll(menuBar);
            primaryStage.setScene(scene);

            TerminalSetup terminalSetup = new TerminalSetup(terminalOutput, commandInput);
            searchBarSetup = new SearchBarSetup(searchBar, searchText, searchResultCount, scene, tabPane);
            EditorSetup editorSetup = new EditorSetup(menuBar, searchBar, tabPane, terminalSetup, scene, searchBarSetup);

            setupScene();
            primaryStage.setTitle("TextEditor");
            primaryStage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMenuBar() {
        menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(new MenuItem("New"), new MenuItem("Open"), new MenuItem("Save"), new MenuItem("Save As"), new MenuItem("Exit"));
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(new MenuItem("Cut"), new MenuItem("Copy"), new MenuItem("Paste"), new MenuItem("Select All"));
        Menu themeMenu = new Menu("Theme");
        themeMenu.getItems().addAll(new MenuItem("Dark Theme"), new MenuItem("Light Theme"));

        menuBar.getMenus().addAll(fileMenu, editMenu, themeMenu);

        fileMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler()));
        editMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler()));
        themeMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler()));
    }

    private void setupScene() {
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.F && event.isControlDown()) {
                if (searchBar.isVisible()) {
                    searchBarSetup.hideSearchBar();
                }
                else {
                    searchBarSetup.showSearchBar();
                }
                event.consume();
            }
            else if (event.getCode() == KeyCode.ENTER) {
                if (searchBar.isVisible()) {
                    searchBarSetup.navigateSearchResults(1);
                    event.consume();
                }
            }
        });

        String css = TextEditor.class.getResource("DarkTheme.css").toExternalForm();
        if (css != null) {
            scene.getStylesheets().add(css);
        }
        else {
            System.err.println("DarkTheme.css not found");
        }
    }

    //menu functions

    static CodeArea getCurrentCodeArea() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            TabData tabData = (TabData) currentTab.getUserData();
            if (tabData != null) {
                return tabData.codeArea;
            }
        }
        return null;
    }

    public static void displayFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        File file = fileChooser.showOpenDialog(primaryStage);

        if (file != null) {
            currentFile = file;
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                CodeArea codeArea = getCurrentCodeArea();
                TabData tabData = getCurrentTabData();
                if (codeArea != null && tabData != null) {
                    codeArea.clear();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        codeArea.appendText(line + "\n");
                    }
                    tabData.isChanged = false;
                    Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                    currentTab.setText(file.getName());
                }
            }
            catch (IOException e) {
                System.out.println("Error reading file");
            }
        }
        else {
            System.out.println("No file selected");
        }
    }


    protected static TabData getCurrentTabData() {
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            return (TabData) currentTab.getUserData();
        }
        return null;
    }

    public static void saveFile() {
        CodeArea codeArea = getCurrentCodeArea();
        TabData tabData = getCurrentTabData();
        if (codeArea != null && tabData != null) {
            if (currentFile == null || currentFile.getName().equals("Text Editor") || currentFile.getName().equals("Untitled")) {
                saveAsFile();
            }
            else {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                    writer.write(codeArea.getText());
                    primaryStage.setTitle(currentFile.getName());
                    tabData.isChanged = false;
                    Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                    currentTab.setText(currentFile.getName());
                }
                catch (IOException e) {
                    System.out.println("Error writing file");
                }
            }
        }
    }

    public static void saveAsFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        File file = fileChooser.showSaveDialog(primaryStage);

        if (file != null) {
            CodeArea codeArea = getCurrentCodeArea();
            TabData tabData = getCurrentTabData();
            if (codeArea != null && tabData != null) {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(codeArea.getText());
                    primaryStage.setTitle(file.getName());
                    currentFile = file;
                    tabData.isChanged = false;
                    Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                    currentTab.setText(file.getName());
                }
                catch (IOException e) {
                    System.out.println("Error writing file");
                }
            }
        }
        else {
            System.out.println("No file selected");
        }
    }


    public static void cut() {
        CodeArea codeArea = getCurrentCodeArea();
        if (codeArea != null) {
            copy();
            codeArea.deleteText(codeArea.getSelection());
        }
    }

    public static void copy() {
        CodeArea codeArea = getCurrentCodeArea();
        if (codeArea != null) {
            String selectedText = codeArea.getSelectedText();
            if (!selectedText.isEmpty()) {
                ClipboardContent content = new ClipboardContent();
                content.putString(selectedText);
                Clipboard.getSystemClipboard().setContent(content);
            }
        }
    }

    public static void paste() {
        CodeArea codeArea = getCurrentCodeArea();
        if (codeArea != null) {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            if (clipboard.hasString()) {
                codeArea.insertText(codeArea.getCaretPosition(), clipboard.getString());
            }
        }
    }

    public static void selectAll() {
        CodeArea codeArea = getCurrentCodeArea();
        if (codeArea != null) {
            codeArea.selectAll();
        }
    }

    public static void changeLightTheme() {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(TextEditor.class.getResource("LightTheme.css")).toExternalForm());
    }

    public static void changeDarkTheme() {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(Objects.requireNonNull(TextEditor.class.getResource("DarkTheme.css")).toExternalForm());
    }

    public static TextArea getTerminalOutput() {
        return terminalOutput;
    }

    public static TextField getCommandInput() {
        return commandInput;
    }

    public static void main(String[] args) {
        launch(args);
    }
}