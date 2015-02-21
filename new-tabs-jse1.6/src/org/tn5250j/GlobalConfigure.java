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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Properties;

import javax.swing.JOptionPane;

import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

/**
 * Utility class for referencing global settings and functions of which at most
 * one instance can exist per VM.
 *
 * Use GlobalConfigure.instance() to access this instance.
 */
public class GlobalConfigure extends ConfigureFactory {

	public static final String TN5250J_FOLDER = ".tn5250j";

	/**
	 * A handle to the unique GlobalConfigure class
	 */
	static private GlobalConfigure _instance;

	/**
	 * A handle to the the Global Properties
	 */
	static private Properties settings;

	static private Hashtable registry = new Hashtable();
	static private Hashtable headers = new Hashtable();  //LUC GORRENS

	// Moved to ConfigureFactory
	//   static final public String SESSIONS = "sessions";
	static final public File ses = new File(SESSIONS);
	//   static final public String MACROS = "macros";
	//   static final public String KEYMAP = "keymap";

	static final private String settingsFile = "tn5250jstartup.cfg";
	private final TN5250jLogger log = TN5250jLogFactory.getLogger (this.getClass());

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
		verifiySettingsFolder();
		loadSettings();
		loadSessions();
		loadMacros();
		loadKeyStrokes();
	}

	/**
	 * check if folder %USERPROFILE%/.tn5250j exists
	 * and create if necessary
	 */
	private void verifiySettingsFolder() {
		final String settingsfolder = System.getProperty("user.home") + File.separator + TN5250J_FOLDER;
		final File f = new File (settingsfolder);
		if (!f.exists()) {
			try {
				if (log.isInfoEnabled()) {
					log.info("Settings folder '"+settingsfolder+"' doesn't exist. Will created now.");
				}
				f.mkdir();
			} catch (Exception e) {
				if (log.isWarnEnabled()) {
					log.warn("Couldn't create settings folder '"+settingsfolder+"'", e);
				}
			}
		}
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
	@Override
	public void reloadSettings() {
		if (log.isInfoEnabled()) {
			log.info("reloading settings");
		}
		loadSettings();
		loadSessions();
		loadMacros();
		loadKeyStrokes();
		if (log.isInfoEnabled()) {
			log.info("Done (reloading settings).");
		}
	}

	/**
	 * Loads the emulator setting from the setting(s) file
	 */
	private void loadSettings() {

		FileInputStream in = null;
		FileInputStream again = null;
		settings = new Properties();

		// here we will check for a system property is provided first.
		if (System.getProperties().containsKey("emulator.settingsDirectory")) {
			settings.setProperty("emulator.settingsDirectory",
					System.getProperty("emulator.settingsDirectory") +
					File.separator);
			checkDirs();
		}
		else {
			settings.setProperty("emulator.settingsDirectory",
					System.getProperty("user.home") + File.separator +
					TN5250J_FOLDER + File.separator);
			try {
				in = new FileInputStream(settingsFile);
				settings.load(in);
			}
			catch (FileNotFoundException fnfe) {
				try {
					again = new FileInputStream(settingsDirectory() + settingsFile);
					settings.load(again);
				}
				catch (FileNotFoundException fnfea) {
					log.info(" Information Message: "
							+ fnfea.getMessage() + ".  The file " + settingsFile
							+ " will be created for first time use.");
					checkLegacy();
					saveSettings();
				}
				catch (IOException ioea) {
					log.warn("IO Exception accessing File "
							+ settingsFile + " for the following reason : "
							+ ioea.getMessage());
				}
				catch (SecurityException sea) {
					log.warn("Security Exception for file "
							+ settingsFile + "  This file can not be "
							+ "accessed because : " + sea.getMessage());
				}
			}
			catch (IOException ioe) {
				log.warn("IO Exception accessing File "
						+ settingsFile + " for the following reason : "
						+ ioe.getMessage());
			}
			catch (SecurityException se) {
				log.warn("Security Exception for file "
						+ settingsFile + "  This file can not be "
						+ "accessed because : " + se.getMessage());
			}
		}
	}

	private void checkDirs() {
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
		if(ses.exists()) {
			int cfc;
			cfc = JOptionPane.showConfirmDialog(null,
					"Dear User,\n\n" +
					"Seems you are using an old version of tn5250j.\n" +
					"In meanwhile the application became multi-user capable,\n" +
					"which means ALL the config- and settings-files are\n" +
					"placed in your home-dir to avoid further problems in\n" +
					"the near future.\n\n" +
					"You have the choice to choose if you want the files\n" +
					"to be copied or not, please make your choice !\n\n" +
					"Shall we copy the files to the new location ?",
					"Old install detected", JOptionPane.WARNING_MESSAGE,
					JOptionPane.YES_NO_OPTION);
			if (cfc == 0) {
				// Here we do a checkdir so we know the destination-dir exists
				checkDirs();
				copyConfigs(SESSIONS);
				copyConfigs(MACROS);
				copyConfigs(KEYMAP);
			}
			else {
				JOptionPane.showMessageDialog(null,
						"Dear User,\n\n" +
						"You choosed not to copy the file.\n" +
						"This means the program will end here.\n\n" +
						"To use this NON-STANDARD behaviour start tn5250j\n" +
						"with -Demulator.settingsDirectory=<settings-dir> \n" +
						"as a parameter to avoid this question all the time.",
						"Using NON-STANDARD behaviour", JOptionPane.WARNING_MESSAGE);
				System.exit(0);
			}
		}
	}

	private void copyConfigs(String sesFile) {
		/** Copy the config-files to the user's home-dir */
		String srcFile = System.getProperty("user.dir") + File.separator + sesFile;
		String dest = System.getProperty("user.home") +
		File.separator + TN5250J_FOLDER + File.separator + sesFile;
		File rmvFile = new File(sesFile);
		try {
			FileReader r = new FileReader(srcFile);
			BufferedReader b = new BufferedReader(r);

			FileWriter w = new FileWriter(dest);
			PrintWriter p = new PrintWriter(w);
			String regel = b.readLine();
			while (regel != null) {
				p.println(regel);
				regel = b.readLine();
			}
			b.close();
			p.close();
			rmvFile.delete();
		}
		catch (FileNotFoundException e) {
			log.warn(srcFile + " not found !");
		}
		catch (IOException e) {
			log.warn("Global io-error !");
		}
		catch (ArrayIndexOutOfBoundsException e) {
		}
	}

	/**
	 * Save the settings for the global configuration
	 */
	@Override
	public void saveSettings() {

		try {
			FileOutputStream out = new FileOutputStream(settingsDirectory() + settingsFile);
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
	@Override
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
	@Override
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
	@Override
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
				log.warn("File not found : writing file "
						+ fileName + ".  Description of error is "
						+ fnfe.getMessage());
			}
			catch (IOException ioe) {
				log.warn("IO Exception : writing file "
						+ fileName + ".  Description of error is "
						+ ioe.getMessage());
			}
			catch (SecurityException se) {
				log.warn("Security Exception : writing file "
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
	@Override
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
	@Override
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
	@Override
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
				log.info(" Information Message: " + fnfe.getMessage()
						+ ".  The file " + fileName + " will"
						+ " be created for first time use.");

				saveSettings(regKey,header);

			}
			else {

				log.info(" Information Message: " + fnfe.getMessage()
						+ ".");

			}
		}
		catch (IOException ioe) {
			log.warn("IO Exception accessing File "+ fileName +
					" for the following reason : "
					+ ioe.getMessage());
		}
		catch (SecurityException se) {
			log.warn("Security Exception for file "+ fileName
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
	@Override
	public Properties getProperties(String regKey) {

		if (registry.containsKey(regKey)) {
			return (Properties)registry.get(regKey);
		}
		return null;
	}

	public Properties getProperties() {
		return settings;
	}

	@Override
	public Properties  getProperties(String regKey,String fileName) {
		return getProperties(regKey,fileName,false,"",false);
	}

	@Override
	public Properties  getProperties(String regKey,String fileName,
			boolean createFile, String header) {
		return getProperties(regKey,fileName,false,"",false);
	}

	@Override
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
					log.info(" Information Message: " + fnfe.getMessage()
							+ ".  The file " + fileName + " will"
							+ " be created for first time use.");

					registry.put(regKey,props);

					saveSettings(regKey,header);

					return props;

				}
				else {

					log.info(" Information Message: " + fnfe.getMessage()
							+ ".");

				}
			}
			catch (IOException ioe) {
				log.warn("IO Exception accessing File "+ fileName +
						" for the following reason : "
						+ ioe.getMessage());
			}
			catch (SecurityException se) {
				log.warn("Security Exception for file "+ fileName
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
	@Override
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
	@Override
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
