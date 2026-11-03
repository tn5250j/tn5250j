package org.tn5250j.mcp;

import java.util.ArrayList;
import java.util.List;

import org.tn5250j.Session5250;
import org.tn5250j.event.AidListener;
import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenOIA;
import org.tn5250j.framework.tn5250.tnvt;

/**
 * Records what a human does in the session window as a list of transactions.
 * <p>
 * Keyboard unlock marks the "screen as presented"; an AID key captures the changed fields and cursor;
 * the next unlock (or AID) supplies the host's response. Events arrive on the session thread and the EDT,
 * and start/stop on MCP request threads, so state changes are synchronized.
 */
public final class TransactionRecorder {

    private static final int HOST_READ_IMMEDIATE_AID = 0;

    private final Session5250 session;

    private volatile boolean recording;
    private ScreenSnapshot presented;
    private PendingTransaction pending;
    private final List<RecordedTransaction> transactions = new ArrayList<>();

    private boolean oiaListenerAttached;
    private tnvt aidListenerTarget;

    private final ScreenOIAListener unlockListener = this::onOiaChanged;
    private final AidListener aidListener = this::onAid;

    private record PendingTransaction(ScreenSnapshot before, List<FieldSnapshot> changedFields, Position cursor,
            String aid) {
    }

    /** @param session the live session; may be null when only the state transitions are exercised */
    public TransactionRecorder(Session5250 session) {
        this.session = session;
    }

    public synchronized void start() {
        tnvt vt = session.getVT();
        if (vt == null || !session.isConnected()) {
            throw new IllegalStateException("session is not connected");
        }
        attachListeners(session.getScreen(), vt);
        begin(ScreenSnapshot.of(session.getScreen()));
    }

    public synchronized List<RecordedTransaction> stop() {
        return end();
    }

    public boolean isRecording() {
        return recording;
    }

    synchronized void begin(ScreenSnapshot current) {
        if (recording) {
            throw new IllegalStateException("already recording");
        }
        transactions.clear();
        pending = null;
        presented = current;
        recording = true;
    }

    synchronized List<RecordedTransaction> end() {
        if (!recording) {
            throw new IllegalStateException("not recording");
        }
        recording = false;
        if (pending != null) {
            complete(null);
        }
        presented = null;
        return List.copyOf(transactions);
    }

    synchronized void onKeyboardUnlocked(ScreenSnapshot current) {
        if (!recording) {
            return;
        }
        if (pending != null) {
            complete(current);
        }
        presented = current;
    }

    synchronized void onAidKey(int aid, ScreenSnapshot current) {
        if (!recording || aid == HOST_READ_IMMEDIATE_AID) {
            return;
        }
        if (pending != null) {
            // the host never unlocked between keys; what the user saw at this key is the previous response
            complete(current);
            presented = current;
        }
        pending = new PendingTransaction(presented, changedFields(presented, current), current.cursor(),
                AidNames.nameOf(aid));
    }

    private void complete(ScreenSnapshot after) {
        transactions.add(new RecordedTransaction(pending.before(), pending.changedFields(), pending.cursor(),
                pending.aid(), after));
        pending = null;
    }

    private static List<FieldSnapshot> changedFields(ScreenSnapshot before, ScreenSnapshot atAid) {
        List<FieldSnapshot> changed = new ArrayList<>();
        for (FieldSnapshot field : atAid.fields()) {
            if (field.modified() && !wasAlreadyModifiedWithSameValue(field, before)) {
                changed.add(field);
            }
        }
        return changed;
    }

    private static boolean wasAlreadyModifiedWithSameValue(FieldSnapshot field, ScreenSnapshot before) {
        return before.fields().stream().anyMatch(field::isUnchangedFrom);
    }

    /**
     * Listeners are added once and never removed: ScreenOIA fires by index over a Vector, so removing
     * from another thread mid-fire could throw on the session thread. The recording flag gates them instead.
     */
    private void attachListeners(Screen5250 screen, tnvt vt) {
        if (!oiaListenerAttached) {
            screen.getOIA().addOIAListener(unlockListener);
            oiaListenerAttached = true;
        }
        // a reconnect creates a new tnvt
        if (aidListenerTarget != vt) {
            if (aidListenerTarget != null) {
                aidListenerTarget.removeAidListener(aidListener);
            }
            vt.addAidListener(aidListener);
            aidListenerTarget = vt;
        }
    }

    private void onOiaChanged(ScreenOIA oia, int change) {
        if (recording && change == ScreenOIAListener.OIA_CHANGED_KEYBOARD_LOCKED && !oia.isKeyBoardLocked()) {
            onKeyboardUnlocked(ScreenSnapshot.of(oia.getSource()));
        }
    }

    private void onAid(int aid) {
        if (recording) {
            onAidKey(aid, ScreenSnapshot.of(session.getScreen()));
        }
    }
}
