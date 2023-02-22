module com.sanchezparralabs.maze {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;

    opens com.sanchezparralabs.maze to javafx.fxml;
    exports com.sanchezparralabs.maze;
}
