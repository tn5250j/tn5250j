/**
 * Title: GuiGraphicBuffer.java
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.5
 *
 * Description:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j;

import java.awt.image.*;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.*;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Font;
import java.awt.font.*;
import java.awt.event.*;
import java.beans.*;
import java.awt.*;

import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.event.ScreenListener;
import org.tn5250j.tools.logging.*;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.event.*;
import org.tn5250j.framework.tn5250.*;

public class GuiGraphicBuffer implements ScreenOIAListener, ScreenListener,
                                          PropertyChangeListener,
                                          SessionConfigListener,
                                          ActionListener,
                                          TN5250jConstants {

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
   private int width;
   private int height;
   private Rectangle2D cursor = new Rectangle2D.Float();
   private final static String xSystem = "X - System";
   private final static String xError = "X - II";
   private int crossRow;
   private int crossCol;
   private Rectangle crossRect = new Rectangle();
   protected int offTop = 0;   // offset from top
   protected int offLeft = 0;  // offset from left
   private boolean resized = false;
   private boolean antialiased = true;
   private Graphics2D gg2d;
   private Screen5250 screen;
   private Data updateRect;
   protected int columnWidth;
   protected int rowHeight;
   private Gui5250 gui;

	LineMetrics lm;
	Font font;
	int lenScreen;
	boolean showHex;
	Color colorBlue;
	Color colorWhite;
	Color colorRed;
	Color colorGreen;
	Color colorPink;
	Color colorYellow;
	Color colorBg;
	Color colorTurq;
	Color colorGUIField;
	Color colorCursor;
	Color colorSep;
	Color colorHexAttr;
	protected int crossHair = 0;
	private boolean updateFont;
	protected int cursorSize = 0;
	protected boolean hotSpots = false;
	private float sfh = 1.2f; // font scale height
	private float sfw = 1.0f; // font scale height
	private float ps132 = 0; // Font point size
	protected boolean guiInterface = false;
	public boolean guiShowUnderline = true;
	protected int cursorBottOffset;
	private boolean defaultPrinter;
	protected boolean rulerFixed;
	private boolean feError;
	private javax.swing.Timer blinker;
   private int colSepLine = 0;
	private StringBuffer hsMore = new StringBuffer("More...");
	private StringBuffer hsBottom = new StringBuffer("Bottom");
   private Rectangle workR = new Rectangle();

	private SessionConfig config;

   protected Rectangle clipper;

   private TN5250jLogger log = TN5250jLogFactory.getLogger ("GFX");

   public GuiGraphicBuffer (Screen5250 screen, Gui5250 gui, SessionConfig config) {

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

		gui.setFont(font);

      getSettings();
      FontRenderContext frc = new FontRenderContext(font.getTransform(),
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

	/**
	 * This is for blinking cursor but should be moved out
	 */
	public void actionPerformed(ActionEvent actionevent) {
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

   public void resize(int width, int height) {

      if (bi.getWidth() != width || bi.getHeight()  != height) {
//         synchronized (lock) {
            bi = null;
            bi = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
            this.width = width;
            this.height = height;
            resized = true;
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

		if (guiInterface)
			colorBg = Color.lightGray;
		else
			colorBg = Color.black;

		colorCursor = Color.white;

		if (!config.isPropertyExists("colorBg"))
			setProperty("colorBg", Integer.toString(colorBg.getRGB()));
		else {
			colorBg = getColorProperty("colorBg");
		}
      gui.setBackground(colorBg);

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
	public void loadProps() {

		loadColors();

		if (config.isPropertyExists("colSeparator")) {
			if (getStringProperty("colSeparator").equals("Line"))
				colSepLine = 0;
			if (getStringProperty("colSeparator").equals("ShortLine"))
				colSepLine = 1;
			if (getStringProperty("colSeparator").equals("Dot"))
				colSepLine = 2;
			if (getStringProperty("colSeparator").equals("Hide"))
				colSepLine = 3;
		}

		if (config.isPropertyExists("showAttr")) {
			if (getStringProperty("showAttr").equals("Hex"))
				showHex = true;
		}

		if (config.isPropertyExists("guiInterface")) {
			if (getStringProperty("guiInterface").equals("Yes")) {
				screen.setUseGUIInterface(true);
            guiInterface = true;
			}
			else {
				screen.setUseGUIInterface(false);
            guiInterface = false;
			}
		}

		if (config.isPropertyExists("guiShowUnderline")) {
			if (getStringProperty("guiShowUnderline").equals("Yes"))
				guiShowUnderline = true;
			else
				guiShowUnderline = false;
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

		if (config.isPropertyExists("colSeparator")) {
			if (getStringProperty("colSeparator").equals("Line"))
				colSepLine = 0;
			if (getStringProperty("colSeparator").equals("ShortLine"))
				colSepLine = 1;
			if (getStringProperty("colSeparator").equals("Dot"))
				colSepLine = 2;
			if (getStringProperty("colSeparator").equals("Hide"))
				colSepLine = 3;
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

		if (config.isPropertyExists("defaultPrinter")) {
			if (getStringProperty("defaultPrinter").equals("Yes"))
				defaultPrinter = true;
			else
				defaultPrinter = false;
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

		if (config.getStringProperty("cursorBlink").equals("Yes")) {
			blinker = new javax.swing.Timer(500, this);
			blinker.start();
		}
	}

	protected final String getStringProperty(String prop) {

		return config.getStringProperty(prop);

	}

	protected final Color getColorProperty(String prop) {

		return config.getColorProperty(prop);

	}

	protected final float getFloatProperty(String prop) {

		return config.getFloatProperty(prop);

	}

	protected final int getIntProperty(String prop) {

		return config.getIntegerProperty(prop);

	}

	protected final void setProperty(String key, String val) {

		config.setProperty(key, val);

	}

   /**
    * Update the configuration settings
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
			if (pce.getNewValue().equals("Yes"))
				rulerFixed = true;
			else
				rulerFixed = false;
		}

		if (pn.equals("colSeparator")) {
			if (pce.getNewValue().equals("Line"))
				colSepLine = 0;
			if (pce.getNewValue().equals("ShortLine"))
				colSepLine = 1;
			if (pce.getNewValue().equals("Dot"))
				colSepLine = 2;
			if (pce.getNewValue().equals("Hide"))
				colSepLine = 3;
		}

		if (pn.equals("showAttr")) {
			if (pce.getNewValue().equals("Hex"))
				showHex = true;
			else
				showHex = false;
		}

		if (pn.equals("guiInterface")) {
			if (pce.getNewValue().equals("Yes")) {
            screen.setUseGUIInterface(true);
				guiInterface = true;
			}
			else {
            screen.setUseGUIInterface(true);
				guiInterface = false;
			}
		}

		if (pn.equals("guiShowUnderline")) {
			if (pce.getNewValue().equals("Yes"))
				guiShowUnderline = true;
			else
				guiShowUnderline = false;
		}

		if (pn.equals("hotspots")) {
			if (pce.getNewValue().equals("Yes"))
				hotSpots = true;
			else
				hotSpots = false;
		}

		if (pn.equals("defaultPrinter")) {
			if (pce.getNewValue().equals("Yes"))
				defaultPrinter = true;
			else
				defaultPrinter = false;
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

		int s0 = y / rowHeight;
		int s1 = x / columnWidth;

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
		point.x = (columnWidth * c) + offLeft;
		point.y = (rowHeight * r) + offTop;

	}

	public boolean isWithinScreenArea(int x, int y) {

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
		int x =  ((columnWidth * screen.getCol(pos)) + columnWidth) - 1;
		int y = ((rowHeight * screen.getRow(pos)) + rowHeight) - 1;

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
		LineMetrics l;
		FontRenderContext f = null;
		k = GUIGraphicsUtils.getDerivedFont(font, width, height, screen.getRows(),
				screen.getColumns(), sfh, sfw, ps132);
		f = new FontRenderContext(k.getTransform(), true, true);

		l = k.getLineMetrics("Wy", f);

		if (font.getSize() != k.getSize() || updateFont
				|| (offLeft != (width - bi.getWidth()) / 2)
				|| (offTop != (height - bi.getHeight()) / 2)) {

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

   public BufferedImage getImageBuffer(int width, int height) {


      int width2 = columnWidth * screen.getColumns();
      int height2 = rowHeight * (screen.getRows() + 2);
//      synchronized (lock) {
         if (bi == null || bi.getWidth() != width2 || bi.getHeight() != height2) {
            // allocate a buffer Image with appropriate size
            bi = new BufferedImage(width2,height2,BufferedImage.TYPE_INT_RGB);
            this.width = width2;
            this.height = height2;
            resized = true;
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
      public Graphics2D drawOIA () {

      	int numRows = screen.getRows();
      	int numCols = screen.getColumns();

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
         g2d.fillRect(0,0,bi.getWidth(null),bi.getHeight(null));
         tArea.setRect(0,0,bi.getWidth(null),(rowHeight * (numRows)));
         cArea.setRect(0,rowHeight * (numRows + 1),bi.getWidth(null),rowHeight * (numRows + 1));
         aArea.setRect(0,0,bi.getWidth(null),bi.getHeight(null));
         sArea.setRect(columnWidth * 9,rowHeight * (numRows + 1),columnWidth * 20,rowHeight);
         pArea.setRect(bi.getWidth(null) - columnWidth * 6,rowHeight * (numRows + 1),columnWidth * 6,rowHeight);
         mArea.setRect((float)(sArea.getX()+ sArea.getWidth()) + columnWidth + columnWidth,
               rowHeight * (numRows + 1),
               columnWidth + columnWidth,
               rowHeight);
         kbArea.setRect((float)(sArea.getX()+ sArea.getWidth()) + (20 * columnWidth),
               rowHeight * (numRows + 1),
               columnWidth + columnWidth,
               rowHeight);
         scriptArea.setRect((float)(sArea.getX()+ sArea.getWidth()) + (16 * columnWidth),
               rowHeight * (numRows + 1),
               columnWidth + columnWidth,
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


   public void drawCursor(int row, int col)   {

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
         r.setSize(r.width,r.height);

         g2.setColor(colorCursor);
         g2.setXORMode(colorBg);

         g2.fill(cursor);

         updateImage(r);

         if (!rulerFixed) {
            crossRow = row;
            crossCol = col;
            crossRect.setBounds(r);
         }
         else {
            if (crossHair == 0) {
               crossRow = row;
               crossCol = col;
               crossRect.setBounds(r);
            }
         }

         switch (crossHair) {
            case 1:  // horizontal
               g2.drawLine(0,(rowHeight * (crossRow + 1))- botOffset,
                           bi.getWidth(null),
                           (rowHeight * (crossRow + 1))- botOffset);
               updateImage(0,rowHeight * (crossRow + 1)- botOffset,
                              bi.getWidth(null),1);
               break;
            case 2:  // vertical
               g2.drawLine(crossRect.x,0,crossRect.x,bi.getHeight(null) - rowHeight - rowHeight);
               updateImage(crossRect.x,0,1,bi.getHeight(null) - rowHeight - rowHeight);
               break;

            case 3:  // horizontal & vertical
               g2.drawLine(0,(rowHeight * (crossRow + 1))- botOffset,
                           bi.getWidth(null),
                           (rowHeight * (crossRow + 1))- botOffset);
               g2.drawLine(crossRect.x,0,crossRect.x,bi.getHeight(null) - rowHeight - rowHeight);
               updateImage(0,rowHeight * (crossRow + 1)- botOffset,
                              bi.getWidth(null),1);
               updateImage(crossRect.x,0,1,bi.getHeight(null) - rowHeight - rowHeight);
               break;
         }

         g2.dispose();
         g2 = getWritingArea(font);
         g2.setPaint(colorBg);

         g2.fill(pArea);
         g2.setColor(colorWhite);

         g2.drawString((row + 1) + "/" + (col + 1)
                        ,(float)pArea.getX(),
                        (float)pArea.getY() + rowHeight);
         updateImage(pArea.getBounds());
         g2.dispose();

   }

   private void drawScriptRunning(Color color) {

      Graphics2D g2d;

      // get ourselves a global pointer to the graphics
      g2d = (Graphics2D)bi.getGraphics();

      g2d.setColor(color);

      // set the points for the polygon
      int[] xs = {(int)scriptArea.getX(),
                  (int)scriptArea.getX(),
                  (int)scriptArea.getX() + (int)(scriptArea.getWidth())};
      int[] ys = {(int)scriptArea.getY(),
                  (int)scriptArea.getY() + (int)scriptArea.getHeight(),
                  (int)scriptArea.getY() + (int)(scriptArea.getHeight() / 2)};

      // now lets draw it
      g2d.fillPolygon(xs,ys,3);
      g2d.setClip(scriptArea);

      // get rid of the pointers
      g2d.dispose();


   }

   private void eraseScriptRunning(Color color) {

      Graphics2D g2d;

      // get ourselves a global pointer to the graphics
      g2d = (Graphics2D)bi.getGraphics();

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

   public synchronized void drawImageBuffer(Graphics2D gg2d,int x, int y, int width, int height) {

       /**
        * @todo this is a hack and should be fixed at the root of the problem
        */
      if (gg2d == null) {
         log.debug(" we got a null graphic object ");
         return;
      }

//      synchronized (lock) {
         gg2d.drawImage(bi.getSubimage(x,y,width,height),null,x + offLeft,y+ offTop);
         // tell waiting threads to wake up
//         lock.notifyAll();
//      }

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
			width += offLeft;
		else
			x += offLeft;
		if (y == 0)
			height += offTop;
		else
			y += offTop;

		gui.repaint(x, y, width, height);

	}

	protected void updateImage(Rectangle r) {
		updateImage(r.x, r.y, r.width, r.height);
	}

   public synchronized void drawImageBuffer(Graphics2D gg2d) {

       /**
        * @todo this is a hack and should be fixed at the root of the problem
        */
      if (gg2d == null) {
		 log.debug(" we got a null graphic object ");
         return;
      }

//      synchronized (lock) {

         gg2d.drawImage(bi,null,offLeft,offTop);
         // tell waiting threads to wake up
//         lock.notifyAll();
//      }


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

   public void setUseAntialias(boolean antialiased) {
      this.antialiased = antialiased;
   }

   private void setStatus(ScreenOIA oia) {

      int attr = oia.getLevel();
      int value = oia.getInputInhibited();
      String s = oia.getInhibitedText();
      Graphics2D g2d = getWritingArea(font);
//      log.info(attr + ", " + value + ", " + s);
      if (g2d == null)
         return;

      try {
         g2d.setColor(colorBg);
         g2d.fill(sArea);

         float Y = ((int)sArea.getY() + rowHeight)- (lm.getLeading() + lm.getDescent());

         switch (attr) {

            case ScreenOIA.OIA_LEVEL_INPUT_INHIBITED:
               if (value == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT) {
                  g2d.setColor(colorWhite);

                  if (s != null)
                     g2d.drawString(s,(float)sArea.getX(),Y);
                  else
                     g2d.drawString(xSystem,(float)sArea.getX(),Y);
               }
               break;
            case ScreenOIA.OIA_LEVEL_INPUT_ERROR:
               if (value == ScreenOIA.INPUTINHIBITED_SYSTEM_WAIT) {
                  g2d.setColor(colorRed);

                  if (s != null)
                     g2d.drawString(s,(float)sArea.getX(),Y);
                  else
                     g2d.drawString(xError,(float)sArea.getX(),Y);

               }
               break;

         }
   		updateImage(sArea.getBounds());
         g2d.dispose();
      }
      catch (Exception e) {

         log.warn(" gui graphics setStatus " + e.getMessage());

      }
   }

   // Dup Character array for display output
   public static final transient char[] dupChar = {'*'};
   public final void drawChar(Graphics2D g, int pos, int row, int col) {

      int attr = updateRect.attr[pos];
      sChar[0] = updateRect.text[pos];
      setDrawAttr(pos);
      boolean attributePlace = updateRect.isAttr[pos] == 0 ? false : true;
      int whichGui = updateRect.graphic[pos];
      boolean useGui = whichGui == 0 ? false : true;

      csArea = modelToView(row, col, csArea);

      int x = csArea.x;
      int y = csArea.y;
      int cy = (int)(y + rowHeight - (lm.getDescent() + lm.getLeading()));

      if (showHex && attributePlace) {
         Font f = g.getFont();

         Font k = f.deriveFont(f.getSize2D()/2);
         g.setFont(k);
         g.setColor(colorHexAttr);
         char[] a = Integer.toHexString(attr).toCharArray();
         g.drawChars(a, 0, 1, x, y + (int)(rowHeight /2));
         g.drawChars(a, 1, 1, x+(int)(columnWidth/2),
            (int)(y + rowHeight - (lm.getDescent() + lm.getLeading())-2));
         g.setFont(f);
      }

      if(!nonDisplay && !attributePlace) {

         if (!useGui) {
            g.setColor(bg);
            g.fill(csArea);
         }
         else {

            if (bg == colorBg && whichGui >= FIELD_LEFT && whichGui <= FIELD_ONE)
               g.setColor(colorGUIField);
            else
               g.setColor(bg);

            g.fill(csArea);

         }

         if (useGui && (whichGui < FIELD_LEFT)) {
            int w = 0;

            g.setColor(fg);

            switch (whichGui) {

               case UPPER_LEFT:
                  if (sChar[0] == '.') {
                     if (screen.isUsingGuiInterface()) {
                        GUIGraphicsUtils.drawWinUpperLeft(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             colorBlue,
                                             x,y,columnWidth,rowHeight);

                     }
                     else {

                        GUIGraphicsUtils.drawWinUpperLeft(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);

                     }
                  }
               break;
               case UPPER:
                  if (sChar[0] == '.') {

                     if (screen.isUsingGuiInterface()) {
                        GUIGraphicsUtils.drawWinUpper(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             colorBlue,
                                             x,y,columnWidth,rowHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinUpper(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);
                     }
                  }
               break;
               case UPPER_RIGHT:
                  if (sChar[0] == '.') {
                     if (screen.isUsingGuiInterface()) {

                        GUIGraphicsUtils.drawWinUpperRight(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             colorBlue,
                                             x,y,columnWidth,rowHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinUpperRight(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);

                     }
                  }
               break;
               case GUI_LEFT:
                  if (sChar[0] == ':') {
                     if (screen.isUsingGuiInterface()) {
                        GUIGraphicsUtils.drawWinLeft(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,columnWidth,rowHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinLeft(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);

                        g.drawLine(x + columnWidth / 2,
                                    y,
                                    x + columnWidth / 2,
                                    y + rowHeight);
                     }
                  }
               break;
               case GUI_RIGHT:
                  if (sChar[0] == ':') {
                     if (screen.isUsingGuiInterface()) {
                        GUIGraphicsUtils.drawWinRight(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,columnWidth,rowHeight);


                     }
                     else {
                        GUIGraphicsUtils.drawWinRight(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);

                     }
                  }
               break;
               case LOWER_LEFT:
                  if (sChar[0] == ':') {

                     if (screen.isUsingGuiInterface()) {

                        GUIGraphicsUtils.drawWinLowerLeft(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,columnWidth,rowHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinLowerLeft(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);
                     }
                  }
               break;
               case BOTTOM:
                  if (sChar[0] == '.') {

                     if (screen.isUsingGuiInterface()) {


                        GUIGraphicsUtils.drawWinBottom(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,columnWidth,rowHeight);


                     }
                     else {

                        GUIGraphicsUtils.drawWinBottom(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);
                     }
                  }
               break;

               case LOWER_RIGHT:
                  if (sChar[0] == ':') {
                     if (screen.isUsingGuiInterface()) {

                        GUIGraphicsUtils.drawWinLowerRight(g,
                                             GUIGraphicsUtils.WINDOW_GRAPHIC,
                                             bg,
                                             x,y,columnWidth,rowHeight);

                     }
                     else {

                        GUIGraphicsUtils.drawWinLowerRight(g,
                                             GUIGraphicsUtils.WINDOW_NORMAL,
                                             fg,
                                             x,y,columnWidth,rowHeight);

                     }
                  }
               break;

            }
         }

         else {
            if (sChar[0] != 0x0) {
            // use this until we define colors for gui stuff
               if ((useGui && whichGui < BUTTON_LEFT) && (fg == colorGUIField))

                  g.setColor(Color.black);
               else
                  g.setColor(fg);

                  try {
                     if (useGui)

                        if (sChar[0] == 0x1C)
                           g.drawChars(dupChar, 0, 1, x+1, cy -2);
                        else
                           g.drawChars(sChar, 0, 1, x+1, cy -2);
                     else
                        if (sChar[0] == 0x1C)
                           g.drawChars(dupChar, 0, 1, x, cy -2);
                        else
                           g.drawChars(sChar, 0, 1, x, cy -2);
                  }
                  catch (IllegalArgumentException iae) {
                     System.out.println(" drawChar iae " + iae.getMessage());

                  }
            }
            if(underLine ) {

               if (!useGui || guiShowUnderline) {
                  g.setColor(fg);
                  g.drawLine(x, (int)(y + (rowHeight - (lm.getLeading() + lm.getDescent()))), (int)(x + columnWidth), (int)(y + (rowHeight -(lm.getLeading() + lm.getDescent()))));

               }
            }

            if(colSep) {
               g.setColor(colorSep);
               switch (colSepLine) {
                  case 0:  // line
                     g.drawLine(x, y, x, y + rowHeight - 1);
                     g.drawLine(x + columnWidth - 1, y, x + columnWidth - 1, y + rowHeight);
                     break;
                  case 1:  // short line
                     g.drawLine(x,  y + rowHeight - (int)lm.getLeading()-4, x, y + rowHeight);
                     g.drawLine(x + columnWidth - 1, y + rowHeight - (int)lm.getLeading()-4, x + columnWidth - 1, y + rowHeight);
                     break;
                  case 2:  // dot
                     g.drawLine(x,  y + rowHeight - (int)lm.getLeading()-3, x, y + rowHeight - (int)lm.getLeading()-4);
                     g.drawLine(x + columnWidth - 1, y + rowHeight - (int)lm.getLeading()-3, x + columnWidth - 1, y + rowHeight - (int)lm.getLeading()-4);
                     break;
                  case 3:  // hide
                     break;
               }
            }
         }
      }

      if (useGui & (whichGui >= FIELD_LEFT)) {
            int w = 0;

            switch (whichGui) {

               case FIELD_LEFT:
                  GUIGraphicsUtils.draw3DLeft(g, GUIGraphicsUtils.INSET, x,y,
                                             columnWidth,rowHeight);

               break;
               case FIELD_MIDDLE:
                  GUIGraphicsUtils.draw3DMiddle(g, GUIGraphicsUtils.INSET, x,y,
                                             columnWidth,rowHeight);
               break;
               case FIELD_RIGHT:
                  GUIGraphicsUtils.draw3DRight(g, GUIGraphicsUtils.INSET, x,y,
                                             columnWidth,rowHeight);
               break;

               case FIELD_ONE:
                  GUIGraphicsUtils.draw3DOne(g, GUIGraphicsUtils.INSET, x,y,
                                             columnWidth,rowHeight);

               break;

               case BUTTON_LEFT:
               case BUTTON_LEFT_UP:
               case BUTTON_LEFT_DN:
               case BUTTON_LEFT_EB:

                  GUIGraphicsUtils.draw3DLeft(g, GUIGraphicsUtils.RAISED, x,y,
                                             columnWidth,rowHeight);

                  break;

               case BUTTON_MIDDLE:
               case BUTTON_MIDDLE_UP:
               case BUTTON_MIDDLE_DN:
               case BUTTON_MIDDLE_EB:

                  GUIGraphicsUtils.draw3DMiddle(g, GUIGraphicsUtils.RAISED, x,y,
                                             columnWidth,rowHeight);
                  break;

               case BUTTON_RIGHT:
               case BUTTON_RIGHT_UP:
               case BUTTON_RIGHT_DN:
               case BUTTON_RIGHT_EB:

                  GUIGraphicsUtils.draw3DRight(g, GUIGraphicsUtils.RAISED, x,y,
                                             columnWidth,rowHeight);

               break;

               // scroll bar
               case BUTTON_SB_UP:
                  GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.RAISED, 1, x,y,
                                             columnWidth,rowHeight,
                                             colorWhite,colorBg);
                  break;

               // scroll bar
               case BUTTON_SB_DN:

                  GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.RAISED, 2, x,y,
                                             columnWidth,rowHeight,
                                             colorWhite,colorBg);


                  break;
               // scroll bar
               case BUTTON_SB_GUIDE:

                  GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.INSET, 0,x,y,
                                             columnWidth,rowHeight,
                                             colorWhite,colorBg);


                  break;

               // scroll bar
               case BUTTON_SB_THUMB:

                  GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.INSET, 3,x,y,
                                             columnWidth,rowHeight,
                                             colorWhite,colorBg);


                  break;

            }
         }

   }

   public void onScreenSizeChanged(int rows, int cols) {
      log.info("screen size change");
      gui.resizeMe();
   }

   public void onScreenChanged(int which, int sr, int sc, int er, int ec) {


      if (which == 3 || which == 4) {
//         log.info("cursor updated -> " +  sr + ", " + sc + "-> active " +
//                        screen.cursorActive + " -> shown " + screen.cursorShown);
         drawCursor(sr,sc);
         return;
      }

      if (hotSpots)
         screen.checkHotSpots();

//      log.info("screen updated -> " +  sr + ", " + sc + ", " + er + ", " + ec);

      int rows = er - sr;
		int cols = 0;
		int lc = 0;
		int lr = screen.getPos(sr,sc);
      int numCols = screen.getColumns();


      updateRect = new Data (sr,sc,er,ec);

      int clipX;
      int clipY;
      int clipWidth;
      int clipHeight;

      Rectangle clipper = new Rectangle();

      int pos = 0;

      lc = ec;
      clipper.x      =   sc * columnWidth;
      clipper.y      =   sr * rowHeight;
      clipper.width  = ((ec - sc) + 1) * columnWidth;
      clipper.height =  ((er - sr ) + 1) * rowHeight;

      gg2d.setClip(clipper.getBounds());

		gg2d.setColor(colorBg);

		gg2d.fillRect(clipper.x, clipper.y, clipper.width, clipper.height);

		while (sr <= er) {
			cols = ec - sc;
			lc = sc;
			while (cols-- >= 0) {
				if (sc + cols <= ec) {
					drawChar(gg2d,pos++,sr,lc);
               lc++;
				}
			}
			sr++;
		}

//      System.out.println(" clipping from screen change " + clipper
//                        + " clipping region of paint " + gg2d.getClipBounds());

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
               g2d.drawString("KB", (float) kbArea.getX(), Y);

               updateImage(kbArea.getBounds());
               g2d.dispose();
            }
            else {

               Graphics2D g2d = getWritingArea(font);

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
            }
            else {
               eraseScriptRunning(colorBg);
               updateImage(scriptArea.getBounds());

            }
            break;
         case ScreenOIA.OIA_LEVEL_INPUT_INHIBITED:
         case ScreenOIA.OIA_LEVEL_NOT_INHIBITED:
         case ScreenOIA.OIA_LEVEL_INPUT_ERROR:
            setStatus(changedOIA);
            break;

      }
   }

   /**
    * get the
    */
   public Rectangle2D getTextArea () {
      return tArea;
   }

   public Rectangle2D getScreenArea () {
      return aArea;
   }

   public Rectangle2D getCommandLineArea () {
      return cArea;
   }

   public Rectangle2D getStatusArea () {
      return sArea;
   }

   public Rectangle2D getPositionArea () {
      return pArea;
   }

   public Rectangle2D getMessageArea () {
      return mArea;
   }

   public Rectangle2D getInsertIndicatorArea () {
      return iArea;
   }

   public Rectangle2D getKBIndicatorArea () {
      return kbArea;
   }

   public Rectangle2D getScriptIndicatorArea () {
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


   protected Data fillData(int startRow, int startCol, int endRow, int endCol) {

      return new Data(startRow, startCol, endRow, endCol );

   }

   Rectangle csArea = new Rectangle();
   char sChar[] = new char[1];

   protected class Data {


      public Data(char[] text, char[] attr, char[] color, char[] extended, char[] graphic) {
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
         int size = ((endCol - startCol) + 1) * ((endRow - startRow) +1);

         text = new char[size];
         attr = new char[size];
         isAttr = new char[size];
         color = new char[size];
         extended =new char[size];
         graphic = new char[size];
         field = null;

         if (size == lenScreen) {
	         screen.GetScreen(text, size, PLANE_TEXT);
	         screen.GetScreen(attr, size, PLANE_ATTR);
	         screen.GetScreen(isAttr, size, PLANE_IS_ATTR_PLACE);
	         screen.GetScreen(color, size, PLANE_COLOR);
	         screen.GetScreen(extended, size, PLANE_EXTENDED);
	         screen.GetScreen(graphic, size, PLANE_EXTENDED_GRAPHIC);
         }
         else {
	         screen.GetScreenRect(text, size, startRow, startCol, endRow, endCol, PLANE_TEXT);
	         screen.GetScreenRect(attr, size, startRow, startCol, endRow, endCol, PLANE_ATTR);
	         screen.GetScreenRect(isAttr, size, startRow, startCol, endRow, endCol, PLANE_IS_ATTR_PLACE);
	         screen.GetScreenRect(color, size, startRow, startCol, endRow, endCol, PLANE_COLOR);
	         screen.GetScreenRect(extended, size, startRow, startCol, endRow, endCol, PLANE_EXTENDED);
	         screen.GetScreenRect(graphic, size, startRow, startCol, endRow, endCol, PLANE_EXTENDED_GRAPHIC);
         }
      }

      public char[] text;
      public char[] attr;
      public char[] isAttr;
      public char[] color;
      public char[] extended;
      public final char[] graphic;
      public final char[] field;
   }

   public final Rectangle modelToView(int row, int col)
   {
     return modelToView(row, col, new Rectangle());
   }

   public final Rectangle modelToView(int row, int col, Rectangle r) {

      // right now row and column is 1,1 offset based.  This will need
      //   to be changed to 0,0 offset based by subtracting 1 from them
      //   when the screen is being passed this way
//     r.x      =  (col - 1) * columnWidth;
//     r.y      =  (row - 1) * rowHeight;
     r.x      =  col * columnWidth;
     r.y      =  row * rowHeight;
     r.width  = columnWidth;
     r.height = rowHeight;
     return r;
   }

   protected Color getColor(char color, boolean background) {
      int c = 0;
      if (background)
         // background
         c = (color & 0xff00) >> 8;
      else
         // foreground
         c = color & 0x00ff;

      switch (c) {
         case COLOR_FG_BLACK:
            return colorBg;
         case COLOR_FG_GREEN:
            return colorGreen;
         case COLOR_FG_BLUE:
            return colorBlue;
         case COLOR_FG_RED:
            return colorRed;
         case COLOR_FG_YELLOW:
            return colorYellow;
         case COLOR_FG_CYAN:
            return colorTurq;
         case COLOR_FG_WHITE:
            return colorWhite;
         case COLOR_FG_MAGENTA:
            return colorPink;
         default:
           return Color.orange;
      }
   }

   boolean      colSep = false;
   boolean   underLine = false;
   boolean   nonDisplay = false;
   Color fg;
   Color bg;

   private void setDrawAttr(int pos) {

      Screen5250 s = screen;
      colSep = false;
      underLine = false;
      nonDisplay = false;

      fg = getColor(updateRect.color[pos],false);
      bg = getColor(updateRect.color[pos],true);
      underLine = (updateRect.extended[pos] & EXTENDED_5250_UNDERLINE) != 0;
      colSep = (updateRect.extended[pos] & EXTENDED_5250_COL_SEP) != 0;
      nonDisplay = (updateRect.extended[pos] & EXTENDED_5250_NON_DSP) != 0;

   }


}
