/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: master_jaf
 *
 * Description:
 * Alternative (extended) implementation of a codepage converter CCSID 875<->Unicode. 
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
 * @see http://www-01.ibm.com/software/globalization/ccsid/ccsid875.jsp
 */
public final class CCSID875 extends CodepageConverterAdapter {

	public final static String NAME = "875";
	public final static String DESCR = "Greek";

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
			'\u0014', '\u0015', '\u009E', '\u001A', ' ', '\u0391', '\u0392',
			'\u0393', '\u0394', '\u0395', '\u0396', '\u0397', '\u0398',
			'\u0399', '[', '.', '<', '(', '+', '!', '&', '\u039A', '\u039B',
			'\u039C', '\u039D', '\u039E', '\u039F', '\u03A0', '\u03A1',
			'\u03A3', ']', '$', '*', ')', ';', '^', '-', '/', '\u03A4',
			'\u03A5', '\u03A6', '\u03A7', '\u03A8', '\u03A9', '\u03AA',
			'\u03AB', '|', ',', '%', '_', '>', '?', '\u00A8', '\u0386',
			'\u0388', '\u0389', '\u00A0', '\u038A', '\u038C', '\u038E',
			'\u038F', '`', ':', '#', '@', '\'', '=', '"', '\u0385', 'a', 'b',
			'c', 'd', 'e', 'f', 'g', 'h', 'i', '\u03B1', '\u03B2', '\u03B3',
			'\u03B4', '\u03B5', '\u03B6', '\u00B0', 'j', 'k', 'l', 'm', 'n',
			'o', 'p', 'q', 'r', '\u03B7', '\u03B8', '\u03B9', '\u03BA',
			'\u03BB', '\u03BC', '\u00B4', '~', 's', 't', 'u', 'v', 'w', 'x',
			'y', 'z', '\u03BD', '\u03BE', '\u03BF', '\u03C0', '\u03C1',
			'\u03C3', '\u00A3', '\u03AC', '\u03AD', '\u03AE', '\u03CA',
			'\u03AF', '\u03CC', '\u03CD', '\u03CB', '\u03CE', '\u03C2',
			'\u03C4', '\u03C5', '\u03C6', '\u03C7', '\u03C8', '{', 'A', 'B',
			'C', 'D', 'E', 'F', 'G', 'H', 'I', '\u00AD', '\u03C9', '\u0390',
			'\u03B0', '\u2018', '\u2015', '}', 'J', 'K', 'L', 'M', 'N', 'O',
			'P', 'Q', 'R', '\u00B1', '\u00BD', '\u001A', '\u0387', '\u2019',
			'\u00A6', '\\', '\u001A', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
			'\u00B2', '\u00A7', '\u001A', '\u001A', '\u00AB', '\u00AC', '0',
			'1', '2', '3', '4', '5', '6', '7', '8', '9', '\u00B3', '\u00A9',
			'\u001A', '\u001A', '\u00BB', '\u009F', };

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
