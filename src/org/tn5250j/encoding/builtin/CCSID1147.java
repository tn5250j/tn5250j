/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: master_jaf
 *
 * Description:
 * Alternative (extended) implementation of a codepage converter CCSID 1147<->Unicode. 
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
 * @see http://www-01.ibm.com/software/globalization/ccsid/ccsid1147.jsp
 */
public final class CCSID1147 extends CodepageConverterAdapter {

	public final static String NAME = "1147";
	public final static String DESCR = "ECECP: France";

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
			'\u0014', '\u0015', '\u009E', '\u001A', ' ', '\u00A0', '\u00E2',
			'\u00E4', '@', '\u00E1', '\u00E3', '\u00E5', '\\', '\u00F1',
			'\u00B0', '.', '<', '(', '+', '!', '&', '{', '\u00EA', '\u00EB',
			'}', '\u00ED', '\u00EE', '\u00EF', '\u00EC', '\u00DF', '\u00A7',
			'$', '*', ')', ';', '^', '-', '/', '\u00C2', '\u00C4', '\u00C0',
			'\u00C1', '\u00C3', '\u00C5', '\u00C7', '\u00D1', '\u00F9', ',',
			'%', '_', '>', '?', '\u00F8', '\u00C9', '\u00CA', '\u00CB',
			'\u00C8', '\u00CD', '\u00CE', '\u00CF', '\u00CC', '\u00B5', ':',
			'\u00A3', '\u00E0', '\'', '=', '"', '\u00D8', 'a', 'b', 'c', 'd',
			'e', 'f', 'g', 'h', 'i', '\u00AB', '\u00BB', '\u00F0', '\u00FD',
			'\u00FE', '\u00B1', '[', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q',
			'r', '\u00AA', '\u00BA', '\u00E6', '\u00B8', '\u00C6', '\u20AC',
			'`', '\u00A8', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '\u00A1',
			'\u00BF', '\u00D0', '\u00DD', '\u00DE', '\u00AE', '\u00A2', '#',
			'\u00A5', '\u00B7', '\u00A9', ']', '\u00B6', '\u00BC', '\u00BD',
			'\u00BE', '\u00AC', '|', '\u00AF', '~', '\u00B4', '\u00D7',
			'\u00E9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', '\u00AD',
			'\u00F4', '\u00F6', '\u00F2', '\u00F3', '\u00F5', '\u00E8', 'J',
			'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', '\u00B9', '\u00FB',
			'\u00FC', '\u00A6', '\u00FA', '\u00FF', '\u00E7', '\u00F7', 'S',
			'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '\u00B2', '\u00D4', '\u00D6',
			'\u00D2', '\u00D3', '\u00D5', '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', '\u00B3', '\u00DB', '\u00DC', '\u00D9', '\u00DA',
			'\u009F', };

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
