package com.thelitblock.texteditor;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.fxmisc.richtext.CodeArea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.thelitblock.texteditor.SyntaxHighlighting.computeHighlighting;

public class SearchBarSetup {
    private HBox searchBar;
    private TextField searchText;
    private Label searchResultCount;
    private TabPane tabPane;
    private int currentSearchIndex = -1;
    private List<Integer> searchIndices = new ArrayList<>();

    public SearchBarSetup(HBox searchBar, TextField searchText, Label searchResultCount, TabPane tabPane) {
        this.searchBar = searchBar;
        this.searchText = searchText;
        this.searchResultCount = searchResultCount;
        this.tabPane = tabPane;
        setupSearchBar();
    }

    private void setupSearchBar() {
        searchText = new TextField();
        searchText.setPromptText("Search");

        TextField replaceText = new TextField();
        replaceText.setPromptText("Replace");

        Button prevButton = new Button("Prev");
        Button nextButton = new Button("Next");
        Button replaceButton = new Button("Replace");
        Button replaceAllButton = new Button("Replace All");

        searchResultCount = new Label("0/0");

        searchText.textProperty().addListener((obs, oldText, newText) -> updateSearchResults(newText));

        prevButton.setOnAction(event -> navigateSearchResults(-1));
        nextButton.setOnAction(event -> navigateSearchResults(1));
        replaceButton.setOnAction(event -> replaceCurrentOccurrence(replaceText.getText()));
        replaceAllButton.setOnAction(event -> replaceAllOccurrences(replaceText.getText()));

        searchBar.getChildren().addAll(searchText, replaceText, replaceButton, replaceAllButton, prevButton, nextButton, searchResultCount);
        searchBar.setSpacing(5);
        searchBar.setAlignment(Pos.CENTER_LEFT);
        searchBar.setPadding(new Insets(5));
        searchBar.setManaged(false);
        searchBar.setVisible(false);

        searchBar.setId("searchBar");
        searchResultCount.setId("searchResultCount");
    }

    private void replaceCurrentOccurrence(String replacement) {
        if (searchIndices.isEmpty() || currentSearchIndex == -1) {
            return;
        }

        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            CodeArea codeArea = ((TabData) currentTab.getUserData()).codeArea;
            int startPos = searchIndices.get(currentSearchIndex);
            int endPos = startPos + searchText.getText().length();
            codeArea.replaceText(startPos, endPos, replacement);

            updateSearchResults(searchText.getText());
            navigateSearchResults(1);
        }
    }

    private void replaceAllOccurrences(String replacement) {
        if (searchIndices.isEmpty()) {
            return;
        }

        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            CodeArea codeArea = ((TabData) currentTab.getUserData()).codeArea;
            String searchTextStr = searchText.getText();
            String text = codeArea.getText();
            text = text.replaceAll(java.util.regex.Pattern.quote(searchTextStr), replacement);
            codeArea.replaceText(0, codeArea.getLength(), text);

            updateSearchResults(searchTextStr);
            navigateSearchResults(1);
        }
    }

    void updateSearchResults(String query) {
        searchIndices.clear();
        currentSearchIndex = -1;

        if (query.isEmpty()) {
            updateSearchResultCount();
            clearHighlights();
            return;
        }

        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            CodeArea codeArea = ((TabData) currentTab.getUserData()).codeArea;
            String text = codeArea.getText();
            int index = text.indexOf(query, 0);
            while (index != -1) {
                searchIndices.add(index);
                index = text.indexOf(query, index + query.length());
            }

            highlightSearchResults(query);
        }

        navigateSearchResults(1);
    }

    void showSearchBar() {
        if (!searchBar.isVisible()) {
            searchBar.setManaged(true);
            searchBar.setVisible(true);
            BorderPane root = (BorderPane) tabPane.getScene().getRoot().lookup("#rootLayout");
            if (root != null) {
                VBox topContainer = new VBox(root.getTop(), searchBar);
                root.setTop(topContainer);
            }
            else {
                System.err.println("Root layout not found");
            }
            searchText.requestFocus();
        }
    }

    void hideSearchBar() {
        if (searchBar.isVisible()) {
            searchBar.setManaged(false);
            searchBar.setVisible(false);
            BorderPane root = (BorderPane) tabPane.getScene().getRoot().lookup("#rootLayout");
            if (root != null) {
                VBox topContainer = (VBox) root.getTop();
                if (topContainer != null && topContainer.getChildren().size() > 1) {
                    root.setTop(topContainer.getChildren().getFirst());
                }
            }
        }
    }

    private void highlightSearchResults(String query) {
        CodeArea codeArea = TextEditor.getCurrentCodeArea();
        if (codeArea == null) return;

        codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));

        if (query == null || query.isEmpty()) return;

        String text = codeArea.getText();
        int index = 0;
        while ((index = text.indexOf(query, index)) >= 0) {
            int endIndex = index + query.length();
            codeArea.setStyle(index, endIndex, Collections.singleton("search-highlight"));
            index = endIndex;
        }
    }

    private void clearHighlights() {
        CodeArea codeArea = TextEditor.getCurrentCodeArea();
        if (codeArea != null) {
            codeArea.setStyleSpans(0, computeHighlighting(codeArea.getText()));
        }
    }

    void navigateSearchResults(int direction) {
        if (searchIndices.isEmpty()) {
            return;
        }

        currentSearchIndex += direction;

        if (currentSearchIndex < 0) {
            currentSearchIndex = searchIndices.size() - 1;
        }
        else if (currentSearchIndex >= searchIndices.size()) {
            currentSearchIndex = 0;
        }

        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab != null) {
            CodeArea codeArea = ((TabData) currentTab.getUserData()).codeArea;
            int pos = searchIndices.get(currentSearchIndex);
            codeArea.selectRange(pos, pos + searchText.getText().length());
        }

        updateSearchResultCount();
    }

    private void updateSearchResultCount() {
        if (searchIndices.isEmpty()) {
            searchResultCount.setText("0/0");
        }
        else {
            searchResultCount.setText((currentSearchIndex + 1) + "/" + searchIndices.size());
        }
    }

    String getSearchText() {
        return searchText.getText();
    }
}
