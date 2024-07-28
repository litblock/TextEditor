package com.thelitblock.texteditor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import java.io.*;
import java.util.*;
import javafx.scene.layout.BorderPane;

public class TextEditor extends Application {
    static final TabPane tabPane = new TabPane();
    static Stage primaryStage;
    static Scene scene;
    static HBox searchBar = new HBox();
    private TextField searchText;
    static MenuBar menuBar;
    static File currentFile = null;

    private MenuEventHandler menuEventHandler;
    //search bar
    private SearchBarSetup searchBarSetup;
    private MenuBarSetup menuBarSetup;
    // Terminal stuff
    private static TextArea terminalOutput;
    private static TextField commandInput;

    //find and replace
    private Label searchResultCount;
    private int currentSearchIndex = -1;
    private List<Integer> searchIndices = new ArrayList<>();

    //folder
    private TreeView<String> folderTreeView;
    private TreeItem<String> rootItem;

    @Override
    public void start(Stage primaryStage) {
        try {
            TextEditor.primaryStage = primaryStage;
            terminalOutput = new TextArea();
            commandInput = new TextField();

            menuBarSetup = new MenuBarSetup(tabPane);
            menuBar = MenuBarSetup.getMenuBar();

            setupFolderTreeView();

            //main
            BorderPane mainLayout = new BorderPane();
            mainLayout.setLeft(folderTreeView);  //folders on left
            mainLayout.setCenter(tabPane);

            // bottom
            VBox bottomLayout = new VBox();
            bottomLayout.getChildren().addAll(terminalOutput, commandInput);
            VBox.setVgrow(terminalOutput, Priority.ALWAYS);
            VBox.setVgrow(commandInput, Priority.NEVER);

            //root
            BorderPane rootLayout = new BorderPane();
            rootLayout.setTop(menuBar);
            rootLayout.setCenter(mainLayout);
            rootLayout.setBottom(bottomLayout);

            scene = new Scene(rootLayout, 800, 600);

            // initialize components
            TerminalSetup terminalSetup = new TerminalSetup(terminalOutput, commandInput);
            searchBarSetup = new SearchBarSetup(searchBar, searchText, searchResultCount, scene, tabPane);
            EditorSetup editorSetup = new EditorSetup(menuBar, searchBar, tabPane, terminalSetup, scene, searchBarSetup);

            setupScene();
            primaryStage.setScene(scene);
            primaryStage.setTitle("TextEditor");
            primaryStage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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

    private void setupFolderTreeView() {
        rootItem = new TreeItem<>("Root");
        folderTreeView = new TreeView<>(rootItem);
        folderTreeView.setShowRoot(false);

        folderTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TreeItem<String> selectedItem = newValue;
                File file = (File) selectedItem.getGraphic().getUserData();
                if (file.isFile()) {
                    openFile(file);
                }
            }
        });

        Button openDirButton = new Button("Open Folder");
        openDirButton.setOnAction(e -> openFolder());
        searchBar.getChildren().add(openDirButton);
    }

    private void openFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(primaryStage);

        if (selectedDirectory != null) {
            rootItem.getChildren().clear();
            createTree(selectedDirectory, rootItem);
        }
    }

    private void createTree(File directory, TreeItem<String> parent) {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            TreeItem<String> item = new TreeItem<>(file.getName());
            parent.getChildren().add(item);

            if (file.isDirectory()) {
                createTree(file, item);
            }
            else {
                item.setGraphic(new Label(file.getName()));
                item.getGraphic().setUserData(file);
            }
        }
    }

    private void openFile(File file) {
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