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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import static javax.swing.BorderFactory.createCompoundBorder;
import static javax.swing.BorderFactory.createEmptyBorder;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.SwingUtilities.layoutCompoundLabel;
import static org.tn5250j.tools.LangTool.getString;

class KeypadPanel extends JPanel {

  private static final long serialVersionUID = -7460283401326716314L;
  private static final int MIN_FONT_SIZE = 3;
  private static final int NO_OF_BUTTONS_PER_ROW = 15;

  private final KeyMnemonicResolver keyMnemonicResolver = new KeyMnemonicResolver();
  private final SessionConfig.SessionConfiguration configuration;

  private JButton[] buttons;

  KeypadPanel(SessionConfig.SessionConfiguration sessionConfiguration) {
    this.configuration = sessionConfiguration;
    setBorder(createEmptyBorder());
    setLayout(new BoxLayout(this, Y_AXIS));
    addComponentListener(new KeypadPanelComponentListener());
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
      buttons[i] = createButton(mnemonic, noMargin, minimalBorder);
      if (buttonPanel == null || i % NO_OF_BUTTONS_PER_ROW == 0) {
        buttonPanel = new JPanel(new GridLayout(1, NO_OF_BUTTONS_PER_ROW, 0, 0));
        add(buttonPanel);
      }
      buttonPanel.add(buttons[i]);
    }
    addInvisibleButtonsToPreventLayout(buttonPanel);
  }

  void updateButtonFontSize(float fontSize) {
    if (0 == buttons.length) return;

    final JButton referenceButton = buttons[0];
    Font buttonFont = referenceButton.getFont();
    buttonFont = buttonFont.deriveFont(fontSize);

    FontMetrics fm = referenceButton.getFontMetrics(buttonFont);
    Rectangle viewRect = referenceButton.getVisibleRect();

    Insets i = referenceButton.getInsets();

    // we now subtract the insets which include the border insets as well
    viewRect.x = i.left;
    viewRect.y = i.top;
    viewRect.width = referenceButton.getWidth() - (i.right + viewRect.x);
    viewRect.height = referenceButton.getHeight() - (i.bottom + viewRect.y);

    Rectangle textRect = new Rectangle();
    Rectangle iconRect = new Rectangle();

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
        && fontSize > (MIN_FONT_SIZE - 1)) {
      buttonFont = buttonFont.deriveFont(--fontSize);
      fm = referenceButton.getFontMetrics(buttonFont);
    }

    if (fontSize >= MIN_FONT_SIZE) {
      for (JButton button : buttons) {
        button.setFont(buttonFont);
      }
    }
  }

  private JButton createButton(KeyMnemonic mnemonic, Insets noMargin, CompoundBorder minimalBorder) {
    JButton b = new JButton();
    b.setMargin(noMargin);
    b.setBorder(minimalBorder);
    b.setText(getString("KP_" + mnemonic.name(), keyMnemonicResolver.getDescription(mnemonic)));
    b.setActionCommand(mnemonic.mnemonic);
    return b;
  }

  private void addInvisibleButtonsToPreventLayout(JPanel bottomPanel) {
    if (buttons.length > NO_OF_BUTTONS_PER_ROW && buttons.length % NO_OF_BUTTONS_PER_ROW > 0) {
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

  private void maximizeButtonSize() {
    updateButtonFontSize(configuration.getKeypadFontSize());
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

  private class KeypadPanelComponentListener extends ComponentAdapter {
    @Override
    public void componentShown(ComponentEvent e) {
      maximizeButtonSize();
    }

    @Override
    public void componentResized(ComponentEvent e) {
      maximizeButtonSize();
    }
  }
}
