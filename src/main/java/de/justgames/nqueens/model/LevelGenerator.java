package de.justgames.nqueens.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class LevelGenerator {
    public enum Direction { UP, DOWN, LEFT, RIGHT, NONE }

    private final int n;
    private int[] solution;
    private final Random random;
    private int[] wrongSolution;

    public LevelGenerator(int n) {
        this.n = n;
        this.random = new Random();
    }

    public void findRandomSolution() {
        int[] currentSolution = new int[n];
        backtrack(0, 0, 0, currentSolution);
    }

    private boolean backtrack(int row, int cols, int adjacentForbidden, int[] currentSolution) {
        if (row == n) {
            solution = Arrays.copyOf(currentSolution, n);
            return true;
        }

        int availablePositions = ((1 << n) - 1) & ~(cols | adjacentForbidden);

        List<Integer> validCols = new ArrayList<>();
        while (availablePositions > 0) {
            int position = availablePositions & -availablePositions;
            validCols.add(Integer.numberOfTrailingZeros(position));
            availablePositions &= availablePositions - 1;
        }

        Collections.shuffle(validCols, random);

        for (int colIdx : validCols) {
            currentSolution[row] = colIdx;
            int position = 1 << colIdx;

            int nextAdjacentForbidden = (position << 1) | (position >> 1);

            if (backtrack(row + 1, cols | position, nextAdjacentForbidden, currentSolution)) {
                return true; 
            }
        }

        return false;
    }

    public Board generateLevel(Difficulty targetDifficulty) {
        int builderHeuristic = (targetDifficulty == Difficulty.EASY) ? 1 : 
                               (targetDifficulty == Difficulty.MEDIUM) ? 2 : 3;
        
        int maxRepairs = n * 4;

        while (true) {
            findRandomSolution(); 
            int[][] blueprint = generateRegionBlueprint(solution, builderHeuristic);
            
            if (blueprint == null) continue;
            
            int repairCount = 0;
            boolean failed = false;

            while (hasAlternativeSolution(blueprint, solution)) {
                if (repairCount >= maxRepairs) {
                    failed = true;
                    break;
                }
                if (!repairBlueprint(blueprint, wrongSolution, solution)) {
                    failed = true;
                    break;
                }
                repairCount++;
            }
            
            if (failed) continue; 

            HumanSolver solver = new HumanSolver(blueprint);
            Difficulty actualDifficulty = solver.solve();

            if (actualDifficulty == targetDifficulty) {
                return new Board(blueprint);
            }
        }
    }

    private boolean repairBlueprint(int[][] blueprint, int[] wrong, int[] master) {
        List<Integer> differingRows = new ArrayList<>();
        
        for (int r = 0; r < n; r++) {
            if (wrong[r] != master[r]) {
                differingRows.add(r);
            }
        }

        Collections.shuffle(differingRows, random);

        for (int row : differingRows) {
            int wrongCol = wrong[row];
            int oldRegionId = blueprint[row][wrongCol];

            List<Position> neighbors = getNeighbors(row, wrongCol, n);
            Collections.shuffle(neighbors, random);

            for (Position neighbor : neighbors) {
                int newRegionId = blueprint[neighbor.row()][neighbor.col()];
                
                if (newRegionId != oldRegionId) {
                    blueprint[row][wrongCol] = newRegionId;

                    if (isContiguous(blueprint, oldRegionId)) {
                        return true;
                    } else {
                        blueprint[row][wrongCol] = oldRegionId;
                    }
                }
            }
        }
        
        return false;
    }

    private boolean isContiguous(int[][] blueprint, int regionId) {
        int startRow = -1, startCol = -1;
        int totalCells = 0;

        for (int r = 0; r < n; r++) {
            for (int c = 0; c < n; c++) {
                if (blueprint[r][c] == regionId) {
                    totalCells++;
                    if (startRow == -1) {
                        startRow = r;
                        startCol = c;
                    }
                }
            }
        }

        if (totalCells == 0) return true;

        boolean[][] visited = new boolean[n][n];
        int reachableCells = countConnectedCells(blueprint, startRow, startCol, regionId, visited);

        return reachableCells == totalCells;
    }

    private int countConnectedCells(int[][] blueprint, int r, int c, int regionId, boolean[][] visited) {
        if (r < 0 || r >= n || c < 0 || c >= n) return 0;
        if (visited[r][c] || blueprint[r][c] != regionId) return 0;

        visited[r][c] = true;
        int count = 1;

        count += countConnectedCells(blueprint, r - 1, c, regionId, visited);
        count += countConnectedCells(blueprint, r + 1, c, regionId, visited);
        count += countConnectedCells(blueprint, r, c - 1, regionId, visited);
        count += countConnectedCells(blueprint, r, c + 1, regionId, visited);

        return count;
    }

    private List<Position> getNeighbors(int r, int c, int size) {
        List<Position> list = new ArrayList<>();
        if (r > 0) list.add(new Position(r - 1, c));
        if (r < size - 1) list.add(new Position(r + 1, c));
        if (c > 0) list.add(new Position(r, c - 1));
        if (c < size - 1) list.add(new Position(r, c + 1));
        return list;
    }

    private int[][] generateRegionBlueprint(int[] solution, int difficulty) {
        Map<Integer, List<Position>> regions = new HashMap<>();
        List<Integer> regionIds = new ArrayList<>();
        Map<Integer, Direction> lastDirections = new HashMap<>();
        int[][] blueprint = new int[n][n];
        int regionCount = solution.length;

        for (int i = 0; i < regionCount; i++) {
            blueprint[i][solution[i]] = i + 1;
            List<Position> cells = new ArrayList<>();
            cells.add(new Position(i, solution[i]));
            regionIds.add(i + 1);
            regions.put(i + 1, cells);
            lastDirections.put(i + 1, Direction.NONE);
        }
        
        int i = 0;
        while (i < (n * n) - regionCount) {
            if (regionIds.isEmpty()) {
                return null;
            }

            int randIndex1 = ThreadLocalRandom.current().nextInt(regionIds.size());
            int randomId = regionIds.get(randIndex1);

            List<Position> cells = regions.get(randomId);

            if (cells.isEmpty()) {
                regionIds.set(randIndex1, regionIds.get(regionIds.size() - 1));
                regionIds.remove(regionIds.size() - 1);
                continue;
            }

            int randIndex2;
            boolean useTip = false;
            if (difficulty == 1) useTip = ThreadLocalRandom.current().nextDouble() < 0.90;
            else if (difficulty == 2) useTip = ThreadLocalRandom.current().nextDouble() < 0.50;
            
            if (useTip) {
                randIndex2 = cells.size() - 1;
            } else {
                randIndex2 = ThreadLocalRandom.current().nextInt(cells.size());
            }
            
            Position cell = cells.get(randIndex2);

            List<Position> neighbours = new ArrayList<>();
            List<Direction> neighborDirs = new ArrayList<>();
            
            int row = cell.row();
            int col = cell.col();

            if (row - 1 >= 0 && blueprint[row - 1][col] == 0) {
                neighbours.add(new Position(row - 1, col));
                neighborDirs.add(Direction.UP);
            }
            if (row + 1 < n && blueprint[row + 1][col] == 0) {
                neighbours.add(new Position(row + 1, col));
                neighborDirs.add(Direction.DOWN);
            }
            if (col - 1 >= 0 && blueprint[row][col - 1] == 0) {
                neighbours.add(new Position(row, col - 1));
                neighborDirs.add(Direction.LEFT);
            }
            if (col + 1 < n && blueprint[row][col + 1] == 0) {
                neighbours.add(new Position(row, col + 1));
                neighborDirs.add(Direction.RIGHT);
            }

            if (neighbours.isEmpty()) {
                cells.set(randIndex2, cells.get(cells.size() - 1));
                cells.remove(cells.size() - 1);
                continue;
            }

            List<Integer> weightedIndices = new ArrayList<>();
            Direction lastDir = lastDirections.get(randomId);
            
            int weight = 1;
            if (difficulty == 1) weight = 10;
            else if (difficulty == 2) weight = 3;

            for (int j = 0; j < neighbours.size(); j++) {
                int count = (neighborDirs.get(j) == lastDir) ? weight : 1;
                for (int w = 0; w < count; w++) {
                    weightedIndices.add(j);
                }
            }

            int chosenIndex = weightedIndices.get(ThreadLocalRandom.current().nextInt(weightedIndices.size()));
            Position pos = neighbours.get(chosenIndex);
            Direction chosenDir = neighborDirs.get(chosenIndex);

            blueprint[pos.row()][pos.col()] = randomId;
            cells.add(pos);
            lastDirections.put(randomId, chosenDir);
            
            i++;
        }

        return blueprint;
    }

    public boolean hasAlternativeSolution(int[][] blueprint, int[] masterSolution) {
            this.wrongSolution = null;
            int[] currentSolution = new int[n];
            
            return solveRegions(0, 0, 0, 0, currentSolution, blueprint, masterSolution);
        }

        private boolean solveRegions(int row, int cols, int adjacentForbidden, int usedRegions, int[] currentSolution, int[][] blueprint, int[] masterSolution) {
        if (row == n) {
            if (Arrays.equals(currentSolution, masterSolution)) {
                return false;
            } else {
                this.wrongSolution = Arrays.copyOf(currentSolution, n);
                return true;
            }
        }

        int availablePositions = ((1 << n) - 1) & ~(cols | adjacentForbidden);

        List<Integer> validCols = new ArrayList<>();
        while (availablePositions > 0) {
            int position = availablePositions & -availablePositions;
            validCols.add(Integer.numberOfTrailingZeros(position));
            availablePositions &= availablePositions - 1;
        }

        for (int colIdx : validCols) {
            int regionId = blueprint[row][colIdx];
            
            if ((usedRegions & (1 << regionId)) != 0) {
                continue;
            }

            currentSolution[row] = colIdx;
            int position = 1 << colIdx;
            
            int nextAdjacentForbidden = (position << 1) | (position >> 1);

            if (solveRegions(
                row + 1,
                cols | position,
                nextAdjacentForbidden,
                usedRegions | (1 << regionId),
                currentSolution,
                blueprint,
                masterSolution
            )) {
                return true; 
            }
        }
        return false;
    }
}