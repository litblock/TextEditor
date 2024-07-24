module com.thelitblock.texteditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires reactfx;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires org.fxmisc.richtext;

    opens com.thelitblock.texteditor to javafx.fxml;
    exports com.thelitblock.texteditor;
}