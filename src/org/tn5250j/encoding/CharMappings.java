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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Character Mappings for EBCDIC to ASCII and ASCII to EBCDIC translations
 */
public class CharMappings {

	public static final String DFT_ENC = "37";
	public static final int NATIVE_CP = 0;
	public static final int TOOLBOX_CP = 1;

	private static final HashMap<String,ICodePage> map = new HashMap<String, ICodePage>();

	public static String[] getAvailableCodePages() {
		Set<String> cpset = new HashSet<String>(); // no double entries
		for (String cp : BuiltInCodePageFactory.getInstance().getAvailableCodePages()) {
			cpset.add(cp);
		}
		for (String cp : ToolboxCodePageFactory.getInstance().getAvailableCodePages()) {
			cpset.add(cp);
		}
		String[] cparray = cpset.toArray(new String[cpset.size()]);
		Arrays.sort(cparray);
		return cparray;
	}

	public static ICodePage getCodePage(String encoding) {
		if (map.containsKey(encoding)) {
			return map.get(encoding);
		}

		ICodePage cp = BuiltInCodePageFactory.getInstance().getCodePage(encoding);
		if (cp != null) {
			map.put(encoding, cp);
			return cp;
		}

		cp = ToolboxCodePageFactory.getInstance().getCodePage(encoding);
		if (cp != null) {
			map.put(encoding, cp);
			return cp;
		}

		cp = JavaCodePageFactory.getCodePage(encoding);
		if (cp != null) {
			map.put(encoding, cp);
			return cp;
		}

		// unsupported codepage ==> return default
		return BuiltInCodePageFactory.getInstance().getCodePage(DFT_ENC);
	}

}
