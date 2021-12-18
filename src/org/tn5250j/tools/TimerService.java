/**
 *
 */
package org.tn5250j.tools;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.tn5250j.Gui5250Cursor;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.util.Duration;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TimerService {
    private final CopyOnWriteArrayList<WeakReference<Gui5250Cursor>> cursors = new CopyOnWriteArrayList<>();
    private static final TimerService instance = new TimerService();

    private final Timeline timer = new Timeline(new KeyFrame(
        Duration.millis(500l),
        this::processTimeAction
    ));

    private TimerService() {
        timer.play();
    }

    public static TimerService getInstance() {
        return instance;
    }

    public void addCursor(final Gui5250Cursor cursor) {
        cursors.add(new WeakReference<>(cursor));
    }

    private void processTimeAction(final ActionEvent e) {
        final List<WeakReference<Gui5250Cursor>> cursors = new ArrayList<>(this.cursors);
        for (final WeakReference<Gui5250Cursor> ref : cursors) {
            final Gui5250Cursor cursor = ref.get();
            if (cursor == null) {
                this.cursors.remove(ref);
            }

            cursor.doBlink();
        }
    }

    public void stop() {
        timer.stop();
    }
}
