/*
 * @(#)Log4jLogger.java
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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * An implementation of the TN5250jLogger to provide log4j logger instances.
 */
public class Log4jLogger extends TN5250jLogger {

   private Logger log;

   Log4jLogger () {

   }

   public void initialize(final String clazz) {
      log = Logger.getLogger(clazz);
   }

   // printing methods:
   public void debug(Object message) {
      log.debug(message);
   }

   public void info(Object message) {
      log.info(message);
   }

   public void warn(Object message) {
      log.warn(message);

   }

   public void warn(Object message, Throwable throw1) {
      log.warn(message, throw1);
   }

   public void error(Object message) {
      log.error(message);

   }

   public void fatal(Object message) {
      log.fatal(message);

   }

   public boolean isDebugEnabled() {
      return log.isDebugEnabled();
   }
 
   public boolean isInfoEnabled() {
      return log.isInfoEnabled();
   }

   public void setLevel(int newLevel) {

      switch (newLevel) {
      	case OFF:
      		Level.toLevel(Level.OFF_INT);
      		break;
      		
         case DEBUG:
            log.getLevel().toLevel(org.apache.log4j.Level.DEBUG_INT);
            break;

         case INFO:
            log.getLevel().toLevel(org.apache.log4j.Level.INFO_INT);
            break;

         case WARN:
            log.getLevel().toLevel(org.apache.log4j.Level.WARN_INT);
            break;

         case ERROR:
            log.getLevel().toLevel(org.apache.log4j.Level.ERROR_INT);
            break;

         case FATAL:
            log.getLevel().toLevel(org.apache.log4j.Level.FATAL_INT);
            break;

      }

   }

   public int getLevel() {

      switch (log.getLevel().toInt()) {

         case (org.apache.log4j.Level.DEBUG_INT):
            return DEBUG;

         case (org.apache.log4j.Level.INFO_INT):
            return INFO;

         case (org.apache.log4j.Level.WARN_INT):
            return WARN;

         case (org.apache.log4j.Level.ERROR_INT):
            return ERROR;

         case (org.apache.log4j.Level.FATAL_INT):
            return FATAL;
         default:
            return WARN;

      }

   }
}