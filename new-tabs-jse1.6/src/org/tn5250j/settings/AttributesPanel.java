/**
 * Title: AttributesPanel
 * Copyright:   Copyright (c) 2001,2002
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.5
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
package org.tn5250j.settings;

import java.awt.*;
import javax.swing.*;
import java.util.*;

import org.tn5250j.tools.*;
import org.tn5250j.SessionConfig;

/**
 * Base class for all attribute panels
 */
public abstract class AttributesPanel extends JPanel {

   private static final long serialVersionUID = 1L;
Properties props;
   Properties schemaProps;
   static final String nodePrefix = "sa.node";
   String name;
   SessionConfig changes = null;

   // content pane to be used if needed by subclasses
   JPanel contentPane;

   public AttributesPanel(SessionConfig config) {
      this(config,"",nodePrefix);
   }

   public AttributesPanel(SessionConfig config, String name) {
      this(config,name,nodePrefix);
   }

   public AttributesPanel(SessionConfig config, String name, String prefix) {
      super();
      changes = config;
      props = config.getProperties();
      this.name = LangTool.getString(prefix + name);
      // define layout
      setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

      try {
         initPanel();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   public abstract void initPanel() throws Exception;

   public abstract void applyAttributes();

   public abstract void save();

   protected final String getStringProperty(String prop) {

      if (changes.isPropertyExists(prop))
         return changes.getStringProperty(prop);
      else
         return "";

   }

   protected final String getStringProperty(String prop,String defaultValue) {

      if (changes.isPropertyExists(prop)) {
         String p = changes.getStringProperty(prop);
         if (p.length() > 0)
            return p;
         else
            return defaultValue;
      }
      else
         return defaultValue;

   }

   protected final int getIntProperty(String prop) {

      return changes.getIntegerProperty(prop);

   }

   protected final Color getColorProperty(String prop) {

      if (changes.isPropertyExists(prop)) {
         Color c = new Color(changes.getIntegerProperty(prop));
         return c;
      }
      else
         return null;

   }

   protected Color getColorProperty(String prop, Color defColor) {

      if (changes.isPropertyExists(prop)) {
         Color c = new Color(changes.getIntegerProperty(prop));
         return c;
      }
      else
         return defColor;

   }

   protected final boolean getBooleanProperty(String prop) {

      if (changes.isPropertyExists(prop)) {
         String b = changes.getStringProperty(prop).toLowerCase();
         if (b.equals("yes") || b.equals("true"))
            return true;
         else
            return false;
      }
      else
         return false;

   }

   protected final boolean getBooleanProperty(String prop, boolean dflt) {

      if (changes.isPropertyExists(prop)) {
         String b = changes.getStringProperty(prop).toLowerCase();
         if (b.equals("yes") || b.equals("true"))
            return true;
         else
            return false;
      }
      else
         return dflt;

   }

   protected Rectangle getRectangleProperty(String key) {

      return changes.getRectangleProperty(key);
   }

   protected void setRectangleProperty(String key, Rectangle rect) {

      changes.setRectangleProperty(key,rect);
   }

   protected final void setProperty(String key, String val) {

      changes.setProperty(key,val);

   }

   public String toString() {
      return name;
   }

}