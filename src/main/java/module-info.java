module com.example.gameshop {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.json;
    requires java.net.http;

    opens com.example.gameshop to javafx.fxml;
    exports com.example.gameshop;
    exports com.example.gameshop.models;
    exports com.example.gameshop.scenes;
    exports com.example.gameshop.services;
}