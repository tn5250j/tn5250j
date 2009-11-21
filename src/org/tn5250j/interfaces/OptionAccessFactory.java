/*
 * @(#)OptionAccessFactory.java
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
package org.tn5250j.interfaces;

import java.util.Vector;
/**
 * An interface defining objects that can create OptionAccess
 * instances.
 */
public abstract class OptionAccessFactory {

  private static OptionAccessFactory  factory;
   /**
    * @return An instance of the OptionAccess.
    */
  public static OptionAccessFactory  getInstance()
  {
    OptionAccessFactory.setFactory();
    return factory;
  }

  private static final void setFactory()
  {
    if (factory == null)
    {
      try
      {
        String  className = System.getProperty(OptionAccessFactory.class.getName());
        if (className != null)
        {
          Class<?> classObject = Class.forName(className);
          Object  object = classObject.newInstance();
          if (object instanceof OptionAccessFactory)
          {
            OptionAccessFactory.factory = (OptionAccessFactory) object;
          }
        }
      }
      catch (Exception  ex)
      {
        ;
      }
      if (OptionAccessFactory.factory == null)
      { //take the default
        OptionAccessFactory.factory = new org.tn5250j.OptionAccess();
      }
    }
  }

   abstract public Vector<String> getOptions();
   abstract public Vector<String> getOptionDescriptions();
   abstract public boolean isValidOption(String option);
   abstract public boolean isRestrictedOption(String option);
   abstract public int getNumberOfRestrictedOptions();
   abstract public void reload();

}