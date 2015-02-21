/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: master_jaf
 *
 * Description:
 * Alternative (extended) implementation of a codepage converter CCSID 424<->Unicode. 
 *
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
package org.tn5250j.encoding.builtin;

/**
 * @author master_jaf
 * @see http://www-01.ibm.com/software/globalization/ccsid/ccsid424.jsp
 */
public final class CCSID424 extends CodepageConverterAdapter {

	public final static String NAME = "424";
	public final static String DESCR = "Hebrew";

	/*
	 * Char maps manually extracted from JTOpen v6.4. Because char maps can't be
	 * covered by any license, this should legal.
	 */
	private static final char[] codepage = { '\u0000', '\u0001', '\u0002',
			'\u0003', '\u009C', '\t', '\u0086', '\u007F', '\u0097', '\u008D',
			'\u008E', '\u000B', '\f', '\r', '\u000E', '\u000F', '\u0010',
			'\u0011', '\u0012', '\u0013', '\u009D', '\u0085', '\u0008',
			'\u0087', '\u0018', '\u0019', '\u0092', '\u008F', '\u001C',
			'\u001D', '\u001E', '\u001F', '\u0080', '\u0081', '\u0082',
			'\u0083', '\u0084', '\n', '\u0017', '\u001B', '\u0088', '\u0089',
			'\u008A', '\u008B', '\u008C', '\u0005', '\u0006', '\u0007',
			'\u0090', '\u0091', '\u0016', '\u0093', '\u0094', '\u0095',
			'\u0096', '\u0004', '\u0098', '\u0099', '\u009A', '\u009B',
			'\u0014', '\u0015', '\u009E', '\u001A', ' ', '\u05D0', '\u05D1',
			'\u05D2', '\u05D3', '\u05D4', '\u05D5', '\u05D6', '\u05D7',
			'\u05D8', '\u00A2', '.', '<', '(', '+', '|', '&', '\u05D9',
			'\u05DA', '\u05DB', '\u05DC', '\u05DD', '\u05DE', '\u05DF',
			'\u05E0', '\u05E1', '!', '$', '*', ')', ';', '\u00AC', '-', '/',
			'\u05E2', '\u05E3', '\u05E4', '\u05E5', '\u05E6', '\u05E7',
			'\u05E8', '\u05E9', '\u00A6', ',', '%', '_', '>', '?', '\u001A',
			'\u05EA', '\u001A', '\u001A', '\u00A0', '\u001A', '\u001A',
			'\u001A', '\u2017', '`', ':', '#', '@', '\'', '=', '"', '\u001A',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', '\u00AB', '\u00BB',
			'\u001A', '\u001A', '\u001A', '\u00B1', '\u00B0', 'j', 'k', 'l',
			'm', 'n', 'o', 'p', 'q', 'r', '\u001A', '\u001A', '\u20AC',
			'\u00B8', '\u20AA', '\u00A4', '\u00B5', '~', 's', 't', 'u', 'v',
			'w', 'x', 'y', 'z', '\u001A', '\u001A', '\u001A', '\u001A',
			'\u001A', '\u00AE', '^', '\u00A3', '\u00A5', '\u2022', '\u00A9',
			'\u00A7', '\u00B6', '\u00BC', '\u00BD', '\u00BE', '[', ']',
			'\u203E', '\u00A8', '\u00B4', '\u00D7', '{', 'A', 'B', 'C', 'D',
			'E', 'F', 'G', 'H', 'I', '\u00AD', '\u001A', '\u001A', '\u001A',
			'\u001A', '\u001A', '}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
			'R', '\u00B9', '\u202D', '\u202E', '\u202C', '\u001A', '\u001A',
			'\\', '\u00F7', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '\u00B2',
			'\u001A', '\u001A', '\u001A', '\u001A', '\u001A', '0', '1', '2',
			'3', '4', '5', '6', '7', '8', '9', '\u00B3', '\u202A', '\u202B',
			'\u200E', '\u200F', '\u009F', };

	public String getName() {
		return NAME;
	}

	public String getDescription() {
		return DESCR;
	}

	public String getEncoding() {
		return NAME;
	}

	@Override
	protected char[] getCodePage() {
		return codepage;
	}
}
