package chess;

public class ChessPosition {
    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
            this.row = row;
            this.col = col;
        }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return col;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChessPosition that = (ChessPosition) o;

        if (row != that.row) return false;
        return col == that.col;
    }

    @Override
    public int hashCode() {
        int result = row;
        result = 31 * result + col;
        return result;
    }

    @Override
    public String toString() {
        return "ChessPosition{" +
                "row=" + row +
                ", col=" + col +
                '}';
    }
}
