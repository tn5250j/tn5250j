/*
 * @(#)ConfigureFactory.java
 * @author  Kenneth J. Pouncey
 * Modified by LDC Luc
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
 * An implementation of the TN5250jLogFactory to provide logger instances.
 */
public class ConsoleLogger extends TN5250jLogger {

   private String clazz;

   ConsoleLogger () {

   }

   public void initialize(final String clazz) {
      this.clazz = clazz;
   }

   // printing methods:
   public void debug(Object message) {
      System.out.println("DEBUG ["+clazz+"] "+ message);
   }

   public void info(Object message) {
      System.out.println("INFO ["+clazz+"] "+ message);

   }

   public void warn(Object message) {
      System.out.println("WARN ["+clazz+"] "+ message);

   }

   public void error(Object message) {
      System.err.println("ERROR ["+clazz+"] "+ message);

   }

   public void fatal(Object message) {
      System.err.println("FATAL ["+clazz+"] "+ message);

   }

}