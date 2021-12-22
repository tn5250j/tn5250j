/**
 *
 */
package org.tn5250j;

import org.tn5250j.tools.CursorService;

import javafx.geometry.Rectangle2D;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CompoundCursor {
    private final Line line1 = new Line();
    private final Line line2 = new Line();
    private final Rectangle cursor = new Rectangle();

    private int crossHair = 0;
    private boolean rulerFixed;
    private int crossRow;
    private Rectangle2D crossRect = Rectangle2D.EMPTY;
    private Rectangle2D cursorArea = Rectangle2D.EMPTY;
    protected int cursorSize = 0;
    private Runnable blinkListener = this::nextBlink;
    private double screenHeight;
    private double screenWidth;
    private double columnWidth;
    private double rowHeight;
    private int bottomOffset;
    private boolean insertMode;

    public CompoundCursor() {
        for (final Shape line : getComponents()) {
            line.setBlendMode(BlendMode.DIFFERENCE);
            line.setVisible(false);
        }
        cursor.setVisible(true);
    }

    public void setCrossHair(final int crH) {
        crossHair = crH;
        setBlinkTo(true);
    }

    public void setRullerFixed(final boolean rullerFixed) {
        this.rulerFixed = rullerFixed;
    }

    public Shape[] getComponents() {
        return new Shape[] {line1, line2, cursor};
    }

    public void setColor(final Color color) {
        for (final Shape line : getComponents()) {
            line.setFill(color);
            line.setStroke(color);
        }
    }

    public void shift() {
        int chr = this.crossHair + 1;
        if (chr > 3)
            chr = 0;

        setCrossHair(chr);
    }

    /**
     * @param blinking state
     */
    public void setBlinking(final boolean blinking) {
        CursorService.getInstance().removeCursor(blinkListener);
        if (blinking) {
            CursorService.getInstance().addCursor(blinkListener);
        }
    }

    private void setCursorArea(final Rectangle2D cursorArea) {
        cursor.relocate(cursorArea.getMinX(), cursorArea.getMinY());
        cursor.setWidth(cursorArea.getWidth());
        cursor.setHeight(cursorArea.getHeight());
    }

    private void nextBlink() {
        setBlinkTo(!cursor.isVisible());
    }

    private void setBlinkTo(final boolean b) {
        cursor.setVisible(b);
        line1.setVisible(b && (crossHair == 1 || crossHair == 3));
        line2.setVisible(b && (crossHair == 2 || crossHair == 3));
    }

    public Color getColor() {
        return (Color) cursor.getFill();
    }

    public void setSize(final int size) {
        this.cursorSize = size;
    }

    public void setScreenSize(final double w, final double h) {
        this.screenWidth = w;
        this.screenHeight = h;
    }

    public void setColumnBounds(final double w, final double h) {
        this.columnWidth = w;
        this.rowHeight = h;
    }

    public void setBottomOffset(final int botOffset) {
        this.bottomOffset = botOffset;
    }

    public int getBottomOffset() {
        return bottomOffset;
    }

    public void setInsertMode(final boolean insertMode) {
        this.insertMode = insertMode;
    }

    public void recalculateSizes(final int row, final int col) {

        switch (cursorSize) {
            case 0:
                cursorArea = new Rectangle2D(
                        columnWidth * (col),
                        (rowHeight * (row + 1)) - bottomOffset,
                        columnWidth,
                        1
                );
                break;
            case 1:
                cursorArea = new Rectangle2D(
                        columnWidth * (col),
                        (rowHeight * (row + 1) - rowHeight / 2),
                        columnWidth,
                        (rowHeight / 2) - bottomOffset
                );
                break;
            case 2:
                cursorArea = new Rectangle2D(
                        columnWidth * (col),
                        (rowHeight * row),
                        columnWidth,
                        rowHeight - bottomOffset
                );
                break;
        }

        if (insertMode && cursorSize != 1) {
            cursorArea = new Rectangle2D(
                    columnWidth * (col),
                    (rowHeight * (row + 1) - rowHeight / 2),
                    columnWidth,
                    (rowHeight / 2) - bottomOffset
            );
        }

        if (!rulerFixed || crossHair == 0) {
            crossRow = row;
            crossRect = cursorArea;
        } else {
            crossRow = 0;
            crossRect = Rectangle2D.EMPTY;
        }

        // line 1
        line1.setStartX(0);
        line1.setStartY((rowHeight * (crossRow + 1)) - bottomOffset);
        line1.setEndX(screenWidth);
        line1.setEndY((rowHeight * (crossRow + 1)) - bottomOffset);

        // line 2
        line2.setStartX(crossRect.getMinX());
        line2.setStartY(0);
        line2.setEndX(crossRect.getMinX());
        line2.setEndY(screenHeight - 2 * rowHeight);

        setCrossHair(crossHair);
        setCursorArea(cursorArea);
    }
}
