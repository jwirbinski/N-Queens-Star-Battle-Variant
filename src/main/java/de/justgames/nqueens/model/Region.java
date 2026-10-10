package de.justgames.nqueens.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Region {
    private final int id;
    private final List<Cell> cells;

    public Region(int id) {
        this.id = id;
        this.cells = new ArrayList<>();
    }

    public int getId() { return id; }

    public void addCell(Cell cell) {
        this.cells.add(cell);
    }

    public List<Cell> getCells() {
        return Collections.unmodifiableList(cells);
    }
}
