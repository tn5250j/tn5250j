package org.tn5250j.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class PositionTest {

    private static final int COLUMNS_80 = 80;
    private static final int COLUMNS_132 = 132;

    @Test
    void topLeftIsOffsetZero() {
        assertEquals(0, new Position(1, 1).toOffset(COLUMNS_80));
        assertEquals(new Position(1, 1), Position.fromOffset(0, COLUMNS_80));
    }

    @Test
    void bottomRightOf24x80() {
        int lastOffset = 24 * 80 - 1;
        assertEquals(lastOffset, new Position(24, 80).toOffset(COLUMNS_80));
        assertEquals(new Position(24, 80), Position.fromOffset(lastOffset, COLUMNS_80));
    }

    @Test
    void bottomRightOf27x132() {
        int lastOffset = 27 * 132 - 1;
        assertEquals(lastOffset, new Position(27, 132).toOffset(COLUMNS_132));
        assertEquals(new Position(27, 132), Position.fromOffset(lastOffset, COLUMNS_132));
    }

    @Test
    void lastColumnDoesNotWrapToNextRow() {
        assertEquals(new Position(1, 80), Position.fromOffset(79, COLUMNS_80));
        assertEquals(new Position(2, 1), Position.fromOffset(80, COLUMNS_80));
    }

    @Test
    void roundTripsEveryOffsetOf27x132() {
        for (int offset = 0; offset < 27 * 132; offset++) {
            assertEquals(offset, Position.fromOffset(offset, COLUMNS_132).toOffset(COLUMNS_132));
        }
    }

    @Test
    void rejectsZeroOrNegativeCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new Position(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Position(1, 0));
    }

    @Test
    void rejectsColumnBeyondScreenWidth() {
        assertThrows(IllegalArgumentException.class, () -> new Position(1, 81).toOffset(COLUMNS_80));
    }

    @Test
    void rejectsNegativeOffset() {
        assertThrows(IllegalArgumentException.class, () -> Position.fromOffset(-1, COLUMNS_80));
    }
}
