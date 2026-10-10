package de.justgames.nqueens.model;

import java.util.List;

public class RuleValidator {
    public boolean canPlaceQueen(Board board, Cell cell) {
        int row = cell.getPosition().row();
        int col = cell.getPosition().col();

        if(board.getCell(row, col).getState() == CellState.QUEEN) return false;

        List<Cell> cellsInSameRegion = cell.getRegion().getCells();
        int size = board.getSize();

        for(int i = 0, j = size - 1; i <= j; i++, j--) {
            if(board.getCell(i, col).getState() == CellState.QUEEN) return false;
            if(board.getCell(j, col).getState() == CellState.QUEEN) return false;
        }

        for(int i = 0, j = size - 1; i <= j; i++, j--) {
            if(board.getCell(row, i).getState() == CellState.QUEEN) return false;
            if(board.getCell(row, j).getState() == CellState.QUEEN) return false;
        }
        
        if(row - 1 >= 0 && col - 1 >= 0) {
            if(board.getCell(row - 1, col - 1).getState() == CellState.QUEEN) return false;
        }
        if(row - 1 >= 0 && col + 1 < size) {
            if(board.getCell(row - 1, col + 1).getState() == CellState.QUEEN) return false;
        }
        if(row + 1 < size && col - 1 >= 0) {
            if(board.getCell(row + 1, col - 1).getState() == CellState.QUEEN) return false;
        }
        if(row + 1 < size && col + 1 < size) {
            if(board.getCell(row + 1, col + 1).getState() == CellState.QUEEN) return false;
        }

        for(Cell currentCell : cellsInSameRegion) {
            if(currentCell.getState() == CellState.QUEEN) return false;
        }

        return true;
    }
}
