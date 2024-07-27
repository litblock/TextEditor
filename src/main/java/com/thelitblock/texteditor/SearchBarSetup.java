package com.thelitblock.texteditor;

import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;

public class SearchBarSetup {
    public static void setupSearchBar(VBox rootLayout) {
        TextField searchField = new TextField();
        searchField.setPromptText("Search...");

        Button nextButton = new Button("Next");
        Button prevButton = new Button("Prev");

        nextButton.setOnAction(event -> searchText(searchField.getText(), true));
        prevButton.setOnAction(event -> searchText(searchField.getText(), false));

        searchField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                searchText(searchField.getText(), true);
            }
        });

        HBox searchBar = new HBox(5);
        searchBar.getChildren().addAll(searchField, prevButton, nextButton);
        rootLayout.getChildren().add(0, searchBar);
    }

    private static void searchText(String searchText, boolean forward) {
        Tab currentTab = TextEditor.tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null && currentTab.getUserData() instanceof TabData) {
            CodeArea codeArea = ((TabData) currentTab.getUserData()).codeArea;
            if (forward) {
                codeArea.requestFollowCaret();
                int start = codeArea.getCaretPosition();
                int end = codeArea.getText().indexOf(searchText, start);
                if (end == -1) {
                    end = codeArea.getText().indexOf(searchText);
                }
                if (end != -1) {
                    codeArea.selectRange(end, end + searchText.length());
                }
            }
            else {
                int start = codeArea.getCaretPosition();
                int end = codeArea.getText().lastIndexOf(searchText, start);
                if (end == -1) {
                    end = codeArea.getText().lastIndexOf(searchText);
                }
                if (end != -1) {
                    codeArea.selectRange(end, end + searchText.length());
                }
            }
        }
    }
}
