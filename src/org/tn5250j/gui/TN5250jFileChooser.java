package org.tn5250j.gui;
/**
 * Title: tn5250J
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


import javax.swing.JFileChooser;
import java.awt.Dimension;

/**
 * Custom JFileChooser class to work around bug 4416982 on some versions of the
 * JDK/JRE
 */
public class TN5250jFileChooser extends JFileChooser {

   public TN5250jFileChooser(String dir) {
      super(dir);
   }

   /**
    * This is to fix
    * Bug Id - 4416982
    * Synopsis JFileChooser does not use its resources to size itself initially
    **/

   public Dimension getPreferredSize() {
      return getLayout().preferredLayoutSize(this);
   }
}