package com.thelitblock.texteditor;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;

import java.util.Optional;

import static com.thelitblock.texteditor.TextEditor.isChanged;

public class MenuEventHandler implements EventHandler<ActionEvent> {
    public void handle(ActionEvent e) {
        MenuItem mItem = (MenuItem) e.getSource();
        if ("New".equals(mItem.getText()) || "Open".equals(mItem.getText()) || "Exit".equals(mItem.getText())) {
            if (isChanged) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Unsaved Changes");
                alert.setHeaderText("You have unsaved changes.");
                alert.setContentText("Do you want to save before continuing?");

                ButtonType btnSave = new ButtonType("Save");
                ButtonType btnCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

                alert.getButtonTypes().setAll(btnSave, btnCancel);

                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == btnSave) {
                    TextEditor.saveFile();
                }
            }
            if ("Exit".equals(mItem.getText())) {
                System.exit(0);
            }
        }

        if ("New".equals(mItem.getText())) {
            TextEditor.codeArea.clear();
            TextEditor.primaryStage.setTitle("Untitled");
            isChanged = false;
        }
        else if ("Open".equals(mItem.getText())) {
            TextEditor.displayFile();
            isChanged = false;
        }
        else if ("Save".equals(mItem.getText())) {
            TextEditor.saveFile();
            isChanged = false;
        }
        else if ("Save as".equals(mItem.getText())) {
            TextEditor.saveAsFile();
            isChanged = false;
        }
        else if ("Cut".equals(mItem.getText())) {
            TextEditor.cut();
        }
        else if ("Copy".equals(mItem.getText())) {
            TextEditor.copy();
        }
        else if ("Paste".equals(mItem.getText())) {
            TextEditor.paste();
        }
        else if ("Select All".equals(mItem.getText())) {
            TextEditor.selectAll();
        }
        else if ("Dark Theme".equals(mItem.getText())) {
            TextEditor.changeDarkTheme();
        }
        else if ("Light Theme".equals(mItem.getText())) {
            TextEditor.changeLightTheme();
        }
    }
}