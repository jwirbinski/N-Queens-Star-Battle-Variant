package de.justgames.nqueens.view;

import de.justgames.nqueens.controller.GameController;
import de.justgames.nqueens.model.Board;
import de.justgames.nqueens.model.Cell;
import de.justgames.nqueens.model.CellState;
import de.justgames.nqueens.model.Difficulty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class GameView extends BorderPane {
    
    private final Board board;
    private final GameController controller;
    private final GridPane boardGrid;
    private boolean gameLocked = false;
    private final StackPane[][] cellPanes;
    private final Text[][] cellTexts;

    public interface NewGameListener {
        void onNewGame(Difficulty difficulty, int size);
    }

    public GameView(Board board, GameController controller, Difficulty currentDifficulty, NewGameListener listener) {
        this.board = board;
        this.controller = controller;
        
        HBox hud = new HBox(15);
        hud.setPadding(new Insets(10));
        hud.setAlignment(Pos.CENTER);

        Label sizeLabel = new Label("Size:");
        ComboBox<Integer> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(5, 6, 7, 8, 9, 10, 12);
        sizeBox.setValue(board.getSize());

        Label diffLabel = new Label("Difficulty:");
        ComboBox<Difficulty> diffBox = new ComboBox<>();
        diffBox.getItems().addAll(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
        diffBox.setValue(currentDifficulty);

        Button newGameBtn = new Button("New Game");
        newGameBtn.setOnAction(e -> listener.onNewGame(diffBox.getValue(), sizeBox.getValue()));

        sizeBox.valueProperty().addListener((obs, oldSize, newSize) -> {
            diffBox.getItems().clear();
            if (newSize <= 6) {
                diffBox.getItems().addAll(Difficulty.EASY, Difficulty.MEDIUM);
            } else if (newSize >= 9) {
                diffBox.getItems().addAll(Difficulty.MEDIUM, Difficulty.HARD);
            } else {
                diffBox.getItems().addAll(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD);
            }
            if (!diffBox.getItems().contains(diffBox.getValue())) {
                diffBox.setValue(diffBox.getItems().get(0));
            }
        });

        hud.getChildren().addAll(sizeLabel, sizeBox, diffLabel, diffBox, newGameBtn);
        this.setTop(hud);

        boardGrid = new GridPane();
        boardGrid.setHgap(0);
        boardGrid.setVgap(0);
        boardGrid.setAlignment(Pos.CENTER);
        this.setCenter(boardGrid);

        int size = board.getSize();
        cellPanes = new StackPane[size][size];
        cellTexts = new Text[size][size];

        createBoard();
        updateView();

        this.setFocusTraversable(true);
        this.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.Z) {
                if (gameLocked) return;
                controller.undoLastMove();
                updateView();
            }
        });
        
        this.setOnMouseClicked(e -> this.requestFocus());
    }

    private void createBoard() {
        int size = board.getSize();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                StackPane pane = new StackPane();
                pane.setPrefSize(50, 50);

                Text text = new Text();
                text.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

                pane.getChildren().add(text);

                final int r = row;
                final int c = col;

                pane.setOnMouseClicked(event -> {
                    if (gameLocked) return;

                    this.requestFocus();

                    if (event.getButton() == MouseButton.PRIMARY) {
                        controller.onLeftClick(r, c);
                    } else if (event.getButton() == MouseButton.SECONDARY) {
                        controller.onRightClick(r, c);
                    }

                    updateView();
                    checkWinCondition();
                });

                pane.setOnDragDetected(event -> {
                    if (gameLocked) return;

                    if (event.getButton() == MouseButton.SECONDARY) {
                        pane.startFullDrag();
                    }
                });

                pane.setOnMouseDragEntered(event -> {
                    if (gameLocked) return;

                    controller.onRightClick(r, c);
                    updateView();
                    checkWinCondition();
                });

                cellPanes[row][col] = pane;
                cellTexts[row][col] = text;

                boardGrid.add(pane, col, row);
            }
        }
    }

    private void updateView() {
        int size = board.getSize();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Cell cell = board.getCell(row, col);
                int currentId = cell.getRegion().getId();

                StackPane pane = cellPanes[row][col];
                Text text = cellTexts[row][col];

                Color regionColor = GameColors.getColorForRegion(currentId);

                if (controller.isConflict(row, col)) {
                    regionColor = Color.web("#FF6666");
                }

                boolean borderTop = row == 0
                        || board.getCell(row - 1, col).getRegion().getId() != currentId;

                boolean borderBottom = row == size - 1
                        || board.getCell(row + 1, col).getRegion().getId() != currentId;

                boolean borderLeft = col == 0
                        || board.getCell(row, col - 1).getRegion().getId() != currentId;

                boolean borderRight = col == size - 1
                        || board.getCell(row, col + 1).getRegion().getId() != currentId;

                double topWidth = borderTop ? 3.0 : 0.5;
                double bottomWidth = borderBottom ? 3.0 : 0.5;
                double leftWidth = borderLeft ? 3.0 : 0.5;
                double rightWidth = borderRight ? 3.0 : 0.5;

                pane.setStyle(String.format(
                        "-fx-background-color: %s; "
                        + "-fx-border-color: black; "
                        + "-fx-border-width: %.1fpx %.1fpx %.1fpx %.1fpx;",
                        toHexString(regionColor),
                        topWidth, rightWidth, bottomWidth, leftWidth
                ));

                if (cell.getState() == CellState.QUEEN) {
                    text.setText("Q");
                    text.setFill(Color.BLACK);
                } else if (cell.getState() == CellState.CROSSED) {
                    text.setText("X");
                    text.setFill(Color.BLACK);
                } else {
                    text.setText("");
                }
            }
        }
    }

    private void checkWinCondition() {
        if (controller.isGameOver() && !gameLocked) {
            gameLocked = true;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Level Completed");
            alert.setHeaderText(null);
            alert.setContentText("Congratulations! You solved the puzzle without errors!");
            alert.showAndWait();
        }
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}