package org.tn5250j;
/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
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

public class Stream5250 {

    public int streamSize;
    public int opCode;
    public int dataStart;
    public int pos;
    public byte buffer[];

    public Stream5250(byte abyte0[]) {
        buffer = abyte0;
        // size without end of record 0xFF 0xEF
        streamSize = (abyte0[0] & 0xff) << 8 | abyte0[1] & 0xff;
        opCode = abyte0[9];
        dataStart = 6 + abyte0[6];
        pos = dataStart;
    }

    public final int getOpCode() {
        return opCode;
    }

    public final byte getNextByte()
        throws Exception  {
        if(pos > buffer.length)
            throw new Exception("Buffer length exceeded: " + pos);
        else
            return buffer[pos++];
    }

    public final void setPrevByte()
        throws Exception {
        if(pos == 0) {
            throw new Exception("Index equals zero.");
        }
        else {
            pos--;
            return;
      }
   }

   public final int getCurrentPos() {
      return pos;
   }

   public final byte getByteOffset(int off)
        throws Exception  {

        if((pos + off ) > buffer.length)
            throw new Exception("Buffer length exceeded: " + pos);
        else
            return buffer[pos + off];

   }

   public final boolean size() {
      return pos >= streamSize;
   }


   public final boolean hasNext() {

//      return pos >= buffer.length;
      return pos < streamSize;
   }
}
