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
public abstract class TN5250jLogger extends TN5250jLogFactory {

   TN5250jLogger () {

   }

   // Initialize the logger
   abstract public void initialize(final String clazz);

   // printing methods:
   abstract public void debug(Object message);

   abstract public void info(Object message);

   abstract public void warn(Object message);

   abstract public void error(Object message);

   abstract public void fatal(Object message);

}