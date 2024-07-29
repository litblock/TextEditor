package com.thelitblock.texteditor;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.SeparatorMenuItem;

public class MenuBarSetup {
     static MenuBar menuBar;
     static TabPane tabPane;

     public MenuBarSetup(TabPane tabPane) {
         this.tabPane = tabPane;
         menuBar = new MenuBar();
         setupMenuBar();
     }

     private void setupMenuBar() {
        Menu fileMenu = new Menu("File");
        MenuItem newItem = new MenuItem("New");
        MenuItem openItem = new MenuItem("Open");
        MenuItem openFolderItem = new MenuItem("Open Folder");
        MenuItem saveItem = new MenuItem("Save");
        MenuItem saveAsItem = new MenuItem("Save As");
        MenuItem exitItem = new MenuItem("Exit");

        fileMenu.getItems().addAll(newItem, openItem, openFolderItem, new SeparatorMenuItem(), saveItem, saveAsItem, new SeparatorMenuItem(), exitItem);

        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(new MenuItem("Cut"), new MenuItem("Copy"), new MenuItem("Paste"), new MenuItem("Select All"));

        Menu themeMenu = new Menu("Theme");
        themeMenu.getItems().addAll(new MenuItem("Dark Theme"), new MenuItem("Light Theme"));

        menuBar.getMenus().addAll(fileMenu, editMenu, themeMenu);

        MenuEventHandler menuEventHandler = new MenuEventHandler(tabPane);

        fileMenu.getItems().forEach(item -> item.setOnAction(menuEventHandler));
        editMenu.getItems().forEach(item -> item.setOnAction(menuEventHandler));
        themeMenu.getItems().forEach(item -> item.setOnAction(menuEventHandler));

        openFolderItem.setOnAction(event -> TextEditor.openFolder());
    }

    public static MenuBar getMenuBar() {
        return menuBar;
    }
}