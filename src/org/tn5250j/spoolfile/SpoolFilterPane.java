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

public class SpoolFilterPane extends JTabbedPane {

   private static final long serialVersionUID = 1L;
private UserTabPanel user;
   private OutputQueueTabPanel queue;
//   private JobTabPanel job;
   private SpoolNameTabPanel spoolName;
   private UserDataTabPanel userData;

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
//      job = new JobTabPanel();
      spoolName = new SpoolNameTabPanel();
      userData = new UserDataTabPanel();

      this.addTab("User",user);
      this.addTab("Output Queue",queue);
//      this.addTab("Job",job);
      this.addTab("Spool Name",spoolName);
      this.addTab("User Data",userData);

   }

   public String getUser() {
      return user.getUser();
   }

   public void setUser(String filter) {
      user.setUser(filter);
      setSelectedComponent(user);
   }

   public String getQueue() {
      return queue.getQueue();
   }

   public String getLibrary() {

      return queue.getLibrary();

   }

   public String getJobName() {
      return " ";
   }

   public String getJobUser() {
      return " ";

   }

   public String getJobNumber() {
      return " ";

   }

   public String getUserData() {
      return userData.getUserData();

   }

   public void setUserData(String filter) {

      userData.setUserData(filter);
      setSelectedComponent(userData);
   }

   public String getSpoolName() {
      return spoolName.getSpoolName();

   }

   public void setSpoolName(String filter) {

      spoolName.setSpoolName(filter);
      setSelectedComponent(spoolName);
   }

   /**
    * Reset the values in the current panel to default values
    */
   public void resetCurrent() {
      ((QueueFilterInterface)this.getSelectedComponent()).reset();
   }

   /**
    * Reset the values in all filter panels to default values
    */
   public void resetAll() {
      for (int x = 0; x < this.getTabCount(); x++) {
         ((QueueFilterInterface)this.getComponent(x)).reset();
      }
   }
}