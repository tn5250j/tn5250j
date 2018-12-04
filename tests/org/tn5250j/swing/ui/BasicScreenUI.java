package org.tn5250j.swing.ui;

import org.tn5250j.api.screen.ScreenListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.tools.GUIGraphicsUtils;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

import static org.tn5250j.TN5250jConstants.*;

/**
 * For testing purpose
 */
class BasicScreen extends BasicSubUI implements ScreenListener {

  private Data updateRect;
  LineMetrics lm;
  int lenScreen;
  int lastScreenUpdate;

  public BasicScreen(Screen5250 screen) {
    this.screen = screen;
    lenScreen = screen.getScreenLength();
  }

  /**
   * Holds row and column coordinates. An iOhioPosition can be constructed using
   * initial row and column coordinates or constructed with no values and have
   * the row and column set later.
   */

  public final class iOhioPosition {

    /**
     * Null constructor for iOhioPosition.
     */
    public iOhioPosition() {
      this(0, 0);
    }

    /**
     * Constructor for iOhioPosition.
     *
     * @param initRow The initial value for the row coordinate
     * @param initCol The initial value for the column coordinate
     */
    public iOhioPosition(int initRow,
                         int initCol) {

      setRow(initRow);
      setColumn(initCol);

    }

    /**
     * Returns the row coordinate.
     *
     * @return The row coordinate
     */
    public int getRow() {

      return row;
    }

    /**
     * Returns the column coordinate.
     *
     * @return The column coordinate
     */
    public int getColumn() {

      return col;
    }

    /**
     * Sets the row coordinate.
     *
     * @param newRow The new row coordinate
     */
    public void setRow(int newRow) {
      row = newRow;
    }

    /**
     * Sets the column coordinate.
     *
     * @param newCol The new column coordinate
     */
    public void setColumn(int newCol) {
      col = newCol;
    }

    /**
     * holds the row
     */
    int row;
    /**
     * holds the column
     */
    int col;

  }

  public void onScreenSizeChanged(int rows, int cols) {

  }

  //  public void onScreenChanged(int inUpdate, iOhioPosition inStart, iOhioPosition inEnd)
  public void onScreenChanged(int inUpdate, int startRow, int startCol, int endRow, int endCol)

  {

    iOhioPosition inStart = new iOhioPosition(startRow, startCol);
    iOhioPosition inEnd = new iOhioPosition(endRow, endCol);
//      System.out.println("screen updated -> " +  startRow + ", "
//                           + startCol + ", " + endRow + ", " + endCol);
    lastScreenUpdate = inUpdate;

    switch (inUpdate) {
      case 1:
      case 2:
      case 3:
      case 4:

//      case iOhio.OHIO_UPDATE_HOST:
        Rectangle sr = modelToView(inStart);
        Rectangle er = modelToView(inEnd);
        addDirtyRectangle(sr.x, sr.y, er.x - sr.x + columnWidth, er.y - sr.y + rowHeight);
        break;
//      case 4:
//        Rectangle cs = modelToView(inStart);
//        Rectangle ce = modelToView(inEnd);
//        addDirtyRectangle(cs.x, cs.y, ce.x - cs.x + columnWidth, ce.y - cs.y + rowHeight);
//        break;
    }
  }

  public void install() {
    this.screen.addScreenListener(this);

    this.rows = screen.getRows();
    this.columns = screen.getColumns();

  }

  public void uninstall() {
    this.columns = 0;
    this.rows = 0;

    this.screen.removeScreenListener(this);

    this.screen = null;
    this.setFont(null, 0, 0);
  }

  public Dimension getPreferredSize() {
    return new Dimension(this.columnWidth * columns, this.rowHeight * rows);
  }

  public void setCursorEnabled(boolean flag) {
//    System.out.println("Set curser enable " + flag);
//    if (flag != this.cursorEnabled)
//    {
//      this.cursorEnabled = flag;
//      this.addDirtyRectangle(modelToView(this.screen.getCursor()));
//    }
  }

  public boolean isCursorEnabled() {
    return cursorEnabled;
  }

  public void setCursor(int x, int y) {
//    iOhioPosition pos = screen.getCursor();
//    Rectangle     r   = modelToView(pos);
//    this.addDirtyRectangle(r);
//
//    pos = viewToModel(x, y);
//
//    // Seems cursor is drifting:
//    // do a setCursor() and getCursor()
//    // They differ in one position in row and columns cooridinates.
//
//    pos.setColumn(pos.getColumn() - 1);
//    pos.setRow(pos.getRow() - 1);
//    screen.setCursor(pos);
//
//    r = modelToView(pos);
//    this.addDirtyRectangle(r.x,r.y,r.width, r.height);
  }


