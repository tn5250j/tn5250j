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
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;

/**
 * Used to encode components into different image file formats. ie .GIF and .PNG
 */
public class EncodeComponent {

   /** specifies PNG encoding */
   public static final Encoding PNG = new Encoding("PNG",
      "Portable Network Graphics",
      "org.tn5250j.tools.encoder.PNGEncoder",
      " PNG Load Error");

   public static final Encoding ENCODINGS[] = {
      PNG
   };

   /**
    * Invoke this methods on Java components to encode their image into the
    * specified format
    * @param component component to encode
    * @param encoding type of encoding to use (Currently GIF or PNG)
    * @param output stream to which to write the encoding
    */
   public static void encode(Encoding encoding, Component component, OutputStream output) throws IOException, EncoderException {
      Encoder encoder = encoding.getEncoder();
      if (encoder == null) {
   	   throw new EncoderException("Graphics Encoder could not be loaded.");
      }
      encoder.encode(component, output);
   }

   /**
    * Invoke this methods on Java components to encode their image into the
    * specified format
    * @param component component to encode
    * @param encoding type of encoding to use (GIF, PNG, JPEG, EPS, PS, PDF,
    * or PCL)
    * @param file file to which to write the encoding
    */
   public static void encode(Encoding encoding, Component component, File file) throws IOException, EncoderException {
      OutputStream os = new java.io.FileOutputStream(file);
      encode(encoding, component, os);
      os.close();
   }

      /**
      * Class used to enumerate valid encodings
      */
      public static class Encoding {
         private String shortName;
         private String longName;
         private String encoderClass;
         private String failureMessage;

         private Encoder encoder;

          /**
           * Constructs and new image encoder.
           */
         public Encoding(String short_name, String long_name,
                     String encoder_class, String failure_message) {
            this.shortName = short_name;
            this.longName = long_name;
            this.encoderClass = encoder_class;
            this.failureMessage = failure_message;
         }

         /**
          * Return the short brief name of the supported encoding type
          */
         public String getShortName() {
            return shortName;
         }

         /**
          * Return the long name of the supported encoding type
          */
         public String getLongName() {
            return longName;
         }

          /**
           * Returns a string representation bassed on the long and short name of the
           * Encoder.
           */
         public String toString() {
            return getLongName() + " (" + getShortName() + ")";
         }

         /**
          * Message to return about possible reasons for encoder load
          * failure (i.e. getEncoder() returns null)
          */
         public String getFailureMessage() {
            return failureMessage != null ? failureMessage : "There was a failure loading encoder.";
         }

         /**
          * Return an encoder for this encoding type.
          * @return returns null if it cannot locate or load the encoder
          */
         public Encoder getEncoder() {
            if (encoder == null) {
               // encoder null so attempt to load it
               Class<?> encoder_class = null;
               try {
                  encoder_class = Class.forName(encoderClass);
               }
               catch (ClassNotFoundException cnfe) {
               }

               if (encoder_class != null) {
                  try {
                     encoder = (Encoder) encoder_class.newInstance();
                  }
                  catch (InstantiationException ie) {
                  }
                  catch (IllegalAccessException iae) {
                  }
               }
            }
            return encoder;
         }

      } // End of Encoding inner class

}
