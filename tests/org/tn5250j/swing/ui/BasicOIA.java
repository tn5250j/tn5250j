package org.tn5250j.swing.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;

import org.tn5250j.event.ScreenListener;
import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenOIA;
import org.tn5250j.gui.UiUtils;

/**
 * For testing purpose
 */
public class BasicOIA extends BasicSubUI implements ScreenListener, ScreenOIAListener {
    public BasicOIA(final ScreenOIA oia) {
        this.oia = oia;
    }

    @Override
    public void setBounds(final int x, final int y, final int width, final int height) {
        super.setBounds(x, y, width, height);
        this.clearLayout();
    }

    @Override
    public void setFont(final Font font, final int charWidth, final int charHeight) {
        super.setFont(font, charWidth, charHeight);
        this.clearLayout();
    }

    @Override
    public void onScreenSizeChanged(final int rows, final int cols) {

    }

    @Override
    public void onScreenChanged(final int inUpdate, final int startRow, final int startCol, final int endRow, final int endCol) {
        if ((inUpdate == 3) && (locationRectangle != null))
            addDirtyRectangle(locationRectangle);
    }

    @Override
    public void onOIAChanged(final ScreenOIA oia, final int change) {
        switch (change) {
            case OIA_CHANGED_BELL:
                ringAudibleBell();
                break;
            case OIA_CHANGED_CLEAR_SCREEN:
                break;
            case OIA_CHANGED_INPUTINHIBITED:
                addDirtyRectangle(inhibitedRectangle);
                break;
            case OIA_CHANGED_INSERT_MODE:
                break;
            case OIA_CHANGED_KEYBOARD_LOCKED:
                setKeyboardLocked(oia.isKeyBoardLocked());
                break;
            case OIA_CHANGED_KEYS_BUFFERED:
                break;
            case OIA_CHANGED_MESSAGELIGHT:
                break;
            case OIA_CHANGED_SCRIPT:
                break;
            default:
                // Do nothing
        }
    }

    @Override
    public void install() {
        this.oia.addOIAListener(this);
        this.oia.getSource().addScreenListener(this);
    }

    @Override
    public void uninstall() {
        this.oia.removeOIAListener(this);
        this.oia.getSource().removeScreenListener(this);

        this.oia = null;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(this.columnWidth, this.rowHeight);
    }

    @Override
    public void paintComponent(final Graphics g) {
        doLayout();

        final Rectangle clip = g.getClipBounds();

        paintRuler(g, clip);
        paintLocation(g, clip);
        paintInhibited(g, clip);
    }

    private void paintRuler(final Graphics g, final Rectangle clip) {
        g.setColor(Color.white);
        g.drawLine(0, 0, this.width, 0);
    }

    private void paintLocation(final Graphics g, final Rectangle clip) {
        if (locationRectangle.intersects(clip)) {
            g.setColor(BasicTerminalUI.DFT_FOREGROUND);

            final Screen5250 s = oia.getSource();
            final int col = s.getCurrentCol();
            final int row = s.getCurrentRow();
            final int cy = (locationRectangle.y + rowHeight - (metrics.getDescent() + metrics.getLeading()));

            g.drawString(col + "/" + row, locationRectangle.x, cy);
        }
    }

    private void paintInhibited(final Graphics g, final Rectangle clip) {

        if (inhibitedRectangle.intersects(clip)) {
            if (oia.getInhibitedText() != null)
                System.out.println(oia.getInhibitedText());

            System.out.println("xsystem " + oia.getLevel() + "," + oia.getInputInhibited());

            g.setColor(BasicTerminalUI.DFT_FOREGROUND);

            final int cy = (inhibitedRectangle.y + rowHeight - (metrics.getDescent() + metrics.getLeading()));
            final int value = oia.getInputInhibited();
            final String stext = oia.getInhibitedText();

            g.setColor(Color.black);
            g.fillRect(inhibitedRectangle.x, inhibitedRectangle.x, inhibitedRectangle.width,
                    inhibitedRectangle.height);

            switch (oia.getLevel()) {
                case 1:
                    if (value == 1) {

                        g.setColor(Color.white);
                        if (stext != null) {
                            g.drawChars(stext.toCharArray(), 0,
                                    oia.getInhibitedText().length(), inhibitedRectangle.x, cy);
                        } else {
                            g.drawChars("X - System".toCharArray(), 0,
                                    "X - System".length(), inhibitedRectangle.x, cy);

                        }
                    }
                    break;
            }

        }
    }

    public final void setPosition(final int row, final int column) {
    }

    transient ScreenOIA oia;
    private transient Rectangle locationRectangle;
    private transient Rectangle inhibitedRectangle;

    private void clearLayout() {
        locationRectangle = null;
        inhibitedRectangle = null;
    }

    private void doLayout() {
        if (locationRectangle == null) {
            final Rectangle bounds = this.getBounds();

            // Location rectangle
            final int w = 6 * this.columnWidth;
            locationRectangle = new Rectangle(bounds.width - w, 0, w, this.rowHeight);
            inhibitedRectangle = new Rectangle(10, 0, 25 * this.columnWidth, this.rowHeight);
        }
    }

    private void ringAudibleBell() {
        UiUtils.beep();
    }

    private void setKeyboardLocked(final boolean locked) {

    }
}
