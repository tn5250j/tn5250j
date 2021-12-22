/**
 *
 */
package org.tn5250j;

import javafx.geometry.Rectangle2D;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CrossHairGroup {
    private Line line1 = new Line();
    private Line line2 = new Line();

    private int crossHair = 0;
    private boolean rulerFixed;
    private int crossRow;
    private Rectangle2D crossRect = Rectangle2D.EMPTY;

    public CrossHairGroup() {
        for (final Line line : getComponents()) {
            line.setBlendMode(BlendMode.DIFFERENCE);
            line.setVisible(false);
        }
    }

    public void updateSizes(final int bottomOffset, final double screenWidth, final double screenHeight,
            final double textWidth, final double textHeight) {
        setCrossHair(crossHair);
        // line 1
        line1.setStartX(0);
        line1.setStartY((textHeight * (crossRow + 1)) - bottomOffset);
        line1.setEndX(screenWidth);
        line1.setEndY((textHeight * (crossRow + 1)) - bottomOffset);

        // line 2
        line2.setStartX(crossRect.getMinX());
        line2.setStartY(0);
        line2.setEndX(crossRect.getMinX());
        line2.setEndY(screenHeight - 2 * textHeight);
    }

    public void setCrossHair(final int crH) {
        crossHair = crH;

        line1.setVisible(crossHair == 1 || crossHair == 3);
        line2.setVisible(crossHair == 2 || crossHair == 3);
    }

    public void setCrossRect(final Rectangle2D rect, final int row) {
        if (!rulerFixed) {
            crossRow = row;
            crossRect = rect;
        } else {
            if (crossHair == 0) {
                crossRow = row;
                crossRect = rect;
            }
        }
    }

    public void setRullerFixed(final boolean rullerFixed) {
        this.rulerFixed = rullerFixed;
    }

    public Line[] getComponents() {
        return new Line[] {line1, line2};
    }

    public void setColor(final Color color) {
        for (final Line line : getComponents()) {
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
}
