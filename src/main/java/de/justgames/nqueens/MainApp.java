package de.justgames.nqueens;

import de.justgames.nqueens.controller.GameController;
import de.justgames.nqueens.model.Board;
import de.justgames.nqueens.model.Difficulty;
import de.justgames.nqueens.model.LevelGenerator;
import de.justgames.nqueens.view.GameView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {
        loadNewGame(stage, Difficulty.EASY, 8);
        stage.setTitle("N-Queens Game");
        stage.show();
    }

    private void loadNewGame(Stage stage, Difficulty difficulty, int size) {
        Label loadingLabel = new Label("Generating " + size + "x" + size + " puzzle...\nPlease wait.");
        loadingLabel.setFont(Font.font("Arial", 20));
        StackPane loadingRoot = new StackPane(loadingLabel);
        stage.setScene(new Scene(loadingRoot, 700, 700));

        new Thread(() -> {
            LevelGenerator lvlGen = new LevelGenerator(size);
            Board board = lvlGen.generateLevel(difficulty);
            GameController controller = new GameController(board);

            Platform.runLater(() -> {
                GameView view = new GameView(board, controller, difficulty, (selectedDifficulty, selectedSize) -> {
                    loadNewGame(stage, selectedDifficulty, selectedSize);
                });
                
                stage.setScene(new Scene(view, 700, 700));
            });
            
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}