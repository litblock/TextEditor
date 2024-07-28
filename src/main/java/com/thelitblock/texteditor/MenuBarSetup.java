package com.thelitblock.texteditor;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;

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
        fileMenu.getItems().addAll(new MenuItem("New"), new MenuItem("Open"), new MenuItem("Save"), new MenuItem("Save As"), new MenuItem("Exit"));
        Menu editMenu = new Menu("Edit");
        editMenu.getItems().addAll(new MenuItem("Cut"), new MenuItem("Copy"), new MenuItem("Paste"), new MenuItem("Select All"));
        Menu themeMenu = new Menu("Theme");
        themeMenu.getItems().addAll(new MenuItem("Dark Theme"), new MenuItem("Light Theme"));

        menuBar.getMenus().addAll(fileMenu, editMenu, themeMenu);

        fileMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler(tabPane)));
        editMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler(tabPane)));
        themeMenu.getItems().forEach(item -> item.setOnAction(new MenuEventHandler(tabPane)));
    }
    public static MenuBar getMenuBar() {
        return menuBar;
    }
}