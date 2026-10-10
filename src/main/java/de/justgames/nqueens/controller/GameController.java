package de.justgames.nqueens.controller;

import de.justgames.nqueens.model.Board;
import de.justgames.nqueens.model.CellState;
import de.justgames.nqueens.model.Cell;
import de.justgames.nqueens.model.RuleValidator;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class GameController {
    private static class CellChange {
        int row;
        int col;
        CellState previousState;

        CellChange(int row, int col, CellState previousState) {
            this.row = row;
            this.col = col;
            this.previousState = previousState;
        }
    }

    private final RuleValidator validator;
    private final Board gameState; 
    private int queensPlaced;
    private final Stack<List<CellChange>> history; 

    public GameController(Board gameState) {
        this.gameState = gameState;
        this.validator = new RuleValidator();
        this.queensPlaced = 0;
        this.history = new Stack<>();
    }

    public void undoLastMove() {
        if (history.isEmpty()) return;

        List<CellChange> lastAction = history.pop();
        
        for (CellChange change : lastAction) {
            Cell cell = gameState.getCell(change.row, change.col);
            
            if (cell.getState() == CellState.QUEEN && change.previousState != CellState.QUEEN) {
                queensPlaced--;
            } else if (cell.getState() != CellState.QUEEN && change.previousState == CellState.QUEEN) {
                queensPlaced++;
            }
            
            cell.setState(change.previousState);
        }
    }

    public void onLeftClick(int row, int col) {
        Cell cell = gameState.getCell(row, col);
        List<CellChange> currentAction = new ArrayList<>();
        currentAction.add(new CellChange(row, col, cell.getState()));
        
        if (cell.getState() == CellState.EMPTY || cell.getState() == CellState.CROSSED) {
            cell.setState(CellState.QUEEN);
            queensPlaced++;
            autoCrossAffectedCells(row, col, currentAction);
        } else if (cell.getState() == CellState.QUEEN) {
            cell.setState(CellState.EMPTY);
            queensPlaced--;
            autoRemoveCrosses(row, col, currentAction);
        }
        
        history.push(currentAction);
    }

    public void onRightClick(int row, int col) {
        Cell cell = gameState.getCell(row, col);
        
        if (cell.getState() == CellState.CROSSED) {
            List<CellChange> currentAction = new ArrayList<>();
            currentAction.add(new CellChange(row, col, cell.getState()));
            cell.setState(CellState.EMPTY);
            history.push(currentAction);
            return;
        }

        List<CellChange> currentAction = new ArrayList<>();
        currentAction.add(new CellChange(row, col, cell.getState()));
        
        if (cell.getState() == CellState.EMPTY) {
            cell.setState(CellState.CROSSED);
        } else if (cell.getState() == CellState.QUEEN) {
            cell.setState(CellState.CROSSED);
            queensPlaced--;
            autoRemoveCrosses(row, col, currentAction);
        }
        
        history.push(currentAction);
    }

    private void autoCrossAffectedCells(int row, int col, List<CellChange> currentAction) {
        int n = gameState.getSize();

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (isRelated(row, col, r, c)) {
                    Cell targetCell = gameState.getCell(r, c);
                    
                    if (targetCell.getState() == CellState.EMPTY) {
                        currentAction.add(new CellChange(r, c, targetCell.getState()));
                        targetCell.setState(CellState.CROSSED);
                    }
                }
            }
        }
    }

    private void autoRemoveCrosses(int row, int col, List<CellChange> currentAction) {
        int n = gameState.getSize();

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (isRelated(row, col, r, c)) {
                    Cell targetCell = gameState.getCell(r, c);
                    
                    if (targetCell.getState() == CellState.CROSSED && !isAffectedByAnyQueen(r, c)) {
                        currentAction.add(new CellChange(r, c, targetCell.getState()));
                        targetCell.setState(CellState.EMPTY);
                    }
                }
            }
        }
    }

    private boolean isAffectedByAnyQueen(int r, int c) {
        int n = gameState.getSize();
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (gameState.getCell(i, j).getState() == CellState.QUEEN && isRelated(r, c, i, j)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isRelated(int r1, int c1, int r2, int c2) {
        if (r1 == r2 && c1 == c2) return false;
        
        boolean sameRow = (r1 == r2);
        boolean sameCol = (c1 == c2);
        boolean sameRegion = (gameState.getCell(r1, c1).getRegion().getId() == gameState.getCell(r2, c2).getRegion().getId());
        boolean isTouching = (Math.abs(r1 - r2) <= 1 && Math.abs(c1 - c2) <= 1);
        
        return sameRow || sameCol || sameRegion || isTouching;
    }

    public boolean isConflict(int row, int col) {
        Cell cell = gameState.getCell(row, col);
        if (cell.getState() != CellState.QUEEN) return false;

        cell.setState(CellState.EMPTY);
        boolean isValid = validator.canPlaceQueen(gameState, cell);
        cell.setState(CellState.QUEEN);

        return !isValid; 
    }

    public boolean isGameOver() {
        int size = gameState.getSize();
        if (queensPlaced != size) return false; 
        
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                Cell cell = gameState.getCell(row, col);
                if (cell.getState() == CellState.QUEEN) {
                    cell.setState(CellState.EMPTY);
                    boolean isValid = validator.canPlaceQueen(gameState, cell);
                    cell.setState(CellState.QUEEN);
                    
                    if (!isValid) return false;
                }
            }
        }
        return true;
    }
}