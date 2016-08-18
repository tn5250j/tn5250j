package org.tn5250j.settings;
/*
 * Title: KeypadAttributesPanel
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
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

import org.tn5250j.SessionConfig;
import org.tn5250j.tools.LangTool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

import static javax.swing.BorderFactory.createTitledBorder;
import static org.tn5250j.SessionConfig.KEYPAD_FONT_SIZE_DEFAULT_VALUE;

public class KeypadAttributesPanel extends AttributesPanel {

  private static final long serialVersionUID = 1L;
  private JCheckBox keyPadEnable;
  private JTextField fontSize;

  public KeypadAttributesPanel(SessionConfig config) {
    super(config, "KP");
  }

  /**
   * Component initialization
   */
  public void initPanel() throws Exception {

    setLayout(new BorderLayout());
    contentPane = new JPanel();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
    add(contentPane, BorderLayout.NORTH);

    JPanel keypadPanel = new JPanel();
    keypadPanel.setLayout(new BoxLayout(keypadPanel, BoxLayout.Y_AXIS));
    keypadPanel.setBorder(createTitledBorder(LangTool.getString("sa.kpp")));
    keypadPanel.add(createKeypadPanel());
    keypadPanel.add(createFontSizePanel());
    contentPane.add(keypadPanel);
  }

  private JPanel createKeypadPanel() {
    JPanel panel = new JPanel();
    keyPadEnable = new JCheckBox(LangTool.getString("sa.kpCheck"));
    keyPadEnable.setSelected(SessionConfig.YES.equals(getStringProperty(SessionConfig.KEYPAD_ENABLED)));
    keyPadEnable.addActionListener(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        updateFontSizeTextEnabled();
      }
    });
    panel.add(keyPadEnable);
    return panel;
  }

  private void updateFontSizeTextEnabled() {
    fontSize.setEnabled(keyPadEnable.isSelected());
  }

  private JPanel createFontSizePanel() {
    JPanel fontSizePanel = new JPanel();
    fontSize = new JTextField(Float.toString(KEYPAD_FONT_SIZE_DEFAULT_VALUE), 5);
    if (getStringProperty(SessionConfig.KEYPAD_FONT_SIZE).length() != 0) {
      fontSize.setText(getStringProperty(SessionConfig.KEYPAD_FONT_SIZE));
    }
    fontSizePanel.add(new JLabel(LangTool.getString("spool.labelOptsFontSize")));
    fontSizePanel.add(fontSize);
    return fontSizePanel;
  }

  @Override
  public void save() {

  }

  @Override
  public void applyAttributes() {
    applyKeypadEnabled();
    applyFontSize();
  }

  private void applyKeypadEnabled() {
    final String newValue = keyPadEnable.isSelected() ? SessionConfig.YES : SessionConfig.NO;
    changes.firePropertyChange(this, SessionConfig.KEYPAD_ENABLED, getStringProperty(SessionConfig.KEYPAD_ENABLED), newValue);
    setProperty(SessionConfig.KEYPAD_ENABLED, newValue);
  }

  private void applyFontSize() {
    final String newValue = ensureValidFloatAsString(fontSize.getText());
    changes.firePropertyChange(this, SessionConfig.KEYPAD_FONT_SIZE, getStringProperty(SessionConfig.KEYPAD_FONT_SIZE), newValue);
    setProperty(SessionConfig.KEYPAD_FONT_SIZE, newValue);
  }

  private String ensureValidFloatAsString(String value) {
    if (value != null) {
      value = value.trim();
      float v = Float.parseFloat(value);
      return Float.toString(v);
    }
    return Float.toString(KEYPAD_FONT_SIZE_DEFAULT_VALUE);
  }
}