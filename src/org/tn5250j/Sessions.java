package org.tn5250j;

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

import org.tn5250j.*;
import org.tn5250j.interfaces.SessionsInterface;
import java.util.*;

/**
 * Contains a collection of Session objects. This list is a static snapshot
 * of the list of Session objects available at the time of the snapshot.
 */
public class Sessions implements SessionsInterface {

   private Vector sessions = null;
   private int count = 0;

   public Sessions() {

      sessions = new Vector();

   }

   protected void addSession(Session newSession) {
      sessions.add(newSession);
      ++count;
   }

   protected void removeSession(Session session) {
      if (session != null) {
         if (session.isConnected())
            session.disconnect();
         sessions.remove(session);
         --count;
      }
   }

   protected void removeSession(String sessionName) {

      removeSession((Session)item(sessionName));

   }

   protected void removeSession(int index) {
      removeSession((Session)item(index));
   }

   public int getCount() {

      return count;
   }

   public Session item (int index) {

      return (Session)sessions.get(index);

   }

   public Session item (String sessionName) {

      Session s = null;
      int x = 0;

      while (x < sessions.size()) {

         s = (Session)sessions.get(x);

         if (s.getSessionName().equals(sessionName))
            return s;

         x++;
      }

      return null;

   }

   public Session item (Session sessionObject) {

      Session s = null;
      int x = 0;

      while (x < sessions.size()) {

         s = (Session)sessions.get(x);

         if (s.equals(sessionObject))
            return s;

         x++;
      }

      return null;

   }

   public void refresh() {



   }


}