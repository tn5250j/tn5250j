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
import javax.swing.*;
import java.io.*;
import com.ibm.as400.access.*;
import com.ibm.as400.vaccess.*;
import org.tn5250j.tools.*;
import org.tn5250j.My5250;
import org.tn5250j.mailtools.SendEMailDialog;
import com.lowagie.text.pdf.*;
import com.lowagie.text.*;

/**
 *
 */
public class SpoolExportWizard extends JFrame {

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

   JRadioButton pc;
   JRadioButton ifs;
   JRadioButton email;

   // Spooled File
   SpooledFile splfile;

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
      contentPane = (JPanel) this.getContentPane();
      contentPane.setLayout(borderLayout1);
      this.setTitle("Spooled File Export Wizard");
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

      AlignLayout alignMe2 = new AlignLayout(2,5,5);
      spoolInfo.setLayout(alignMe2);
      spoolInfo.setBorder(BorderFactory.createTitledBorder("Export To Information"));

      cvtType = new JComboBox();

      cvtType.addItem(LangTool.getString("spool.toPDF"));
      cvtType.addItem(LangTool.getString("spool.toText"));

      spoolInfo.add(new JLabel(LangTool.getString("spool.labelFormat")));
      spoolInfo.add(cvtType);

      pc = new JRadioButton(LangTool.getString("spool.labelPCPath"));
      pcPathInfo = new JTextField(30);

      spoolInfo.add(pc);
      spoolInfo.add(pcPathInfo);

      ifs = new JRadioButton(LangTool.getString("spool.labelIFSPath"));
      ifsPathInfo = new JTextField(30);

      spoolInfo.add(ifs);
      spoolInfo.add(ifsPathInfo);

      email = new JRadioButton(LangTool.getString("spool.labelEmail"));

      spoolInfo.add(email);
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

      JPanel buttonPanel = new JPanel();

      JButton next = new JButton(LangTool.getString("spool.buttonNext"));
      JButton previous = new JButton(LangTool.getString("spool.buttonPrev"));
      JButton convert = new JButton(LangTool.getString("spool.buttonConvert"));

      previous.setEnabled(false);

      next.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(ActionEvent e) {
            doExport();
         }
      });

      buttonPanel.add(previous);
      buttonPanel.add(next);

      spoolOptions.add(buttonPanel,BorderLayout.SOUTH);

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

   private void doItemStateChanged(ItemEvent e) {

      pcPathInfo.setEnabled(false);
      ifsPathInfo.setEnabled(false);

      if (e.getStateChange() == ItemEvent.SELECTED) {
         if (pc.isSelected()) {
            pcPathInfo.setEnabled(true);
            pcPathInfo.grabFocus();
         }

         if (ifs.isSelected()) {
            ifsPathInfo.setEnabled(true);
            ifsPathInfo.grabFocus();
         }
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
            fw = new FileOutputStream(spooledFile.getText().trim() + suffix);

         if (cvtType.getSelectedIndex() > 0)
            return;

         if (document == null) {
            document = new Document();

            bos = PdfWriter.getInstance(document,fw);
//            document.setPageSize(new Rectangle(0.0f,
//                                                0.0f,
//                                                getPointFromInches(13),
//                                                getPointFromInches(11)));

            BaseFont bf = BaseFont.createFont("Courier", "Cp1252", false);
            font = new com.lowagie.text.Font(bf, 8, com.lowagie.text.Font.NORMAL);
            document.setPageSize(PageSize.A3.rotate());
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


}