package org.tn5250j.swing.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Toolkit;

import org.tn5250j.event.ScreenListener;
import org.tn5250j.event.ScreenOIAListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.ScreenOIA;

/**
 * For testing purpose
 */
public class BasicOIA extends BasicSubUI implements ScreenListener, ScreenOIAListener
{
  public BasicOIA(ScreenOIA oia)
  {
    this.oia = oia;
  }

  public void setBounds(int x, int y, int width, int height)
  {
    super.setBounds(x,y,width, height);
    this.clearLayout();
  }

  public void setFont(Font font, int charWidth, int charHeight)
  {
    super.setFont(font, charWidth, charHeight);
    this.clearLayout();
  }

   public void onScreenSizeChanged(int rows, int cols) {

   }

  public void onScreenChanged(int inUpdate, int startRow, int startCol, int endRow, int endCol)
  {
    if  ((inUpdate == 3) && (locationRectangle != null))
      addDirtyRectangle(locationRectangle);
  }

  public void onOIAChanged(ScreenOIA oia, int change)
  {
    switch (change)
    {
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

  public void install()
  {
    this.oia.addOIAListener(this);
    this.oia.getSource().addScreenListener(this);
  }

  public void uninstall()
  {
    this.oia.removeOIAListener(this);
    this.oia.getSource().removeScreenListener(this);

    this.oia = null;
  }

  public Dimension getPreferredSize()
  {
    return new Dimension(this.columnWidth, this.rowHeight);
  }

  public void paintComponent(Graphics g)
  {
    doLayout();

    Rectangle clip = g.getClipBounds();

    paintRuler(g, clip);
    paintLocation(g, clip);
    paintInhibited(g, clip);
  }

  private void paintRuler(Graphics g, Rectangle clip)
  {
    g.setColor(Color.white);
    g.drawLine(0, 0, this.width, 0);
  }

  private void paintLocation(Graphics g, Rectangle clip)
  {
    if (locationRectangle.intersects(clip))
    {
      g.setColor(BasicTerminalUI.DFT_FOREGROUND);

      Screen5250 s = oia.getSource();
      int col = s.getCurrentCol();
      int row = s.getCurrentRow();
      int cy = (locationRectangle.y + rowHeight - (metrics.getDescent() + metrics.getLeading()));

      g.drawString(col + "/" + row, locationRectangle.x, cy);
    }
  }

  private void paintInhibited(Graphics g, Rectangle clip)
  {

     if (inhibitedRectangle.intersects(clip))
     {
        if (oia.getInhibitedText() != null)
           System.out.println(oia.getInhibitedText());

        System.out.println("xsystem " + oia.getLevel() + "," + oia.getInputInhibited());

        g.setColor(BasicTerminalUI.DFT_FOREGROUND);

       int cy = (inhibitedRectangle.y + rowHeight - (metrics.getDescent() + metrics.getLeading()));
       int value = oia.getInputInhibited();
       String stext = oia.getInhibitedText();

       g.setColor(Color.black);
       g.fillRect(inhibitedRectangle.x,inhibitedRectangle.x,inhibitedRectangle.width,
             inhibitedRectangle.height);

       switch (oia.getLevel()) {
          case 1:
             if (value == 1) {

	             g.setColor(Color.white);
	             if (stext != null) {
	                g.drawChars(stext.toCharArray(),0,
	                      oia.getInhibitedText().length(),inhibitedRectangle.x,cy);
	             }
	             else {
	                g.drawChars("X - System".toCharArray(),0,
	                      "X - System".length(),inhibitedRectangle.x,cy);

	             }
             }
             break;
       }

     }
  }

  public final void setPosition(int row, int column)
  {
  }

  transient ScreenOIA      oia;
  private transient Rectangle locationRectangle;
  private transient Rectangle inhibitedRectangle;

  private void clearLayout()
  {
    locationRectangle = null;
    inhibitedRectangle = null;
  }

  private void doLayout()
  {
    if (locationRectangle == null)
    {
      Rectangle bounds = this.getBounds();

      // Location rectangle
      int w = 6 * this.columnWidth;
      locationRectangle = new Rectangle(bounds.width - w, 0, w, this.rowHeight);
      inhibitedRectangle = new Rectangle(10, 0, 25* this.columnWidth, this.rowHeight);
    }
  }

  private void ringAudibleBell()
  {
    Toolkit.getDefaultToolkit().beep();
  }

  private void setKeyboardLocked(boolean locked)
  {

  }
}
