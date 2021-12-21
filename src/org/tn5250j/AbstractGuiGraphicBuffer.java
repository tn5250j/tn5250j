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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.tn5250j.event.ScreenListener;
import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.gui.Line2D;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.sessionsettings.ColumnSeparator;
import org.tn5250j.tools.GUIGraphicsUtils;

import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public abstract class AbstractGuiGraphicBuffer implements ScreenOIAListener,
        ScreenListener,
        PropertyChangeListener,
        SessionConfigListener {

    // Dup Character array for display output
    protected static final char[] dupChar = {'*'};

    protected Line2D separatorLine = new Line2D();
    protected Rectangle2D aArea = Rectangle2D.EMPTY; // all screen area
    protected Rectangle2D cArea = Rectangle2D.EMPTY; // command line area
    protected Rectangle2D sArea = Rectangle2D.EMPTY; // status area
    protected Rectangle2D pArea = Rectangle2D.EMPTY; // position area (cursor etc..)
    protected Rectangle2D mArea = Rectangle2D.EMPTY; // message area
    protected Rectangle2D iArea = Rectangle2D.EMPTY; // insert indicator
    protected Rectangle2D kbArea = Rectangle2D.EMPTY; // keybuffer indicator
    protected Rectangle2D scriptArea = Rectangle2D.EMPTY; // script indicator
    protected Rectangle2D cursorArea = Rectangle2D.EMPTY;
    protected final static String xSystem = "X - System";
    protected final static String xError = "X - II";
    protected int crossRow;
    protected Rectangle2D crossRect = Rectangle2D.EMPTY;
    protected boolean antialiased = true;
    protected Screen5250 screen;
    protected Data updateRect;
    protected int columnWidth;
    protected int rowHeight;
    protected SessionGui gui;

    /*default*/ Font font;
    protected boolean showHex;
    protected Color colorBlue;
    protected Color colorWhite;
    protected Color colorRed;
    protected Color colorGreen;
    protected Color colorPink;
    protected Color colorYellow;
    /*default*/ Color colorBg;
    protected Color colorTurq;
    protected Color colorGUIField;
    protected Color colorCursor;
    protected Color colorSep;
    protected Color colorHexAttr;
    protected int crossHair = 0;
    protected int cursorSize = 0;
    protected boolean hotSpots = false;
    protected float sfh = 1.2f; // font scale height
    protected float sfw = 1.0f; // font scale height
    protected float ps132 = 0; // Font point size
    protected boolean cfg_guiInterface = false;
    protected boolean cfg_guiShowUnderline = true;
    protected int cursorBottOffset;
    protected boolean rulerFixed;
    protected ColumnSeparator colSepLine;
    protected final StringBuffer hsMore = new StringBuffer("More...");
    protected final StringBuffer hsBottom = new StringBuffer("Bottom");
    protected Rectangle2D workR = Rectangle2D.EMPTY;

    protected boolean colSep = false;
    protected boolean underLine = false;
    protected boolean nonDisplay = false;
    protected Color fg;
    protected Color bg;

    protected SessionConfig config;

    public AbstractGuiGraphicBuffer(final Screen5250 screen, final SessionGui gui, final SessionConfig config) {

        this.screen = screen;
        this.config = config;
        this.gui = gui;

        config.addSessionConfigListener(this);
        // load the session properties from it's profile.
        loadProps();

        final Dimension2D cellBounds = getCellBounds();
        columnWidth = (int) Math.ceil(cellBounds.getWidth());
        rowHeight = (int) Math.ceil(cellBounds.getHeight());

        screen.getOIA().addOIAListener(this);
        screen.addScreenListener(this);
    }

    protected abstract Dimension2D getCellBounds();

    public Font getFont() {
        return font;
    }

    public abstract void resize(final double width, final double height);

    protected final void loadColors() {

        colorBlue = Color.rgb(140, 120, 255);
        colorTurq = Color.rgb(0, 240, 255);
        colorRed = Color.RED;
        colorWhite = Color.WHITE;
        colorYellow = Color.YELLOW;
        colorGreen = Color.GREEN;
        colorPink = Color.MAGENTA;
        colorGUIField = Color.WHITE;
        colorSep = Color.WHITE;
        colorHexAttr = Color.WHITE;

        if (cfg_guiInterface)
            colorBg = Color.LIGHTGRAY;
        else
            colorBg = Color.BLACK;

        colorCursor = Color.WHITE;

        if (!config.isPropertyExists("colorBg"))
            setProperty("colorBg", Integer.toString(UiUtils.toRgb(colorBg)));
        else {
            colorBg = getColorProperty("colorBg");
        }

        if (!config.isPropertyExists("colorBlue"))
            setProperty("colorBlue", Integer.toString(UiUtils.toRgb(colorBlue)));
        else
            colorBlue = getColorProperty("colorBlue");

        if (!config.isPropertyExists("colorTurq"))
            setProperty("colorTurq", Integer.toString(UiUtils.toRgb(colorTurq)));
        else
            colorTurq = getColorProperty("colorTurq");

        if (!config.isPropertyExists("colorRed"))
            setProperty("colorRed", Integer.toString(UiUtils.toRgb(colorRed)));
        else
            colorRed = getColorProperty("colorRed");

        if (!config.isPropertyExists("colorWhite"))
            setProperty("colorWhite", Integer.toString(UiUtils.toRgb(colorWhite)));
        else
            colorWhite = getColorProperty("colorWhite");

        if (!config.isPropertyExists("colorYellow"))
            setProperty("colorYellow", Integer.toString(UiUtils.toRgb(colorYellow)));
        else
            colorYellow = getColorProperty("colorYellow");

        if (!config.isPropertyExists("colorGreen"))
            setProperty("colorGreen", Integer.toString(UiUtils.toRgb(colorGreen)));
        else
            colorGreen = getColorProperty("colorGreen");

        if (!config.isPropertyExists("colorPink"))
            setProperty("colorPink", Integer.toString(UiUtils.toRgb(colorPink)));
        else
            colorPink = getColorProperty("colorPink");

        if (!config.isPropertyExists("colorGUIField"))
            setProperty("colorGUIField", Integer.toString(UiUtils.toRgb(colorGUIField)));
        else
            colorGUIField = getColorProperty("colorGUIField");

        if (!config.isPropertyExists("colorCursor"))
            setProperty("colorCursor", Integer.toString(UiUtils.toRgb(colorCursor)));
        else
            colorCursor = getColorProperty("colorCursor");

        if (!config.isPropertyExists("colorSep")) {
            colorSep = colorWhite;
            setProperty("colorSep", Integer.toString(UiUtils.toRgb(colorSep)));
        } else
            colorSep = getColorProperty("colorSep");

        if (!config.isPropertyExists("colorHexAttr")) {
            colorHexAttr = colorWhite;
            setProperty("colorHexAttr", Integer.toString(UiUtils.toRgb(colorHexAttr)));
        } else
            colorHexAttr = getColorProperty("colorHexAttr");

    }

    public Color getBackground() {
        return colorBg;
    }

    public void loadProps() {
        // change by Luc - LDC If the font from the properties file does not
        // exist
        //    select the default font
        String fontName = null;
        if (config.isPropertyExists("font")) {
            fontName = getStringProperty("font");
            if (GUIGraphicsUtils.isFontNameExists(fontName) == false)
                fontName = null;
        }

        //      if (!config.isPropertyExists("font")) {
        if (fontName == null) {
            font = new Font(GUIGraphicsUtils.getDefaultFont(), 14);
            //         font = new Font("Courier New",Font.PLAIN,14);
            config.setProperty("font", font.getName());
        } else {
            //font = new Font(getStringProperty("font"),Font.PLAIN,14);
            font = new Font(fontName, 14);
        }

        loadColors();

        colSepLine = ColumnSeparator.getFromName(getStringProperty("colSeparator"));

        if (config.isPropertyExists("showAttr")) {
            if (getStringProperty("showAttr").equals("Hex"))
                showHex = true;
        }

        if (config.isPropertyExists("guiInterface")) {
            if (getStringProperty("guiInterface").equals("Yes")) {
                screen.setUseGUIInterface(true);
                cfg_guiInterface = true;
            } else {
                screen.setUseGUIInterface(false);
                cfg_guiInterface = false;
            }
        }

        if (config.isPropertyExists("guiShowUnderline")) {
            if (getStringProperty("guiShowUnderline").equals("Yes"))
                cfg_guiShowUnderline = true;
            else
                cfg_guiShowUnderline = false;
        }

        if (config.isPropertyExists("hotspots")) {
            if (getStringProperty("hotspots").equals("Yes"))
                hotSpots = true;
            else
                hotSpots = false;
        }

        if (config.isPropertyExists("hsMore")) {
            if (getStringProperty("hsMore").length() > 0) {
                hsMore.setLength(0);
                hsMore.append(getStringProperty("hsMore"));
            }
        }

        if (config.isPropertyExists("hsBottom")) {
            if (getStringProperty("hsBottom").length() > 0) {
                hsBottom.setLength(0);
                hsBottom.append(getStringProperty("hsBottom"));
            }
        }

        if (config.isPropertyExists("cursorSize")) {
            if (getStringProperty("cursorSize").equals("Full"))
                cursorSize = 2;
            if (getStringProperty("cursorSize").equals("Half"))
                cursorSize = 1;
            if (getStringProperty("cursorSize").equals("Line"))
                cursorSize = 0;

        }

        if (config.isPropertyExists("crossHair")) {
            if (getStringProperty("crossHair").equals("None"))
                crossHair = 0;
            if (getStringProperty("crossHair").equals("Horz"))
                crossHair = 1;
            if (getStringProperty("crossHair").equals("Vert"))
                crossHair = 2;
            if (getStringProperty("crossHair").equals("Both"))
                crossHair = 3;

        }

        if (config.isPropertyExists("rulerFixed")) {

            if (getStringProperty("rulerFixed").equals("Yes"))
                rulerFixed = true;
            else
                rulerFixed = false;

        }

        if (config.isPropertyExists("fontScaleHeight")) {
            sfh = getFloatProperty("fontScaleHeight");
        }

        if (config.isPropertyExists("fontScaleWidth")) {
            sfw = getFloatProperty("fontScaleWidth");
        }

        if (config.isPropertyExists("fontPointSize")) {
            ps132 = getFloatProperty("fontPointSize");
        }

        if (config.isPropertyExists("cursorBottOffset")) {
            cursorBottOffset = getIntProperty("cursorBottOffset");
        }

        if (config.isPropertyExists("resetRequired")) {
            if (getStringProperty("resetRequired").equals("Yes"))
                screen.setResetRequired(true);
            else
                screen.setResetRequired(false);
        }

        if (config.isPropertyExists("useAntialias")) {

            if (getStringProperty("useAntialias").equals("Yes"))
                antialiased = true;
            else
                antialiased = false;

        }

        if (getStringProperty("cursorBlink").equals("Yes")) {
            setCursorBlinking(true);
        }

        if (config.isPropertyExists("backspaceError")) {
            if (getStringProperty("backspaceError").equals("Yes"))
                screen.setBackspaceError(true);
            else
                screen.setBackspaceError(false);
        }
    }

    @SuppressWarnings("deprecation")
    protected final String getStringProperty(final String prop) {
        return config.getStringProperty(prop);
    }

    @SuppressWarnings("deprecation")
    protected final Color getColorProperty(final String prop) {

        return config.getColorProperty(prop);

    }

    @SuppressWarnings("deprecation")
    protected final float getFloatProperty(final String prop) {

        return config.getFloatProperty(prop);

    }

    @SuppressWarnings("deprecation")
    protected final int getIntProperty(final String prop) {

        return config.getIntegerProperty(prop);

    }

    protected final void setProperty(final String key, final String val) {

        config.setProperty(key, val);

    }

    /**
     * Update the configuration settings
     * @param pce
     */
    @Override
    public void onConfigChanged(final SessionConfigEvent pce) {
        this.propertyChange(pce);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent pce) {

        final String pn = pce.getPropertyName();
        boolean resetAttr = false;

        if (pn.equals("colorBg")) {
            colorBg = (Color) pce.getNewValue();
            resetAttr = true;

        }

        if (pn.equals("colorBlue")) {
            colorBlue = (Color) pce.getNewValue();
            resetAttr = true;
        }

        if (pn.equals("colorTurq")) {
            colorTurq = (Color) pce.getNewValue();
            resetAttr = true;
        }

        if (pn.equals("colorRed")) {
            colorRed = (Color) pce.getNewValue();
            resetAttr = true;
        }

        if (pn.equals("colorWhite")) {
            colorWhite = (Color) pce.getNewValue();
            resetAttr = true;
        }

        if (pn.equals("colorYellow")) {
            colorYellow = (Color) pce.getNewValue();
            resetAttr = true;
        }

        if (pn.equals("colorGreen")) {
            colorGreen = (Color) pce.getNewValue();
            resetAttr = true;
        }

        if (pn.equals("colorPink")) {
            colorPink = (Color) pce.getNewValue();
            resetAttr = true;
        }

        if (pn.equals("colorGUIField")) {
            colorGUIField = (Color) pce.getNewValue();
            resetAttr = true;
        }

        if (pn.equals("colorCursor")) {
            colorCursor = (Color) pce.getNewValue();
            resetAttr = true;
        }

        if (pn.equals("colorSep")) {
            colorSep = (Color) pce.getNewValue();
            resetAttr = true;
        }

        if (pn.equals("colorHexAttr")) {
            colorHexAttr = (Color) pce.getNewValue();
            resetAttr = true;
        }

        if (pn.equals("cursorSize")) {
            if (pce.getNewValue().equals("Full"))
                cursorSize = 2;
            if (pce.getNewValue().equals("Half"))
                cursorSize = 1;
            if (pce.getNewValue().equals("Line"))
                cursorSize = 0;

        }

        if (pn.equals("crossHair")) {
            if (pce.getNewValue().equals("None"))
                crossHair = 0;
            if (pce.getNewValue().equals("Horz"))
                crossHair = 1;
            if (pce.getNewValue().equals("Vert"))
                crossHair = 2;
            if (pce.getNewValue().equals("Both"))
                crossHair = 3;
        }

        if (pn.equals("rulerFixed")) {
            if (pce.getNewValue().equals("Yes"))
                rulerFixed = true;
            else
                rulerFixed = false;
        }

        colSepLine = ColumnSeparator.getFromName(pce.getNewValue().toString());

        if (pn.equals("showAttr")) {
            if (pce.getNewValue().equals("Hex"))
                showHex = true;
            else
                showHex = false;
        }

        if (pn.equals("guiInterface")) {
            if (pce.getNewValue().equals("Yes")) {
                screen.setUseGUIInterface(true);
                cfg_guiInterface = true;
            } else {
                screen.setUseGUIInterface(true);
                cfg_guiInterface = false;
            }
        }

        if (pn.equals("guiShowUnderline")) {
            if (pce.getNewValue().equals("Yes"))
                cfg_guiShowUnderline = true;
            else
                cfg_guiShowUnderline = false;
        }

        if (pn.equals("hotspots")) {
            if (pce.getNewValue().equals("Yes"))
                hotSpots = true;
            else
                hotSpots = false;
        }

        if (pn.equals("resetRequired")) {
            if (pce.getNewValue().equals("Yes"))
                screen.setResetRequired(true);
            else
                screen.setResetRequired(false);
        }

        if (pn.equals("hsMore")) {
            hsMore.setLength(0);
            hsMore.append((String) pce.getNewValue());

        }

        if (pn.equals("hsBottom")) {
            hsBottom.setLength(0);
            hsBottom.append((String) pce.getNewValue());

        }

        boolean updateFont = false;
        if (pn.equals("font")) {
            font = new Font((String) pce.getNewValue(), 14);
            updateFont = true;
        }

        if (pn.equals("useAntialias")) {
            if (pce.getNewValue().equals("Yes"))
                this.antialiased = true;
           else
               this.antialiased = false;
            updateFont = true;
        }

        if (pn.equals("fontScaleHeight")) {

            //         try {
            sfh = Float.parseFloat((String) pce.getNewValue());
            updateFont = true;
            //         }

        }

        if (pn.equals("fontScaleWidth")) {

            //         try {
            sfw = Float.parseFloat((String) pce.getNewValue());
            updateFont = true;
            //         }

        }

        if (pn.equals("fontPointSize")) {

            //         try {
            ps132 = Float.parseFloat((String) pce.getNewValue());
            updateFont = true;
            //         }

        }

        if (pn.equals("cursorBottOffset")) {
            cursorBottOffset = getIntProperty("cursorBottOffset");
        }

        if (pn.equals("cursorBlink")) {
            setCursorBlinking(pce.getNewValue().equals("Yes"));
        }

        if (pn.equals("backspaceError")) {
            if (pce.getNewValue().equals("Yes"))
                screen.setBackspaceError(true);
            else
                screen.setBackspaceError(false);
        }

        if (updateFont) {
            final Dimension2D r = gui.getDrawingSize();
            resizeScreenArea(r.getWidth(), r.getHeight(), updateFont);
        }

        if (resetAttr) {
            //			for (int y = 0; y < lenScreen; y++) {
            //				screen[y].setAttribute(screen[y].getCharAttr());
            //			}
            recalculateOIASizes();
            drawOIA();
        }

        refreshView();
    }

    protected void refreshView() {
        ((Component) gui).validate();
        ((Component) gui).repaint();
    }

    protected abstract void setCursorBlinking(final boolean blinking);

    /**
     * @return text area size.
     */
    protected abstract Dimension2D getTextArea();

    /**
     * Return the row column based on the screen x,y position coordinates
     *
     * It will calculate a 0,0 based row and column based on the screen point
     * coordinate.
     *
     * @param x0
     *            screen x position
     * @param y0
     *            screen y position
     *
     * @return screen array position based 0,0 so position row 1 col 3 would be
     *         2
     */
    public int getRowColFromPoint(final double x0, final double y0) {
        final Dimension2D tArea = getTextArea();

        double x = x0;
        double y = y0;

        if (x > tArea.getWidth())
            x = tArea.getWidth() - 1;
        if (y > tArea.getHeight())
            y = (int) tArea.getHeight() - 1;
        if (x < 0)
            x = 0;
        if (y < 0)
            y = 0;

        final int s0 = (int) (y / rowHeight);
        final int s1 = (int) (x / columnWidth);

        return screen.getPos(s0, s1);
    }


    /**
     * This will return the screen coordinates of a row and column.
     *
     * @param r
     * @param c
     * @param point
     */
    public Point2D getPointFromRowCol(final int r, final int c) {

        // here the x + y coordinates of the row and column are obtained from
        // the character array which is based on a upper left 0,0 coordinate
        //  we will then add to that the offsets to get the screen position point
        //  x,y coordinates. Maybe change this to a translate routine method or
        //  something.
        return new Point2D(columnWidth * c, rowHeight * r);
    }

    public boolean isWithinScreenArea(final int x, final int y) {
        final Dimension2D ta = getTextArea();
        return x >= 0 && x <= ta.getWidth() && y >= 0 && y >= ta.getWidth();

    }

    /**
     *
     * RubberBanding start code
     *
     */

    /**
     * Translate the starting point of mouse movement to encompass a full
     * character
     *
     * @param start
     * @return Point
     */
    public Point2D translateStart(final double px, final double py) {

        // because getRowColFromPoint returns position offset as 1,1 we need
        // to translate as offset 0,0
        final int pos = getRowColFromPoint(px, py);
        return new Point2D(columnWidth * screen.getCol(pos), rowHeight * screen.getRow(pos));
    }

    /**
     * Translate the ending point of mouse movement to encompass a full
     * character
     *
     * @param end
     * @return Point
     */
    public Point2D translateEnd(final double px, final double py) {

        // because getRowColFromPoint returns position offset as 1,1 we need
        // to translate as offset 0,0
        int pos = getRowColFromPoint(px, py);

        if (pos >= screen.getScreenLength()) {
            pos = screen.getScreenLength() - 1;
        }

        return new Point2D(
            ((columnWidth * screen.getCol(pos)) + columnWidth) - 1,
            ((rowHeight * screen.getRow(pos)) + rowHeight) - 1
        );
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
    public Rectangle2D getBoundingArea() {

        // check to see if there is an area selected. If not then return all
        //    screen area.
        if (!gui.getRubberband().isAreaSelected()) {

            return new Rectangle2D(1, 1, screen.getColumns(), screen.getRows());
        } else {
            // lets get the bounding area using a rectangle that we have already
            // allocated

            // get starting row and column
            final int sPos = getRowColFromPoint(workR.getMinX(), workR.getMinY());
            // get the width and height
            final int ePos = getRowColFromPoint(workR.getWidth(), workR.getHeight());

            final double row = screen.getRow(sPos) + 1;
            final double col = screen.getCol(sPos) + 1;

            return new Rectangle2D(row, col, screen.getCol(ePos) + 1, screen.getRow(ePos) + 1);
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
    protected void resizeScreenArea(final double width, final double height, final boolean updateFont) {
        final Font k = GUIGraphicsUtils.getDerivedFont(font, width, height,
                screen.getRows(), screen.getColumns(), sfh, sfw, ps132);

        if (font.getSize() != k.getSize() || updateFont) {

            // set up all the variables that are used in calculating the new
            // size
            font = k;

            final Dimension2D cellBounds = getCellBounds();
            columnWidth = (int) Math.ceil(cellBounds.getWidth());
            rowHeight = (int) Math.ceil(cellBounds.getHeight());

            // set the offsets for the screen centering.
            redrawResized(columnWidth * screen.getColumns(), rowHeight * (screen.getRows() + 2));
        }
    }

    protected void redrawResized(final double w, final double h) {
        resize(w, h);
        recalculateOIASizes();
        drawOIA();
    }

    public final Dimension2D getPreferredSize() {

        return new Dimension2D(columnWidth * screen.getColumns(), rowHeight * (screen.getRows() + 2));

    }

    /**
     * Draw the operator information area
     */
    protected abstract void drawOIA();

    protected void recalculateOIASizes() {
        final int numRows = screen.getRows();

        cArea = new Rectangle2D(0, rowHeight * (numRows + 1), getWidth(), rowHeight * (numRows + 1));
        aArea = new Rectangle2D(0, 0, getWidth(), getHeight());
        sArea = new Rectangle2D(columnWidth * 9, rowHeight * (numRows + 1), columnWidth * 20, rowHeight);
        pArea = new Rectangle2D(getWidth() - columnWidth * 6, rowHeight * (numRows + 1), columnWidth * 6, rowHeight);
        mArea = new Rectangle2D((float) (sArea.getMinX() + sArea.getWidth()) + columnWidth + columnWidth,
                rowHeight * (numRows + 1),
                columnWidth + columnWidth,
                rowHeight);
        kbArea = new Rectangle2D((float) (sArea.getMinX() + sArea.getWidth()) + (20 * columnWidth),
                rowHeight * (numRows + 1),
                columnWidth + columnWidth,
                rowHeight);
        scriptArea = new Rectangle2D((float) (sArea.getMinX() + sArea.getWidth()) + (16 * columnWidth),
                rowHeight * (numRows + 1),
                columnWidth + columnWidth,
                rowHeight);
        iArea = new Rectangle2D((float) (sArea.getMinX() + sArea.getWidth()) + (25 * columnWidth),
                rowHeight * (numRows + 1),
                columnWidth,
                rowHeight);

        separatorLine.setLine(0,
                (rowHeight * (numRows + 1)) - (rowHeight / 2),
                getWidth(),
                (rowHeight * (numRows + 1)) - (rowHeight / 2));
    }

    protected abstract void drawCursor(final int row, final int col, final int botOffset);

    protected int recalculateCursorSizes(final int row, final int col) {
        final int botOffset = cursorBottOffset;
        final boolean insertMode = screen.getOIA().isInsertMode();

        switch (cursorSize) {
            case 0:
                cursorArea = new Rectangle2D(
                        columnWidth * (col),
                        (rowHeight * (row + 1)) - botOffset,
                        columnWidth,
                        1
                );
                break;
            case 1:
                cursorArea = new Rectangle2D(
                        columnWidth * (col),
                        (rowHeight * (row + 1) - rowHeight / 2),
                        columnWidth,
                        (rowHeight / 2) - botOffset
                );
                break;
            case 2:
                cursorArea = new Rectangle2D(
                        columnWidth * (col),
                        (rowHeight * row),
                        columnWidth,
                        rowHeight - botOffset
                );
                break;
        }

        if (insertMode && cursorSize != 1) {
            cursorArea = new Rectangle2D(
                    columnWidth * (col),
                    (rowHeight * (row + 1) - rowHeight / 2),
                    columnWidth,
                    (rowHeight / 2) - botOffset
            );
        }

        if (!rulerFixed) {
            crossRow = row;
            crossRect = cursorArea;
        } else {
            if (crossHair == 0) {
                crossRow = row;
                crossRect = cursorArea;
            }
        }

        return botOffset;
    }

    @Override
    public void onScreenSizeChanged(final int rows, final int cols) {
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

    protected abstract void drawScreen(int sr, final int sc, final int er, final int ec);

    protected abstract double getWidth();

    protected abstract double getHeight();

    protected Data fillData(final int startRow, final int startCol, final int endRow, final int endCol) {

        return new Data(startRow, startCol, endRow, endCol);

    }

    protected class Data {

        public char[] text;
        public char[] attr;
        public char[] isAttr;
        public char[] color;
        public char[] extended;
        public final char[] graphic;
        public final char[] field;

        public Data(final char[] text, final char[] attr, final char[] color, final char[] extended, final char[] graphic) {
            this.text = text;
            this.color = color;
            this.extended = extended;
            this.graphic = graphic;
            this.attr = attr;
            this.field = null;
        }

        public Data(int startRow, int startCol, int endRow, int endCol) {
            startRow++;
            startCol++;
            endRow++;
            endCol++;
            final int size = ((endCol - startCol) + 1) * ((endRow - startRow) + 1);

            text = new char[size];
            attr = new char[size];
            isAttr = new char[size];
            color = new char[size];
            extended = new char[size];
            graphic = new char[size];
            field = new char[size];

            if (size == screen.getScreenLength()) {
                screen.GetScreen(text, size, TN5250jConstants.PLANE_TEXT);
                screen.GetScreen(attr, size, TN5250jConstants.PLANE_ATTR);
                screen.GetScreen(isAttr, size, TN5250jConstants.PLANE_IS_ATTR_PLACE);
                screen.GetScreen(color, size, TN5250jConstants.PLANE_COLOR);
                screen.GetScreen(extended, size, TN5250jConstants.PLANE_EXTENDED);
                screen.GetScreen(graphic, size, TN5250jConstants.PLANE_EXTENDED_GRAPHIC);
                screen.GetScreen(field, size, TN5250jConstants.PLANE_FIELD);
            } else {
                screen.GetScreenRect(text, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_TEXT);
                screen.GetScreenRect(attr, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_ATTR);
                screen.GetScreenRect(isAttr, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_IS_ATTR_PLACE);
                screen.GetScreenRect(color, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_COLOR);
                screen.GetScreenRect(extended, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_EXTENDED);
                screen.GetScreenRect(graphic, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_EXTENDED_GRAPHIC);
                screen.GetScreenRect(field, size, startRow, startCol, endRow, endCol, TN5250jConstants.PLANE_FIELD);
            }
        }
    }

    public final Rectangle2D modelToView(final int row, final int col) {
        // right now row and column is 1,1 offset based.  This will need
        //   to be changed to 0,0 offset based by subtracting 1 from them
        //   when the screen is being passed this way
        //     r.x      =  (col - 1) * columnWidth;
        //     r.y      =  (row - 1) * rowHeight;
        return new Rectangle2D(
            col * columnWidth,
            row * rowHeight,
            columnWidth,
            rowHeight
        );
    }

    protected Color getColor(final char color, final boolean background) {
        int c = 0;
        if (background)
            // background
            c = (color & 0xff00) >> 8;
        else
            // foreground
            c = color & 0x00ff;

        switch (c) {
            case TN5250jConstants.COLOR_FG_BLACK:
                return colorBg;
            case TN5250jConstants.COLOR_FG_GREEN:
                return colorGreen;
            case TN5250jConstants.COLOR_FG_BLUE:
                return colorBlue;
            case TN5250jConstants.COLOR_FG_RED:
                return colorRed;
            case TN5250jConstants.COLOR_FG_YELLOW:
                return colorYellow;
            case TN5250jConstants.COLOR_FG_CYAN:
                return colorTurq;
            case TN5250jConstants.COLOR_FG_WHITE:
                return colorWhite;
            case TN5250jConstants.COLOR_FG_MAGENTA:
                return colorPink;
            default:
                return Color.ORANGE;
        }
    }

    protected void setDrawAttr(final int pos) {
        colSep = false;
        underLine = false;
        nonDisplay = false;

        fg = getColor(updateRect.color[pos], false);
        bg = getColor(updateRect.color[pos], true);
        underLine = (updateRect.extended[pos] & TN5250jConstants.EXTENDED_5250_UNDERLINE) != 0;
        colSep = (updateRect.extended[pos] & TN5250jConstants.EXTENDED_5250_COL_SEP) != 0;
        nonDisplay = (updateRect.extended[pos] & TN5250jConstants.EXTENDED_5250_NON_DSP) != 0;
    }
}
