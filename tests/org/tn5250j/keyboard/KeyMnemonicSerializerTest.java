/*
 * Title: KeypadMnemonicSerializerTest.java
 * Copyright:   Copyright (c) 2016
 * Company:
 *
 * @author Martin W. Kirst
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
package org.tn5250j.keyboard;

import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.*;
import static org.tn5250j.keyboard.KeyMnemonic.*;

public class KeyMnemonicSerializerTest {

    private KeyMnemonicSerializer serializer;

    @Before
    public void setUp() throws Exception {
        serializer = new KeyMnemonicSerializer();
    }

    @Test
    public void mnemonics_are_serialized_as_comma_separated_string() throws Exception {
        String actual = serializer.serialize(new KeyMnemonic[]{CLEAR, ATTN, COPY});

        assertEquals("[clear],[attn],[copy]", actual);
    }

    @Test
    public void serializer_is_null_safe() throws Exception {
        serializer.serialize(null);

        // assert no exception
    }

    @Test
    public void serializer_is_empty_array_safe() throws Exception {
        String actual = serializer.serialize(new KeyMnemonic[0]);

        assertEquals("", actual);
    }

    @Test
    public void mnemonics_are_deserialized_from_comma_separated_string() throws Exception {
        KeyMnemonic[] actual = serializer.deserialize("[clear], [attn] ,[copy]");

        assertTrue(actual.length == 3);
        assertEquals(actual[0], CLEAR);
        assertEquals(actual[1], ATTN);
        assertEquals(actual[2], COPY);
    }

    @Test
    public void deserializer_is_null_safe() throws Exception {
        KeyMnemonic[] actual = serializer.deserialize(null);

        assertNotNull(actual);
        assertTrue(actual.length == 0);
    }

    @Test
    public void deserializer_is_safe_with_empty_string() throws Exception {
        KeyMnemonic[] actual = serializer.deserialize("");

        assertNotNull(actual);
        assertTrue(actual.length == 0);
    }

    @Test
    public void deserializer_ignores_unknown_values() throws Exception {
        KeyMnemonic[] actual = serializer.deserialize("[clear],,[foobar],[attn]");

        assertTrue(actual.length == 2);
        assertEquals(actual[0], CLEAR);
        assertEquals(actual[1], ATTN);
    }
}
