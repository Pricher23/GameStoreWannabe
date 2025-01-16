module com.example.gameshop {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens com.example.gameshop to javafx.fxml;
    opens com.example.gameshop.models to javafx.base;
    exports com.example.gameshop;
    exports com.example.gameshop.scenes;
    exports com.example.gameshop.models;
}