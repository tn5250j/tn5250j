package org.tn5250j.spoolfile;
/**
 * Title: SpoolExporter.java
 * Copyright:   Copyright (c) 2002
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

import javax.swing.JTabbedPane;
import javax.swing.BorderFactory;


public class SpoolFilterPane extends JTabbedPane {

   private UserTabPanel user;
   private OutputQueueTabPanel queue;

   public SpoolFilterPane() {
      try {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }
   private void jbInit() throws Exception {
      user = new UserTabPanel();
      queue = new OutputQueueTabPanel();

      this.addTab("User",user);
      this.addTab("Output Queue",queue);

   }

   public String getUser() {
      return user.getUser();
   }

   public String getQueue() {
      return queue.getQueue();
   }

   public String getLibrary() {

      return queue.getLibrary();

   }


}