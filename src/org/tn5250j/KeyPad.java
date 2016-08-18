package org.tn5250j;
/*
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.SwingUtilities.layoutCompoundLabel;
import static org.tn5250j.TN5250jConstants.*;
import static org.tn5250j.tools.LangTool.getString;

class KeyPad extends JPanel {

  private static final long serialVersionUID = -7460283401326716314L;
  private static final int MIN_SIZE = 3;
  private static final int NO_OF_BUTTONS = 30;

  private final JButton[] buttons = new JButton[NO_OF_BUTTONS];
  private final Rectangle textRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final SessionConfig.SessionConfiguration sessionConfig;

  private JPanel keyPadTop;
  private JPanel keyPadBottom;
  private KeyPadMode currentKeyPadMode = KeyPadMode.ONE;

  KeyPad(SessionConfig.SessionConfiguration sessionConfiguration) {
    this.sessionConfig = sessionConfiguration;
    jbInit();
  }

  private void jbInit() {

    final GridLayout gridLayout = new GridLayout(1, NO_OF_BUTTONS / 2, 0, 0);
    keyPadTop = new JPanel(gridLayout);
    keyPadBottom = new JPanel(gridLayout);

    final Insets noMargin = new Insets(0, 0, 0, 0);
    for (int i = 0; i < NO_OF_BUTTONS; i++) {
      buttons[i] = new JButton();
      buttons[i].setMargin(noMargin);
    }

    switchKeypadMode(currentKeyPadMode);
    addButtonsTop();
    addButtonsBottom();

    this.setLayout(new BorderLayout());
    this.setBorder(createEmptyBorder());
    this.add(keyPadTop, BorderLayout.NORTH);
    this.add(keyPadBottom, BorderLayout.SOUTH);
  }

  void addActionListener(ActionListener actionlistener) {
    for (int x = 0; x < NO_OF_BUTTONS; x++) {
      buttons[x].addActionListener(actionlistener);
    }
  }

  void nextPad() {
    if (currentKeyPadMode == KeyPadMode.ONE)
      switchKeypadMode(KeyPadMode.TWO);
    else
      switchKeypadMode(KeyPadMode.ONE);
  }

  private void switchKeypadMode(KeyPadMode newKeyPadMode) {
    currentKeyPadMode = newKeyPadMode;
    buttons[0].setText(getString("KP_F1", "PF1"));
    buttons[0].setActionCommand(MNEMONIC_PF1);
    buttons[1].setText(getString("KP_F2", "PF2"));
    buttons[1].setActionCommand(MNEMONIC_PF2);
    buttons[2].setText(getString("KP_F3", "PF3"));
    buttons[2].setActionCommand(MNEMONIC_PF3);
    buttons[3].setText(getString("KP_F4", "PF4"));
    buttons[3].setActionCommand(MNEMONIC_PF4);
    buttons[4].setText(getString("KP_F5", "PF5"));
    buttons[4].setActionCommand(MNEMONIC_PF5);
    buttons[5].setText(getString("KP_F6", "PF6"));
    buttons[5].setActionCommand(MNEMONIC_PF6);
    buttons[6].setText(getString("KP_F7", "PF7"));
    buttons[6].setActionCommand(MNEMONIC_PF7);
    buttons[7].setText(getString("KP_F8", "PF8"));
    buttons[7].setActionCommand(MNEMONIC_PF8);
    buttons[8].setText(getString("KP_F9", "PF9"));
    buttons[8].setActionCommand(MNEMONIC_PF9);
    buttons[9].setText(getString("KP_F10", "PF10"));
    buttons[9].setActionCommand(MNEMONIC_PF10);
    buttons[10].setText(getString("KP_F11", "PF11"));
    buttons[10].setActionCommand(MNEMONIC_PF11);
    buttons[11].setText(getString("KP_F12", "PF12"));
    buttons[11].setActionCommand(MNEMONIC_PF12);
    buttons[12].setText(getString("KP_ENTER", "Enter"));
    buttons[12].setActionCommand(MNEMONIC_ENTER);
    buttons[13].setText(getString("KP_PGUP", "PgUp"));
    buttons[13].setActionCommand(MNEMONIC_PAGE_UP);
    if (newKeyPadMode == KeyPadMode.ONE) {
      buttons[14].setText(getString("KP_CLEAR", "Clear"));
      buttons[14].setActionCommand(MNEMONIC_CLEAR);
    } else if (newKeyPadMode == KeyPadMode.TWO) {
      buttons[14].setText(getString("KP_HELP", "Help"));
      buttons[14].setActionCommand(MNEMONIC_HELP);
    } else {
      throw new IllegalStateException("Not implemented KeyPadMode: " + newKeyPadMode);
    }
    buttons[15].setText(getString("KP_F13", "PF13"));
    buttons[15].setActionCommand(MNEMONIC_PF13);
    buttons[16].setText(getString("KP_F14", "PF14"));
    buttons[16].setActionCommand(MNEMONIC_PF14);
    buttons[17].setText(getString("KP_F15", "PF15"));
    buttons[17].setActionCommand(MNEMONIC_PF15);
    buttons[18].setText(getString("KP_F16", "PF16"));
    buttons[18].setActionCommand(MNEMONIC_PF16);
    buttons[19].setText(getString("KP_F17", "PF17"));
    buttons[19].setActionCommand(MNEMONIC_PF17);
    buttons[20].setText(getString("KP_F18", "PF18"));
    buttons[20].setActionCommand(MNEMONIC_PF18);
    buttons[21].setText(getString("KP_F19", "PF19"));
    buttons[21].setActionCommand(MNEMONIC_PF19);
    buttons[22].setText(getString("KP_F20", "PF20"));
    buttons[22].setActionCommand(MNEMONIC_PF20);
    buttons[23].setText(getString("KP_F21", "PF21"));
    buttons[23].setActionCommand(MNEMONIC_PF21);
    buttons[24].setText(getString("KP_F22", "PF22"));
    buttons[24].setActionCommand(MNEMONIC_PF22);
    buttons[25].setText(getString("KP_F23", "PF23"));
    buttons[25].setActionCommand(MNEMONIC_PF23);
    buttons[26].setText(getString("KP_F24", "PF24"));
    buttons[26].setActionCommand(MNEMONIC_PF24);
    buttons[27].setText(getString("KP_SR", "SysReq"));
    buttons[27].setActionCommand(MNEMONIC_SYSREQ);
    buttons[28].setText(getString("KP_PGDN", "PgDn"));
    buttons[28].setActionCommand(MNEMONIC_PAGE_DOWN);
    buttons[29].setText(getString("KP_NXTPAD", "Next Pad"));
    buttons[29].setActionCommand("NXTPAD");
  }

  private void addButtonsTop() {
    for (int x = 0; x < NO_OF_BUTTONS / 2; x++) {
      keyPadTop.add(buttons[x]);
    }
  }

  private void addButtonsBottom() {
    for (int x = NO_OF_BUTTONS / 2; x < NO_OF_BUTTONS; x++) {
      keyPadBottom.add(buttons[x]);
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    final JButton referenceButton = buttons[NO_OF_BUTTONS - 1];
    Font buttonFont = referenceButton.getFont();
    float fs = sessionConfig.getKeypadFontSize();
    buttonFont = buttonFont.deriveFont(fs);

    FontMetrics fm = referenceButton.getFontMetrics(buttonFont);
    Rectangle viewRect = referenceButton.getVisibleRect();

    Insets i = referenceButton.getInsets();

    // we now subtract the insets which include the border insets as well
    viewRect.x = i.left;
    viewRect.y = i.top;
    viewRect.width = referenceButton.getWidth() - (i.right + viewRect.x);
    viewRect.height = referenceButton.getHeight() - (i.bottom + viewRect.y);

    // initialize the textRect and iconRect to 0 so they will be calculated
    textRect.x = textRect.y = textRect.width = textRect.height = 0;
    iconRect.x = iconRect.y = iconRect.width = iconRect.height = 0;

    // now compute the text that will be displayed until we run do not get
    //    elipses or we go passes the minimum of our text size that we want
    final int textIconGap = 0;
    final Icon icon = null;
    String largestText = findLargestText();
    while (layoutCompoundLabel(fm, largestText, icon,
        referenceButton.getVerticalAlignment(),
        referenceButton.getHorizontalAlignment(),
        referenceButton.getVerticalTextPosition(),
        referenceButton.getHorizontalTextPosition(),
        viewRect,
        iconRect,
        textRect,
        textIconGap).endsWith("...")
        && fs > (MIN_SIZE - 1)) {
      buttonFont = buttonFont.deriveFont(--fs);
      fm = referenceButton.getFontMetrics(buttonFont);

    }

    if (fs >= MIN_SIZE) {
      for (int x = 0; x < NO_OF_BUTTONS; x++) {
        buttons[x].setFont(buttonFont);
      }
    }
  }

  private String findLargestText() {
    String text = "";
    for (JButton button : buttons) {
      if (button.getText().length() > text.length()) {
        text = button.getText();
      }
    }
    return text;
  }

  private enum KeyPadMode {
    ONE,
    TWO
  }
}
