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

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import java.io.*;
import com.ibm.as400.access.*;
import com.ibm.as400.vaccess.*;
import org.tn5250j.tools.*;
import org.tn5250j.event.WizardListener;
import org.tn5250j.event.WizardEvent;
import org.tn5250j.gui.Wizard;
import org.tn5250j.gui.WizardPage;
import org.tn5250j.My5250;
import org.tn5250j.mailtools.SendEMailDialog;
import com.lowagie.text.pdf.*;
import com.lowagie.text.*;

/**
 *
 */
public class SpoolExportWizard extends JFrame implements WizardListener {

   JPanel contentPane;
   JLabel statusBar = new JLabel();
   BorderLayout borderLayout1 = new BorderLayout();
   JPanel spoolPanel = new JPanel();
   JPanel spoolData = new JPanel();
   BorderLayout borderLayout2 = new BorderLayout();
   JPanel spoolOptions = new JPanel();
   AlignLayout alignMe = new AlignLayout(2,5,5);
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

   // Spooled File
   SpooledFile splfile;

   JPanel two;

   // Wizard
   Wizard wizard;
   JButton nextButton;

   // pdf variables
   private PdfWriter bos;
   private Document document;
   private com.lowagie.text.Font font;

   // output stream
   private FileOutputStream fw;

   // conical path of file
   private String conicalPath;

