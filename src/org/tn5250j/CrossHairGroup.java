/**
 *
 */
package org.tn5250j;

import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.shape.Line;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CrossHairGroup {
    private Line line1 = new Line();
    private Line line2 = new Line();
    private Line line31 = new Line();
    private Line line32 = new Line();

    private Dimension2D screen = new Dimension2D(0, 0);
    private Dimension2D text = new Dimension2D(0, 0);
    private int crossHair = 0;
    private int bottomOffset = 0;
    private boolean rulerFixed;

    private Rectangle2D crossRect = Rectangle2D.EMPTY;

    public void applySizes(final int bottomOffset, final double screenWidth, final double screenHeight,
            final double textWidth, final double textHeight) {
        if (screen.getWidth() != screenWidth  || screen.getHeight() != screenHeight
                || text.getWidth() != textWidth || text.getHeight() != textHeight) {
//            screen = new Dimension2D(screenWidth, screenHeight);
//            text = new Dimension2D(textWidth, textHeight);
//            this.bottomOffset = bottomOffset;
//
//                g2.strokeLine(0, (rowHeight * (crossRow + 1)) - bottomOffset,
//                        bi.getWidth(),
//                        (rowHeight * (crossRow + 1)) - bottomOffset);
//                break;
//            case 2:  // vertical
//                g2.strokeLine(crossRect.getMinX(), 0, crossRect.getMinX(),
//                        bi.getHeight() - 2 * rowHeight);
//                break;
//
//            case 3:  // horizontal & vertical
//                g2.strokeLine(0, (rowHeight * (crossRow + 1)) - bottomOffset,
//                        bi.getWidth(),
//                        (rowHeight * (crossRow + 1)) - bottomOffset);
//                g2.strokeLine(crossRect.getMinX(), 0, crossRect.getMinX(),
//                        bi.getHeight() - 2 * rowHeight);
//                break;
        }
    }

    public void setCrossHair(final int crH) {
        if (crossHair != crH) {
            crossHair = crH;

            line1.setVisible(crossHair == 1);
            line2.setVisible(crossHair == 2);
            line31.setVisible(crossHair == 3);
            line32.setVisible(crossHair == 3);
        }
    }
}
