package org.tn5250j.settings;
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

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.*;

import org.tn5250j.tools.*;
import org.tn5250j.SessionConfig;

/**
 * Base class for all attribute panels
 */
public abstract class AttributesPanel extends JPanel {

   Properties props;
   Properties schemaProps;

   SessionConfig changes = null;

   public AttributesPanel(SessionConfig config ) {
      super();
      changes = config;
      props = config.getProperties();

      // define layout
      setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

      try {
         initPanel();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   abstract void initPanel() throws Exception;

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

   protected final Color getColorProperty(String prop, Color defColor) {

      if (changes.isPropertyExists(prop)) {
         Color c = new Color(changes.getIntegerProperty(prop));
         return c;
      }
      else
         return defColor;

   }

   protected final void setProperty(String key, String val) {

      changes.setProperty(key,val);

   }

   abstract void applyAttributes();
}