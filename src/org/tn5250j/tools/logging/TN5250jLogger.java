/*
 * @(#)TN5250jLogger.java
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
 * An implementation of the TN5250jLogFactory to provide logger instances.
 */
public abstract class TN5250jLogger extends TN5250jLogFactory {

   // debug levels - The levels work from lower to higher.  The lower levels
   //   will be activated by turning on a higher level
   public static final int OFF = 0;
   public static final int DEBUG = 1;
   public static final int INFO  = 2;
   public static final int WARN  = 4;
   public static final int ERROR = 8;
   public static final int FATAL = 16;

   // The log level at which we are now
   protected int logLevel;

   TN5250jLogger () {

   }

   // Initialize the logger
   abstract public void initialize(final String clazz);

   // printing methods:
   abstract public void debug(Object message);

   abstract public void info(Object message);

   abstract public void warn(Object message);

   abstract public void warn(Object message, Throwable obj1);

   abstract public void error(Object message);

   abstract public void fatal(Object message);

   // are we enabled for X?
   abstract public boolean isDebugEnabled();

   abstract public boolean isInfoEnabled();

   // level accessors
   abstract public void setLevel(int newLevel);
   abstract public int getLevel();

}