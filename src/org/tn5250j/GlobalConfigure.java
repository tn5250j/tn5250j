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
   static private Hashtable headers = new Hashtable();  //LUC GORRENS

   static final public String SESSIONS = "sessions";
   static final public String MACROS = "macros";
   static final public String KEYMAP = "keymap";

   static final private String settingsFile = 
   System.getProperty("user.home") + File.separator + ".tn5250j" + 
   File.separator + "tn5250jstartup.cfg";

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

      loadSettings();
      loadSessions();
      loadMacros();
      loadKeyStrokes();
   }

   /**
    * Load the sessions properties
    */
   private void loadSessions() {

      setProperties(SESSIONS,SESSIONS,"------ Sessions --------",true);
   }

   /**
    * Load the macros
    */
   private void loadMacros() {

      setProperties(MACROS,MACROS,"------ Macros --------",true);

   }

   private void loadKeyStrokes() {

      setProperties(KEYMAP,KEYMAP,
      "------ Key Map key=keycode,isShiftDown,isControlDown,isAltDown,isAltGrDown --------",
                     true);

   }

   /**
    * Reload the environment settings.
    */
   public void reloadSettings() {

   }

   /**
    * Loads the emulator setting from the setting(s) file
    */
   private void loadSettings() {

      FileInputStream in = null;
      settings = new Properties();

      // here we will check for a system property is provided first.
      if (System.getProperties().containsKey("emulator.settingsDirectory")) {
         settings.setProperty("emulator.settingsDirectory",
                                 System.getProperty("emulator.settingsDirectory") +
                                 File.separator);
      }
      else {

         try {
            in = new FileInputStream(settingsFile);
            settings.load(in);

         }
         catch (FileNotFoundException fnfe) {

            System.out.println(" Information Message: " + fnfe.getMessage()
                              + ".  The file " + settingsFile + " will"
                              + " be created for first time use.");
            checkLegacy();
            saveSettings();
         }
         catch (IOException ioe) {
            System.out.println("IO Exception accessing File " + settingsFile
                                 + " for the following reason : "
                                 + ioe.getMessage());
         }
         catch (SecurityException se) {
            System.out.println("Security Exception for file " + settingsFile
                                 + "  This file can not be "
                                 + "accessed because : " + se.getMessage());
         }
      }

      // we now check to see if the settings directory is a directory.  If not then we create it
      File sd = new File(settings.getProperty("emulator.settingsDirectory"));
      if (!sd.isDirectory())
         sd.mkdirs();
   }

   private void checkLegacy() {
      // we check if the sessions file already exists in the directory
      // if it does exist we are working with an old install so we
      // need to set the settings directory to the users directory
      // SESSIONS is declared as a string, so we just can use the keyword here.
      File ses = new File(SESSIONS);
      if(ses.exists()) {
         settings.setProperty("emulator.settingsDirectory", System.getProperty("user.dir") + File.separator);
      }
      else {
         settings.setProperty("emulator.settingsDirectory", System.getProperty("user.home") + File.separator + ".tn5250j"  + File.separator);
         System.out.println("User Home = " + System.getProperty("user.home"));
      }
   }

   /**
    * Save the settings for the global configuration
    */
   public void saveSettings() {

      try {
         FileOutputStream out = new FileOutputStream(settingsFile);
         settings.store(out,"----------------- tn5250j Global Settings --------------");
      }
      catch (FileNotFoundException fnfe) {}
      catch (IOException ioe) {}
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
                                    settingsDirectory() + fileName);
            Properties props = (Properties)registry.get(regKey);
            props.store(out,header);
            out.flush();
            out.close();
         }
         catch (FileNotFoundException fnfe) {
            System.out.println("File not found : writing file "
                                 + fileName + ".  Description of error is "
                                 + fnfe.getMessage());
         }
         catch (IOException ioe) {
            System.out.println("IO Exception : writing file "
                                 + fileName + ".  Description of error is "
                                 + ioe.getMessage());
         }
         catch (SecurityException se) {
            System.out.println("Security Exception : writing file "
                                 + fileName + ".  Description of error is "
                                 + se.getMessage());
         }

      }

   }

   /**
    * Place the Properties in the registry under a given registry name
    *
    * @param regKey
    * @param regProps
    */
   public void setProperties(String regKey, Properties regProps) {

      registry.put(regKey, regProps);

   }

   /**
    * Set the properties for the given registry key.
    *
    * @param regKey
    * @param fileName
    * @param header
    */
   public void setProperties(String regKey,  String fileName, String  header) {  //LG NEW
      setProperties(regKey, fileName, header, false);
   }

   /**
    * Set the properties for the given registry key.
    *
    * @param regKey
    * @param fileName
    * @param header
    * @param createFile
    */
  public void setProperties(String regKey,  String fileName, String  header,
                              boolean createFile) {

      FileInputStream in = null;
      Properties props = new Properties();
      headers.put(regKey, header);

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

            saveSettings(regKey,header);

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

      if (!registry.containsKey(regKey) || reloadIfLoaded) {

         FileInputStream in = null;
         Properties props = new Properties();
         headers.put(regKey, header);

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
      else {
         return (Properties)registry.get(regKey);
      }
   }

   /**
    * Returns the setting from the given key of the global properties or the
    * default passed if the property does not exist.
    *
    * @param key
    * @param def
    * @return
    */
   public String getProperty(String key, String def) {
      if (settings.containsKey(key))
         return settings.getProperty(key);
      else
         return def;
   }

   /**
    * Returns the setting from the given key of the global properties.
    *
    * @param key
    * @return
    */
   public String getProperty(String key) {
      return settings.getProperty(key);
   }

   /**
    * Private helper to return the settings directory
    *
    * @return
    */
   private String settingsDirectory() {
      //System.out.println(settings.getProperty("emulator.settingsDirectory"));
      return settings.getProperty("emulator.settingsDirectory");

   }

   /**
    * Not sure yet so be careful using this.
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
