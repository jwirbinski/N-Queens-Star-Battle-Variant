package de.justgames.nqueens.model;

public class Cell {
    private final Position position;
    private final Region region;
    private CellState state;

    public Cell(Position position, Region region) {
        this.position = position;
        this.region = region;
        this.state = CellState.EMPTY;
    }

    public Position getPosition() { return position; }
    public Region getRegion() { return region; }
    public CellState getState() { return state; }

    public void setState(CellState state) {
        this.state = state;
    }
    
    public boolean hasQueen() {
        return this.state == CellState.QUEEN;
    }
}
