/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,202,2003
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.4
 * <p>
 * Description:
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
package org.tn5250j.tools.encoder;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.io.IOException;
import java.io.OutputStream;

import org.tn5250j.gui.UiUtils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;

/**
 * This is the base class for encoding a component to a stream file.
 */
public abstract class AbstractImageEncoder implements Encoder {

    protected Image img = null;
    protected OutputStream ofile = null;

    public void encode(final Image image, final OutputStream os) throws IOException, EncoderException {
        img = image;
        ofile = os;
        saveImage();
    }

    @Override
    public void encode(final Object component, final OutputStream os) throws IOException, EncoderException {
        if (component instanceof  Component) {
            encodeComponent((Component) component, os);
        } else if (component instanceof Node) {
            encodeNode((Node) component, os);
        } else {
            throw new EncoderException("Usupported component type: " + component.getClass().getName());
        }
    }
    public void encodeComponent(final Component component, final OutputStream os) throws IOException, EncoderException {
        encode(snapshot(component), os);
    }
    public void encodeNode(final Node node, final OutputStream os) throws IOException, EncoderException {
        final WritableImage image = UiUtils.runInFxAndWait(
                () -> node.snapshot(new SnapshotParameters(), null));
        encode(SwingFXUtils.fromFXImage(image, null), os);
    }

    public static Image snapshot(final Component component) {
        final Image img = component.createImage(component.getSize().width,
                component.getSize().height);
        if (img != null) {
            final Graphics igc = img.getGraphics();
            //Gotta set the clip, or else paint throws an exception
            igc.setClip(0, 0, component.getSize().width,
                    component.getSize().height);
            component.paint(igc);
        }
        return img;
    }

    public abstract void saveImage() throws IOException, EncoderException;

    public byte createByte(final int b7, final int b6, final int b5, final int b4, final int b3, final int b2, final int b1, final int b0) {
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

    public static byte byteFromInt(final int value) {
        return ((byte) (value & 255));
    }

    public static byte[] bytesFromLong(final long value) {
        final byte[] buf = new byte[4];
        buf[0] = ((byte) ((value >> 24) & 255));
        buf[1] = ((byte) ((value >> 16) & 255));
        buf[2] = ((byte) ((value >> 8) & 255));
        buf[3] = ((byte) ((value) & 255));
        return buf;
    }

    public static byte byteFromChar(final char ochar) {
        final int temp = ochar;
        byte bits = 0;

        final int curpos = 0;
        for (int i = 0; i <= 7; i++) {
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
    public byte compressColor(final int clr) {
        return compressColor((clr >> 16) & 255, (clr >> 8) & 255, clr & 255);
    }

    /**
     * Compress the given color into one 8 bit representation.
     * @param red value of the red portion of the color
     * @param green value of the green portion of the color
     * @param blue value of the blue portion of the color
     * @return color compressed into 8-bit representation
     */
    public byte compressColor(final int red, final int green, final int blue) {
        // take 3 most significatnt bits of red, 3 most significant bits of
        // green and 2 most significant bits of blue to form 8-bit compression
        // of color values

        return (byte) ((red & 224) | ((green >> 3) & 28) | ((blue >> 6) & 3));
    }

    protected void error(final String msg) throws EncoderException {
        throw new EncoderException(msg);
    }

}
