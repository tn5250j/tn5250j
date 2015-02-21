/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,202,2003
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
package org.tn5250j.tools.encoder;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This is the base class for encoding a component to a stream file.
 */
public abstract class AbstractImageEncoder implements Encoder {

   protected Image img = null;
   protected OutputStream ofile = null;

   public void encode(Image image, OutputStream os) throws IOException, EncoderException {
      img = image;
      ofile = os;
      saveImage();
   }

   public void encode(Component component, OutputStream os) throws IOException, EncoderException {
      encode(snapshot(component), os);
   }

   public static Image snapshot(Component component) {
      Image img = component.createImage(component.getSize().width,
                             component.getSize().height);
      if (img != null) {
      Graphics igc = img.getGraphics();
      //Gotta set the clip, or else paint throws an exception
      igc.setClip(0, 0, component.getSize().width,
         component.getSize().height);
         component.paint(igc);
      }
      return img;
   }

   public abstract void saveImage() throws IOException, EncoderException;

   public byte createByte(int b7, int b6, int b5, int b4, int b3, int b2, int b1, int b0) {
      byte bits = 0;
      if (b0 == 1) bits = (byte) (bits | 1);
      if (b1 == 1) bits = (byte) (bits | 2);
      if (b2 == 1) bits = (byte) (bits | 4);
      if (b3 == 1) bits = (byte) (bits | 8);
      if (b4 == 1) bits = (byte) (bits | 16);
      if (b5 == 1) bits = (byte) (bits | 32);
      if (b6 == 1) bits = (byte) (bits | 64);
      if (b7 == 1) bits = (byte) (bits | 128);
      return bits;
   }

   public static byte byteFromInt(int value) {
      return ((byte) (value & 255));
   }

   public static byte[] bytesFromLong(long value) {
      byte[] buf = new byte[4];
      buf[0] = ((byte) ((value >> 24) & 255));
      buf[1] = ((byte) ((value >> 16) & 255));
      buf[2] = ((byte) ((value >> 8) & 255));
      buf[3] = ((byte) ((value) & 255));
      return buf;
   }

   public static byte byteFromChar(char ochar) {
      int temp = ochar;
      byte bits = 0;

      int curpos = 0;
      for (int i = 0; i <=7; i++) {
        if ((temp & ((byte) Math.pow(2, i))) != 0) {
            bits = (byte) (bits | ((byte) Math.pow(2, i)));
        }
      }
      return bits;
   }

   /**
   * Compress the given color into one 8 bit representation.
   * @param clr integer representation of rgb value (lowest byte is blue,
   *            second lowest byte is green, third lowest byte is red)
   * @return color compressed into 8-bit representation
   */
   public byte compressColor(int clr) {
      return compressColor((clr >> 16) & 255, (clr >> 8) & 255, clr & 255);
   }

   /**
   * Compress the given color into one 8 bit representation.
   * @param red value of the red portion of the color
   * @param green value of the green portion of the color
   * @param blue value of the blue portion of the color
   * @return color compressed into 8-bit representation
   */
   public byte compressColor(int red, int green, int blue) {
      // take 3 most significatnt bits of red, 3 most significant bits of
      // green and 2 most significant bits of blue to form 8-bit compression
      // of color values

      return (byte) ((red & 224) | ((green >> 3) & 28) | ((blue >> 6) & 3));
   }

   protected void error(String msg) throws EncoderException {
      throw new EncoderException(msg);
   }

}

