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

public class KeyMapper {

   private static HashMap mappedKeys;
   private static KeyStroker workStroke;
   private static String keyMapName;
   private static String lastKeyMnemonic;

   public static void init() {
      if (mappedKeys != null)
         return;

      init("keymap");

   }

   public static void init(String map) {

      if (mappedKeys != null)
         return;

      keyMapName = map;
      mappedKeys = new HashMap(60);
      workStroke = new KeyStroker(0, false, false, false,false);

      Properties keys = new Properties();

      if (!loadKeyStrokes(keys)) {
         // keycode shift control alternate

         // Key <-> Keycode , isShiftDown , isControlDown , isAlternateDown

         // my personal preference
         mappedKeys.put(new KeyStroker(10, false, false, false, false),"[fldext]");
         mappedKeys.put(new KeyStroker(17, false, true, false, false),"[enter]");

//         mappedKeys.put(new KeyStroker(10, false, false, false),"[enter]");
//         mappedKeys.put(new KeyStroker(10, true, false, false),"[fldext]");

         mappedKeys.put(new KeyStroker(8, false, false, false, false),"[backspace]");
         mappedKeys.put(new KeyStroker(9, false, false, false, false),"[tab]");
         mappedKeys.put(new KeyStroker(9, true, false, false, false),"[backtab]");
         mappedKeys.put(new KeyStroker(127, false, false, false, false),"[delete]");
         mappedKeys.put(new KeyStroker(155, false, false, false, false),"[insert]");
         mappedKeys.put(new KeyStroker(19, false, false, false, false),"[clear]");
         mappedKeys.put(new KeyStroker(27, false, false, false, false),"[reset]");
         mappedKeys.put(new KeyStroker(27, true, false, false, false),"[sysreq]");

         mappedKeys.put(new KeyStroker(35, false, false, false, false),"[eof]");
         mappedKeys.put(new KeyStroker(36, false, false, false, false),"[home]");
         mappedKeys.put(new KeyStroker(39, false, false, false, false),"[right]");
         mappedKeys.put(new KeyStroker(39, false, false, true, false),"[nextword]");
         mappedKeys.put(new KeyStroker(37, false, false, false, false),"[left]");
         mappedKeys.put(new KeyStroker(37, false, false, true, false),"[prevword]");
         mappedKeys.put(new KeyStroker(38, false, false, false, false),"[up]");
         mappedKeys.put(new KeyStroker(40, false, false, false, false),"[down]");
         mappedKeys.put(new KeyStroker(34, false, false, false, false),"[pgdown]");
         mappedKeys.put(new KeyStroker(33, false, false, false, false),"[pgup]");

         mappedKeys.put(new KeyStroker(96, false, false, false, false),"[keypad0]");
         mappedKeys.put(new KeyStroker(97, false, false, false, false),"[keypad1]");
         mappedKeys.put(new KeyStroker(98, false, false, false, false),"[keypad2]");
         mappedKeys.put(new KeyStroker(99, false, false, false, false),"[keypad3]");
         mappedKeys.put(new KeyStroker(100, false, false, false, false),"[keypad4]");
         mappedKeys.put(new KeyStroker(101, false, false, false, false),"[keypad5]");
         mappedKeys.put(new KeyStroker(102, false, false, false, false),"[keypad6]");
         mappedKeys.put(new KeyStroker(103, false, false, false, false),"[keypad7]");
         mappedKeys.put(new KeyStroker(104, false, false, false, false),"[keypad8]");
         mappedKeys.put(new KeyStroker(105, false, false, false, false),"[keypad9]");

         mappedKeys.put(new KeyStroker(109, false, false, false, false),"[field-]");
         mappedKeys.put(new KeyStroker(107, false, false, false, false),"[field+]");
         mappedKeys.put(new KeyStroker(112, false, false, false, false),"[pf1]");
         mappedKeys.put(new KeyStroker(113, false, false, false, false),"[pf2]");
         mappedKeys.put(new KeyStroker(114, false, false, false, false),"[pf3]");
         mappedKeys.put(new KeyStroker(115, false, false, false, false),"[pf4]");
         mappedKeys.put(new KeyStroker(116, false, false, false, false),"[pf5]");
         mappedKeys.put(new KeyStroker(117, false, false, false, false),"[pf6]");
         mappedKeys.put(new KeyStroker(118, false, false, false, false),"[pf7]");
         mappedKeys.put(new KeyStroker(119, false, false, false, false),"[pf8]");
         mappedKeys.put(new KeyStroker(120, false, false, false, false),"[pf9]");
         mappedKeys.put(new KeyStroker(121, false, false, false, false),"[pf10]");
         mappedKeys.put(new KeyStroker(122, false, false, false, false),"[pf11]");
         mappedKeys.put(new KeyStroker(123, false, false, false, false),"[pf12]");
         mappedKeys.put(new KeyStroker(112, true, false, false, false),"[pf13]");
         mappedKeys.put(new KeyStroker(113, true, false, false, false),"[pf14]");
         mappedKeys.put(new KeyStroker(114, true, false, false, false),"[pf15]");
         mappedKeys.put(new KeyStroker(115, true, false, false, false),"[pf16]");
         mappedKeys.put(new KeyStroker(116, true, false, false, false),"[pf17]");
         mappedKeys.put(new KeyStroker(117, true, false, false, false),"[pf18]");
         mappedKeys.put(new KeyStroker(118, true, false, false, false),"[pf19]");
         mappedKeys.put(new KeyStroker(119, true, false, false, false),"[pf20]");
         mappedKeys.put(new KeyStroker(120, true, false, false, false),"[pf21]");
         mappedKeys.put(new KeyStroker(121, true, false, false, false),"[pf22]");
         mappedKeys.put(new KeyStroker(122, true, false, false, false),"[pf23]");
         mappedKeys.put(new KeyStroker(123, true, false, false, false),"[pf24]");
         mappedKeys.put(new KeyStroker(112, false, false, true, false),"[help]");

         mappedKeys.put(new KeyStroker(72, false, false, true, false),"[hostprint]");
         mappedKeys.put(new KeyStroker(67, false, false, true, false),"[copy]");
         mappedKeys.put(new KeyStroker(86, false, false, true, false),"[paste]");

         mappedKeys.put(new KeyStroker(39, true, false, false, false),"[markright]");
         mappedKeys.put(new KeyStroker(37, true, false, false, false),"[markleft]");
         mappedKeys.put(new KeyStroker(38, true, false, false, false),"[markup]");
         mappedKeys.put(new KeyStroker(40, true, false, false, false),"[markdown]");

         saveKeyMap();
      }
      else {

         setKeyMap(keys);

      }

   }

