/**
 * Title: ToolboxCodePage
 * Copyright:   Copyright (c) 2001, 2002, 2003
 * Company:
 * @author  Kenneth J. Pouncey
 *          rewritten by LDC, WVL, Luc
 * @version 0.4
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
package org.tn5250j.encoding;

import java.lang.reflect.*;

class ToolboxCodePage
     extends CodePage
{
  ToolboxCodePage(String encoding, Object converter)
  {
    super(encoding);
    this.converter = converter;
  }

  public char ebcdic2uni (int index)
  {
    Object result;
    try
    {
      result = TOSTRING_METHOD.invoke(this.converter, new Object[] {new byte[] { (byte) index}});
    }
    catch (Throwable t)
    {
      result = null;
    }

    if (result == null)
      return 0x00;

    return ((String)result).charAt(0);
  }

  public byte uni2ebcdic (char index)
  {
    Object result;
    try
    {
      result = TOBYTES_METHOD.invoke(this.converter, new Object[] {new String(new char[] {index})});
    }
    catch (Throwable t)
    {
      result = null;
    }

    if (result == null)
      return 0x00;

    return ((byte[])result)[0];
  }

  public static CodePage getCodePage(String encoding)
  {
    System.err.println("Trying ToolboxCodePage:" + encoding);
    
    if (CONVERTER_CONSTRUCTOR == null)
      return null;

    Object conv;
    try
    {
      conv = CONVERTER_CONSTRUCTOR.newInstance(new Object[] {encoding});
    }
    catch (Throwable t)
    {
      System.err.println("Error while loading " + CONVERTER_NAME + ": " + t);
      conv = null;
    }

    if (conv != null)
      return new ToolboxCodePage(encoding, conv);

    return null;
  }


  private Object converter;

  private static final String      CONVERTER_NAME = "com.ibm.as400.access.CharConverter";
  private static final String      TOBYTES_NAME   = "stringToByteArray";
  private static final String      TOSTRING_NAME  = "byteArrayToString";
  private static final Class       CONVERTER_CLASS;
  private static final Constructor CONVERTER_CONSTRUCTOR;
  private static final Method      TOBYTES_METHOD;
  private static final Method      TOSTRING_METHOD;

  static
  {
    Class       conv_class;
    Constructor conv_constructor;
    Method      toBytes_method;
    Method      toString_method;

    try
    {
      ClassLoader loader = ToolboxCodePage.class.getClassLoader();
      if (loader == null)
        loader = ClassLoader.getSystemClassLoader();

      conv_class       = loader.loadClass(CONVERTER_NAME);
      conv_constructor = conv_class.getConstructor(new Class[] {String.class});
      toBytes_method   = conv_class.getMethod(TOBYTES_NAME, new Class[] {String.class});
      toString_method  = conv_class.getMethod(TOSTRING_NAME, new Class[] {byte[].class});
    }
    catch (Throwable t)
    {
      conv_class       = null;
      conv_constructor = null;
      toBytes_method   = null;
      toString_method  = null;
    }

    CONVERTER_CLASS       = conv_class;
    CONVERTER_CONSTRUCTOR = conv_constructor;
    TOBYTES_METHOD        = toBytes_method;
    TOSTRING_METHOD       = toString_method;
  }
}