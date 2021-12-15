/**
 *
 */
package org.tn5250j;

import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class RubberBandFX implements RubberBand {
    private final Rectangle selection = new Rectangle();
    private final EventHandler<MouseEvent> listener = this::mouseDraged;
    private Point2D start;

    public RubberBandFX() {
        selection.setVisible(false);
        selection.setX(0);
        selection.setY(0);
        selection.setMouseTransparent(true);
        selection.setFocusTraversable(false);
        selection.setFill(null); // transparent
        // selection.setFill(Color.WHITE);
        selection.setStroke(Color.WHITE); // border
        selection.setBlendMode(BlendMode.DIFFERENCE);
        selection.setStrokeWidth(2.);
        selection.setWidth(100);
        selection.setHeight(100);
    }

    public Rectangle getComponent() {
        return selection;
    }

    public void startListen(final Node n) {
        n.addEventHandler(MouseEvent.ANY, listener);
    }
    public void stopListen(final Node n) {
        n.removeEventHandler(MouseEvent.ANY, listener);
    }

    private void mouseDraged(final MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) {
            ensureRubberBandStopped();
        }

        final double x = e.getX();
        final double y = e.getY();

        boolean shouldProcess = false;

        final EventType<? extends MouseEvent> type = e.getEventType();
        if (type == MouseEvent.MOUSE_PRESSED) {
            start = new Point2D(x, y);
            shouldProcess = true;
        } else if (type == MouseEvent.MOUSE_RELEASED) {
            ensureRubberBandStopped();
        } else if (start != null && type == MouseEvent.MOUSE_DRAGGED) {
            shouldProcess = true;
        }

        if (shouldProcess) {
            selection.setX(Math.min(x, start.getX()));
            selection.setY(Math.min(y, start.getY()));
            selection.setWidth(Math.abs(x - start.getX()));
            selection.setHeight(Math.abs(y - start.getY()));

            if (!selection.isVisible()) {
                selection.setVisible(true);
            }
        }
    }

    private void ensureRubberBandStopped() {
        if (start != null) {
            start = null;

            //TODO notify listeners
        }
    }

    public void reset() {
        start = null;
        selection.setVisible(false);
    }

    public Rectangle getSelection() {
        return start == null ? null : selection;
    }

    @Override
    public boolean isAreaSelected() {
        return getSelection() != null;
    }

    @Override
    public void erase() {
    }

    @Override
    public Rectangle2D getBoundingArea() {
        return new Rectangle2D(selection.getX(), selection.getY(), selection.getWidth(), selection.getHeight());
    }

    @Override
    public void draw() {
    }
}
