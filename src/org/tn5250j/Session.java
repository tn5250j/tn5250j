package org.tn5250j;

/*
 * @(#)Session.java
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
import org.tn5250j.interfaces.SessionInterface;
import org.tn5250j.event.SessionListener;
import org.tn5250j.event.SessionChangeEvent;
import java.util.*;
import javax.swing.*;

/**
 * A host session
 */
public class Session extends Gui5250 implements SessionInterface,TN5250jConstants {

   private String configurationResource = null;
   private String sessionName = null;
   private boolean connected = false;
   private int sessionType = 0;
   private Properties sesProps;
   private Vector listeners = null;
   private SessionChangeEvent sce;

   public Session (My5250 m, Properties props, String configurationResource
                                                , String sessionName) {

      super(m,configurationResource,false);
      this.configurationResource = configurationResource;
      this.sessionName = sessionName;
      sesProps = props;
      sce = new SessionChangeEvent(this);

   }

   public String getConfigurationResource() {

      return configurationResource;

   }

   public boolean isConnected() {

      return vt.isConnected();
//      return connected;

   }

   public String getSessionName() {
      return sessionName;
   }

   public String getAllocDeviceName() {
      return vt.getAllocatedDeviceName();
   }

   public int getSessionType() {

      return sessionType;

   }

   public Screen5250 getScreen() {

      return screen;

   }

   public void connect() {

      String proxyPort = "1080"; // default socks proxy port
      boolean enhanced = false;
      boolean support132 = false;
      int port = 23; // default telnet port

      enhanced = sesProps.containsKey(SESSION_TN_ENHANCED);

      if (sesProps.containsKey(SESSION_SCREEN_SIZE))
         if (((String)sesProps.getProperty(SESSION_SCREEN_SIZE)).equals(SCREEN_SIZE_27X132_STR))
            support132 = true;

      final tnvt vt = new tnvt(screen,enhanced,support132);
      setVT(vt);

      vt.setController(this);

      if (sesProps.containsKey(SESSION_PROXY_PORT))
         proxyPort = (String)sesProps.getProperty(SESSION_PROXY_PORT);

      if (sesProps.containsKey(SESSION_PROXY_HOST))
         vt.setProxy((String)sesProps.getProperty(SESSION_PROXY_HOST),
                     proxyPort);


      if (sesProps.containsKey(SESSION_CODE_PAGE))
         vt.setCodePage((String)sesProps.getProperty(SESSION_CODE_PAGE));

      if (sesProps.containsKey(SESSION_DEVICE_NAME))
         vt.setDeviceName((String)sesProps.getProperty(SESSION_DEVICE_NAME));

      if (sesProps.containsKey(SESSION_HOST_PORT)) {
         port = Integer.parseInt((String)sesProps.getProperty(SESSION_HOST_PORT));
      }
      else {
         // set to default 23 of telnet
         port = 23;
      }

      final String ses = (String)sesProps.getProperty(SESSION_HOST);
      final int portp = port;

      // lets set this puppy up to connect within its own thread
      Runnable connectIt = new Runnable() {
            public void run() {
               vt.connect(ses,portp);
            }

        };

      // now lets set it to connect within its own daemon thread
      //    this seems to work better and is more responsive than using
      //    swingutilities's invokelater
      Thread ct = new Thread(connectIt);
      ct.setDaemon(true);
      ct.start();
//      fireSessionChanged(STATE_CONNECTED);

   }

   public void disconnect() {

      connected = false;
      vt.disconnect();
//      fireSessionChanged(STATE_DISCONNECTED);

   }

   /**
    * Notify all registered listeners of the onSessionChanged event.
    *
    * @param state  The state change property object.
    */
   protected void fireSessionChanged(int state) {

   	if (listeners != null) {
	      int size = listeners.size();
	      for (int i = 0; i < size; i++) {
	         SessionListener target =
                    (SessionListener)listeners.elementAt(i);
            sce.setState(state);
	         target.onSessionChanged(sce);
	      }
   	}
   }

   /**
    * Add a SessionListener to the listener list.
    *
    * @param listener  The SessionListener to be added
    */
   public synchronized void addSessionListener(SessionListener listener) {

      if (listeners == null) {
          listeners = new java.util.Vector(3);
      }
      listeners.addElement(listener);

   }

   /**
    * Remove a SessionListener from the listener list.
    *
    * @param listener  The SessionListener to be removed
    */
   public synchronized void removeSessionListener(SessionListener listener) {
      if (listeners == null) {
          return;
      }
      listeners.removeElement(listener);

   }
}