package de.justgames.nqueens.model;

import java.util.HashMap;
import java.util.Map;

public class Board {
    private final int size;
    private final Cell[][] grid;
    private final Map<Integer, Region> regions;

    public Board(int[][] regionBlueprint) {
        this.size = regionBlueprint.length;
        this.grid = new Cell[size][size];
        this.regions = new HashMap<>();

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int regionId = regionBlueprint[row][col];
                
                Region region = regions.computeIfAbsent(regionId, Region::new);
                
                Cell cell = new Cell(new Position(row, col), region);
                grid[row][col] = cell;
                region.addCell(cell);
            }
        }
    }

    public int getSize() { return size; }

    public Cell getCell(int row, int col) {
        if (row < 0 || row >= size || col < 0 || col >= size) {
            throw new IllegalArgumentException("Coordinates out of bounds");
        }
        return grid[row][col];
    }

    public Map<Integer, Region> getRegions() {
        return regions;
    }
}
