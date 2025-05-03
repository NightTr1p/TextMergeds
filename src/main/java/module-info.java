module your.module.name {
    requires javafx.controls;
    requires javafx.fxml;

    opens org to javafx.fxml;
    exports org;
}