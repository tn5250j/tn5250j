/*
 * @(#)SessionGUI.java
 * Copyright:    Copyright (c) 2001 - 2004
 * @author Kenneth J. Pouncey
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
package org.tn5250j;

import java.util.*;
import java.awt.Rectangle;

import org.tn5250j.event.SessionListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.framework.tn5250.*;

/**
 * A host GUI session
 */
public class SessionGUI extends Gui5250 implements SessionListener,TN5250jConstants {

   private String configurationResource;
   private String sessionName;
   private boolean connected;
   private Vector listeners;
   private boolean firstScreen;
   private char[] signonSave;


   public SessionGUI (Session5250 session) {
      super(session);

      this.configurationResource = session.getConfigurationResource();
      this.sessionName = session.getSessionName();

      session.getConfiguration().addSessionConfigListener(this);
      session.addSessionListener(this);
   }

   public Session5250 getSession() {
      return super.session;
   }

   public void setSession(Session5250 session) {
      this.session = session;
   }


   public boolean isConnected() {

      return session.getVT().isConnected();

   }

   public boolean isOnSignOnScreen() {

      // check to see if we should check.
      if (firstScreen) {

         char[] so = screen.getScreenAsChars();
         int size = signonSave.length;

         Rectangle region = super.sesConfig.getRectangleProperty("signOnRegion");

         int fromRow = region.x;
         int fromCol = region.y;
         int toRow = region.width;
         int toCol = region.height;

         // make sure we are within range.
         if (fromRow == 0)
            fromRow = 1;
         if (fromCol == 0)
            fromCol = 1;
         if (toRow == 0)
            toRow = 24;
         if (toCol == 0)
            toCol = 80;

         int pos = 0;

         for (int r = fromRow; r <= toRow; r++)
            for (int c =fromCol;c <= toCol; c++) {
               pos = screen.getPos(r - 1, c - 1);
//               System.out.println(signonSave[pos]);
               if (signonSave[pos] != so[pos])
                  return false;
            }
      }

      return true;
   }

   public String getSessionName() {
      return sessionName;
   }

   public String getAllocDeviceName() {
      if (session.getVT() != null)
         return session.getVT().getAllocatedDeviceName();
      else
         return null;
   }

   public String getHostName() {
      return session.getVT().getHostName();
   }

   public Screen5250 getScreen() {

      return screen;

   }


   public void connect() {

      session.connect();
   }

   public void disconnect() {

      session.disconnect();
   }

   public void onSessionChanged(SessionChangeEvent changeEvent) {

      switch (changeEvent.getState()) {
         case STATE_CONNECTED:
            // first we check for the signon save or now
            if (!firstScreen) {
               firstScreen = true;
               signonSave = screen.getScreenAsChars();
//               System.out.println("Signon saved");
            }

            // check for on connect macro
            String mac = sesConfig.getStringProperty("connectMacro");
            if (mac.length() > 0)
               executeMacro(mac);
            break;
         default:
            firstScreen = false;
            signonSave = null;
      }
   }

   /**
    * Add a SessionListener to the listener list.
    *
    * @param listener  The SessionListener to be added
    */
   public synchronized void addSessionListener(SessionListener listener) {

      session.addSessionListener(listener);

   }

   /**
    * Remove a SessionListener from the listener list.
    *
    * @param listener  The SessionListener to be removed
    */
   public synchronized void removeSessionListener(SessionListener listener) {
      session.removeSessionListener(listener);

   }

}