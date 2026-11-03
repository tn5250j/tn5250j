package org.tn5250j.mcp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.tn5250j.TN5250jConstants;

class ScreenSnapshotTest {

    private static final int COLUMNS = 4;
    private static final char VISIBLE = 0;
    private static final char NON_DISPLAY = (char) TN5250jConstants.EXTENDED_5250_NON_DSP;

    @Test
    void splitsIntoRowsOfScreenWidth() {
        List<String> lines = ScreenSnapshot.linesOf("ABCDEFGHIJKL".toCharArray(), COLUMNS);
        assertEquals(List.of("ABCD", "EFGH", "IJKL"), lines);
    }

    @Test
    void stripsTrailingSpacesButKeepsLeadingOnes() {
        List<String> lines = ScreenSnapshot.linesOf("  A   B ".toCharArray(), COLUMNS);
        assertEquals(List.of("  A", "  B"), lines);
    }

    @Test
    void blankRowBecomesEmptyString() {
        List<String> lines = ScreenSnapshot.linesOf("    XY  ".toCharArray(), COLUMNS);
        assertEquals(List.of("", "XY"), lines);
    }

    @Test
    void rejectsTextThatIsNotWholeRows() {
        assertThrows(IllegalArgumentException.class, () -> ScreenSnapshot.linesOf("ABCDE".toCharArray(), COLUMNS));
    }

    @Test
    void masksNonDisplayCharactersAsSpaces() {
        char[] text = "USERPASS".toCharArray();
        char[] extended = {VISIBLE, VISIBLE, VISIBLE, VISIBLE, NON_DISPLAY, NON_DISPLAY, NON_DISPLAY, NON_DISPLAY};

        char[] masked = ScreenSnapshot.maskNonDisplay(text, extended);

        assertArrayEquals("USER    ".toCharArray(), masked);
        assertArrayEquals("USERPASS".toCharArray(), text, "input must not be mutated");
    }

    @Test
    void masksOnlyTheNonDisplayBitNotOtherExtendedFlags() {
        char underlineAndColumnSeparator = 0x06;
        char[] masked = ScreenSnapshot.maskNonDisplay("AB".toCharArray(),
                new char[] {underlineAndColumnSeparator, (char) (underlineAndColumnSeparator | NON_DISPLAY)});
        assertArrayEquals("A ".toCharArray(), masked);
    }
}
