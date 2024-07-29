package com.thelitblock.texteditor;

import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.util.Objects;

public class FolderSetup {
    private TreeView<String> folderTreeView;
    private TreeItem<String> rootItem;
    private static final String FOLDER_ICON = "\uD83D\uDCC1"; // 📁
    private static final String FILE_ICON = "\uD83D\uDCC4"; // 📄
    private static final String JAVA_ICON = "☕";
    private static final String PYTHON_ICON = "\uD83D\uDC0D";
    private static final String JS_ICON = "JS";
    private static final String CPP_ICON = "C++";
    private static final String C_ICON = "C";
    private static final String HTML_ICON = "\uD83C\uDF10";
    private static final String CSS_ICON = "\uD83C\uDF08";
    private static final String JSON_ICON = "{}";
    private static final String XML_ICON = "</>";

    public FolderSetup(HBox searchBar, TreeView<String> folderTreeView, TreeItem<String> rootItem) {
        this.folderTreeView = folderTreeView;
        this.rootItem = rootItem;
        folderTreeView.setShowRoot(false);

        Button openDirButton = new Button("Open Folder");
        openDirButton.setOnAction(e -> openFolder());
        searchBar.getChildren().add(openDirButton);

        setupTreeViewCellFactory();
    }

    public TreeView<String> getFolderTreeView() {
        return folderTreeView;
    }

    private void setupTreeViewCellFactory() {
        folderTreeView.setCellFactory(tv -> new TreeCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                }
                else {
                    TreeItem<String> treeItem = getTreeItem();
                    if (treeItem != null && treeItem.getGraphic() != null) {
                        File file = (File) treeItem.getGraphic().getUserData();
                        if (file.isDirectory()) {
                            setText(FOLDER_ICON + " " + item);
                            setStyle("-fx-text-fill: #c678dd;");
                        }
                        else {
                            String icon = getFileIcon(file.getName());
                            setText(icon + " " + item);
                            setStyle("-fx-text-fill: #61afef;");
                        }
                    }
                    else {
                        setText(item);
                        setStyle("");
                    }
                }
            }
        });
    }

    private String getFileIcon(String fileName) {
        String extension = getFileExtension(fileName);
        switch (extension.toLowerCase()) {
            case "java":
                return JAVA_ICON;
            case "py":
                return PYTHON_ICON;
            case "js":
                return JS_ICON;
            case "cpp":
                return CPP_ICON;
            case "c":
                return C_ICON;
            case "html":
                return HTML_ICON;
            case "css":
                return CSS_ICON;
            case "json":
                return JSON_ICON;
            case "xml":
                return XML_ICON;
            default:
                return FILE_ICON;
        }
    }

    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    public void setupFolderTreeView() {
        folderTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                TreeItem<String> selectedItem = newValue;
                File file = (File) selectedItem.getGraphic().getUserData();
                if (file.isFile()) {
                    TextEditor.openFileInNewTab(file);
                }
            }
        });
    }

    public void createTree(File directory, TreeItem<String> parent) {
        for (File file : Objects.requireNonNull(directory.listFiles())) {
            TreeItem<String> item = new TreeItem<>(file.getName());
            parent.getChildren().add(item);

            Label label = new Label(file.getName());
            label.setUserData(file);
            item.setGraphic(label);

            if (file.isDirectory()) {
                createTree(file, item);
            }
        }
    }

    private void openFolder() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File selectedDirectory = directoryChooser.showDialog(TextEditor.getPrimaryStage());

        if (selectedDirectory != null) {
            rootItem.getChildren().clear();
            Label rootLabel = new Label(selectedDirectory.getName());
            rootLabel.setUserData(selectedDirectory);
            rootItem.setGraphic(rootLabel);
            createTree(selectedDirectory, rootItem);
            folderTreeView.setRoot(rootItem);
        }
    }
}