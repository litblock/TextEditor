module com.thelitblock.texteditor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;

    opens com.thelitblock.texteditor to javafx.fxml;
    exports com.thelitblock.texteditor;
}