/*
 * @(#)TN5250jFrame.java
 * Copyright:    Copyright (c) 2001 , 2002
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

package org.tn5250j.gui;

import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JFrame;

import org.tn5250j.tools.GUIGraphicsUtils;

/**
 * Convenient base class for all TN5250j windows/frames.
 * Supports the standard application icon and a {@link #centerFrame()} method.
 * <br><br>
 * Direct known subclasses:
 * <ul>
 * <li>{@link org.tn5250j.interfaces.GUIViewInterface}</li>
 * <li>{@link org.tn5250j.mailtools.SendEMailDialog}</li>
 * <li>{@link org.tn5250j.spoolfile.SpoolExporter}</li>
 * <li>{@link org.tn5250j.spoolfile.SpoolExportWizard}</li>
 * <li>{@link org.tn5250j.tools.XTFRFile}</li>
 * </ul> 
 */
public class GenericTn5250JFrame extends JFrame {

	private static final long serialVersionUID = 7349671770294342782L;
	
	protected boolean packFrame = false;

   public GenericTn5250JFrame() {
      super();
      setIconImages(GUIGraphicsUtils.getApplicationIcons());
   }

   public void centerFrame() {

      if (packFrame)
         pack();
      else
         validate();

      //Center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = getSize();
      if (frameSize.height > screenSize.height)
         frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
         frameSize.width = screenSize.width;

      setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height) / 2);


   }

}