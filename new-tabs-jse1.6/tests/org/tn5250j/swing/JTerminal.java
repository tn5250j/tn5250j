package org.tn5250j.swing;

import java.awt.AWTEvent;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;

import org.tn5250j.Session5250;
import org.tn5250j.keyboard.KeyboardHandler;

/**
 * For testing purpose
 */
public class JTerminal extends JComponent {

	private static final long serialVersionUID = 1L;
	
	private KeyboardHandler keyHandler;

  public JTerminal(Session5250 session)
  {
    super();
    this.session = session;

    updateUI();
    installInputMap();
  }

  public void updateUI()
  {
     this.setUI(org.tn5250j.swing.ui.BasicTerminalUI.createUI(this));
  }

  public Session5250 getSession()
  {
    return this.session;
  }

  public boolean isFocusCycleRoot()
  {
    return true;
  }

  public boolean isFocusTraversable()
  {
    return true;
  }

  public boolean isFocusable()
  {
    return true;
  }

  public boolean isFocusTraversalKeysEnabled()
  {
    return false;
  }

//  protected void processComponentKeyEvent(KeyEvent e)
//  {
//    if (e.getID() == KeyEvent.KEY_TYPED)
//    {
//      char c = e.getKeyChar();
//      if (c != KeyEvent.CHAR_UNDEFINED)
//      {
//        session.getScreen().sendKeys(""+c, null);
//        e.consume();
//      }
//    }
//  }

   public void processKeyEvent(KeyEvent evt) {

      keyHandler.processKeyEvent(evt);

      if(!evt.isConsumed())
         super.processKeyEvent(evt);
   }

  protected void installInputMap()
  {
      enableEvents(AWTEvent.KEY_EVENT_MASK);
      keyHandler = KeyboardHandler.getKeyboardHandlerInstance(session);
//    InputMap map = this.getInputMap(JComponent.WHEN_FOCUSED);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), OhioConstants.OS_OHIO_MNEMONIC_ENTER);
//
//    //map.put(OS_OHIO_MNEMONIC_CLEAR);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false), OS_OHIO_MNEMONIC_ENTER);
//    //map.put(OS_OHIO_MNEMONIC_HELP);
//
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, false), OS_OHIO_MNEMONIC_PF1);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0, false), OS_OHIO_MNEMONIC_PF2);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0, false), OS_OHIO_MNEMONIC_PF3);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, 0, false), OS_OHIO_MNEMONIC_PF4);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0, false), OS_OHIO_MNEMONIC_PF5);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, 0, false), OS_OHIO_MNEMONIC_PF6);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0, false), OS_OHIO_MNEMONIC_PF7);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0, false), OS_OHIO_MNEMONIC_PF8);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0, false), OS_OHIO_MNEMONIC_PF9);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0, false), OS_OHIO_MNEMONIC_PF10);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0, false), OS_OHIO_MNEMONIC_PF11);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0, false), OS_OHIO_MNEMONIC_PF12);
//
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F1, KeyEvent.SHIFT_MASK, false), OS_OHIO_MNEMONIC_PF13);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F2, KeyEvent.SHIFT_MASK, false), OS_OHIO_MNEMONIC_PF14);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, KeyEvent.SHIFT_MASK, false), OS_OHIO_MNEMONIC_PF15);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.SHIFT_MASK, false), OS_OHIO_MNEMONIC_PF16);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, KeyEvent.SHIFT_MASK, false), OS_OHIO_MNEMONIC_PF17);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F6, KeyEvent.SHIFT_MASK, false), OS_OHIO_MNEMONIC_PF18);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.SHIFT_MASK, false), OS_OHIO_MNEMONIC_PF19);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F8, KeyEvent.SHIFT_MASK, false), OS_OHIO_MNEMONIC_PF20);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, KeyEvent.SHIFT_MASK, false), OS_OHIO_MNEMONIC_PF21);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F10, KeyEvent.SHIFT_MASK, false), OS_OHIO_MNEMONIC_PF22);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, KeyEvent.SHIFT_MASK, false), OS_OHIO_MNEMONIC_PF23);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, KeyEvent.SHIFT_MASK, false), OS_OHIO_MNEMONIC_PF24);
//
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_PRINTSCREEN, 0, false), OS_OHIO_MNEMONIC_PRINT);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), OS_OHIO_MNEMONIC_DOWN);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), OS_OHIO_MNEMONIC_LEFT);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), OS_OHIO_MNEMONIC_RIGHT);
//    map.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), OS_OHIO_MNEMONIC_UP);
  }

  private Session5250 session;
}