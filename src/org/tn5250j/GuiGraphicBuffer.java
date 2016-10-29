/*
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

import org.tn5250j.api.screen.ScreenListenerAdapter;
import org.tn5250j.api.screen.ScreenPlanes;
import org.tn5250j.api.screen.ScreenListener;
import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenOIA;
import org.tn5250j.framework.tn5250.ScreenPlanesImpl;
import org.tn5250j.sessionsettings.ColumnSeparator;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javax.swing.*;
import java.awt.*;
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

import static org.tn5250j.SessionConfig.YES;
import static org.tn5250j.tools.GUIGraphicsUtils.*;

public class GuiGraphicBuffer implements ScreenOIAListener,
    PropertyChangeListener,
    SessionConfigListener,
    ActionListener {

  // Dup Character array for display output
  private static final transient char[] DUP_CHAR = {'*'};
  private static final String xSystem = "X - System";
  private static final String xError = "X - II";


  private BufferedImage bi;
  private final Object lock = new Object();
  private Line2D separatorLine = new Line2D.Float();
  private Rectangle2D textArea;
  private Rectangle2D allScreenArea;
  private Rectangle2D commandLineArea;
  private Rectangle2D statusArea;
  private Rectangle2D positionArea;
  private Rectangle2D messageArea;
  private Rectangle2D insertIndicatorArea;
  private Rectangle2D keyBufferIndicatorArea;
  private Rectangle2D scriptIndicatorArea;
  private Rectangle2D cursor = new Rectangle2D.Float();
  private int crossRow;
  private Rectangle crossRect = new Rectangle();
  private int offsetFromTop = 0;
  private int offsetFromLeft = 0;
  private boolean antialiased = true;
  private Graphics2D gg2d;
  private Screen5250 screen;
  private ScreenPlanes updateRect;
  protected int columnWidth;
  protected int rowHeight;
  private SessionPanel gui;

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

  private final TN5250jLogger log = TN5250jLogFactory.getLogger(getClass());

  public GuiGraphicBuffer(Screen5250 screen, SessionPanel gui, SessionConfig config) {

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

      fontName = this.config.getStringProperty("font");
      if (GUIGraphicsUtils.isFontNameExists(fontName) == false)
        fontName = null;
    }

    if (fontName == null) {
      font = new Font(GUIGraphicsUtils.getDefaultFont(), Font.PLAIN, 14);
      config.setProperty("font", font.getFontName());
    } else {
      font = new Font(fontName, Font.PLAIN, 14);
    }

    gui.setFont(font);

    lenScreen = this.screen.getScreenLength();
    FontRenderContext frc = new FontRenderContext(font.getTransform(),
        true, true);
    lm = font.getLineMetrics("Wy", frc);
    columnWidth = (int) font.getStringBounds("W", frc).getWidth() + 1;
    rowHeight = (int) (font.getStringBounds("g", frc).getHeight()
        + lm.getDescent() + lm.getLeading());

    screen.getOIA().addOIAListener(this);
    screen.addScreenListener(new ScreenListenerAdapter() {
      @Override
      public void onScreenSizeChanged(int rows, int columns) {
        GuiGraphicBuffer.this.onScreenSizeChanged(rows, columns);
      }

      @Override
      public void onScreenChanged(int inUpdate, int startRow, int startCol, int endRow, int endCol) {
        GuiGraphicBuffer.this.onScreenChanged(inUpdate, startRow, startCol, endRow, endCol);
      }
    });
    textArea = new Rectangle2D.Float();
    commandLineArea = new Rectangle2D.Float();
    allScreenArea = new Rectangle2D.Float();
    statusArea = new Rectangle2D.Float();
    positionArea = new Rectangle2D.Float();
    messageArea = new Rectangle2D.Float();
    insertIndicatorArea = new Rectangle2D.Float();
    keyBufferIndicatorArea = new Rectangle2D.Float();
    scriptIndicatorArea = new Rectangle2D.Float();

  }

  /**
   * This is for blinking cursor but should be moved out
   */
  public void actionPerformed(ActionEvent actionevent) {
    if (actionevent.getSource() instanceof javax.swing.Timer) {
      if (screen.isCursorActive())
        screen.setCursorActive(false);
      else
        screen.setCursorActive(true);
    }
  }

  public boolean isBlinkCursor() {
    return blinker != null;
  }

  public void resize(int width, int height) {

    if (bi.getWidth() != width || bi.getHeight() != height) {
      bi = null;
      bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
      lenScreen = screen.getScreenLength();
    }

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
      config.setProperty("colorBg", Integer.toString(colorBg.getRGB()));
    else {

      colorBg = config.getColorProperty("colorBg");
    }
    gui.setBackground(colorBg);

    if (!config.isPropertyExists("colorBlue"))
      config.setProperty("colorBlue", Integer.toString(colorBlue.getRGB()));
    else
      colorBlue = config.getColorProperty("colorBlue");

    if (!config.isPropertyExists("colorTurq"))
      config.setProperty("colorTurq", Integer.toString(colorTurq.getRGB()));
    else
      colorTurq = config.getColorProperty("colorTurq");

    if (!config.isPropertyExists("colorRed"))
      config.setProperty("colorRed", Integer.toString(colorRed.getRGB()));
    else
      colorRed = config.getColorProperty("colorRed");

    if (!config.isPropertyExists("colorWhite"))
      config.setProperty("colorWhite", Integer.toString(colorWhite.getRGB()));
    else
      colorWhite = config.getColorProperty("colorWhite");

    if (!config.isPropertyExists("colorYellow"))
      config.setProperty("colorYellow", Integer.toString(colorYellow.getRGB()));
    else
      colorYellow = config.getColorProperty("colorYellow");

    if (!config.isPropertyExists("colorGreen"))
      config.setProperty("colorGreen", Integer.toString(colorGreen.getRGB()));
    else
      colorGreen = config.getColorProperty("colorGreen");

    if (!config.isPropertyExists("colorPink"))
      config.setProperty("colorPink", Integer.toString(colorPink.getRGB()));
    else
      colorPink = config.getColorProperty("colorPink");

    if (!config.isPropertyExists("colorGUIField"))
      config.setProperty("colorGUIField", Integer.toString(colorGUIField
          .getRGB()));
    else
      colorGUIField = config.getColorProperty("colorGUIField");

    if (!config.isPropertyExists("colorCursor"))
      config.setProperty("colorCursor", Integer.toString(colorCursor.getRGB()));
    else
      colorCursor = config.getColorProperty("colorCursor");

    if (!config.isPropertyExists("colorSep")) {
      colorSep = colorWhite;
      config.setProperty("colorSep", Integer.toString(colorSep.getRGB()));
    } else
      colorSep = config.getColorProperty("colorSep");

    if (!config.isPropertyExists("colorHexAttr")) {
      colorHexAttr = colorWhite;
      config.setProperty("colorHexAttr", Integer.toString(colorHexAttr.getRGB()));
    } else
      colorHexAttr = config.getColorProperty("colorHexAttr");

  }

  private void loadProps() {

    loadColors();

    colSepLine = ColumnSeparator.getFromName(config.getStringProperty("colSeparator"));

    if (config.isPropertyExists("showAttr")) {

      if (config.getStringProperty("showAttr").equals("Hex"))
        showHex = true;
    }

    if (config.isPropertyExists("guiInterface")) {

      if (YES.equals(config.getStringProperty("guiInterface"))) {
        screen.setUseGUIInterface(true);
        cfg_guiInterface = true;
      } else {
        screen.setUseGUIInterface(false);
        cfg_guiInterface = false;
      }
    }

    cfg_guiShowUnderline = YES.equals(config.getStringProperty("guiShowUnderline"));

    hotSpots = YES.equals(config.getStringProperty("hotspots"));

    if (config.isPropertyExists("hsMore")) {

      if (config.getStringProperty("hsMore").length() > 0) {
        hsMore.setLength(0);

        hsMore.append(config.getStringProperty("hsMore"));
      }
    }

    if (config.isPropertyExists("hsBottom")) {

      if (config.getStringProperty("hsBottom").length() > 0) {
        hsBottom.setLength(0);

        hsBottom.append(config.getStringProperty("hsBottom"));
      }
    }

    if (config.isPropertyExists("cursorSize")) {

      if (config.getStringProperty("cursorSize").equals("Full"))
        cursorSize = 2;

      if (config.getStringProperty("cursorSize").equals("Half"))
        cursorSize = 1;

      if (config.getStringProperty("cursorSize").equals("Line"))
        cursorSize = 0;

    }

    if (config.isPropertyExists("crossHair")) {

      if (config.getStringProperty("crossHair").equals("None"))
        crossHair = 0;

      if (config.getStringProperty("crossHair").equals("Horz"))
        crossHair = 1;

      if (config.getStringProperty("crossHair").equals("Vert"))
        crossHair = 2;

      if (config.getStringProperty("crossHair").equals("Both"))
        crossHair = 3;

    }

    rulerFixed = YES.equals(config.getStringProperty("rulerFixed"));

    if (config.isPropertyExists("fontScaleHeight")) {

      sfh = config.getFloatProperty("fontScaleHeight");
    }

    if (config.isPropertyExists("fontScaleWidth")) {

      sfw = config.getFloatProperty("fontScaleWidth");
    }

    if (config.isPropertyExists("fontPointSize")) {

      ps132 = config.getFloatProperty("fontPointSize");
    }

    if (config.isPropertyExists("cursorBottOffset")) {

      cursorBottOffset = config.getIntegerProperty("cursorBottOffset");
    }

    if (config.isPropertyExists("resetRequired")) {

      if (YES.equals(config.getStringProperty("resetRequired")))
        screen.setResetRequired(true);
      else
        screen.setResetRequired(false);
    }

    antialiased = YES.equals(config.getStringProperty("useAntialias"));

    if (YES.equals(config.getStringProperty("cursorBlink"))) {
      blinker = new javax.swing.Timer(500, this);
      blinker.start();
    }

    if (config.isPropertyExists("backspaceError")) {

      if (YES.equals(config.getStringProperty("backspaceError")))
        screen.setBackspaceError(true);
      else
        screen.setBackspaceError(false);
    }
  }

  /**
   * Update the configuration settings
   *
   * @param pce
   */
  public void onConfigChanged(SessionConfigEvent pce) {
    this.propertyChange(pce);
  }

  public void propertyChange(PropertyChangeEvent pce) {

    String pn = pce.getPropertyName();
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
      if (YES.equals(pce.getNewValue()))
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
      if (YES.equals(pce.getNewValue())) {
        screen.setUseGUIInterface(true);
        cfg_guiInterface = true;
      } else {
        screen.setUseGUIInterface(true);
        cfg_guiInterface = false;
      }
    }

    if (pn.equals("guiShowUnderline")) {
      if (YES.equals(pce.getNewValue()))
        cfg_guiShowUnderline = true;
      else
        cfg_guiShowUnderline = false;
    }

    if (pn.equals("hotspots")) {
      if (YES.equals(pce.getNewValue()))
        hotSpots = true;
      else
        hotSpots = false;
    }

    if (pn.equals("resetRequired")) {
      if (YES.equals(pce.getNewValue()))
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
      if (YES.equals(pce.getNewValue()))
        setUseAntialias(true);
      else
        setUseAntialias(false);
      updateFont = true;
    }

    if (pn.equals("fontScaleHeight")) {
      sfh = Float.parseFloat((String) pce.getNewValue());
      updateFont = true;
    }

    if (pn.equals("fontScaleWidth")) {
      sfw = Float.parseFloat((String) pce.getNewValue());
      updateFont = true;
    }

    if (pn.equals("fontPointSize")) {
      ps132 = Float.parseFloat((String) pce.getNewValue());
      updateFont = true;
    }

    if (pn.equals("cursorBottOffset")) {

      cursorBottOffset = config.getIntegerProperty("cursorBottOffset");
    }

    if (pn.equals("cursorBlink")) {

      log.debug(config.getStringProperty("cursorBlink"));
      if (YES.equals(pce.getNewValue())) {

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
      if (YES.equals(pce.getNewValue()))
        screen.setBackspaceError(true);
      else
        screen.setBackspaceError(false);
    }

    if (updateFont) {
      Rectangle r = gui.getDrawingBounds();
      resizeScreenArea(r.width, r.height);
      updateFont = false;
    }

    if (resetAttr) {
      //			for (int y = 0; y < lenScreen; y++) {
      //				screen[y].setAttribute(screen[y].getCharAttr());
      //			}
      drawOIA();
    }

    gui.validate();
    gui.repaint();
  }

  /**
   * @param x
   * @param y
   * @return
   */
  public int getPosFromView(int x, int y) {

    // we have to translate the point into a an upper left 0,0 based format
    // to get the position into the character array which is 0,0 based.
    // we take the point of x,y and subtract the screen offsets.

    x -= offsetFromLeft;
    y -= offsetFromTop;

    if (x > textArea.getMaxX())
      x = (int) textArea.getMaxX() - 1;
    if (y > textArea.getMaxY())
      y = (int) textArea.getMaxY() - 1;
    if (x < textArea.getMinX())
      x = 0;
    if (y < textArea.getMinY())
      y = 0;

    int s0 = y / rowHeight;
    int s1 = x / columnWidth;

    return screen.getPos(s0, s1);

  }

  /**
   * Return the row column based on the screen x,y position coordinates
   * <p>
   * It will calculate a 0,0 based row and column based on the screen point
   * coordinate.
   *
   * @param x screen x position
   * @param y screen y position
   * @return screen array position based 0,0 so position row 1 col 3 would be
   * 2
   */
  public int getRowColFromPoint(int x, int y) {

    if (x > textArea.getMaxX())
      x = (int) textArea.getMaxX() - 1;
    if (y > textArea.getMaxY())
      y = (int) textArea.getMaxY() - 1;
    if (x < textArea.getMinX())
      x = 0;
    if (y < textArea.getMinY())
      y = 0;

    int s0 = y / rowHeight;
    int s1 = x / columnWidth;

    return screen.getPos(s0, s1);

  }


  /**
   * This will return the screen coordinates of a row and column.
   *
   * @param r
   * @param c
   * @param point
   */
  public void getPointFromRowCol(int r, int c, Point point) {

    // here the x + y coordinates of the row and column are obtained from
    // the character array which is based on a upper left 0,0 coordinate
    //  we will then add to that the offsets to get the screen position point
    //  x,y coordinates. Maybe change this to a translate routine method or
    //  something.
    point.x = (columnWidth * c) + offsetFromLeft;
    point.y = (rowHeight * r) + offsetFromTop;

  }

  public boolean isWithinScreenArea(int x, int y) {

    return textArea.contains(x, y);

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
  public Point translateStart(Point start) {

    // because getRowColFromPoint returns position offset as 1,1 we need
    // to translate as offset 0,0
    int pos = getPosFromView(start.x, start.y);
    int x = columnWidth * screen.getCol(pos);
    int y = rowHeight * screen.getRow(pos);
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
  public Point translateEnd(Point end) {

    // because getRowColFromPoint returns position offset as 1,1 we need
    // to translate as offset 0,0
    int pos = getPosFromView(end.x, end.y);

    if (pos >= lenScreen) {
      pos = lenScreen - 1;
    }
    int x = ((columnWidth * screen.getCol(pos)) + columnWidth) - 1;
    int y = ((rowHeight * screen.getRow(pos)) + rowHeight) - 1;

    end.setLocation(x, y);

    return end;
  }

  /**
   * Fills the passed Rectangle with the starting row and column and width and
   * height of the selected area.
   * <p>
   * 1 BASED so column 1 row one is returned 1,1
   * <p>
   * If there is no area bounded then the full screen area is returned.
   *
   * @param bounds
   */
  public void getBoundingArea(Rectangle bounds) {

    // check to see if there is an area selected. If not then return all
    //    screen area.
    if (!gui.rubberband.isAreaSelected()) {

      bounds.setBounds(1, 1, screen.getColumns(), screen.getRows());
    } else {
      // lets get the bounding area using a rectangle that we have already
      // allocated
      gui.rubberband.getBoundingArea(workR);

      // get starting row and column
      int sPos = getRowColFromPoint(workR.x, workR.y);
      // get the width and height
      int ePos = getRowColFromPoint(workR.width, workR.height);

      int row = screen.getRow(sPos) + 1;
      int col = screen.getCol(sPos) + 1;

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
  protected final void resizeScreenArea(int width, int height) {

    Font k = null;
    k = GUIGraphicsUtils.getDerivedFont(font, width, height, screen.getRows(),
        screen.getColumns(), sfh, sfw, ps132);

    if (font.getSize() != k.getSize() || updateFont
        || (offsetFromLeft != (width - bi.getWidth()) / 2)
        || (offsetFromTop != (height - bi.getHeight()) / 2)) {

      // set up all the variables that are used in calculating the new
      // size
      font = k;
      FontRenderContext frc = new FontRenderContext(font.getTransform(),
          true, true);
      lm = font.getLineMetrics("Wy", frc);
      columnWidth = (int) font.getStringBounds("W", frc).getWidth() + 2;
      rowHeight = (int) (font.getStringBounds("g", frc).getHeight()
          + lm.getDescent() + lm.getLeading());

      resize(columnWidth * screen.getColumns(), rowHeight * (screen.getRows() + 2));

      // set the offsets for the screen centering.
      offsetFromLeft = (width - getWidth()) / 2;
      offsetFromTop = (height - getHeight()) / 2;
      if (offsetFromLeft < 0)
        offsetFromLeft = 0;
      if (offsetFromTop < 0)
        offsetFromTop = 0;

      drawOIA();

      updateFont = false;
    }

  }

  public final Dimension getPreferredSize() {

    return new Dimension(columnWidth * screen.getColumns(), rowHeight * (screen.getRows() + 2));

  }

  public BufferedImage getImageBuffer(int width, int height) {
    int width2 = columnWidth * screen.getColumns();
    int height2 = rowHeight * (screen.getRows() + 2);
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
  public Graphics2D drawOIA() {

    int numRows = screen.getRows();

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
    textArea.setRect(0, 0, bi.getWidth(null), (rowHeight * (numRows)));
    commandLineArea.setRect(0, rowHeight * (numRows + 1), bi.getWidth(null), rowHeight * (numRows + 1));
    allScreenArea.setRect(0, 0, bi.getWidth(null), bi.getHeight(null));
    statusArea.setRect(columnWidth * 9, rowHeight * (numRows + 1), columnWidth * 20, rowHeight);
    positionArea.setRect(bi.getWidth(null) - columnWidth * 6, rowHeight * (numRows + 1), columnWidth * 6, rowHeight);
    messageArea.setRect((float) (statusArea.getX() + statusArea.getWidth()) + columnWidth + columnWidth,
        rowHeight * (numRows + 1),
        columnWidth + columnWidth,
        rowHeight);
    keyBufferIndicatorArea.setRect((float) (statusArea.getX() + statusArea.getWidth()) + (20 * columnWidth),
        rowHeight * (numRows + 1),
        columnWidth + columnWidth,
        rowHeight);
    scriptIndicatorArea.setRect((float) (statusArea.getX() + statusArea.getWidth()) + (16 * columnWidth),
        rowHeight * (numRows + 1),
        columnWidth + columnWidth,
        rowHeight);
    insertIndicatorArea.setRect((float) (statusArea.getX() + statusArea.getWidth()) + (25 * columnWidth),
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


  public void drawCursor(int row, int col) {

    int botOffset = cursorBottOffset;
    boolean insertMode = screen.getOIA().isInsertMode();

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

    Rectangle r = cursor.getBounds();
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

    g2.fill(positionArea);
    g2.setColor(colorWhite);

    g2.drawString((row + 1) + "/" + (col + 1)
        , (float) positionArea.getX(),
        (float) positionArea.getY() + rowHeight);
    updateImage(positionArea.getBounds());
    g2.dispose();

  }

  private void drawScriptRunning(Color color) {
    Graphics2D g2d = (Graphics2D) bi.getGraphics();
    g2d.setColor(color);

    // set the points for the polygon
    int[] xs = {(int) scriptIndicatorArea.getX(),
        (int) scriptIndicatorArea.getX(),
        (int) scriptIndicatorArea.getX() + (int) (scriptIndicatorArea.getWidth())};
    int[] ys = {(int) scriptIndicatorArea.getY(),
        (int) scriptIndicatorArea.getY() + (int) scriptIndicatorArea.getHeight(),
        (int) scriptIndicatorArea.getY() + (int) (scriptIndicatorArea.getHeight() / 2)};

    // now lets draw it
    g2d.fillPolygon(xs, ys, 3);
    g2d.setClip(scriptIndicatorArea);

    // get rid of the pointers
    g2d.dispose();
  }

  private void eraseScriptRunning(Color color) {
    Graphics2D g2d = (Graphics2D) bi.getGraphics();
    g2d.setColor(color);
    g2d.fill(scriptIndicatorArea);
    g2d.dispose();
  }

  /**
   * Returns a pointer to the graphics area that we can draw on
   */
  public Graphics2D getDrawingArea() {
    return bi.createGraphics();
  }

  public synchronized void drawImageBuffer(Graphics2D gg2d, int x, int y, int width, int height) {

    /**
     * @todo this is a hack and should be fixed at the root of the problem
     */
    if (gg2d == null) {
      log.debug(" we got a null graphicPlane object ");
      return;
    }
    gg2d.drawImage(bi.getSubimage(x, y, width, height), null, x + offsetFromLeft, y + offsetFromTop);
  }

  protected void updateImage(int x, int y, int width, int height) {


    // check for selected area and erase it before updating screen
    if (gui.rubberband != null && gui.rubberband.isAreaSelected()) {
      gui.rubberband.erase();
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
    if (gui.rubberband != null && gui.rubberband.isAreaSelected()) {
      gui.rubberband.draw();
    }

    if (x == 0)
      width += offsetFromLeft;
    else
      x += offsetFromLeft;
    if (y == 0)
      height += offsetFromTop;
    else
      y += offsetFromTop;

    final int heightf = height;
    final int widthf = width;
    final int xf = x;
    final int yf = y;
    try {
      SwingUtilities.invokeLater(new Runnable() {
        public void run() {
          gui.repaint(xf, yf, widthf, heightf);
        }
      });

    } catch (Exception exc) {
      log.warn("setStatus(ON) " + exc.getMessage());

    }

  }

  protected void updateImage(Rectangle r) {
    updateImage(r.x, r.y, r.width, r.height);
  }

  public synchronized void drawImageBuffer(Graphics2D gg2d) {

    /**
     * @todo this is a hack and should be fixed at the root of the problem
     */
    if (gg2d == null) {
      log.debug(" we got a null graphicPlane object ");
      return;
    }
    gg2d.drawImage(bi, null, offsetFromLeft, offsetFromTop);
  }

  /**
   * Returns a pointer to the graphics area that we can write on
   */
  public Graphics2D getWritingArea(Font font) {

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
    return g2;
  }

  public void setUseAntialias(boolean antialiased) {
    this.antialiased = antialiased;
  }

  private void setStatus(ScreenOIA oia) {

    int attr = oia.getLevel();
    int value = oia.getInputInhibited();
    String s = oia.getInhibitedText();
    Graphics2D g2d = getWritingArea(font);
    if (g2d == null)
      return;

    try {
      g2d.setColor(colorBg);
      g2d.fill(statusArea);

      float Y = ((int) statusArea.getY() + rowHeight) - (lm.getLeading() + lm.getDescent());

      switch (attr) {

        case ScreenOIA.OIA_LEVEL_INPUT_INHIBITED:
          if (value == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT) {
            g2d.setColor(colorWhite);

            if (s != null)
              g2d.drawString(s, (float) statusArea.getX(), Y);
            else
              g2d.drawString(xSystem, (float) statusArea.getX(), Y);
          }
          break;
        case ScreenOIA.OIA_LEVEL_INPUT_ERROR:
          if (value == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT) {
            g2d.setColor(colorRed);

            if (s != null)
              g2d.drawString(s, (float) statusArea.getX(), Y);
            else
              g2d.drawString(xError, (float) statusArea.getX(), Y);

          }
          break;

      }
      updateImage(statusArea.getBounds());
      g2d.dispose();
    } catch (Exception e) {

      log.warn(" gui graphics setStatus " + e.getMessage());

    }
  }

  public final void drawChar(Graphics2D g, int pos, int row, int col) {
    Rectangle csArea = new Rectangle();
    char sChar[] = new char[1];
    int attr = updateRect.getAttributePlane()[pos];
    sChar[0] = updateRect.getTextPlane()[pos];
    setDrawAttr(pos);
    boolean attributePlace = updateRect.getIsAttributePlane()[pos] != 0;
    int whichGui = updateRect.getGraphicPlane()[pos];
    boolean useGui = whichGui != 0;

    csArea = modelToView(row, col, csArea);

    int x = csArea.x;
    int y = csArea.y;
    int cy = (int) (y + rowHeight - (lm.getDescent() + lm.getLeading()));

    if (showHex && attributePlace) {
      Font f = g.getFont();
      Font k = f.deriveFont(f.getSize2D() / 2);
      g.setFont(k);
      g.setColor(colorHexAttr);
      char[] a = Integer.toHexString(attr).toCharArray();
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
                drawWinUpperLeft(g, WINDOW_GRAPHIC, colorBlue, x, y, columnWidth, rowHeight);
              } else {
                drawWinUpperLeft(g, WINDOW_NORMAL, fg, x, y, columnWidth, rowHeight);
              }
            }
            break;

          case TN5250jConstants.UPPER:
            if (sChar[0] == '.') {
              if (screen.isUsingGuiInterface()) {
                drawWinUpper(g, WINDOW_GRAPHIC, colorBlue, x, y, columnWidth, rowHeight);
              } else {
                drawWinUpper(g, WINDOW_NORMAL, fg, x, y, columnWidth, rowHeight);
              }
            }
            break;

          case TN5250jConstants.UPPER_RIGHT:
            if (sChar[0] == '.') {
              if (screen.isUsingGuiInterface()) {
                drawWinUpperRight(g, WINDOW_GRAPHIC, colorBlue, x, y, columnWidth, rowHeight);
              } else {
                drawWinUpperRight(g, WINDOW_NORMAL, fg, x, y, columnWidth, rowHeight);
              }
            }
            break;

          case TN5250jConstants.GUI_LEFT:
            if (sChar[0] == ':') {
              if (screen.isUsingGuiInterface()) {
                drawWinLeft(g, WINDOW_GRAPHIC, bg, x, y, columnWidth, rowHeight);
              } else {
                drawWinLeft(g, WINDOW_NORMAL, fg, x, y, columnWidth, rowHeight);
                g.drawLine(x + columnWidth / 2, y, x + columnWidth / 2, y + rowHeight);
              }
            }
            break;

          case TN5250jConstants.GUI_RIGHT:
            if (sChar[0] == ':') {
              if (screen.isUsingGuiInterface()) {
                drawWinRight(g, WINDOW_GRAPHIC, bg, x, y, columnWidth, rowHeight);
              } else {
                drawWinRight(g, WINDOW_NORMAL, fg, x, y, columnWidth, rowHeight);
              }
            }
            break;

          case TN5250jConstants.LOWER_LEFT:
            if (sChar[0] == ':') {
              if (screen.isUsingGuiInterface()) {
                drawWinLowerLeft(g, WINDOW_GRAPHIC, bg, x, y, columnWidth, rowHeight);
              } else {
                drawWinLowerLeft(g, WINDOW_NORMAL, fg, x, y, columnWidth, rowHeight);
              }
            }
            break;

          case TN5250jConstants.BOTTOM:
            if (sChar[0] == '.') {
              if (screen.isUsingGuiInterface()) {
                drawWinBottom(g, WINDOW_GRAPHIC, bg, x, y, columnWidth, rowHeight);
              } else {
                drawWinBottom(g, WINDOW_NORMAL, fg, x, y, columnWidth, rowHeight);
              }
            }
            break;

          case TN5250jConstants.LOWER_RIGHT:
            if (sChar[0] == ':') {
              if (screen.isUsingGuiInterface()) {
                drawWinLowerRight(g, WINDOW_GRAPHIC, bg, x, y, columnWidth, rowHeight);
              } else {
                drawWinLowerRight(g, WINDOW_NORMAL, fg, x, y, columnWidth, rowHeight);
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
                g.drawChars(DUP_CHAR, 0, 1, x + 1, cy - 2);
              else
                g.drawChars(sChar, 0, 1, x + 1, cy - 2);
            else if (sChar[0] == 0x1C)
              g.drawChars(DUP_CHAR, 0, 1, x, cy - 2);
            else
              g.drawChars(sChar, 0, 1, x, cy - 2);
          } catch (IllegalArgumentException iae) {
            log.warn("drawChar iae", iae);
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
          draw3DLeft(g, INSET, x, y, columnWidth, rowHeight);

          break;
        case TN5250jConstants.FIELD_MIDDLE:
          draw3DMiddle(g, INSET, x, y, columnWidth, rowHeight);
          break;
        case TN5250jConstants.FIELD_RIGHT:
          draw3DRight(g, INSET, x, y, columnWidth, rowHeight);
          break;

        case TN5250jConstants.FIELD_ONE:
          draw3DOne(g, INSET, x, y, columnWidth, rowHeight);
          break;

        case TN5250jConstants.BUTTON_LEFT:
        case TN5250jConstants.BUTTON_LEFT_UP:
        case TN5250jConstants.BUTTON_LEFT_DN:
        case TN5250jConstants.BUTTON_LEFT_EB:

          draw3DLeft(g, RAISED, x, y, columnWidth, rowHeight);

          break;

        case TN5250jConstants.BUTTON_MIDDLE:
        case TN5250jConstants.BUTTON_MIDDLE_UP:
        case TN5250jConstants.BUTTON_MIDDLE_DN:
        case TN5250jConstants.BUTTON_MIDDLE_EB:

          draw3DMiddle(g, RAISED, x, y, columnWidth, rowHeight);
          break;

        case TN5250jConstants.BUTTON_RIGHT:
        case TN5250jConstants.BUTTON_RIGHT_UP:
        case TN5250jConstants.BUTTON_RIGHT_DN:
        case TN5250jConstants.BUTTON_RIGHT_EB:

          draw3DRight(g, RAISED, x, y, columnWidth, rowHeight);

          break;

        // scroll bar
        case TN5250jConstants.BUTTON_SB_UP:
          drawScrollBar(g, RAISED, 1, x, y, columnWidth, rowHeight, colorWhite, colorBg);
          break;

        // scroll bar
        case TN5250jConstants.BUTTON_SB_DN:

          drawScrollBar(g, RAISED, 2, x, y, columnWidth, rowHeight, colorWhite, colorBg);
          break;

        // scroll bar
        case TN5250jConstants.BUTTON_SB_GUIDE:
          drawScrollBar(g, INSET, 0, x, y, columnWidth, rowHeight, colorWhite, colorBg);
          break;

        // scroll bar
        case TN5250jConstants.BUTTON_SB_THUMB:
          drawScrollBar(g, INSET, 3, x, y, columnWidth, rowHeight, colorWhite, colorBg);
          break;
      }
    }

  }

  private void onScreenSizeChanged(int rows, int cols) {
    log.info("screen size change");
    gui.resizeMe();
  }

  private void onScreenChanged(int which, int sr, int sc, int er, int ec) {
    if (which == 3 || which == 4) {
      drawCursor(sr, sc);
      return;
    }

    if (hotSpots) {
      screen.checkHotSpots();
    }
    updateRect = new ScreenPlanesImpl(screen, sr, sc, er, ec);

    Rectangle clipper = new Rectangle();
    clipper.x = sc * columnWidth;
    clipper.y = sr * rowHeight;
    clipper.width = ((ec - sc) + 1) * columnWidth;
    clipper.height = ((er - sr) + 1) * rowHeight;

    gg2d.setClip(clipper.getBounds());
    gg2d.setColor(colorBg);
    gg2d.fillRect(clipper.x, clipper.y, clipper.width, clipper.height);

    for (int pos = 0; sr <= er; ) {
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

  public void onOIAChanged(ScreenOIA changedOIA, int change) {

    switch (changedOIA.getLevel()) {

      case ScreenOIA.OIA_LEVEL_KEYS_BUFFERED:
        if (changedOIA.isKeysBuffered()) {
          Graphics2D g2d = getWritingArea(font);
          float Y = (rowHeight * (screen.getRows() + 2))
              - (lm.getLeading() + lm.getDescent());
          g2d.setColor(colorYellow);
          g2d.drawString("KB", (float) keyBufferIndicatorArea.getX(), Y);

          updateImage(keyBufferIndicatorArea.getBounds());
          g2d.dispose();
        } else {

          Graphics2D g2d = getWritingArea(font);

          g2d.setColor(colorBg);
          g2d.fill(keyBufferIndicatorArea);
          updateImage(keyBufferIndicatorArea.getBounds());
          g2d.dispose();


        }
        break;
      case ScreenOIA.OIA_LEVEL_MESSAGE_LIGHT_OFF:
        Graphics2D g2d = getWritingArea(font);

        g2d.setColor(colorBg);
        g2d.fill(messageArea);
        updateImage(messageArea.getBounds());
        g2d.dispose();
        break;
      case ScreenOIA.OIA_LEVEL_MESSAGE_LIGHT_ON:
        g2d = getWritingArea(font);
        float Y = (rowHeight * (screen.getRows() + 2))
            - (lm.getLeading() + lm.getDescent());
        g2d.setColor(colorBlue);
        g2d.drawString("MW", (float) messageArea.getX(), Y);
        updateImage(messageArea.getBounds());
        g2d.dispose();
        break;
      case ScreenOIA.OIA_LEVEL_SCRIPT:
        if (changedOIA.isScriptActive()) {
          drawScriptRunning(colorGreen);
          updateImage(scriptIndicatorArea.getBounds());
        } else {
          eraseScriptRunning(colorBg);
          updateImage(scriptIndicatorArea.getBounds());

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
          g2d.drawLine((int) insertIndicatorArea.getX(), (int) Y, (int) (insertIndicatorArea.getX() + ((insertIndicatorArea.getWidth() / 2) - 1)), (int) (Y - (rowHeight / 2)));
          g2d.drawLine((int) (insertIndicatorArea.getX() + insertIndicatorArea.getWidth() - 1), (int) Y, (int) (insertIndicatorArea.getX() + (insertIndicatorArea.getWidth() / 2)), (int) (Y - (rowHeight / 2)));
          updateImage(insertIndicatorArea.getBounds());
          g2d.dispose();
        } else {
          g2d = getWritingArea(font);
          g2d.setColor(colorBg);
          g2d.fill(insertIndicatorArea);
          updateImage(insertIndicatorArea.getBounds());
          g2d.dispose();

        }
        break;

    }
  }

  /**
   * get the
   */
  public Rectangle2D getTextArea() {
    return textArea;
  }

  public Rectangle2D getScreenArea() {
    return allScreenArea;
  }

  public Rectangle2D getCommandLineArea() {
    return commandLineArea;
  }

  public Rectangle2D getStatusArea() {
    return statusArea;
  }

  public Rectangle2D getPositionArea() {
    return positionArea;
  }

  public Rectangle2D getMessageArea() {
    return messageArea;
  }

  public Rectangle2D getInsertIndicatorArea() {
    return insertIndicatorArea;
  }

  public Rectangle2D getKBIndicatorArea() {
    return keyBufferIndicatorArea;
  }

  public Rectangle2D getScriptIndicatorArea() {
    return scriptIndicatorArea;
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

  public int getWidth(ImageObserver io) {

    synchronized (lock) {
      // tell waiting threads to wake up
      lock.notifyAll();
      return bi.getWidth(io);
    }
  }

  public int getHeight(ImageObserver io) {
    synchronized (lock) {
      // tell waiting threads to wake up
      lock.notifyAll();
      return bi.getHeight(io);
    }
  }

  public final Rectangle modelToView(int row, int col) {
    return modelToView(row, col, new Rectangle());
  }

  public final Rectangle modelToView(int row, int col, Rectangle r) {

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

  private Color getColor(char color, boolean background) {
    final int c;
    if (background) {
      // background
      c = (color & 0xff00) >> 8;
    } else {
      // foreground
      c = color & 0x00ff;
    }
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

  private void setDrawAttr(int pos) {
    colSep = false;
    underLine = false;
    nonDisplay = false;
    fg = getColor(updateRect.getColorPlane()[pos], false);
    bg = getColor(updateRect.getColorPlane()[pos], true);
    underLine = (updateRect.getExtendedPlane()[pos] & TN5250jConstants.EXTENDED_5250_UNDERLINE) != 0;
    colSep = (updateRect.getExtendedPlane()[pos] & TN5250jConstants.EXTENDED_5250_COL_SEP) != 0;
    nonDisplay = (updateRect.getExtendedPlane()[pos] & TN5250jConstants.EXTENDED_5250_NON_DSP) != 0;
  }

}
