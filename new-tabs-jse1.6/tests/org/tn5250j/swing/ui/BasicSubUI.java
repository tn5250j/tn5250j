package org.tn5250j.swing.ui;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * For testing purpose
 */
public abstract class BasicSubUI
{
  public abstract void install();
  public abstract void uninstall();
  public abstract void paintComponent(Graphics g);
  public abstract Dimension getPreferredSize();

  public void setBounds(int x, int y, int width, int height)
  {
    this.x      = x;
    this.y      = y;
    this.width  = width;
    this.height = height;
  }

  public Rectangle getBounds()
  {
    return new Rectangle(x, y, width, height);
  }

  public void getBounds(Rectangle r)
  {
    r.x      = this.x;
    r.y      = this.y;
    r.width  = this.width;
    r.height = this.height;
  }

  public Point getLocation()
  {
    return new Point(this.x, this.y);
  }

  public void setFont(Font font, int charWidth, int charHeight)
  {
    if (font != null)
    {
      this.font        = font;
      this.columnWidth = charWidth;
      this.rowHeight   = charHeight;
    }
    else
    {
      this.font        = null;
      this.columnWidth = 0;
      this.rowHeight   = 0;
    }

    this.metrics = null;
  }

  public void setRepainter(Repainter repainter)
  {
    this.repainter = repainter;
  }

  public void paint(Graphics g)
  {
    g.setFont(font);
    if (metrics == null)
      metrics = g.getFontMetrics(font);

    paintComponent(g);
  }

  public void addDirtyRectangle(Rectangle dirty)
  {
    if (repainter == null)
      return;

    repainter.addDirtyRectangle(this, dirty.x + this.x, dirty.y + this.y, dirty.width, dirty.height);
  }

  public void addDirtyRectangle(int x, int y, int width, int height)
  {
    if (repainter == null)
      return;

    repainter.addDirtyRectangle(this, x + this.x, y + this.y, width, height);
  }

  protected transient Font        font;
  protected transient FontMetrics metrics;
  protected transient int         rowHeight;
  protected transient int         columnWidth;

  protected transient int         x;
  protected transient int         y;
  protected transient int         width;
  protected transient int         height;

  protected transient Repainter   repainter;

  public static interface Repainter
  {
    public void addDirtyRectangle(BasicSubUI origin, int x, int y, int width, int height);
  }
}
