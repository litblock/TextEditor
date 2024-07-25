package com.thelitblock.texteditor;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.*;
import java.util.Objects;
import java.util.Optional;

import static com.thelitblock.texteditor.SyntaxHighlighting.computeHighlighting;

public class TextEditor extends Application {
    static final TabPane tabPane = new TabPane();
    static Stage primaryStage;
    static Scene scene;
    static HBox searchBar = new HBox();
    private TextField searchText;
    static MenuBar menuBar;
    static File currentFile = null;

    @Override
    public void start(Stage primaryStage) {
        TextEditor.primaryStage = primaryStage;
        setupEditor();
        setupMenuBar();
        setupScene();
        primaryStage.show();
    }

    private void setupEditor() {
        Tab defaultTab = createNewTab();
        tabPane.getTabs().add(defaultTab);
    
        Tab plusTab = new Tab("+");
        plusTab.setClosable(false);
        tabPane.getTabs().add(plusTab);
    
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == plusTab) {
                Tab newTabToAdd = createNewTab();
                tabPane.getTabs().add(tabPane.getTabs().size() - 1, newTabToAdd);
                tabPane.getSelectionModel().select(newTabToAdd);
            }
        });
    }
    
    static Tab createNewTab() {
        CodeArea codeArea = new CodeArea();
        codeArea.setId("codeArea");
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
    
        Font menloFont = Font.loadFont(Objects.requireNonNull(TextEditor.class.getResourceAsStream("Menlo-Regular.woff")), 12);
        codeArea.setStyle("-fx-font-family: 'Menlo'; -fx-font-size: 10pt");
    
        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
            TabData tabData = (TabData) currentTab.getUserData();
            if (tabData != null) {
                tabData.isChanged = true;
                if (!currentTab.getText().endsWith("*")) {
                    currentTab.setText(currentTab.getText() + "*");
                }
            }
        });
    
        codeArea.richChanges()
            .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
            .subscribe(change -> {
                codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
            });

        return getTab(codeArea);
    }

    private static Tab getTab(CodeArea codeArea) {
        VirtualizedScrollPane<CodeArea> virtualizedScrollPane = new VirtualizedScrollPane<>(codeArea);
        Tab tab = new Tab("Untitled", virtualizedScrollPane);
        TabData tabData = new TabData(codeArea);
        tab.setUserData(tabData);

        tab.setOnCloseRequest(event -> {
            if (tabData.isChanged) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved Changes");
                alert.setHeaderText("You have unsaved changes.");
                alert.setContentText("Do you want to save before closing?");

                ButtonType btnSave = new ButtonType("Save");
                ButtonType btnDontSave = new ButtonType("Don't Save");
                ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(btnSave, btnDontSave, btnCancel);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent()) {
                    if (result.get() == btnSave) {
                        saveFile();
                    }
                    else if (result.get() == btnCancel) {
                        event.consume();
                    }
                }
            }
        });
        return tab;
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

        searchText = new TextField();
        Button findNext = new Button("Next");
        Button findPrevious = new Button("Previous");
        searchBar.getChildren().addAll(new Label("Find:"), searchText, findNext, findPrevious);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setVisible(false);
        menuBar.getMenus().add(new Menu("", searchBar));
    }

    private void setupScene() {
        VBox vBox = new VBox();
        Scene scene = new Scene(vBox, 800, 600);
        vBox.getChildren().addAll(menuBar, tabPane);
        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);
        TextEditor.scene = scene;
        scene.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            final KeyCombination keyComb = new KeyCodeCombination(KeyCode.F, KeyCombination.CONTROL_DOWN);
            if (keyComb.match(event)) {
                searchBar.setVisible(!searchBar.isVisible());
                if (searchBar.isVisible()) {
                    searchText.requestFocus();
                }
                event.consume();
            }
        });
        scene.getStylesheets().add(Objects.requireNonNull(TextEditor.class.getResource("DarkTheme.css")).toExternalForm());
        primaryStage.setScene(scene);
    }

    private static CodeArea getCurrentCodeArea() {
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
                    primaryStage.setTitle(file.getName());
                    tabData.isChanged = false;
                    Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                    if (currentTab.getText().endsWith("*")) {
                        currentTab.setText(currentTab.getText().substring(0, currentTab.getText().length() - 1));
                    }
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
                    if (currentTab.getText().endsWith("*")) {
                        currentTab.setText(currentTab.getText().substring(0, currentTab.getText().length() - 1));
                    }
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
                    if (currentTab.getText().endsWith("*")) {
                        currentTab.setText(currentTab.getText().substring(0, currentTab.getText().length() - 1));
                    }
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

    public static void main(String[] args) {
        launch(args);
    }
}