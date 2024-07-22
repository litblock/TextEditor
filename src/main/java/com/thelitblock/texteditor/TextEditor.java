package com.thelitblock.texteditor;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class TextEditor extends Application {
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Text Editor");

        VBox vBox = new VBox();
        TextArea textArea = new TextArea();
        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        menuBar.getMenus().add(fileMenu);

        vBox.getChildren().addAll(menuBar, textArea);

        Scene scene = new Scene(vBox, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}