   private static boolean loadKeyStrokes(Properties keystrokes) {

      FileInputStream in = null;
      try {
         in = new FileInputStream(keyMapName);
         keystrokes.load(in);
         return true;
      }
      catch (FileNotFoundException fnfe) {System.out.println(fnfe.getMessage());}
      catch (IOException ioe) {System.out.println(ioe.getMessage());}
      catch (SecurityException se) {
         System.out.println(se.getMessage());
      }

      return false;
   }

   private static void parseKeyStrokes(Properties keystrokes) {

      String theStringList = "";
      String theKey = "";
      Enumeration ke = keystrokes.propertyNames();
      while (ke.hasMoreElements()) {
         theKey = (String)ke.nextElement();
         theStringList = keystrokes.getProperty(theKey);
         int x = 0;
         int kc = 0;
         boolean is = false;
         boolean ic = false;
         boolean ia = false;
         boolean iag = false;

         StringTokenizer tokenizer = new StringTokenizer(theStringList, ",");

         // first is the keycode
         kc = Integer.parseInt(tokenizer.nextToken());
         // isShiftDown
         if (tokenizer.nextToken().equals("true"))
            is = true;
         else
            is =false;
         // isControlDown
         if (tokenizer.nextToken().equals("true"))
            ic = true;
         else
            ic =false;
         // isAltDown
         if (tokenizer.nextToken().equals("true"))
            ia = true;
         else
            ia =false;

         // isAltDown Gr
         if (tokenizer.hasMoreTokens())
            if (tokenizer.nextToken().equals("true"))
               iag = true;
            else
               iag =false;

         mappedKeys.put(new KeyStroker(kc, is, ic, ia, iag),theKey);

      }

   }

   protected static void setKeyMap(Properties keystrokes) {

      parseKeyStrokes(keystrokes);

   }

   public final static boolean isEqualLast(KeyEvent ke) {
      return workStroke.equals(ke);
   }

