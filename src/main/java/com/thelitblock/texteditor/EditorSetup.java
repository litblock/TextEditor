package com.thelitblock.texteditor;

import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import java.util.*;

import static com.thelitblock.texteditor.SyntaxHighlighting.computeHighlighting;

public class EditorSetup {
    private MenuBar menuBar;
    private HBox searchBar;
    private static TabPane tabPane;
    private TerminalSetup terminalSetup;
    private static Set<Integer> untitledNumbers = new HashSet<>();
    private static int untitledCounter = 1;
    private static SearchBarSetup searchBarSetup;

    public EditorSetup(MenuBar menuBar, HBox searchBar, TabPane tabPane, TerminalSetup terminalSetup, SearchBarSetup searchBarSetup) {
        this.menuBar = menuBar;
        this.searchBar = searchBar;
        EditorSetup.tabPane = tabPane;
        this.terminalSetup = terminalSetup;
        EditorSetup.searchBarSetup = searchBarSetup;
        setupEditor();
    }

    private void setupEditor() {
        Tab defaultTab = createNewTab("Untitled");
        tabPane.getTabs().add(defaultTab);
        untitledNumbers.add(untitledCounter++);

        Tab plusTab = new Tab("+");
        plusTab.setClosable(false);
        tabPane.getTabs().add(plusTab);

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab == plusTab) {
                String title = getNextUntitledName();
                Tab newTabToAdd = createNewTab(title);
                tabPane.getTabs().add(tabPane.getTabs().size() - 1, newTabToAdd);
                tabPane.getSelectionModel().select(newTabToAdd);
            }
        });
    }

    public VBox createEditorLayout() {
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(tabPane, terminalSetup.createTerminalPane());

        VBox editorBox = new VBox(menuBar, searchBar, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        return editorBox;
    }

    static Tab createNewTab(String title) {
        CodeArea codeArea = new CodeArea();
        codeArea.setId("codeArea");
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        Font menloFont = Font.loadFont(Objects.requireNonNull(TextEditor.class.getResourceAsStream("Menlo-Regular.woff")), 12);
        codeArea.setStyle("-fx-font-family: 'Menlo'; -fx-font-size: 10pt");

        codeArea.textProperty().addListener((observable, oldValue, newValue) -> {
            searchBarSetup.updateSearchResults(searchBarSetup.getSearchText());
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

        return getTab(codeArea, title);
    }

    private static Tab getTab(CodeArea codeArea, String title) {
        VirtualizedScrollPane<CodeArea> virtualizedScrollPane = new VirtualizedScrollPane<>(codeArea);
        Tab tab = new Tab(title, virtualizedScrollPane);
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
                        TextEditor.saveFile();
                    }
                    else if (result.get() == btnCancel) {
                        event.consume();
                        return;
                    }
                }
            }
            String tabText = tab.getText().replace("*", "");
            if (tabText.startsWith("Untitled ")) {
                int number = Integer.parseInt(tabText.substring(9));
                untitledNumbers.add(number);
            }
            else if (tabText.equals("Untitled")) {
                untitledNumbers.add(0);
            }
        });

        return tab;
    }

    static String getNextUntitledName() {
        if (!untitledNumbers.isEmpty()) {
            int nextNumber = Collections.min(untitledNumbers);
            untitledNumbers.remove(nextNumber);
            if (nextNumber == 0) {
                return "Untitled";
            }
            return "Untitled " + nextNumber;
        }
        else {
            return "Untitled " + untitledCounter++;
        }
    }
}
