/**
 * Title: GlobalConfigure.java
 * Copyright:   Copyright (c) 2001, 2002, 2003
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.1
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
package org.tn5250j;

import java.util.Properties;
import java.util.Hashtable;
import java.io.*;

import org.tn5250j.interfaces.ConfigureFactory;

/**
 * Utility class for referencing global settings and functions of which at most
 * one instance can exist per VM.
 *
 * Use GlobalConfigure.instance() to access this instance.
 */
public class GlobalConfigure extends ConfigureFactory {

   /**
    * A handle to the unitque GlobalConfigure class
    */
   static private GlobalConfigure _instance;

   /**
    * A handle to the the Global Properties
    */
   static private Properties settings;

   static private Hashtable registry = new Hashtable();

   static final public String SESSIONS = "sessions";
   static final public String MACROS = "macros";
   static final public String KEYMAP = "keymap";

   /**
    * The constructor is made protected to allow overriding.
    */
   public GlobalConfigure() {

      if (_instance == null) {
         // initialize the settings information
         initialize();
         // set our instance to this one.
         _instance = this;
      }
   }

   /**
    *
    * @return The unique instance of this class.
    */
   static public GlobalConfigure instance() {

      if (_instance == null) {
         _instance = new GlobalConfigure();
      }
      return _instance;

   }

   /**
    * Initialize the properties registry for use later.
    *
    */
   private void initialize() {

      loadSessions();
      loadMacros();
      loadKeyStrokes();
   }

   /**
    * Load the sessions properties
    */
   private void loadSessions() {

//      FileInputStream in = null;
//      Properties sessions = new Properties();
//
//      try {
//         in = new FileInputStream(settingssDirectory() +  SESSIONS);
//         sessions.load(in);
//
//      }
//      catch (FileNotFoundException fnfe) {
//         System.out.println(" Information Message: " + fnfe.getMessage()
//                           + ".  Default sessions file will"
//                           + " be created for first time use.");
//      }
//      catch (IOException ioe) {System.out.println(ioe.getMessage());}
//
//      if (sessions != null) {
//
//         registry.put(SESSIONS,sessions);
//      }
      getProperties(SESSIONS,SESSIONS,true,"",true);
   }

   /**
    * Load the macros
    */
   private void loadMacros() {

//      FileInputStream in = null;
//      Properties macs = new Properties();
//
//      try {
//         in = new FileInputStream(settingssDirectory()
//                                    +  MACROS);
//         macs.load(in);
//      }
//      catch (FileNotFoundException fnfe) {System.out.println(fnfe.getMessage());}
//      catch (IOException ioe) {System.out.println(ioe.getMessage());}
//      catch (SecurityException se) {
//         System.out.println(se.getMessage());
//      }
//
//      if (macs != null) {
//         registry.put(MACROS,macs);
//      }
      getProperties(MACROS,MACROS,true,"",true);

   }

   private void loadKeyStrokes() {

//      FileInputStream in = null;
//      Properties keystrokes = new Properties();
//
//      try {
//         in = new FileInputStream(settingssDirectory()
//                                    +  KEYMAP);
//         keystrokes.load(in);
//      }
//      catch (FileNotFoundException fnfe) {System.out.println(fnfe.getMessage());}
//      catch (IOException ioe) {System.out.println(ioe.getMessage());}
//      catch (SecurityException se) {
//         System.out.println(se.getMessage());
//      }
//
//      if (keystrokes != null) {
//         registry.put(KEYMAP,keystrokes);
//      }
      getProperties(KEYMAP,KEYMAP,true,"",true);

   }

   /**
    * Reload the environment settings.
    */
   public void reloadSettings() {

   }

   /**
    * Save the settings for the global configuration
    */
   public void saveSettings() {

   }

   /**
    * Save the setting in the registry using the key passed in with no header
    * output.
    *
    * @param regKey
    */
   public void saveSettings(String regKey) {

      saveSettings(regKey,"");
   }

   /**
    * Save the settings in the registry using the key passed with a header
    * in the output.
    *
    * @param regKey
    * @param Header
    */
   public void saveSettings(String regKey, String header) {

      saveSettings(regKey,regKey,header);
   }

   /**
    * Save the settings in the registry using the key passed with a header
    * in the output.
    *
    * @param regKey
    * @param Header
    */
   public void saveSettings(String regKey, String fileName, String header) {

      if (registry.containsKey(regKey)) {
         try {
            FileOutputStream out = new FileOutputStream(
                                    settingsDirectory() + regKey);
            Properties props = (Properties)registry.get(regKey);
            props.store(out,header);
         }
         catch (FileNotFoundException fnfe) {}
         catch (IOException ioe) {}
      }

   }

   /**
    * Returns the properties associated with a given registry key.
    *
    * @param regKey
    * @return
    */
   public Properties getProperties(String regKey) {

      if (registry.containsKey(regKey)) {
         return (Properties)registry.get(regKey);
      }
      return null;
   }

   public Properties getProperties() {
      return settings;
   }

   public Properties  getProperties(String regKey,String fileName) {
      return getProperties(regKey,fileName,false,"",false);
   }

   public Properties  getProperties(String regKey,String fileName,
                                                boolean createFile, String header) {
      return getProperties(regKey,fileName,false,"",false);
   }

   public Properties  getProperties(String regKey,String fileName,
                                                boolean createFile,String header,
                                                boolean reloadIfLoaded) {

      FileInputStream in = null;
      Properties props = new Properties();

      try {
         in = new FileInputStream(settingsDirectory()
                                    +  fileName);
         props.load(in);

      }
      catch (FileNotFoundException fnfe) {

         if (createFile) {
            System.out.println(" Information Message: " + fnfe.getMessage()
                              + ".  The file " + fileName + " will"
                              + " be created for first time use.");

            registry.put(regKey,props);

            saveSettings(regKey,header);

            return props;

         }
         else {

            System.out.println(" Information Message: " + fnfe.getMessage()
                              + ".");

         }
      }
      catch (IOException ioe) {
         System.out.println("IO Exception accessing File "+ fileName +
                              " for the following reason : "
                              + ioe.getMessage());
      }
      catch (SecurityException se) {
         System.out.println("Security Exception for file "+ fileName
                              + ".  This file can not be accessed because : "
                              + se.getMessage());
      }

      registry.put(regKey,props);

      return props;

   }

   /**
    * Not implemented yet
    *
    * @param key
    * @param def
    * @return
    */
   public String getProperty(String key, String def) {
      return def;
   }

   /**
    * Not implemented yet
    *
    * @param key
    * @return
    */
   public String getProperty(String key) {
      return "";
   }

   /**
    * Private helper to return the settings directory
    *
    * @return
    */
   private String settingsDirectory() {

      return System.getProperty("user.dir") + File.separator;

   }

   /**
    * Not sure yet
    *
    * @return
    */
   public ClassLoader getClassLoader() {

      ClassLoader loader = GlobalConfigure.class.getClassLoader();
      if (loader == null)
        loader = ClassLoader.getSystemClassLoader();

      return loader;
   }

}
