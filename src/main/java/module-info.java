module com.mechaduel {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mechaduel to javafx.fxml;
    opens com.mechaduel.view to javafx.fxml;

    exports com.mechaduel;
    exports com.mechaduel.view;
    exports com.mechaduel.controller;
    exports com.mechaduel.model;
    exports com.mechaduel.network;
    exports com.mechaduel.exception;
}
