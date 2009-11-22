/**
 * Title: OptionAccess.java
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

import static org.tn5250j.TN5250jConstants.mnemonicData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.interfaces.OptionAccessFactory;
import org.tn5250j.tools.LangTool;

/**
 * Utility class for referencing the global options allowed for access
 * of which at most one instance can exist per VM.
 *
 * Use OptionAccessFactory.instance() to access this instance.
 */
public class OptionAccess extends OptionAccessFactory {

   /**
    * A handle to the unique OptionAccess class
    */
   static private OptionAccess _instance;

   /**
    * A handle to non valid options.
    */
   static private List<String> restricted = new ArrayList<String>();

   /**
    * The constructor is made protected to allow overriding.
    */
   public OptionAccess() {
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
   static public OptionAccess instance() {

      if (_instance == null) {
         _instance = new OptionAccess();
      }
      return _instance;

   }

   /**
    * Initialize the properties registry for use later.
    *
    */
   private void initialize() {

      loadOptions();
   }

   /**
    * Load a list of available options
    */
   private void loadOptions() {

      restricted.clear();
		String restrictedProp =
			ConfigureFactory.getInstance().getProperties(
				ConfigureFactory.SESSIONS).getProperty("emul.restricted");

      if (restrictedProp != null) {
         StringTokenizer tokenizer = new StringTokenizer(restrictedProp, ";");
         while (tokenizer.hasMoreTokens()) {
            restricted.add(tokenizer.nextToken());
         }
      }

   }

   public Vector<String> getOptions() {

      Vector<String> v = new Vector<String>(mnemonicData.length);
      for (int x = 0; x < mnemonicData.length; x++) {
         v.add(mnemonicData[x]);
      }

      Collections.sort(v);

      return v;
   }

   public Vector<String> getOptionDescriptions() {

      Vector<String> v = new Vector<String>(mnemonicData.length);
      for (int x = 0; x < mnemonicData.length; x++) {
         v.add(LangTool.getString("key."+mnemonicData[x]));
      }

      Collections.sort(v);
      return v;
   }

   public boolean isValidOption(String option) {

      return !restricted.contains(option);
   }

   public boolean isRestrictedOption(String option) {

      return restricted.contains(option);
   }

   public int getNumberOfRestrictedOptions() {

      return restricted.size();
   }

   public void reload() {
      loadOptions();
   }
}
