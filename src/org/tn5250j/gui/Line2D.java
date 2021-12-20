/**
 *
 */
package org.tn5250j.gui;

import javafx.geometry.Point2D;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class Line2D {
    private Point2D start = Point2D.ZERO;
    private Point2D end = Point2D.ZERO;

    public Point2D getStart() {
        return start;
    }
    public void setStart(final Point2D start) {
        this.start = start;
    }
    public Point2D getEnd() {
        return end;
    }
    public void setEnd(final Point2D end) {
        this.end = end;
    }
    public void setLine(final double startX, final double startY, final double endX, final double endY) {
        start = new Point2D(startX, startY);
        end = new Point2D(endX, endY);
    }
}