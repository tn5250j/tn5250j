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
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.SwingUtilities;

import org.tn5250j.event.ScreenListener;
import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenOIA;
import org.tn5250j.sessionsettings.ColumnSeparator;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public class GuiGraphicBufferSwing implements ScreenOIAListener,
        ScreenListener,
        PropertyChangeListener,
        SessionConfigListener,
        ActionListener {

    // Dup Character array for display output
    private static final transient char[] dupChar = {'*'};

    private BufferedImage bi;
    private final Object lock = new Object();
    private Line2D separatorLine = new Line2D.Float();
    private Rectangle2D tArea; // text area
    private Rectangle2D aArea; // all screen area
    private Rectangle2D cArea; // command line area
    private Rectangle2D sArea; // status area
    private Rectangle2D pArea; // position area (cursor etc..)
    private Rectangle2D mArea; // message area
    private Rectangle2D iArea; // insert indicator
    private Rectangle2D kbArea; // keybuffer indicator
    private Rectangle2D scriptArea; // script indicator
    private Rectangle2D cursor = new Rectangle2D.Float();
    private final static String xSystem = "X - System";
    private final static String xError = "X - II";
    private int crossRow;
    private Rectangle crossRect = new Rectangle();
    private int offTop = 0;   // offset from top
    private int offLeft = 0;  // offset from left
    private boolean antialiased = true;
    private Graphics2D gg2d;
    private Screen5250 screen;
    private Data updateRect;
    protected int columnWidth;
    protected int rowHeight;
    private SessionGui gui;

    private LineMetrics lm;
    /*default*/ Font font;
    private int lenScreen;
    private boolean showHex;
    private Color colorBlue;
    private Color colorWhite;
    private Color colorRed;
    private Color colorGreen;
    private Color colorPink;
    private Color colorYellow;
    /*default*/ Color colorBg;
    private Color colorTurq;
    private Color colorGUIField;
    private Color colorCursor;
    private Color colorSep;
    private Color colorHexAttr;
    protected int crossHair = 0;
    private boolean updateFont;
    private int cursorSize = 0;
    protected boolean hotSpots = false;
    private float sfh = 1.2f; // font scale height
    private float sfw = 1.0f; // font scale height
    private float ps132 = 0; // Font point size
    private boolean cfg_guiInterface = false;
    private boolean cfg_guiShowUnderline = true;
    private int cursorBottOffset;
    private boolean rulerFixed;
    private javax.swing.Timer blinker;
    private ColumnSeparator colSepLine;
    private final StringBuffer hsMore = new StringBuffer("More...");
    private final StringBuffer hsBottom = new StringBuffer("Bottom");
    private Rectangle workR = new Rectangle();

    private boolean colSep = false;
    private boolean underLine = false;
    private boolean nonDisplay = false;
    private Color fg;
    private Color bg;

    private SessionConfig config;

    protected Rectangle clipper;

    private final TN5250jLogger log = TN5250jLogFactory.getLogger("GFX");

    public GuiGraphicBufferSwing(final Screen5250 screen, final SessionGui gui, final SessionConfig config) {

        this.screen = screen;
        this.config = config;
        this.gui = gui;

        config.addSessionConfigListener(this);
        // load the session properties from it's profile.
        loadProps();

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

        getSettings();
        final FontRenderContext frc = new FontRenderContext(font.getTransform(),
                true, true);
        lm = font.getLineMetrics("Wy", frc);
        columnWidth = (int) font.getStringBounds("W", frc).getWidth() + 1;
        rowHeight = (int) (font.getStringBounds("g", frc).getHeight()
                + lm.getDescent() + lm.getLeading());

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

    public Font getFont() {
        return font;
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

    public void resize(final int width, final int height) {

        if (bi == null || bi.getWidth() != width || bi.getHeight() != height) {
            //         synchronized (lock) {
            bi = null;
            bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            getSettings();
            // tell waiting threads to wake up
            //            lock.notifyAll();
            //         }
        }

    }

    private void getSettings() {

        lenScreen = screen.getScreenLength();

    }

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
            blinker = new javax.swing.Timer(500, this);
            blinker.start();
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

        if (pn.equals("font")) {
            font = new Font((String) pce.getNewValue(), Font.PLAIN, 14);
            updateFont = true;
        }

        if (pn.equals("useAntialias")) {
            if (pce.getNewValue().equals("Yes"))
                setUseAntialias(true);
            else
                setUseAntialias(false);
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

            log.debug(getStringProperty("cursorBlink"));
            if (pce.getNewValue().equals("Yes")) {

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

        if (pn.equals("backspaceError")) {
            if (pce.getNewValue().equals("Yes"))
                screen.setBackspaceError(true);
            else
                screen.setBackspaceError(false);
        }

        if (updateFont) {
            final javafx.geometry.Rectangle2D r = gui.getDrawingBounds();
            resizeScreenArea((int) r.getWidth(), (int) r.getHeight());
            updateFont = false;
        }

        if (resetAttr) {
            //			for (int y = 0; y < lenScreen; y++) {
            //				screen[y].setAttribute(screen[y].getCharAttr());
            //			}
            drawOIA();
        }

        ((Component) gui).validate();
        ((Component) gui).repaint();
    }

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

        if (pos >= lenScreen) {
            pos = lenScreen - 1;
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
     * @param width
     * @param height
     */
    protected final void resizeScreenArea(final int width, final int height) {
        if (bi == null) {
            return;
        }

        Font k = null;
        k = GUIGraphicsUtils.getDerivedFont(font, width, height, screen.getRows(),
                screen.getColumns(), sfh, sfw, ps132);

        if (font.getSize() != k.getSize() || updateFont
                || (offLeft != (width - bi.getWidth()) / 2)
                || (offTop != (height - bi.getHeight()) / 2)) {

            // set up all the variables that are used in calculating the new
            // size
            font = k;
            final FontRenderContext frc = new FontRenderContext(font.getTransform(),
                    true, true);
            lm = font.getLineMetrics("Wy", frc);
            columnWidth = (int) font.getStringBounds("W", frc).getWidth() + 2;
            rowHeight = (int) (font.getStringBounds("g", frc).getHeight()
                    + lm.getDescent() + lm.getLeading());

            resize(columnWidth * screen.getColumns(), rowHeight * (screen.getRows() + 2));

            // set the offsets for the screen centering.
            offLeft = (width - getWidth()) / 2;
            offTop = (height - getHeight()) / 2;
            if (offLeft < 0)
                offLeft = 0;
            if (offTop < 0)
                offTop = 0;

            drawOIA();

            updateFont = false;
        }

    }

    public final Dimension getPreferredSize() {

        return new Dimension(columnWidth * screen.getColumns(), rowHeight * (screen.getRows() + 2));

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
        drawOIA();
        return bi;
    }

    /**
     * Draw the operator information area
     */
    private Graphics2D drawOIA() {

        final int numRows = screen.getRows();

        Graphics2D g2d;

        // get ourselves a global pointer to the graphics
        g2d = getDrawingArea();

        if (antialiased)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING,
                RenderingHints.VALUE_COLOR_RENDER_SPEED);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
        g2d.setFont(font);


        g2d.setColor(colorBg);
        g2d.fillRect(0, 0, bi.getWidth(null), bi.getHeight(null));
        tArea.setRect(0, 0, bi.getWidth(null), (rowHeight * (numRows)));
        cArea.setRect(0, rowHeight * (numRows + 1), bi.getWidth(null), rowHeight * (numRows + 1));
        aArea.setRect(0, 0, bi.getWidth(null), bi.getHeight(null));
        sArea.setRect(columnWidth * 9, rowHeight * (numRows + 1), columnWidth * 20, rowHeight);
        pArea.setRect(bi.getWidth(null) - columnWidth * 6, rowHeight * (numRows + 1), columnWidth * 6, rowHeight);
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
                bi.getWidth(null),
                (rowHeight * (numRows + 1)) - (rowHeight / 2));

        g2d.setColor(colorBlue);
        g2d.draw(separatorLine);
        gg2d = g2d;
        return g2d;
    }


    public void drawCursor(final int row, final int col) {

        final int botOffset = cursorBottOffset;
        final boolean insertMode = screen.getOIA().isInsertMode();

        Graphics2D g2 = getDrawingArea();

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

        final Rectangle r = cursor.getBounds();
        r.setSize(r.width, r.height);

        g2.setColor(colorCursor);
        g2.setXORMode(colorBg);

        g2.fill(cursor);

        updateImage(r);

        if (!rulerFixed) {
            crossRow = row;
            crossRect.setBounds(r);
        } else {
            if (crossHair == 0) {
                crossRow = row;
                crossRect.setBounds(r);
            }
        }

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
        g2.setPaint(colorBg);

        g2.fill(pArea);
        g2.setColor(colorWhite);

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

    protected void updateImage(final Rectangle r) {
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
    public Graphics2D getWritingArea(final Font font) {

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

    public void setUseAntialias(final boolean antialiased) {
        this.antialiased = antialiased;
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
            g2d.setColor(colorBg);
            g2d.fill(sArea);

            final float Y = ((int) sArea.getY() + rowHeight) - (lm.getLeading() + lm.getDescent());

            switch (attr) {

                case ScreenOIA.OIA_LEVEL_INPUT_INHIBITED:
                    if (value == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT) {
                        g2d.setColor(colorWhite);

                        if (s != null)
                            g2d.drawString(s, (float) sArea.getX(), Y);
                        else
                            g2d.drawString(xSystem, (float) sArea.getX(), Y);
                    }
                    break;
                case ScreenOIA.OIA_LEVEL_INPUT_ERROR:
                    if (value == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT) {
                        g2d.setColor(colorRed);

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

    public final void drawChar(final Graphics2D g, final int pos, final int row, final int col) {
        Rectangle csArea = new Rectangle();
        final char sChar[] = new char[1];
        final int attr = updateRect.attr[pos];
        sChar[0] = updateRect.text[pos];
        setDrawAttr(pos);
        final boolean attributePlace = updateRect.isAttr[pos] == 0 ? false : true;
        final int whichGui = updateRect.graphic[pos];
        final boolean useGui = whichGui == 0 ? false : true;

        csArea = modelToView(row, col, csArea);

        final int x = csArea.x;
        final int y = csArea.y;
        final int cy = (int) (y + rowHeight - (lm.getDescent() + lm.getLeading()));

        if (showHex && attributePlace) {
            final Font f = g.getFont();

            final Font k = f.deriveFont(f.getSize2D() / 2);
            g.setFont(k);
            g.setColor(colorHexAttr);
            final char[] a = Integer.toHexString(attr).toCharArray();
            g.drawChars(a, 0, 1, x, y + (rowHeight / 2));
            g.drawChars(a, 1, 1, x + (columnWidth / 2),
                    (int) (y + rowHeight - (lm.getDescent() + lm.getLeading()) - 2));
            g.setFont(f);
        }

        if (!nonDisplay && !attributePlace) {

            if (!useGui) {
                g.setColor(bg);
                g.fill(csArea);
            } else {

                if (bg == colorBg && whichGui >= TN5250jConstants.FIELD_LEFT && whichGui <= TN5250jConstants.FIELD_ONE)
                    g.setColor(colorGUIField);
                else
                    g.setColor(bg);

                g.fill(csArea);

            }

            if (useGui && (whichGui < TN5250jConstants.FIELD_LEFT)) {

                g.setColor(fg);

                switch (whichGui) {

                    case TN5250jConstants.UPPER_LEFT:
                        if (sChar[0] == '.') {
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
                        if (sChar[0] == '.') {

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
                        if (sChar[0] == '.') {
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
                        if (sChar[0] == ':') {
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
                        if (sChar[0] == ':') {

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
                        if (sChar[0] == '.') {

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
                        if (sChar[0] == ':') {
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
                if (sChar[0] != 0x0) {
                    // use this until we define colors for gui stuff
                    if ((useGui && whichGui < TN5250jConstants.BUTTON_LEFT) && (fg == colorGUIField))

                        g.setColor(Color.black);
                    else
                        g.setColor(fg);

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
                        g.setColor(fg);
                        g.drawLine(x, (int) (y + (rowHeight - (lm.getLeading() + lm.getDescent()))), (x + columnWidth), (int) (y + (rowHeight - (lm.getLeading() + lm.getDescent()))));

                    }
                }

                if (colSep) {
                    g.setColor(colorSep);
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

    @Override
    public void onScreenSizeChanged(final int rows, final int cols) {
        log.info("screen size change");
        gui.resizeMe();
    }

    @Override
    public void onScreenChanged(final int which, int sr, final int sc, final int er, final int ec) {
        if (which == 3 || which == 4) {
            drawCursor(sr, sc);
            return;
        }

        if (hotSpots) screen.checkHotSpots();

        updateRect = new Data(sr, sc, er, ec);

        final Rectangle clipper = new Rectangle();
        clipper.x = sc * columnWidth;
        clipper.y = sr * rowHeight;
        clipper.width = ((ec - sc) + 1) * columnWidth;
        clipper.height = ((er - sr) + 1) * rowHeight;

        gg2d.setClip(clipper.getBounds());

        gg2d.setColor(colorBg);

        gg2d.fillRect(clipper.x, clipper.y, clipper.width, clipper.height);

        int pos = 0;
        while (sr <= er) {
            int cols = ec - sc;
            int lc = sc;
            while (cols-- >= 0) {
                if (sc + cols <= ec) {
                    drawChar(gg2d, pos++, sr, lc);
                    lc++;
                }
            }
            sr++;
        }
        updateImage(clipper);
    }

    @Override
    public void onOIAChanged(final ScreenOIA changedOIA, final int change) {

        switch (changedOIA.getLevel()) {

            case ScreenOIA.OIA_LEVEL_KEYS_BUFFERED:
                if (changedOIA.isKeysBuffered()) {
                    final Graphics2D g2d = getWritingArea(font);
                    final float Y = (rowHeight * (screen.getRows() + 2))
                            - (lm.getLeading() + lm.getDescent());
                    g2d.setColor(colorYellow);
                    g2d.drawString("KB", (float) kbArea.getX(), Y);

                    updateImage(kbArea.getBounds());
                    g2d.dispose();
                } else {
                    final Graphics2D g2d = getWritingArea(font);
                    g2d.setColor(colorBg);
                    g2d.fill(kbArea);
                    updateImage(kbArea.getBounds());
                    g2d.dispose();
                }
                break;
            case ScreenOIA.OIA_LEVEL_MESSAGE_LIGHT_OFF:
                Graphics2D g2d = getWritingArea(font);

                g2d.setColor(colorBg);
                g2d.fill(mArea);
                updateImage(mArea.getBounds());
                g2d.dispose();
                break;
            case ScreenOIA.OIA_LEVEL_MESSAGE_LIGHT_ON:
                g2d = getWritingArea(font);
                float Y = (rowHeight * (screen.getRows() + 2))
                        - (lm.getLeading() + lm.getDescent());
                g2d.setColor(colorBlue);
                g2d.drawString("MW", (float) mArea.getX(), Y);
                updateImage(mArea.getBounds());
                g2d.dispose();
                break;
            case ScreenOIA.OIA_LEVEL_SCRIPT:
                if (changedOIA.isScriptActive()) {
                    drawScriptRunning(colorGreen);
                    updateImage(scriptArea.getBounds());
                } else {
                    eraseScriptRunning(colorBg);
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
                    g2d.setColor(colorBlue);
                    g2d.drawLine((int) iArea.getX(), (int) Y, (int) (iArea.getX() + ((iArea.getWidth() / 2) - 1)), (int) (Y - (rowHeight / 2)));
                    g2d.drawLine((int) (iArea.getX() + iArea.getWidth() - 1), (int) Y, (int) (iArea.getX() + (iArea.getWidth() / 2)), (int) (Y - (rowHeight / 2)));
                    //g2d.drawString("I", (float) iArea.getX(), Y);

                    updateImage(iArea.getBounds());
                    g2d.dispose();
                } else {

                    g2d = getWritingArea(font);

                    g2d.setColor(colorBg);
                    g2d.fill(iArea);
                    updateImage(iArea.getBounds());
                    g2d.dispose();

                }
                break;

        }
    }

    /**
     * get the
     */
    public Rectangle2D getTextArea() {
        return tArea;
    }

    public Rectangle2D getScreenArea() {
        return aArea;
    }

    public Rectangle2D getCommandLineArea() {
        return cArea;
    }

    public Rectangle2D getStatusArea() {
        return sArea;
    }

    public Rectangle2D getPositionArea() {
        return pArea;
    }

    public Rectangle2D getMessageArea() {
        return mArea;
    }

    public Rectangle2D getInsertIndicatorArea() {
        return iArea;
    }

    public Rectangle2D getKBIndicatorArea() {
        return kbArea;
    }

    public Rectangle2D getScriptIndicatorArea() {
        return scriptArea;
    }

    public int getWidth() {

        synchronized (lock) {
            // tell waiting threads to wake up
            lock.notifyAll();
            return bi.getWidth();
        }

    }

    public int getHeight() {

        synchronized (lock) {
            // tell waiting threads to wake up
            lock.notifyAll();
            return bi.getHeight();
        }
    }

    public int getWidth(final ImageObserver io) {

        synchronized (lock) {
            // tell waiting threads to wake up
            lock.notifyAll();
            return bi.getWidth(io);
        }
    }

    public int getHeight(final ImageObserver io) {

        synchronized (lock) {
            // tell waiting threads to wake up
            lock.notifyAll();
            return bi.getHeight(io);
        }
    }


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

            if (size == lenScreen) {
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

    private void setDrawAttr(final int pos) {

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
