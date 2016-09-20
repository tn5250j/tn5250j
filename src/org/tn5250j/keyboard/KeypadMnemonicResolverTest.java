package org.tn5250j.keyboard;

import org.junit.Before;
import org.junit.Test;
import org.tn5250j.keyboard.KeypadMnemonicResolver;

import static org.junit.Assert.assertEquals;

public class KeypadMnemonicResolverTest {

  private KeypadMnemonicResolver resolver;

  @Before
  public void setUp() throws Exception {
    resolver = new KeypadMnemonicResolver();
  }

  @Test
  public void if_mnemonic_not_fount_return_ZERO() throws Exception {
    int value = resolver.getMnemonicValue("illegal value");
    assertEquals(0, value);
  }

  @Test
  public void search_is_null_safe() throws Exception {
    int value = resolver.getMnemonicValue(null);
    assertEquals(0, value);
  }
}