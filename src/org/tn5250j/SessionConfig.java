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
package org.tn5250j;

import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.keyboard.KeyMnemonic;
import org.tn5250j.keyboard.KeyMnemonicSerializer;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;

import javax.swing.*;
import java.awt.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.Float.parseFloat;
import static org.tn5250j.keyboard.KeyMnemonic.*;

/**
 * A host session configuration object
 */
public class SessionConfig {

  public static final float KEYPAD_FONT_SIZE_DEFAULT_VALUE = 12.0f;
  public static final String CONFIG_KEYPAD_FONT_SIZE = "keypadFontSize";
  public static final String CONFIG_KEYPAD_ENABLED = "keypad";
  public static final String CONFIG_KEYPAD_MNEMONICS = "keypadMnemonics";
  public static final String YES = "Yes";
  public static final String NO = "No";

  private final SessionConfiguration sessionConfiguration = new SessionConfiguration();
  private final KeyMnemonicSerializer keyMnemonicSerializer = new KeyMnemonicSerializer();

  private String configurationResource;
  private String sessionName;
  private Properties sesProps;
  private boolean usingDefaults;

  private List<SessionConfigListener> sessionCfglisteners = null;
  private final ReadWriteLock sessionCfglistenersLock = new ReentrantReadWriteLock();

  public SessionConfig(String configurationResource, String sessionName) {
    this.configurationResource = configurationResource;
    this.sessionName = sessionName;
    loadConfigurationResource();
  }

  public String getConfigurationResource() {

    if (configurationResource == null || configurationResource.trim().isEmpty()) {
      configurationResource = "TN5250JDefaults.props";
      usingDefaults = true;
    }

    return configurationResource;

  }

  public String getSessionName() {
    return sessionName;
  }

  public final void firePropertyChange(Object source, String propertyName, Object oldValue, Object newValue) {

    if (oldValue != null && newValue != null && oldValue.equals(newValue)) {
      return;
    }

    sessionCfglistenersLock.readLock().lock();
    try {
      if (this.sessionCfglisteners != null) {
        final SessionConfigEvent sce = new SessionConfigEvent(source, propertyName, oldValue, newValue);
        for (SessionConfigListener target : this.sessionCfglisteners) {
          target.onConfigChanged(sce);
        }
      }
    } finally {
      sessionCfglistenersLock.readLock().unlock();
    }

  }

  /**
   * @return
   * @deprecated see {@link SessionConfiguration}
   */
  @Deprecated
  public Properties getProperties() {

    return sesProps;
  }

  public void setModified() {
    sesProps.setProperty("saveme", "");
  }

  public void saveSessionProps(java.awt.Container parent) {

    if (sesProps.containsKey("saveme")) {

      sesProps.remove("saveme");

      Object[] args = {getConfigurationResource()};
      String message = MessageFormat.format(
          LangTool.getString("messages.saveSettings"),
          args);

      int result = JOptionPane.showConfirmDialog(parent, message);

      if (result == JOptionPane.OK_OPTION) {
        saveSessionProps();
      }
    }
  }

  public void saveSessionProps() {

    if (usingDefaults) {

      ConfigureFactory.getInstance().saveSettings("dfltSessionProps",
          getConfigurationResource(),
          "");

    } else {
      try {
        FileOutputStream out = new FileOutputStream(settingsDirectory() + getConfigurationResource());
        // save off the width and height to be restored later
        sesProps.store(out, "------ Defaults --------");
      } catch (FileNotFoundException ignore) {
        // ignore
      } catch (IOException ignore) {
        // ignore
      }
    }
  }

  private void loadConfigurationResource() {

    sesProps = new Properties();

    if (configurationResource == null || configurationResource.trim().isEmpty()) {
      configurationResource = "TN5250JDefaults.props";
      usingDefaults = true;
      loadDefaults();
    } else {
      try {
        FileInputStream in = new FileInputStream(settingsDirectory() + getConfigurationResource());
        sesProps.load(in);
        if (sesProps.size() == 0)
          loadDefaults();
      } catch (IOException ioe) {
        System.out.println("Information Message: Properties file is being "
            + "created for first time use:  File name "
            + getConfigurationResource());
        loadDefaults();
      } catch (SecurityException se) {
        System.out.println(se.getMessage());
      }
    }
  }

