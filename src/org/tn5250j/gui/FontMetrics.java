/**
 *
 */
package org.tn5250j.gui;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class FontMetrics {
    private final double leading;
    private final double ascent;
    private final double descent;

    public FontMetrics(final double ascent, final double descent, final double leading) {
        super();
        this.ascent = ascent;
        this.descent = descent;
        this.leading = leading;
    }

    /**
     * @return font ascent.
     */
    public double getAscent() {
        return ascent;
    }
    /**
     * @return font decent.
     */
    public double getDescent() {
        return descent;
    }
    /**
     * @return font leading
     */
    public double getLeading() {
        return leading;
    }

    public static FontMetrics deriveFrom(final Font font) {
        final Text text = new Text("W");
        text.setFont(font);
        final double ascent = text.getBoundsInLocal().getHeight();

        text.setText("Wg");
        final double descent = text.getBoundsInLocal().getHeight() - ascent;
        //for English is zero
        final double leading = 0.;

        return new FontMetrics(ascent, descent, leading);
    }
}
