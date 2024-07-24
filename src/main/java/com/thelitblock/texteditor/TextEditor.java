package com.thelitblock.texteditor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import javafx.scene.text.Font;
import javafx.geometry.Insets;

import java.io.*;
import java.util.Objects;

import static com.thelitblock.texteditor.SyntaxHighlighting.computeHighlighting;

public class TextEditor extends Application {
    public static CodeArea codeArea;
    static Stage primaryStage;
    static File currentFile = null;
    static boolean isChanged = false;
    static Scene scene;

    @Override
    public void start(Stage primaryStage) {
        TextEditor.primaryStage = primaryStage;
        codeArea = new CodeArea();
        codeArea.setId("codeArea");

        VirtualizedScrollPane<CodeArea> virtualizedScrollPane = new VirtualizedScrollPane<>(codeArea);

        primaryStage.setTitle("Text Editor");
        Font menloFont = Font.loadFont(Objects.requireNonNull(getClass().getResourceAsStream("Menlo-Regular.woff")), 12);
        codeArea.setStyle("-fx-font-family: 'Menlo'; -fx-font-size: 10pt");
        //codeArea.setPadding(new Insets(0, 20, 0, 0));

        VBox vBox = new VBox();
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(new MenuItem("New"), new MenuItem("Open"), new MenuItem("Save"), new MenuItem("Save As"), new MenuItem("Exit"));
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(new MenuItem("Cut"), new MenuItem("Copy"), new MenuItem("Paste"), new MenuItem("Select All"));
        Menu themeMenu = new Menu("Theme");
        themeMenu.getItems().addAll(new MenuItem("Dark Theme"), new MenuItem("Light Theme"));

        fileMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler()));
        editMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler()));
        themeMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler()));

        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));
        codeArea.textProperty().addListener((observable, oldValue, newValue) -> isChanged = true);
        codeArea.richChanges()
            .filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
            .subscribe(change -> {
               codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
        });
        codeArea.getStyleClass().add("codeArea");

        menuBar.getMenus().addAll(fileMenu, editMenu, themeMenu);
        vBox.getChildren().addAll(menuBar, virtualizedScrollPane);
        VBox.setVgrow(virtualizedScrollPane, javafx.scene.layout.Priority.ALWAYS);

        scene = new Scene(vBox, 800, 600);

        scene.getStylesheets().add(Objects.requireNonNull(TextEditor.class.getResource("DarkTheme.css")).toExternalForm());

        primaryStage.setScene(scene);
        primaryStage.show();
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