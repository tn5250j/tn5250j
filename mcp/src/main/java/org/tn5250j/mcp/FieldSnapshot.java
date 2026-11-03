package org.tn5250j.mcp;

import org.tn5250j.framework.tn5250.ScreenField;

/**
 * An input field as it looked at one moment. {@code hidden} fields (non-display, e.g. passwords)
 * never carry their value.
 */
public record FieldSnapshot(
        int row,
        int col,
        int length,
        String value,
        boolean protectedField,
        boolean numeric,
        boolean modified,
        boolean hidden,
        int fieldId) {

    static final String HIDDEN_VALUE = "";

    public static FieldSnapshot of(ScreenField field, int columns, boolean hidden) {
        Position start = Position.fromOffset(field.startPos(), columns);
        String value = hidden ? HIDDEN_VALUE : field.getString().stripTrailing();
        return new FieldSnapshot(start.row(), start.col(), field.getLength(), value,
                field.isBypassField(), field.isNumeric(), field.isModified(), hidden, field.getFieldId());
    }

    public Position position() {
        return new Position(row, col);
    }

    /** Same field, same value, and already modified: the user did not change it. */
    boolean isUnchangedFrom(FieldSnapshot earlier) {
        return earlier.row == row && earlier.col == col && earlier.modified && earlier.value.equals(value);
    }
}
