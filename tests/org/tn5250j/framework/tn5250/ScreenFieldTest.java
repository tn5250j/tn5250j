package org.tn5250j.framework.tn5250;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ScreenFieldTest {

  private static final int FFW1_MDT_BIT = 0x8;
  private static final int NO_FLAGS = 0;
  private static final int ATTR_NORMAL = 32;

  private ScreenField field;

  @Before
  public void setUp() {
    field = new ScreenField(new Screen5250());
  }

  @Test
  public void is_modified_when_ffw1_has_mdt_bit() {
    field.setField(ATTR_NORMAL, 5, 10, 8, FFW1_MDT_BIT, NO_FLAGS, NO_FLAGS, NO_FLAGS);
    assertTrue(field.isModified());
  }

  @Test
  public void is_not_modified_when_ffw1_lacks_mdt_bit() {
    field.setField(ATTR_NORMAL, 5, 10, 8, NO_FLAGS, NO_FLAGS, NO_FLAGS, NO_FLAGS);
    assertFalse(field.isModified());
  }

  @Test
  public void reset_mdt_clears_modified() {
    field.setField(ATTR_NORMAL, 5, 10, 8, FFW1_MDT_BIT, NO_FLAGS, NO_FLAGS, NO_FLAGS);
    field.resetMDT();
    assertFalse(field.isModified());
  }

  @Test
  public void set_mdt_marks_modified() {
    field.setField(ATTR_NORMAL, 5, 10, 8, NO_FLAGS, NO_FLAGS, NO_FLAGS, NO_FLAGS);
    field.setMDT();
    assertTrue(field.isModified());
  }
}
