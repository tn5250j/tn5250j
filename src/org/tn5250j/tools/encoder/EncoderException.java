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

/**
 * This class is an exception that is raised by Encode or one of it's
 * subclasses.  It may also be subclassed for exceptions thrown by subclasses
 * of Encode. It represents any problem encountered while encoding an image.
 * The message is used to state the type of error.
*/
public class EncoderException extends Exception {
   private static final long serialVersionUID = 1L;

/**
    * Creates an exception with the given message.
    */
   public EncoderException(String msg) {
       super(msg);
   }

}
