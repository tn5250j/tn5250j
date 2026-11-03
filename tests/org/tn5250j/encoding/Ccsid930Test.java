/**
 * $Id$
 * <p>
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009,2021
 * Company:
 *
 * @author: nitram509
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j.encoding;

import org.junit.Test;
import org.tn5250j.encoding.builtin.CCSID930;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.assertEquals;
import static org.tn5250j.framework.tn5250.ByteExplainer.SHIFT_IN;
import static org.tn5250j.framework.tn5250.ByteExplainer.SHIFT_OUT;

public class Ccsid930Test {

    @Test
    public void double_byte_character_can_be_converted() throws UnsupportedEncodingException {
        CCSID930 ccsid930 = new CCSID930();
        char c;

        c = ccsid930.ebcdic2uni(SHIFT_IN);
        assertEquals("SHIFT IN must be converted to zero", 0, c);

        c = ccsid930.ebcdic2uni(0x43);
        assertEquals("first byte must be converted to zero", 0, c);
        c = ccsid930.ebcdic2uni(0x8C);
        assertEquals("second byte must be converted to a japanese character", '\u30B5', c);

        c = ccsid930.ebcdic2uni(0x43);
        assertEquals("first byte must be converted to zero", 0, c);
        c = ccsid930.ebcdic2uni(0xD1);
        assertEquals("second byte must be converted to a japanese character", '\u30D6', c);

        c = ccsid930.ebcdic2uni(SHIFT_OUT);
        assertEquals("SHIFT OUT must be converted to zero", 0, c);
    }
}
