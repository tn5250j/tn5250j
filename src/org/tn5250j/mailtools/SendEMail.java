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
package org.tn5250j.mailtools;

import java.util.Date;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;

public class SendEMail {

   private String to;
   private String from;
   private String pers;
   private String cc;
   private String subject;
   private String configFile;
   private String message;
   private String attachment;
   private String attachmentName;
   private String fileName;

   // SMTP Properties file
   java.util.Properties SMTPProperties;

   public void setTo(String to) {
      this.to = to;
   }
   public String getTo() {
      return to;
   }
   public void setFrom(String from) {
      this.from = from;
   }
   public String getFrom() {
      return from;
   }
   public void setCC(String cc) {
      this.cc = cc;
   }
   public String getCC() {
      return cc;
   }
   public void setSubject(String subject) {
      this.subject = subject;
   }
   public String getSubject() {
      return subject;
   }
   public void setConfigFile(String file) {
      this.configFile = file;
   }
   public String getConfigFile() {
      return configFile;
   }

   public void setAttachment(String text) {
      this.attachment = text;
   }

   public String getAttachment() {
      return attachment;
   }

   public void setMessage(String text) {
      this.message = text;
   }

   public String getMessage() {
      return message;
   }

   public void setAttachmentName(String desc) {

      attachmentName = desc;
   }

   public String getAttachmentName() {

      return attachmentName;

   }

   public void setFileName(String name) {
      this.fileName = name;
   }

   public String getFileName() {
      return fileName;
   }


   /**
    * <p>Loads the given configuration file.
    *
    * @param name Configuration file name
    * @return true if the configuration file was loaded
    */
   private boolean loadConfig(String name) throws Exception {

      SMTPProperties = ConfigureFactory.getInstance().getProperties("smtp",
                           "SMTPProperties.cfg");

      if (SMTPProperties.size() > 0)
         return true;
      else
         return false;
   }

   // clean-up -- this should be called by the JSP Container...
   public void release() {

      // clean up variables to be used the next time
      to=null;
      from=null;
      cc=null;
      subject=null;
      configFile=null;
      message=null;
      attachment = null;
      attachmentName = null;
      fileName=null;
   }

   /**
    * This method processes the send request from the compose form
    */
   public boolean send() throws Exception {

      try {
         if(!loadConfig(configFile))
            return false;

         Session session = Session.getDefaultInstance(SMTPProperties, null);
         session.setDebug(false);

         // create the Multipart and its parts to it
         Multipart mp = new MimeMultipart();

         Message msg = new MimeMessage(session);
         InternetAddress[] toAddrs = null, ccAddrs = null;

         toAddrs = InternetAddress.parse(to, false);
         msg.setRecipients(Message.RecipientType.TO, toAddrs);

         if (cc != null) {
            ccAddrs = InternetAddress.parse(cc, false);
            msg.setRecipients(Message.RecipientType.CC, ccAddrs);
         }

         if (subject != null)
            msg.setSubject(subject.trim());

         if (from == null)
            from = SMTPProperties.getProperty("mail.smtp.from");

         if (from != null && from.length() > 0) {
         	pers = SMTPProperties.getProperty("mail.smtp.realname");
         	if (pers != null) msg.setFrom(new InternetAddress(from, pers));
         }

         if (message != null && message.length() > 0) {
            // create and fill the attachment message part
            MimeBodyPart mbp = new MimeBodyPart();
            mbp.setText(message,"us-ascii");
            mp.addBodyPart(mbp);
         }

         msg.setSentDate(new Date());

         if (attachment != null && attachment.length() > 0) {
            // create and fill the attachment message part
            MimeBodyPart abp = new MimeBodyPart();

            abp.setText(attachment,"us-ascii");

            if (attachmentName == null || attachmentName.length() == 0)
               abp.setFileName("tn5250j.txt");
            else
               abp.setFileName(attachmentName);
            mp.addBodyPart(abp);

         }

         if (fileName != null && fileName.length() > 0) {
            // create and fill the attachment message part
            MimeBodyPart fbp = new MimeBodyPart();

            fbp.setText("File sent using tn5250j","us-ascii");

            if (attachmentName == null || attachmentName.length() == 0) {
               	fbp.setFileName("tn5250j.txt");
            }
            else
               fbp.setFileName(attachmentName);

             // Get the attachment
             DataSource source = new FileDataSource(fileName);

             // Set the data handler to the attachment
             fbp.setDataHandler(new DataHandler(source));

            mp.addBodyPart(fbp);

         }

         // add the Multipart to the message
         msg.setContent(mp);

         // send the message
         Transport.send(msg);
         return true;
      }
      catch (SendFailedException sfe) {
         showFailedException(sfe);
      }
      return false;
   }

   /**
    * Show the error list from the e-mail API if there are errors
    *
    * @param parent
    * @param sfe
    */
   private void showFailedException(SendFailedException sfe) {

      String error = sfe.getMessage() + "\n";

      Address[] ia = sfe.getInvalidAddresses();

      if (ia != null) {
         for (int x = 0; x < ia.length; x++) {
            error += "Invalid Address: " + ia[x].toString() + "\n";
         }
      }

      JTextArea ea = new JTextArea(error,6,50);
      JScrollPane errorScrollPane = new JScrollPane(ea);
      errorScrollPane.setHorizontalScrollBarPolicy(
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      errorScrollPane.setVerticalScrollBarPolicy(
      JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
      JOptionPane.showMessageDialog(null,
                                       errorScrollPane,
                                       LangTool.getString("em.titleConfirmation"),
                                       JOptionPane.ERROR_MESSAGE);


   }

}
