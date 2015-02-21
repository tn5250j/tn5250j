/**
 * Title: JavaCodePage
 * Copyright:   Copyright (c) 2001, 2002, 2003
 * Company:
 * @author  LDC, WVL, Luc, master_jaf
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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

/* package */ class JavaCodePageFactory extends AbstractCodePage {

	private final CharsetEncoder encoder;
	private final CharsetDecoder decoder;

	/* package */ JavaCodePageFactory(String encoding, CharsetEncoder encoder, CharsetDecoder decoder) {
		super(encoding);
		this.encoder = encoder;
		this.decoder = decoder;
	}

	/* (non-Javadoc)
	 * @see org.tn5250j.encoding.CodePage#ebcdic2uni(int)
	 */
	@Override
	public char ebcdic2uni(int codepoint) {
		try {
			final ByteBuffer in = ByteBuffer.wrap(new byte[] { (byte) codepoint });
			final CharBuffer out = this.decoder.decode(in);
			return out.get(0);
		} catch (Exception cce) {
			return ' ';
		}
	}

	/* (non-Javadoc)
	 * @see org.tn5250j.encoding.CodePage#uni2ebcdic(char)
	 */
	@Override
	public byte uni2ebcdic(char character) {
		try {
			final CharBuffer in = CharBuffer.wrap(new char[] {character});
			final ByteBuffer out = this.encoder.encode(in);
			return out.get(0);
		} catch (Exception cce) {
			return 0x0;
		}
	}

	/**
	 * @param encoding
	 * @return A new {@link CodePage} object OR null, if not available.
	 */
	/* package */ static ICodePage getCodePage(final String encoding) {
		CharsetDecoder dec = null;
		CharsetEncoder enc = null;
		try {
			final Charset cs = java.nio.charset.Charset.forName(encoding);
			dec = cs.newDecoder();
			enc = cs.newEncoder();
		} catch (Exception e) {
			enc = null;
			dec = null;
		}
		if ((enc != null) && (dec != null)) {
			return new JavaCodePageFactory(encoding, enc, dec);
		}
		return null;
	}

}