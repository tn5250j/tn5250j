package org.tn5250j;

/*
 * @(#)KeyStrokenizer.java
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

public class KeyStrokenizer {

   public KeyStrokenizer() {

      setKeyStrokes(null);
   }

   public void setKeyStrokes (String strokes) {

      if (strokes != null) {
//         keyStrokes.setLength(0);
         keyStrokes.append(strokes);
//         System.out.println("set "+ keyStrokes);
//         index = 0;
         length = keyStrokes.length();
      }
      else {

         keyStrokes = new StringBuffer();
         sb = new StringBuffer();
         index = 0;
         length = 0;

      }

   }

   public boolean hasMoreKeyStrokes() {
      return length > index;
   }

   public String nextKeyStroke() {

      String s = null;
      boolean gotOne = false;
      int start = index;
      if(length > index) {
         sb.setLength(0);

         char c = keyStrokes.charAt(index);
         switch(c) {
            case '[':
               sb.append(c);
               index++;
               // we need to throw an error here
               if(index >= length) {
                  System.out.println(" mnemonic key was incomplete :1 " +
                                       "at position " + index);
               }
               else {
                  c = keyStrokes.charAt(index);

                  if(c == '[')
                       index++;
                  else {
                     while(!gotOne) {

                        if(c == ']') { // did we find an ending
                           sb.append(c);
                           index++;
                           gotOne = true;
                        }
                        else {
                           sb.append(c);
                           index++;
                           // we need to throw an error here because we did not
                           //   find an ending for the potential mnemonic
                           if(index >= length) {
                              System.out.println(
                              " mnemonic key was not not complete ending not found :2 " +
                                          "at position " + index);
                           }
                           c = keyStrokes.charAt(index);
                        }
                     }
                  }
               }
               break;

            case ']':
               index++;
               if(index >= length) {
                  System.out.println(
                  " mnemonic key was not not complete ending not found :3 " +
                              "at position " + index);
               }
               c = keyStrokes.charAt(index);
               if(c == ']') {
                  sb.append(c);
                  index++;
               }
               else {
                  System.out.println(
                  " mnemonic key was not complete beginning not found :4 " +
                              "at position " + index);
               }
               break;

            default:
               sb.append(c);
               index++;
               break;
         }
         if(sb != null) {
            s = new String(sb);
//            System.out.println("next before "+ keyStrokes + " " + start + " " + index + " " + length);
            keyStrokes.delete(start,index);
            index = 0;
            length = keyStrokes.length();
//            System.out.println("next before "+ keyStrokes + " " + start + " " + index + " " + length);
         }

      }
//      System.out.println("next "+ keyStrokes);

      return s;
   }

   public String getUnprocessedKeyStroked() {


      if(index >= length)
//      if (keyStrokes.length() == 0)
         return null;
      else {
//         return keyStrokes.toString();
         String ks = keyStrokes.substring(index);
         keyStrokes.setLength(0);
         return ks;
      }

   }

   private StringBuffer keyStrokes;
   private StringBuffer sb;
   private int index;
   private int length;

}