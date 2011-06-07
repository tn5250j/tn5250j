/**
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
package org.tn5250j;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.tn5250j.gui.JSortTable;
import org.tn5250j.gui.SortTableModel;
import org.tn5250j.gui.TN5250jMultiSelectList;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.interfaces.OptionAccessFactory;
import org.tn5250j.tools.AlignLayout;
import org.tn5250j.tools.DESSHA1;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public class ConnectDialog extends JDialog implements ActionListener, ChangeListener, TN5250jConstants {

   private static final String USER_PREF_LAST_SESSION = "last_session";

   private static final long serialVersionUID = 1L;

   volatile private static TN5250jLogger LOG = TN5250jLogFactory.getLogger(ConnectDialog.class);

   // panels to be displayed
   JPanel configOptions = new JPanel();
   JPanel sessionPanel = new JPanel();
   JPanel options = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
   JPanel interfacePanel = null;
   JPanel sessionOpts = new JPanel();
   JPanel sessionOptPanel = new JPanel(
         new FlowLayout(FlowLayout.CENTER, 30, 10));
   JPanel emulOptPanel = new JPanel();
   JPanel emptyPane = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
   JPanel accessPanel = new JPanel();
   JPanel loggingPanel = new JPanel();
   JPanel levelPanel = new JPanel();
   JPanel appenderPanel = new JPanel();
   JPanel externalPanel = new JPanel();
   JPanel externalOpts = new JPanel();
   JPanel externalOptPanel = new JPanel(
         new FlowLayout(FlowLayout.CENTER, 30, 10));
   JPanel aboutPanel = null;
   
   JTable sessions = null;
   JTable externals = null;
   GridBagConstraints gbc;

   // LoggingPanel Components
   JRadioButton intOFF = null;
   JRadioButton intDEBUG = null;
   JRadioButton intINFO = null;
   JRadioButton intWARN = null;
   JRadioButton intERROR = null;
   JRadioButton intFATAL = null;

   // button needing global access
   JButton editButton = null;
   JButton removeButton = null;
   JButton connectButton = null;
   JButton applyButton = null;
   
   JButton cAddButton = null;
   JButton cEditButton = null;
   JButton cRemoveButton = null;
   
   
   // custom table model
   SessionsTableModel ctm = null;
   
   CustomizedTableModel etm = null;
   
   // The scroll pane that holds the table.
   JScrollPane scrollPane;
   JScrollPane scrollPane2;

   // ListSelectionModel of our custom table.
   ListSelectionModel rowSM = null;
   ListSelectionModel rowSM2 = null;
   // Properties
   Properties props = null;
   Properties etnProps = null;
   
   // property input structures
   JRadioButton intTABS = null;
   JCheckBox hideTabBar = null;
   JCheckBox showMe = null;
   JCheckBox lastView = null;

   // create some reusable borders and layouts
   Border etchedBorder = BorderFactory.createEtchedBorder();
   BorderLayout borderLayout = new BorderLayout();

   TN5250jMultiSelectList accessOptions;
   // password protection field for access to options list
   JPasswordField password;
   JButton setPassButton;

   // Selection value for connection
   String connectKey = null;
   private JRadioButton intConsole;
   private JRadioButton intFile;
   private JRadioButton intBoth;

   private JTextField browser;
   private JTextField mailer;

   public ConnectDialog(Frame frame, String title, Properties prop) {

      super(frame, title, true);

      props = ConfigureFactory.getInstance().getProperties(
            ConfigureFactory.SESSIONS);
	  etnProps = ExternalProgramConfig.getInstance().getEtnPgmProps();
	  
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
         setLocation((x2 + w2/2) - frameSize.width/2, (y2 + h2/2) - frameSize.height/2);

         // now show the world what we and they can do
         this.setVisible(true);
      }
      catch (Exception ex) {
         LOG.warn("Error while initializing!", ex);
      }
   }

   void jbInit() throws Exception {

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
            if (((Boolean) ctm.getValueAt(x, 2)).booleanValue()){
            	selInterval = x; break;
            }
         }
         // if no default selected, use last selection
         if (selInterval < 0) {
        	 final String lastConKey = loadSelectedSessionPreference();
        	 if (lastConKey != null) {
        		 for (int x = 0; x < sessions.getRowCount(); x++) {
        			 if (lastConKey.equals(ctm.getValueAt(x, 0))) {
        				 selInterval = x; break;
        			 }
        		 }
        	 }
         }
         if (selInterval<0) selInterval = 0;  
         sessions.getSelectionModel().setSelectionInterval(selInterval, selInterval);
         int targetrow = Math.min(sessions.getRowCount()-1, selInterval+3); // show additional 3 more lines
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
      }
      else {
         this.setTitle(LangTool.getString("ss.title") + " - "
               + LangTool.getString("ss.labelConnections"));
         connectButton.setEnabled(true);
      }
   }

   private void createSessionsPanel() {

      // get an instance of our table model
      ctm = new SessionsTableModel();

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

      sessions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      sessions.setPreferredScrollableViewportSize(new Dimension(500, 200));
      sessions.setShowGrid(false);

      // Create the scroll pane and add the table to it.
      scrollPane = new JScrollPane(sessions);
      scrollPane
            .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane
            .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

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
            }
            else {

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
      interfacePanel = new JPanel(new GridBagLayout());

      TitledBorder tb = BorderFactory.createTitledBorder(LangTool
            .getString("conf.labelPresentation"));
      tb.setTitleJustification(TitledBorder.CENTER);

      interfacePanel.setBorder(tb);

      ButtonGroup intGroup = new ButtonGroup();

      // create the checkbox for hiding the tab bar when only one tab exists
      hideTabBar = new JCheckBox(LangTool.getString("conf.labelHideTabBar"));

      hideTabBar.setSelected(false);
      if (props.containsKey("emul.hideTabBar")) {
         if (props.getProperty("emul.hideTabBar").equals("yes"))
            hideTabBar.setSelected(true);
      }

      hideTabBar.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            hideTabBar_itemStateChanged(e);
         }
      });

      intTABS = new JRadioButton(LangTool.getString("conf.labelTABS"));
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
      if (props.containsKey("emul.showConnectDialog"))
         showMe.setSelected(true);

      showMe.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            showMe_itemStateChanged(e);
         }
      });

      lastView = new JCheckBox(LangTool.getString("ss.labelLastView"));
      if (props.containsKey("emul.startLastView"))
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
      levelPanel = new JPanel(new GridBagLayout());
      TitledBorder tb = BorderFactory.createTitledBorder(LangTool
            .getString("logscr.Level"));
      tb.setTitleJustification(TitledBorder.CENTER);
      levelPanel.setBorder(tb);
      // Create the Checkboxes
      // The translation section is called ...
      // #Logging Literals
      // Search and translate into the message property-files
      int logLevel = Integer.parseInt(props.getProperty("emul.logLevel",
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

      appenderPanel = new JPanel(new GridBagLayout());
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
            intConsole_itemStateChanged(e);
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

      accessOptions = new TN5250jMultiSelectList();

      if (props.getProperty("emul.accessDigest") != null)
         accessOptions.setEnabled(false);

      List<String> options = OptionAccessFactory.getInstance().getOptions();

      // set up a hashtable of option descriptions to options
      Hashtable<String, String> ht = new Hashtable<String, String> (options.size());
      for (int x = 0; x < options.size(); x++) {
         ht.put(LangTool.getString("key." + options.get(x)), options.get(x));
      }

      // get the sorted descriptions of the options
      List<String> descriptions = OptionAccessFactory.getInstance()
            .getOptionDescriptions();

      // set the option descriptions
      accessOptions.setListData(descriptions.toArray());

      // we now mark the invalid options
      int num = OptionAccessFactory.getInstance()
            .getNumberOfRestrictedOptions();
      int[] si = new int[num];
      int i = 0;
      for (int x = 0; x < descriptions.size(); x++) {
         if (!OptionAccessFactory.getInstance().isValidOption(
               ht.get(descriptions.get(x))))
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
                  props.setProperty("emul.accessDigest", sha.digest(new String(
                        password.getPassword()), "tn5205j"));
               }
               catch (Exception ex) {}
            }
         }
      };

      setPassButton = new JButton(action);

      if (props.getProperty("emul.accessDigest") != null)
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
      if (props.containsKey("emul.protocol.http")) {
         browser.setText(props.getProperty("emul.protocol.http"));
      }
      externalPrograms.add(browser);
      externalPrograms.add(new JButton("..."));

      externalPrograms.add(new JLabel(LangTool.getString("external.mailto")));
      mailer = new JTextField(30);
      if (props.containsKey("emul.protocol.mailto")) {
         mailer.setText(props.getProperty("emul.protocol.mailto"));
      }
      externalPrograms.add(mailer);
      externalPrograms.add(new JButton("..."));

      externalPanel.add(externalPrograms, BorderLayout.NORTH);
	  
	  //For Customized External Program
      etm = new CustomizedTableModel();
      // create a table using our custom table model
      externals = new JSortTable(etm);	  
	  externals.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	  externals.setPreferredScrollableViewportSize(new Dimension(500, 100));
	  externals.setShowGrid(false);
      // Create the scroll pane and add the table to it.
      scrollPane2 = new JScrollPane(externals);
      scrollPane2
            .setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane2
            .setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

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
            }
            else {
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

      if (props.getProperty("emul.accessDigest") != null) {
         try {
            DESSHA1 sha = new DESSHA1();
            // System.out.println(props.getProperty("emul.accessDigest")
            // + " -> " + sha.digest(new
            // String(password.getPassword()),"tn5205j"));
            if (props.getProperty("emul.accessDigest").equals(
                  sha.digest(new String(password.getPassword()), "tn5205j"))) {
               accessOptions.setEnabled(true);
               setPassButton.setEnabled(true);
            }
         }
         catch (Exception ex) {
        	 LOG.warn(ex.getMessage(), ex);
         }
      }
   }

   private void doNothingEntered() {

   }

   private void createButtonOptions() {

      connectButton = addOptButton(LangTool.getString("ss.optConnect"),
            "CONNECT", options, false);

      applyButton = addOptButton(LangTool.getString("ss.optApply"), "APPLY",
            options, true);

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
	   contenpane.add(new JLabel("Version: " + TN5250jConstants.tn5250jRelease + TN5250jConstants.tn5250jVersion + TN5250jConstants.tn5250jSubVer));
	   
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
         StringBuffer sb = new StringBuffer(text);
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
               props);
         ctm.addSession();
         // I we have only one row then select the first one so that
         // we do not get a selection index out of range problem.
         if (ctm.getRowCount() == 1) {
            sessions.getSelectionModel().setSelectionInterval(0, 0);
         }
         else {
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
               selectedRow, 0), props);
         ctm.chgSession(selectedRow);
         sessions.requestFocus();
      }
	  if (e.getActionCommand().equals("cADD")) {
	         String name = ExternalProgramConfig.doEntry((JFrame) getParent(), null,
					 etnProps);
			 
	         etm.addSession();
	         // I we have only one row then select the first one so that
	         // we do not get a selection index out of range problem.
	         if (etm.getRowCount() == 1) {
	            externals.getSelectionModel().setSelectionInterval(0, 0);
	         }
	         else {
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
		               selectedRow, 0), etnProps);
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

   /**
    * Saves the last selected session in a system provided user space.
    * 
    * @param connectKey
    * @see {@link java.util.prefs.Preferences}
    */
   private void saveSelectedSessionPreference(String connectKey) {
	   final Preferences userpref = Preferences.userNodeForPackage(SessionsDataModel.class);
	   userpref.put(USER_PREF_LAST_SESSION, connectKey);
   }
   
   /**
    * Saves the last selected session in a system provided user space.
    * 
    * @param connectKey
    * @see {@link java.util.prefs.Preferences}
    */
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

         props.setProperty("emul.protocol.http", browser.getText().trim());
      }
      else {

         props.remove("emul.protocol.http");
      }

      // set the external mailer program to use
      if (mailer.getText().trim().length() > 0) {

         props.setProperty("emul.protocol.mailto", mailer.getText().trim());
      }
      else {

         props.remove("emul.protocol.mailto");
      }

   }

   private void setLogLevel() {

      if (intOFF.isSelected()) {

         props
               .setProperty("emul.logLevel", Integer
                     .toString(TN5250jLogger.OFF));
      }

      if (intDEBUG.isSelected()) {

         props.setProperty("emul.logLevel", Integer
               .toString(TN5250jLogger.DEBUG));
      }

      if (intINFO.isSelected()) {

         props.setProperty("emul.logLevel", Integer
               .toString(TN5250jLogger.INFO));
      }

      if (intWARN.isSelected()) {

         props.setProperty("emul.logLevel", Integer
               .toString(TN5250jLogger.WARN));
      }

      if (intERROR.isSelected()) {

         props.setProperty("emul.logLevel", Integer
               .toString(TN5250jLogger.ERROR));
      }

      if (intFATAL.isSelected()) {

         props.setProperty("emul.logLevel", Integer
               .toString(TN5250jLogger.FATAL));
      }

   }

   private void setOptionAccess() {

      List<String> options = OptionAccessFactory.getInstance().getOptions();

      // set up a hashtable of option descriptions to options
      Hashtable<String, String> ht = new Hashtable<String,String>(options.size());
      for (int x = 0; x < options.size(); x++) {
         ht.put(LangTool.getString("key." + options.get(x)), options.get(x));
      }

      Object[] restrict = accessOptions.getSelectedValues();
      String s = "";
      for (int x = 0; x < restrict.length; x++) {
         s += ht.get(restrict[x]) + ";";
      }
      props.setProperty("emul.restricted", s);
   }

