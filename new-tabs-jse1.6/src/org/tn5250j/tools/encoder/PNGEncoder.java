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

import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.IOException;
import java.util.zip.Deflater;

/**
 * This class encodes a Png file from an Image to an OutputStream.  No color
 * depths except 8 and 16 bpp are supported.  No additional Png blocks except
 * those absolutely necessary for Png encoding are included.
 */
public class PNGEncoder extends AbstractImageEncoder {

   public void saveImage() throws IOException, EncoderException {
      if (img == null)
      error("PNG encoding error: Image is NULL.");

      PixelGrabber pg = new PixelGrabber(img, 0,0,img.getWidth(null), img.getHeight(null), false);
      try {
         pg.grabPixels();
      }
      catch (InterruptedException e) {
        error("PNG encoding error: Unable to retrieve pixels from image.");
      }

      ColorModel cmodel = pg.getColorModel();
      int pixels = cmodel.getPixelSize();
      int numcolors = (int) Math.pow(2,pixels);
      if ((pixels != 8) && (pixels != 16) && (pixels != 24) && (pixels !=32)) {
         error("PNG encoding error: PNG method must have 8 or 16 bit colors.");
      }

      int[] pixelarray = null;
      byte[] pixelarray8 = null;
      if (pixels >= 16) {
         pixelarray = (int[]) pg.getPixels();
      }
      else {
         pixelarray8 = (byte[]) pg.getPixels();
      }

      ofile.write(byteFromInt(137));
      ofile.write(byteFromInt(80));
      ofile.write(byteFromInt(78));
      ofile.write(byteFromInt(71));
      ofile.write(byteFromInt(13));
      ofile.write(byteFromInt(10));
      ofile.write(byteFromInt(26));
      ofile.write(byteFromInt(10));

      // IHDR

      long crc = start_crc();

      // length

      ofile.write(bytesFromLong(13));
      ofile.write(byteFromChar('I'));
      ofile.write(byteFromChar('H'));
      ofile.write(byteFromChar('D'));
      ofile.write(byteFromChar('R'));

      crc = update_crc(crc, byteFromChar('I'));
      crc = update_crc(crc, byteFromChar('H'));
      crc = update_crc(crc, byteFromChar('D'));
      crc = update_crc(crc, byteFromChar('R'));

      int width = img.getWidth(null);
      int height = img.getHeight(null);
      crc = update_crc(crc, bytesFromLong(width));
      crc = update_crc(crc, bytesFromLong(height));
      crc = update_crc(crc, byteFromInt(8));

      if (pixels >= 16)
         crc = update_crc(crc, byteFromInt(2));
      else
         crc = update_crc(crc, byteFromInt(3));

      crc = update_crc(crc, byteFromInt(0));
      crc = update_crc(crc, byteFromInt(0));
      crc = update_crc(crc, byteFromInt(0));

      ofile.write(bytesFromLong(width));
      ofile.write(bytesFromLong(height));
      ofile.write(byteFromInt(8));

      if (pixels >= 16) {
         ofile.write(byteFromInt(2)); // Color type
      }
      else {
         ofile.write(byteFromInt(3)); // Color type
      }

      ofile.write(byteFromInt(0)); // Compression type
      ofile.write(byteFromInt(0)); // Filter method
      ofile.write(byteFromInt(0)); // Interlace method
      ofile.write(bytesFromLong(end_crc(crc)));

      // PLTE

      if (pixels == 8) {
         crc = start_crc();

         ofile.write(bytesFromLong(numcolors * 3));
         ofile.write(byteFromChar('P'));
         ofile.write(byteFromChar('L'));
         ofile.write(byteFromChar('T'));
         ofile.write(byteFromChar('E'));

         crc = update_crc(crc, byteFromChar('P'));
         crc = update_crc(crc, byteFromChar('L'));
         crc = update_crc(crc, byteFromChar('T'));
         crc = update_crc(crc, byteFromChar('E'));

         for(int i = 0; i < numcolors; i++) {
            byte red = byteFromInt(cmodel.getRed(i));
            byte green = byteFromInt(cmodel.getGreen(i));
            byte blue = byteFromInt(cmodel.getBlue(i));
            crc = update_crc(crc, red);
            crc = update_crc(crc, green);
            crc = update_crc(crc, blue);

            ofile.write(red);
            ofile.write(green);
            ofile.write(blue);
         }
         ofile.write(bytesFromLong(end_crc(crc)));
      }

      // IDAT
      byte[] outarray = null;
      if (pixels == 8)
        outarray = new byte[(pixelarray8.length) + height];
      else
        outarray = new byte[(pixelarray.length * 3) + height];

      for (int i = 0; i < outarray.length; i++) {
        outarray[i] = 0;
      }

      int size = 0;
      if (pixels >= 16)
        size = compress(outarray, pixelarray, cmodel, width, height);
      else
        size = compress(outarray, pixelarray8, width, height);

      crc = start_crc();

      ofile.write(bytesFromLong(size));
      ofile.write(byteFromChar('I'));
      ofile.write(byteFromChar('D'));
      ofile.write(byteFromChar('A'));
      ofile.write(byteFromChar('T'));

      crc = update_crc(crc, byteFromChar('I'));
      crc = update_crc(crc, byteFromChar('D'));
      crc = update_crc(crc, byteFromChar('A'));
      crc = update_crc(crc, byteFromChar('T'));

      ofile.write(outarray, 0, size);

      for (int i = 0; i < (size); i++) {
        crc = update_crc(crc, outarray[i]);
      }

      ofile.write(bytesFromLong(end_crc(crc)));

      // IEND
      crc = start_crc();

      ofile.write(bytesFromLong(0));
      ofile.write(byteFromChar('I'));
      ofile.write(byteFromChar('E'));
      ofile.write(byteFromChar('N'));
      ofile.write(byteFromChar('D'));

      crc = update_crc(crc, byteFromChar('I'));
      crc = update_crc(crc, byteFromChar('E'));
      crc = update_crc(crc, byteFromChar('N'));
      crc = update_crc(crc, byteFromChar('D'));

      ofile.write(bytesFromLong(end_crc(crc)));

   }

