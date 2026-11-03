package org.tn5250j.keyboard;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeyMnemonicResolverTest {

    private KeyMnemonicResolver resolver;

    @Before
    public void setUp() throws Exception {
        resolver = new KeyMnemonicResolver();
    }

    @Test
    public void if_mnemonic_not_fount_return_ZERO() throws Exception {
        int value = resolver.findMnemonicValue("illegal value");
        assertEquals(0, value);
    }

    @Test
    public void search_is_null_safe() throws Exception {
        int value = resolver.findMnemonicValue(null);
        assertEquals(0, value);
    }
}
