package org.tn5250j.spoolfile;

/**
 * Title: SpoolExportWizard.java
 * Copyright:   Copyright (c) 2002
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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.tn5250j.SessionPanel;
import org.tn5250j.event.WizardEvent;
import org.tn5250j.event.WizardListener;
import org.tn5250j.gui.GenericTn5250JFrame;
import org.tn5250j.gui.TN5250jFileChooser;
import org.tn5250j.gui.Wizard;
import org.tn5250j.gui.WizardPage;
import org.tn5250j.mailtools.SendEMailDialog;
import org.tn5250j.tools.AlignLayout;
import org.tn5250j.tools.LangTool;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFileOutputStream;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.PrintObjectTransformedInputStream;
import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.vaccess.IFSFileDialog;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

/**
 *
 */
public class SpoolExportWizard extends GenericTn5250JFrame implements WizardListener {

   private static final long serialVersionUID = 1L;
JPanel contentPane;
   JLabel statusBar = new JLabel();

   JPanel spoolPanel = new JPanel();
   JPanel spoolData = new JPanel();
   JPanel spoolOptions = new JPanel();

   JPanel destPanel = new JPanel();
   JLabel labelSpooledFile = new JLabel();
   JLabel spooledFile = new JLabel();
   JLabel labelJobName = new JLabel();
   JLabel jobName = new JLabel();
   JLabel labelUser = new JLabel();
   JLabel user = new JLabel();
   JLabel labelNumber = new JLabel();
   JLabel number = new JLabel();
   JLabel labelFileNumber = new JLabel();
   JLabel spooledFileNumber = new JLabel();
   JLabel labelSystem = new JLabel();
   JLabel systemName = new JLabel();
   JLabel labelPages = new JLabel();
   JLabel pages = new JLabel();

   JComboBox cvtType;
   JTextField pcPathInfo;
   JTextField ifsPathInfo;
   JButton pcSave;
   JButton ifsSave;

   JRadioButton pc;
   JRadioButton ifs;
   JRadioButton email;

   // PDF Properties
   JTextField title;
   JTextField subject;
   JTextField author;

   // PDF Options
   JTextField fontSize;
   JComboBox pageSize;
   JRadioButton portrait;
   JRadioButton landscape;

   // Text Options
   JCheckBox openAfter;
   JTextField editor;
   JButton getEditor;

   // Spooled File
   SpooledFile splfile;

   // Session object
   SessionPanel session;

   JPanel twoPDF;
   JPanel twoText;

   // Wizard
   Wizard wizard;
   WizardPage page;
   WizardPage pagePDF;
   WizardPage pageText;
   JButton nextButton;

   // pdf variables
   private PdfWriter bos;
   private Document document;
   private com.lowagie.text.Font font;

   // output stream
   private FileOutputStream fw;
   private IFSFileOutputStream ifsfw;

   // conical path of file
   private String conicalPath;

   // exporting worker thread
   private Thread workingThread;

   //Construct the frame
   public SpoolExportWizard(SpooledFile splfile, SessionPanel session) {

      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      this.splfile = splfile;
      this.session = session;

      try {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   //Component initialization
   private void jbInit() throws Exception  {

      // create ourselves a new wizard
      wizard = new Wizard();

      // create the event handler as being this module
      wizard.addWizardListener(this);

      // add our wizard to the frame
      this.getContentPane().add(wizard);

      // create the first wizard page
      page = new WizardPage(WizardPage.NEXT |
                       WizardPage.FINISH |
                       WizardPage.CANCEL |
                       WizardPage.HELP);

      page.setName(LangTool.getString("spool.titlePage1"));

      setTitle(page.getName());

      // get the next button so we can set it enabled or disabled depending
      // on output type.
      nextButton = page.getNextButton();

      page.getContentPane().add(pageOne(), BorderLayout.CENTER);

      wizard.add(page);

      pagePDF = new WizardPage(WizardPage.PREVIOUS |
                       WizardPage.FINISH |
                       WizardPage.CANCEL |
                       WizardPage.HELP);
      pagePDF.setName(LangTool.getString("spool.titlePage2PDF"));

      pagePDF.getContentPane().add(pageTwoPDF(), BorderLayout.CENTER);
      wizard.add(pagePDF);

      pageText = new WizardPage(WizardPage.PREVIOUS |
                       WizardPage.FINISH |
                       WizardPage.CANCEL |
                       WizardPage.HELP);
      pageText.setName(LangTool.getString("spool.titlePage2Txt"));

      pageText.getContentPane().add(pageTwoText(), BorderLayout.CENTER);
      wizard.add(pageText);

      pack();

      //Center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = getSize();
      if (frameSize.height > screenSize.height)
         frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
         frameSize.width = screenSize.width;

      setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height) / 2);

   }

