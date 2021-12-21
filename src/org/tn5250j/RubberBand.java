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
public class RubberBand {
    private final Rectangle selection = new Rectangle();
    private final EventHandler<MouseEvent> listener = this::mouseDraged;
    private final SessionGui gui;
    private Point2D start;

    public RubberBand(final SessionGui gui) {
        this.gui = gui;

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
        if (!isProcessing(e)) {
            return;
        }

        double x = e.getX();
        double y = e.getY();

        boolean shouldProcess = false;

        final EventType<? extends MouseEvent> type = e.getEventType();
        if (type == MouseEvent.MOUSE_PRESSED) {
            reset();
        } else if (start != null && type == MouseEvent.MOUSE_CLICKED) {
            final Point2D p = gui.translateStart(new Point2D(x, y));
            x = p.getX();
            y = p.getY();
            shouldProcess = true;
        } else if (type == MouseEvent.MOUSE_DRAGGED) {
            if (start == null) {
                start = gui.translateStart(new Point2D(x, y));
            }
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

    private boolean isProcessing(final MouseEvent e) {
        return e.getButton() == MouseButton.PRIMARY;
    }

    public void reset() {
        start = null;
        selection.setVisible(false);
    }

    public boolean isAreaSelected() {
        return selection.isVisible();
    }

    public Rectangle2D getBoundingArea() {
        return new Rectangle2D(selection.getX(), selection.getY(), selection.getWidth(), selection.getHeight());
    }

    public Node getSelectionComponent() {
        return selection;
    }
}
