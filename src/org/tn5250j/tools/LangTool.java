/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.4
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
package org.tn5250j.tools;

import java.text.MessageFormat;
import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class LangTool {

   static Locale locale;
   static ResourceBundle labels = null;

   public static void init() {
      if (labels != null)
         return;

      locale = Locale.getDefault();
//      System.out.println(locale);
      init("tn5250jMsgs");
   }

   public static void init(Locale l) {
      if (labels != null)
         return;

      locale = l;
//      System.out.println(locale);
      init("tn5250jMsgs");
   }

   public static void init(String initMsgFile) {
      if (labels != null)
         return;

      try {
         labels = ResourceBundle.getBundle(initMsgFile,locale);
      }
      catch (MissingResourceException mre) {
         System.out.println(mre.getLocalizedMessage());
      }

   }

   public static String getString(String key) {

      try {
         return labels.getString(key);
      }
      catch (MissingResourceException mre) {
         System.out.println(mre.getLocalizedMessage());
         return key;
      }

   }

   public static String getString(String key, String defaultString) {


      try {
         return labels.getString(key);
      }
      catch (MissingResourceException mre) {
//         System.out.println(mre.getLocalizedMessage());
         return defaultString;
      }

   }

   public static String messageFormat (String key,Object[] args) {

      return MessageFormat.format(getString(key),args);


   }

   // helper method for now
   static void iterateKeys() {

      try {
         ResourceBundle labels = ResourceBundle.getBundle("tn5250jMsgs",locale);

         Enumeration<String> bundleKeys = labels.getKeys();

         while (bundleKeys.hasMoreElements()) {
            String key = bundleKeys.nextElement();
            String value  = labels.getString(key);
            System.out.println("key = " + key + ", " +
              "value = " + value);
         }
      }
      catch (MissingResourceException mre) {
         System.out.println(mre.getLocalizedMessage());
      }

   }



}
