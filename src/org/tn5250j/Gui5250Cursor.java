/**
 *
 */
package org.tn5250j;

import javafx.geometry.Rectangle2D;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface Gui5250Cursor {
    void doBlink();
    void setRect(double x, double y, double width, double height);
    Rectangle2D getBounds();
    void setBlinking(boolean b);
    void setSize(int size);
}