  private String settingsDirectory() {
    return ConfigureFactory.getInstance().getProperty("emulator.settingsDirectory");
  }

  private void loadDefaults() {
    final ConfigureFactory configureFactory = ConfigureFactory.getInstance();
    try {
      sesProps = configureFactory
          .getProperties("dfltSessionProps", getConfigurationResource(), true, "Default Settings");
      if (sesProps.size() == 0) {
        sesProps.putAll(loadPropertiesFromResource(getConfigurationResource()));

        Properties colorSchemaDefaults = loadPropertiesFromResource("tn5250jSchemas.properties");
        String prefix = colorSchemaDefaults.getProperty("schemaDefault");
        sesProps.setProperty("colorBg", colorSchemaDefaults.getProperty(prefix + ".colorBg"));
        sesProps.setProperty("colorRed", colorSchemaDefaults.getProperty(prefix + ".colorRed"));
        sesProps.setProperty("colorTurq", colorSchemaDefaults.getProperty(prefix + ".colorTurq"));
        sesProps.setProperty("colorCursor", colorSchemaDefaults.getProperty(prefix + ".colorCursor"));
        sesProps.setProperty("colorGUIField", colorSchemaDefaults.getProperty(prefix + ".colorGUIField"));
        sesProps.setProperty("colorWhite", colorSchemaDefaults.getProperty(prefix + ".colorWhite"));
        sesProps.setProperty("colorYellow", colorSchemaDefaults.getProperty(prefix + ".colorYellow"));
        sesProps.setProperty("colorGreen", colorSchemaDefaults.getProperty(prefix + ".colorGreen"));
        sesProps.setProperty("colorPink", colorSchemaDefaults.getProperty(prefix + ".colorPink"));
        sesProps.setProperty("colorBlue", colorSchemaDefaults.getProperty(prefix + ".colorBlue"));
        sesProps.setProperty("colorSep", colorSchemaDefaults.getProperty(prefix + ".colorSep"));
        sesProps.setProperty("colorHexAttr", colorSchemaDefaults.getProperty(prefix + ".colorHexAttr"));
        sesProps.setProperty("font", GUIGraphicsUtils.getDefaultFont());

        configureFactory.saveSettings("dfltSessionProps", getConfigurationResource(), "");
      }
    } catch (IOException ioe) {
      System.out.println("Information Message: Properties file is being "
          + "created for first time use:  File name "
          + getConfigurationResource());
    } catch (SecurityException se) {
      System.out.println(se.getMessage());
    }
  }

  private Properties loadPropertiesFromResource(String resourceName) throws IOException {
    Properties properties = new Properties();
    URL url = getClass().getClassLoader().getResource(resourceName);
    if (url != null) {
      properties.load(url.openStream());
    }
    return properties;
  }

  public boolean isPropertyExists(String prop) {
    return sesProps.containsKey(prop);
  }

  /**
   * @return
   * @deprecated see {@link SessionConfiguration}
   */
  @Deprecated
  public String getStringProperty(String prop) {

    if (sesProps.containsKey(prop)) {
      return (String) sesProps.get(prop);
    }
    return "";

  }

  /**
   * @return
   * @deprecated see {@link SessionConfiguration}
   */
  @Deprecated
  public int getIntegerProperty(String prop) {

    if (sesProps.containsKey(prop)) {
      try {
        return Integer.parseInt((String) sesProps.get(prop));
      } catch (NumberFormatException ne) {
        return 0;
      }
    }
    return 0;

  }

  /**
   * @return
   * @deprecated see {@link SessionConfiguration}
   */
  @Deprecated
  public Color getColorProperty(String prop) {

    if (sesProps.containsKey(prop)) {
      return new Color(getIntegerProperty(prop));
    }
    return null;

  }

