package org.tn5250j.framework.tn5250;

import org.junit.Before;
import org.junit.Test;
import org.tn5250j.Session5250;
import org.tn5250j.SessionConfig;
import org.tn5250j.event.AidListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.tn5250j.TN5250jConstants.AID_ENTER;
import static org.tn5250j.TN5250jConstants.AID_PF3;

/**
 * No socket is connected, so writeGDS returns early (bout == null) and
 * sendAidKey completes without host I/O.
 */
public class AidListenerTest {

  private static final String SESSION_NAME = "aid-listener-test";
  private static final int ATTR_NORMAL = 32;
  private static final int NO_FLAGS = 0;
  // 0-based, as ScreenFields.setField expects
  private static final int FIELD_ROW = 4;
  private static final int FIELD_COL = 9;
  private static final int FIELD_LENGTH = 10;

  private Screen5250 screen;
  private tnvt vt;
  private ScreenField inputField;

  @Before
  public void setUp() {
    Session5250 session = new Session5250(new Properties(), null, SESSION_NAME,
        new SessionConfig(null, SESSION_NAME));
    screen = session.getScreen();
    vt = new tnvt(session, screen, true, false);
    inputField = screen.getScreenFields().setField(ATTR_NORMAL, FIELD_ROW, FIELD_COL,
        FIELD_LENGTH, NO_FLAGS, NO_FLAGS, NO_FLAGS, NO_FLAGS);
  }

  @Test
  public void listener_receives_aid_passed_to_sendAidKey() {
    final List<Integer> received = new ArrayList<Integer>();
    vt.addAidListener(new AidListener() {
      @Override
      public void onAidKey(int aid) {
        received.add(aid);
      }
    });

    vt.sendAidKey(AID_ENTER);

    assertEquals(1, received.size());
    assertEquals(AID_ENTER, (int) received.get(0));
  }

  @Test
  public void listener_sees_user_input_state_before_send() {
    inputField.setString("HELLO");
    screen.setCursor(FIELD_ROW + 1, FIELD_COL + 3);

    final boolean[] modifiedInCallback = new boolean[1];
    final String[] textInCallback = new String[1];
    final int[] cursorInCallback = new int[2];
    final boolean[] lockedInCallback = new boolean[1];
    vt.addAidListener(new AidListener() {
      @Override
      public void onAidKey(int aid) {
        modifiedInCallback[0] = inputField.isModified();
        textInCallback[0] = inputField.getString();
        cursorInCallback[0] = screen.getCurrentRow();
        cursorInCallback[1] = screen.getCurrentCol();
        lockedInCallback[0] = screen.getOIA().isKeyBoardLocked();
      }
    });
    screen.getOIA().setKeyBoardLocked(false);

    vt.sendAidKey(AID_ENTER);

    assertTrue(modifiedInCallback[0]);
    assertEquals("HELLO     ", textInCallback[0]);
    assertEquals(FIELD_ROW + 1, cursorInCallback[0]);
    assertEquals(FIELD_COL + 3, cursorInCallback[1]);
    assertFalse("listener must fire before sendAidKey locks the keyboard", lockedInCallback[0]);
  }

  @Test
  public void failing_listener_does_not_block_other_listeners_or_the_send() {
    final List<Integer> received = new ArrayList<Integer>();
    vt.addAidListener(new AidListener() {
      @Override
      public void onAidKey(int aid) {
        throw new IllegalStateException("boom");
      }
    });
    vt.addAidListener(new AidListener() {
      @Override
      public void onAidKey(int aid) {
        received.add(aid);
      }
    });

    assertTrue(vt.sendAidKey(AID_ENTER));
    assertEquals(1, received.size());
    assertTrue("send proceeded and locked the keyboard", screen.getOIA().isKeyBoardLocked());
  }

  @Test
  public void removed_listener_is_not_notified() {
    final List<Integer> received = new ArrayList<Integer>();
    AidListener listener = new AidListener() {
      @Override
      public void onAidKey(int aid) {
        received.add(aid);
      }
    };
    vt.addAidListener(listener);
    vt.removeAidListener(listener);

    vt.sendAidKey(AID_PF3);

    assertTrue(received.isEmpty());
  }
}
