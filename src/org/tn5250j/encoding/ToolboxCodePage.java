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

   public static String[] getAvailableCodePages()
   {
      return  NativeCodePage.acp;
   }

  public char ebcdic2uni (int index)
  {
    Object result;
    try
    {
      result = TOSTRING_METHOD.invoke(this.converter, new Object[] {new byte[] { (byte) (index & 0xFF) }});
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
    if (CONVERTER_CONSTRUCTOR == null)
      return null;

    Object conv;
    try
    {
      conv = CONVERTER_CONSTRUCTOR.newInstance(new Object[] {encoding});
    }
    catch (Throwable t)
    {
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


    static final String[] acp = {
         "Big5",
         "Cp037",
         "Cp273",
         "Cp277",
         "Cp278",
         "Cp280",
         "Cp284",
         "Cp285",
         "Cp297",
         "Cp420",
         "Cp424",
         "Cp437",
         "Cp500",
         "Cp737",
         "Cp775",
         "Cp838",
         "Cp850",
         "Cp852",
         "Cp855",
         "Cp856",
         "Cp857",
         "Cp858",
         "Cp860",
         "Cp861",
         "Cp862",
         "Cp863",
         "Cp864",
         "Cp865",
         "Cp866",
         "Cp868",
         "Cp869",
         "Cp870",
         "Cp871",
         "Cp874",
         "Cp875",
         "Cp918",
         "Cp921",
         "Cp922",
         "Cp923",  // IBM Latin-9.
         "Cp930",
         "Cp933",
         "Cp935",
         "Cp937",
         "Cp939",
         "Cp942",
         "Cp943",
         "Cp948",
         "Cp949",
         "Cp950",
         "Cp964",
         "Cp970",
         "Cp1006",
         "Cp1025",
         "Cp1026",
         "Cp1046",
         "Cp1097",
         "Cp1098",
         "Cp1112",
         "Cp1122",
         "Cp1123",
         "Cp1124",
         "Cp1140",
         "Cp1141",
         "Cp1142",
         "Cp1143",
         "Cp1144",
         "Cp1145",
         "Cp1146",
         "Cp1147",
         "Cp1148",
         "Cp1149",
         "Cp1252",
         "Cp1250",
         "Cp1251",
         "Cp1253",
         "Cp1254",
         "Cp1255",
         "Cp1256",
         "Cp1257",
         "Cp1258",
         "Cp1381",
         "Cp1383",
         "Cp33722"};

}