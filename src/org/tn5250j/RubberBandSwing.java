package org.tn5250j;
/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.4
 * <p>
 * Description:
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.SwingUtilities;

import javafx.geometry.Rectangle2D;

public class RubberBandSwing implements RubberBand {
    private volatile SessionPanelSwing canvas;
    protected volatile Point startPoint;
    protected volatile Point endPoint;
    private volatile boolean eraseSomething = false;
    private volatile boolean isSomethingBounded = false;
    private volatile boolean isDragging = false;

    private class MouseHandler extends MouseAdapter {
        @Override
        public void mouseReleased(final MouseEvent e) {
            isDragging = false;
        }

    }

    private class MouseMotionHandler extends MouseMotionAdapter {

        @Override
        public void mouseDragged(final MouseEvent e) {

            if (!SwingUtilities.isRightMouseButton(e) && canvas.canDrawRubberBand(RubberBandSwing.this)) {
                erase();
                if (!isDragging) {
                    reset();
                    start(canvas.translateStart(e.getPoint()));
                }
                isDragging = true;
                stop(canvas.translateEnd(e.getPoint()));
                notifyRubberBandCanvas();
                draw();
                notifyRubberBandCanvas();
            }
        }

    }

    public boolean isDragging() {
        return isDragging;
    }

    public RubberBandSwing(final SessionPanelSwing c) {
        super();
        setCanvas(c);
        canvas.addMouseListener(new MouseHandler());
        canvas.addMouseMotionListener(new MouseMotionHandler());
    }

    @Override
    public void draw() {
        final Graphics g = canvas.createDrawingGraphics();

        if (g != null) {
            try {
                if (canvas.canDrawRubberBand(this)) {
                    g.setXORMode(canvas.getBackground());

                    final Point start = getStartPoint();
                    final Point end = getEndPoint();
//                    System.out.println("color: " + g.getColor() + ", start: " + start + ", end: " + end);

                    if ((end.x > start.x) && (end.y > start.y)) {
                        g.drawRect(start.x, start.y, end.x - start.x, end.y - start.y);
                    } else if ((end.x < start.x) && (end.y < start.y)) {
                        g.drawRect(end.x, end.y, start.x - end.x, start.y - end.y);
                    } else if ((end.x > start.x) && (end.y < start.y)) {
                        g.drawRect(start.x, end.y, end.x - start.x, start.y - end.y);
                    } else if ((end.x < start.x) && (end.y > start.y)) {
                        g.drawRect(end.x, start.y, start.x - end.x, end.y - start.y);
                    }

                    isSomethingBounded = true;
                    // we have drawn something, set the flag to indicate this
                    setEraseSomething(true);
                }
            } finally {
                g.dispose();
            }
        }
    }

    @Override
    public void erase() {

        if (getEraseSomething()) {
            draw();
            setEraseSomething(false);
        }

    }

    public Point getEndPoint() {

        if (this.endPoint == null) {
            final Point p = canvas.getInitialPoint();
            setEndPoint(p);
        }
        return this.endPoint;
    }

    public Point getStartPoint() {

        if (this.startPoint == null) {
            final Point p = canvas.getInitialPoint();
            setStartPoint(p);
        }
        return this.startPoint;

    }

    protected final boolean getEraseSomething() {
        return this.eraseSomething;
    }

    protected void notifyRubberBandCanvas() {

        int startX, startY, endX, endY;

        if (getStartPoint().x < getEndPoint().x) {
            startX = getStartPoint().x;
            endX = getEndPoint().x;
        } else {
            startX = getEndPoint().x;
            endX = getStartPoint().x;
        }
        if (getStartPoint().y < getEndPoint().y) {
            startY = getStartPoint().y;
            endY = getEndPoint().y;
        } else {
            startY = getEndPoint().y;
            endY = getStartPoint().y;
        }

        canvas.areaBounded(this, startX, startY, endX, endY);

    }

    public final void setCanvas(final SessionPanelSwing c) {
        this.canvas = c;
    }

    protected final void setEndPoint(final Point newValue) {
        this.endPoint = newValue;
    }

    protected final void setEraseSomething(final boolean newValue) {
        this.eraseSomething = newValue;
    }

    protected final void setStartPoint(final Point newValue) {
        this.startPoint = newValue;
        if (startPoint == null)
            endPoint = null;

    }

    protected void start(final Point p) {
        setEndPoint(p);
        setStartPoint(p);
    }

    protected void stop(final Point p) {

        if (p.x < 0) {
            p.x = 0;
        }

        if (p.y < 0) {
            p.y = 0;
        }

        setEndPoint(p);
    }

    @Override
    public Rectangle2D getBoundingArea() {
        if ((getEndPoint().x > getStartPoint().x) && (getEndPoint().y > getStartPoint().y)) {
            return new Rectangle2D(getStartPoint().x, getStartPoint().y, getEndPoint().x - getStartPoint().x, getEndPoint().y - getStartPoint().y);
        } else if ((getEndPoint().x < getStartPoint().x) && (getEndPoint().y < getStartPoint().y)) {
            return new Rectangle2D(getEndPoint().x, getEndPoint().y, getStartPoint().x - getEndPoint().x, getStartPoint().y - getEndPoint().y);
        } else if ((getEndPoint().x > getStartPoint().x) && (getEndPoint().y < getStartPoint().y)) {
            return new Rectangle2D(getStartPoint().x, getEndPoint().y, getEndPoint().x - getStartPoint().x, getStartPoint().y - getEndPoint().y);
        } else { // if ((getEndPoint().x < getStartPoint().x) && (getEndPoint().y > getStartPoint().y)) {
            return new Rectangle2D(getEndPoint().x, getStartPoint().y, getStartPoint().x - getEndPoint().x, getEndPoint().y - getStartPoint().y);
        }
    }

    protected void reset() {
        setStartPoint(null);
        setEndPoint(null);
        isSomethingBounded = false;

    }

    @Override
    public final boolean isAreaSelected() {
        return isSomethingBounded;
    }
}
