/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: master_jaf
 *
 * Description:
 * Alternative (extended) implementation of a codepage converter CCSID 1025<->Unicode. 
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
 * @see http://www-01.ibm.com/software/globalization/ccsid/ccsid1025.jsp
 */
public final class CCSID1025 extends CodepageConverterAdapter {

	public final static String NAME = "1025";
	public final static String DESCR = "Cyrillic Multilingual";

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
			'\u0014', '\u0015', '\u009E', '\u001A', ' ', '\u00A0', '\u0452',
			'\u0453', '\u0451', '\u0454', '\u0455', '\u0456', '\u0457',
			'\u0458', '[', '.', '<', '(', '+', '!', '&', '\u0459', '\u045A',
			'\u045B', '\u045C', '\u045E', '\u045F', '\u042A', '\u2116',
			'\u0402', ']', '$', '*', ')', ';', '^', '-', '/', '\u0403',
			'\u0401', '\u0404', '\u0405', '\u0406', '\u0407', '\u0408',
			'\u0409', '|', ',', '%', '_', '>', '?', '\u040A', '\u040B',
			'\u040C', '\u00AD', '\u040E', '\u040F', '\u044E', '\u0430',
			'\u0431', '`', ':', '#', '@', '\'', '=', '"', '\u0446', 'a', 'b',
			'c', 'd', 'e', 'f', 'g', 'h', 'i', '\u0434', '\u0435', '\u0444',
			'\u0433', '\u0445', '\u0438', '\u0439', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', '\u043A', '\u043B', '\u043C', '\u043D',
			'\u043E', '\u043F', '\u044F', '~', 's', 't', 'u', 'v', 'w', 'x',
			'y', 'z', '\u0440', '\u0441', '\u0442', '\u0443', '\u0436',
			'\u0432', '\u044C', '\u044B', '\u0437', '\u0448', '\u044D',
			'\u0449', '\u0447', '\u044A', '\u042E', '\u0410', '\u0411',
			'\u0426', '\u0414', '\u0415', '\u0424', '\u0413', '{', 'A', 'B',
			'C', 'D', 'E', 'F', 'G', 'H', 'I', '\u0425', '\u0418', '\u0419',
			'\u041A', '\u041B', '\u041C', '}', 'J', 'K', 'L', 'M', 'N', 'O',
			'P', 'Q', 'R', '\u041D', '\u041E', '\u041F', '\u042F', '\u0420',
			'\u0421', '\\', '\u00A7', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'\u0422', '\u0423', '\u0416', '\u0412', '\u042C', '\u042B', '0',
			'1', '2', '3', '4', '5', '6', '7', '8', '9', '\u0417', '\u0428',
			'\u042D', '\u0429', '\u0427', '\u009F', };

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
