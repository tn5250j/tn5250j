/**
 * Title: JavaCodePage
 * Copyright:   Copyright (c) 2001, 2002, 2003
 * Company:
 * @author  LDC, WVL, Luc
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

import java.io.UnsupportedEncodingException;
import java.io.CharConversionException;
import sun.io.CharToByteConverter;
import sun.io.ByteToCharConverter;

class JavaCodePage extends CodePage {
  JavaCodePage(String encoding, CharToByteConverter c2b, ByteToCharConverter b2c)
  {
    super(encoding);
    this.c2b = c2b;
    this.b2c = b2c;
  }

  public char ebcdic2uni (int index)
  {
    try
    {
      return b2c.convertAll(new byte[] { (byte) index})[0];
    }
    catch (CharConversionException cce)
    {
      return ' ';
    }
  }

  public byte uni2ebcdic (char index)
  {
    try
    {
      return c2b.convertAll(new char[] {index})[0];
    }
    catch (CharConversionException cce)
    {
      return 0x0;
    }
  }

  public static CodePage getCodePage(String encoding)
  {
    CharToByteConverter c2b;
    ByteToCharConverter b2c;
    try
    {
      c2b = CharToByteConverter.getConverter(encoding);
      b2c = ByteToCharConverter.getConverter(encoding);
    }
    catch (UnsupportedEncodingException uee)
    {
      c2b = null;
      b2c = null;
    }

    if ( (c2b != null) && (b2c != null) )
      return new JavaCodePage(encoding, c2b, b2c);

    return null;
  }


  private CharToByteConverter c2b;
  private ByteToCharConverter b2c;
}