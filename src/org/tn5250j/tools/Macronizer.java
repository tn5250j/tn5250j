package org.tn5250j.tools;

/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
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
import java.awt.event.KeyEvent;
import java.util.*;
import java.io.*;

public class Macronizer {

   private static String macroName;
   private static Properties macros;
   private static boolean macrosExist;

   public static void init() {
      if (macros != null)
         return;

      init("macros");

   }

   public static void init(String macroFileName) {

      if (macros != null)
         return;

      macroName = macroFileName;

      macros = new Properties();

      macrosExist = loadMacros(macros);

   }

   private static boolean loadMacros(Properties macs) {

      FileInputStream in = null;
      try {
         in = new FileInputStream(macroName);
         macs.load(in);
         return true;
      }
      catch (FileNotFoundException fnfe) {System.out.println(fnfe.getMessage());}
      catch (IOException ioe) {System.out.println(ioe.getMessage());}
      catch (SecurityException se) {
         System.out.println(se.getMessage());
      }
      return false;
   }

   private final static void saveMacros() {

      try {
         FileOutputStream out = new FileOutputStream(macroName);
         macros.store(out,"------ Macros --------");
      }
      catch (FileNotFoundException fnfe) {}
      catch (IOException ioe) {}


   }

   public final static boolean isMacrosExist() {

      return macrosExist;
   }

   public final static int getNumOfMacros() {

      return macros.size();

   }

   public final static String[] getMacroList() {

      String[] macroList = new String[macros.size()];
      Set macroSet = macros.keySet();
      Iterator macroIterator = macroSet.iterator();
      String byName = null;
      int x = 0;
      while (macroIterator.hasNext()) {
         byName = (String)macroIterator.next();
         int period = byName.indexOf(".");
         macroList[x++] = byName.substring(period+1);
      }

      return macroList;
   }

   public final static String getMacroByNumber(int num) {
      String mac = "macro" + num + ".";

      Set macroSet = macros.keySet();
      Iterator macroIterator = macroSet.iterator();
      String byNum = null;
      while (macroIterator.hasNext()) {
         byNum = (String)macroIterator.next();
         if (byNum.startsWith(mac)) {
            return (String)macros.get(byNum);
         }
      }
      return null;
   }

   public final static String getMacroByName(String name) {

      Set macroSet = macros.keySet();
      Iterator macroIterator = macroSet.iterator();
      String byName = null;
      while (macroIterator.hasNext()) {
         byName = (String)macroIterator.next();
         if (byName.endsWith(name)) {
            return (String)macros.get(byName);
         }
      }
      return null;
   }

   public final static void setMacro(String name, String keyStrokes) {

      int x = 0;

      while (getMacroByNumber(++x) != null) {}
      macros.put("macro" + x + "." + name,keyStrokes);
      macrosExist = true;
      saveMacros();

   }

}