  /**
   * PRE:
   * The x,y coordinate must be between the innerbounds of the screen!
   */
  public final iOhioPosition viewToModel(int x, int y) {
    return viewToModel(x, y, new iOhioPosition());
  }

  public final iOhioPosition viewToModel(int x, int y, iOhioPosition pos) {
    pos.setColumn(Math.min(columns, (x / columnWidth) + 1));
    pos.setRow(Math.min(rows, (y / rowHeight) + 1));

    return pos;
  }

  public final Rectangle modelToView(iOhioPosition pos) {
    return modelToView(pos, new Rectangle());
  }

  public final Rectangle modelToView(iOhioPosition pos, Rectangle r) {
    r.x = (pos.getColumn() - 1) * columnWidth;
    r.y = (pos.getRow() - 1) * rowHeight;
    r.width = columnWidth;
    r.height = rowHeight;

    return r;
  }

  public void paintComponent(Graphics g) {
    paintGrid(g);
    paintScreen(g);
    paintCursor(g);
  }

  protected void paintGrid(Graphics g) {
    g.setColor(Color.white);
    g.drawRect(0, 0, width - 1, height - 1);

  }

  protected void paintCursor(Graphics g) {
//       if (cursorEnabled)
//       {
//         iOhioPosition pos = screen.getCursor();
//      if (screen.cursorShown) {
    iOhioPosition pos = new iOhioPosition(screen.getCurrentRow(), screen.getCurrentCol());
//   //      System.out.println("Cursor at " + pos.getColumn() + "," + pos.getRow());
    Rectangle r = modelToView(pos);
//         g.setColor(Color.red);
//         g.setXORMode(Color.red);
    g.setColor(Color.red);
    g.setXORMode(colorBg);
    g.fillRect(r.x, r.y, r.width, r.height);
    g.setPaintMode();
//       }
//     }
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
//         startRow++;
//         startCol++;
//         endRow++;
//         endCol++;
      int size = ((endCol - startCol) + 1) * ((endRow - startRow) + 1);

      text = new char[size];
      attr = new char[size];
      isAttr = new char[size];
      color = new char[size];
      extended = new char[size];
      graphic = new char[size];
      field = null;

      if (size == lenScreen) {
//            log.info("full screen" + size);
        screen.getScreen(text, size, PLANE_TEXT);
        screen.getScreen(attr, size, PLANE_ATTR);
        screen.getScreen(isAttr, size, PLANE_IS_ATTR_PLACE);
        screen.getScreen(color, size, PLANE_COLOR);
        screen.getScreen(extended, size, PLANE_EXTENDED);
        screen.getScreen(graphic, size, PLANE_EXTENDED_GRAPHIC);
      } else {
        screen.getScreenRect(text, size, startRow, startCol, endRow, endCol, PLANE_TEXT);
        screen.getScreenRect(attr, size, startRow, startCol, endRow, endCol, PLANE_ATTR);
        screen.getScreenRect(isAttr, size, startRow, startCol, endRow, endCol, PLANE_IS_ATTR_PLACE);
        screen.getScreenRect(color, size, startRow, startCol, endRow, endCol, PLANE_COLOR);
        screen.getScreenRect(extended, size, startRow, startCol, endRow, endCol, PLANE_EXTENDED);
        screen.getScreenRect(graphic, size, startRow, startCol, endRow, endCol, PLANE_EXTENDED_GRAPHIC);
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

  protected void paintScreen(Graphics g) {
    Rectangle clip = g.getClipBounds();
    //System.out.println("CLIP = " + clip);
    Rectangle topaint = adjustRectangle(clip);
    this.paintRegion(g, topaint);
  }

  protected void paintRegion(Graphics g, Rectangle region) {
    iOhioPosition start = viewToModel(region.x, region.y);
    iOhioPosition end = viewToModel(region.x + region.width - columnWidth, region.y + region.height - rowHeight);
    if (lastScreenUpdate != 4) {
      paintPositions(g, region.x, region.y, start, end);
    }
  }

  protected void paintPositions(Graphics g, int x, int y, iOhioPosition start, iOhioPosition end) {

    if (lm == null) {
      FontRenderContext frc = new FontRenderContext(font.getTransform(),
          true, true);
      lm = font.getLineMetrics("Wy", frc);
    }

//      Graphics2D gg2d = (Graphics2D)g;
    Graphics gg2d = g;
    int sr = start.getRow();
    int sc = start.getColumn();
    int er = end.getRow();
    int ec = end.getColumn();

    int cols = 0;
    int lc = 0;


    updateRect = new Data(sr, sc, er, ec);

    Rectangle clipper = new Rectangle();

    int pos = 0;

    lc = ec;
    clipper.x = sc * columnWidth;
    clipper.y = sr * rowHeight;
    clipper.width = ((ec - sc) + 1) * columnWidth;
    clipper.height = ((er - sr) + 1) * rowHeight;

    while (sr <= er) {
      cols = ec - sc;
      lc = sc;
      while (cols-- >= 0) {
        if (sc + cols <= ec) {
          drawChar(gg2d, pos++, sr, lc);

          lc++;
        }
      }
      sr++;
    }

  }

  // Dup Character array for display output
  public static final transient char[] dupChar = {'*'};
  boolean showHex = false;

  //   public final void drawChar(Graphics2D g, int pos, int row, int col) {
  public final void drawChar(Graphics g, int pos, int row, int col) {

    int attr = updateRect.attr[pos];
    sChar[0] = updateRect.text[pos];
    char ch = sChar[0];

    setDrawAttr(pos);

    boolean attributePlace = updateRect.isAttr[pos] == 0 ? false : true;
    int whichGui = updateRect.graphic[pos];
    boolean useGui = whichGui == 0 ? false : true;

    csArea = modelToView(new iOhioPosition(row, col), csArea);

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
//            g.setColor(bg);
        //g.fill(csArea);
//            g.fillRect(csArea.x, csArea.y, csArea.width, csArea.height);
      } else {

        if (bg == colorBg && whichGui >= FIELD_LEFT && whichGui <= FIELD_ONE) {
          g.setColor(colorGUIField);
          g.fillRect(csArea.x, csArea.y, csArea.width, csArea.height);
        }
//            else
//               g.setColor(bg);

        //g.fill(csArea);
//            g.fillRect(csArea.x, csArea.y, csArea.width, csArea.height);

      }

      if (useGui && (whichGui < FIELD_LEFT)) {

        g.setColor(fg);

        switch (whichGui) {

          case UPPER_LEFT:
            if (ch == '.') {
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
          case UPPER:
            if (ch == '.') {

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
          case UPPER_RIGHT:
            if (ch == '.') {
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
          case GUI_LEFT:
            if (ch == ':') {
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
          case GUI_RIGHT:
            if (ch == ':') {
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
          case LOWER_LEFT:
            if (ch == ':') {

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
          case BOTTOM:
            if (ch == '.') {

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

          case LOWER_RIGHT:
            if (ch == ':') {
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
        if ((ch != 0x0) && (!Character.isWhitespace(ch))) {
          // use this until we define colors for gui stuff
          if ((useGui && whichGui < BUTTON_LEFT) && (fg == colorGUIField))
            g.setColor(Color.black);
          else
            g.setColor(fg);

          try {
            if (useGui)
              if (ch == 0x1C)
                g.drawChars(dupChar, 0, 1, x + 1, cy - 2);
              else
                g.drawChars(sChar, 0, 1, x + 1, cy - 2);
            else if (ch == 0x1C)
              g.drawChars(dupChar, 0, 1, x, cy - 2);
            else
              g.drawChars(sChar, 0, 1, x, cy - 2);
          } catch (IllegalArgumentException iae) {
            System.out.println(" ScreenChar iae " + iae.getMessage());

          }
        }
        if (underLine) {
          if (!useGui) {
            g.setColor(fg);
            g.drawLine(x, (int) (y + (rowHeight - (lm.getLeading() + lm.getDescent()))), (x + columnWidth), (int) (y + (rowHeight - (lm.getLeading() + lm.getDescent()))));
          }
        }

      }
    }

    if (useGui & (whichGui >= FIELD_LEFT)) {

      switch (whichGui) {

        case FIELD_LEFT:
          GUIGraphicsUtils.draw3DLeft(g, GUIGraphicsUtils.INSET, x, y,
              columnWidth, rowHeight);

          break;
        case FIELD_MIDDLE:
          GUIGraphicsUtils.draw3DMiddle(g, GUIGraphicsUtils.INSET, x, y,
              columnWidth, rowHeight);
          break;
        case FIELD_RIGHT:
          GUIGraphicsUtils.draw3DRight(g, GUIGraphicsUtils.INSET, x, y,
              columnWidth, rowHeight);
          break;

        case FIELD_ONE:
          GUIGraphicsUtils.draw3DOne(g, GUIGraphicsUtils.INSET, x, y,
              columnWidth, rowHeight);

          break;

        case BUTTON_LEFT:
        case BUTTON_LEFT_UP:
        case BUTTON_LEFT_DN:
        case BUTTON_LEFT_EB:

          GUIGraphicsUtils.draw3DLeft(g, GUIGraphicsUtils.RAISED, x, y,
              columnWidth, rowHeight);

          break;

        case BUTTON_MIDDLE:
        case BUTTON_MIDDLE_UP:
        case BUTTON_MIDDLE_DN:
        case BUTTON_MIDDLE_EB:

          GUIGraphicsUtils.draw3DMiddle(g, GUIGraphicsUtils.RAISED, x, y,
              columnWidth, rowHeight);
          break;

        case BUTTON_RIGHT:
        case BUTTON_RIGHT_UP:
        case BUTTON_RIGHT_DN:
        case BUTTON_RIGHT_EB:

          GUIGraphicsUtils.draw3DRight(g, GUIGraphicsUtils.RAISED, x, y,
              columnWidth, rowHeight);

          break;

        // scroll bar
        case BUTTON_SB_UP:
          GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.RAISED, 1, x, y,
              columnWidth, rowHeight,
              colorWhite, colorBg);
          break;

        // scroll bar
        case BUTTON_SB_DN:

          GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.RAISED, 2, x, y,
              columnWidth, rowHeight,
              colorWhite, colorBg);


          break;
        // scroll bar
        case BUTTON_SB_GUIDE:

          GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.INSET, 0, x, y,
              columnWidth, rowHeight,
              colorWhite, colorBg);


          break;

        // scroll bar
        case BUTTON_SB_THUMB:

          GUIGraphicsUtils.drawScrollBar(g, GUIGraphicsUtils.INSET, 3, x, y,
              columnWidth, rowHeight,
              colorWhite, colorBg);


          break;

      }
    }

  }

  Color colorBlue = new Color(140, 120, 255);
  Color colorTurq = new Color(0, 240, 255);
  Color colorRed = Color.red;
  Color colorWhite = Color.white;
  Color colorYellow = Color.yellow;
  Color colorGreen = Color.green;
  Color colorPink = Color.magenta;
  Color colorGUIField = Color.white;
  Color colorSep = Color.white;
  Color colorHexAttr = Color.white;
  Color colorBg = Color.black;

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

  boolean colSep = false;
  boolean underLine = false;
  boolean nonDisplay = false;
  Color fg;
  Color bg;

  private void setDrawAttr(int pos) {

    colSep = false;
    underLine = false;
    nonDisplay = false;

    fg = getColor(updateRect.color[pos], false);
    bg = getColor(updateRect.color[pos], true);
    underLine = (updateRect.extended[pos] & EXTENDED_5250_UNDERLINE) != 0;
    colSep = (updateRect.extended[pos] & EXTENDED_5250_COL_SEP) != 0;
    nonDisplay = (updateRect.extended[pos] & EXTENDED_5250_NON_DSP) != 0;

  }

  private Rectangle adjustRectangle(Rectangle region) {
    if (region == null)
      return this.getBounds();

    Rectangle adjs = new Rectangle(region);
    int off;

    off = columnOffset(adjs.x);
    adjs.x -= off;

    off = rowOffset(adjs.y);
    adjs.y -= off;

    off = columnOffset(adjs.x + adjs.width);
    if (off > 0)
      adjs.width += columnWidth - off;

    off = rowOffset(adjs.y + adjs.height);
    if (off > 0)
      adjs.height += rowHeight - off;

    return adjs;
  }

  private int columnOffset(int x) {
    return (x - this.x) % columnWidth;
  }

  private int rowOffset(int y) {
    return (y - this.y) % rowHeight;
  }

  transient Screen5250 screen;
  transient int columns;
  transient int rows;
  transient boolean cursorEnabled;
  transient Rectangle cursorRectangle = new Rectangle();

  @Override
  public void onKeysSent(Screen5250 screen, String keys) {
    // nothing to do
  }

  @Override
  public void onCursorMoved(Screen5250 screen, int pos) {
    // nothing to do
  }
}
