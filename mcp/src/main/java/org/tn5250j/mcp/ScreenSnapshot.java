package org.tn5250j.mcp;

import java.util.ArrayList;
import java.util.List;

import org.tn5250j.TN5250jConstants;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;

/**
 * The screen text, input fields, cursor and keyboard state at one moment.
 * Non-display characters (e.g. a typed password) are blanked.
 */
public record ScreenSnapshot(List<String> lines, List<FieldSnapshot> fields, Position cursor, boolean keyboardLocked) {

    private static final char MASK = ' ';

    public ScreenSnapshot {
        lines = List.copyOf(lines);
        fields = List.copyOf(fields);
    }

    public static ScreenSnapshot of(Screen5250 screen) {
        int columns = screen.getColumns();
        char[] extended = extendedPlane(screen);

        List<String> lines = linesOf(maskNonDisplay(screen.getScreenAsChars(), extended), columns);

        List<FieldSnapshot> fields = new ArrayList<>();
        for (ScreenField field : screen.getScreenFields().getFields()) {
            fields.add(FieldSnapshot.of(field, columns, isNonDisplay(extended[field.startPos()])));
        }

        // tn5250j already reports the cursor 1-based
        Position cursor = new Position(screen.getCurrentRow(), screen.getCurrentCol());
        return new ScreenSnapshot(lines, fields, cursor, screen.getOIA().isKeyBoardLocked());
    }

    static List<String> linesOf(char[] text, int columns) {
        if (text.length % columns != 0) {
            throw new IllegalArgumentException(
                    "text length " + text.length + " is not a whole number of " + columns + "-column rows");
        }
        List<String> lines = new ArrayList<>(text.length / columns);
        for (int start = 0; start < text.length; start += columns) {
            lines.add(new String(text, start, columns).stripTrailing());
        }
        return lines;
    }

    static char[] maskNonDisplay(char[] text, char[] extended) {
        char[] masked = text.clone();
        for (int offset = 0; offset < masked.length; offset++) {
            if (isNonDisplay(extended[offset])) {
                masked[offset] = MASK;
            }
        }
        return masked;
    }

    private static boolean isNonDisplay(char extendedAttributes) {
        return (extendedAttributes & TN5250jConstants.EXTENDED_5250_NON_DSP) != 0;
    }

    private static char[] extendedPlane(Screen5250 screen) {
        int lastRow = screen.getRows() - 1;
        // getData's end position is exclusive, so (lastRow, columns) covers the whole screen
        char[] plane = screen.getData(0, 0, lastRow, screen.getColumns(), TN5250jConstants.PLANE_EXTENDED);
        if (plane == null) {
            throw new IllegalStateException("could not read the screen's extended attribute plane");
        }
        return plane;
    }
}
