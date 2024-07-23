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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class TextEditor extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Text Editor");

        VBox vBox = new VBox();
        TextArea textArea = new TextArea();
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(new MenuItem("New"), new MenuItem("Open"), new MenuItem("Save"), new MenuItem("Save As"), new MenuItem("Exit"));
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(new MenuItem("Cut"), new MenuItem("Copy"), new MenuItem("Paste"), new MenuItem("Select All"));

        fileMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler()));
        editMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler()));

        menuBar.getMenus().addAll(fileMenu, editMenu);
        vBox.getChildren().addAll(menuBar, textArea);

        Scene scene = new Scene(vBox, 800, 600);
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
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println(line);
                    //implement text area to display
                }
            }
            catch (IOException e) {
                e.printStackTrace();
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