   //Construct the frame
   public SpoolExportWizard(SpooledFile splfile) {

      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      this.splfile = splfile;

      try {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   //Component initialization
   private void jbInit() throws Exception  {

      setIconImage(My5250.tnicon.getImage());

      wizard = new Wizard();

      wizard.addWizardListener(this);

      this.getContentPane().add(wizard);

      WizardPage page;

      page = new WizardPage(WizardPage.NEXT |
                       WizardPage.FINISH |
                       WizardPage.CANCEL |
                       WizardPage.HELP);

      page.setName("Spool Export Wizard - 1");

      setTitle(page.getName());

      // get the next button so we can set it enabled or disabled depending
      // on output type.
      nextButton = page.getNextButton();

      page.getContentPane().add(pageOne(), BorderLayout.CENTER);

      wizard.add(page);

      page = new WizardPage(WizardPage.PREVIOUS |
                       WizardPage.FINISH |
                       WizardPage.CANCEL |
                       WizardPage.HELP);
      page.setName("Spool Export Wizard - 2");

      page.getContentPane().add(pageTwo(), BorderLayout.CENTER);
      wizard.add(page);

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

   private JPanel pageTwo () {

      two = new JPanel();

      two.setLayout(new BorderLayout());

      JPanel docProps = new JPanel();

      docProps.setBorder(BorderFactory.createTitledBorder("PDF Properties"));
      docProps.setLayout(new AlignLayout(2,5,5));

      docProps.add(new JLabel("Title"));
      docProps.add(title = new JTextField(40));
      docProps.add(new JLabel("Subject"));
      docProps.add(subject = new JTextField(40));
      docProps.add(new JLabel("Author"));
      docProps.add(author = new JTextField(40));

      JPanel options = new JPanel();

      options.setBorder(BorderFactory.createTitledBorder("Options"));
      options.setLayout(new AlignLayout(2,5,5));

      options.add(new JLabel("Font Size"));
      options.add(fontSize = new JTextField(5));

      options.add(new JLabel("Page Size"));
      options.add(pageSize = new JComboBox());

      pageSize.addItem("A3");
      pageSize.addItem("A4");
      pageSize.addItem("A5");
      pageSize.addItem("LETTER");
      pageSize.addItem("LEGAL");
      pageSize.addItem("LEDGER");

      options.add(portrait = new JRadioButton("Portrait"));
      options.add(landscape = new JRadioButton("Landscape"));

      ButtonGroup orientation = new ButtonGroup();
      orientation.add(portrait);
      orientation.add(landscape);

      landscape.setSelected(true);

      two.add(docProps,BorderLayout.NORTH);
      two.add(options,BorderLayout.CENTER);
//      two.add(statusBar,BorderLayout.SOUTH);

      return two;
   }

   private JPanel pageOne () throws Exception {

      contentPane = new JPanel();

      contentPane.setLayout(borderLayout1);
      statusBar.setText(" ");
      statusBar.setBorder(BorderFactory.createEtchedBorder());

      spoolPanel.setLayout(borderLayout2);

      labelSpooledFile.setText("Spooled file:");
      labelJobName.setText("Job Name:");
      labelUser.setText("     User:");
      labelNumber.setText("     Number:");
      labelFileNumber.setText("Spooled file number:");
      labelSystem.setText("System:");
      labelPages.setText("Total pages:");


      contentPane.add(statusBar, BorderLayout.SOUTH);
      contentPane.add(spoolPanel, BorderLayout.CENTER);

      spoolData.setLayout(alignMe);
      spoolData.setBorder(BorderFactory.createTitledBorder("Spooled File Information"));
//                                       LangTool.getString("spool.filterTitle")));

      spooledFile.setText(splfile.getName());
      jobName.setText(splfile.getJobName());
      user.setText(splfile.getJobUser());
      spooledFileNumber.setText(Integer.toString(splfile.getNumber()));
      number.setText(splfile.getJobNumber());
      systemName.setText(splfile.getSystem().getSystemName());
      pages.setText(splfile.getIntegerAttribute(splfile.ATTR_PAGES).toString());

      spoolPanel.add(spoolData,  BorderLayout.CENTER);

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

      spoolOptions.setLayout(new BorderLayout());

      JPanel spoolInfo = new JPanel();

      AlignLayout alignMe2 = new AlignLayout(3,5,5);
      spoolInfo.setLayout(alignMe2);
      spoolInfo.setBorder(BorderFactory.createTitledBorder("Export To Information"));

      cvtType = new JComboBox();

      cvtType.addItem(LangTool.getString("spool.toPDF"));
      cvtType.addItem(LangTool.getString("spool.toText"));

      cvtType.addItemListener(new java.awt.event.ItemListener() {
         public void itemStateChanged(ItemEvent e) {
            if (((String)cvtType.getSelectedItem()).equals(LangTool.getString("spool.toText")))
               nextButton.setEnabled(false);
            else
               nextButton.setEnabled(true);
         }
      });

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

      spoolInfo.add(ifs);
      spoolInfo.add(ifsPathInfo);
      spoolInfo.add(new JLabel(""));

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

   private void doItemStateChanged(ItemEvent e) {

      pcPathInfo.setEnabled(false);
      ifsPathInfo.setEnabled(false);
      pcSave.setEnabled(false);

      if (e.getStateChange() == ItemEvent.SELECTED) {
         if (pc.isSelected()) {
            pcPathInfo.setEnabled(true);
            pcSave.setEnabled(true);
            pcPathInfo.grabFocus();
         }

         if (ifs.isSelected()) {
            ifsPathInfo.setEnabled(true);
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
      MyFileChooser pcFileChooser = new MyFileChooser(workingDir);

      // set the file filters for the file chooser
      ExportFileFilter filter;

      if (((String)cvtType.getSelectedItem()).equals(LangTool.getString("spool.toPDF")))
         filter = new ExportFileFilter("pdf","PDF Files");
      else
         filter = new ExportFileFilter("txt","Text Files");

      pcFileChooser.addChoosableFileFilter(filter );
//
      int ret = pcFileChooser.showSaveDialog(this);

      // check to see if something was actually chosen
      if (ret == JFileChooser.APPROVE_OPTION) {
         File file = pcFileChooser.getSelectedFile();
         pcPathInfo.setText(filter.setExtension(file));

      }

   }

   //Overridden so we can exit when window is closed
   protected void processWindowEvent(WindowEvent e) {
      super.processWindowEvent(e);
      if (e.getID() == WindowEvent.WINDOW_CLOSING) {
         this.setVisible(false);
         this.dispose();
      }
   }

   private void doExport() {

      if (!pagesValid())
         return;

      Thread cvt = null;

      if (cvtType.getSelectedIndex() == 0)
         cvt = new Thread(new Runnable () {
            public void run() {
               cvtToPDF();
            }
         });
      else
         cvt = new Thread(new Runnable () {
            public void run() {
               cvtToText();
            }
         });

      cvt.start();
   }

   private void emailMe() {

      SendEMailDialog semd = new SendEMailDialog(this
                                 ,conicalPath);

   }

   private void cvtToText() {

      BufferedOutputStream dw;

      try {

         openOutputFile();

         dw = new BufferedOutputStream(fw);

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
         BufferedReader br = new BufferedReader(
                    new InputStreamReader(inStream));

         // get the number of available bytes
         int avail = inStream.available();
         byte[] buf = new byte[avail + 1];

         int read = 0;

         System.out.println("Starting Output");
         // read the transformed spooled file, creating the jobLog String
         while (avail > 0) {
            if (avail > buf.length) {
               buf = new byte[avail + 1];
            }

            read += inStream.read(buf, 0, avail);

            dw.write(buf);

            final int byr = read;

               SwingUtilities.invokeLater(
                  new Runnable () {
                     public void run() {
                        statusBar.setText("Bytes read " + byr);
                     }
                  }
               );
//            System.out.println("Bytes read " + byr);

//            System.out.println(new String(buf));
            //
            // process the data buffer
            //
            avail = inStream.available();
         }

         dw.flush();
         dw.close();
         final int byr = read;
         SwingUtilities.invokeLater(
            new Runnable () {
               public void run() {
                  statusBar.setText("Total bytes converted " + byr);

               }
            }
         );
         if (email.isSelected())
            emailMe();
        }
        catch (Exception e) {
            final String msg = "Error: " + e.getMessage ();
               SwingUtilities.invokeLater(
                  new Runnable () {
                     public void run() {
                        statusBar.setText(msg);
                     }
                  }
               );
           System.out.println ("Error: " + e.getMessage ());
        }

   }

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

         BufferedReader br = new BufferedReader(
                    new InputStreamReader(inStream));

         // get the number of available bytes
         int avail = inStream.available();
         byte[] buf = new byte[avail + 1];

         int read = 0;
         int totBytes = 0;

         StringBuffer sb = new StringBuffer();

         System.out.println("Starting Output");
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
                     writeChar(sb.toString());
                     document.newPage();
                     sb.setLength(0);
                     break;
                  default:
                     sb.append((char)buf[x]);
               }
            }


            totBytes += read;
            final int byr = totBytes;

            SwingUtilities.invokeLater(
               new Runnable () {
                  public void run() {
                     statusBar.setText("Bytes read " + byr);
                  }
               }
            );
            //
            // process the data buffer
            //
            avail = inStream.available();
         }
         closeOutputFile();
         final int byr = totBytes;
            SwingUtilities.invokeLater(
               new Runnable () {
                  public void run() {
                     statusBar.setText("Total bytes converted " + byr);
                  }
               }
            );
         if (email.isSelected())
            emailMe();

        }
        catch (Exception e) {
            final String msg = "Error: " + e.getMessage ();
               SwingUtilities.invokeLater(
                  new Runnable () {
                     public void run() {
                        statusBar.setText(msg);

                     }
                  }
               );
           System.out.println ("Error: " + e.getMessage ());
        }

   }

   private void writeChar(String s) {

      if (!document.isOpen())
         document.open();

      try {
         document.add(new Paragraph(s,font));
      }
      catch (com.lowagie.text.DocumentException de) {
         System.out.println(de);
      }
   }

   public void openOutputFile() {

      try {

         System.out.println("Opening file");
         String suffix = ".txt";
         if (cvtType.getSelectedIndex() == 0)
            suffix = ".pdf";

         String fileName = pcPathInfo.getText().trim();

         if (email.isSelected()) {
            File dir = new File(System.getProperty("user.dir"));
            File f = File.createTempFile(number.getText().trim(),suffix,dir);

            System.out.println(f.getName());
            System.out.println(f.getCanonicalPath());
            conicalPath = f.getCanonicalPath();
            f.deleteOnExit();
            fw = new FileOutputStream(f);
         }
         else
            fw = new FileOutputStream(fileName);

         if (cvtType.getSelectedIndex() > 0)
            return;

         if (document == null) {
            document = new Document();

            bos = PdfWriter.getInstance(document,fw);

            BaseFont bf = BaseFont.createFont("Courier", "Cp1252", false);

            float size = 9.0f;

            if (fontSize.getText().length() > 0) {
               size = Float.valueOf(fontSize.getText()).floatValue();
            }

            font = new com.lowagie.text.Font(bf, size, com.lowagie.text.Font.NORMAL);

            if (author.getText().length() > 0)
               document.addAuthor(author.getText());
            if (title.getText().length() > 0)
               document.addTitle(title.getText());
            if (subject.getText().length() > 0)
               document.addSubject(subject.getText());

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
         System.out.println("Cannot open");

      }
      catch(Exception _ex) {
         System.out.println("Cannot open");
      }

   }

   public void closeOutputFile() {

         document.close();
         document = null;

   }

   public void nextBegin(WizardEvent e) {
      System.out.println(e.getCurrentPage().getName() + " Next Begin");
//      WizardPage wiz = (WizardPage)e.getNewPage();
//      JPanel pan = ((JPanel)wiz.getContentPane());
//      pan.add(statusBar,BorderLayout.SOUTH);
//      pan.invalidate();
//      pan.validate();
//      pan.repaint();
//      setTitle(e.getCurrentPage().getName());
      two.add(statusBar,BorderLayout.SOUTH);
   }

   public void nextComplete(WizardEvent e) {
      System.out.println(e.getCurrentPage().getName() + " Next Complete");
      setTitle(e.getNewPage().getName());

   }

   public void previousBegin(WizardEvent e){
      System.out.println(e.getCurrentPage().getName() + " Prev Begin");
//      WizardPage wiz = (WizardPage)e.getNewPage();
//      JPanel pan = ((JPanel)wiz.getContentPane());
//      pan.add(statusBar,BorderLayout.SOUTH);
//      pan.invalidate();
//      pan.validate();
//      pan.repaint();
      contentPane.add(statusBar,BorderLayout.SOUTH);
   }

   public void previousComplete(WizardEvent e) {
      System.out.println(e.getCurrentPage().getName() + " Prev Complete");
      setTitle(e.getNewPage().getName());
   }

   public void finished(WizardEvent e) {
      System.out.println("It is finished!");
      doExport();
      //      closeWizard();
   }

   public void canceled(WizardEvent e) {
      System.out.println("It is canceled!");
      this.hide();
      this.dispose();
   }

   public void help(WizardEvent e) {
      System.out.println(e.getCurrentPage().getName());
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