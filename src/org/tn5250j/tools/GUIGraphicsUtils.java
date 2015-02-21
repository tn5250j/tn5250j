/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,202,2003
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.4
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
package org.tn5250j.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import org.tn5250j.tools.system.OperatingSystem;

public class GUIGraphicsUtils {

	private static final Insets GROOVE_INSETS = new Insets(2, 2, 2, 2);
	private static final Insets ETCHED_INSETS = new Insets(2, 2, 2, 2);
	public static final int RAISED = 1;
	public static final int INSET = 2;
	public static final int WINDOW_NORMAL = 3;
	public static final int WINDOW_GRAPHIC = 4;
	private static String defaultFont;

	private static ImageIcon lockImgOpen;
	private static ImageIcon lockImgClose;
	private static List<Image> tnicon;

	public static void draw3DLeft(Graphics g,int which,
			int x,int y,
			int fmWidth, int fmHeight) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);

		if (which == RAISED) {
			g.setColor(Color.white);
			// --  horizontal top
			g.drawLine(x,
					y,
					x + fmWidth,
					y);

			// --  horizontal top
			g.drawLine(x,
					y + 1,
					x + fmWidth,
					y + 1);

			// | vertical
			g.drawLine(x,
					y,
					x,
					y + fmHeight - 2);

			// | vertical
			g.drawLine(x + 1,
					y,
					x + 1,
					y + fmHeight - 2);

			g.setColor(Color.black);

			// --  horizontal bottom
			g.drawLine(x,
					y + fmHeight - 2,
					x + fmWidth,
					y + fmHeight - 2) ;

			g.setColor(Color.lightGray);

			// --  horizontal bottom
			g.drawLine(x + 1,
					y + fmHeight - 3,
					x + fmWidth,
					y + fmHeight - 3) ;
		}
		if (which == INSET) {

			g.setColor(Color.black);
			// --  horizontal top
			g.drawLine(x,
					y,
					x + fmWidth,
					y);

			// --  horizontal top
			g.drawLine(x,
					y + 1,
					x + fmWidth,
					y + 1);

			// | vertical
			g.drawLine(x,
					y,
					x,
					y + fmHeight - 2);

			// | vertical
			g.drawLine(x + 1,
					y,
					x + 1,
					y + fmHeight - 2);

			g.setColor(Color.white);

			// --  horizontal bottom
			g.drawLine(x,
					y + fmHeight - 2,
					x + fmWidth,
					y + fmHeight - 2) ;

			g.setColor(Color.lightGray);

			// --  horizontal bottom
			g.drawLine(x + 1,
					y + fmHeight - 3,
					x + fmWidth,
					y + fmHeight - 3) ;

		}
		//      g.translate(-x, -y);
		g.setColor(oldColor);
	}

	public static void draw3DMiddle(Graphics g,int which,
			int x,int y,
			int fmWidth, int fmHeight) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);

		if (which == RAISED) {
			g.setColor(Color.white);
			// --  horizontal top
			g.drawLine(x,
					y,
					x + fmWidth,
					y);

			// --  horizontal top
			g.drawLine(x,
					y + 1,
					x + fmWidth,
					y + 1);

			g.setColor(Color.black);

			// --  horizontal bottom
			g.drawLine(x,
					y + fmHeight - 2,
					x + fmWidth,
					y + fmHeight - 2) ;

			g.setColor(Color.lightGray);

			// --  horizontal bottom
			g.drawLine(x,
					y + fmHeight - 3,
					x + fmWidth,
					y + fmHeight - 3) ;
		}
		if (which == INSET) {

			g.setColor(Color.black);
			// --  horizontal top
			g.drawLine(x,
					y,
					x + fmWidth,
					y);

			// --  horizontal top
			g.drawLine(x,
					y + 1,
					x + fmWidth,
					y + 1);

			g.setColor(Color.white);

			// --  horizontal bottom
			g.drawLine(x,
					y + fmHeight - 2,
					x + fmWidth,
					y + fmHeight - 2) ;

			g.setColor(Color.lightGray);

			// --  horizontal bottom
			g.drawLine(x,
					y + fmHeight - 3,
					x + fmWidth,
					y + fmHeight - 3) ;

		}
		//      g.translate(-x, -y);
		g.setColor(oldColor);

	}

	public static void draw3DRight(Graphics g, int which,
			int x,int y,
			int fmWidth, int fmHeight) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);

		if (which == RAISED) {

			g.setColor(Color.white);
			// --  horizontal top
			g.drawLine(x,
					y,
					x + fmWidth - 2,
					y);

			// --  horizontal top
			g.drawLine(x,
					y + 1,
					x + fmWidth - 3,
					y + 1);

			g.setColor(Color.black);

			// --  horizontal bottom
			g.drawLine(x,
					y + fmHeight - 2,
					x + fmWidth - 2,
					y + fmHeight - 2) ;

			// | vertical
			g.drawLine(x + fmWidth - 1,
					y,
					x + fmWidth - 1,
					y + fmHeight - 2);

			g.setColor(Color.lightGray);

			// | vertical
			g.drawLine(x + fmWidth - 2,
					y + 1,
					x + fmWidth - 2,
					y + fmHeight - 3);

			// --  horizontal bottom
			g.drawLine(x,
					y + fmHeight - 3,
					x + fmWidth - 2,
					y + fmHeight - 3) ;
		}
		if (which == INSET) {

			g.setColor(Color.black);
			// --  horizontal top
			g.drawLine(x,
					y,
					x + fmWidth - 2,
					y);

			// --  horizontal top
			g.drawLine(x,
					y + 1,
					x + fmWidth - 3,
					y + 1);

			g.setColor(Color.white);

			// --  horizontal bottom
			g.drawLine(x,
					y + fmHeight - 2,
					x + fmWidth - 2,
					y + fmHeight - 2) ;

			// | vertical
			g.drawLine(x + fmWidth - 1,
					y,
					x + fmWidth - 1,
					y + fmHeight - 2);

			g.setColor(Color.lightGray);

			// | vertical
			g.drawLine(x + fmWidth - 2,
					y + 1,
					x + fmWidth - 2,
					y + fmHeight - 3);

			// --  horizontal bottom
			g.drawLine(x,
					y + fmHeight - 3,
					x + fmWidth - 2,
					y + fmHeight - 3) ;

		}
		//      g.translate(-x, -y);
		g.setColor(oldColor);

	}

	public static void draw3DOne(Graphics g, int which,
			int x,int y,
			int fmWidth, int fmHeight) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);

		if (which == INSET) {

			g.setColor(Color.black);
			// --  horizontal top
			g.drawLine(x,
					y,
					x + fmWidth - 2,
					y);

			// --  horizontal top
			g.drawLine(x,
					y + 1,
					x + fmWidth - 3,
					y + 1);

			// | vertical
			g.drawLine(x,
					y,
					x,
					y + fmHeight - 2);

			// | vertical
			g.drawLine(x + 1,
					y,
					x + 1,
					y + fmHeight - 2);

			g.setColor(Color.white);

			// --  horizontal bottom
			g.drawLine(x,
					y + fmHeight - 2,
					x + fmWidth - 2,
					y + fmHeight - 2) ;

			// | vertical right
			g.drawLine(x + fmWidth - 1,
					y,
					x + fmWidth - 1,
					y + fmHeight - 2);

			g.setColor(Color.lightGray);

			// | vertical right
			g.drawLine(x + fmWidth - 2,
					y + 1,
					x + fmWidth - 2,
					y + fmHeight - 3);

			// --  horizontal bottom
			g.drawLine(x + 1,
					y + fmHeight - 3,
					x + fmWidth - 2,
					y + fmHeight - 3) ;

		}
		if (which == RAISED) {

			g.setColor(Color.white);
			// --  horizontal top
			g.drawLine(x,
					y,
					x + fmWidth - 2,
					y);

			// --  horizontal top
			g.drawLine(x,
					y + 1,
					x + fmWidth - 3,
					y + 1);

			// | vertical
			g.drawLine(x,
					y,
					x,
					y + fmHeight - 2);

			// | vertical
			g.drawLine(x + 1,
					y,
					x + 1,
					y + fmHeight - 2);

			g.setColor(Color.darkGray);

			// --  horizontal bottom
			g.drawLine(x,
					y + fmHeight - 2,
					x + fmWidth - 2,
					y + fmHeight - 2) ;

			// | vertical right
			g.drawLine(x + fmWidth - 1,
					y,
					x + fmWidth - 1,
					y + fmHeight - 2);

			g.setColor(Color.lightGray);

			// | vertical right
			g.drawLine(x + fmWidth - 2,
					y + 1,
					x + fmWidth - 2,
					y + fmHeight - 3);

			// --  horizontal bottom
			g.drawLine(x + 1,
					y + fmHeight - 3,
					x + fmWidth - 2,
					y + fmHeight - 3) ;

		}

		//      g.translate(-x, -y);
		g.setColor(oldColor);

	}

	// draw scroll bar top
	public static void drawScrollBar(Graphics g, int which, int direction,
			int x,int y,
			int fmWidth, int fmHeight,
			Color fg,Color bg) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);

		if (which == INSET) {

			//            g.setColor(Color.black);
			//            // --  horizontal top
			//            g.drawLine(x,
			//                        y,
			//                        x + fmWidth - 2,
			//                        y);
			//
			//            // --  horizontal top
			//            g.drawLine(x,
			//                        y + 1,
			//                        x + fmWidth - 3,
			//                        y + 1);
			//
			//            // | vertical
			//            g.drawLine(x,
			//                        y,
			//                        x,
			//                        y + fmHeight - 2);
			//
			//            // | vertical
			//            g.drawLine(x + 1,
			//                        y,
			//                        x + 1,
			//                        y + fmHeight - 2);
			//
			//            g.setColor(Color.white);
			//
			//            // --  horizontal bottom
			//            g.drawLine(x,
			//                        y + fmHeight - 2,
			//                        x + fmWidth - 2,
			//                        y + fmHeight - 2) ;
			//
			//            // | vertical right
			//            g.drawLine(x + fmWidth - 1,
			//                        y,
			//                        x + fmWidth - 1,
			//                        y + fmHeight - 2);
			//
			//            g.setColor(Color.lightGray);
			//
			//            // | vertical right
			//            g.drawLine(x + fmWidth - 2,
			//                        y + 1,
			//                        x + fmWidth - 2,
			//                        y + fmHeight - 3);
			//
			//            // --  horizontal bottom
			//            g.drawLine(x + 1,
			//                        y + fmHeight - 3,
			//                        x + fmWidth - 2,
			//                        y + fmHeight - 3) ;
			g.setColor(bg);
			g.fillRect(x,y,fmWidth,fmHeight);
			g.setColor(fg);
			g.drawLine(x,
					y,
					x,
					y + fmHeight);
			g.drawLine(x+ fmWidth - 1,
					y,
					x+ fmWidth - 1,
					y + fmHeight);

			//            g.drawRect(x,y,fmWidth-2,fmHeight);
		}
		if (which == RAISED) {

			//            g.setColor(Color.white);
			//            // --  horizontal top
			//            g.drawLine(x,
			//                        y,
			//                        x + fmWidth - 2,
			//                        y);
			//
			//            // --  horizontal top
			//            g.drawLine(x,
			//                        y + 1,
			//                        x + fmWidth - 3,
			//                        y + 1);
			//
			//            // | vertical
			//            g.drawLine(x,
			//                        y,
			//                        x,
			//                        y + fmHeight - 2);
			//
			//            // | vertical
			//            g.drawLine(x + 1,
			//                        y,
			//                        x + 1,
			//                        y + fmHeight - 2);
			//
			//            g.setColor(Color.darkGray);
			//
			//            // --  horizontal bottom
			//            g.drawLine(x,
			//                        y + fmHeight - 2,
			//                        x + fmWidth - 2,
			//                        y + fmHeight - 2) ;
			//
			//            // | vertical right
			//            g.drawLine(x + fmWidth - 1,
			//                        y,
			//                        x + fmWidth - 1,
			//                        y + fmHeight - 2);
			//
			//            g.setColor(Color.lightGray);
			//
			//            // | vertical right
			//            g.drawLine(x + fmWidth - 2,
			//                        y + 1,
			//                        x + fmWidth - 2,
			//                        y + fmHeight - 3);
			//
			//            // --  horizontal bottom
			//            g.drawLine(x + 1,
			//                        y + fmHeight - 3,
			//                        x + fmWidth - 2,
			//                        y + fmHeight - 3) ;
			g.setColor(bg);
			g.fillRect(x,y,fmWidth,fmHeight);
			g.setColor(fg);
			g.drawLine(x,
					y,
					x,
					y + fmHeight);
			g.drawLine(x+ fmWidth - 1,
					y,
					x+ fmWidth - 1,
					y + fmHeight);

			//            g.drawRect(x,y,fmWidth-2,fmHeight);

		}

		if (direction == 1) {
			g.setColor(fg.brighter());
			g.drawLine(x + (fmWidth / 2),
					y + 2,
					x + 2,
					y + fmHeight - 4);

			g.setColor(fg.darker());

			g.drawLine(x + (fmWidth / 2),
					y + 2,
					x + fmWidth - 2,
					y + fmHeight - 4);

			g.drawLine(x + 2,
					y + fmHeight - 4,
					x + fmWidth - 2,
					y + fmHeight - 4);

			g.setColor(fg);
			g.drawLine(x,
					y,
					x+ fmWidth - 1,
					y);

			g.drawLine(x,
					y + fmHeight - 1,
					x+ fmWidth - 1,
					y + fmHeight - 1);

		}

		if (direction == 2) {
			g.setColor(fg.brighter());
			g.drawLine(x + (fmWidth / 2),
					y + fmHeight - 4,
					x + 2,
					y + 2);
			g.drawLine(x + 2,
					y + 2,
					x + fmWidth - 2,
					y + 2 );


			g.setColor(fg.darker());

			g.drawLine(x + (fmWidth / 2),
					y + fmHeight - 4,
					x + fmWidth -2,
					y + 2);

			g.setColor(fg);
			g.drawLine(x,
					y,
					x+ fmWidth ,
					y);

			g.drawLine(x,
					y + fmHeight -1,
					x+ fmWidth ,
					y + fmHeight - 1);

		}

		if (direction == 3) {

			g.setColor(fg);
			g.fillRect(x+2,y,fmWidth-4,fmHeight);

			//            g.setColor(bg);
			//            g.fillOval(x + 3,
					//                        y + 2,
					//                        fmWidth -4,
					//                        fmHeight - 4);

		}
		//      g.translate(-x, -y);
		g.setColor(oldColor);

	}

	public static void drawWinUpperLeft(Graphics g,int which, Color fill,
			int x,int y,
			int fmWidth, int fmHeight) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);
		g.setColor(fill);

		if (which == WINDOW_GRAPHIC) {
			g.fillRect(x,y,fmWidth,fmHeight);

			g.setColor(Color.white);
			// --  horizontal
			g.drawLine(x,
					y,
					x + fmWidth,
					y);
			// | vertical
			g.drawLine(x,
					y,
					x,
					y + fmHeight);

		}
		if (which == WINDOW_NORMAL) {
			// --  horizontal
			g.drawLine(x + fmWidth / 2,
					y + fmHeight / 2,
					x + fmWidth,
					y + fmHeight / 2);
			// | vertical
			g.drawLine(x + fmWidth / 2,
					y + fmHeight / 2,
					x + fmWidth / 2,
					y + fmHeight);

		}
		//      g.translate(-x, -y);
		g.setColor(oldColor);
	}

	public static void drawWinUpper(Graphics g,int which, Color fill,
			int x,int y,
			int fmWidth, int fmHeight) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);
		g.setColor(fill);

		if (which == WINDOW_GRAPHIC) {

			g.fillRect(x,y,fmWidth,fmHeight);

			g.setColor(Color.white);
			// --  horizontal
			g.drawLine(x,
					y,
					x + fmWidth,
					y);

			g.setColor(Color.black);
			// --  horizontal
			g.drawLine(x,
					y + fmHeight - 1,
					x + fmWidth,
					y + fmHeight - 1);

			g.setColor(Color.white);
			// --  horizontal
			g.drawLine(x,
					y + fmHeight,
					x + fmWidth,
					y + fmHeight);

		}
		if (which == WINDOW_NORMAL) {

			g.drawLine(x,
					y + fmHeight / 2,
					x + fmWidth,
					y + fmHeight / 2);

		}
		//      g.translate(-x, -y);
		g.setColor(oldColor);
	}

	public static void drawWinUpperRight(Graphics g,int which, Color fill,
			int x,int y,
			int fmWidth, int fmHeight) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);
		g.setColor(fill);

		if (which == WINDOW_GRAPHIC) {

			g.fillRect(x,y,fmWidth,fmHeight);

			g.setColor(Color.white);
			// --  horizontal
			g.drawLine(x,
					y,
					x + fmWidth,
					y);

			g.setColor(Color.black);

			// | vertical
			g.drawLine(x + fmWidth,
					y,
					x + fmWidth,
					y + fmHeight);


		}
		if (which == WINDOW_NORMAL) {

			// | vertical
			g.drawLine(x + fmWidth / 2,
					y + fmHeight / 2,
					x + fmWidth / 2,
					y + fmHeight);
			// -- horizontal
			g.drawLine(x,
					y + fmHeight / 2,
					x + fmWidth / 2,
					y + fmHeight / 2);

			g.setColor(fill.darker());

			int w = 0;

			while (w < 3) {
				g.fillRect((x + fmWidth / 2 ) + (3 + w),
						y + ++w + fmHeight / 2,
						1,
						(fmHeight / 2));
			}

		}
		//      g.translate(-x, -y);
		g.setColor(oldColor);
	}

	public static void drawWinLeft(Graphics g,int which, Color fill,
			int x,int y,
			int fmWidth, int fmHeight) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);
		g.setColor(fill);

		if (which == WINDOW_GRAPHIC) {

			g.fillRect(x,y,fmWidth,fmHeight);

			g.setColor(Color.white);
			// | vertical
			g.drawLine(x,
					y,
					x,
					y + fmHeight);

			g.setColor(Color.black);
			// --  vertical
			g.drawLine(x + fmWidth - 1,
					y,
					x + fmWidth - 1,
					y + fmHeight);

			g.setColor(Color.white);
			// --  vertical
			g.drawLine(x + fmWidth,
					y,
					x + fmWidth,
					y + fmHeight);

		}
		if (which == WINDOW_NORMAL) {

			g.drawLine(x + fmWidth / 2,
					y,
					x + fmWidth / 2,
					y + fmHeight);


		}
		//      g.translate(-x, -y);
		g.setColor(oldColor);
	}

	public static void drawWinRight(Graphics g,int which, Color fill,
			int x,int y,
			int fmWidth, int fmHeight) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);
		g.setColor(fill);

		if (which == WINDOW_GRAPHIC) {

			g.fillRect(x,y,fmWidth,fmHeight);

			g.setColor(Color.black);

			// | vertical
			g.drawLine(x + fmWidth,
					y,
					x + fmWidth,
					y + fmHeight);

			g.setColor(Color.white);

			// | vertical
			g.drawLine(x,
					y,
					x,
					y + fmHeight);

			g.setColor(Color.black);

			// | vertical
			g.drawLine(x + 1,
					y,
					x + 1,
					y + fmHeight);

		}
		if (which == WINDOW_NORMAL) {

			g.drawLine(x + fmWidth / 2,
					y,
					x + fmWidth / 2,
					y + fmHeight);

			g.setColor(fill.darker());
			g.fillRect((x + fmWidth / 2 ) + 3,
					y,
					3,
					fmHeight);

		}
		//      g.translate(-x, -y);
		g.setColor(oldColor);
	}

	public static void drawWinLowerLeft(Graphics g,int which, Color fill,
			int x,int y,
			int fmWidth, int fmHeight) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);
		g.setColor(fill);

		if (which == WINDOW_GRAPHIC) {

			g.fillRect(x,y,fmWidth,fmHeight);

			g.setColor(Color.black);
			// --  horizontal
			g.drawLine(x,
					y + fmHeight - 1,
					x + fmWidth,
					y + fmHeight - 1);

			g.setColor(Color.white);

			// | vertical
			g.drawLine(x,
					y,
					x,
					y + fmHeight - 1);

		}
		if (which == WINDOW_NORMAL) {


			// | horizontal
			g.drawLine(x + fmWidth / 2,
					y + fmHeight / 2,
					x + fmWidth / 2,
					y);

			// -- vertical
			g.drawLine(x + fmWidth / 2,
					y + fmHeight / 2,
					x + fmWidth,
					y + fmHeight / 2);

			g.setColor(fill.darker());
			int w = 0;

			while (w < 3) {
				g.fillRect((x + fmWidth / 2 ) + ++w,
						y + fmHeight / 2 + (2 + w),
						fmWidth / 2,
						1);
			}

		}
		//      g.translate(-x, -y);
		g.setColor(oldColor);
	}

	public static void drawWinBottom(Graphics g,int which, Color fill,
			int x,int y,
			int fmWidth, int fmHeight) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);
		g.setColor(fill);

		if (which == WINDOW_GRAPHIC) {

			g.fillRect(x,y,fmWidth,fmHeight);

			g.setColor(Color.black);

			// | horizontal
			g.drawLine(x,
					y + fmHeight - 1,
					x + fmWidth,
					y + fmHeight - 1);

			g.setColor(Color.white);
			// --  horizontal
			g.drawLine(x,
					y,
					x + fmWidth,
					y);

			g.setColor(Color.black);
			// --  horizontal
			g.drawLine(x,
					y + 1,
					x + fmWidth,
					y + 1);


		}
		if (which == WINDOW_NORMAL) {

			g.drawLine(x,
					y + fmHeight / 2,
					x + fmWidth,
					y + fmHeight / 2);

			// bottom
			g.setColor(fill.darker());
			g.fillRect(x,
					(y + fmHeight / 2) + 3,
					fmWidth,
					3);

		}
		//      g.translate(-x, -y);
		g.setColor(oldColor);
	}

	public static void drawWinLowerRight(Graphics g,int which, Color fill,
			int x,int y,
			int fmWidth, int fmHeight) {

		Color oldColor = g.getColor(); // make sure we leave it as we found it
		//      g.translate(x, y);
		g.setColor(fill);

		if (which == WINDOW_GRAPHIC) {

			g.fillRect(x,y,fmWidth,fmHeight);

			g.setColor(Color.black);
			// --  horizontal
			g.drawLine(x,
					y + fmHeight - 1,
					x + fmWidth,
					y + fmHeight - 1);

			// | vertical
			g.drawLine(x + fmWidth,
					y,
					x + fmWidth,
					y + fmHeight - 1);


		}
		if (which == WINDOW_NORMAL) {

			// vertical
			g.drawLine(x + fmWidth / 2,
					y,
					x + fmWidth / 2,
					y + fmHeight / 2);
			// horizontal
			g.drawLine(x + fmWidth / 2,
					y + fmHeight / 2,
					x,
					y + fmHeight / 2);

			g.setColor(fill.darker());
			// right part
			g.fillRect((x + fmWidth / 2 ) + 3,
					y,
					3,
					(fmHeight / 2) + 3);
			// bottom part
			g.fillRect(x,
					(y + fmHeight / 2) + 3,
					(fmWidth /2) + 6,
					3);


		}
		//      g.translate(-x, -y);
		g.setColor(oldColor);
	}

	public static void drawEtchedRect(Graphics g, int x, int y, int w, int h,
			Color shadow, Color darkShadow,
			Color highlight, Color lightHighlight) {

		Color oldColor = g.getColor();  // Make no net change to g
		//       g.translate(x, y);

		g.setColor(shadow);
		g.drawLine(0, 0, w-1, 0);      // outer border, top
		g.drawLine(0, 1, 0, h-2);      // outer border, left

		g.setColor(darkShadow);
		g.drawLine(1, 1, w-3, 1);      // inner border, top
		g.drawLine(1, 2, 1, h-3);      // inner border, left

		g.setColor(lightHighlight);
		g.drawLine(w-1, 0, w-1, h-1);  // outer border, bottom
		g.drawLine(0, h-1, w-1, h-1);  // outer border, right

		g.setColor(highlight);
		g.drawLine(w-2, 1, w-2, h-3);  // inner border, right
		g.drawLine(1, h-2, w-2, h-2);  // inner border, bottom
		//       g.translate(-x, -y);
		g.setColor(oldColor);
	}


	/**
	 * Returns the amount of space taken up by a border drawn by
	 * <code>drawEtchedRect()</code>
	 *
	 * @return  the inset of an etched rect
	 */
	public static Insets getEtchedInsets() {
		return ETCHED_INSETS;
	}


	public static void drawGroove(Graphics g, int x, int y, int w, int h,
			Color shadow, Color highlight) {

		Color oldColor = g.getColor();  // Make no net change to g
		//      g.translate(x, y);

		g.setColor(shadow);
		g.drawRect(0, 0, w-2, h-2);

		g.setColor(highlight);
		g.drawLine(1, h-3, 1, 1);
		g.drawLine(1, 1, w-3, 1);

		g.drawLine(0, h-1, w-1, h-1);
		g.drawLine(w-1, h-1, w-1, 0);

		//      g.translate(-x, -y);
		g.setColor(oldColor);
	}

	/**
	 * Returns the amount of space taken up by a border drawn by
	 * <code>drawGroove()</code>
	 *
	 * @return  the inset of a groove border
	 */
	public static Insets getGrooveInsets() {
		return GROOVE_INSETS;
	}


	public static void drawBezel(Graphics g, int x, int y, int w, int h,
			boolean isPressed, boolean isDefault,
			Color shadow, Color darkShadow,
			Color highlight, Color lightHighlight) {

		Color oldColor = g.getColor();  // Make no net change to g
		//     g.translate(x, y);

		if (isPressed) {
			if (isDefault) {
				g.setColor(darkShadow);          // outer border
				g.drawRect(0, 0, w-1, h-1);
			}

			g.setColor(shadow);         // inner border
			g.drawRect(1, 1, w-3, h-3);

		}
		else {
			if (isDefault) {
				g.setColor(darkShadow);
				g.drawRect(0, 0, w-1, h-1);

				g.setColor(lightHighlight);
				g.drawLine(1, 1, 1, h-3);
				g.drawLine(2, 1, w-3, 1);

				g.setColor(highlight);
				g.drawLine(2, 2, 2, h-4);
				g.drawLine(3, 2, w-4, 2);

				g.setColor(shadow);
				g.drawLine(2, h-3, w-3, h-3);
				g.drawLine(w-3, 2, w-3, h-4);

				g.setColor(darkShadow);
				g.drawLine(1, h-2, w-2, h-2);
				g.drawLine(w-2, h-2, w-2, 1);
			}
			else {
				g.setColor(lightHighlight);
				g.drawLine(0, 0, 0, h-1);
				g.drawLine(1, 0, w-2, 0);

				g.setColor(highlight);
				g.drawLine(1, 1, 1, h-3);
				g.drawLine(2, 1, w-3, 1);

				g.setColor(shadow);
				g.drawLine(1, h-2, w-2, h-2);
				g.drawLine(w-2, 1, w-2, h-3);

				g.setColor(darkShadow);
				g.drawLine(0, h-1, w-1, h-1);
				g.drawLine(w-1, h-1, w-1, 0);
			}

			//         g.translate(-x, -y);
			g.setColor(oldColor);
		}
	}

	public static void drawLoweredBezel(Graphics g, int x, int y, int w, int h,
			Color shadow, Color darkShadow,
			Color highlight, Color lightHighlight)  {
		g.setColor(darkShadow);
		g.drawLine(0, 0, 0, h-1);
		g.drawLine(1, 0, w-2, 0);

		g.setColor(shadow);
		g.drawLine(1, 1, 1, h-2);
		g.drawLine(1, 1, w-3, 1);

		g.setColor(lightHighlight);
		g.drawLine(0, h-1, w-1, h-1);
		g.drawLine(w-1, h-1, w-1, 0);

		g.setColor(highlight);
		g.drawLine(1, h-2, w-2, h-2);
		g.drawLine(w-2, h-2, w-2, 1);
	}


	/** Draw a string with the graphics g at location (x,y) just like g.drawString() would.
	 *  The first occurence of underlineChar in text will be underlined. The matching is
	 *  not case sensitive.
	 */
	public static void drawString(Graphics g,String text,int underlinedChar,int x,int y) {

		//        char b[] = new char[1];
		//      String s;
		char lc,uc;
		int index=-1,lci,uci;

		if(underlinedChar != '\0') {
			//           b[0] = (char)underlinedChar;
			//           s = new String(b).toUpperCase();
			//       uc = s.charAt(0);
			uc = Character.toUpperCase((char)underlinedChar);

			//            s = new String(b).toLowerCase();
			lc = Character.toLowerCase((char)underlinedChar);

			uci = text.indexOf(uc);
			lci = text.indexOf(lc);

			if(uci == -1)
				index = lci;
			else if(lci == -1)
				index = uci;
			else
				index = (lci < uci) ? lci : uci;
		}

		g.drawString(text,x,y);
		if(index != -1) {
			FontMetrics fm = g.getFontMetrics();
			//            Rectangle underlineRect = new Rectangle();

			int underlineRectX = x + fm.stringWidth(text.substring(0,index));
			int underlineRectY = y;
			int underlineRectWidth = fm.charWidth(text.charAt(index));
			int underlineRectHeight = 1;
			g.fillRect(underlineRectX, underlineRectY + fm.getDescent() - 1,
					underlineRectWidth, underlineRectHeight);
		}
	}


	public static void drawDashedRect(Graphics g,int x,int y,int width,int height) {

		int vx,vy;

		// draw upper and lower horizontal dashes
		for (vx = x; vx < (x + width); vx+=2) {
			g.drawLine(vx, y, vx, y);
			g.drawLine(vx, y + height-1, vx, y + height-1);
		}

		// draw left and right vertical dashes
		for (vy = y; vy < (y + height); vy+=2) {
			g.drawLine(x, vy, x, vy);
			g.drawLine(x+width-1, vy, x + width-1, vy);
		}
	}

	public static Font getDerivedFont(Font font, int width,int height,
			int numRows,int numCols,
			float scaleHeight,
			float scaleWidth,
			float pointSize) {

		// get the new proposed width and height of the screen that we
		// are suppose to fit within
		int w = width / numCols;     // proposed width
		int h = height / (numRows + 2);     // proposed height

		int sw = 0;
		int sh = 0;

		Font k = null;
		LineMetrics l;
		FontRenderContext f = null;
		AffineTransform at = new AffineTransform();
		if (numCols == 132) {

			// width, height
			at.scale( scaleWidth, scaleHeight );

		}
		else {
			at.setToScale( 1.0f, 1.0f );
			pointSize = 0;
		}
		//         at.setToScale( 1.0f, 1.0f );
		//         pointSize = 0;

		float j = 1;

		if (pointSize == 0) {

			// loop through the sizes of the fonts until we find one that will not
			// fit within the width or the height of the new proposed size
			for (; j < 36; j++) {

				k = font.deriveFont(j);

				// now apply the scale to the font
				k = k.deriveFont( at );
				f = new FontRenderContext(k.getTransform(),true,true);

				l = k.getLineMetrics("Wy",f);
				//         float ats = (float)((k.getStringBounds("W",f).getWidth() + 1) /
				//            (k.getStringBounds("y",f).getHeight()  +
				//                     l.getDescent() + l.getLeading()));
				//         System.out.println(ats);


				sw = (int)k.getStringBounds("W",f).getWidth() + 2;
				sh = (int)(k.getStringBounds("y",f).getHeight() +
						l.getDescent() + l.getLeading());
				if (
						w < sw || h < sh) {
					//               (w < (int)k.getStringBounds("W",f).getWidth() + 2) ||
					//                  h < (int)(k.getStringBounds("y",f).getHeight() +
					//                        l.getDescent() + l.getLeading())

					//               ) {

					//                  if (w != sw && h != sh) {
					//                     k = font.deriveFont(--j);
					//                     k = k.deriveFont( at );
					//                     sw = (int)k.getStringBounds("W",f).getWidth() + 2;
					//                     sh = (int)(k.getStringBounds("y",f).getHeight() +
					//                           l.getDescent() + l.getLeading());
					//                  }
					//   //               if (h > w) {
					//   //                  if (w <= sw) {
					//   //                     sch += (float)sw / (float)sh;
					//   //
					//   //                  }
					//   //                  else if (h < sh) {
					//   //                     scw = (float)sh / (float)sw;
					//   //
					//   //                  }
					//                  if (sh > sw) {
					//
					//   //                  sch = 1.0f / (((float)sh * (float)(numRows + 2 )) / (float)height);
					//                     sch = (float)((float)sh / (float)sw);
					//                  }
					//                     // width, height
					//                     at.scale( scw, sch );
					//                     // now apply the scale to the font
					//                     k = k.deriveFont( at );
					//     //             }

					break;
				}
			}
		}
		else {
			k = font.deriveFont(pointSize);
		}


		// since we obtained one that will not fit within the proposed size
		// we need to decrement it so that we obtain the last one that did fit
		if (j > 1)
			k = font.deriveFont(--j);

		// now apply the scale to the font
		k = k.deriveFont( at );
		//      System.out.println(k.getSize() + " ");

		return k;
	}

	public static void positionPopup(Component component, JPopupMenu jpm,
			int xCoord , int yCoord) {

		Dimension popupSize = jpm.getSize();
		if(popupSize.width == 0)
			popupSize = jpm.getPreferredSize();
		Point point = new Point(xCoord + popupSize.width, yCoord + popupSize.height);
		SwingUtilities.convertPointToScreen(point, component);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int x = 0;
		int y = 0;
		if(point.y > screenSize.height - 25)
			y = yCoord - popupSize.height;
		if(point.x > screenSize.width)
			x = xCoord - popupSize.width;
		jpm.show(component, x != 0 ? x : xCoord, y != 0 ? y : yCoord);
	}

	/**
	 * Windows fonts to search for in order of precedence
	 */
	static final String[] windowsFonts = {"Andale Mono","Letter Gothic Bold",
		"Lucida Sans Typewriter Regular",
		"Lucida Sans Typewriter Bold",
		"Lucida Console",
		"Courier New Bold",
		"Courier New", "Courier"};

	/**
	 * *nix fonts to search for in order of precedence
	 */
	static final String[] nixFonts = {"Lucida Sans Typewriter Regular",
		"Lucida Sans Typewriter Bold",
		"Courier New Bold",
		"Courier New",
		"Courier Bold",
	"Courier"};

	/**
	 * Mac fonts to search for in order of precedence
	 */
	static final String[] macFonts = {"Monaco",
		"Courier New Bold",
		"Courier New", "Courier"};

	public static String getDefaultFont() {

		if (defaultFont == null) {
			String[] fonts = windowsFonts;
			if (OperatingSystem.isMacOS()) {
				fonts = macFonts;
			}
			else if (OperatingSystem.isUnix()) {
				fonts = nixFonts;
			}

			for (int x = 0; x < fonts.length; x++) {
				if (isFontNameExists(fonts[x])) {
					defaultFont = fonts[x];
					break;
				}
			}

			// we will just make if a space at this time until we come up with
			//  a better solution
			if (defaultFont == null) {
				defaultFont = "";
			}
		}

		return defaultFont;
	}

	/**
	 * Checks to see if the font name exists within our environment
	 *
	 * @return whether the font passed exists or not.
	 */
	public static boolean isFontNameExists(String fontString) {

		// fonts from the environment
		Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

		for (int x = 0; x < fonts.length; x++) {
			if (fonts[x].getFontName().indexOf('.') < 0)
				if (fonts[x].getFontName().equals(fontString))
					return true;
		}

		return false;
	}

	/**
	 * This routine will extract image resources from jar file and create
	 * an ImageIcon
	 */
	public static ImageIcon createImageIcon (String image) {
		URL file=null;

		ClassLoader classLoader = GUIGraphicsUtils.class.getClassLoader();
		if (classLoader == null)
			classLoader = ClassLoader.getSystemClassLoader();

		try {
			file = classLoader.getResource(image);

		}
		catch (Exception e) {
			System.err.println(e);
		}
		return new ImageIcon( file);
	}

	/**
	 * @see {@link #getClosedLockIcon()}
	 * @return an image that lock like a opened lock
	 */
	public static ImageIcon getOpenLockIcon() {
		if (lockImgOpen == null) {
			lockImgOpen = createImageIcon("lock-open.png");
		}
		return lockImgOpen;
	}

	/**
	 * @see {@link #getOpenLockIcon()}
	 * @return an image that lock like a closed lock
	 */
	public static ImageIcon getClosedLockIcon() {
		if (lockImgClose == null) {
			lockImgClose = createImageIcon("lock.png");
		}
		return lockImgClose;
	}


	public final static List<Image> getApplicationIcons () {

		if (tnicon == null) {
			tnicon = new ArrayList<Image>();
			tnicon.add(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("tn5250j-16x16.png")).getImage());
			tnicon.add(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("tn5250j-32x32.png")).getImage());
			tnicon.add(new ImageIcon(ClassLoader.getSystemClassLoader().getResource("tn5250j-48x48.png")).getImage());
		}
		return tnicon;
	}

}
