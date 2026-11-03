package org.tn5250j.mcp;

import java.util.List;

/**
 * One human transaction: the screen as presented, what they changed, the key they pressed,
 * and the screen the host returned. {@code after} is null if recording stopped before the host responded.
 */
public record RecordedTransaction(
        ScreenSnapshot before,
        List<FieldSnapshot> changedFields,
        Position cursor,
        String aid,
        ScreenSnapshot after) {

    public RecordedTransaction {
        changedFields = List.copyOf(changedFields);
    }
}
