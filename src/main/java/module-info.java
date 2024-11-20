module org.sortingalgorithms {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;


    opens org.sortingalgorithms to javafx.fxml;
    exports org.sortingalgorithms;
}