   /**
	* @param outarray
	* @param pixelarray
	* @param cmodel
	* @param width
	* @param height
	* @return
	* @throws EncoderException
	*/
   public int compress(byte[] outarray, int[] pixelarray, ColorModel cmodel, int width, int height) throws EncoderException {
       byte[] inarray = new byte[(pixelarray.length * 3) + height];
       for (int i = 0; i < height; i++) {
           inarray[i * ((width * 3) + 1)] = byteFromInt(0);
           for (int j = 0; j < (width * 3); j+= 3) {
               inarray[(i * ((width * 3) + 1)) + j + 1] = (byte) cmodel.getRed(pixelarray[(i * width) + (int)Math.floor(j / 3)]);
               inarray[(i * ((width * 3) + 1)) + j + 2] = (byte) cmodel.getGreen(pixelarray[(i * width) + (int)Math.floor(j / 3)]);
               inarray[(i * ((width * 3) + 1)) + j + 3] = (byte) cmodel.getBlue(pixelarray[(i * width) + (int)Math.floor(j / 3)]);
           }
       }
       return compressInternal(outarray, inarray);
   }

   /**
    * @param outarray
	* @param pixelarray
	* @param width
	* @param height
	* @return
	* @throws EncoderException
	*/
	public int compress(byte[] outarray, byte[] pixelarray, int width, int height) throws EncoderException {
       byte[] inarray = new byte[pixelarray.length + height];
       for (int i = 0; i < height; i++) {
           inarray[i * (width + 1)] = byteFromInt(0);
           for (int j = 0; j < width; j++) {
               inarray[(i * (width + 1)) + j + 1] = pixelarray[(i * width) + j];
           }
       }
       return compressInternal(outarray, inarray);
   }

   /**
    * @param outarray
    * @param inarray
    * @return
 	* @throws EncoderException
 	*/
	private int compressInternal(byte[] outarray, byte[] inarray) throws EncoderException {
		Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
		try {
			deflater.setInput(inarray, 0,inarray.length);
			deflater.finish();
			deflater.deflate(outarray);
			if (!deflater.finished()) {
				error("PNG encoding error: Deflater could not compress image data.");
			}
			return (deflater.getTotalOut());
		} finally {
			deflater.end();
		}
	}

   private long crc_table[] = null;

   private void make_crc_table() {
      crc_table = new long[256];
      long c;
      int n;
      int k;
      for (n = 0; n < 256; n++) {
         c = n;
         for (k = 0; k < 8; k++) {
            if ((c & 1) != 0)
                c = 0xedb88320L ^ (c >> 1);
            else
                c = c >> 1;

         }
         crc_table[n] = c;

      }
   }

   private final static long start_crc() {
      return 0xffffffffL;
   }
   private final static long end_crc(final long crc) {
      return crc ^ 0xffffffffL;
   }

   private long update_crc(long crc, byte[] buf) {

      long c = crc;
      for (int i = 0; i < buf.length; i++) {
         c = update_crc(c,buf[i]);
      }

      return c;
   }

   private long update_crc(long crc, byte buf) {
      if (crc_table == null) {
         make_crc_table();
      }
      return crc_table[(int) ((crc ^ buf) & 0xff)] ^ (crc >> 8);
   }

}

