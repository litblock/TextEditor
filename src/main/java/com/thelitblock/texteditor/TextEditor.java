package com.thelitblock.texteditor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.io.*;

public class TextEditor extends Application {
    public static CodeArea codeArea;
    static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        TextEditor.primaryStage = primaryStage;
        codeArea = new CodeArea();
        primaryStage.setTitle("Text Editor");

        VBox vBox = new VBox();
        MenuBar menuBar = new MenuBar();

        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(new MenuItem("New"), new MenuItem("Open"), new MenuItem("Save"), new MenuItem("Save As"), new MenuItem("Exit"));
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(new MenuItem("Cut"), new MenuItem("Copy"), new MenuItem("Paste"), new MenuItem("Select All"));

        fileMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler()));
        editMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler()));

        menuBar.getMenus().addAll(fileMenu, editMenu);
        vBox.getChildren().addAll(menuBar, codeArea);
        VBox.setVgrow(codeArea, javafx.scene.layout.Priority.ALWAYS);

        Scene scene = new Scene(vBox, 800, 600);
        scene.getStylesheets().add("TextEditor.css");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void displayFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        File file = fileChooser.showOpenDialog(new Stage());

        if (file != null) {
            System.out.println("File selected");
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                codeArea.clear();
                String line;
                while ((line = reader.readLine()) != null) {
                    codeArea.appendText(line + "\n");
                    primaryStage.setTitle(file.getName());
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

    public static void saveFile() {
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

    public static void main(String[] args) {
        launch(args);
    }
}