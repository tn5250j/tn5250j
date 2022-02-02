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

import org.tn5250j.framework.tn5250.Rect;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenOIA;
import org.tn5250j.gui.FontMetrics;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.application.Platform;
import javafx.geometry.Dimension2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class GuiGraphicBuffer extends AbstractGuiGraphicBuffer {

    private final Canvas bi;
    private final TN5250jLogger log = TN5250jLogFactory.getLogger("GFX");
    private FontMetrics fontMetrics;

    public GuiGraphicBuffer(final Screen5250 screen, final SessionGui gui,
            final SessionConfig config, final Canvas canvas, final CompoundCursor cursor) {
        super(screen, gui, cursor, config);
        this.bi = canvas;
        fontMetrics = FontMetrics.deriveFrom(font);
    }

    @Override
    protected Dimension2D getCellBounds() {
        final Text text = new Text("W");
        text.setFont(getFont());
        final double w = text.getBoundsInLocal().getWidth() + 1;

        text.setText("Wg");
        final double h = text.getBoundsInLocal().getHeight();
        return new Dimension2D(w, h);
    }

    @Override
    public void resize(final double width, final double height) {
        bi.setWidth(width);
        bi.setHeight(height);
    }

    @Override
    protected void resizeScreenArea(final double width, final double height, final boolean updateFont) {
        if (updateFont) {
            fontMetrics = FontMetrics.deriveFrom(getFont());
        }
        super.resizeScreenArea(width, height, updateFont);
    }

    /**
     * Draw the operator information area
     */
    @Override
    protected void drawOIA() {
        final GraphicsContext g2d = getContext();

        g2d.setFill(colorBg);
        g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());

        setForeground(colorBlue);
        g2d.strokeLine(
                separatorLine.getStart().getX(),
                separatorLine.getStart().getY(),
                separatorLine.getEnd().getX(),
                separatorLine.getEnd().getY());
    }

    private void setForeground(final Color color) {
        final GraphicsContext g = getContext();
        g.setFill(color);
        g.setStroke(color);
    }

    private GraphicsContext getContext() {
        final GraphicsContext context = bi.getGraphicsContext2D();
        context.setFont(getFont());
        context.setLineWidth(2);
        fontMetrics = FontMetrics.deriveFrom(getFont());
        return context;
    }

    @Override
    protected void drawFutter(final int row, final int col, final int botOffset) {
        final GraphicsContext g2 = getContext();

        g2.setFont(getFont());
        g2.setFill(colorBg);

        g2.fillRect(pArea.getMinX(), pArea.getMinY(), pArea.getWidth(), pArea.getHeight());
        g2.setFill(colorWhite);

        g2.fillText((row + 1) + "/" + (col + 1), pArea.getMinX(), pArea.getMinY() + rowHeight);
    }

    private void drawScriptRunning(final Color color) {
        final GraphicsContext g2d = getContext();

        // set the points for the polygon
        final double[] xs = {scriptArea.getMinX(),
                scriptArea.getMinX(),
                scriptArea.getMinX() + (scriptArea.getWidth())};
        final double[] ys = {scriptArea.getMinY(),
                scriptArea.getMinY() + scriptArea.getHeight(),
                scriptArea.getMinY() + (scriptArea.getHeight() / 2)};

        // now lets draw it
        g2d.fillPolygon(xs, ys, 3);
    }

    private void eraseScriptRunning(final Color color) {
        final GraphicsContext g2d = getContext();

        // get ourselves a global pointer to the graphics
        g2d.setFill(color);
        g2d.fillRect(scriptArea.getMinX(), scriptArea.getMinY(), scriptArea.getWidth(), scriptArea.getHeight());
    }

    private void setStatus(final ScreenOIA oia) {

        final int attr = oia.getLevel();
        final int value = oia.getInputInhibited();
        final String s = oia.getInhibitedText();
        final GraphicsContext g2d = getContext();

        try {
            g2d.setFill(colorBg);
            g2d.fillRect(sArea.getMinX(), sArea.getMinY(), sArea.getWidth(), sArea.getHeight());

            final double Y = (sArea.getMinY() + rowHeight) - (fontMetrics.getLeading()
                    + fontMetrics.getDescent());

            switch (attr) {

                case ScreenOIA.OIA_LEVEL_INPUT_INHIBITED:
                    if (value == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT) {
                        g2d.setFill(colorWhite);

                        if (s != null)
                            g2d.fillText(s, sArea.getMinX(), Y);
                        else
                            g2d.fillText(xSystem, sArea.getMinX(), Y);
                    }
                    break;
                case ScreenOIA.OIA_LEVEL_INPUT_ERROR:
                    if (value == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT) {
                        g2d.setFill(colorRed);

                        if (s != null)
                            g2d.fillText(s, sArea.getMinX(), Y);
                        else
                            g2d.fillText(xError, sArea.getMinX(), Y);

                    }
                    break;

            }
        } catch (final Exception e) {
            log.warn(" gui graphics setStatus " + e.getMessage());
        }
    }

    protected final void drawChar(final int pos, final int row, final int col) {
        final char sChar = updateRect.text[pos];

        final boolean attributePlace = updateRect.isAttr[pos] == 0 ? false : true;
        final int whichGui = updateRect.graphic[pos];
        final boolean useGui = whichGui == 0 ? false : true;

        final Rectangle2D csArea = modelToView(row, col);

        final double x = csArea.getMinX();
        final double y = csArea.getMinY();
        final double cy = y + rowHeight - (fontMetrics.getDescent() + fontMetrics.getLeading());

        final GraphicsContext g = getContext();
        if (showHex && attributePlace) {
            final Font f = g.getFont();
            try {
                final Font k = UiUtils.deriveFont(f, f.getSize() / 2);
                g.setFont(k);
                g.setFill(colorHexAttr);
                final char[] a = toTwoHexChars(sChar);
                g.fillText(new String(a, 0, 1), x, y + (rowHeight / 2));
                g.fillText(new String(a, 1, 1), x + (columnWidth / 2),
                        y + rowHeight - (fontMetrics.getDescent() + fontMetrics.getLeading()) - 2);
            } finally {
                g.setFont(f);
            }
        }

        if (!nonDisplay && !attributePlace) {

            if (!useGui) {
                g.setFill(bg);
                UiUtils.fill(g, csArea);
            } else {

                if (bg == colorBg && whichGui >= TN5250jConstants.FIELD_LEFT && whichGui <= TN5250jConstants.FIELD_ONE)
                    g.setFill(colorGUIField);
                else
                    g.setFill(bg);

                UiUtils.fill(g, csArea);
            }

            if (useGui && (whichGui < TN5250jConstants.FIELD_LEFT)) {

                g.setFill(fg);

                switch (whichGui) {

                    case TN5250jConstants.UPPER_LEFT:
                        if (sChar == '.') {
                            if (screen.isUsingGuiInterface()) {
                                GUIGraphicsUtils.drawWinUpperLeft(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        colorBlue,
                                        x, y, columnWidth, rowHeight);

                            } else {

                                GUIGraphicsUtils.drawWinUpperLeft(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        fg,
                                        x, y, columnWidth, rowHeight);

                            }
                        }
                        break;
                    case TN5250jConstants.UPPER:
                        if (sChar == '.') {

                            if (screen.isUsingGuiInterface()) {
                                GUIGraphicsUtils.drawWinUpper(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        colorBlue,
                                        x, y, columnWidth, rowHeight);


                            } else {

                                GUIGraphicsUtils.drawWinUpper(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        fg,
                                        x, y, columnWidth, rowHeight);
                            }
                        }
                        break;
                    case TN5250jConstants.UPPER_RIGHT:
                        if (sChar == '.') {
                            if (screen.isUsingGuiInterface()) {

                                GUIGraphicsUtils.drawWinUpperRight(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        colorBlue,
                                        x, y, columnWidth, rowHeight);


                            } else {

                                GUIGraphicsUtils.drawWinUpperRight(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        fg,
                                        x, y, columnWidth, rowHeight);

                            }
                        }
                        break;
                    case TN5250jConstants.GUI_LEFT:
                        if (sChar == ':') {
                            if (screen.isUsingGuiInterface()) {
                                GUIGraphicsUtils.drawWinLeft(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        bg,
                                        x, y, columnWidth, rowHeight);


                            } else {

                                GUIGraphicsUtils.drawWinLeft(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        fg,
                                        x, y, columnWidth, rowHeight);

                                g.strokeLine(x + columnWidth / 2,
                                        y,
                                        x + columnWidth / 2,
                                        y + rowHeight);
                            }
                        }
                        break;
                    case TN5250jConstants.GUI_RIGHT:
                        if (sChar == ':') {
                            if (screen.isUsingGuiInterface()) {
                                GUIGraphicsUtils.drawWinRight(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        bg,
                                        x, y, columnWidth, rowHeight);


                            } else {
                                GUIGraphicsUtils.drawWinRight(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        fg,
                                        x, y, columnWidth, rowHeight);

                            }
                        }
                        break;
                    case TN5250jConstants.LOWER_LEFT:
                        if (sChar == ':') {

                            if (screen.isUsingGuiInterface()) {

                                GUIGraphicsUtils.drawWinLowerLeft(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        bg,
                                        x, y, columnWidth, rowHeight);


                            } else {

                                GUIGraphicsUtils.drawWinLowerLeft(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        fg,
                                        x, y, columnWidth, rowHeight);
                            }
                        }
                        break;
                    case TN5250jConstants.BOTTOM:
                        if (sChar == '.') {

                            if (screen.isUsingGuiInterface()) {


                                GUIGraphicsUtils.drawWinBottom(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        bg,
                                        x, y, columnWidth, rowHeight);


                            } else {

                                GUIGraphicsUtils.drawWinBottom(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        fg,
                                        x, y, columnWidth, rowHeight);
                            }
                        }
                        break;

                    case TN5250jConstants.LOWER_RIGHT:
                        if (sChar == ':') {
                            if (screen.isUsingGuiInterface()) {

                                GUIGraphicsUtils.drawWinLowerRight(g,
                                        GUIGraphicsUtils.WINDOW_GRAPHIC,
                                        bg,
                                        x, y, columnWidth, rowHeight);

                            } else {

                                GUIGraphicsUtils.drawWinLowerRight(g,
                                        GUIGraphicsUtils.WINDOW_NORMAL,
                                        fg,
                                        x, y, columnWidth, rowHeight);

                            }
                        }
                        break;

                }
            } else {
                if (sChar != 0x0) {
                    // use this until we define colors for gui stuff
                    if ((useGui && whichGui < TN5250jConstants.BUTTON_LEFT) && (fg == colorGUIField))

                        g.setFill(Color.BLACK);
                    else
                        g.setFill(fg);

                    try {
                        if (useGui)

                            if (sChar == 0x1C)
                                g.fillText(new String(dupChar), x + 1, cy - 2);
                            else
                                g.fillText(new String(new char[] {sChar}), x + 1, cy - 2);
                        else if (sChar == 0x1C)
                            g.fillText(new String(dupChar), x, cy - 2);
                        else
                            g.fillText(new String(new char[] {sChar}), x, cy - 2);
                    } catch (final IllegalArgumentException iae) {
                        System.out.println(" drawChar iae " + iae.getMessage());

                    }
                }
                if (underLine) {

                    if (!useGui || cfg_guiShowUnderline) {
                        setForeground(fg);
                        g.strokeLine(x, y + (rowHeight - (fontMetrics.getLeading() + fontMetrics.getDescent())), (x + columnWidth),
                            y + (rowHeight - (fontMetrics.getLeading() + fontMetrics.getDescent())));
                    }
                }

                if (colSep) {
                    g.setFill(colorSep);
                    switch (colSepLine) {
                        case Line:  // line
                            g.strokeLine(x, y, x, y + rowHeight - 1);
                            g.strokeLine(x + columnWidth - 1, y, x + columnWidth - 1, y + rowHeight);
                            break;
                        case ShortLine:  // short line
                            g.strokeLine(x, y + rowHeight - (int) fontMetrics.getLeading() - 4, x, y + rowHeight);
                            g.strokeLine(x + columnWidth - 1, y + rowHeight - (int) fontMetrics.getLeading() - 4, x + columnWidth - 1, y + rowHeight);
                            break;
                        case Dot:  // dot
                            g.strokeLine(x, y + rowHeight - (int) fontMetrics.getLeading() - 3, x, y + rowHeight - (int) fontMetrics.getLeading() - 4);
                            g.strokeLine(x + columnWidth - 1, y + rowHeight - (int) fontMetrics.getLeading() - 3, x + columnWidth - 1, y + rowHeight - (int) fontMetrics.getLeading() - 4);
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
                            colorWhite, colorBg);
                    break;

                // scroll bar
                case TN5250jConstants.BUTTON_SB_DN:

                    GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.RAISED, 2, x, y,
                            columnWidth, rowHeight,
                            colorWhite, colorBg);


                    break;
                // scroll bar
                case TN5250jConstants.BUTTON_SB_GUIDE:

                    GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.INSET, 0, x, y,
                            columnWidth, rowHeight,
                            colorWhite, colorBg);


                    break;

                // scroll bar
                case TN5250jConstants.BUTTON_SB_THUMB:

                    GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.INSET, 3, x, y,
                            columnWidth, rowHeight,
                            colorWhite, colorBg);


                    break;

            }
        }

    }

    private char[] toTwoHexChars(final int attr) {
        final String hex = Integer.toHexString(attr);
        if (hex.length() < 2) {
            return new char[] {'0', hex.charAt(0)};
        }
        return hex.toCharArray();
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
            drawFutter(sr, sc, botOffset);
            return;
        }

        if (hotSpots) screen.checkHotSpots();

        updateRect = new Data(sr, sc, er, ec);
        drawScreen(sr, sc, er, ec);
    }

    @Override
    protected Dimension2D getTextArea() {
        return new Dimension2D(bi.getWidth(), bi.getHeight());
    }

    @Override
    protected void drawScreen(int sr, final int sc, final int er, final int ec) {
        final double x = sc * columnWidth;
        final double y = sr * rowHeight;
        final double width = ((ec - sc) + 1) * columnWidth;
        final double height = ((er - sr) + 1) * rowHeight;

        final GraphicsContext gg2d = getContext();

        gg2d.setFill(colorBg);
        gg2d.fillRect(x, y, width, height);

        int pos = 0;
        while (sr <= er) {
            int cols = ec - sc;
            int lc = sc;
            while (cols-- >= 0) {
                if (sc + cols <= ec) {
                    setDrawAttr(pos);
                    drawChar(pos, sr, lc);
                    pos++;
                    lc++;
                }
            }
            sr++;
        }
    }

    @Override
    public void onOIAChanged(final ScreenOIA changedOIA, final int change) {
        Platform.runLater(() -> onOIAChanged(changedOIA));
    }

    /**
     * @param changedOIA
     */
    private void onOIAChanged(final ScreenOIA changedOIA) {
        final GraphicsContext g2d = getContext();

        switch (changedOIA.getLevel()) {
            case ScreenOIA.OIA_LEVEL_KEYS_BUFFERED:
                if (changedOIA.isKeysBuffered()) {
                    final double Y = (rowHeight * (screen.getRows() + 2))
                            - (fontMetrics.getLeading() + fontMetrics.getDescent());
                    g2d.setFill(colorYellow);
                    g2d.fillText("KB", kbArea.getMinX(), Y);
                } else {
                    g2d.setFill(colorBg);
                    UiUtils.fill(g2d, kbArea);
                }
                break;
            case ScreenOIA.OIA_LEVEL_MESSAGE_LIGHT_OFF:
                g2d.setFill(colorBg);
                UiUtils.fill(g2d, mArea);
                break;
            case ScreenOIA.OIA_LEVEL_MESSAGE_LIGHT_ON:
                double Y = (rowHeight * (screen.getRows() + 2))
                        - (fontMetrics.getLeading() + fontMetrics.getDescent());
                g2d.setFill(colorBlue);
                g2d.fillText("MW", mArea.getMinX(), Y);
                break;
            case ScreenOIA.OIA_LEVEL_SCRIPT:
                if (changedOIA.isScriptActive()) {
                    drawScriptRunning(colorGreen);
                } else {
                    eraseScriptRunning(colorBg);

                }
                break;
            case ScreenOIA.OIA_LEVEL_INPUT_INHIBITED:
            case ScreenOIA.OIA_LEVEL_NOT_INHIBITED:
            case ScreenOIA.OIA_LEVEL_INPUT_ERROR:
                setStatus(changedOIA);
                break;
            case ScreenOIA.OIA_LEVEL_INSERT_MODE:
                if (changedOIA.isInsertMode()) {
                    Y = (rowHeight * (screen.getRows() + 2))
                            - (fontMetrics.getLeading() + fontMetrics.getDescent());
                    setForeground(colorBlue);
                    g2d.strokeLine(iArea.getMinX(), Y, (iArea.getMinX() + ((iArea.getWidth() / 2) - 1)), (Y - (rowHeight / 2)));
                    g2d.strokeLine((iArea.getMinX() + iArea.getWidth() - 1), Y, (iArea.getMinX() + (iArea.getWidth() / 2)), (Y - (rowHeight / 2)));
                    //g2d.drawString("I", (float) iArea.getX(), Y);
                } else {
                    g2d.setFill(colorBg);
                    UiUtils.fill(g2d, iArea);
                }
                break;

        }
    }
    /**
     * Fills the passed Rectangle with the starting row and column and width and
     * height of the selected area.
     *
     * 1 BASED so column 1 row one is returned 1,1
     *
     * If there is no area bounded then the full screen area is returned.
     *
     * @param bounds
     */
    public Rect getBoundingArea(final Rectangle2D workR) {
        // get starting row and column
        final int sPos = getRowColFromPoint(workR.getMinX(), workR.getMinY());
        // get the ending row and column
        final int ePos = getRowColFromPoint(workR.getMaxX(), workR.getMaxY());

        final int x1 = screen.getCol(sPos);
        final int y1 = screen.getRow(sPos);
        final int x2 = screen.getCol(ePos);
        final int y2 = screen.getRow(ePos);

        return new Rect(x1, y1, x2 - x1, y2 - y1);
    }

    @Override
    protected double getWidth() {
        return bi.getWidth();
    }

    @Override
    protected double getHeight() {
        return bi.getHeight();
    }
}
