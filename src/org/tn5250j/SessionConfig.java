package org.tn5250j;

/*
 * @(#)SessionConfig.java
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

import java.util.*;
import javax.swing.*;
import java.io.*;
import java.text.MessageFormat;
import java.awt.Color;

import org.tn5250j.*;
import org.tn5250j.tools.LangTool;
import org.tn5250j.interfaces.SessionInterface;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.event.SessionConfigEvent;

/**
 * A host session configuration object
 */
public class SessionConfig implements TN5250jConstants {

   private String configurationResource;
   private String sessionName;
   private boolean connected;
   private int sessionType;
   private Properties sesProps;
   private Vector listeners;
   private SessionConfigEvent sce;
   private String sslType;

   public SessionConfig (String configurationResource,
                           String sessionName) {

      this.configurationResource = configurationResource;
      this.sessionName = sessionName;
      loadConfigurationResource();

   }

   public String getConfigurationResource() {

      return configurationResource;

   }

   public String getSessionName() {
      return sessionName;
   }

   /**
    * Notify all registered listeners of the onSessionChanged event.
    *
    * @param state  The state change property object.
    */
   protected void fireConfigChanged() {

      if (listeners != null) {
         int size = listeners.size();
         for (int i = 0; i < size; i++) {
            SessionConfigListener target =
                    (SessionConfigListener)listeners.elementAt(i);
            target.onConfigChanged(sce);
         }
      }
   }

   public Properties getProperties() {

      return sesProps;
   }

   public void setSessionProps(Properties props) {

      sesProps.putAll(props);

   }

   public void saveSessionProps(java.awt.Container parent) {

      if (sesProps.containsKey("saveme")) {

         sesProps.remove("saveme");

         Object[] args = {configurationResource};
         String message = MessageFormat.format(
                           LangTool.getString("messages.saveSettings"),
                           args);

         int result = JOptionPane.showConfirmDialog(parent,message);

         if (result == JOptionPane.OK_OPTION) {
            saveSessionProps();
         }


      }

   }

   public void saveSessionProps() {

      try {
         FileOutputStream out = new FileOutputStream(configurationResource);
            // save off the width and height to be restored later
         sesProps.store(out,"------ Defaults --------");
      }
      catch (FileNotFoundException fnfe) {}
      catch (IOException ioe) {}

   }

   private void loadConfigurationResource() {

      sesProps = new Properties();
      if (configurationResource == null || configurationResource == "")
         configurationResource = "TN5250JDefaults.props";

      try {
         FileInputStream in = new FileInputStream(configurationResource);
         //InputStream in = getClass().getClassLoader().getResourceAsStream(propFileName);
         sesProps.load(in);

      }
      catch (IOException ioe) {
         System.out.println("Information Message: Properties file is being "
                              + "created for first time use:  File name "
                              + configurationResource);
      }
      catch (SecurityException se) {
         System.out.println(se.getMessage());
      }

   }

   public boolean isPropertyExists(String prop) {
      return sesProps.containsKey(prop);
   }

   public String getStringProperty(String prop) {

      if (sesProps.containsKey(prop))
         return (String)sesProps.get(prop);
      else
         return "";

   }

   public final int getIntegerProperty(String prop) {

      if (sesProps.containsKey(prop)) {
         try {
            int i = Integer.parseInt((String)sesProps.get(prop));
            return i;
         }
         catch (NumberFormatException ne) {
            return 0;
         }
      }
      else
         return 0;

   }

   protected final Color getColorProperty(String prop) {

      if (sesProps.containsKey(prop)) {
         Color c = new Color(getIntegerProperty(prop));
         return c;
      }
      else
         return null;

   }

   protected final float getFloatProperty(String prop) {

      if (sesProps.containsKey(prop)) {
         float f = Float.parseFloat((String)sesProps.get(prop));
         return f;
      }
      else
         return 0.0f;

   }

   public Object setProperty(String key, String value ) {
      return sesProps.setProperty(key,value);
   }
   public synchronized Vector getSessionConfigListeners () {

      return listeners;
   }

   /**
    * Add a SessionConfigListener to the listener list.
    *
    * @param listener  The SessionListener to be added
    */
   public synchronized void addSessionConfigListener(SessionConfigListener listener) {

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
   public synchronized void removeSessionConfigListener(SessionConfigListener listener) {
      if (listeners == null) {
          return;
      }
      listeners.removeElement(listener);

   }

}