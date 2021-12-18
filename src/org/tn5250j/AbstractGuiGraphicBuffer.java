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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.tn5250j.event.ScreenListener;
import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.sessionsettings.ColumnSeparator;
import org.tn5250j.tools.GUIGraphicsUtils;

import javafx.geometry.Dimension2D;

public abstract class AbstractGuiGraphicBuffer implements ScreenOIAListener,
        ScreenListener,
        PropertyChangeListener,
        SessionConfigListener {

    // Dup Character array for display output
    protected static final char[] dupChar = {'*'};

    protected Line2D separatorLine = new Line2D.Float();
    protected Rectangle2D tArea; // text area
    protected Rectangle2D aArea; // all screen area
    protected Rectangle2D cArea; // command line area
    protected Rectangle2D sArea; // status area
    protected Rectangle2D pArea; // position area (cursor etc..)
    protected Rectangle2D mArea; // message area
    protected Rectangle2D iArea; // insert indicator
    protected Rectangle2D kbArea; // keybuffer indicator
    protected Rectangle2D scriptArea; // script indicator
    protected Rectangle2D cursor = new Rectangle2D.Float();
    protected final static String xSystem = "X - System";
    protected final static String xError = "X - II";
    protected int crossRow;
    protected Rectangle crossRect = new Rectangle();
    protected int offTop = 0;   // offset from top
    protected int offLeft = 0;  // offset from left
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
    protected Rectangle workR = new Rectangle();

    protected boolean colSep = false;
    protected boolean underLine = false;
    protected boolean nonDisplay = false;
    protected Color fg;
    protected Color bg;

    protected SessionConfig config;

    protected Rectangle clipper;

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

        tArea = new Rectangle2D.Float();
        cArea = new Rectangle2D.Float();
        aArea = new Rectangle2D.Float();
        sArea = new Rectangle2D.Float();
        pArea = new Rectangle2D.Float();
        mArea = new Rectangle2D.Float();
        iArea = new Rectangle2D.Float();
        kbArea = new Rectangle2D.Float();
        scriptArea = new Rectangle2D.Float();

    }

    protected Dimension2D getCellBounds() {
        final FontRenderContext frc = new FontRenderContext(getFont().getTransform(),
                true, true);
        final LineMetrics lm = getFont().getLineMetrics("Wy", frc);
        final double w = getFont().getStringBounds("W", frc).getWidth() + 1;
        final double h = (getFont().getStringBounds("g", frc).getHeight()
                + lm.getDescent() + lm.getLeading());
        return new Dimension2D(w, h);
    }

    public Font getFont() {
        return font;
    }

    public abstract void resize(final int width, final int height);

    protected final void loadColors() {

        colorBlue = new Color(140, 120, 255);
        colorTurq = new Color(0, 240, 255);
        colorRed = Color.red;
        colorWhite = Color.white;
        colorYellow = Color.yellow;
        colorGreen = Color.green;
        colorPink = Color.magenta;
        colorGUIField = Color.white;
        colorSep = Color.white;
        colorHexAttr = Color.white;

        if (cfg_guiInterface)
            colorBg = Color.lightGray;
        else
            colorBg = Color.black;

        colorCursor = Color.white;

        if (!config.isPropertyExists("colorBg"))
            setProperty("colorBg", Integer.toString(colorBg.getRGB()));
        else {
            colorBg = getColorProperty("colorBg");
        }

        if (!config.isPropertyExists("colorBlue"))
            setProperty("colorBlue", Integer.toString(colorBlue.getRGB()));
        else
            colorBlue = getColorProperty("colorBlue");

        if (!config.isPropertyExists("colorTurq"))
            setProperty("colorTurq", Integer.toString(colorTurq.getRGB()));
        else
            colorTurq = getColorProperty("colorTurq");

        if (!config.isPropertyExists("colorRed"))
            setProperty("colorRed", Integer.toString(colorRed.getRGB()));
        else
            colorRed = getColorProperty("colorRed");

        if (!config.isPropertyExists("colorWhite"))
            setProperty("colorWhite", Integer.toString(colorWhite.getRGB()));
        else
            colorWhite = getColorProperty("colorWhite");

        if (!config.isPropertyExists("colorYellow"))
            setProperty("colorYellow", Integer.toString(colorYellow.getRGB()));
        else
            colorYellow = getColorProperty("colorYellow");

        if (!config.isPropertyExists("colorGreen"))
            setProperty("colorGreen", Integer.toString(colorGreen.getRGB()));
        else
            colorGreen = getColorProperty("colorGreen");

        if (!config.isPropertyExists("colorPink"))
            setProperty("colorPink", Integer.toString(colorPink.getRGB()));
        else
            colorPink = getColorProperty("colorPink");

        if (!config.isPropertyExists("colorGUIField"))
            setProperty("colorGUIField", Integer.toString(colorGUIField
                    .getRGB()));
        else
            colorGUIField = getColorProperty("colorGUIField");

        if (!config.isPropertyExists("colorCursor"))
            setProperty("colorCursor", Integer.toString(colorCursor.getRGB()));
        else
            colorCursor = getColorProperty("colorCursor");

        if (!config.isPropertyExists("colorSep")) {
            colorSep = colorWhite;
            setProperty("colorSep", Integer.toString(colorSep.getRGB()));
        } else
            colorSep = getColorProperty("colorSep");

        if (!config.isPropertyExists("colorHexAttr")) {
            colorHexAttr = colorWhite;
            setProperty("colorHexAttr", Integer.toString(colorHexAttr.getRGB()));
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
            font = new Font(GUIGraphicsUtils.getDefaultFont(), Font.PLAIN, 14);
            //         font = new Font("Courier New",Font.PLAIN,14);
            config.setProperty("font", font.getFontName());
        } else {
            //font = new Font(getStringProperty("font"),Font.PLAIN,14);
            font = new Font(fontName, Font.PLAIN, 14);
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
            font = new Font((String) pce.getNewValue(), Font.PLAIN, 14);
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
            final javafx.geometry.Rectangle2D r = gui.getDrawingBounds();
            resizeScreenArea((int) r.getWidth(), (int) r.getHeight(), updateFont);
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
     *
     *
     * @param x
     * @param y
     * @return
     */
    public int getPosFromView(int x, int y) {

        // we have to translate the point into a an upper left 0,0 based format
        // to get the position into the character array which is 0,0 based.
        // we take the point of x,y and subtract the screen offsets.

        x -= offLeft;
        y -= offTop;

        if (x > tArea.getMaxX())
            x = (int) tArea.getMaxX() - 1;
        if (y > tArea.getMaxY())
            y = (int) tArea.getMaxY() - 1;
        if (x < tArea.getMinX())
            x = 0;
        if (y < tArea.getMinY())
            y = 0;

        final int s0 = y / rowHeight;
        final int s1 = x / columnWidth;

        return screen.getPos(s0, s1);

    }

    /**
     * Return the row column based on the screen x,y position coordinates
     *
     * It will calculate a 0,0 based row and column based on the screen point
     * coordinate.
     *
     * @param x
     *            screen x position
     * @param y
     *            screen y position
     *
     * @return screen array position based 0,0 so position row 1 col 3 would be
     *         2
     */
    public int getRowColFromPoint(int x, int y) {

        if (x > tArea.getMaxX())
            x = (int) tArea.getMaxX() - 1;
        if (y > tArea.getMaxY())
            y = (int) tArea.getMaxY() - 1;
        if (x < tArea.getMinX())
            x = 0;
        if (y < tArea.getMinY())
            y = 0;

        final int s0 = y / rowHeight;
        final int s1 = x / columnWidth;

        return screen.getPos(s0, s1);

    }


    /**
     * This will return the screen coordinates of a row and column.
     *
     * @param r
     * @param c
     * @param point
     */
    public void getPointFromRowCol(final int r, final int c, final Point point) {

        // here the x + y coordinates of the row and column are obtained from
        // the character array which is based on a upper left 0,0 coordinate
        //  we will then add to that the offsets to get the screen position point
        //  x,y coordinates. Maybe change this to a translate routine method or
        //  something.
        point.x = (columnWidth * c) + offLeft;
        point.y = (rowHeight * r) + offTop;

    }

    public boolean isWithinScreenArea(final int x, final int y) {

        return tArea.contains(x, y);

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
    public Point translateStart(final Point start) {

        // because getRowColFromPoint returns position offset as 1,1 we need
        // to translate as offset 0,0
        final int pos = getPosFromView(start.x, start.y);
        final int x = columnWidth * screen.getCol(pos);
        final int y = rowHeight * screen.getRow(pos);
        start.setLocation(x, y);
        return start;

    }

    /**
     * Translate the ending point of mouse movement to encompass a full
     * character
     *
     * @param end
     * @return Point
     */
    public Point translateEnd(final Point end) {

        // because getRowColFromPoint returns position offset as 1,1 we need
        // to translate as offset 0,0
        int pos = getPosFromView(end.x, end.y);

        if (pos >= screen.getScreenLength()) {
            pos = screen.getScreenLength() - 1;
        }
        final int x = ((columnWidth * screen.getCol(pos)) + columnWidth) - 1;
        final int y = ((rowHeight * screen.getRow(pos)) + rowHeight) - 1;

        end.setLocation(x, y);

        return end;
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
    public void getBoundingArea(final Rectangle bounds) {

        // check to see if there is an area selected. If not then return all
        //    screen area.
        if (!gui.getRubberband().isAreaSelected()) {

            bounds.setBounds(1, 1, screen.getColumns(), screen.getRows());
        } else {
            // lets get the bounding area using a rectangle that we have already
            // allocated
            final javafx.geometry.Rectangle2D rect = gui.getRubberband().getBoundingArea();
            bounds.x = (int) Math.round(rect.getMinX());
            bounds.y = (int) Math.round(rect.getMinY());
            bounds.width = (int) Math.round(rect.getWidth());
            bounds.height = (int) Math.round(rect.getHeight());

            // get starting row and column
            final int sPos = getRowColFromPoint(workR.x, workR.y);
            // get the width and height
            final int ePos = getRowColFromPoint(workR.width, workR.height);

            final int row = screen.getRow(sPos) + 1;
            final int col = screen.getCol(sPos) + 1;

            bounds.setBounds(row, col, screen.getCol(ePos) + 1, screen.getRow(ePos) + 1);
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
    protected void resizeScreenArea(final int width, final int height, final boolean updateFont) {
        Font k = null;
        k = GUIGraphicsUtils.getDerivedFont(font, width, height, screen.getRows(),
                screen.getColumns(), sfh, sfw, ps132);

        if (font.getSize() != k.getSize() || updateFont
                || (offLeft != (width - getWidth()) / 2)
                || (offTop != (height - getHeight()) / 2)) {

            // set up all the variables that are used in calculating the new
            // size
            font = k;

            final Dimension2D cellBounds = getCellBounds();
            columnWidth = (int) Math.ceil(cellBounds.getWidth());
            rowHeight = (int) Math.ceil(cellBounds.getHeight());

            // set the offsets for the screen centering.
            offLeft = (width - getWidth()) / 2;
            offTop = (height - getHeight()) / 2;
            if (offLeft < 0)
                offLeft = 0;
            if (offTop < 0)
                offTop = 0;

            redrawResized(columnWidth * screen.getColumns(), rowHeight * (screen.getRows() + 2));
        }
    }

    protected void redrawResized(final int w, final int h) {
        resize(w, h);
        recalculateOIASizes();
        drawOIA();
    }

    public final Dimension getPreferredSize() {

        return new Dimension(columnWidth * screen.getColumns(), rowHeight * (screen.getRows() + 2));

    }

    /**
     * Draw the operator information area
     */
    protected abstract void drawOIA();

    protected void recalculateOIASizes() {
        final int numRows = screen.getRows();

        tArea.setRect(0, 0, getWidth(), (rowHeight * (numRows)));
        cArea.setRect(0, rowHeight * (numRows + 1), getWidth(), rowHeight * (numRows + 1));
        aArea.setRect(0, 0, getWidth(), getHeight());
        sArea.setRect(columnWidth * 9, rowHeight * (numRows + 1), columnWidth * 20, rowHeight);
        pArea.setRect(getWidth() - columnWidth * 6, rowHeight * (numRows + 1), columnWidth * 6, rowHeight);
        mArea.setRect((float) (sArea.getX() + sArea.getWidth()) + columnWidth + columnWidth,
                rowHeight * (numRows + 1),
                columnWidth + columnWidth,
                rowHeight);
        kbArea.setRect((float) (sArea.getX() + sArea.getWidth()) + (20 * columnWidth),
                rowHeight * (numRows + 1),
                columnWidth + columnWidth,
                rowHeight);
        scriptArea.setRect((float) (sArea.getX() + sArea.getWidth()) + (16 * columnWidth),
                rowHeight * (numRows + 1),
                columnWidth + columnWidth,
                rowHeight);
        iArea.setRect((float) (sArea.getX() + sArea.getWidth()) + (25 * columnWidth),
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
                cursor.setRect(
                        columnWidth * (col),
                        (rowHeight * (row + 1)) - botOffset,
                        columnWidth,
                        1
                );
                break;
            case 1:
                cursor.setRect(
                        columnWidth * (col),
                        (rowHeight * (row + 1) - rowHeight / 2),
                        columnWidth,
                        (rowHeight / 2) - botOffset
                );
                break;
            case 2:
                cursor.setRect(
                        columnWidth * (col),
                        (rowHeight * row),
                        columnWidth,
                        rowHeight - botOffset
                );
                break;
        }

        if (insertMode && cursorSize != 1) {
            cursor.setRect(
                    columnWidth * (col),
                    (rowHeight * (row + 1) - rowHeight / 2),
                    columnWidth,
                    (rowHeight / 2) - botOffset
            );
        }

        if (!rulerFixed) {
            crossRow = row;
            crossRect.setBounds(cursor.getBounds());
        } else {
            if (crossHair == 0) {
                crossRow = row;
                crossRect.setBounds(cursor.getBounds());
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

    protected abstract int getWidth();

    protected abstract int getHeight();

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

    public final Rectangle modelToView(final int row, final int col) {
        return modelToView(row, col, new Rectangle());
    }

    public final Rectangle modelToView(final int row, final int col, final Rectangle r) {

        // right now row and column is 1,1 offset based.  This will need
        //   to be changed to 0,0 offset based by subtracting 1 from them
        //   when the screen is being passed this way
        //     r.x      =  (col - 1) * columnWidth;
        //     r.y      =  (row - 1) * rowHeight;
        r.x = col * columnWidth;
        r.y = row * rowHeight;
        r.width = columnWidth;
        r.height = rowHeight;
        return r;
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
                return Color.orange;
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
