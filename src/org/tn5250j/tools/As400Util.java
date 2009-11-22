/**
 * $Id$
 * 
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 * @author: master_jaf
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
package org.tn5250j.tools;

/**
 * Collection of commonly used methods.
 * 
 * @author master_jaf
 */
public class As400Util {

	/**
	 * Convert an as400 packed field to an integer
	 */
	public static final int packed2int(final byte[] cByte, final int startOffset, final int length) {

		StringBuffer sb = new StringBuffer(length * 2);

		int end = startOffset + length - 1;

		// example field of buffer length 4 with decimal precision 0
		// output length is (4 * 2) -1 = 7
		//
		// each byte of the buffer contains 2 digits, one in the zone
		// portion and one in the zone portion of the byte, the last
		// byte of the field contains the last digit in the ZONE
		// portion and the sign is contained in the DIGIT portion.
		//
		// The number 1234567 would be represented as follows:
		// byte 1 of 4 -> 12
		// byte 2 of 4 -> 34
		// byte 3 of 4 -> 56
		// byte 4 of 4 -> 7F The F siginifies a positive number
		//
		// The number -1234567 would be represented as follows:
		// byte 1 of 4 -> 12
		// byte 2 of 4 -> 34
		// byte 3 of 4 -> 56
		// byte 4 of 4 -> 7D The D siginifies a negative number
		//
		for (int f = startOffset - 1; f < end - 1; f++) {
			byte bzd = cByte[f];
			int byteZ = (bzd >> 4) & 0x0f; // get the zone portion
			int byteD = (bzd & 0x0f); // get the digit portion

			sb.append(byteZ); // assign the zone portion as the first digit
			sb.append(byteD); // assign the digit portion as the second digit
		}

		// here we obtain the last byte to determine the sign of the field
		byte bzd = cByte[end - 1];

		int byteZ = (bzd >> 4) & 0x0f; // get the zone portion
		int byteD = (bzd & 0x0f); // get the digit portion
		sb.append(byteZ); // append the zone portion as the
		// the last digit of the number
		// convert to integer
		int p2i = Integer.parseInt(sb.toString());

		// Here we interrogate the the DIGIT portion for the sign
		// 0x0f = positive -> 0x0f | 0x0d = 0x0f
		// 0x0d = negative -> 0x0d | 0x0d = 0x0d
		if ((byteD | 0x0d) == 0x0d)
			p2i *= -1;

		return p2i;

	}

}