   protected final static void saveKeyMap() {

      Properties map = new Properties();
      try {
         FileOutputStream out = new FileOutputStream(keyMapName);
         // save off the width and height to be restored later
         Collection v = mappedKeys.values();
         Set o = mappedKeys.keySet();
         Iterator k = o.iterator();
         Iterator i = v.iterator();
         while (k.hasNext()) {
            KeyStroker ks = (KeyStroker)k.next();
            String keyVal = ks.toString();
//            System.out.println(key + " " + keyVal);
            map.put((String)i.next(),ks.toString());
         }

         map.store(out,"------ Key Map key=keycode,isShiftDown,isControlDown,isAltDown,isAltGrDown --------");
      }
      catch (FileNotFoundException fnfe) {}
      catch (IOException ioe) {}
      catch (SecurityException se) {
         System.out.println(se.getMessage());
      }


   }

   public final static String getKeyStrokeText(KeyEvent ke) {
      if (!workStroke.equals(ke)) {
         workStroke.setAttributes(ke);
         lastKeyMnemonic = (String)mappedKeys.get(workStroke);
      }
      return lastKeyMnemonic;

   }

   public final static String getKeyStrokeText(KeyEvent ke,boolean isAltGr) {
      if (!workStroke.equals(ke,isAltGr)) {
         workStroke.setAttributes(ke,isAltGr);
         lastKeyMnemonic = (String)mappedKeys.get(workStroke);
      }
      return lastKeyMnemonic;

   }

   public final static int getKeyStrokeCode() {
      return workStroke.hashCode();
   }

   public final static String getKeyStrokeDesc(String which) {

      Collection v = mappedKeys.values();
      Set o = mappedKeys.keySet();
      Iterator k = o.iterator();
      Iterator i = v.iterator();
      while (k.hasNext()) {
         KeyStroker ks = (KeyStroker)k.next();
         String keyVal = (String)i.next();
         if (keyVal.equals(which))
            return ks.getKeyStrokeDesc();
      }

      return LangTool.getString("key.dead");
   }

   public final static void removeKeyStroke(String which) {

      Collection v = mappedKeys.values();
      Set o = mappedKeys.keySet();
      Iterator k = o.iterator();
      Iterator i = v.iterator();
      while (k.hasNext()) {
         KeyStroker ks = (KeyStroker)k.next();
         String keyVal = (String)i.next();
         if (keyVal.equals(which)) {
            mappedKeys.remove(ks);
            return;
         }
      }

   }

   public final static void setKeyStroke(String which, KeyEvent ke) {

      if (ke == null)
         return;
      Collection v = mappedKeys.values();
      Set o = mappedKeys.keySet();
      Iterator k = o.iterator();
      Iterator i = v.iterator();
      while (k.hasNext()) {
         KeyStroker ks = (KeyStroker)k.next();
         String keyVal = (String)i.next();
         if (keyVal.equals(which)) {
            mappedKeys.remove(ks);
            mappedKeys.put(new KeyStroker(ke.getKeyCode(),
                                          ke.isShiftDown(),
                                          ke.isControlDown(),
                                          ke.isAltDown(),
                                          ke.isAltGraphDown()),keyVal);
            return;
         }
      }
      // if we got here it was a dead key and we need to add it.
      mappedKeys.put(new KeyStroker(ke.getKeyCode(),
                                    ke.isShiftDown(),
                                    ke.isControlDown(),
                                    ke.isAltDown(),
                                    ke.isAltGraphDown()),which);


   }

   public final static void setKeyStroke(String which, KeyEvent ke, boolean isAltGr) {

      if (ke == null)
         return;
      Collection v = mappedKeys.values();
      Set o = mappedKeys.keySet();
      Iterator k = o.iterator();
      Iterator i = v.iterator();
      while (k.hasNext()) {
         KeyStroker ks = (KeyStroker)k.next();
         String keyVal = (String)i.next();
         if (keyVal.equals(which)) {
            mappedKeys.remove(ks);
            mappedKeys.put(new KeyStroker(ke.getKeyCode(),
                                          ke.isShiftDown(),
                                          ke.isControlDown(),
                                          ke.isAltDown(),
                                          isAltGr),keyVal);
            return;
         }
      }
      // if we got here it was a dead key and we need to add it.
      mappedKeys.put(new KeyStroker(ke.getKeyCode(),
                                    ke.isShiftDown(),
                                    ke.isControlDown(),
                                    ke.isAltDown(),
                                    isAltGr),which);


   }

   public final static HashMap getKeyMap() {

      return mappedKeys;
   }
}
