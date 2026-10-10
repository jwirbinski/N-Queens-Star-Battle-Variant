package de.justgames.nqueens.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class RuleValidatorTest {
    private final RuleValidator validator = new RuleValidator();

    //------------------------------
    // Test Features
    //------------------------------

    private Board smallBoard() {
        int[][] regions = new int[][] {
            {1,3,3},
            {1,2,3},
            {2,2,2}
        };
        return new Board(regions);
    }

    private Board mediumBoard() {
        int[][] regions = new int[][] {
            {1,1,1,2,2,3},
            {1,4,2,2,3,3},
            {5,4,4,4,3,3},
            {5,5,5,5,5,3},
            {6,6,6,6,5,3},
            {6,6,6,6,6,6}
        };
        return new Board(regions);
    }

    //------------------------------
    // canPlaceQueen
    //------------------------------

    @Test 
    void canNotPlaceAQueenOnAnExistingQueen() {
        Board board = smallBoard();
        board.getCell(1, 1).setState(CellState.QUEEN);
        Cell cell = board.getCell(1, 1);
        assertFalse(validator.canPlaceQueen(board, cell));
    }

    @Test 
    void canNotPlaceTwoQueensInARow() {
        Board board = smallBoard();
        board.getCell(0, 0).setState(CellState.QUEEN);
        Cell cell = board.getCell(0, 2);
        assertFalse(validator.canPlaceQueen(board, cell));
    }

    @Test 
    void canNotPlaceTwoQueensInAColumn() {
        Board board = smallBoard();
        board.getCell(0, 0).setState(CellState.QUEEN);
        Cell cell = board.getCell(2, 0);
        assertFalse(validator.canPlaceQueen(board, cell));
    }

    @Test 
    void canNotPlaceTwoQueensInSameRegion() {
        Board board = mediumBoard();
        board.getCell(4, 0).setState(CellState.QUEEN);
        Cell cell = board.getCell(5,2);
        assertFalse(validator.canPlaceQueen(board, cell));
    }

    @Test 
    void canNotPlaceAQueenDiagonalToAnotherQueen() {
        Board board = smallBoard();
        board.getCell(0, 0).setState(CellState.QUEEN);
        Cell cell = board.getCell(1, 1);
        assertFalse(validator.canPlaceQueen(board, cell));
    }
}
