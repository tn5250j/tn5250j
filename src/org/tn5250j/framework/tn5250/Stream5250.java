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
package org.tn5250j.framework.tn5250;

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

    public Stream5250() {
       buffer = null;
       streamSize = 0;
       opCode = 0;
       dataStart = 0;
       pos = dataStart;
   }

    /**
     * This method takes a byte array and initializes the object information
     *    to be used.
     * 
     * @param abyte0
     */
    public void initialize(byte abyte0[]) {
       
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
        if(buffer == null || pos > buffer.length)
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

   /**
    * Returns where we are in the buffer
    * @return position in the buffer
    */
   public final int getCurrentPos() {
      return pos;
   }

   public final byte getByteOffset(int off)
        throws Exception  {

        if(buffer == null || (pos + off ) > buffer.length)
            throw new Exception("Buffer length exceeded: " + pos);
        else
            return buffer[pos + off];

   }

   public final boolean size() {
      return pos >= streamSize;
   }


   /**
    * Determines if any more bytes are available in the buffer to be processed.
    * @return yes or no
    */
   public final boolean hasNext() {

//      return pos >= buffer.length;
      return pos < streamSize;
   }

   /**
    * This routine will retrieve a segment based on the first two bytes being
    * the length of the segment.
    *
    * @return a new byte array containing the bytes of the segment.
    * @throws Exception
    */
   public final byte[] getSegment() throws Exception {

      // The first two bytes contain the length of the segment.
      int length = ((buffer[pos] & 0xff )<< 8 | (buffer[pos+1] & 0xff));
      // allocate space for it.
      byte[] segment = new byte[length];

      getSegment(segment,length,true);

      return segment;
   }


   /**
    * This routine will retrieve a byte array based on the first two bytes being
    * the length of the segment.
    *
    * @param segment - byte array
    * @param length - length of segment to return
    * @param adjustPos - adjust the position of the buffer to the end of the seg
    *                      ment
    * @throws Exception
    */
   public final void getSegment(byte[] segment, int length, boolean adjustPos)
               throws Exception {

      // If the length is larger than what is available throw an exception
      if((pos + length ) > buffer.length)
            throw new Exception("Buffer length exceeded: start " + pos
                                 + " length " + length);
      // use the system array copy to move the bytes from the buffer
      //    to the allocated byte array
      System.arraycopy(buffer,pos,segment,0,length);

      // update the offset to be after the segment so the next byte can be read
      if (adjustPos)
         pos +=length;

   }

}
