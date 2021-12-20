/**
 *
 */
package org.tn5250j;

import javafx.geometry.Rectangle2D;

/**
 * Should be removed after full implemented FX
 *
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public interface RubberBand {
    boolean isAreaSelected();
    void erase();
    Rectangle2D getBoundingArea();
    void draw();
    void reset();
}
