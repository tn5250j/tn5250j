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

import org.tn5250j.keyboard.KeyMnemonic;
import org.tn5250j.keyboard.KeyMnemonicResolver;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import java.awt.*;
import java.awt.event.ActionListener;

import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.SwingUtilities.layoutCompoundLabel;
import static org.tn5250j.tools.LangTool.getString;

class KeypadPanel extends JPanel {

  private static final long serialVersionUID = -7460283401326716314L;
  private static final int MIN_SIZE = 3;
  private static final int NO_OF_BUTTONS_PER_ROW = 15;

  private final KeyMnemonicResolver keyMnemonicResolver = new KeyMnemonicResolver();
  private final Rectangle textRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final SessionConfig.SessionConfiguration configuration;

  private JButton[] buttons;

  KeypadPanel(SessionConfig.SessionConfiguration sessionConfiguration) {
    this.configuration = sessionConfiguration;
    setBorder(createEmptyBorder());
    setLayout(new BoxLayout(this, Y_AXIS));
    reInitializeButtons(configuration.getKeypadMnemonics());
  }

  void reInitializeButtons(KeyMnemonic[] keyMnemonics) {
    removeAll();
    final Insets noMargin = new Insets(0, 0, 0, 0);
    final CompoundBorder minimalBorder = createCompoundBorder(createEmptyBorder(), createEmptyBorder(2, 3, 3, 3));
    buttons = new JButton[keyMnemonics.length];
    JPanel buttonPanel = null;
    for (int i = 0; i < buttons.length; i++) {
      final KeyMnemonic mnemonic = keyMnemonics[i];
      buttons[i] = new JButton();
      buttons[i].setMargin(noMargin);
      buttons[i].setBorder(minimalBorder);
      buttons[i].setText(getString("KP_" + mnemonic.name(), keyMnemonicResolver.getDescription(mnemonic)));
      buttons[i].setActionCommand(mnemonic.mnemonic);
      if (i % NO_OF_BUTTONS_PER_ROW == 0 || buttonPanel == null) {
        buttonPanel = new JPanel(new GridLayout(1, NO_OF_BUTTONS_PER_ROW, 0, 0));
        add(buttonPanel);
      }
      buttonPanel.add(buttons[i]);
    }
    addInvisibleButtonsToPreventLayout(buttonPanel);
  }

  private void addInvisibleButtonsToPreventLayout(JPanel bottomPanel) {
    if (buttons.length > NO_OF_BUTTONS_PER_ROW) {
      for (int i = buttons.length % NO_OF_BUTTONS_PER_ROW; i < NO_OF_BUTTONS_PER_ROW; i++) {
        JButton button = new JButton();
        button.setVisible(false);
        bottomPanel.add(button);
      }
    }
  }

  void addActionListener(ActionListener actionlistener) {
    for (JButton button : buttons) {
      button.addActionListener(actionlistener);
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    final JButton referenceButton = buttons[buttons.length - 1];
    Font buttonFont = referenceButton.getFont();
    float fs = configuration.getKeypadFontSize();
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
      for (JButton button : buttons) {
        button.setFont(buttonFont);
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

}
