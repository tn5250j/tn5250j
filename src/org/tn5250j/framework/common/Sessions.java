/*
 * @(#)Sessions.java
 * Copyright:    Copyright (c) 2001
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
package org.tn5250j.framework.common;

import java.util.*;
import java.awt.event.*;
import javax.swing.Timer;

import org.tn5250j.Session5250;
import org.tn5250j.tools.logging.*;
import org.tn5250j.interfaces.SessionsInterface;


/**
 * Contains a collection of Session objects. This list is a static snapshot
 * of the list of Session objects available at the time of the snapshot.
 */
public class Sessions implements SessionsInterface,ActionListener {

   private Vector sessions = null;
   private int count = 0;
   private Timer heartBeater;

   private TN5250jLogger  log = TN5250jLogFactory.getLogger (this.getClass());

   public Sessions() {

      sessions = new Vector();
   }

   public void actionPerformed(ActionEvent e) {

      Session5250 ses;
      for (int x = 0; x < sessions.size(); x++) {
         try {
            ses = (Session5250)sessions.get(x);
            if (ses.isConnected() && ses.isSendKeepAlive()) {
               ses.getVT().sendHeartBeat();
               log.info(" sent heartbeat to " +  ses.getSessionName());
            }
         }
         catch (Exception ex) {
            log.warn(ex.getMessage());
         }
      }

   }

   protected void addSession(Session5250 newSession) {
      sessions.add(newSession);
      log.debug("adding Session: "+newSession.getSessionName());
      if (newSession.isSendKeepAlive() && heartBeater == null) {
         heartBeater = new Timer(15000,this);
//         heartBeater = new Timer(3000,this);
         heartBeater.start();

      }
      ++count;
   }

   protected void removeSession(Session5250 session) {
      log.debug("Removing session: "+session.getSessionName());
      if (session != null) {
         if (session.isConnected())
            session.disconnect();
         sessions.remove(session);
         --count;
      }
   }

   protected void removeSession(String sessionName) {
      log.debug("Remove session by name: "+sessionName);
      removeSession(item(sessionName));

   }

   protected void removeSession(int index) {
   	  log.debug("Remove session by index: "+index);
//      removeSession((SessionGUI)(((Session5250)item(index)).getGUI()));
      removeSession(item(index));
   }

   public int getCount() {

      return count;
   }

   public Session5250 item (int index) {

      return (Session5250)sessions.get(index);

   }

   public Session5250 item (String sessionName) {

      Session5250 s = null;
      int x = 0;

      while (x < sessions.size()) {

         s = (Session5250)sessions.get(x);

         if (s.getSessionName().equals(sessionName))
            return s;

         x++;
      }

      return null;

   }

   public Session5250 item (Session5250 sessionObject) {

      Session5250 s = null;
      int x = 0;

      while (x < sessions.size()) {

         s = (Session5250)sessions.get(x);

         if (s.equals(sessionObject))
            return s;

         x++;
      }

      return null;

   }

   public Vector getSessionsList() {
      Vector newS = new Vector(sessions.size());
      for (int x = 0; x < sessions.size(); x++)
         newS.add(sessions.get(x));
      return newS;
   }

   public void refresh() {



   }


}