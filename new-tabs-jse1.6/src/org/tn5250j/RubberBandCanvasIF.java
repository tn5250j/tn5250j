package org.tn5250j;
/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
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

import java.awt.*;
import java.awt.event.*;

public interface RubberBandCanvasIF {
   void addMouseListener(MouseListener l);
   void addMouseMotionListener(MouseMotionListener l);
   void areaBounded(RubberBand b, int startX, int startY, int endX, int endY);
   boolean canDrawRubberBand(RubberBand band);
   Point translateStart(Point startPoint);
   Point translateEnd(Point endPoint);
   Color getBackground();
//   Graphics getGraphics();
   Graphics getDrawingGraphics();

}