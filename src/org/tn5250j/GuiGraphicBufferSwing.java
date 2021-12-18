/**
 * Title: GuiGraphicBuffer.java
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
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
package org.tn5250j;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.image.BufferedImage;

import javax.swing.SwingUtilities;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenOIA;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.geometry.Dimension2D;

public class GuiGraphicBufferSwing extends AbstractGuiGraphicBuffer implements ActionListener {

    private BufferedImage bi;
    private final Object lock = new Object();
    private Graphics2D gg2d;

    private javax.swing.Timer blinker;

    private final TN5250jLogger log = TN5250jLogFactory.getLogger("GFX");

    public GuiGraphicBufferSwing(final Screen5250 screen, final SessionGui gui, final SessionConfig config) {
        super(screen, gui, config);
    }

    @Override
    protected Dimension2D getCellBounds() {
        final FontRenderContext frc = new FontRenderContext(getFont().getTransform(),
                true, true);
        final LineMetrics lm = getFont().getLineMetrics("Wy", frc);
        final double w = getFont().getStringBounds("W", frc).getWidth() + 1;
        final double h = (getFont().getStringBounds("g", frc).getHeight()
                + lm.getDescent() + lm.getLeading());
        return new Dimension2D(w, h);
    }

    /**
     * This is for blinking cursor but should be moved out
     */
    @Override
    public void actionPerformed(final ActionEvent actionevent) {
        if (actionevent.getSource() instanceof javax.swing.Timer) {

            //         if (!cursorActive)
            //            return;
            //
            //         if (cursorShown)
            //            setCursorOff();
            //         else
            //            setCursorOn();
            if (screen.isCursorActive())
                screen.setCursorActive(false);
            else
                screen.setCursorActive(true);
        }
    }

    public boolean isBlinkCursor() {

        return blinker != null;

    }

    @Override
    public void resize(final int width, final int height) {

        if (bi == null || bi.getWidth() != width || bi.getHeight() != height) {
            //         synchronized (lock) {
            bi = null;
            bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            // tell waiting threads to wake up
            //            lock.notifyAll();
            //         }
        }

    }

    @Override
    protected void refreshView() {
        ((Component) gui).validate();
        ((Component) gui).repaint();
    }

    @Override
    protected void setCursorBlinking(final boolean blinking) {
        if (blinking) {
            if (blinker == null) {
                blinker = new javax.swing.Timer(500, this);
                blinker.start();
            }
        } else {
            if (blinker != null) {
                blinker.stop();
                blinker = null;
            }
        }
    }

    /**
     * Convinience method to resize the screen area such as when the parent
     * frame is resized.
     *
     * @param width screen width.
     * @param height screen height.
     * @param updateFont font is updated.
     */
    @Override
    protected void resizeScreenArea(final int width, final int height, final boolean updateFont) {
        if (bi == null) {
            return;
        }

        super.resizeScreenArea(width, height, updateFont);
    }

    public BufferedImage getImageBuffer(final int width, final int height) {


        final int width2 = columnWidth * screen.getColumns();
        final int height2 = rowHeight * (screen.getRows() + 2);
        //      synchronized (lock) {
        if (bi == null || bi.getWidth() != width2 || bi.getHeight() != height2) {
            // allocate a buffer Image with appropriate size
            bi = new BufferedImage(width2, height2, BufferedImage.TYPE_INT_RGB);
        }
        //         // tell waiting threads to wake up
        //         lock.notifyAll();
        //      }
        recalculateOIASizes();
        drawOIA();
        return bi;
    }

    /**
     * Draw the operator information area
     */
    @Override
    protected void drawOIA() {
        final Graphics2D g2d = getDrawingArea();

        if (antialiased)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
        g2d.setFont(font);


        g2d.setColor(UiUtils.toAwtColor(colorBg));
        g2d.fillRect(0, 0, bi.getWidth(null), bi.getHeight(null));

        g2d.setColor(UiUtils.toAwtColor(colorBlue));
        g2d.draw(separatorLine);
        gg2d = g2d;
    }

    @Override
    protected void drawCursor(final int row, final int col, final int botOffset) {
        Graphics2D g2 = getDrawingArea();
        g2.setColor(UiUtils.toAwtColor(colorCursor));
        g2.setXORMode(UiUtils.toAwtColor(colorBg));

        g2.fill(cursor);

        updateImage(cursor.getBounds());

        switch (crossHair) {
            case 1:  // horizontal
                g2.drawLine(0, (rowHeight * (crossRow + 1)) - botOffset,
                        bi.getWidth(null),
                        (rowHeight * (crossRow + 1)) - botOffset);
                updateImage(0, rowHeight * (crossRow + 1) - botOffset,
                        bi.getWidth(null), 1);
                break;
            case 2:  // vertical
                g2.drawLine(crossRect.x, 0, crossRect.x, bi.getHeight(null) - rowHeight - rowHeight);
                updateImage(crossRect.x, 0, 1, bi.getHeight(null) - rowHeight - rowHeight);
                break;

            case 3:  // horizontal & vertical
                g2.drawLine(0, (rowHeight * (crossRow + 1)) - botOffset,
                        bi.getWidth(null),
                        (rowHeight * (crossRow + 1)) - botOffset);
                g2.drawLine(crossRect.x, 0, crossRect.x, bi.getHeight(null) - rowHeight - rowHeight);
                updateImage(0, rowHeight * (crossRow + 1) - botOffset,
                        bi.getWidth(null), 1);
                updateImage(crossRect.x, 0, 1, bi.getHeight(null) - rowHeight - rowHeight);
                break;
        }

        g2.dispose();
        g2 = getWritingArea(font);
        g2.setPaint(UiUtils.toAwtColor(colorBg));

        g2.fill(pArea);
        g2.setColor(UiUtils.toAwtColor(colorWhite));

        g2.drawString((row + 1) + "/" + (col + 1)
                , (float) pArea.getX(),
                (float) pArea.getY() + rowHeight);
        updateImage(pArea.getBounds());
        g2.dispose();

    }

    private void drawScriptRunning(final Color color) {

        Graphics2D g2d;

        // get ourselves a global pointer to the graphics
        g2d = (Graphics2D) bi.getGraphics();

        g2d.setColor(color);

        // set the points for the polygon
        final int[] xs = {(int) scriptArea.getX(),
                (int) scriptArea.getX(),
                (int) scriptArea.getX() + (int) (scriptArea.getWidth())};
        final int[] ys = {(int) scriptArea.getY(),
                (int) scriptArea.getY() + (int) scriptArea.getHeight(),
                (int) scriptArea.getY() + (int) (scriptArea.getHeight() / 2)};

        // now lets draw it
        g2d.fillPolygon(xs, ys, 3);
        g2d.setClip(scriptArea);

        // get rid of the pointers
        g2d.dispose();
    }

    private void eraseScriptRunning(final Color color) {

        Graphics2D g2d;

        // get ourselves a global pointer to the graphics
        g2d = (Graphics2D) bi.getGraphics();

        g2d.setColor(color);
        g2d.fill(scriptArea);
        g2d.dispose();
    }

    /**
     * Returns a pointer to the graphics area that we can draw on
     */
    public Graphics2D getDrawingArea() {

        //      try {
        //         synchronized (lock) {
        // wait until there is something to read
        //            while (bi == null) {
        //               log.debug(" bi = null ");
        //               lock.wait();
        //            }
        // we have the lock and state we're seeking
        // check for selected area and erase it before updating screen
        //         if (gui.rubberband != null && gui.rubberband.isAreaSelected()) {
        //            gui.rubberband.erase();
        //         }

        Graphics2D g2;

        g2 = bi.createGraphics();
        // tell waiting threads to wake up
        //            lock.notifyAll();
        return g2;
        //         }
        //      }
        //      catch (InterruptedException ie) {
        //         log.warn("getDrawingarea : " + ie.getMessage());
        //         return null;
        //      }
    }

    protected void updateImage(int x, int y, int width, int height) {


        // check for selected area and erase it before updating screen
        if (gui.getRubberband() != null && gui.getRubberband().isAreaSelected()) {
            gui.getRubberband().erase();
        }

        gg2d.setClip(x, y, width, height);
        //		if (!cursorActive && x + width <= bi.getWidth(null)
        //				&& y + height <= (bi.getHeight(null) - fmWidth)) {
        //			paintComponent2(biGraphics2d);
        //		}

        //      if (tileimage != null) {
        //
        //         AlphaComposite ac =
        // AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
        //         g2d.setComposite(ac);
        //         g2d.drawImage(tileimage, null, null);
        //      }

        // LDC - WVL : 08/09/2003 : TR.000358
        // TN5250j overpaints superimposed components
        // as swing doesn't support overlay detection when painting a component
        // we have to adhere to swing's paint request and use dirty rectangle
        // marking
        // instead of off-thread painting
        // So we replaced the complete block underneath by 1 repaint request

        // fix for jdk1.4 - found this while testing under jdk1.4
        //   if the height and or the width are equal to zero we skip the
        //   the updating of the image.
        //      if (gui.isVisible() && height > 0 && width > 0) {
        //         bi.drawImageBuffer(gg2d,x,y,width,height);
        //      }
        //         if (gui.isVisible()) {
        //            if (height > 0 && width > 0) {

        // We now redraw the selected area rectangle.
        if (gui.getRubberband() != null && gui.getRubberband().isAreaSelected()) {
            gui.getRubberband().draw();
        }

        if (x == 0)
            width += offLeft;
        else
            x += offLeft;
        if (y == 0)
            height += offTop;
        else
            y += offTop;

        final int heightf = height;
        final int widthf = width;
        final int xf = x;
        final int yf = y;
        try {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ((Component) gui).repaint(xf, yf, widthf, heightf);
                }
            });

        } catch (final Exception exc) {
            log.warn("setStatus(ON) " + exc.getMessage());

        }

    }

    private void updateImage(final Rectangle r) {
        updateImage(r.x, r.y, r.width, r.height);
    }

    public synchronized void drawImageBuffer(final Graphics2D gg2d) {

        /**
         * @todo this is a hack and should be fixed at the root of the problem
         */
        if (gg2d == null) {
            log.debug(" we got a null graphic object ");
            return;
        }

        //      synchronized (lock) {

        gg2d.drawImage(bi, null, offLeft, offTop);
        // tell waiting threads to wake up
        //         lock.notifyAll();
        //      }


    }

    /**
     * Returns a pointer to the graphics area that we can write on
     */
    private Graphics2D getWritingArea(final Font font) {

        Graphics2D g2;
        // we could be in the middle of creating the graphics because of the
        //    threads, resizing etc....   so lets wait until we have one.
        //    If this causes problems we should implement a thresh-hold of sorts
        //    to keep an infinate loop from occurring.  So far not problems
        //      try {
        //         synchronized (lock) {
        // wait until there is something to read
        //            while (bi == null) {
        //               log.debug( " bi = null wa ");
        //               lock.wait();
        //            }
        // we have the lock and state we're seeking

        g2 = bi.createGraphics();

        if (g2 != null) {
            if (antialiased)
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                    RenderingHints.VALUE_COLOR_RENDER_SPEED);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_SPEED);
            g2.setFont(font);
        }

        // tell waiting threads to wake up
        //            lock.notifyAll();
        return g2;

        //         }
        //      }
        //      catch (InterruptedException ie) {
        //         log.warn("getWritingarea : " + ie.getMessage());
        //         return null;
        //      }
    }

    private LineMetrics getLineMetrics() {
        final FontRenderContext frc = new FontRenderContext(getFont().getTransform(),
                true, true);
        return getFont().getLineMetrics("Wy", frc);
    }

    private void setStatus(final ScreenOIA oia) {

        final int attr = oia.getLevel();
        final int value = oia.getInputInhibited();
        final String s = oia.getInhibitedText();
        final Graphics2D g2d = getWritingArea(font);
        //      log.info(attr + ", " + value + ", " + s);
        if (g2d == null)
            return;

        try {
            g2d.setColor(UiUtils.toAwtColor(colorBg));
            g2d.fill(sArea);

            final LineMetrics lm = getLineMetrics();
            final float Y = ((int) sArea.getY() + rowHeight) - (lm.getLeading() + lm.getDescent());

            switch (attr) {

                case ScreenOIA.OIA_LEVEL_INPUT_INHIBITED:
                    if (value == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT) {
                        g2d.setColor(UiUtils.toAwtColor(colorWhite));

                        if (s != null)
                            g2d.drawString(s, (float) sArea.getX(), Y);
                        else
                            g2d.drawString(xSystem, (float) sArea.getX(), Y);
                    }
                    break;
                case ScreenOIA.OIA_LEVEL_INPUT_ERROR:
                    if (value == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT) {
                        g2d.setColor(UiUtils.toAwtColor(colorRed));

                        if (s != null)
                            g2d.drawString(s, (float) sArea.getX(), Y);
                        else
                            g2d.drawString(xError, (float) sArea.getX(), Y);

                    }
                    break;

            }
            updateImage(sArea.getBounds());
            g2d.dispose();
        } catch (final Exception e) {

            log.warn(" gui graphics setStatus " + e.getMessage());

        }
    }

    protected final void drawChar(final Graphics2D g, final int pos, final int row, final int col) {
        Rectangle csArea = new Rectangle();
        final char sChar[] = new char[1];
        final int attr = updateRect.attr[pos];
        sChar[0] = updateRect.text[pos];
        final boolean attributePlace = updateRect.isAttr[pos] == 0 ? false : true;
        final int whichGui = updateRect.graphic[pos];
        final boolean useGui = whichGui == 0 ? false : true;

        csArea = modelToView(row, col, csArea);

        final LineMetrics lm = getLineMetrics();
        final int x = csArea.x;
        final int y = csArea.y;
        final int cy = (int) (y + rowHeight - (lm.getDescent() + lm.getLeading()));

        if (showHex && attributePlace) {
            final Font f = g.getFont();

            final Font k = f.deriveFont(f.getSize2D() / 2);
            g.setFont(k);
            g.setColor(UiUtils.toAwtColor(colorHexAttr));
            final char[] a = Integer.toHexString(attr).toCharArray();
            g.drawChars(a, 0, 1, x, y + (rowHeight / 2));
            g.drawChars(a, 1, 1, x + (columnWidth / 2),
                    (int) (y + rowHeight - (lm.getDescent() + lm.getLeading()) - 2));
            g.setFont(f);
        }

        if (!nonDisplay && !attributePlace) {

            if (!useGui) {
                g.setColor(UiUtils.toAwtColor(bg));
                g.fill(csArea);
            } else {

                if (bg == colorBg && whichGui >= TN5250jConstants.FIELD_LEFT && whichGui <= TN5250jConstants.FIELD_ONE)
                    g.setColor(UiUtils.toAwtColor(colorGUIField));
                else
                    g.setColor(UiUtils.toAwtColor(bg));

                g.fill(csArea);

            }

            if (useGui && (whichGui < TN5250jConstants.FIELD_LEFT)) {

                g.setColor(UiUtils.toAwtColor(fg));

                switch (whichGui) {

                    case TN5250jConstants.UPPER_LEFT:
                        if (sChar[0] == '.') {
                            if (screen.isUsingGuiInterface()) {
                                GUIGraphicsUtils.drawWinUpperLeft(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        UiUtils.toAwtColor(colorBlue),
                                        x, y, columnWidth, rowHeight);

                            } else {

                                GUIGraphicsUtils.drawWinUpperLeft(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        UiUtils.toAwtColor(fg),
                                        x, y, columnWidth, rowHeight);

                            }
                        }
                        break;
                    case TN5250jConstants.UPPER:
                        if (sChar[0] == '.') {

                            if (screen.isUsingGuiInterface()) {
                                GUIGraphicsUtils.drawWinUpper(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        UiUtils.toAwtColor(colorBlue),
                                        x, y, columnWidth, rowHeight);


                            } else {

                                GUIGraphicsUtils.drawWinUpper(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        UiUtils.toAwtColor(fg),
                                        x, y, columnWidth, rowHeight);
                            }
                        }
                        break;
                    case TN5250jConstants.UPPER_RIGHT:
                        if (sChar[0] == '.') {
                            if (screen.isUsingGuiInterface()) {

                                GUIGraphicsUtils.drawWinUpperRight(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        UiUtils.toAwtColor(colorBlue),
                                        x, y, columnWidth, rowHeight);


                            } else {

                                GUIGraphicsUtils.drawWinUpperRight(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        UiUtils.toAwtColor(fg),
                                        x, y, columnWidth, rowHeight);

                            }
                        }
                        break;
                    case TN5250jConstants.GUI_LEFT:
                        if (sChar[0] == ':') {
                            if (screen.isUsingGuiInterface()) {
                                GUIGraphicsUtils.drawWinLeft(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        UiUtils.toAwtColor(bg),
                                        x, y, columnWidth, rowHeight);


                            } else {

                                GUIGraphicsUtils.drawWinLeft(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        UiUtils.toAwtColor(fg),
                                        x, y, columnWidth, rowHeight);

                                g.drawLine(x + columnWidth / 2,
                                        y,
                                        x + columnWidth / 2,
                                        y + rowHeight);
                            }
                        }
                        break;
                    case TN5250jConstants.GUI_RIGHT:
                        if (sChar[0] == ':') {
                            if (screen.isUsingGuiInterface()) {
                                GUIGraphicsUtils.drawWinRight(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        UiUtils.toAwtColor(bg),
                                        x, y, columnWidth, rowHeight);


                            } else {
                                GUIGraphicsUtils.drawWinRight(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        UiUtils.toAwtColor(fg),
                                        x, y, columnWidth, rowHeight);

                            }
                        }
                        break;
                    case TN5250jConstants.LOWER_LEFT:
                        if (sChar[0] == ':') {

                            if (screen.isUsingGuiInterface()) {

                                GUIGraphicsUtils.drawWinLowerLeft(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        UiUtils.toAwtColor(bg),
                                        x, y, columnWidth, rowHeight);


                            } else {

                                GUIGraphicsUtils.drawWinLowerLeft(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        UiUtils.toAwtColor(fg),
                                        x, y, columnWidth, rowHeight);
                            }
                        }
                        break;
                    case TN5250jConstants.BOTTOM:
                        if (sChar[0] == '.') {

                            if (screen.isUsingGuiInterface()) {


                                GUIGraphicsUtils.drawWinBottom(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        UiUtils.toAwtColor(bg),
                                        x, y, columnWidth, rowHeight);


                            } else {

                                GUIGraphicsUtils.drawWinBottom(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        UiUtils.toAwtColor(fg),
                                        x, y, columnWidth, rowHeight);
                            }
                        }
                        break;

                    case TN5250jConstants.LOWER_RIGHT:
                        if (sChar[0] == ':') {
                            if (screen.isUsingGuiInterface()) {

                                GUIGraphicsUtils.drawWinLowerRight(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        UiUtils.toAwtColor(bg),
                                        x, y, columnWidth, rowHeight);

                            } else {

                                GUIGraphicsUtils.drawWinLowerRight(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        UiUtils.toAwtColor(fg),
                                        x, y, columnWidth, rowHeight);

                            }
                        }
                        break;

                }
            } else {
                if (sChar[0] != 0x0) {
                    // use this until we define colors for gui stuff
                    if ((useGui && whichGui < TN5250jConstants.BUTTON_LEFT) && (fg == colorGUIField))

                        g.setColor(Color.black);
                    else
                        g.setColor(UiUtils.toAwtColor(fg));

                    try {
                        if (useGui)

                            if (sChar[0] == 0x1C)
                                g.drawChars(dupChar, 0, 1, x + 1, cy - 2);
                            else
                                g.drawChars(sChar, 0, 1, x + 1, cy - 2);
                        else if (sChar[0] == 0x1C)
                            g.drawChars(dupChar, 0, 1, x, cy - 2);
                        else
                            g.drawChars(sChar, 0, 1, x, cy - 2);
                    } catch (final IllegalArgumentException iae) {
                        System.out.println(" drawChar iae " + iae.getMessage());

                    }
                }
                if (underLine) {

                    if (!useGui || cfg_guiShowUnderline) {
                        g.setColor(UiUtils.toAwtColor(fg));
                        g.drawLine(x, (int) (y + (rowHeight - (lm.getLeading() + lm.getDescent()))), (x + columnWidth), (int) (y + (rowHeight - (lm.getLeading() + lm.getDescent()))));

                    }
                }

                if (colSep) {
                    g.setColor(UiUtils.toAwtColor(colorSep));
                    switch (colSepLine) {
                        case Line:  // line
                            g.drawLine(x, y, x, y + rowHeight - 1);
                            g.drawLine(x + columnWidth - 1, y, x + columnWidth - 1, y + rowHeight);
                            break;
                        case ShortLine:  // short line
                            g.drawLine(x, y + rowHeight - (int) lm.getLeading() - 4, x, y + rowHeight);
                            g.drawLine(x + columnWidth - 1, y + rowHeight - (int) lm.getLeading() - 4, x + columnWidth - 1, y + rowHeight);
                            break;
                        case Dot:  // dot
                            g.drawLine(x, y + rowHeight - (int) lm.getLeading() - 3, x, y + rowHeight - (int) lm.getLeading() - 4);
                            g.drawLine(x + columnWidth - 1, y + rowHeight - (int) lm.getLeading() - 3, x + columnWidth - 1, y + rowHeight - (int) lm.getLeading() - 4);
                            break;
                        case Hide:  // hide
                            break;
                    }
                }
            }
        }

        if (useGui & (whichGui >= TN5250jConstants.FIELD_LEFT)) {

            switch (whichGui) {

                case TN5250jConstants.FIELD_LEFT:
                    GUIGraphicsUtils.draw3DLeft(g, GUIGraphicsUtils.INSET, x, y,
                            columnWidth, rowHeight);

                    break;
                case TN5250jConstants.FIELD_MIDDLE:
                    GUIGraphicsUtils.draw3DMiddle(g, GUIGraphicsUtils.INSET, x, y,
                            columnWidth, rowHeight);
                    break;
                case TN5250jConstants.FIELD_RIGHT:
                    GUIGraphicsUtils.draw3DRight(g, GUIGraphicsUtils.INSET, x, y,
                            columnWidth, rowHeight);
                    break;

                case TN5250jConstants.FIELD_ONE:
                    GUIGraphicsUtils.draw3DOne(g, GUIGraphicsUtils.INSET, x, y,
                            columnWidth, rowHeight);

                    break;

                case TN5250jConstants.BUTTON_LEFT:
                case TN5250jConstants.BUTTON_LEFT_UP:
                case TN5250jConstants.BUTTON_LEFT_DN:
                case TN5250jConstants.BUTTON_LEFT_EB:

                    GUIGraphicsUtils.draw3DLeft(g, GUIGraphicsUtils.RAISED, x, y,
                            columnWidth, rowHeight);

                    break;

                case TN5250jConstants.BUTTON_MIDDLE:
                case TN5250jConstants.BUTTON_MIDDLE_UP:
                case TN5250jConstants.BUTTON_MIDDLE_DN:
                case TN5250jConstants.BUTTON_MIDDLE_EB:

                    GUIGraphicsUtils.draw3DMiddle(g, GUIGraphicsUtils.RAISED, x, y,
                            columnWidth, rowHeight);
                    break;

                case TN5250jConstants.BUTTON_RIGHT:
                case TN5250jConstants.BUTTON_RIGHT_UP:
                case TN5250jConstants.BUTTON_RIGHT_DN:
                case TN5250jConstants.BUTTON_RIGHT_EB:

                    GUIGraphicsUtils.draw3DRight(g, GUIGraphicsUtils.RAISED, x, y,
                            columnWidth, rowHeight);

                    break;

                // scroll bar
                case TN5250jConstants.BUTTON_SB_UP:
                    GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.RAISED, 1, x, y,
                            columnWidth, rowHeight,
                            UiUtils.toAwtColor(colorWhite), UiUtils.toAwtColor(colorBg));
                    break;

                // scroll bar
                case TN5250jConstants.BUTTON_SB_DN:

                    GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.RAISED, 2, x, y,
                            columnWidth, rowHeight,
                            UiUtils.toAwtColor(colorWhite), UiUtils.toAwtColor(colorBg));


                    break;
                // scroll bar
                case TN5250jConstants.BUTTON_SB_GUIDE:

                    GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.INSET, 0, x, y,
                            columnWidth, rowHeight,
                            UiUtils.toAwtColor(colorWhite), UiUtils.toAwtColor(colorBg));


                    break;

                // scroll bar
                case TN5250jConstants.BUTTON_SB_THUMB:

                    GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.INSET, 3, x, y,
                            columnWidth, rowHeight,
                            UiUtils.toAwtColor(colorWhite), UiUtils.toAwtColor(colorBg));


                    break;

            }
        }

    }

    @Override
    public void onScreenSizeChanged(final int rows, final int cols) {
        log.info("screen size change");
        gui.resizeMe();
    }

    @Override
    public void onScreenChanged(final int which, final int sr, final int sc, final int er, final int ec) {
        if (which == 3 || which == 4) {
            final int botOffset = recalculateCursorSizes(sr, sc);
            drawCursor(sr, sc, botOffset);
            return;
        }

        if (hotSpots) screen.checkHotSpots();

        updateRect = new Data(sr, sc, er, ec);
        drawScreen(sr, sc, er, ec);
    }

    @Override
    protected void drawScreen(int sr, final int sc, final int er, final int ec) {
        final Rectangle clipper = new Rectangle();
        clipper.x = sc * columnWidth;
        clipper.y = sr * rowHeight;
        clipper.width = ((ec - sc) + 1) * columnWidth;
        clipper.height = ((er - sr) + 1) * rowHeight;

        gg2d.setClip(clipper.getBounds());

        gg2d.setColor(UiUtils.toAwtColor(colorBg));

        gg2d.fillRect(clipper.x, clipper.y, clipper.width, clipper.height);

        int pos = 0;
        while (sr <= er) {
            int cols = ec - sc;
            int lc = sc;
            while (cols-- >= 0) {
                if (sc + cols <= ec) {
                    setDrawAttr(pos);
                    drawChar(gg2d, pos, sr, lc);
                    pos++;
                    lc++;
                }
            }
            sr++;
        }
        updateImage(clipper);
    }

    @Override
    public void onOIAChanged(final ScreenOIA changedOIA, final int change) {
        final LineMetrics lm = getLineMetrics();

        switch (changedOIA.getLevel()) {

            case ScreenOIA.OIA_LEVEL_KEYS_BUFFERED:
                if (changedOIA.isKeysBuffered()) {
                    final Graphics2D g2d = getWritingArea(font);
                    final float Y = (rowHeight * (screen.getRows() + 2))
                            - (lm.getLeading() + lm.getDescent());
                    g2d.setColor(UiUtils.toAwtColor(colorYellow));
                    g2d.drawString("KB", (float) kbArea.getX(), Y);

                    updateImage(kbArea.getBounds());
                    g2d.dispose();
                } else {
                    final Graphics2D g2d = getWritingArea(font);
                    g2d.setColor(UiUtils.toAwtColor(colorBg));
                    g2d.fill(kbArea);
                    updateImage(kbArea.getBounds());
                    g2d.dispose();
                }
                break;
            case ScreenOIA.OIA_LEVEL_MESSAGE_LIGHT_OFF:
                Graphics2D g2d = getWritingArea(font);

                g2d.setColor(UiUtils.toAwtColor(colorBg));
                g2d.fill(mArea);
                updateImage(mArea.getBounds());
                g2d.dispose();
                break;
            case ScreenOIA.OIA_LEVEL_MESSAGE_LIGHT_ON:
                g2d = getWritingArea(font);
                float Y = (rowHeight * (screen.getRows() + 2))
                        - (lm.getLeading() + lm.getDescent());
                g2d.setColor(UiUtils.toAwtColor(colorBlue));
                g2d.drawString("MW", (float) mArea.getX(), Y);
                updateImage(mArea.getBounds());
                g2d.dispose();
                break;
            case ScreenOIA.OIA_LEVEL_SCRIPT:
                if (changedOIA.isScriptActive()) {
                    drawScriptRunning(UiUtils.toAwtColor(colorGreen));
                    updateImage(scriptArea.getBounds());
                } else {
                    eraseScriptRunning(UiUtils.toAwtColor(colorBg));
                    updateImage(scriptArea.getBounds());

                }
                break;
            case ScreenOIA.OIA_LEVEL_INPUT_INHIBITED:
            case ScreenOIA.OIA_LEVEL_NOT_INHIBITED:
            case ScreenOIA.OIA_LEVEL_INPUT_ERROR:
                setStatus(changedOIA);
                break;
            case ScreenOIA.OIA_LEVEL_INSERT_MODE:
                if (changedOIA.isInsertMode()) {
                    g2d = getWritingArea(font);
                    Y = (rowHeight * (screen.getRows() + 2))
                            - (lm.getLeading() + lm.getDescent());
                    g2d.setColor(UiUtils.toAwtColor(colorBlue));
                    g2d.drawLine((int) iArea.getX(), (int) Y, (int) (iArea.getX() + ((iArea.getWidth() / 2) - 1)), (int) (Y - (rowHeight / 2)));
                    g2d.drawLine((int) (iArea.getX() + iArea.getWidth() - 1), (int) Y, (int) (iArea.getX() + (iArea.getWidth() / 2)), (int) (Y - (rowHeight / 2)));
                    //g2d.drawString("I", (float) iArea.getX(), Y);

                    updateImage(iArea.getBounds());
                    g2d.dispose();
                } else {

                    g2d = getWritingArea(font);

                    g2d.setColor(UiUtils.toAwtColor(colorBg));
                    g2d.fill(iArea);
                    updateImage(iArea.getBounds());
                    g2d.dispose();

                }
                break;

        }
    }

    @Override
    protected int getWidth() {
        synchronized (lock) {
            // tell waiting threads to wake up
            lock.notifyAll();
            return bi.getWidth();
        }

    }

    @Override
    protected int getHeight() {
        synchronized (lock) {
            // tell waiting threads to wake up
            lock.notifyAll();
            return bi.getHeight();
        }
    }
}
