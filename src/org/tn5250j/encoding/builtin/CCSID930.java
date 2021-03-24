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
package org.tn5250j.encoding.builtin;

import com.ibm.as400.access.ConvTable;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.tn5250j.framework.tn5250.ByteExplainer.*;

/**
 * @author nitram509
 */
public final class CCSID930 implements ICodepageConverter {

    public final static String NAME = "930";
    public final static String DESCR = "Japan Katakana (extended range), DBCS";

    private final AtomicBoolean doubleByteActive = new AtomicBoolean(false);
    private final AtomicBoolean secondByteNeeded = new AtomicBoolean(false);
    private final AtomicInteger lastByte = new AtomicInteger(0);
    private final ConvTable convTable;

    public CCSID930() {
        try {
            convTable = ConvTable.getTable("Cp930");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return NAME;
    }

    public String getDescription() {
        return DESCR;
    }

    @Override
    public ICodepageConverter init() {
        return null;
    }

    public String getEncoding() {
        return NAME;
    }

    @Override
    public byte uni2ebcdic(char index) {
        return 0;
    }

    @Override
    public char ebcdic2uni(int index) {
        if (isShiftIn(index)) {
            doubleByteActive.set(true);
            secondByteNeeded.set(false);
            return 0;
        }
        if (isShiftOut(index)) {
            doubleByteActive.set(false);
            secondByteNeeded.set(false);
            return 0;
        }
        if (isDoubleByteActive()) {
            if (!secondByteNeeded()) {
                lastByte.set(index);
                secondByteNeeded.set(true);
                return 0;
            } else {
                int i = lastByte.get() << 8 | (index & 0xff);
                secondByteNeeded.set(false);
                return convTable.byteArrayToString(new byte[]{SHIFT_IN, lastByte.byteValue(), (byte) (index & 0xff), SHIFT_OUT}, 0, 4).charAt(0);
            }
        }
        return convTable.byteArrayToString(new byte[]{(byte) (index & 0xff)}, 0, 1).charAt(0);
    }

    @Override
    public boolean isDoubleByteActive() {
        return doubleByteActive.get();
    }

    @Override
    public boolean secondByteNeeded() {
        return secondByteNeeded.get();
    }
}