//   private void addLabelComponent(String text, Component comp,
//         Container container) {
//
//      JLabel label = new JLabel(text);
//      label.setAlignmentX(Component.LEFT_ALIGNMENT);
//      label.setHorizontalTextPosition(JLabel.LEFT);
//      container.add(label);
//      container.add(comp);
//   }

   private void removeEntry() {
      int selectedRow = rowSM.getMinSelectionIndex();
      props.remove(ctm.getValueAt(selectedRow, 0));
      ctm.removeSession(selectedRow);
   }
   
   private void removeExternalProgram() {
      int selectedRow = rowSM2.getMinSelectionIndex();
	  String propKey = (String)etm.getValueAt(selectedRow, 0);
	  int num=0;
      
	  for (Enumeration<Object> e = etnProps.keys() ; e.hasMoreElements() ;) {
		 String key = (String)e.nextElement();
		 if(etnProps.getProperty(key) == propKey){
			 String subKey = key.substring(8);
			 int index = subKey.indexOf(".");
			 num = Integer.parseInt(subKey.substring(0,index));
			 break;
		 }
      }	
	  Properties newProps = new Properties();
	  String count = etnProps.getProperty("etn.pgm.support.total.num");
	  if(count != null && count.length() > 0){
		int total = Integer.parseInt(count);
		for(int i=1;i<=total;i++){
			int order =i;			
			if(i > num) order = i -1;
			if(i != num){
				String program = etnProps.getProperty("etn.pgm."+i+".command.name");
				String wCommand = etnProps.getProperty("etn.pgm."+i+".command.window");
				String uCommand = etnProps.getProperty("etn.pgm."+i+".command.unix");
				newProps.setProperty("etn.pgm."+order+".command.name",program);
				newProps.setProperty("etn.pgm."+order+".command.window",wCommand);
				newProps.setProperty("etn.pgm."+order+".command.unix",uCommand);
			}
		}
		newProps.setProperty("etn.pgm.support.total.num",String.valueOf(total - 1));
	  }
	  
	  etnProps.clear();
	  etnProps.putAll(newProps);
      etm.removeSession(selectedRow);
   }

   void hideTabBar_itemStateChanged(ItemEvent e) {

      if (hideTabBar.isSelected())
         props.setProperty("emul.hideTabBar", "yes");
      else
         props.remove("emul.hideTabBar");

   }

   void showMe_itemStateChanged(ItemEvent e) {

      if (showMe.isSelected()) {
         props.setProperty("emul.showConnectDialog", "");
      }
      else {

         props.remove("emul.showConnectDialog");
      }

   }

   void lastView_itemStateChanged(ItemEvent e) {

      if (lastView.isSelected()) {
         props.setProperty("emul.startLastView", "");
      }
      else {

         props.remove("emul.startLastView");
      }

   }

   private void intOFF_itemStateChanged(ItemEvent e) {
      if (!intOFF.isSelected() && TN5250jLogFactory.isLog4j()) {
         intConsole.setEnabled(true);
         intFile.setEnabled(true);
         intBoth.setEnabled(true);
      }
      else {
         intConsole.setEnabled(false);
         intConsole.setSelected(true);
         intFile.setEnabled(false);
         intBoth.setEnabled(false);
         TN5250jLogFactory.setLogLevels(TN5250jLogger.OFF);
      }
   }

   private void intDEBUG_itemStateChanged(ItemEvent e) {
      if (intDEBUG.isSelected()) {
         // intConsole.setEnabled(true);
         // intFile.setEnabled(true);
         // intBoth.setEnabled(true);
         TN5250jLogFactory.setLogLevels(TN5250jLogger.DEBUG);
      }
   }

   private void intINFO_itemStateChanged(ItemEvent e) {
      if (intINFO.isSelected()) {
         // intConsole.setEnabled(true);
         // intFile.setEnabled(true);
         // intBoth.setEnabled(true);
         TN5250jLogFactory.setLogLevels(TN5250jLogger.INFO);
      }
   }

   private void intWARN_itemStateChanged(ItemEvent e) {
      if (intWARN.isSelected()) {
         // intConsole.setEnabled(true);
         // intFile.setEnabled(true);
         // intBoth.setEnabled(true);
         TN5250jLogFactory.setLogLevels(TN5250jLogger.WARN);
      }
   }

   private void intERROR_itemStateChanged(ItemEvent e) {
      if (intERROR.isSelected()) {
         // intConsole.setEnabled(true);
         // intFile.setEnabled(true);
         // intBoth.setEnabled(true);
         TN5250jLogFactory.setLogLevels(TN5250jLogger.ERROR);
      }
   }

   private void intFATAL_itemStateChanged(ItemEvent e) {
      if (intFATAL.isSelected()) {
         // intConsole.setEnabled(true);
         // intFile.setEnabled(true);
         // intBoth.setEnabled(true);
         TN5250jLogFactory.setLogLevels(TN5250jLogger.FATAL);
      }
   }

   private void intConsole_itemStateChanged(ItemEvent e) {
   // TODO: Provide the itemstatechanged for the intConsole checkbox
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

   /*
    * ========================================================================
    */
   
   /**
    * Simple data model representing rows within the {@link SessionsTableModel}. 
    */
   private static final class SessionsDataModel {
	   private final String name;
	   private final String host;
	   private final Boolean deflt;
	   
	   public SessionsDataModel(String name, String host, Boolean deflt) {
		   super();
		   this.name = name;
		   this.host = host;
		   this.deflt = deflt;
	   }
   }
   
   /*
    * ========================================================================
    */
   
   /**
    * Table model to show all available sessions,
    * with 'name', 'host' and 'default' column 
    */
    private class SessionsTableModel extends AbstractTableModel implements SortTableModel {
	   
	  private static final long serialVersionUID = 1L;

      private final String[] COLS = { LangTool.getString("conf.tableColA"),
            LangTool.getString("conf.tableColB"),
            LangTool.getString("conf.tableColC") };

      private List<SessionsDataModel> sortedItems = new ArrayList<SessionsDataModel>();
      
      public SessionsTableModel() {
         super();
         resetSorted();
      }

      private void resetSorted() {
         Enumeration<Object> e = props.keys();
         sortedItems.clear();
         String ses = null;
         while (e.hasMoreElements()) {
            ses = (String) e.nextElement();

            if (!ses.startsWith("emul.")) {
                String[] args = new String[TN5250jConstants.NUM_PARMS];
                Configure.parseArgs(props.getProperty(ses), args);
                final Boolean deflt =new Boolean(ses.equals(props.getProperty("emul.default", "")));
                sortedItems.add(new SessionsDataModel(ses, args[0], deflt));
            }
         }

         sortColumn(0, true);
      }

      public boolean isSortable(int col) {
         if (col == 0) return true;
         if (col == 1) return true;
         return false;
      }

      public void sortColumn(final int col, final boolean ascending) {
         if (col == 0) Collections.sort(sortedItems, new Comparator<SessionsDataModel>() {
			public int compare(SessionsDataModel sdm1, SessionsDataModel sdm2) {
				if (ascending) return sdm1.name.compareToIgnoreCase(sdm2.name);
				return sdm2.name.compareToIgnoreCase(sdm1.name);
			}
         });
         if (col == 1) Collections.sort(sortedItems,new Comparator<SessionsDataModel>() {
			public int compare(SessionsDataModel sdm1, SessionsDataModel sdm2) {
				if (ascending) return sdm1.host.compareToIgnoreCase(sdm2.host);
				return sdm2.host.compareToIgnoreCase(sdm1.host);
			}
         });
      }

      public int getColumnCount() {
         return COLS.length;
      }

      public String getColumnName(int col) {
         return COLS[col];
      }

      public int getRowCount() {
         return sortedItems.size();
      }

      /*
       * Implement this so that the default session can be selected.
       */
      public void setValueAt(Object value, int row, int col) {

    	  boolean which = ((Boolean) value).booleanValue();
    	  final String newDefaultSession = sortedItems.get(row).name;
    	  if (which) {
    		  props.setProperty("emul.default", newDefaultSession);
    	  } else {
    		  props.setProperty("emul.default", "");
    	  }
    	  // update internal list of data models
    	  for (int i=0, len=sortedItems.size(); i<len; i++) {
    		  final SessionsDataModel oldsdm = sortedItems.get(i);
    		  if (newDefaultSession.equals(oldsdm.name)) {
    			  sortedItems.set(i, new SessionsDataModel(oldsdm.name, oldsdm.host, (Boolean)value));
    		  } else if (oldsdm.deflt.booleanValue()) {
    			  // clear the old default flag
    			  sortedItems.set(i, new SessionsDataModel(oldsdm.name, oldsdm.host, Boolean.FALSE));
    		  }
    	  }
    	  this.fireTableDataChanged();
      }

      public Object getValueAt(int row, int col) {
    	  switch (col) {
    	  case 0:  return this.sortedItems.get(row).name;
    	  case 1:  return this.sortedItems.get(row).host;
    	  case 2:  return this.sortedItems.get(row).deflt;
    	  default: return null;
    	  }
      }

      /*
       * We need to implement this so that the default session column can
       *    be updated.
       */
      public boolean isCellEditable(int row, int col) {
         //Note that the data/cell address is constant,
         //no matter where the cell appears onscreen.
         if (col == 2) {
            return true;
         }
         return false;
      }

      /*
       * JTable uses this method to determine the default renderer/
       * editor for each cell.  If we didn't implement this method,
       * then the default column would contain text ("true"/"false"),
       * rather than a check box.
       */
      public Class<?> getColumnClass(int c) {
         return getValueAt(0, c).getClass();
      }

      public void addSession() {
         resetSorted();
         fireTableRowsInserted(props.size() - 1, props.size() - 1);
      }

      public void chgSession(int row) {
         resetSorted();
         fireTableRowsUpdated(row, row);
      }

      public void removeSession(int row) {
         resetSorted();
         fireTableRowsDeleted(row, row);
      }

   }
   
   /*
    * ========================================================================
    */
   
   class CustomizedExternalProgram {
	   private String name;
	   private String wCommand;
	   private String uCommand;
	   
	   CustomizedExternalProgram(String name, String wCommand, String uCommand){
		   this.name=name;
		   this.wCommand=wCommand;
		   this.uCommand=uCommand;
	   }

		public String toString(){		
			return this.name;
		}
		
		 /**
		  * @see java.lang.Object#hashCode()
		  */
		public int hashCode() {
			
			return getName().hashCode();
		} 
	   
	   	/**
	   	 * @see java.lang.Object#equals(Object)
	   	 */
	    public boolean equals(Object other) {
	        if ( !(other instanceof CustomizedExternalProgram) ) return false;
			CustomizedExternalProgram castOther = (CustomizedExternalProgram) other;
	        if(this.hashCode()!=castOther.hashCode()) return false;
	        return true;
	    }

		public String getName() {
			return name;
		}
		

		public String getUCommand() {
			return uCommand;
		}
		

		public String getWCommand() {
			return wCommand;
		}
		

   }
   private class CustomizedTableModel extends AbstractTableModel implements
   SortTableModel {
	   private static final long serialVersionUID = 1L;

	final String[] cols = { LangTool.getString("customized.name"),
	      LangTool.getString("customized.window"),
	      LangTool.getString("customized.unix") };
	
	List<CustomizedExternalProgram> mySort = new ArrayList<CustomizedExternalProgram>();
	int sortedColumn = 0;
	boolean isAscending = true;
	
	public CustomizedTableModel() {
	   super();
	   resetSorted();
	}
	
	private void resetSorted() {
	  mySort.clear();
	  
	  String count = etnProps.getProperty("etn.pgm.support.total.num");
	  if(count != null && count.length() > 0){
		int total = Integer.parseInt(count);
		for(int i=1;i<=total;i++){
			String program = etnProps.getProperty("etn.pgm."+i+".command.name");
			String wCommand = etnProps.getProperty("etn.pgm."+i+".command.window");
			String uCommand = etnProps.getProperty("etn.pgm."+i+".command.unix");
			mySort.add(new CustomizedExternalProgram(program,wCommand,uCommand));
		}
	  }
	   
	   sortColumn(sortedColumn, isAscending);
	}
	
	public boolean isSortable(int col) {
	   if (col == 0) return true;
	   return false;
	}
	
	public void sortColumn(int col, boolean ascending) {
	   sortedColumn = col;
	   isAscending = ascending;
	   Collections.sort(mySort, new SessionComparator(ascending));
	}
	
	public int getColumnCount() {
	
	   return cols.length;
	}
	
	public String getColumnName(int col) {
	   return cols[col];
	}
	
	public int getRowCount() {
	   return mySort.size();
	}
	
	/*
	 * Implement this so that the default session can be selected.
	 */
	public void setValueAt(Object value, int row, int col) {
	
	}
	
	public Object getValueAt(int row, int col) {
		CustomizedExternalProgram c = mySort.get(row);
	   if (col == 0)
	      return c.getName();
	   if (col == 1)
	      return c.getWCommand();
	   if (col == 2) {
	      return c.getUCommand();
	   }
	   return null;
	
	}
	
	public boolean isCellEditable(int row, int col) {
      return false;
	}
	
	/*
	 * JTable uses this method to determine the default renderer/
	 * editor for each cell.  If we didn't implement this method,
	 * then the default column would contain text ("true"/"false"),
	 * rather than a check box.
	 */
	public Class<?> getColumnClass(int c) {
	   return getValueAt(0, c).getClass();
	}
		
	public void addSession() {
	   resetSorted();
	   fireTableRowsInserted(mySort.size() - 1, mySort.size() - 1);
	}
	
	public void chgSession(int row) {
	   resetSorted();
	   fireTableRowsUpdated(row, row);
	}
	
	public void removeSession(int row) {
	   resetSorted();
	   fireTableRowsDeleted(row, row);
	}
	
	}
	
	public class SessionComparator implements Comparator<Object> {
		private boolean ascending;

		public SessionComparator(boolean ascending) {
			this.ascending = ascending;
		}

		public int compare(Object one, Object two) {

			// Compare, if used for Strings
			if (one instanceof String && two instanceof String) {

				String s1 = one.toString();
				String s2 = two.toString();
				int result = 0;

				if (ascending)
					result = s1.compareTo(s2);
				else
					result = s2.compareTo(s1);

				if (result < 0) {
					return -1;
				} else if (result > 0) {
					return 1;
				} else
					return 0;
			} 

			// Compare, if used for Booleans
			if (one instanceof Boolean && two instanceof Boolean) {
				boolean bOne = ((Boolean) one).booleanValue();
				boolean bTwo = ((Boolean) two).booleanValue();

				if (ascending) {
					if (bOne == bTwo) {
						return 0;
					} else if (bOne) { // Define false < true
						return 1;
					} else {
						return -1;
					}
				}
				
				if (bOne == bTwo) {
					return 0;
				} else if (bTwo) { // Define false < true
					return 1;
				} else {
					return -1;
				}
				
			} 
			
			// Compare, if used for Compareables
			if (one instanceof Comparable<?> && two instanceof Comparable<?>) {
				Comparable<Object> cOne = (Comparable<Object>) one;
				Comparable<Object> cTwo = (Comparable<Object>) two;
				if (ascending) {
					return cOne.compareTo(two);
				} 
				return cTwo.compareTo(cOne);
			}
			return 1;
			
		}
	}
}
