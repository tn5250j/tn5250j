package org.tn5250j;

/*
 * @(#)SessionManager.java
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
import org.tn5250j.interfaces.SessionManagerInterface;
import java.util.*;
import java.io.*;

/**
 * The SessionManager is the central repository for access to all sessions.
 * The SessionManager contains a list of all Session objects available.
 */
public class SessionManager implements SessionManagerInterface, TN5250jConstants {

   Sessions sessions;
   My5250 me;

   public SessionManager() {

      sessions = new Sessions();

   }

   public void setController(My5250 m) {

      me = m;
   }

   public Sessions getSessions() {
      return sessions;
   }

   public void closeSession(String sessionName) {

      Session session = (Session)sessions.item(sessionName);
      if (session != null)
         closeSession(session);

   }

   public void closeSession(Session sessionObject) {

      sessionObject.closeDown();
      sessions.removeSession((Session)sessionObject);

   }

   public Session openSession(Properties sesProps, String configurationResource
                                                , String sessionName) {
//                                             throws TN5250jException {

      sesProps.put(SESSION_NAME,sessionName);

      if (configurationResource == null)
         configurationResource = "";

      sesProps.put(SESSION_CONFIG_RESOURCE
                        ,configurationResource);

      Session newSession = new Session(me,sesProps,configurationResource,sessionName);
      sessions.addSession(newSession);
      return newSession;

   }


   protected static void loadConfigurationResource(Properties props,
                                                   String resourceName) {

      FileInputStream in = null;
      try {
         in = new FileInputStream(resourceName);
         props.load(in);

      }
      catch (FileNotFoundException fnfe) {System.out.println(fnfe.getMessage());}
      catch (IOException ioe) {System.out.println(ioe.getMessage());}

   }

}