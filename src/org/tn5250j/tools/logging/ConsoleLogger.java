/*
 * @(#)ConsoleLogger.java
 * @author  Kenneth J. Pouncey
 *
 * Copyright:    Copyright (c) 2001, 2002, 2003
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
package org.tn5250j.tools.logging;


/**
 * An implementation of the TN5250jLogger to provide logger instances to the
 * console - System.out or System.err.
 */
public class ConsoleLogger extends TN5250jLogger {

   private String clazz;

      ConsoleLogger () {

   }

   public void initialize(final String clazz) {
      this.clazz = clazz;
//      logLevel = Integer.parseInt(ConfigureFactory.getInstance().getProperty(
//                  "emulator.logLevel", INFO + ""));
      logLevel = INFO;
   }

   // printing methods:
   public void debug(Object message) {
      if (isDebugEnabled())
         System.out.println("DEBUG ["+clazz+"] "+ message);
   }

   public void info(Object message) {
      if (logLevel <= INFO)
         System.out.println("INFO ["+clazz+"] "+ message);

   }

   public void warn(Object message) {
      if (logLevel <= WARN)
         System.out.println("WARN ["+clazz+"] "+ message);

   }

   public void warn(Object message, Throwable obj1) {
      if (logLevel <= WARN)
         System.out.println("WARN ["+clazz+"] "+ new StringBuffer(32).append(message)
                                                    .append(obj1.getMessage()));
   }

   public void error(Object message) {
      if (logLevel <= ERROR)
         System.err.println("ERROR ["+clazz+"] "+ message);

   }

   public void fatal(Object message) {
      if (logLevel <= FATAL)
         System.err.println("FATAL ["+clazz+"] "+ message);

   }

   public boolean isDebugEnabled() {
      if (logLevel <= DEBUG)
         return true;
      else
         return false;
   }

   public boolean isInfoEnabled() {
      if (logLevel <= INFO)
         return true;
      else
         return false;
   }

   public int getLevel() {
      return logLevel;
   }

   public void setLevel(int newLevel) {
      logLevel = newLevel;
   }

}