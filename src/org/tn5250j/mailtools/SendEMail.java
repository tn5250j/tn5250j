package org.tn5250j.mailtools;
/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.4
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


import java.util.Hashtable;
import java.io.*;
import java.beans.*;
import java.util.*;
import java.math.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class SendEMail {

   private String id;
   private String to;
   private String from;
   private String cc;
   private String subject;
   private String configFile;
   private String message;
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
   public void setMessage(String text) {
      this.message = text;
   }
   public String getMessage() {
      return message;
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
      boolean rc = false;

      SMTPProperties = new java.util.Properties();
      if (name == null || name == "")
         name = "SMTPProperties.cfg";

      try {
         FileInputStream in = new FileInputStream(name);

         SMTPProperties.load(in);
         rc = true;
      }
      catch (IOException ioe) {System.out.println(ioe.getMessage());}
      catch (SecurityException se) {
         System.out.println(se.getMessage());
      }

      return rc;
    }

   // clean-up -- this should be called by the JSP Container...
   public void release() {

      // clean up variables to be used the next time
      id=null;
      to=null;
      from=null;
      cc=null;
      subject=null;
      configFile=null;
      message=null;
      fileName=null;
   }

   /**
    * This method processes the send request from the compose form
    */
   public void send()	throws IOException {

      try {
         if(!loadConfig(configFile))
            return;

   		Session session = Session.getDefaultInstance(SMTPProperties, null);
	   	session.setDebug(false);

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

         if (from != null && from.length() > 0)
	         msg.setFrom(new InternetAddress(from));

         msg.setSentDate(new Date());

	    // create and fill the first message part
	    MimeBodyPart mbp = new MimeBodyPart();

	    mbp.setText(message,"us-ascii");

       if (fileName == null || fileName.length() == 0)
          mbp.setFileName("tn5250j.txt");
       else
         mbp.setFileName(fileName);

	    // create the Multipart and its parts to it
	    Multipart mp = new MimeMultipart();
	    mp.addBodyPart(mbp);

	    // add the Multipart to the message
	    msg.setContent(mp);

	    // send the message
	    Transport.send(msg);
      }
      catch (Exception mex) {
         System.out.println(mex.toString());
	   }
   }

}
