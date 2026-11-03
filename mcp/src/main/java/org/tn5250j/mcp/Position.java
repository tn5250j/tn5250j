package org.tn5250j.mcp;

/**
 * A 1-based screen coordinate, as shown to MCP clients and humans.
 * tn5250j uses 0-based linear offsets internally; this is the only place the two are converted.
 */
public record Position(int row, int col) {

    public Position {
        if (row < 1 || col < 1) {
            throw new IllegalArgumentException("row and col are 1-based, got (" + row + ", " + col + ")");
        }
    }

    public static Position fromOffset(int offset, int columns) {
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be >= 0, got " + offset);
        }
        return new Position(offset / columns + 1, offset % columns + 1);
    }

    public int toOffset(int columns) {
        if (col > columns) {
            throw new IllegalArgumentException("col " + col + " is beyond screen width " + columns);
        }
        return (row - 1) * columns + (col - 1);
    }
}
