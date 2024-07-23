package com.thelitblock.texteditor;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

public class MenuEventHandler implements EventHandler<ActionEvent> {
    public void handle(ActionEvent e) {
        MenuItem mItem = (MenuItem)e.getSource();
        if (mItem.getText().equals("New")) {
            TextEditor.codeArea.clear();
            TextEditor.primaryStage.setTitle("Untitled");
        }
        else if (mItem.getText().equals("Open")) {
            TextEditor.displayFile();
        }
        else if (mItem.getText().equals("Save")) {
            TextEditor.saveFile();
        }
        else if (mItem.getText().equals("Save as")) {
            TextEditor.saveAsFile();
        }
    }
}


