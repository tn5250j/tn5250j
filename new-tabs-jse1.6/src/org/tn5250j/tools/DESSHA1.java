/**
 * Title: DESSHA1.java
 *
 * Copyright:   Copyright (c) 2001, 2002, 2003
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.5
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


import java.io.Serializable;
import java.security.MessageDigest;

public class DESSHA1 implements Serializable {

	private static final long serialVersionUID = 1L;

	/** Message digest object. */
	private final MessageDigest md;

	/** quick array to convert byte values to hex codes. */
	private final static char[] HEX = {'0','1','2','3','4','5','6','7',
																		 '8', '9','a','b','c','d','e','f'};

	/**
	 * Creates a new <code>MessageDigest</code> instance.
	 * @throws InstantiationException
	 */
	public DESSHA1() throws InstantiationException {
		try {
         md=MessageDigest.getInstance("SHA");
      }
		catch (java.security.NoSuchAlgorithmException e) {
         throw new InstantiationException("No such algorithm: SHA");
      }
	}

	/**
	 * used to hash passwords and other key data to fireSend over the wire,
	 * to prevent plaintext transmit.
	 *
	 * @param identifier prefix, such as the SessionID
	 * @param key passkey, such as password or handshake
	 * @return <code>String</code> with the hash in hex string form
	 */
   public String digest(String identifier,String key) {
		if (identifier!=null)
         md.update(identifier.getBytes());
		if (key!=null)
	      md.update(key.getBytes());

		return DESSHA1.bytesToHex(md.digest());
	}

	/**
	 * This utility method is passed an array of bytes. It returns
	 * this array as a String in hexadecimal format. This is used
	 * internally by <code>digest()</code>. Data is returned in
	 * the format specified by the Jabber protocol.
	 *
	 * @param data
	 * @return String of our hex bytes
	 */
	public static String bytesToHex(byte[] data) {

      StringBuffer retval=new StringBuffer();

		for(int i=0;i<data.length;i++) {
			retval.append(HEX[ (data[i]>>4)&0x0F ]);
			retval.append(HEX[ data[i]&0x0F ]);
		}

		return retval.toString();
	}
}
