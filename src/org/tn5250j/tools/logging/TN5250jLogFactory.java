/*
 * @(#)TN5250jLogFactory.java
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

import org.tn5250j.tools.logging.TN5250jLogger;
import java.util.HashMap;
import java.util.Map;

/**
 * An interface defining objects that can create Configure
 * instances.
 *
 * The model for the HashMap implementation of loggers came from the POI project
 * thanks to Nicola Ken Barozzi (nicolaken at apache.org) for the reference.
 *
 */
public abstract class TN5250jLogFactory {

   // map of TN5250jLogger instances, with classes as keys
   private static Map _loggers = new HashMap();
   private static boolean log4j;
   private static String customLogger;

   /**
    * Here we try to do a little more work up front.
    */
   static {

      try {
         String  customLogger = System.getProperty(TN5250jLogFactory.class.getName());
         if (customLogger == null) {
            try {
               Class classObject = Class.forName("org.apache.log4j.Logger");
               log4j = true;
            }
            catch (Exception  ex) { ; }
         }

      }
      catch (Exception  ex) { ; }

   }

   /**
    * Set package access only so we have to use getLogger() to return a logger object.
    */
   TN5250jLogFactory() {

   }

   /**
    * @return An instance of the TN5250jLogger.
    */
   public static TN5250jLogger getLogger (Class clazz) {
      return getLogger(clazz.getName());
   }

   /**
    * @return An instance of the TN5250jLogger.
    */
   public static TN5250jLogger getLogger (String clazzName) {
      TN5250jLogger logger = null;

      if (_loggers.containsKey(clazzName)) {
         logger = ( TN5250jLogger ) _loggers.get(clazzName);
      }
      else {

         if (customLogger != null) {
            try {

               Class classObject = Class.forName(customLogger);
               Object  object = classObject.newInstance();
               if (object instanceof TN5250jLogFactory) {
                  logger = (TN5250jLogger) object;
               }
            }
            catch (Exception  ex) { ; }
         }
         else {
            if (logger == null) {
               if (log4j)
                  logger = new Log4jLogger();
               else
                  // take the default logger.
                  logger = new ConsoleLogger();
            }

            logger.initialize(clazzName);
            _loggers.put(clazzName, logger);
         }
      }

      return logger;
   }

}