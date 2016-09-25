package org.tn5250j.sessionsettings;
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
import org.tn5250j.keyboard.KeypadMnemonic;
import org.tn5250j.keyboard.KeypadMnemonicResolver;
import org.tn5250j.tools.LangTool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

import static javax.swing.BorderFactory.createTitledBorder;
import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;
import static org.tn5250j.SessionConfig.KEYPAD_FONT_SIZE_DEFAULT_VALUE;
import static org.tn5250j.SessionConfig.YES;

class KeypadAttributesPanel extends AttributesPanel {

  private static final long serialVersionUID = 1L;
  private static final int VISIBLE_ROW_COUNT = 15;

  private JCheckBox keyPadEnable;
  private JTextField fontSize;
  private JList availableButtonsList;
  private JList configuredButtonsList;

  KeypadAttributesPanel(SessionConfig config) {
    super(config, "KP");
  }

  @Override
  public void applyAttributes() {
    applyKeypadEnabled();
    applyFontSize();
  }

  /**
   * Component initialization
   */
  public void initPanel() throws Exception {
    setLayout(new BorderLayout());
    contentPane = new JPanel();
    contentPane.setLayout(new BoxLayout(contentPane, Y_AXIS));
    add(contentPane, BorderLayout.NORTH);

    contentPane.add(createEnableKeypadAndFontSizePanel());
    contentPane.add(createCustomButtonConfigurationPanel());
  }

  private JPanel createEnableKeypadAndFontSizePanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, Y_AXIS));
    panel.setBorder(createTitledBorder(LangTool.getString("sa.kpp")));
    panel.add(createEnableKeypadCheckboxPanel());
    panel.add(createFontSizePanel());
    return panel;
  }

  private JPanel createEnableKeypadCheckboxPanel() {
    JPanel panel = new JPanel();
    keyPadEnable = new JCheckBox(LangTool.getString("sa.kpCheck"));
    keyPadEnable.setSelected(YES.equals(getStringProperty(SessionConfig.KEYPAD_ENABLED)));
    keyPadEnable.addActionListener(new UpdateFontSizeTextEnabledAction());
    panel.add(keyPadEnable);
    return panel;
  }

  private JPanel createCustomButtonConfigurationPanel() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, X_AXIS));
    panel.setBorder(createTitledBorder("Custom Buttons"));
    panel.add(createAvailableButtonsList());
    panel.add(createListMoveButtons());
    panel.add(createConfiguredButtonsList());
    return panel;
  }

  private JPanel createListMoveButtons() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, Y_AXIS));
    panel.add(createMoveButton(">", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        moveFromTo(availableButtonsList, configuredButtonsList);
      }
    }));
    panel.add(createMoveButton("<", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        moveFromTo(configuredButtonsList, availableButtonsList);
      }
    }));
    return panel;
  }

  private JButton createMoveButton(String text, AbstractAction abstractAction) {
    JButton button = new JButton(text);
    button.addActionListener(abstractAction);
    return button;
  }

  private void moveFromTo(JList sourceList, JList destinationList) {
    int[] selectedIndices = sourceList.getSelectedIndices();
    DefaultListModel sourceModel = (DefaultListModel) sourceList.getModel();
    DefaultListModel destinationModel = (DefaultListModel) destinationList.getModel();
    for (int selectedIndex : selectedIndices) {
      Object element = sourceModel.getElementAt(selectedIndex);
      destinationModel.addElement(element);
    }
    safeDeleteByIndex(sourceModel, selectedIndices);
  }

  private JComponent createAvailableButtonsList() {
    DefaultListModel listModel = new DefaultListModel();
    for (KeypadMnemonic mnemonic : KeypadMnemonic.values()) {
      listModel.addElement(mnemonic);
    }
    availableButtonsList = new JList(listModel);
    availableButtonsList.setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
    availableButtonsList.setLayoutOrientation(JList.VERTICAL);
    availableButtonsList.setVisibleRowCount(VISIBLE_ROW_COUNT);
    availableButtonsList.setCellRenderer(new KeypadMnemonicListCellRenderer());
    return createScrollPaneForList(availableButtonsList);
  }

  private JComponent createConfiguredButtonsList() {
    DefaultListModel listModel = new DefaultListModel();
    configuredButtonsList = new JList(listModel);
    configuredButtonsList.setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
    configuredButtonsList.setLayoutOrientation(JList.VERTICAL);
    configuredButtonsList.setVisibleRowCount(VISIBLE_ROW_COUNT);
    configuredButtonsList.setCellRenderer(new KeypadMnemonicListCellRenderer());
    return createScrollPaneForList(configuredButtonsList);
  }

  private JScrollPane createScrollPaneForList(JList list) {
    JScrollPane scrollPane = new JScrollPane(list);
    scrollPane.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_ALWAYS);
    scrollPane.setPreferredSize(list.getPreferredScrollableViewportSize());
    scrollPane.setVisible(true);
    return scrollPane;
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

  private void applyKeypadEnabled() {
    final String newValue = keyPadEnable.isSelected() ? YES : SessionConfig.NO;
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

  private static void safeDeleteByIndex(DefaultListModel listModel, int[] indexesToBeDeleted) {
    Arrays.sort(indexesToBeDeleted);
    for (int i = indexesToBeDeleted.length - 1; i >= 0; i--) {
      listModel.remove(indexesToBeDeleted[i]);
    }
  }

  private class UpdateFontSizeTextEnabledAction extends AbstractAction {
    @Override
    public void actionPerformed(ActionEvent e) {
      fontSize.setEnabled(keyPadEnable.isSelected());
    }
  }

  private static class KeypadMnemonicListCellRenderer implements ListCellRenderer {
    private DefaultListCellRenderer delegateRenderer = new DefaultListCellRenderer();
    private KeypadMnemonicResolver keypadMnemonicResolver = new KeypadMnemonicResolver();

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      String description = keypadMnemonicResolver.getDescription((KeypadMnemonic) value);
      return delegateRenderer.getListCellRendererComponent(list, description, index, isSelected, cellHasFocus);
    }
  }


}