   /**
    * Create the second page of the wizard pages for PDF
    *
    * @return
    */
   private JPanel pageTwoPDF () {

      twoPDF = new JPanel();

      twoPDF.setLayout(new BorderLayout());

      JPanel docProps = new JPanel();

      docProps.setBorder(BorderFactory.createTitledBorder(
                           LangTool.getString("spool.labelProps")));

      docProps.setLayout(new AlignLayout(2,5,5));

      docProps.add(new JLabel(LangTool.getString("spool.labelPropsTitle")));
      docProps.add(title = new JTextField(40));
      docProps.add(new JLabel(LangTool.getString("spool.labelPropsSubject")));
      docProps.add(subject = new JTextField(40));
      docProps.add(new JLabel(LangTool.getString("spool.labelPropsAuthor")));
      docProps.add(author = new JTextField(40));

      JPanel options = new JPanel();

      options.setBorder(BorderFactory.createTitledBorder(
                           LangTool.getString("spool.labelOpts")));
      options.setLayout(new AlignLayout(2,5,5));

      options.add(new JLabel(LangTool.getString("spool.labelOptsFontSize")));
      options.add(fontSize = new JTextField(5));

      options.add(new JLabel(LangTool.getString("spool.labelOptsPageSize")));
      options.add(pageSize = new JComboBox());

      pageSize.addItem("A3");
      pageSize.addItem("A4");
      pageSize.addItem("A5");
      pageSize.addItem("LETTER");
      pageSize.addItem("LEGAL");
      pageSize.addItem("LEDGER");

      options.add(portrait =
               new JRadioButton(LangTool.getString("spool.labelOptsPortrait")));
      options.add(landscape =
               new JRadioButton(LangTool.getString("spool.labelOptsLandscape")));

      ButtonGroup orientation = new ButtonGroup();
      orientation.add(portrait);
      orientation.add(landscape);

      landscape.setSelected(true);

      twoPDF.add(docProps,BorderLayout.NORTH);
      twoPDF.add(options,BorderLayout.CENTER);

      return twoPDF;
   }

