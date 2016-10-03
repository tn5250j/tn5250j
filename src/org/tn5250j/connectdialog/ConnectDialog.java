/*
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.5
 *
 * Description:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.connectdialog;

import org.tn5250j.ExternalProgramConfig;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.gui.JSortTable;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.interfaces.OptionAccessFactory;
import org.tn5250j.keyboard.KeyMnemonicResolver;
import org.tn5250j.tools.AlignLayout;
import org.tn5250j.tools.DESSHA1;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.prefs.Preferences;

import static java.lang.Boolean.TRUE;
import static javax.swing.ListSelectionModel.SINGLE_SELECTION;
import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

public class ConnectDialog extends JDialog implements ActionListener, ChangeListener {

  private static final String USER_PREF_LAST_SESSION = "last_session";

  private static final long serialVersionUID = 1L;

  volatile private static TN5250jLogger LOG = TN5250jLogFactory.getLogger(ConnectDialog.class);

  private final KeyMnemonicResolver keyMnemonicResolver = new KeyMnemonicResolver();

  // panels to be displayed
  private JPanel configOptions = new JPanel();
  private JPanel sessionPanel = new JPanel();
  private JPanel options = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
  private JPanel sessionOpts = new JPanel();
  private JPanel sessionOptPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
  private JPanel emulOptPanel = new JPanel();
  private JPanel accessPanel = new JPanel();
  private JPanel loggingPanel = new JPanel();
  private JPanel externalPanel = new JPanel();
  private JPanel externalOpts = new JPanel();
  private JPanel externalOptPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
  private JPanel aboutPanel = null;

  private JTable sessions = null;
  private JTable externals = null;
  private GridBagConstraints gbc;

  // LoggingPanel Components
  private JRadioButton intOFF = null;
  private JRadioButton intDEBUG = null;
  private JRadioButton intINFO = null;
  private JRadioButton intWARN = null;
  private JRadioButton intERROR = null;
  private JRadioButton intFATAL = null;

  // button needing global access
  private JButton editButton = null;
  private JButton removeButton = null;
  private JButton connectButton = null;

  private JButton cAddButton = null;
  private JButton cEditButton = null;
  private JButton cRemoveButton = null;


  // custom table model
  private SessionsTableModel ctm = null;

  private CustomizedTableModel etm = null;

  // ListSelectionModel of our custom table.
  private ListSelectionModel rowSM = null;
  private ListSelectionModel rowSM2 = null;
  // Properties
  private Properties properties = null;
  private Properties externalProgramConfig = null;

  private JCheckBox hideTabBar = null;
  private JCheckBox showMe = null;
  private JCheckBox lastView = null;

  // create some reusable borders and layouts
  private BorderLayout borderLayout = new BorderLayout();

  private MultiSelectListComponent accessOptions;
  // password protection field for access to options list
  private JPasswordField password;
  private JButton setPassButton;

  // Selection value for connection
  private String connectKey = null;
  private JRadioButton intConsole;
  private JRadioButton intFile;
  private JRadioButton intBoth;

  private JTextField browser;
  private JTextField mailer;

  public ConnectDialog(Frame frame, String title, Properties prop) {

    super(frame, title, true);

    properties = ConfigureFactory.getInstance().getProperties(
        ConfigureFactory.SESSIONS);
    externalProgramConfig = ExternalProgramConfig.getInstance().getEtnPgmProps();

    try {
      jbInit();
      // center on top of main window/frame
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = getSize();
      if (frameSize.height > screenSize.height) frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width) frameSize.width = screenSize.width;
      int w2 = frame.getWidth();
      int h2 = frame.getHeight();
      int x2 = frame.getX();
      int y2 = frame.getY();
      setLocation((x2 + w2 / 2) - frameSize.width / 2, (y2 + h2 / 2) - frameSize.height / 2);

      // now show the world what we and they can do
      this.setVisible(true);
    } catch (Exception ex) {
      LOG.warn("Error while initializing!", ex);
    }
  }

  private void jbInit() throws Exception {

    // make it non resizable
    setResizable(false);

    this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    // create sessions panel
    createSessionsPanel();

    // create emulator options panel
    createEmulatorOptionsPanel();

    // create the button options
    createButtonOptions();

    JTabbedPane optionTabs = new JTabbedPane();

    optionTabs.addChangeListener(this);

    optionTabs.addTab(LangTool.getString("ss.labelConnections"), sessionPanel);
    optionTabs.addTab(LangTool.getString("ss.labelOptions1"), emulOptPanel);
    createLoggingPanel();
    optionTabs.addTab(LangTool.getString("ss.labelLogging"), loggingPanel);
    createAccessPanel();
    optionTabs.addTab(LangTool.getString("ss.labelOptions2"), accessPanel);

    // create external programs panel
    createExternalProgramsPanel();
    optionTabs.addTab(LangTool.getString("ss.labelExternal"), externalPanel);

    createAboutPanel();
    optionTabs.addTab("About", aboutPanel);

    // add the panels to our dialog
    getContentPane().add(optionTabs, BorderLayout.CENTER);
    getContentPane().add(options, BorderLayout.SOUTH);

    // pack it
    pack();

    if (sessions.getRowCount() > 0) {
      int selInterval = -1;
      // set default selection value as the first row or default session
      for (int x = 0; x < sessions.getRowCount(); x++) {
        if (TRUE.equals(ctm.getValueAt(x, 2))) {
          selInterval = x;
          break;
        }
      }
      // if no default selected, use last selection
      if (selInterval < 0) {
        final String lastConKey = loadSelectedSessionPreference();
        if (lastConKey != null) {
          for (int x = 0; x < sessions.getRowCount(); x++) {
            if (lastConKey.equals(ctm.getValueAt(x, 0))) {
              selInterval = x;
              break;
            }
          }
        }
      }
      if (selInterval < 0) selInterval = 0;
      sessions.getSelectionModel().setSelectionInterval(selInterval, selInterval);
      int targetrow = Math.min(sessions.getRowCount() - 1, selInterval + 3); // show additional 3 more lines
      Rectangle cellRect = sessions.getCellRect(targetrow, 0, true);
      sessions.scrollRectToVisible(cellRect);
    } else {
      connectButton.setEnabled(false);
    }
    // Oh man what a pain in the ass. Had to add a window listener to request
    // focus of the sessions list.
    addWindowListener(new WindowAdapter() {
      public void windowOpened(WindowEvent e) {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {

            sessions.requestFocus();
          }
        });
      }
    });

    this.setIconImages(GUIGraphicsUtils.getApplicationIcons());
  }

  public void stateChanged(ChangeEvent e) {

    JTabbedPane p = (JTabbedPane) e.getSource();
    int index = p.getSelectedIndex();
    if (!p.getTitleAt(index)
        .equals(LangTool.getString("ss.labelConnections"))) {
      connectButton.setEnabled(false);
      this.setTitle(LangTool.getString("ss.title") + " - "
          + p.getTitleAt(index));
    } else {
      this.setTitle(LangTool.getString("ss.title") + " - "
          + LangTool.getString("ss.labelConnections"));
      connectButton.setEnabled(true);
    }
  }

  private void createSessionsPanel() {

    // get an instance of our table model
    ctm = new SessionsTableModel(properties);

    // create a table using our custom table model
    sessions = new JSortTable(ctm);

    // prefered sizes ...
    sessions.getColumnModel().getColumn(0).setPreferredWidth(250);
    sessions.getColumnModel().getColumn(1).setPreferredWidth(250);
    sessions.getColumnModel().getColumn(2).setPreferredWidth(65);

    // Add enter as default key for connect with this session
    Action connect = new AbstractAction("connect") {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        doActionConnect();
      }
    };

    KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0, false);
    sessions.getInputMap().put(enter, "connect");
    sessions.getActionMap().put("connect", connect);

    sessions.setSelectionMode(SINGLE_SELECTION);
    sessions.setPreferredScrollableViewportSize(new Dimension(500, 200));
    sessions.setShowGrid(false);

    // Create the scroll pane and add the table to it.
    JScrollPane scrollPane = new JScrollPane(sessions);
    scrollPane
        .setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane
        .setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // This will make the connect dialog react to two clicks instead of having
    // to click on the selection and then clicking twice
    sessions.addMouseListener(new MouseAdapter() {

      public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
          doActionConnect();
        }
      }

    });

    // Setup our selection model listener
    rowSM = sessions.getSelectionModel();
    rowSM.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {

        // Ignore extra messages.
        if (e.getValueIsAdjusting())
          return;

        ListSelectionModel lsm = (ListSelectionModel) e.getSource();

        if (lsm.isSelectionEmpty()) {
          // no rows are selected
          editButton.setEnabled(false);
          removeButton.setEnabled(false);
          connectButton.setEnabled(false);
        } else {

          // selectedRow is selected
          editButton.setEnabled(true);
          removeButton.setEnabled(true);
          connectButton.setEnabled(true);
        }
      }
    });

    // Setup panels
    configOptions.setLayout(borderLayout);

    sessionPanel.setLayout(borderLayout);

    configOptions.add(sessionPanel, BorderLayout.CENTER);

    // emptyPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    // emptyPane.setPreferredSize(new Dimension(200, 10));
    sessionOpts.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    sessionOpts.add(scrollPane, BorderLayout.CENTER);

    sessionPanel.add(sessionOpts, BorderLayout.NORTH);
    sessionPanel.add(sessionOptPanel, BorderLayout.SOUTH);
    // sessionPanel.setBorder(BorderFactory.createRaisedBevelBorder());

    // add the option buttons
    addOptButton(LangTool.getString("ss.optAdd"), "ADD", sessionOptPanel);

    removeButton = addOptButton(LangTool.getString("ss.optDelete"), "REMOVE",
        sessionOptPanel, false);

    editButton = addOptButton(LangTool.getString("ss.optEdit"), "EDIT",
        sessionOptPanel, false);

  }

  private void createEmulatorOptionsPanel() {

    // create emulator options panel
    emulOptPanel.setLayout(new BorderLayout());

    JPanel contentPane = new JPanel();
    contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

    emulOptPanel.add(contentPane, BorderLayout.NORTH);

    // setup the frame interface panel
    JPanel interfacePanel = new JPanel(new GridBagLayout());

    TitledBorder tb = BorderFactory.createTitledBorder(LangTool
        .getString("conf.labelPresentation"));
    tb.setTitleJustification(TitledBorder.CENTER);

    interfacePanel.setBorder(tb);

    ButtonGroup intGroup = new ButtonGroup();

    // create the checkbox for hiding the tab bar when only one tab exists
    hideTabBar = new JCheckBox(LangTool.getString("conf.labelHideTabBar"));

    hideTabBar.setSelected(false);
    if (properties.containsKey("emul.hideTabBar")) {
      if (properties.getProperty("emul.hideTabBar").equals("yes"))
        hideTabBar.setSelected(true);
    }

    hideTabBar.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        hideTabBar_itemStateChanged(e);
      }
    });

    JRadioButton intTABS = new JRadioButton(LangTool.getString("conf.labelTABS"));
    intTABS.setSelected(true);
    intTABS.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        // intTABS_itemStateChanged(e);
        // nothing to do because there is only one option
      }
    });

    // add the interface options to the group control
    intGroup.add(intTABS);

    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(10, 10, 5, 10);
    interfacePanel.add(intTABS, gbc);
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 27, 5, 10);
    interfacePanel.add(hideTabBar, gbc);

    // create startup panel
    JPanel startupPanel = new JPanel();
    startupPanel.setLayout(new AlignLayout(1, 5, 5));
    TitledBorder smb = BorderFactory.createTitledBorder(LangTool
        .getString("ss.labelStartup"));
    smb.setTitleJustification(TitledBorder.CENTER);

    startupPanel.setBorder(smb);

    showMe = new JCheckBox(LangTool.getString("ss.labelShowMe"));
    if (properties.containsKey("emul.showConnectDialog"))
      showMe.setSelected(true);

    showMe.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        showMe_itemStateChanged(e);
      }
    });

    lastView = new JCheckBox(LangTool.getString("ss.labelLastView"));
    if (properties.containsKey("emul.startLastView"))
      lastView.setSelected(true);

    lastView.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        lastView_itemStateChanged(e);
      }
    });

    startupPanel.add(showMe);
    startupPanel.add(lastView);

    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    contentPane.add(interfacePanel);
    // contentPane.add(Box.createHorizontalStrut(10));
    contentPane.add(Box.createVerticalStrut(10));
    contentPane.add(startupPanel);
  }

  private void createLoggingPanel() {
    loggingPanel.setLayout(new GridBagLayout());
    // levelPanel
    JPanel levelPanel = new JPanel(new GridBagLayout());
    TitledBorder tb = BorderFactory.createTitledBorder(LangTool
        .getString("logscr.Level"));
    tb.setTitleJustification(TitledBorder.CENTER);
    levelPanel.setBorder(tb);
    // Create the Checkboxes
    // The translation section is called ...
    // #Logging Literals
    // Search and translate into the message property-files
    int logLevel = Integer.parseInt(properties.getProperty("emul.logLevel",
        Integer.toString(TN5250jLogger.INFO)));

    ButtonGroup levelGroup = new ButtonGroup();
    intOFF = new JRadioButton(LangTool.getString("logscr.Off"));
    intOFF.setSelected(true);
    intOFF.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        intOFF_itemStateChanged(e);
      }
    });

    intDEBUG = new JRadioButton(LangTool.getString("logscr.Debug"));
    intDEBUG.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        intDEBUG_itemStateChanged(e);
      }
    });
    intINFO = new JRadioButton(LangTool.getString("logscr.Info"));
    intINFO.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        intINFO_itemStateChanged(e);
      }
    });

    intWARN = new JRadioButton(LangTool.getString("logscr.Warn"));
    intWARN.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        intWARN_itemStateChanged(e);
      }
    });

    intERROR = new JRadioButton(LangTool.getString("logscr.Error"));
    intERROR.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        intERROR_itemStateChanged(e);
      }
    });

    intFATAL = new JRadioButton(LangTool.getString("logscr.Fatal"));
    intFATAL.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        intFATAL_itemStateChanged(e);
      }
    });

    // add the interface options to the group control
    levelGroup.add(intOFF);
    levelGroup.add(intDEBUG);
    levelGroup.add(intINFO);
    levelGroup.add(intWARN);
    levelGroup.add(intERROR);
    levelGroup.add(intFATAL);

    // add the levelPanel components
    gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(20, 20, 5, 20);
    levelPanel.add(intOFF, gbc);
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 20, 5, 20);
    levelPanel.add(intDEBUG, gbc);
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 20, 5, 20);
    levelPanel.add(intINFO, gbc);
    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 20, 5, 20);
    levelPanel.add(intWARN, gbc);
    gbc.gridx = 0;
    gbc.gridy = 4;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 20, 5, 20);
    levelPanel.add(intERROR, gbc);
    gbc.gridx = 0;
    gbc.gridy = 5;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 20, 20, 20);
    levelPanel.add(intFATAL, gbc);

    JPanel appenderPanel = new JPanel(new GridBagLayout());
    tb = BorderFactory.createTitledBorder(LangTool
        .getString("logscr.Appender"));
    tb.setTitleJustification(TitledBorder.CENTER);
    appenderPanel.setBorder(tb);

    ButtonGroup appenderGroup = new ButtonGroup();
    intConsole = new JRadioButton(LangTool.getString("logscr.Console"));
    intConsole.setSelected(true);
    intConsole.setEnabled(false);
    intConsole.addItemListener(new java.awt.event.ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        // TODO: Provide the itemstatechanged for the intConsole checkbox
      }
    });
    intFile = new JRadioButton(LangTool.getString("logscr.File"));
    intFile.setEnabled(false);
    intBoth = new JRadioButton(LangTool.getString("logscr.Both"));
    intBoth.setEnabled(false);

    appenderGroup.add(intConsole);
    appenderGroup.add(intFile);
    appenderGroup.add(intBoth);

    switch (logLevel) {
      case TN5250jLogger.OFF:
        intOFF.setSelected(true);
        break;
      case TN5250jLogger.DEBUG:
        intDEBUG.setSelected(true);
        break;
      case TN5250jLogger.INFO:
        intINFO.setSelected(true);
        break;
      case TN5250jLogger.WARN:
        intWARN.setSelected(true);
        break;
      case TN5250jLogger.ERROR:
        intERROR.setSelected(true);
        break;
      case TN5250jLogger.FATAL:
        intFATAL.setSelected(true);
        break;
      default:
        intINFO.setSelected(true);
    }

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(20, 20, 5, 20);
    appenderPanel.add(intConsole, gbc);
    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 20, 5, 20);
    appenderPanel.add(intFile, gbc);
    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(5, 20, 20, 20);
    appenderPanel.add(intBoth, gbc);

    // add the pannels to the mainpanel
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(10, 10, 10, 20);
    loggingPanel.add(levelPanel, gbc);
    gbc.gridx = 1;
    gbc.gridy = 0;
    gbc.anchor = GridBagConstraints.WEST;
    gbc.insets = new Insets(10, 20, 10, 10);
    loggingPanel.add(appenderPanel, gbc);
  }

  private void createAccessPanel() {

    accessOptions = new MultiSelectListComponent();

    if (properties.getProperty("emul.accessDigest") != null)
      accessOptions.setEnabled(false);

    String[] options = keyMnemonicResolver.getMnemonicsSorted();

    // set up a hashtable of option descriptions to options
    Hashtable<String, String> ht = new Hashtable<String, String>(options.length);
    for (String option : options) {
      ht.put(LangTool.getString("key." + option), option);
    }

    String[] descriptions = keyMnemonicResolver.getMnemonicDescriptions();
    Arrays.sort(descriptions);

    // set the option descriptions
    accessOptions.setListData(descriptions);

    // we now mark the invalid options
    int num = OptionAccessFactory.getInstance().getNumberOfRestrictedOptions();
    int[] si = new int[num];
    int i = 0;
    for (int x = 0; x < descriptions.length; x++) {
      if (!OptionAccessFactory.getInstance().isValidOption(
          ht.get(descriptions[x])))
        si[i++] = x;
    }

    accessOptions.setSelectedIndices(si);

    accessOptions.setSourceHeader(LangTool.getString("ss.labelActive"),
        JLabel.CENTER);
    accessOptions.setSelectionHeader(
        LangTool.getString("ss.labelRestricted"), JLabel.CENTER);

    // create emulator options panel
    accessPanel.setLayout(new BorderLayout());
    accessPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 5));

    accessPanel.add(accessOptions, BorderLayout.CENTER);

    JPanel passPanel = new JPanel();
    passPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
    passPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 0));

    Action action = new AbstractAction(LangTool.getString("ss.labelSetPass")) {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        if (password.getPassword().length > 0) {
          try {
            DESSHA1 sha = new DESSHA1();
            properties.setProperty("emul.accessDigest", sha.digest(new String(
                password.getPassword()), "tn5205j"));
          } catch (Exception ex) {
          }
        }
      }
    };

    setPassButton = new JButton(action);

    if (properties.getProperty("emul.accessDigest") != null)
      setPassButton.setEnabled(false);

    passPanel.add(setPassButton);

    password = new JPasswordField(15);
    password.setDocument(new CheckPasswordDocument());

    passPanel.add(password);

    accessPanel.add(passPanel, BorderLayout.NORTH);

  }

  private void createExternalProgramsPanel() {

    // create external options panel

    JPanel externalPrograms = new JPanel();

    // define layout
    externalPanel.setLayout(new BorderLayout());
    externalPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 5));

    externalPrograms.setLayout(new AlignLayout(3, 5, 5));
    externalPrograms.setBorder(BorderFactory.createTitledBorder(LangTool
        .getString("external.title")));

    externalPrograms.add(new JLabel(LangTool.getString("external.http")));
    browser = new JTextField(30);
    if (properties.containsKey("emul.protocol.http")) {
      browser.setText(properties.getProperty("emul.protocol.http"));
    }
    externalPrograms.add(browser);
    externalPrograms.add(new JButton("..."));

    externalPrograms.add(new JLabel(LangTool.getString("external.mailto")));
    mailer = new JTextField(30);
    if (properties.containsKey("emul.protocol.mailto")) {
      mailer.setText(properties.getProperty("emul.protocol.mailto"));
    }
    externalPrograms.add(mailer);
    externalPrograms.add(new JButton("..."));

    externalPanel.add(externalPrograms, BorderLayout.NORTH);

    //For Customized External Program
    etm = new CustomizedTableModel(externalProgramConfig);
    // create a table using our custom table model
    externals = new JSortTable(etm);
    externals.setSelectionMode(SINGLE_SELECTION);
    externals.setPreferredScrollableViewportSize(new Dimension(500, 100));
    externals.setShowGrid(false);
    // Create the scroll pane and add the table to it.
    JScrollPane scrollPane2 = new JScrollPane(externals);
    scrollPane2.setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
    scrollPane2.setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_AS_NEEDED);

    // This will make the connect dialog react to two clicks instead of having
    // to click on the selection and then clicking twice
    externals.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) {
          doActionConnect();
        }
      }
    });

    // Setup our selection model listener
    rowSM2 = externals.getSelectionModel();
    rowSM2.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        // Ignore extra messages.
        if (e.getValueIsAdjusting())
          return;
        ListSelectionModel lsm = (ListSelectionModel) e.getSource();
        if (lsm.isSelectionEmpty()) {
          // no rows are selected
          cEditButton.setEnabled(false);
          cRemoveButton.setEnabled(false);
          cAddButton.setEnabled(false);
        } else {
          // selectedRow is selected
          cEditButton.setEnabled(true);
          cRemoveButton.setEnabled(true);
          cAddButton.setEnabled(true);
        }
      }
    });

    // Setup panels
    JPanel cExternalPrograms = new JPanel();
    cExternalPrograms.setLayout(new BorderLayout());
    cExternalPrograms.setBorder(BorderFactory.createTitledBorder(LangTool.getString("customized.title")));
    externalOpts.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
    externalOpts.add(scrollPane2, BorderLayout.CENTER);

    cExternalPrograms.add(externalOpts, BorderLayout.NORTH);
    cExternalPrograms.add(externalOptPanel, BorderLayout.SOUTH);
    // sessionPanel.setBorder(BorderFactory.createRaisedBevelBorder());

    // add the option buttons
    cAddButton = addOptButton(LangTool.getString("ss.optAdd"), "cADD", externalOptPanel);

    cRemoveButton = addOptButton(LangTool.getString("ss.optDelete"), "cREMOVE",
        externalOptPanel, false);

    cEditButton = addOptButton(LangTool.getString("ss.optEdit"), "cEDIT",
        externalOptPanel, false);

    externalPanel.add(cExternalPrograms, BorderLayout.CENTER);
  }

  private void doSomethingEntered() {

    if (properties.getProperty("emul.accessDigest") != null) {
      try {
        DESSHA1 sha = new DESSHA1();
        if (properties.getProperty("emul.accessDigest").equals(
            sha.digest(new String(password.getPassword()), "tn5205j"))) {
          accessOptions.setEnabled(true);
          setPassButton.setEnabled(true);
        }
      } catch (Exception ex) {
        LOG.warn(ex.getMessage(), ex);
      }
    }
  }

  private void doNothingEntered() {

  }

  private void createButtonOptions() {
    connectButton = addOptButton(LangTool.getString("ss.optConnect"), "CONNECT", options, false);
    addOptButton(LangTool.getString("ss.optApply"), "APPLY", options, true);
    addOptButton(LangTool.getString("ss.optCancel"), "DONE", options);
  }

  /**
   * Simple about the program ...
   */
  private void createAboutPanel() {

    aboutPanel = new JPanel();

    JPanel contenpane = new JPanel();

    TitledBorder tb = BorderFactory.createTitledBorder("About");
    tb.setTitleJustification(TitledBorder.CENTER);

    contenpane.add(new JLabel("TN5250j"));
    contenpane.add(new JLabel("Version: " + TN5250jConstants.VERSION_INFO));

    contenpane.setLayout(new BoxLayout(contenpane, BoxLayout.Y_AXIS));

    aboutPanel.add(contenpane);
    aboutPanel.setBorder(tb);
  }

  private JButton addOptButton(String text, String ac, Container container) {

    return addOptButton(text, ac, container, true);
  }

  private JButton addOptButton(String text, String ac, Container container,
                               boolean enabled) {

    JButton button = new JButton(text);
    button.setEnabled(enabled);
    button.setActionCommand(ac);
    button.setPreferredSize(new Dimension(140, 28));

    // we check if there was mnemonic specified and if there was then we
    // set it.
    int mnemIdx = text.indexOf("&");
    if (mnemIdx >= 0) {
      StringBuilder sb = new StringBuilder(text);
      sb.deleteCharAt(mnemIdx);
      button.setText(sb.toString());
      button.setMnemonic(text.charAt(mnemIdx + 1));
    }
    button.addActionListener(this);
    button.setAlignmentX(Component.CENTER_ALIGNMENT);
    container.add(button);

    return button;
  }

  // Process out button actions
  public void actionPerformed(ActionEvent e) {

    if (e.getActionCommand().equals("DONE")) {
      saveProps();
      setVisible(false);
    }

    if (e.getActionCommand().equals("ADD")) {
      String systemName = Configure.doEntry((JFrame) getParent(), null,
          properties);
      ctm.addSession();
      // I we have only one row then select the first one so that
      // we do not get a selection index out of range problem.
      if (ctm.getRowCount() == 1) {
        sessions.getSelectionModel().setSelectionInterval(0, 0);
      } else {
        // Here we will select the entry that we just added.
        int selInterval = 0;
        for (int x = 0; x < sessions.getRowCount(); x++) {
          if (((String) ctm.getValueAt(x, 0)).equals(systemName))
            selInterval = x;
        }
        sessions.getSelectionModel().setSelectionInterval(selInterval,
            selInterval);
      }

      sessions.requestFocus();

    }
    if (e.getActionCommand().equals("REMOVE")) {
      removeEntry();
      editButton.setEnabled(false);
      removeButton.setEnabled(false);
    }

    if (e.getActionCommand().equals("EDIT")) {
      int selectedRow = rowSM.getMinSelectionIndex();
      Configure.doEntry((JFrame) getParent(), (String) ctm.getValueAt(
          selectedRow, 0), properties);
      ctm.chgSession(selectedRow);
      sessions.requestFocus();
    }
    if (e.getActionCommand().equals("cADD")) {
      String name = ExternalProgramConfig.doEntry((JFrame) getParent(), null,
          externalProgramConfig);

      etm.addSession();
      // I we have only one row then select the first one so that
      // we do not get a selection index out of range problem.
      if (etm.getRowCount() == 1) {
        externals.getSelectionModel().setSelectionInterval(0, 0);
      } else {
        // Here we will select the entry that we just added.
        int selInterval = 0;
        for (int x = 0; x < externals.getRowCount(); x++) {
          if (((String) etm.getValueAt(x, 0)).equals(name))
            selInterval = x;
        }
        externals.getSelectionModel().setSelectionInterval(selInterval,
            selInterval);
      }

      externals.requestFocus();

    }
    if (e.getActionCommand().equals("cEDIT")) {
      int selectedRow = rowSM2.getMinSelectionIndex();
      ExternalProgramConfig.doEntry((JFrame) getParent(), (String) etm.getValueAt(
          selectedRow, 0), externalProgramConfig);
      etm.chgSession(selectedRow);
      externals.requestFocus();
    }

    if (e.getActionCommand().equals("cREMOVE")) {
      removeExternalProgram();
      cEditButton.setEnabled(false);
      cRemoveButton.setEnabled(false);
    }

    if (e.getActionCommand().equals("CONNECT")) {
      doActionConnect();
    }

    if (e.getActionCommand().equals("APPLY")) {
      saveProps();
    }
  }

  private void doActionConnect() {

    if (!connectButton.isEnabled())
      return;

    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    int selectedRow = rowSM.getMinSelectionIndex();
    connectKey = (String) ctm.getValueAt(selectedRow, 0);
    saveProps();

    // this thread.sleep will get rid of those extra keystrokes that keep
    // propogating to other peers. This is a very annoying bug that
    // should be fixed. This seems to work through 1.4.0 but in 1.4.1
    // beta seems to be broken again. WHY!!!!!
    // try {Thread.sleep(500);}catch(java.lang.InterruptedException ie) {}

    setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

    this.dispose();

  }

  public String getConnectKey() {
    return connectKey;
  }

  private void saveProps() {

    if (connectKey != null) {
      saveSelectedSessionPreference(connectKey);
    }

    setOptionAccess();

    setExternalPrograms();

    setLogLevel();

    ConfigureFactory.getInstance().saveSettings(ConfigureFactory.SESSIONS,
        "------ Session Information --------");
    ConfigureFactory.getInstance().saveSettings(ExternalProgramConfig.EXTERNAL_PROGRAM_REGISTRY_KEY,
        ExternalProgramConfig.EXTERNAL_PROGRAM_PROPERTIES_FILE_NAME,
        ExternalProgramConfig.EXTERNAL_PROGRAM_HEADER);
    OptionAccessFactory.getInstance().reload();
  }

  private void saveSelectedSessionPreference(String connectKey) {
    final Preferences userpref = Preferences.userNodeForPackage(SessionsDataModel.class);
    userpref.put(USER_PREF_LAST_SESSION, connectKey);
  }

  private String loadSelectedSessionPreference() {
    final Preferences userpref = Preferences.userNodeForPackage(SessionsDataModel.class);
    return userpref.get(USER_PREF_LAST_SESSION, null);
  }

  /**
   * Set the external programs that are to be used globally within the
   * emulator. Right now external browser and mail programs are supported
   */
  private void setExternalPrograms() {

    // set the external browser program to use
    if (browser.getText().trim().length() > 0) {

      properties.setProperty("emul.protocol.http", browser.getText().trim());
    } else {

      properties.remove("emul.protocol.http");
    }

    // set the external mailer program to use
    if (mailer.getText().trim().length() > 0) {

      properties.setProperty("emul.protocol.mailto", mailer.getText().trim());
    } else {

      properties.remove("emul.protocol.mailto");
    }

  }

  private void setLogLevel() {

    if (intOFF.isSelected()) {

      properties
          .setProperty("emul.logLevel", Integer
              .toString(TN5250jLogger.OFF));
    }

    if (intDEBUG.isSelected()) {

      properties.setProperty("emul.logLevel", Integer
          .toString(TN5250jLogger.DEBUG));
    }

    if (intINFO.isSelected()) {

      properties.setProperty("emul.logLevel", Integer
          .toString(TN5250jLogger.INFO));
    }

    if (intWARN.isSelected()) {

      properties.setProperty("emul.logLevel", Integer
          .toString(TN5250jLogger.WARN));
    }

    if (intERROR.isSelected()) {

      properties.setProperty("emul.logLevel", Integer
          .toString(TN5250jLogger.ERROR));
    }

    if (intFATAL.isSelected()) {

      properties.setProperty("emul.logLevel", Integer
          .toString(TN5250jLogger.FATAL));
    }

  }

  private void setOptionAccess() {

    String[] options = keyMnemonicResolver.getMnemonicsSorted();

    // set up a hashtable of option descriptions to options
    Hashtable<String, String> ht = new Hashtable<String, String>(options.length);
    for (int x = 0; x < options.length; x++) {
      ht.put(LangTool.getString("key." + options[x]), options[x]);
    }

    Object[] restrict = accessOptions.getSelectedValues();
    String s = "";
    for (int x = 0; x < restrict.length; x++) {
      s += ht.get(restrict[x]) + ";";
    }
    properties.setProperty("emul.restricted", s);
  }

  private void removeEntry() {
    int selectedRow = rowSM.getMinSelectionIndex();
    properties.remove(ctm.getValueAt(selectedRow, 0));
    ctm.removeSession(selectedRow);
  }

  private void removeExternalProgram() {
    int selectedRow = rowSM2.getMinSelectionIndex();
    String propKey = (String) etm.getValueAt(selectedRow, 0);
    int num = 0;

    for (Enumeration<Object> e = externalProgramConfig.keys(); e.hasMoreElements(); ) {
      String key = (String) e.nextElement();
      if (propKey != null && propKey.equals(externalProgramConfig.getProperty(key))) {
        String subKey = key.substring(8);
        int index = subKey.indexOf(".");
        num = Integer.parseInt(subKey.substring(0, index));
        break;
      }
    }
    Properties newProps = new Properties();
    String count = externalProgramConfig.getProperty("etn.pgm.support.total.num");
    if (count != null && count.length() > 0) {
      int total = Integer.parseInt(count);
      for (int i = 1; i <= total; i++) {
        int order = i;
        if (i > num) order = i - 1;
        if (i != num) {
          String program = externalProgramConfig.getProperty("etn.pgm." + i + ".command.name");
          String wCommand = externalProgramConfig.getProperty("etn.pgm." + i + ".command.window");
          String uCommand = externalProgramConfig.getProperty("etn.pgm." + i + ".command.unix");
          newProps.setProperty("etn.pgm." + order + ".command.name", program);
          newProps.setProperty("etn.pgm." + order + ".command.window", wCommand);
          newProps.setProperty("etn.pgm." + order + ".command.unix", uCommand);
        }
      }
      newProps.setProperty("etn.pgm.support.total.num", String.valueOf(total - 1));
    }

    externalProgramConfig.clear();
    externalProgramConfig.putAll(newProps);
    etm.removeSession(selectedRow);
  }

  private void hideTabBar_itemStateChanged(ItemEvent e) {

    if (hideTabBar.isSelected())
      properties.setProperty("emul.hideTabBar", "yes");
    else
      properties.remove("emul.hideTabBar");

  }

  private void showMe_itemStateChanged(ItemEvent e) {

    if (showMe.isSelected()) {
      properties.setProperty("emul.showConnectDialog", "");
    } else {

      properties.remove("emul.showConnectDialog");
    }

  }

  private void lastView_itemStateChanged(ItemEvent e) {

    if (lastView.isSelected()) {
      properties.setProperty("emul.startLastView", "");
    } else {

      properties.remove("emul.startLastView");
    }

  }

  private void intOFF_itemStateChanged(ItemEvent e) {
    if (!intOFF.isSelected() && TN5250jLogFactory.isLog4j()) {
      intConsole.setEnabled(true);
      intFile.setEnabled(true);
      intBoth.setEnabled(true);
    } else {
      intConsole.setEnabled(false);
      intConsole.setSelected(true);
      intFile.setEnabled(false);
      intBoth.setEnabled(false);
      TN5250jLogFactory.setLogLevels(TN5250jLogger.OFF);
    }
  }

  private void intDEBUG_itemStateChanged(ItemEvent e) {
    if (intDEBUG.isSelected()) {
      TN5250jLogFactory.setLogLevels(TN5250jLogger.DEBUG);
    }
  }

  private void intINFO_itemStateChanged(ItemEvent e) {
    if (intINFO.isSelected()) {
      TN5250jLogFactory.setLogLevels(TN5250jLogger.INFO);
    }
  }

  private void intWARN_itemStateChanged(ItemEvent e) {
    if (intWARN.isSelected()) {
      TN5250jLogFactory.setLogLevels(TN5250jLogger.WARN);
    }
  }

  private void intERROR_itemStateChanged(ItemEvent e) {
    if (intERROR.isSelected()) {
      TN5250jLogFactory.setLogLevels(TN5250jLogger.ERROR);
    }
  }

  private void intFATAL_itemStateChanged(ItemEvent e) {
    if (intFATAL.isSelected()) {
      TN5250jLogFactory.setLogLevels(TN5250jLogger.FATAL);
    }
  }

  private class CheckPasswordDocument extends PlainDocument {

    private static final long serialVersionUID = 1L;

    public void insertString(int offs, String str, AttributeSet a)
        throws BadLocationException {

      super.insertString(offs, str, a);
      if (getText(0, getLength()).length() > 0)
        doSomethingEntered();
    }

    public void remove(int offs, int len) throws BadLocationException {
      super.remove(offs, len);
      if (getText(0, getLength()).length() == 0)
        doNothingEntered();
    }
  }

}
