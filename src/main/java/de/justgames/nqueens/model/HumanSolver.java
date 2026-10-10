package de.justgames.nqueens.model;

import java.util.ArrayList;
import java.util.List;

public class HumanSolver {
    private enum SolverState { UNKNOWN, CROSSED, QUEEN }

    private final int n;
    private final int[][] blueprint;
    private final SolverState[][] grid;
    private int queensPlaced = 0;

    public HumanSolver(int[][] blueprint) {
        this.n = blueprint.length;
        this.blueprint = blueprint;
        this.grid = new SolverState[n][n];
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                grid[r][c] = SolverState.UNKNOWN;
            }
        }
    }

    public Difficulty solve() {
        int easyMoves = 0;
        int mediumMoves = 0;
        int hardMoves = 0;
        int expertMoves = 0;
        boolean progressMade = true;

        while (progressMade && queensPlaced < n) {
            progressMade = false;

            if (applyLastFreeCell()) { 
                easyMoves++;
                progressMade = true;
                continue; 
            }
            if (applyLineIntersections()) { 
                mediumMoves++;
                progressMade = true;
                continue;
            }
            if (applyBlockOuts()) { 
                hardMoves++;
                progressMade = true;
                continue;
            }
            if (applyContradictionRule()) {
                expertMoves++;
                progressMade = true;
                continue;
            }
        }

        if (queensPlaced < n) {
            return Difficulty.STUCK; 
        }

        int totalMoves = easyMoves + mediumMoves + hardMoves + expertMoves;
        double mediumRatio = (double) mediumMoves / totalMoves;
        double hardRatio = (double) hardMoves / totalMoves;

        if (expertMoves > 0 || hardMoves >= 2 || hardRatio > 0.15 || mediumRatio > 0.4) {
            return Difficulty.HARD;
        } else if (mediumMoves > 2 || mediumRatio > 0.1) {
            return Difficulty.MEDIUM;
        } else {
            return Difficulty.EASY;
        }
    }

    private boolean applyLastFreeCell() {
        for (int i = 0; i < n; i++) {
            if (placeIfOnlyOneUnknownInLine(true, i)) return true;
            if (placeIfOnlyOneUnknownInLine(false, i)) return true;
        }
        
        for (int reg = 1; reg <= n; reg++) {
            if (placeIfOnlyOneUnknownInRegion(reg)) return true;
        }
        
        return false;
    }

    private boolean placeIfOnlyOneUnknownInLine(boolean isRow, int index) {
        int unknownCount = 0;
        int queenCount = 0;
        int lastUnknownR = -1;
        int lastUnknownC = -1;

        for (int i = 0; i < n; i++) {
            int r = isRow ? index : i;
            int c = isRow ? i : index;
            
            if (grid[r][c] == SolverState.UNKNOWN) {
                unknownCount++;
                lastUnknownR = r;
                lastUnknownC = c;
            } else if (grid[r][c] == SolverState.QUEEN) {
                queenCount++;
            }
        }

        if (queenCount == 0 && unknownCount == 1) {
            placeQueen(lastUnknownR, lastUnknownC, grid);
            queensPlaced++;
            return true;
        }
        
        return false;
    }

    private boolean placeIfOnlyOneUnknownInRegion(int regionId) {
        List<Position> unknowns = getUnknownsInRegion(regionId, grid);
        int queenCount = getQueenCountInRegion(regionId, grid);
        
        if (queenCount == 0 && unknowns.size() == 1) {
            placeQueen(unknowns.get(0).row(), unknowns.get(0).col(), grid);
            queensPlaced++;
            return true;
        }
        
        return false;
    }

    private boolean applyLineIntersections() {
        for (int reg = 1; reg <= n; reg++) {
            List<Position> unknowns = getUnknownsInRegion(reg, grid);
            if (unknowns.isEmpty()) continue;

            int firstRow = unknowns.get(0).row();
            if (unknowns.stream().allMatch(p -> p.row() == firstRow)) {
                if (crossOutLineExceptRegion(true, firstRow, reg)) return true;
            }

            int firstCol = unknowns.get(0).col();
            if (unknowns.stream().allMatch(p -> p.col() == firstCol)) {
                if (crossOutLineExceptRegion(false, firstCol, reg)) return true;
            }
        }
        return false;
    }

    private boolean crossOutLineExceptRegion(boolean isRow, int index, int regionId) {
        boolean changed = false;
        for (int i = 0; i < n; i++) {
            int r = isRow ? index : i;
            int c = isRow ? i : index;
            if (blueprint[r][c] != regionId && grid[r][c] == SolverState.UNKNOWN) {
                grid[r][c] = SolverState.CROSSED;
                changed = true;
            }
        }
        return changed;
    }

    private boolean applyBlockOuts() {
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == SolverState.UNKNOWN) {
                    SolverState[][] clonedGrid = cloneGrid(grid);
                    placeQueen(r, c, clonedGrid);
                    
                    if (hasDeadEnd(clonedGrid)) {
                        grid[r][c] = SolverState.CROSSED;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean applyContradictionRule() {
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (grid[r][c] == SolverState.UNKNOWN) {
                    
                    if (leadsToContradiction(r, c, SolverState.QUEEN)) {
                        grid[r][c] = SolverState.CROSSED;
                        return true;
                    }
                    
                    if (leadsToContradiction(r, c, SolverState.CROSSED)) {
                        placeQueen(r, c, grid);
                        queensPlaced++;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean leadsToContradiction(int r, int c, SolverState assumption) {
        SolverState[][] backupGrid = cloneGrid(grid);
        int backupQueens = queensPlaced;

        if (assumption == SolverState.QUEEN) {
            placeQueen(r, c, grid);
            queensPlaced++;
        } else {
            grid[r][c] = SolverState.CROSSED;
        }

        boolean changed = true;
        while (changed) {
            changed = false;
            if (applyLastFreeCell()) changed = true;
            else if (applyLineIntersections()) changed = true;
            else if (applyBlockOuts()) changed = true;
        }

        boolean contradictionFound = hasDeadEnd(grid);
        restoreGrid(backupGrid);
        queensPlaced = backupQueens;

        return contradictionFound;
    }

    private void placeQueen(int r, int c, SolverState[][] targetGrid) {
        targetGrid[r][c] = SolverState.QUEEN;
        int regionId = blueprint[r][c];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == r && j == c) continue;
                
                boolean sameRow = (i == r);
                boolean sameCol = (j == c);
                boolean isTouching = (Math.abs(i - r) <= 1 && Math.abs(j - c) <= 1);
                boolean sameRegion = (blueprint[i][j] == regionId);

                if ((sameRow || sameCol || isTouching || sameRegion) && targetGrid[i][j] == SolverState.UNKNOWN) {
                    targetGrid[i][j] = SolverState.CROSSED;
                }
            }
        }
    }

    private boolean hasDeadEnd(SolverState[][] targetGrid) {
        for (int i = 0; i < n; i++) {
            if (isLineDead(true, i, targetGrid)) return true;
            if (isLineDead(false, i, targetGrid)) return true;
        }
        
        for (int reg = 1; reg <= n; reg++) {
            if (isRegionDead(reg, targetGrid)) return true;
        }
        
        return false;
    }

    private boolean isLineDead(boolean isRow, int index, SolverState[][] targetGrid) {
        for (int i = 0; i < n; i++) {
            int r = isRow ? index : i;
            int c = isRow ? i : index;
            if (targetGrid[r][c] == SolverState.QUEEN || targetGrid[r][c] == SolverState.UNKNOWN) {
                return false;
            }
        }
        return true;
    }

    private boolean isRegionDead(int regionId, SolverState[][] targetGrid) {
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (blueprint[r][c] == regionId) {
                    if (targetGrid[r][c] == SolverState.QUEEN || targetGrid[r][c] == SolverState.UNKNOWN) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private List<Position> getUnknownsInRegion(int regionId, SolverState[][] targetGrid) {
        List<Position> list = new ArrayList<>();
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (blueprint[r][c] == regionId && targetGrid[r][c] == SolverState.UNKNOWN) {
                    list.add(new Position(r, c));
                }
            }
        }
        return list;
    }

    private int getQueenCountInRegion(int regionId, SolverState[][] targetGrid) {
        int count = 0;
        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (blueprint[r][c] == regionId && targetGrid[r][c] == SolverState.QUEEN) {
                    count++;
                }
            }
        }
        return count;
    }

    private SolverState[][] cloneGrid(SolverState[][] source) {
        SolverState[][] clone = new SolverState[n][n];
        for (int r = 0; r < n; r++) {
            System.arraycopy(source[r], 0, clone[r], 0, n);
        }
        return clone;
    }

    private void restoreGrid(SolverState[][] backup) {
        for (int r = 0; r < n; r++) {
            System.arraycopy(backup[r], 0, grid[r], 0, n);
        }
    }
}