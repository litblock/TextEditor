package com.thelitblock.texteditor;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Objects;

public class FolderSetup {
    private TreeView<String> folderTreeView;
    private TreeItem<String> rootItem;

    public FolderSetup(HBox searchBar, TreeView<String> folderTreeView, TreeItem<String> rootItem) {
        this.folderTreeView = folderTreeView;
        this.rootItem = rootItem;
        folderTreeView.setShowRoot(false);

        Button openDirButton = new Button("Open Folder");
        openDirButton.setOnAction(e -> openFolder());
        searchBar.getChildren().add(openDirButton);
    }

    public TreeView<String> getFolderTreeView() {
        return folderTreeView;
    }

    public void setupFolderTreeView() {
        folderTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TreeItem<String> selectedItem = newValue;
                File file = (File) selectedItem.getGraphic().getUserData();
                if (file.isFile()) {
                    TextEditor.openFile(file);
                }
            }
        });
    }

    public void createTree(File directory, TreeItem<String> parent) {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            TreeItem<String> item = new TreeItem<>(file.getName());
            parent.getChildren().add(item);

            if (file.isDirectory()) {
                createTree(file, item);
            }
            else {
                item.setGraphic(new Label(file.getName()));
                item.getGraphic().setUserData(file);
            }
        }
    }

    private void openFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(TextEditor.getPrimaryStage());

        if (selectedDirectory != null) {
            rootItem.getChildren().clear();
            createTree(selectedDirectory, rootItem);
        }
    }
}
