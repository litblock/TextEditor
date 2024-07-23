package com.thelitblock.texteditor;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;

public class MenuEventHandler implements EventHandler<ActionEvent> {
    public void handle(ActionEvent e) {
        MenuItem mItem = (MenuItem)e.getSource();
        if (mItem.getText().equals("Open")) {
            TextEditor.displayFile();
        }
    }
}
