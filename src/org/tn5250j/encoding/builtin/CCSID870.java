/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: master_jaf
 *
 * Description:
 * Alternative (extended) implementation of a codepage converter CCSID 870<->Unicode. 
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
 * @see http://www-01.ibm.com/software/globalization/ccsid/ccsid870.jsp
 */
public final class CCSID870 extends CodepageConverterAdapter {

	public final static String NAME = "870";
	public final static String DESCR = "Latin 2 - EBCDIC Multilingual";

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
			'\u00E4', '\u0163', '\u00E1', '\u0103', '\u010D', '\u00E7',
			'\u0107', '[', '.', '<', '(', '+', '!', '&', '\u00E9', '\u0119',
			'\u00EB', '\u016F', '\u00ED', '\u00EE', '\u013E', '\u013A',
			'\u00DF', ']', '$', '*', ')', ';', '^', '-', '/', '\u00C2',
			'\u00C4', '\u02DD', '\u00C1', '\u0102', '\u010C', '\u00C7',
			'\u0106', '|', ',', '%', '_', '>', '?', '\u02C7', '\u00C9',
			'\u0118', '\u00CB', '\u016E', '\u00CD', '\u00CE', '\u013D',
			'\u0139', '`', ':', '#', '@', '\'', '=', '"', '\u02D8', 'a', 'b',
			'c', 'd', 'e', 'f', 'g', 'h', 'i', '\u015B', '\u0148', '\u0111',
			'\u00FD', '\u0159', '\u015F', '\u00B0', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', '\u0142', '\u0144', '\u0161', '\u00B8',
			'\u02DB', '\u00A4', '\u0105', '~', 's', 't', 'u', 'v', 'w', 'x',
			'y', 'z', '\u015A', '\u0147', '\u0110', '\u00DD', '\u0158',
			'\u015E', '\u02D9', '\u0104', '\u017C', '\u0162', '\u017B',
			'\u00A7', '\u017E', '\u017A', '\u017D', '\u0179', '\u0141',
			'\u0143', '\u0160', '\u00A8', '\u00B4', '\u00D7', '{', 'A', 'B',
			'C', 'D', 'E', 'F', 'G', 'H', 'I', '\u00AD', '\u00F4', '\u00F6',
			'\u0155', '\u00F3', '\u0151', '}', 'J', 'K', 'L', 'M', 'N', 'O',
			'P', 'Q', 'R', '\u011A', '\u0171', '\u00FC', '\u0165', '\u00FA',
			'\u011B', '\\', '\u00F7', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'\u010F', '\u00D4', '\u00D6', '\u0154', '\u00D3', '\u0150', '0',
			'1', '2', '3', '4', '5', '6', '7', '8', '9', '\u010E', '\u0170',
			'\u00DC', '\u0164', '\u00DA', '\u009F', };

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
