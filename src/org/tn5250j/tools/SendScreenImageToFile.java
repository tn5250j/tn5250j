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

package org.tn5250j.tools;

import java.io.*;
import javax.swing.*;
import java.awt.Frame;

import org.tn5250j.tools.logging.*;
import org.tn5250j.SessionPanel;
import org.tn5250j.tools.encoder.EncodeComponent;
import org.tn5250j.tools.filters.XTFRFileFilter;
import org.tn5250j.gui.TN5250jFileChooser;

public class SendScreenImageToFile {

   SessionPanel session;
   //  Change sent by Luc - LDC to pass a parent frame like the other dialogs
   Frame  parent;
   private TN5250jLogger  log = TN5250jLogFactory.getLogger (this.getClass());

   public SendScreenImageToFile(Frame parent, SessionPanel ses) {

      session = ses;
      this.parent = parent;


      try {
         jbInit();
      }
      catch(Exception ex) {
         log.warn("Error in constructor: "+ ex.getMessage());
      }
   }

   void jbInit() throws Exception {
      getPCFile();

   }

   /**
    * Get the local file from a file chooser
    */
   private void getPCFile() {

      String workingDir = System.getProperty("user.dir");
      TN5250jFileChooser pcFileChooser = new TN5250jFileChooser(workingDir);

      XTFRFileFilter pngFilter = new XTFRFileFilter("png", "Portable Network Graphics");

      pcFileChooser.setFileFilter(pngFilter);

      int ret = pcFileChooser.showSaveDialog(parent);

      // check to see if something was actually chosen
      if (ret == JFileChooser.APPROVE_OPTION) {

         File file;

         try {
            if (!pcFileChooser.getSelectedFile().getCanonicalPath().endsWith(".png"))
               file = new File(pcFileChooser.getSelectedFile().getCanonicalPath()
                                 + ".png");
            else
               file = pcFileChooser.getSelectedFile();


            EncodeComponent.encode(EncodeComponent.PNG,session, file);
         }
         catch (Exception e) {
            log.warn("Error generating PNG exception caught: " + e.getMessage());

         }

      }

   }

}
