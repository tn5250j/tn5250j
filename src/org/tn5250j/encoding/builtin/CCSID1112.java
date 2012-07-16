/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: master_jaf
 *
 * Description:
 * Alternative (extended) implementation of a codepage converter CCSID 1112<->Unicode. 
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
 * @see http://www-01.ibm.com/software/globalization/ccsid/ccsid1112.jsp
 */
public final class CCSID1112 extends CodepageConverterAdapter {

	public final static String NAME = "1112";
	public final static String DESCR = "Baltic, Multilingual";

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
			'\u0014', '\u0015', '\u009E', '\u001A', ' ', '\u00A0', '\u0161',
			'\u00E4', '\u0105', '\u012F', '\u016B', '\u00E5', '\u0113',
			'\u017E', '\u00A2', '.', '<', '(', '+', '|', '&', '\u00E9',
			'\u0119', '\u0117', '\u010D', '\u0173', '\u201E', '\u201C',
			'\u0123', '\u00DF', '!', '$', '*', ')', ';', '\u00AC', '-', '/',
			'\u0160', '\u00C4', '\u0104', '\u012E', '\u016A', '\u00C5',
			'\u0112', '\u017D', '\u00A6', ',', '%', '_', '>', '?', '\u00F8',
			'\u00C9', '\u0118', '\u0116', '\u010C', '\u0172', '\u012A',
			'\u013B', '\u0122', '`', ':', '#', '@', '\'', '=', '"', '\u00D8',
			'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', '\u00AB', '\u00BB',
			'\u0101', '\u017C', '\u0144', '\u00B1', '\u00B0', 'j', 'k', 'l',
			'm', 'n', 'o', 'p', 'q', 'r', '\u0156', '\u0157', '\u00E6',
			'\u0137', '\u00C6', '\u00A4', '\u00B5', '~', 's', 't', 'u', 'v',
			'w', 'x', 'y', 'z', '\u201D', '\u017A', '\u0100', '\u017B',
			'\u0143', '\u00AE', '^', '\u00A3', '\u012B', '\u00B7', '\u00A9',
			'\u00A7', '\u00B6', '\u00BC', '\u00BD', '\u00BE', '[', ']',
			'\u0179', '\u0136', '\u013C', '\u00D7', '{', 'A', 'B', 'C', 'D',
			'E', 'F', 'G', 'H', 'I', '\u00AD', '\u014D', '\u00F6', '\u0146',
			'\u00F3', '\u00F5', '}', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
			'R', '\u00B9', '\u0107', '\u00FC', '\u0142', '\u015B', '\u2019',
			'\\', '\u00F7', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '\u00B2',
			'\u014C', '\u00D6', '\u0145', '\u00D3', '\u00D5', '0', '1', '2',
			'3', '4', '5', '6', '7', '8', '9', '\u00B3', '\u0106', '\u00DC',
			'\u0141', '\u015A', '\u009F', };

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