  public Rectangle getRectangleProperty(String key) {

    Rectangle rectProp = new Rectangle();

    if (sesProps.containsKey(key)) {
      String rect = sesProps.getProperty(key);
      StringTokenizer stringtokenizer = new StringTokenizer(rect, ",");
      if (stringtokenizer.hasMoreTokens())
        rectProp.x = Integer.parseInt(stringtokenizer.nextToken());
      if (stringtokenizer.hasMoreTokens())
        rectProp.y = Integer.parseInt(stringtokenizer.nextToken());
      if (stringtokenizer.hasMoreTokens())
        rectProp.width = Integer.parseInt(stringtokenizer.nextToken());
      if (stringtokenizer.hasMoreTokens())
        rectProp.height = Integer.parseInt(stringtokenizer.nextToken());

    }

    return rectProp;

  }

  public void setRectangleProperty(String key, Rectangle rect) {

    String rectStr = rect.x + "," +
        rect.y + "," +
        rect.width + "," +
        rect.height;
    sesProps.setProperty(key, rectStr);
  }

  /**
   * @return
   * @deprecated see {@link SessionConfiguration}
   */
  @Deprecated
  public float getFloatProperty(String prop) {
    return getFloatProperty(prop, 0.0f);
  }

  /**
   * @return
   * @deprecated see {@link SessionConfiguration}
   */
  @Deprecated
  public float getFloatProperty(String propertyName, float defaultValue) {
    if (sesProps.containsKey(propertyName)) {
      return parseFloat((String) sesProps.get(propertyName));
    }
    return defaultValue;
  }

  public Object setProperty(String key, String value) {
    return sesProps.setProperty(key, value);
  }

  public Object removeProperty(String key) {
    return sesProps.remove(key);
  }

  /**
   * Add a SessionConfigListener to the listener list.
   *
   * @param listener The SessionListener to be added
   */
  public final void addSessionConfigListener(SessionConfigListener listener) {
    sessionCfglistenersLock.writeLock().lock();
    try {
      if (sessionCfglisteners == null) {
        sessionCfglisteners = new ArrayList<SessionConfigListener>(3);
      }
      sessionCfglisteners.add(listener);
    } finally {
      sessionCfglistenersLock.writeLock().unlock();
    }
  }

  /**
   * Remove a SessionListener from the listener list.
   *
   * @param listener The SessionListener to be removed
   */
  public final void removeSessionConfigListener(SessionConfigListener listener) {
    sessionCfglistenersLock.writeLock().lock();
    try {
      if (sessionCfglisteners != null) {
        sessionCfglisteners.remove(listener);
      }
    } finally {
      sessionCfglistenersLock.writeLock().unlock();
    }
  }

  public SessionConfiguration getConfig() {
    return sessionConfiguration;
  }

  public void setKeypadMnemonicsAndFireChangeEvent(KeyMnemonic[] keyMnemonics) {
    String newValue = keyMnemonicSerializer.serialize(keyMnemonics);
    firePropertyChange(this, CONFIG_KEYPAD_MNEMONICS, getStringProperty(CONFIG_KEYPAD_MNEMONICS), newValue);
    setProperty(CONFIG_KEYPAD_MNEMONICS, newValue);
  }

  /**
   * This is the new intended way to access configuration.
   * Only Getters are allowed here!
   * <p>
   * TODO: refactor all former usages which access properties directly
   */
  public class SessionConfiguration {
    public float getKeypadFontSize() {
      return getFloatProperty(CONFIG_KEYPAD_FONT_SIZE, KEYPAD_FONT_SIZE_DEFAULT_VALUE);
    }

    public boolean isKeypadEnabled() {
      return YES.equals(getStringProperty(CONFIG_KEYPAD_ENABLED));
    }

    public KeyMnemonic[] getKeypadMnemonics() {
      String s = getStringProperty(CONFIG_KEYPAD_MNEMONICS);
      KeyMnemonic[] result = keyMnemonicSerializer.deserialize(s);
      if (result.length == 0) {
        return getDefaultKeypadMnemonics();
      }
      return result;
    }

    public KeyMnemonic[] getDefaultKeypadMnemonics() {
      return new KeyMnemonic[]{
          PF1, PF2, PF3, PF4, PF5, PF6, PF7, PF8, PF9, PF10, PF11, PF12, ENTER, PAGE_UP, CLEAR,
          PF13, PF14, PF15, PF16, PF17, PF18, PF19, PF20, PF21, PF22, PF23, PF24, SYSREQ, PAGE_DOWN
      };
    }

  }

}
