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
import org.tn5250j.keyboard.KeyMnemonic;
import org.tn5250j.keyboard.KeyMnemonicResolver;
import org.tn5250j.tools.LangTool;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;

import static javax.swing.BorderFactory.createTitledBorder;
import static javax.swing.BoxLayout.X_AXIS;
import static javax.swing.BoxLayout.Y_AXIS;
import static javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
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
    changes.setKeypadMnemonicsAndFireChangeEvent(getConfiguredKeypadMnemonics());
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
    keyPadEnable.setSelected(YES.equals(getStringProperty(SessionConfig.CONFIG_KEYPAD_ENABLED)));
    keyPadEnable.addActionListener(new UpdateFontSizeTextEnabledAction());
    panel.add(keyPadEnable);
    return panel;
  }

  private JPanel createCustomButtonConfigurationPanel() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, Y_AXIS));
    mainPanel.setBorder(createTitledBorder(LangTool.getString("sa.kpVisibleButtons")));

    JPanel listPanel = new JPanel();
    listPanel.setLayout(new BoxLayout(listPanel, X_AXIS));
    listPanel.add(createAvailableButtonsList());
    listPanel.add(createListMoveButtons());
    listPanel.add(createConfiguredButtonsList());
    listPanel.add(createListOrderButtons());

    mainPanel.add(listPanel);
    JButton resetButton = new JButton(LangTool.getString("sa.kpResetDefaults"));
    resetButton.addActionListener(new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        resetModelsToDefaultValues();
      }
    });
    mainPanel.add(resetButton);
    return mainPanel;
  }

  private void resetModelsToDefaultValues() {
    DefaultListModel configuredModel = (DefaultListModel) configuredButtonsList.getModel();
    configuredModel.clear();
    for (KeyMnemonic mnemonic : changes.getConfig().getDefaultKeypadMnemonics()) {
      configuredModel.addElement(mnemonic);
    }

    DefaultListModel availableModel = (DefaultListModel) availableButtonsList.getModel();
    availableModel.clear();
    for (KeyMnemonic mnemonic : getAvailableAndNotYetConfiguredMnemonics(changes.getConfig().getDefaultKeypadMnemonics())) {
      availableModel.addElement(mnemonic);
    }
  }

  private Component createListMoveButtons() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, Y_AXIS));
    panel.add(createButton(">", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        moveFromTo(availableButtonsList, configuredButtonsList);
      }
    }));
    panel.add(createButton("<", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        moveFromTo(configuredButtonsList, availableButtonsList);
      }
    }));
    return panel;
  }

  private Component createListOrderButtons() {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, Y_AXIS));
    panel.add(createButton(UIManager.getIcon("Table.ascendingSortIcon"), new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        moveUp();
      }
    }));
    panel.add(createButton(UIManager.getIcon("Table.descendingSortIcon"), new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        moveDown();
      }
    }));
    return panel;
  }

  private void moveUp() {
    int selectedIndex = configuredButtonsList.getSelectedIndex();
    if (selectedIndex > 0) {
      DefaultListModel model = (DefaultListModel) configuredButtonsList.getModel();
      Object element = model.remove(selectedIndex);
      model.insertElementAt(element, selectedIndex - 1);
      configuredButtonsList.setSelectedIndex(selectedIndex - 1);
    }
  }

  private void moveDown() {
    int selectedIndex = configuredButtonsList.getSelectedIndex();
    DefaultListModel model = (DefaultListModel) configuredButtonsList.getModel();
    if (selectedIndex < model.size() - 1) {
      Object element = model.remove(selectedIndex);
      model.insertElementAt(element, selectedIndex + 1);
      configuredButtonsList.setSelectedIndex(selectedIndex + 1);
    }
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
    KeyMnemonic[] configuredMnemonics = this.changes.getConfig().getKeypadMnemonics();
    KeyMnemonic[] availableMnemonics = getAvailableAndNotYetConfiguredMnemonics(configuredMnemonics);
    availableButtonsList = new JList(createListModelMnemonics(availableMnemonics));
    availableButtonsList.setSelectionMode(MULTIPLE_INTERVAL_SELECTION);
    availableButtonsList.setLayoutOrientation(JList.VERTICAL);
    availableButtonsList.setVisibleRowCount(VISIBLE_ROW_COUNT);
    availableButtonsList.setCellRenderer(new KeypadMnemonicListCellRenderer());
    return createScrollPaneForList(availableButtonsList);
  }

  private KeyMnemonic[] getAvailableAndNotYetConfiguredMnemonics(KeyMnemonic[] excludedMnemonics) {
    java.util.List<KeyMnemonic> result = new ArrayList<KeyMnemonic>();
    Set<KeyMnemonic> alreadyConfigured = new HashSet<KeyMnemonic>();
    Collections.addAll(alreadyConfigured, excludedMnemonics);
    for (KeyMnemonic mnemonic : KeyMnemonic.values()) {
      if (!alreadyConfigured.contains(mnemonic)) result.add(mnemonic);
    }
    Collections.sort(result, new KeypadMnemonicDescriptionComparator());
    return result.toArray(new KeyMnemonic[result.size()]);
  }

  private KeyMnemonic[] getConfiguredKeypadMnemonics() {
    DefaultListModel model = (DefaultListModel) configuredButtonsList.getModel();
    KeyMnemonic[] newValue = new KeyMnemonic[model.size()];
    Enumeration<?> elements = model.elements();
    int counter = 0;
    while (elements.hasMoreElements()) {
      KeyMnemonic mnemonic = (KeyMnemonic) elements.nextElement();
      newValue[counter++] = mnemonic;
    }
    return newValue;
  }

  private JComponent createConfiguredButtonsList() {
    configuredButtonsList = new JList(createListModelMnemonics(this.changes.getConfig().getKeypadMnemonics()));
    configuredButtonsList.setSelectionMode(SINGLE_SELECTION);
    configuredButtonsList.setLayoutOrientation(JList.VERTICAL);
    configuredButtonsList.setVisibleRowCount(VISIBLE_ROW_COUNT);
    configuredButtonsList.setCellRenderer(new KeypadMnemonicListCellRenderer());
    return createScrollPaneForList(configuredButtonsList);
  }

  private DefaultListModel createListModelMnemonics(KeyMnemonic[] keyMnemonics) {
    DefaultListModel model = new DefaultListModel();
    for (KeyMnemonic mnemonic : keyMnemonics) {
      model.addElement(mnemonic);
    }
    return model;
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
    if (getStringProperty(SessionConfig.CONFIG_KEYPAD_FONT_SIZE).length() != 0) {
      fontSize.setText(getStringProperty(SessionConfig.CONFIG_KEYPAD_FONT_SIZE));
    }
    fontSizePanel.add(new JLabel(LangTool.getString("spool.labelOptsFontSize")));
    fontSizePanel.add(fontSize);
    return fontSizePanel;
  }

  private void applyKeypadEnabled() {
    final String newValueEnabled = keyPadEnable.isSelected() ? YES : SessionConfig.NO;
    changes.firePropertyChange(this, SessionConfig.CONFIG_KEYPAD_ENABLED, getStringProperty(SessionConfig.CONFIG_KEYPAD_ENABLED), newValueEnabled);
    setProperty(SessionConfig.CONFIG_KEYPAD_ENABLED, newValueEnabled);
  }

  private void applyFontSize() {
    final String newValue = ensureValidFloatAsString(fontSize.getText());
    changes.firePropertyChange(this, SessionConfig.CONFIG_KEYPAD_FONT_SIZE, getStringProperty(SessionConfig.CONFIG_KEYPAD_FONT_SIZE), newValue);
    setProperty(SessionConfig.CONFIG_KEYPAD_FONT_SIZE, newValue);
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

  private static JButton createButton(Icon icon, AbstractAction action) {
    JButton button = new JButton(icon);
    button.addActionListener(action);
    return button;
  }

  private static JButton createButton(String text, AbstractAction action) {
    JButton button = new JButton(text);
    button.addActionListener(action);
    return button;
  }

  private static class KeypadMnemonicDescriptionComparator implements Comparator<KeyMnemonic> {
    private final KeyMnemonicResolver resolver = new KeyMnemonicResolver();

    @Override
    public int compare(KeyMnemonic mnemonic1, KeyMnemonic mnemonic2) {
      return resolver.getDescription(mnemonic1).compareToIgnoreCase(resolver.getDescription(mnemonic2));
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
    private KeyMnemonicResolver keyMnemonicResolver = new KeyMnemonicResolver();

    @Override
    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      String description = keyMnemonicResolver.getDescription((KeyMnemonic) value);
      return delegateRenderer.getListCellRendererComponent(list, description, index, isSelected, cellHasFocus);
    }
  }

}
