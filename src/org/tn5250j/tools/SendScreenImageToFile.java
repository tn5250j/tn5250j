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
import org.tn5250j.Session;
import org.tn5250j.tools.encoder.EncodeComponent;
import org.tn5250j.tools.filters.XTFRFileFilter;
import org.tn5250j.gui.TN5250jFileChooser;

public class SendScreenImageToFile {

   Session session;

   public SendScreenImageToFile(Session ses) {

      session = ses;
      try {
         jbInit();
      }
      catch(Exception ex) {
         ex.printStackTrace();
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

      int ret = pcFileChooser.showSaveDialog(new JFrame());

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
            System.out.println("Error generating PNG exception caught: " + e);
         }

      }

   }

}
