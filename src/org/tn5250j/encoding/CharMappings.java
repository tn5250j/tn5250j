/**
 * Title: CharMappings.java
 * Copyright:   Copyright (c) 2001,2002,2003
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
import java.util.HashMap;

/**
 * Character Mappings for EBCDIC to ASCII and ASCII to EBCDIC translations
 */
public class CharMappings {

// note to myself - execute the following on linux to obtain others
// EXAMPLE *** recode -v -h ebcdic-cp-es > ebcdic284.txt

   public static final String DFT_ENC = "37";

   public static String[] getAvailableCodePages()
   {
      return  NativeCodePage.acp;
   }

   public static CodePage getCodePage(String encoding)
   {
     if (map.containsKey(encoding))
     {
       return (CodePage) map.get(encoding);
     }

     CodePage cp = NativeCodePage.getCodePage(encoding);
     if (cp != null)
     {
       map.put(encoding, cp);
       return cp;
     }

     cp = ToolboxCodePage.getCodePage(encoding);
     if (cp != null)
     {
       map.put(encoding, cp);
       return cp;
     }

     cp = JavaCodePage.getCodePage(encoding);
     if (cp != null)
     {
       map.put(encoding, cp);
       return cp;
     }

     // unsupported codepage
     // ==> return default;
     return NativeCodePage.getCodePage(DFT_ENC);
   }

   private static final HashMap map = new HashMap();
}