package org.tn5250j.mcp;

import org.tn5250j.Session5250;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenField;
import org.tn5250j.framework.tn5250.ScreenOIA;

/**
 * Reads and drives the live screen the same way prod scripts do:
 * {@code findByPosition(...).setString(...)} and {@code sendKeys(...)}.
 * Invalid requests throw IllegalArgumentException / IllegalStateException with a message for the caller.
 */
public final class ScreenDriver {

    private static final long POLL_INTERVAL_MS = 50;

    private final Session5250 session;

    public record WaitResult(boolean ready, ScreenSnapshot screen) {
    }

    public ScreenDriver(Session5250 session) {
        this.session = session;
    }

    public ScreenSnapshot snapshot() {
        return ScreenSnapshot.of(session.getScreen());
    }

    /** @return the start of the field that was set */
    public Position setField(Position position, String text) {
        Screen5250 screen = session.getScreen();
        if (position.row() > screen.getRows()) {
            throw new IllegalArgumentException("row " + position.row() + " is beyond screen height " + screen.getRows());
        }
        if (!isReady(screen.getOIA())) {
            throw new IllegalStateException("keyboard is locked; call wait_for_ready first");
        }

        ScreenField field = screen.getScreenFields().findByPosition(position.toOffset(screen.getColumns()));
        if (field == null) {
            throw new IllegalArgumentException("no input field at " + describe(position));
        }
        Position fieldStart = Position.fromOffset(field.startPos(), screen.getColumns());
        if (field.isBypassField()) {
            throw new IllegalArgumentException("field at " + describe(fieldStart) + " is protected");
        }
        if (text.length() > field.getLength()) {
            // setString would silently truncate
            throw new IllegalArgumentException("text is " + text.length() + " characters but field at "
                    + describe(fieldStart) + " holds " + field.getLength());
        }

        field.setString(text);
        return fieldStart;
    }

    /** Text and mnemonics such as "[enter]", "[pf3]", "[tab]". While the keyboard is locked tn5250j buffers them. */
    public void sendKeys(String keys) {
        if (keys.isEmpty()) {
            throw new IllegalArgumentException("keys must not be empty");
        }
        session.getScreen().sendKeys(keys);
    }

    public WaitResult waitForReady(long timeoutMs) {
        ScreenOIA oia = session.getScreen().getOIA();
        long deadline = System.nanoTime() + timeoutMs * 1_000_000;
        boolean ready = isReady(oia);
        while (!ready && System.nanoTime() < deadline) {
            try {
                Thread.sleep(POLL_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
            ready = isReady(oia);
        }
        return new WaitResult(ready, snapshot());
    }

    private static boolean isReady(ScreenOIA oia) {
        return !oia.isKeyBoardLocked() && oia.getInputInhibited() == ScreenOIA.INPUTINHIBITED_NOTINHIBITED;
    }

    private static String describe(Position position) {
        return "(" + position.row() + ", " + position.col() + ")";
    }
}
