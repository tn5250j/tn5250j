package org.tn5250j.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tn5250j.TN5250jConstants;

/** Exercises the state transitions only; no session or host is involved. */
class TransactionRecorderTest {

    private static final int HOST_READ_IMMEDIATE = 0;
    private static final Position CURSOR = new Position(6, 20);

    private TransactionRecorder recorder;

    @BeforeEach
    void setUp() {
        recorder = new TransactionRecorder(null);
    }

    @Test
    void unlockAidUnlockRecordsOneTransaction() {
        ScreenSnapshot menu = screen("MAIN MENU", field(6, 20, "", false));
        ScreenSnapshot typed = screen("MAIN MENU", field(6, 20, "1", true));
        ScreenSnapshot result = screen("WORK WITH ORDERS");

        recorder.begin(menu);
        recorder.onAidKey(TN5250jConstants.AID_ENTER, typed);
        recorder.onKeyboardUnlocked(result);
        List<RecordedTransaction> transactions = recorder.end();

        assertEquals(1, transactions.size());
        RecordedTransaction transaction = transactions.get(0);
        assertSame(menu, transaction.before());
        assertEquals(List.of(field(6, 20, "1", true)), transaction.changedFields());
        assertEquals(CURSOR, transaction.cursor());
        assertEquals("[enter]", transaction.aid());
        assertSame(result, transaction.after());
    }

    @Test
    void screenAsPresentedIsTheLatestUnlockBeforeTheAid() {
        ScreenSnapshot first = screen("FIRST");
        ScreenSnapshot second = screen("SECOND");

        recorder.begin(first);
        recorder.onKeyboardUnlocked(second);
        recorder.onAidKey(TN5250jConstants.AID_PF3, second);
        recorder.onKeyboardUnlocked(screen("THIRD"));

        assertSame(second, recorder.end().get(0).before());
    }

    @Test
    void consecutiveTransactionsChain() {
        ScreenSnapshot one = screen("ONE");
        ScreenSnapshot two = screen("TWO");
        ScreenSnapshot three = screen("THREE");

        recorder.begin(one);
        recorder.onAidKey(TN5250jConstants.AID_ENTER, one);
        recorder.onKeyboardUnlocked(two);
        recorder.onAidKey(TN5250jConstants.AID_PF12, two);
        recorder.onKeyboardUnlocked(three);
        List<RecordedTransaction> transactions = recorder.end();

        assertEquals(2, transactions.size());
        assertSame(two, transactions.get(0).after());
        assertSame(two, transactions.get(1).before());
        assertEquals("[pf12]", transactions.get(1).aid());
        assertSame(three, transactions.get(1).after());
    }

    @Test
    void hostReadImmediateIsIgnored() {
        recorder.begin(screen("ONE"));
        recorder.onAidKey(HOST_READ_IMMEDIATE, screen("ONE"));
        recorder.onKeyboardUnlocked(screen("TWO"));

        assertTrue(recorder.end().isEmpty());
    }

    @Test
    void aidWhileNotRecordingDoesNothing() {
        recorder.onAidKey(TN5250jConstants.AID_ENTER, screen("ONE"));
        recorder.onKeyboardUnlocked(screen("TWO"));

        recorder.begin(screen("TWO"));
        assertTrue(recorder.end().isEmpty());
    }

    @Test
    void eventsAfterEndAreIgnored() {
        recorder.begin(screen("ONE"));
        recorder.end();
        recorder.onAidKey(TN5250jConstants.AID_ENTER, screen("ONE"));
        recorder.onKeyboardUnlocked(screen("TWO"));

        recorder.begin(screen("TWO"));
        assertTrue(recorder.end().isEmpty());
    }

    @Test
    void endKeepsTransactionStillAwaitingTheHost() {
        recorder.begin(screen("ONE"));
        recorder.onAidKey(TN5250jConstants.AID_PF3, screen("ONE"));

        List<RecordedTransaction> transactions = recorder.end();

        assertEquals(1, transactions.size());
        assertNull(transactions.get(0).after());
    }

    @Test
    void secondAidBeforeUnlockClosesThePreviousTransaction() {
        ScreenSnapshot one = screen("ONE");
        ScreenSnapshot two = screen("TWO");

        recorder.begin(one);
        recorder.onAidKey(TN5250jConstants.AID_ENTER, one);
        recorder.onAidKey(TN5250jConstants.AID_PF3, two);
        List<RecordedTransaction> transactions = recorder.end();

        assertEquals(2, transactions.size());
        assertSame(two, transactions.get(0).after());
        assertSame(two, transactions.get(1).before());
    }

    @Test
    void fieldModifiedByHostAndUntouchedIsNotAChange() {
        FieldSnapshot hostPreset = field(4, 10, "DEFAULT", true);
        ScreenSnapshot presented = screen("FORM", hostPreset, field(5, 10, "", false));
        ScreenSnapshot submitted = screen("FORM", hostPreset, field(5, 10, "NEW", true));

        recorder.begin(presented);
        recorder.onAidKey(TN5250jConstants.AID_ENTER, submitted);

        assertEquals(List.of(field(5, 10, "NEW", true)), recorder.end().get(0).changedFields());
    }

    @Test
    void hostPresetFieldEditedByUserIsAChange() {
        ScreenSnapshot presented = screen("FORM", field(4, 10, "DEFAULT", true));
        ScreenSnapshot submitted = screen("FORM", field(4, 10, "EDITED", true));

        recorder.begin(presented);
        recorder.onAidKey(TN5250jConstants.AID_ENTER, submitted);

        assertEquals(List.of(field(4, 10, "EDITED", true)), recorder.end().get(0).changedFields());
    }

    @Test
    void beginTwiceIsRejected() {
        recorder.begin(screen("ONE"));
        assertThrows(IllegalStateException.class, () -> recorder.begin(screen("ONE")));
    }

    @Test
    void endWithoutBeginIsRejected() {
        assertThrows(IllegalStateException.class, () -> recorder.end());
    }

    private static ScreenSnapshot screen(String title, FieldSnapshot... fields) {
        return new ScreenSnapshot(List.of(title), List.of(fields), CURSOR, false);
    }

    private static FieldSnapshot field(int row, int col, String value, boolean modified) {
        return new FieldSnapshot(row, col, 10, value, false, false, modified, false, row * 100 + col);
    }
}