   /**
    * Create the second page of the wizard pages for Text
    *
    * @return
    */
   private JPanel pageTwoText () {

      twoText = new JPanel();

      twoText.setLayout(new BorderLayout());

      JPanel textProps = new JPanel();

      textProps.setBorder(BorderFactory.createTitledBorder(
                           LangTool.getString("spool.labelTextProps")));

      textProps.setLayout(new AlignLayout(2,5,5));

      textProps.add(openAfter =
                  new JCheckBox(LangTool.getString("spool.labelUseExternal")));
      textProps.add(new JLabel());
      textProps.add(editor = new JTextField(30));
      getEditor = new JButton("Browse");

      getEditor.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e) {
            getEditor();
         }
      });
      textProps.add(getEditor);

      // see if we have an external viewer defined and if we use it or not
      if (session.getSession().getConfiguration().isPropertyExists("useExternal"))
         openAfter.setEnabled(true);

      if (session.getSession().getConfiguration().isPropertyExists("externalViewer"))
         editor.setText(session.getSession().getConfiguration().getStringProperty("externalViewer"));

      twoText.add(textProps,BorderLayout.CENTER);

      return twoText;
   }

   /**
    * Create the first page of the export wizard
    *
    * @return
    * @throws Exception
    */
   private JPanel pageOne () throws Exception {

      contentPane = new JPanel();

      contentPane.setLayout(new BorderLayout());
      statusBar.setText(" ");
      statusBar.setBorder(BorderFactory.createEtchedBorder());

      spoolPanel.setLayout(new BorderLayout());

      contentPane.add(spoolPanel, BorderLayout.CENTER);
      contentPane.add(statusBar, BorderLayout.SOUTH);

      // create the labels to be used for the spooled file data
      labelSpooledFile.setText(LangTool.getString("spool.labelSpooledFile"));
      labelJobName.setText(LangTool.getString("spool.labelJobName"));
      labelUser.setText(LangTool.getString("spool.labelJobUser"));
      labelNumber.setText(LangTool.getString("spool.labelJobNumber"));
      labelFileNumber.setText(LangTool.getString("spool.labelSpoolNumber"));
      labelSystem.setText(LangTool.getString("spool.labelSystem"));
      labelPages.setText(LangTool.getString("spool.labelPages"));

      spoolData.setLayout(new AlignLayout(2,5,5));
      spoolData.setBorder(BorderFactory.createTitledBorder(
                                       LangTool.getString("spool.labelSpoolInfo")));

      // create the data fields to be used for the spooled file data
      spooledFile.setText(splfile.getName());
      jobName.setText(splfile.getJobName());
      user.setText(splfile.getJobUser());
      spooledFileNumber.setText(Integer.toString(splfile.getNumber()));
      number.setText(splfile.getJobNumber());
      systemName.setText(splfile.getSystem().getSystemName());
      pages.setText(splfile.getIntegerAttribute(SpooledFile.ATTR_PAGES).toString());

      spoolData.add(labelSystem, null);
      spoolData.add(systemName, null);
      spoolData.add(labelSpooledFile, null);
      spoolData.add(spooledFile, null);
      spoolData.add(labelJobName, null);
      spoolData.add(jobName, null);
      spoolData.add(labelUser, null);
      spoolData.add(user, null);
      spoolData.add(labelNumber, null);
      spoolData.add(number, null);
      spoolData.add(labelFileNumber, null);
      spoolData.add(spooledFileNumber, null);
      spoolData.add(labelPages, null);
      spoolData.add(pages, null);

      spoolPanel.add(spoolOptions,  BorderLayout.SOUTH);

      // set the spool export panel
      spoolPanel.add(spoolData,  BorderLayout.CENTER);

      spoolOptions.setLayout(new BorderLayout());

      JPanel spoolInfo = new JPanel();

      AlignLayout alignMe2 = new AlignLayout(3,5,5);
      spoolInfo.setLayout(alignMe2);
      spoolInfo.setBorder(BorderFactory.createTitledBorder(
                           LangTool.getString("spool.labelExportInfo")));

      cvtType = new JComboBox();

      cvtType.addItem(LangTool.getString("spool.toPDF"));
      cvtType.addItem(LangTool.getString("spool.toText"));

//         cvtType.addItemListener(new java.awt.event.ItemListener() {
//            public void itemStateChanged(ItemEvent e) {
//   //            if (((String)cvtType.getSelectedItem()).equals(
//   //                                          LangTool.getString("spool.toText"))) {
//   //               twoText.setVisible(true);
//   //               twoPDF.setVisible(false);
//   //            }
//   //            else {
//   //               twoText.setVisible(false);
//   //               twoPDF.setVisible(true);
//   //            }
//            }
//         });

      spoolInfo.add(new JLabel(LangTool.getString("spool.labelFormat")));
      spoolInfo.add(cvtType);
      spoolInfo.add(new JLabel(""));

      pc = new JRadioButton(LangTool.getString("spool.labelPCPath"));
      pcPathInfo = new JTextField(30);

      pcSave = new JButton("...");

      pcSave.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e) {
            getPCFile();
         }
      });

      spoolInfo.add(pc);
      spoolInfo.add(pcPathInfo);
      spoolInfo.add(pcSave);

      ifs = new JRadioButton(LangTool.getString("spool.labelIFSPath"));
      ifsPathInfo = new JTextField(30);

      ifsSave = new JButton("...");

      ifsSave.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e) {
            getIFSFile();
         }
      });

      spoolInfo.add(ifs);
      spoolInfo.add(ifsPathInfo);
      spoolInfo.add(ifsSave);

      email = new JRadioButton(LangTool.getString("spool.labelEmail"));

      spoolInfo.add(email);
      spoolInfo.add(new JLabel(""));
      spoolInfo.add(new JLabel(""));

      ButtonGroup bg = new ButtonGroup();
      bg.add(pc);
      bg.add(ifs);
      bg.add(email);

      pc.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            doItemStateChanged(e);
         }
      });

      ifs.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            doItemStateChanged(e);
         }
      });

      email.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            doItemStateChanged(e);
         }
      });

      pc.setSelected(true);

      spoolOptions.add(spoolInfo,BorderLayout.CENTER);

      return contentPane;
   }

   /**
    * React on the state change for radio buttons
    *
    * @param e Item event to react to
    */
   private void doItemStateChanged(ItemEvent e) {

      pcPathInfo.setEnabled(false);
      ifsPathInfo.setEnabled(false);
      pcSave.setEnabled(false);
      ifsSave.setEnabled(false);

      if (e.getStateChange() == ItemEvent.SELECTED) {
         if (pc.isSelected()) {
            pcPathInfo.setEnabled(true);
            pcSave.setEnabled(true);
            pcPathInfo.grabFocus();
         }

         if (ifs.isSelected()) {
            ifsPathInfo.setEnabled(true);
            ifsSave.setEnabled(true);
            ifsPathInfo.grabFocus();
         }
      }
   }

   private boolean pagesValid() {

      if (pc.isSelected()) {
         if (pcPathInfo.getText().length() == 0)
            getPCFile();
         if (pcPathInfo.getText().length() == 0)
            return false;
      }

      return true;
   }

   /**
    * Get the local file from a file chooser
    */
   private void getPCFile() {

      String workingDir = System.getProperty("user.dir");
      TN5250jFileChooser pcFileChooser = new TN5250jFileChooser(workingDir);

      // set the file filters for the file chooser
      ExportFileFilter filter;

      if (((String)cvtType.getSelectedItem()).equals(LangTool.getString("spool.toPDF")))
         filter = new ExportFileFilter("pdf","PDF Files");
      else
         filter = new ExportFileFilter("txt","Text Files");

      pcFileChooser.addChoosableFileFilter(filter );

      int ret = pcFileChooser.showSaveDialog(this);

      // check to see if something was actually chosen
      if (ret == JFileChooser.APPROVE_OPTION) {
         File file = pcFileChooser.getSelectedFile();
         pcPathInfo.setText(filter.setExtension(file));

      }

   }

   /**
    * Get the IFS file from a file chooser
    */
   private void getIFSFile() {

      IFSFileDialog fd = new IFSFileDialog(this, "Save As", splfile.getSystem());
      com.ibm.as400.vaccess.FileFilter[] filterList =
                                       new com.ibm.as400.vaccess.FileFilter[2];
      filterList[0] = new com.ibm.as400.vaccess.FileFilter("All files (*.*)",
                                                            "*.*");

      // Set up the filter based on the type of export specifed
      if (cvtType.getSelectedIndex() == 0) {
         filterList[1] = new com.ibm.as400.vaccess.FileFilter("PDF files (*.pdf)",
                                                               "*.pdf");
      }
      else {
         filterList[1] = new com.ibm.as400.vaccess.FileFilter("Text files (*.txt)",
                                                               "*.txt");

      }
      fd.setFileFilter(filterList, 1);

      // show the dialog and obtain the file if selected
      if (fd.showDialog() == IFSFileDialog.OK) {
         ifsPathInfo.setText(fd.getAbsolutePath());
      }
   }

   /**
    * Get the local file from a file chooser
    */
   private void getEditor() {

      String workingDir = System.getProperty("user.dir");
      TN5250jFileChooser pcFileChooser = new TN5250jFileChooser(workingDir);

      int ret = pcFileChooser.showOpenDialog(this);

      // check to see if something was actually chosen
      if (ret == JFileChooser.APPROVE_OPTION) {
         File file = pcFileChooser.getSelectedFile();
         try {
            editor.setText(file.getCanonicalPath());
         }
         catch (IOException e) {

         }
      }
   }

   /**
    * Overridden so we can exit when window is closed
    */
   protected void processWindowEvent(WindowEvent e) {
      super.processWindowEvent(e);
      if (e.getID() == WindowEvent.WINDOW_CLOSING) {
         this.setVisible(false);
         this.dispose();
      }
   }

   /**
    * Export the spool file
    */
   private void doExport() {

      if (!pagesValid())
         return;

      workingThread = null;

      if (cvtType.getSelectedIndex() == 0)
         workingThread = new Thread(new Runnable () {
            public void run() {
               cvtToPDF();
            }
         });
      else
         workingThread = new Thread(new Runnable () {
            public void run() {
               cvtToText();
            }
         });

      workingThread.start();
   }

   /**
    * E-mail the information after export
    */
   private void emailMe() {

      SendEMailDialog semd = new SendEMailDialog(this,
                                 session,conicalPath);

   }

   /**
    * Convert spoolfile to text file
    */
   private void cvtToText() {

      java.io.PrintStream dw;

      try {

         openOutputFile();

         if (ifs.isSelected())
            dw = new java.io.PrintStream(ifsfw);
         else
            dw = new java.io.PrintStream(fw);

         // Create an AS400 object.  The system name was passed
         // as the first command line argument.
         AS400 system = new AS400 (systemName.getText());

         String splfName = spooledFile.getText();
         int splfNumber = Integer.parseInt(spooledFileNumber.getText());
         String _jobName = jobName.getText();
         String _jobUser = user.getText();
         String _jobNumber = number.getText();

         SpooledFile splF = new SpooledFile(system,
         splfName,
         splfNumber,
         _jobName,
         _jobUser,
         _jobNumber);

         PrintParameterList printParms = new PrintParameterList();
         printParms.setParameter(PrintObject.ATTR_WORKSTATION_CUST_OBJECT,
         "/QSYS.LIB/QWPDEFAULT.WSCST");
         printParms.setParameter(PrintObject.ATTR_MFGTYPE, "*WSCST");

         // get the text (via a transformed input stream) from the spooled file
         PrintObjectTransformedInputStream inStream = splF.getTransformedInputStream(printParms);
         //            DataInputStream dis = new DataInputStream(inStream);

         // get the number of available bytes
         int avail = inStream.available();
         byte[] buf = new byte[avail + 1];

         int read = 0;
         int totBytes = 0;
         StringBuffer sb = new StringBuffer();

         updateStatus("Starting Output");

         // read the transformed spooled file, creating the jobLog String
         while (avail > 0) {
            if (avail > buf.length) {
               buf = new byte[avail + 1];
            }

            read = inStream.read(buf, 0, avail);

            for (int x = 0; x < read; x++) {
               switch (buf[x]) {
                  case 0x0:      // 0x00
                     break;
                  // write line feed to the stream
                  case 0x0A:
                     dw.println(sb.toString().toCharArray());
                     sb.setLength(0);
                     break;
                  // we will skip the carrage return
                  case 0x0D:
//                     sb.append('\n');
//                     writeChar("\n");
//                     System.out.println();
                     break;
                  // new page
                  case 0x0C:
//                     writeChar(sb.toString());
//                     dw.write(sb.toString().getBytes());
                     dw.println(sb.toString().toCharArray());
                     sb.setLength(0);

                     break;
                  default:
                     sb.append(byte2char(buf[x], "cp850"));
               }
            }

            totBytes += read;

            updateStatus("Bytes read " + totBytes);
            //
            // process the data buffer
            //
            avail = inStream.available();
         }

         if (sb.length() > 0)
            dw.println(sb.toString().toCharArray());
         dw.flush();
         dw.close();

         updateStatus("Total bytes converted " + totBytes);

         // if we are to open it afterwards then execute the program with the
         //  text file as a parameter
         if (openAfter.isSelected()) {

            // not sure if this works on linux yet but here we go.
            try {
               Runtime rt = Runtime.getRuntime();
               String[] cmdArray = {editor.getText(),pcPathInfo.getText()};
               // We need to probably do some checking here in the future
               // Process proc = rt.exec(cmdArray);
               rt.exec(cmdArray);

               // now we set the field to use external viewer or not
               if (openAfter.isSelected())
                  session.getSession().getConfiguration().setProperty("useExternal","");
               else
                  session.getSession().getConfiguration().removeProperty("useExternal");

               // now we set the property for external viewer
               session.getSession().getConfiguration().setProperty("externalViewer",
                                                      editor.getText());
               // save it off
               session.getSession().getConfiguration().saveSessionProps();
            }
            catch (Throwable t) {
               // print a stack trace
               t.printStackTrace();
               // throw up the message error
               JOptionPane.showMessageDialog(this,t.getMessage(),"error",
                                                JOptionPane.ERROR_MESSAGE);
            }

         }

         if (email.isSelected())
            emailMe();
        }

        catch (Exception e) {
           updateStatus("Error: " + e.getMessage ());
           System.out.println ("Error: " + e.getMessage ());
        }

   }

   /**
    * Convert spoolfile to PDF file
    */
   private void cvtToPDF() {

      try {

         openOutputFile();

         // Create the printparameters to be used in the transform of the
         //    input stream
         PrintParameterList printParms = new PrintParameterList();
         printParms.setParameter(PrintObject.ATTR_WORKSTATION_CUST_OBJECT,
         "/QSYS.LIB/QWPDEFAULT.WSCST");
         printParms.setParameter(PrintObject.ATTR_MFGTYPE, "*WSCST");

         // get the text (via a transformed input stream) from the spooled file
         PrintObjectTransformedInputStream inStream = splfile.getTransformedInputStream(printParms);

         // get the number of available bytes
         int avail = inStream.available();
         byte[] buf = new byte[avail + 1];

         int read = 0;
         int totBytes = 0;

         StringBuffer sb = new StringBuffer();

         updateStatus("Starting Output");

         // read the transformed spooled file, creating the jobLog String
         while (avail > 0) {
            if (avail > buf.length) {
               buf = new byte[avail + 1];
            }

            read = inStream.read(buf, 0, avail);

            for (int x = 0; x < read; x++) {
               switch (buf[x]) {
                  case 0x0:      // 0x00
                     break;
                  // write line feed to the stream
                  case 0x0A:
//                     writeChar(sb.toString());
                     sb.append((char)buf[x]);
//                     System.out.print(sb);
//                     sb.setLength(0);
                     break;
                  // we will skip the carrage return
                  case 0x0D:
//                     sb.append('\n');
//                     writeChar("\n");
//                     System.out.println();
                     break;
                  // new page
                  case 0x0C:
                     writeBuffer(sb.toString());
                     document.newPage();
                     sb.setLength(0);
                     break;
                  default:
                     sb.append(byte2char(buf[x], "cp850"));
               }
            }

            totBytes += read;

            updateStatus("Bytes read " + totBytes);
            //
            // process the data buffer
            //
            avail = inStream.available();
         }
         closeOutputFile();
         updateStatus("Total bytes converted " + totBytes);

         if (email.isSelected())
            emailMe();

        }

        catch (Exception e) {
           updateStatus("Error: " + e.getMessage ());
           System.out.println ("Error: " + e.getMessage ());
        }
   }

   /**
    *
    * @param s
    */
   private void writeBuffer(String s) {

      if (!document.isOpen())
         document.open();

      try {
         document.add(new Paragraph(s,font));
      }
      catch (com.lowagie.text.DocumentException de) {
         System.out.println(de);
      }
   }

   /**
    * Open the correct type of output file depending on selection(s)
    */
   public void openOutputFile() {

      try {

         // update status
         updateStatus("Opening File");

         // default to txt extention
         String suffix = ".txt";
         String fileName = "";

         // if pdf then change to pdf extenstion
         if (cvtType.getSelectedIndex() == 0)
            suffix = ".pdf";


         // for e-mailing setup a temporary file
         if (email.isSelected()) {
            File dir = new File(System.getProperty("user.dir"));

            //  setup the temp file name
            String tempFile = spooledFile.getText().trim() + '_' +
                              jobName.getText().trim() + '_' +
                              user.getText().trim() + '_' +
                              spooledFileNumber.getText().trim() + '_' +
                              number.getText().trim();

            // create the temporary file
            File f = File.createTempFile(tempFile,suffix,dir);

            System.out.println(f.getName());
            System.out.println(f.getCanonicalPath());

            conicalPath = f.getCanonicalPath();

            // set it to delete on exit
            f.deleteOnExit();

            // create the file
            fw = new FileOutputStream(f);
         }
         else

            if (ifs.isSelected()) {
               fileName = ifsPathInfo.getText().trim();
               ifsfw = new IFSFileOutputStream(splfile.getSystem(),fileName);
            }
            else {
               fileName = pcPathInfo.getText().trim();
               fw = new FileOutputStream(fileName);
            }

         // if not PDF then this is all we have to do so return
         if (cvtType.getSelectedIndex() > 0)
            return;

         // On pdf's then we need to create a PDF document
         if (document == null) {

            document = new Document();

            // create the pdf writer based on selection of pc or ifs file
            if (ifs.isSelected()) {
               bos = PdfWriter.getInstance(document,ifsfw);
            }
            else {
               bos = PdfWriter.getInstance(document,fw);
            }

            // create the base font
            BaseFont bf = BaseFont.createFont("Courier", "Cp1252", false);

            // set the default size of the font to 9.0
            float fontsize = 9.0f;

            // if we have a font selectd then try to use it
            if (fontSize.getText().length() > 0)
               fontsize = Float.parseFloat(fontSize.getText().trim());

            // create the pdf font to use within the document
            font = new com.lowagie.text.Font(bf, fontsize,
                                             com.lowagie.text.Font.NORMAL);

            // set the PDF properties of the supplied properties
            if (author.getText().length() > 0)
               document.addAuthor(author.getText());
            if (title.getText().length() > 0)
               document.addTitle(title.getText());
            if (subject.getText().length() > 0)
               document.addSubject(subject.getText());

            // set the page sizes and the page orientation
            String ps = (String)pageSize.getSelectedItem();

            if (ps.equals("A3")) {
               if (portrait.isSelected())
                  document.setPageSize(PageSize.A3);
               else
                  document.setPageSize(PageSize.A3.rotate());

            }

            if (ps.equals("A4")) {
               if (portrait.isSelected())
                  document.setPageSize(PageSize.A4);
               else
                  document.setPageSize(PageSize.A4.rotate());
            }

            if (ps.equals("A5")) {
               if (portrait.isSelected())
                  document.setPageSize(PageSize.A5);
               else
                  document.setPageSize(PageSize.A5.rotate());
            }
            if (ps.equals("LETTER")) {
               if (portrait.isSelected())
                  document.setPageSize(PageSize.LETTER);
               else
                  document.setPageSize(PageSize.LETTER.rotate());
            }
            if (ps.equals("LEGAL")) {
               if (portrait.isSelected())
                  document.setPageSize(PageSize.LEGAL);
               else
                  document.setPageSize(PageSize.LEGAL.rotate());
            }
            if (ps.equals("LEDGER")) {
               if (portrait.isSelected())
                  document.setPageSize(PageSize.LEDGER);
               else
                  document.setPageSize(PageSize.LEDGER.rotate());
            }
         }
      }
      catch(IOException _ex) {
         System.out.println("Cannot open 1 " + _ex.getMessage());

      }
      catch(Exception _ex2) {
         System.out.println("Cannot open 2 " + _ex2.getMessage());
      }

   }

   private void closeOutputFile() {

         document.close();
         document = null;

   }

   private void updateStatus(final String stat) {

      SwingUtilities.invokeLater(
         new Runnable () {
            public void run() {
               statusBar.setText(stat);
            }
         }
      );

   }

   public void nextBegin(WizardEvent e) {
//      System.out.println(e.getCurrentPage().getName() + " Next Begin");
      if (((String)cvtType.getSelectedItem()).equals(
                                    LangTool.getString("spool.toText"))) {
         twoText.add(statusBar,BorderLayout.SOUTH);
         e.setNewPage(pageText);
      }
      else {
         twoPDF.add(statusBar,BorderLayout.SOUTH);
         e.setNewPage(pagePDF);

      }

   }

   public void nextComplete(WizardEvent e) {
//      System.out.println(e.getCurrentPage().getName() + " Next Complete");
      setTitle(e.getNewPage().getName());
   }

   public void previousBegin(WizardEvent e){
//      System.out.println(e.getCurrentPage().getName() + " Prev Begin");
      e.setNewPage(page);
      contentPane.add(statusBar,BorderLayout.SOUTH);
   }

   public void previousComplete(WizardEvent e) {
//      System.out.println(e.getCurrentPage().getName() + " Prev Complete");
      setTitle(e.getNewPage().getName());
   }

   public void finished(WizardEvent e) {
      doExport();
   }

   public void canceled(WizardEvent e) {
//      System.out.println("It is canceled!");
      if (workingThread != null) {
         workingThread.interrupt();
         workingThread = null;
      }
      this.setVisible(false);
      this.dispose();
   }

   public void help(WizardEvent e) {
      System.out.println(e.getCurrentPage().getName());
   }

   /**
    * Converts a byte to a char
    *
    * @param b the byte to be converted
    * @param charsetName the name of a charset in the which the byte is encoded
    * @return the converted char
    */
   public static char byte2char(byte b, String charsetName) {
      char c = ' ';
      try {
         byte[] bytes = {b};
         c = (new String(bytes, charsetName)).charAt(0);
      } catch (java.io.UnsupportedEncodingException uee) {
         System.err.println(uee);
         System.err.println("Error while converting byte to char, returning blank...");
      }
      return c;
   }


}