package org.tn5250j.tools;

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
import org.tn5250j.*;
import org.tn5250j.event.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.filechooser.*;
import javax.swing.border.*;
import java.awt.event.*;
import java.io.*;
import java.beans.*;
import java.text.MessageFormat;
import org.tn5250j.tools.filters.*;
import org.tn5250j.mailtools.SendEMailDialog;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import org.tn5250j.sql.AS400Xtfr;
import org.tn5250j.sql.SqlWizard;

public class XTFRFile extends JDialog implements ActionListener, FTPStatusListener,
                                                   ItemListener {

   FTP5250Prot ftpProtocol;
   AS400Xtfr axtfr;

   JTextField user;
   JPasswordField password;
   JTextField systemName;
   JTextField hostFile;
   JTextField localFile;
   JRadioButton allFields;
   JRadioButton selectedFields;
   JComboBox decimalSeparator;
   JComboBox fileFormat;
   JCheckBox useQuery;
   JButton queryWizard;
   JTextArea queryStatement;
   JButton customize;

   JRadioButton intDesc;
   JRadioButton txtDesc;

   JPanel as400QueryP;
   JPanel as400FieldP;
   JPanel as400p;

   boolean fieldsSelected;
   boolean emailIt;

   tnvt vt;
   XTFRFileFilter htmlFilter;
   XTFRFileFilter KSpreadFilter;
   XTFRFileFilter OOFilter;
   XTFRFileFilter ExcelFilter;
   XTFRFileFilter DelimitedFilter;
   XTFRFileFilter FixedWidthFilter;
//   XTFRFileFilter ExcelWorkbookFilter;

   // default file filter used.
   XTFRFileFilter fileFilter;

   ProgressMonitor pm;
   JProgressBar progressBar;
   JTextArea taskOutput;
   JLabel label;
   JLabel note;
   ProgressOptionPane monitor;
   JDialog dialog;
   XTFRFileFilter filter;

   static String messageProgress;

   public XTFRFile (Frame parent,tnvt pvt) {
      super(parent);
      setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
      vt = pvt;
      ftpProtocol = new FTP5250Prot(vt);
      ftpProtocol.addFTPStatusListener(this);
      axtfr = new AS400Xtfr(vt);
      axtfr.addFTPStatusListener(this);

      createProgressMonitor();
      initFileFilters();
      initXTFRInfo();

      addWindowListener(new WindowAdapter() {

         public void windowClosing(WindowEvent we) {
            if (ftpProtocol.isConnected())
               ftpProtocol.disconnect();
         }

      });

      messageProgress = LangTool.getString("xtfr.messageProgress");
      setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
   }

   private void initFileFilters() {
      htmlFilter = new XTFRFileFilter(new String[] {"html", "htm"}, "Hyper Text Markup Language");
      htmlFilter.setOutputFilterName("org.tn5250j.tools.filters.HTMLOutputFilter");
      KSpreadFilter = new XTFRFileFilter("ksp", "KSpread KDE Spreadsheet");
      KSpreadFilter.setOutputFilterName("org.tn5250j.tools.filters.KSpreadOutputFilter");
      OOFilter = new XTFRFileFilter("sxc", "OpenOffice");
      OOFilter.setOutputFilterName("org.tn5250j.tools.filters.OpenOfficeOutputFilter");
      ExcelFilter = new XTFRFileFilter("xls", "Excel");
      ExcelFilter.setOutputFilterName("org.tn5250j.tools.filters.ExcelOutputFilter");
      DelimitedFilter = new XTFRFileFilter(new String[] {"csv", "tab"}, "Delimited");
      DelimitedFilter.setOutputFilterName("org.tn5250j.tools.filters.DelimitedOutputFilter");
      FixedWidthFilter = new XTFRFileFilter("txt", "Fixed Width");
      FixedWidthFilter.setOutputFilterName("org.tn5250j.tools.filters.FixedWidthOutputFilter");
//      ExcelWorkbookFilter = new XTFRFileFilter("xls", "Excel 95 97 XP 2000");
//      ExcelWorkbookFilter.setOutputFilterName("org.tn5250j.tools.filters.ExcelWorkbookOutputFilter");
   }

   public void statusReceived(FTPStatusEvent statusevent) {

      if (monitor.isCanceled()) {
         ftpProtocol.setAborted();
      }
      else {
         final int prog = statusevent.getCurrentRecord();
         final int len = statusevent.getFileLength();
         Runnable udp = new Runnable () {
            public void run() {

               if (prog >= len) {

                  progressBar.setValue(len);
                  label.setText(LangTool.getString("xtfr.labelComplete"));
                  note.setText(getTransferredNote(len));
                  monitor.setDone();
                  if (emailIt)
                     emailMe();

               }
               else {
                  progressBar.setValue(prog);
                  note.setText(getProgressNote(prog,len));
               }
            }
         };
         SwingUtilities.invokeLater(udp);
      }
   }

   private String getProgressNote(int prog, int len) {

      Object[] args = {Integer.toString(prog),Integer.toString(len)};

      try {
         return MessageFormat.format(messageProgress,args);
      }
      catch (Exception exc) {
         System.out.println(" getProgressNote: " + exc.getMessage());
         return "Record " + prog + " of " + len;
      }
   }

   private void emailMe() {

      SendEMailDialog semd = new SendEMailDialog((Frame)(this.getParent())
                                 ,localFile.getText());

   }

   private String getTransferredNote(int len) {

      Object[] args = {Integer.toString(len)};

      try {
         return MessageFormat.format(
                           LangTool.getString("xtfr.messageTransferred"),
                              args);
      }
      catch (Exception exc) {
         System.out.println(" getTransferredNote: " + exc.getMessage());
         return len + " records transferred!";
      }
   }

   public void commandStatusReceived(FTPStatusEvent statusevent) {
      final String message = statusevent.getMessage() + '\n';
      Runnable cdp = new Runnable () {
         public void run() {
            taskOutput.setText(
               taskOutput.getText() + message);
         }
      };
      SwingUtilities.invokeLater(cdp);


   }

   public void fileInfoReceived(FTPStatusEvent statusevent) {

      hostFile.setText(ftpProtocol.getFullFileName(hostFile.getText()));

      if (allFields.isSelected()) {
         doTransfer();
      }
      else {
         selectFields();
      }
   }

   public void actionPerformed(ActionEvent e) {

      if (e.getActionCommand().equals("XTFR")
                  || e.getActionCommand().equals("EMAIL")) {

         if (e.getActionCommand().equals("EMAIL"))
            emailIt = true;
         else
            emailIt = false;

         initializeMonitor();
         dialog.show();

         if (useQuery.isSelected()) {

            axtfr.login(user.getText(),new String(password.getPassword()));
            // this will execute in it's own thread and will send a
            //    fileInfoReceived(FTPStatusEvent statusevent) event when
            //    finished without an error.
            axtfr.setDecimalChar(getDecimalChar());
            axtfr.connect(systemName.getText());


         }
         else {
            if (ftpProtocol.connect(systemName.getText(),21)) {

               if (ftpProtocol.login(user.getText(),new String(password.getPassword()))) {
                  // this will execute in it's own thread and will send a
                  //    fileInfoReceived(FTPStatusEvent statusevent) event when
                  //    finished without an error.
                  ftpProtocol.setDecimalChar(getDecimalChar());
                  ftpProtocol.getFileInfo(hostFile.getText(),intDesc.isSelected());
               }
            }
            else {

               disconnect();
            }
         }
      }

      if (e.getActionCommand().equals("BROWSEPC")) {

         getPCFile();

      }

      if (e.getActionCommand().equals("CUSTOMIZE")) {

         filter.getOutputFilterInstance().setCustomProperties();

      }

   }

   private char getDecimalChar() {
      String ds = (String)decimalSeparator.getSelectedItem();
      return ds.charAt(1);
   }

   private void initializeMonitor() {

      progressBar.setValue(0);
      progressBar.setMinimum(0);
      progressBar.setMaximum(0);
      label.setText(LangTool.getString("xtfr.labelInProgress"));
      note.setText(LangTool.getString("xtfr.labelFileInfo"));
      progressBar.setStringPainted(false);
      monitor.reset();

   }

   private void disconnect() {

      ftpProtocol.disconnect();
      ftpProtocol = null;

   }

   private void doTransfer() {

      progressBar.setMaximum(ftpProtocol.getFileSize());
      progressBar.setStringPainted(true);

      fileFilter = getFilterByDescription();

      if (useQuery.isSelected()) {

         axtfr.setOutputFilter(fileFilter.getOutputFilterInstance());
         axtfr.getFile(hostFile.getText(),
                              fileFilter.setExtension(localFile.getText()),
                              queryStatement.getText().trim(),
                              intDesc.isSelected());

      }
      else {
         ftpProtocol.setOutputFilter(fileFilter.getOutputFilterInstance());

         ftpProtocol.getFile(hostFile.getText(),
                              fileFilter.setExtension(localFile.getText()));
      }
   }

   private XTFRFileFilter getFilterByExtension() {

      if (filter != null && filter.isExtensionInList(localFile.getText()))
         return filter;

      if (KSpreadFilter.isExtensionInList(localFile.getText()))
         return KSpreadFilter;
      if (OOFilter.isExtensionInList(localFile.getText()))
         return OOFilter;
      if (ExcelFilter.isExtensionInList(localFile.getText()))
         return ExcelFilter;
      if (DelimitedFilter.isExtensionInList(localFile.getText()))
         return DelimitedFilter;
      if (FixedWidthFilter.isExtensionInList(localFile.getText()))
         return FixedWidthFilter;
//      if (ExcelWorkbookFilter.isExtensionInList(localFile.getText()))
//         return ExcelWorkbookFilter;

      return htmlFilter;
   }

   private XTFRFileFilter getFilterByDescription() {

      String desc = (String)fileFormat.getSelectedItem();

//      if (filter.getDescription().equals(desc))
//         return filter;

      if (KSpreadFilter.getDescription().equals(desc))
         return KSpreadFilter;
      if (OOFilter.getDescription().equals(desc))
         return OOFilter;
      if (ExcelFilter.getDescription().equals(desc))
         return ExcelFilter;
      if (DelimitedFilter.getDescription().equals(desc))
         return DelimitedFilter;
      if (FixedWidthFilter.getDescription().equals(desc))
         return FixedWidthFilter;
//      if (ExcelWorkbookFilter.isExtensionInList(localFile.getText()))
//         return ExcelWorkbookFilter;

      return htmlFilter;
   }

   private void createProgressMonitor() {

      progressBar = new JProgressBar(0, 0);
      progressBar.setValue(0);

      taskOutput = new JTextArea(5, 20);
      taskOutput.setMargin(new Insets(5,5,5,5));
      taskOutput.setEditable(false);

      JPanel panel = new JPanel();
      note = new JLabel();
      note.setForeground(Color.blue);
      label = new JLabel();
      label.setForeground(Color.blue);
      panel.setLayout(new BorderLayout());
      panel.add(label,BorderLayout.NORTH);
      panel.add(note,BorderLayout.CENTER);
      panel.add(progressBar,BorderLayout.SOUTH);

      JPanel contentPane = new JPanel();
      contentPane.setLayout(new BorderLayout());
      contentPane.add(panel, BorderLayout.NORTH);
      contentPane.add(new JScrollPane(taskOutput), BorderLayout.CENTER);

      monitor = new ProgressOptionPane(contentPane);

      taskOutput.setRows(6);

      dialog = monitor.createDialog(this,LangTool.getString("xtfr.progressTitle"));


   }

   private void startWizard() {

      try {
         SqlWizard wizard = new SqlWizard(systemName.getText().trim(),
                                          user.getText(),
                                          new String(password.getPassword()));

         wizard.setQueryTextArea(queryStatement);
      }
      catch (NoClassDefFoundError ncdfe) {
         JOptionPane.showMessageDialog(this,
                                       LangTool.getString("messages.noAS400Toolbox"),
                                       "Error",
                                       JOptionPane.ERROR_MESSAGE,null);
      }
      catch (Exception e) {
         System.out.println(e.getMessage());
      }
   }

   /**
    * Get the local file from a file chooser
    */
   private void getPCFile() {

      String workingDir = System.getProperty("user.dir");
      MyFileChooser pcFileChooser = new MyFileChooser(workingDir);

      // set the file filters for the file chooser
      filter = getFilterByDescription();

      pcFileChooser.addChoosableFileFilter(filter);

      int ret = pcFileChooser.showSaveDialog(this);

      // check to see if something was actually chosen
      if (ret == JFileChooser.APPROVE_OPTION) {
         File file = pcFileChooser.getSelectedFile();
         filter = null;
         if (pcFileChooser.getFileFilter() instanceof XTFRFileFilter )
            filter = (XTFRFileFilter)pcFileChooser.getFileFilter();
         else
            filter = htmlFilter;

         localFile.setText(filter.setExtension(file));

      }

   }

   /**
    * Creates the dialog components for prompting the user for the information
    * of the transfer
    */
   private void initXTFRInfo() {

      // create some reusable borders and layouts
      BorderLayout borderLayout = new BorderLayout();
      Border etchedBorder = BorderFactory.createEtchedBorder();

      // main panel
      JPanel mp = new JPanel();
      mp.setLayout(borderLayout);

      // system panel
      JPanel sp = new JPanel();
      sp.setLayout(new BorderLayout());
      sp.setBorder(etchedBorder);

      // host panel for as400
      as400p = new JPanel();
      as400p.setBorder(BorderFactory.createTitledBorder(
                                    LangTool.getString("xtfr.labelAS400")));

      as400p.setLayout(new BorderLayout());

      JPanel as400HostP = new JPanel();

      AlignLayout as400pLayout = new AlignLayout();
      as400HostP.setLayout(as400pLayout);

      // system name panel
      JLabel snpLabel = new JLabel(LangTool.getString("xtfr.labelSystemName"));

      systemName = new JTextField(vt.getHostName());
      systemName.setColumns(30);

      as400HostP.add(snpLabel);
      as400HostP.add(systemName);

      // host file name panel
      JLabel hfnpLabel = new JLabel(LangTool.getString("xtfr.labelHostFile"));
      hostFile = new JTextField();
      hostFile.setColumns(30);

      as400HostP.add(hfnpLabel);
      as400HostP.add(hostFile);

      // user id panel
      JLabel idpLabel = new JLabel(LangTool.getString("xtfr.labelUserId"));

      user = new JTextField();
      user.setColumns(15);
      as400HostP.add(idpLabel);
      as400HostP.add(user);

      // password panel
      JLabel pwpLabel = new JLabel(LangTool.getString("xtfr.labelPassword"));

      password = new JPasswordField();
      password.setColumns(15);

      as400HostP.add(pwpLabel);
      as400HostP.add(password);

      // Query Wizard
      useQuery = new JCheckBox(LangTool.getString("xtfr.labelUseQuery"));
      queryWizard = new JButton(LangTool.getString("xtfr.labelQueryWizard"));

      queryWizard.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e) {
            startWizard();
         }
      });

      queryWizard.setEnabled(false);

      useQuery.addItemListener(this);

      as400HostP.add(useQuery);
      as400HostP.add(queryWizard);

      as400p.add(as400HostP,BorderLayout.CENTER);

      as400QueryP = new JPanel();
      as400QueryP.setLayout(new BorderLayout());

      queryStatement = new JTextArea(5,40);
      JScrollPane scrollPane = new JScrollPane(queryStatement);
      queryStatement.setLineWrap(true);
      as400QueryP.add(scrollPane,BorderLayout.CENTER);

      as400FieldP = new JPanel();

      AlignLayout as400fLayout = new AlignLayout();
      as400FieldP.setLayout(as400fLayout);

      // Field Selection panel
      JLabel fieldsLabel = new JLabel(LangTool.getString("xtfr.labelFields"));

      allFields = new JRadioButton(LangTool.getString("xtfr.labelAllFields"));
      allFields.setSelected(true);
      selectedFields = new JRadioButton(LangTool.getString("xtfr.labelSelectedFields"));
      JPanel fgPanel = new JPanel();
      ButtonGroup fieldGroup = new ButtonGroup();

      fieldGroup.add(allFields);
      fieldGroup.add(selectedFields);

      as400FieldP.add(fieldsLabel);
      fgPanel.add(allFields);
      fgPanel.add(selectedFields);
      as400FieldP.add(fgPanel);

      // Field Text Description panel
      JLabel textDescLabel = new JLabel(LangTool.getString("xtfr.labelTxtDesc"));

      txtDesc = new JRadioButton(LangTool.getString("xtfr.labelTxtDescFull"));
      txtDesc.setSelected(true);
      intDesc = new JRadioButton(LangTool.getString("xtfr.labelTxtDescInt"));
      JPanel tdPanel = new JPanel();
      ButtonGroup txtDescGroup = new ButtonGroup();

      txtDescGroup.add(txtDesc);
      txtDescGroup.add(intDesc);

      as400FieldP.add(textDescLabel);
      tdPanel.add(txtDesc);
      tdPanel.add(intDesc);
      as400FieldP.add(tdPanel);

      as400p.add(as400HostP,BorderLayout.CENTER);
      as400p.add(as400FieldP,BorderLayout.SOUTH);

      // pc panel for pc information
      JPanel pcp = new JPanel();
      pcp.setBorder(BorderFactory.createTitledBorder(
                                    LangTool.getString("xtfr.labelpc")));

      AlignLayout pcLayout = new AlignLayout(3,5,5);

      pcp.setLayout(pcLayout);

      JLabel pffLabel = new JLabel(LangTool.getString("xtfr.labelFileFormat"));
      fileFormat = new JComboBox();

      fileFormat.addItem(htmlFilter.getDescription());
      fileFormat.addItem(OOFilter.getDescription());
      fileFormat.addItem(ExcelFilter.getDescription());
      fileFormat.addItem(KSpreadFilter.getDescription());
      fileFormat.addItem(DelimitedFilter.getDescription());
      fileFormat.addItem(FixedWidthFilter.getDescription());

      fileFormat.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            JComboBox cb = (JComboBox)e.getSource();
            filter = getFilterByDescription();
            if (filter.getOutputFilterInstance().isCustomizable())
               customize.setEnabled(true);
            else
               customize.setEnabled(false);
         }
      });

      customize = new JButton(LangTool.getString("xtfr.labelCustomize"));
      customize.setActionCommand("CUSTOMIZE");
      customize.addActionListener(this);

      // now make sure we set the customizable button enabled or not
      // depending on the filter.
      fileFormat.setSelectedIndex(0);

      pcp.add(pffLabel);
      pcp.add(fileFormat);
      pcp.add(customize);

      JLabel pcpLabel = new JLabel(LangTool.getString("xtfr.labelPCFile"));

      localFile = new JTextField();
      localFile.setColumns(30);

      JButton browsePC = new JButton(LangTool.getString("xtfr.labelPCBrowse"));
      browsePC.setActionCommand("BROWSEPC");
      browsePC.addActionListener(this);

      pcp.add(pcpLabel);
      pcp.add(localFile);
      pcp.add(browsePC);

      decimalSeparator = new JComboBox();
      decimalSeparator.addItem(LangTool.getString("xtfr.period"));
      decimalSeparator.addItem(LangTool.getString("xtfr.comma"));

      // obtain the decimal separator for the machine locale
      DecimalFormat formatter =
            (DecimalFormat)NumberFormat.getInstance(Locale.getDefault()) ;

      if (formatter.getDecimalFormatSymbols().getDecimalSeparator() == '.')
         decimalSeparator.setSelectedIndex(0);
      else
         decimalSeparator.setSelectedIndex(1);

      pcp.add(new JLabel(LangTool.getString("xtfr.labelDecimal")));
      pcp.add(decimalSeparator);

      sp.add(as400p,BorderLayout.NORTH);
      sp.add(pcp,BorderLayout.SOUTH);

      // options panel
      JPanel op = new JPanel();
      JButton xtfrButton = new JButton(LangTool.getString("xtfr.labelXTFR"));
      xtfrButton.addActionListener(this);
      xtfrButton.setActionCommand("XTFR");
      op.add(xtfrButton);

      JButton emailButton = new JButton(LangTool.getString("xtfr.labelXTFREmail"));
      emailButton.addActionListener(this);
      emailButton.setActionCommand("EMAIL");
      op.add(emailButton);

      mp.add(sp,BorderLayout.CENTER);
      mp.add(op,BorderLayout.SOUTH);

      this.getContentPane().add(mp,BorderLayout.CENTER);

      this.setModal(false);
      this.setTitle(LangTool.getString("xtfr.title"));


      // pack it and center it on the screen
      pack();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = getSize();
      if (frameSize.height > screenSize.height)
         frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
         frameSize.width = screenSize.width;
      setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

      // now show the world what we can do
      show();

   }

   /** Listens to the use query check boxe */
   public void itemStateChanged(ItemEvent e) {
      Object source = e.getItemSelectable();
      if (source == useQuery) {
         if (useQuery.isSelected()) {
            queryWizard.setEnabled(true);
            as400p.remove(as400FieldP);
            as400p.add(as400QueryP,BorderLayout.SOUTH);
         }
         else {
            queryWizard.setEnabled(false);
            as400p.remove(as400QueryP);
            as400p.add(as400FieldP,BorderLayout.SOUTH);
         }
         this.validate();
         this.repaint();
      }
   }

   private void selectFields() {

      FFDTableModel ffdtm = new FFDTableModel();

      //Create table to hold field data
      JTable fields = new JTable(ffdtm);
      fields.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      fields.setPreferredScrollableViewportSize(new Dimension(500,200));

      //Create the scroll pane and add the table to it.
      JScrollPane scrollPane = new JScrollPane(fields);
      scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

      JPanel jpm = new JPanel();
      jpm.add(scrollPane);

      Object[]      message = new Object[1];
      message[0] = jpm;
      String[] options = {LangTool.getString("xtfr.tableSelectAll"),
                           LangTool.getString("xtfr.tableSelectNone"),
                           LangTool.getString("xtfr.tableDone")
                          };

      int result = 0;
      while (result != 2) {
         result = JOptionPane.showOptionDialog(
             null,                              // the parent that the dialog blocks
             message,                           // the dialog message array
             LangTool.getString("xtfr.titleFieldSelection"),    // the title of the dialog window
             JOptionPane.DEFAULT_OPTION,        // option type
             JOptionPane.PLAIN_MESSAGE,   // message type
             null,                              // optional icon, use null to use the default icon
             options,                           // options string array, will be made into buttons//
             options[1]                         // option that should be made into a default button
         );

         switch(result) {
            case 0: // Select all
               ftpProtocol.selectAll();
               break;
            case 1: // Select none
               ftpProtocol.selectNone();
               break;
            default:
               fieldsSelected = ftpProtocol.isFieldsSelected();
               if (ftpProtocol.isFieldsSelected())
                  doTransfer();
               break;
         }
      }
   }

   /**
    * Table model for File Field Definitions
    */
   class FFDTableModel extends AbstractTableModel {

      final String[] cols = {
                  LangTool.getString("xtfr.tableColA"),
                  LangTool.getString("xtfr.tableColB")};

      public FFDTableModel() {
        super();

      }

      public int getColumnCount() {

        return cols.length;
      }

      public String getColumnName(int col) {
        return cols[col];
      }

      public int getRowCount() {

        return ftpProtocol.getNumberOfFields();
      }

      public Object getValueAt(int row, int col) {

         if (col == 0) {

            return new Boolean(ftpProtocol.isFieldSelected(row));

         }
         if (col == 1)
            return ftpProtocol.getFieldName(row);

        return null;

      }

      public Class getColumnClass(int col) {
         return getValueAt(0,col).getClass();

      }

      public boolean isCellEditable(int row,int col) {
         if (col == 0)
            return true;
         else
            return false;

      }

      public void setValueAt(Object value, int row, int col) {

         fireTableCellUpdated(row,col);
         ftpProtocol.setFieldSelected(row,((Boolean)value).booleanValue());

      }
   }

   /**
    * Create a option pane to show status of the transfer
    */
   private class ProgressOptionPane extends JOptionPane {

      ProgressOptionPane(Object messageList) {

         super(messageList,
               JOptionPane.INFORMATION_MESSAGE,
               JOptionPane.DEFAULT_OPTION,
               null,
               new Object[] {UIManager.getString("OptionPane.cancelButtonText")},
               null);
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      }

      public void setDone() {
         Object[] option = this.getOptions();
         option[0] = LangTool.getString("xtfr.tableDone");
         this.setOptions(option);
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }

      public void reset() {

         Object[] option = this.getOptions();
         option[0] = UIManager.getString("OptionPane.cancelButtonText");
         this.setOptions(option);
         monitor.setValue(null);

      }

      public int getMaxCharactersPerLineCount() {
         return 60;
      }

      /**
       * Returns true if the user hits the Cancel button in the progress dialog.
       *
       * @return whether or not dialog was cancelled
       */
      public boolean isCanceled() {
        if (this == null) return false;
        Object v = this.getValue();
        return (v != null);
      }

      // Equivalent to JOptionPane.createDialog,
      // but create a modeless dialog.
      // This is necessary because the Solaris implementation doesn't
      // support Dialog.setModal yet.
      public JDialog createDialog(Component parentComponent, String title) {

         Frame frame = JOptionPane.getFrameForComponent(parentComponent);
         final JDialog dialog = new JDialog(frame, title, false);
         Container contentPane = dialog.getContentPane();

         contentPane.setLayout(new BorderLayout());
         contentPane.add(this, BorderLayout.CENTER);
         dialog.pack();
         dialog.setLocationRelativeTo(parentComponent);
         dialog.addWindowListener(new WindowAdapter() {
             boolean gotFocus = false;

             public void windowClosing(WindowEvent we) {
                 setValue(null);
             }

             public void windowActivated(WindowEvent we) {
                 // Once window gets focus, set initial focus
                 if (!gotFocus) {
                     selectInitialValue();
                     gotFocus = true;
                 }
             }
         });

         addPropertyChangeListener(new PropertyChangeListener() {
             public void propertyChange(PropertyChangeEvent event) {
                 if(dialog.isVisible() &&
                     event.getSource() == ProgressOptionPane.this &&
                     (event.getPropertyName().equals(VALUE_PROPERTY) ||
                        event.getPropertyName().equals(INPUT_VALUE_PROPERTY))){
                     ftpProtocol.setAborted();
                     dialog.setVisible(false);
                     dialog.dispose();
                 }
             }
         });
         return dialog;
      }
    }

    /**
     * This is to fix
     * Bug Id - 4416982
     * Synopsis JFileChooser does not use its resources to size itself initially
     */
   class MyFileChooser extends JFileChooser {
      MyFileChooser(String dir) {
         super(dir);
      }

      public Dimension getPreferredSize() {
         return getLayout().preferredLayoutSize(this);
      }
   }

}
