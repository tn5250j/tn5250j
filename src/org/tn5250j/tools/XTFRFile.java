package org.tn5250j.tools;

/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.1
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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class XTFRFile extends JDialog implements ActionListener, FTPStatusListener {

   FTP5250Prot ftpProtocol;

   JTextField user;
   JPasswordField password;
   JTextField systemName;
   JTextField hostFile;
   JTextField localFile;
   JRadioButton allFields;
   JRadioButton selectedFields;

   JRadioButton intDesc;
   JRadioButton txtDesc;

   boolean fieldsSelected;
   tnvt vt;
   XTFRFileFilter htmlFilter;
   XTFRFileFilter KSpreadFilter;
   XTFRFileFilter OOFilter;
   XTFRFileFilter ExcelFilter;

   // default file filter used.
   XTFRFileFilter fileFilter;

   ProgressMonitor pm;
   JProgressBar progressBar;
   JTextArea taskOutput;
   JLabel label;
   JLabel note;
   ProgressOptionPane monitor;
   JDialog dialog;
   JComboBox decimalSeparator;

   static String messageProgress;

   public XTFRFile (Frame parent,tnvt pvt) {
      super(parent);
      vt = pvt;
      ftpProtocol = new FTP5250Prot(vt);
      ftpProtocol.addFTPStatusListener(this);
      createProgressMonitor();
      getXTFRInfo();

      addWindowListener(new WindowAdapter() {

         public void windowClosing(WindowEvent we) {
            if (ftpProtocol.isConnected())
               ftpProtocol.disconnect();
         }

      });
      setFileFilters();

      messageProgress = LangTool.getString("xtfr.messageProgress");
   }

   private void setFileFilters() {
      htmlFilter = new XTFRFileFilter(new String[] {"html", "htm"}, "Hyper Text Markup Language");
      htmlFilter.setOutputFilterName("org.tn5250j.tools.filters.HTMLOutputFilter");
      KSpreadFilter = new XTFRFileFilter("ksp", "KSpread KDE Spreadsheet");
      KSpreadFilter.setOutputFilterName("org.tn5250j.tools.filters.KSpreadOutputFilter");
      OOFilter = new XTFRFileFilter("sxc", "OpenOffice 6");
      OOFilter.setOutputFilterName("org.tn5250j.tools.filters.OpenOfficeOutputFilter");
      ExcelFilter = new XTFRFileFilter("xls", "Excel");
      ExcelFilter.setOutputFilterName("org.tn5250j.tools.filters.ExcelOutputFilter");
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

      if (allFields.isSelected()) {
         doTransfer();
      }
      else {
         selectFields();
      }
   }

   public void actionPerformed(ActionEvent e) {

      if (e.getActionCommand().equals("XTFR")) {

         initializeMonitor();
         dialog.show();

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

      if (e.getActionCommand().equals("BROWSEPC")) {

         getPCFile();

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
      monitor.setValue(null);

   }

   private void disconnect() {

      ftpProtocol.disconnect();
      ftpProtocol = null;

   }

   private void doTransfer() {

      progressBar.setMaximum(ftpProtocol.getFileSize());
      progressBar.setStringPainted(true);

      fileFilter = getFilterByExtension();

      ftpProtocol.setOutputFilter(fileFilter.getOutputFilterInstance());
      ftpProtocol.getFile(hostFile.getText(),
                           fileFilter.setExtension(localFile.getText()));

   }

   private XTFRFileFilter getFilterByExtension() {

      if (KSpreadFilter.isExtensionInList(localFile.getText()))
         return KSpreadFilter;
      if (OOFilter.isExtensionInList(localFile.getText()))
         return OOFilter;
      if (ExcelFilter.isExtensionInList(localFile.getText()))
         return ExcelFilter;

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

      Object[] cancelOption = new Object[1];
      cancelOption[0] = UIManager.getString("OptionPane.cancelButtonText");

      monitor = new ProgressOptionPane(contentPane,cancelOption);
//      taskOutput.setColumns(monitor.getMaxCharactersPerLineCount());
      taskOutput.setRows(6);

      dialog = monitor.createDialog(this,LangTool.getString("xtfr.progressTitle"));


   }

   /**
    * Get the local file from a file chooser
    */
   private void getPCFile() {

      String workingDir = System.getProperty("user.dir");
      MyFileChooser pcFileChooser = new MyFileChooser(workingDir);

      // set the file filters for the file chooser
      pcFileChooser.addChoosableFileFilter(ExcelFilter);
      pcFileChooser.addChoosableFileFilter(KSpreadFilter);
      pcFileChooser.addChoosableFileFilter(OOFilter);
      pcFileChooser.addChoosableFileFilter(htmlFilter);

      int ret = pcFileChooser.showSaveDialog(this);

      // check to see if something was actually chosen
      if (ret == JFileChooser.APPROVE_OPTION) {
         File file = pcFileChooser.getSelectedFile();
         XTFRFileFilter filter = null;
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
   private void getXTFRInfo() {

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
      JPanel as400p = new JPanel();
      as400p.setBorder(BorderFactory.createTitledBorder(
                                    LangTool.getString("xtfr.labelAS400")));

      AlignLayout as400pLayout = new AlignLayout();
      as400p.setLayout(as400pLayout);

      // system name panel
      JLabel snpLabel = new JLabel(LangTool.getString("xtfr.labelSystemName"));

      systemName = new JTextField(vt.getHostName());
      systemName.setColumns(30);

      as400p.add(snpLabel);
      as400p.add(systemName);

      // host file name panel
      JLabel hfnpLabel = new JLabel(LangTool.getString("xtfr.labelHostFile"));
      hostFile = new JTextField();
      hostFile.setColumns(30);

      as400p.add(hfnpLabel);
      as400p.add(hostFile);

      // user id panel
      JLabel idpLabel = new JLabel(LangTool.getString("xtfr.labelUserId"));

      user = new JTextField();
      user.setColumns(15);
      as400p.add(idpLabel);
      as400p.add(user);

      // password panel
      JLabel pwpLabel = new JLabel(LangTool.getString("xtfr.labelPassword"));

      password = new JPasswordField();
      password.setColumns(15);

      as400p.add(pwpLabel);
      as400p.add(password);

      // Field Selection panel
      JLabel fieldsLabel = new JLabel(LangTool.getString("xtfr.labelFields"));

      allFields = new JRadioButton(LangTool.getString("xtfr.labelAllFields"));
      allFields.setSelected(true);
      selectedFields = new JRadioButton(LangTool.getString("xtfr.labelSelectedFields"));
      JPanel fgPanel = new JPanel();
      ButtonGroup fieldGroup = new ButtonGroup();

      fieldGroup.add(allFields);
      fieldGroup.add(selectedFields);

      as400p.add(fieldsLabel);
      fgPanel.add(allFields);
      fgPanel.add(selectedFields);
      as400p.add(fgPanel);

      // Field Text Description panel
      JLabel textDescLabel = new JLabel(LangTool.getString("xtfr.labelTxtDesc"));

      txtDesc = new JRadioButton(LangTool.getString("xtfr.labelTxtDescFull"));
      txtDesc.setSelected(true);
      intDesc = new JRadioButton(LangTool.getString("xtfr.labelTxtDescInt"));
      JPanel tdPanel = new JPanel();
      ButtonGroup txtDescGroup = new ButtonGroup();

      txtDescGroup.add(txtDesc);
      txtDescGroup.add(intDesc);

      as400p.add(textDescLabel);
      tdPanel.add(txtDesc);
      tdPanel.add(intDesc);
      as400p.add(tdPanel);

      // pc panel for pc information
      JPanel pcp = new JPanel();
      pcp.setBorder(BorderFactory.createTitledBorder(
                                    LangTool.getString("xtfr.labelpc")));
      pcp.setLayout(new BorderLayout());

      // panel for pc file information
      JPanel pcf = new JPanel();

      JLabel pcpLabel = new JLabel(LangTool.getString("xtfr.labelPCFile"));
      localFile = new JTextField();
      localFile.setColumns(30);

      JButton browsePC = new JButton(LangTool.getString("xtfr.labelPCBrowse"));
      browsePC.setActionCommand("BROWSEPC");
      browsePC.addActionListener(this);


      pcf.add(pcpLabel);
      pcf.add(localFile);
      pcf.add(browsePC);

      // panel for decimal separator information
      JPanel pcd = new JPanel();
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

//      JLabel pcpLabel = new JLabel(LangTool.getString("xtfr.labelPCFile"));
      pcd.add(new JLabel(LangTool.getString("xtfr.labelDecimal")));
      pcd.add(decimalSeparator);

      pcp.add(pcf,BorderLayout.NORTH);
      pcp.add(pcd,BorderLayout.SOUTH);

      sp.add(as400p,BorderLayout.NORTH);
      sp.add(pcp,BorderLayout.SOUTH);

      // options panel
      JPanel op = new JPanel();
      JButton xtfrButton = new JButton(LangTool.getString("xtfr.labelXTFR"));
      xtfrButton.addActionListener(this);
      xtfrButton.setActionCommand("XTFR");
      op.add(xtfrButton);

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

      ProgressOptionPane(Object messageList,Object[] cancelOption) {
         super(messageList,
               JOptionPane.INFORMATION_MESSAGE,
               JOptionPane.DEFAULT_OPTION,
               null,
               cancelOption,
               null);
      }


      public int getMaxCharactersPerLineCount() {
         return 60;
      }

      /**
       * Returns true if the user hits the Cancel button in the progress dialog.
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
