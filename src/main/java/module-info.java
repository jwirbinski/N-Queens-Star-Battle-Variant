module de.justgames.nqueens {
    requires javafx.controls;
    requires javafx.fxml;

    exports de.justgames.nqueens;
    opens de.justgames.nqueens to javafx.fxml;
}
