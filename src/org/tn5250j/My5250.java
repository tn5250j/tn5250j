package org.tn5250j;
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

import java.util.*;
import java.util.zip.*;
import java.text.*;
import java.io.*;
import javax.swing.UIManager;
import javax.swing.*;
import java.awt.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.font.*;
import java.awt.geom.*;
import org.tn5250j.tools.*;
import org.tn5250j.event.*;
import java.net.*;


public class My5250 implements ChangeListener,BootListener,TN5250jConstants,
                                                    SessionListener {

   private boolean packFrame = false;
   protected Gui5250Frame frame;
   private int selectedIndex = 0;
   Vector sessionsV;
   String[] sessionArgs = null;
   static Properties sessions = new Properties();
   ImageIcon focused = null;
   ImageIcon unfocused = null;
   static BootStrapper strapper = null;
   private SessionManager manager;

   My5250 () {
      try  {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch(Exception e) {
      }

      frame = new Gui5250Frame(this);
      loadSessions();
      int width = 600;
      int height = 500;

      if (sessions.containsKey("emul.width"))
         width = Integer.parseInt(sessions.getProperty("emul.width"));
      if (sessions.containsKey("emul.height"))
         height = Integer.parseInt(sessions.getProperty("emul.height"));

      frame.setSize(width,height);

      centerFrame();

      frame.sessionPane.addChangeListener(this);

      focused = createImageIcon("focused.gif");
      unfocused = createImageIcon("unfocused.gif");
      ImageIcon tnicon = createImageIcon("tnicon.jpg");
      frame.setIconImage(tnicon.getImage());
      sessionsV = new Vector();
      setDefaultLocale();
      manager = new SessionManager();
      manager.setController(this);
   }

   /**
    * Check if there are any other instances of tn5250j running
    */
   static private boolean checkBootStrapper (String[] args) {

      try {
         Socket boot = new Socket("localhost", 3036);

         PrintWriter out = new PrintWriter(boot.getOutputStream(), true);

         // parse args into a string to send to the other instance of
         //    tn5250j
         String opts = null;
         for (int x = 0;x < args.length; x++) {
            if (opts != null)
               opts += args[x] + " ";
            else
               opts = args[x] + " ";
         }
         out.println(opts);
         return true;

      }
      catch (UnknownHostException e) {
         System.err.println("localhost not known.");
      }
      catch (IOException e) {
         System.err.println("No other instances of tn5250j running.");
      }

      return false;
   }

   public void bootOptionsReceived(BootEvent bootEvent) {

      System.out.println(" boot options received " + bootEvent.getNewSessionOptions());

      // If the options are not equal to the string 'null' then we have
      //    boot options
      if (!bootEvent.getNewSessionOptions().equals("null")) {
         // check if a session parameter is specified on the command line
         String[] args = new String[NUM_PARMS];
         parseArgs(bootEvent.getNewSessionOptions(), args);


         if (isSpecified("-s",args)) {

            String sd = getParm("-s",args);
            if (sessions.containsKey(sd)) {
               parseArgs(sessions.getProperty(sd), args);
               final String[] args2 = args;
               final String sd2 = sd;
               SwingUtilities.invokeLater(
                  new Runnable () {
                     public void run() {
                        newSession(sd2,args2);

                     }
                  }
               );
            }
         }
         else {

            if (args[0].startsWith("-")) {
               SwingUtilities.invokeLater(
                  new Runnable () {
                     public void run() {
                        startNewSession();

                     }
                  }
               );
            }
            else {
               final String[] args2 = args;
               final String sd2 = args[0];
               SwingUtilities.invokeLater(
                  new Runnable () {
                     public void run() {
                        newSession(sd2,args2);

                     }
                  }
               );
            }
         }
      }
      else {
         SwingUtilities.invokeLater(
            new Runnable () {
               public void run() {
                  startNewSession();

               }
            }
         );
      }
   }

   static public void main(String[] args) {

      if (!isSpecified("-nc",args)) {

         if (!checkBootStrapper(args)) {

            // if we did not find a running instance and the -d options is
            //    specified start up the bootstrap deamon to allow checking
            //    for running instances
            if (isSpecified("-d",args)) {
               strapper = new BootStrapper();

               strapper.start();
            }
         }
         else {

            System.exit(0);
         }
      }

      My5250 m = new My5250();

      if (strapper != null)
         strapper.addBootListener(m);

      if (args.length > 0) {

         if (isSpecified("-width",args) ||
               isSpecified("-height",args)) {
            int width = m.frame.getWidth();
            int height = m.frame.getHeight();

            if (isSpecified("-width",args)) {
               width = Integer.parseInt(m.getParm("-width",args));
            }
            if (isSpecified("-height",args)) {
               height = Integer.parseInt(m.getParm("-height",args));
            }

            m.frame.setSize(width,height);
            m.centerFrame();


         }

          /**
           * @todo this crap needs to be rewritten it is a mess
           */
         if (args[0].startsWith("-")) {

            // check if a session parameter is specified on the command line
            if (isSpecified("-s",args)) {

               String sd = getParm("-s",args);
               if (sessions.containsKey(sd)) {
                  sessions.setProperty("emul.default",sd);
               }
               else {
                  args = null;
               }

            }

            // check if a locale parameter is specified on the command line
            if (isSpecified("-L",args)) {

               Locale.setDefault(parseLocal(getParm("-L",args)));
               LangTool.init();
               if (args[0].startsWith("-")) {

                  m.sessionArgs = null;
               }
               else {
                  m.frame.setVisible(true);
                  LangTool.init();
                  m.sessionArgs = args;
               }
            }
            else {
               LangTool.init();
               if (isSpecified("-s",args))
                  m.sessionArgs = args;
               else
                  m.sessionArgs = null;
            }
         }
         else {

            LangTool.init();
            m.sessionArgs = args;
         }
      }
      else {
         LangTool.init();
         m.sessionArgs = null;
      }

      if (m.sessionArgs != null) {

         // BEGIN
         // 2001/09/19 natural computing MR
         Vector os400_sessions = new Vector();
         Vector session_params = new Vector();

         for (int x = 0; x < args.length; x++) {

            if (args[x].equals("-s")) {
               x++;
               if (args[x] != null && sessions.containsKey(args[x])) {
                  os400_sessions.addElement(args[x]);
               }else{
                  x--;
                  session_params.addElement(args[x]);
               }
            }else{
               session_params.addElement(args[x]);
            }

         }

         for (int x = 0; x < session_params.size(); x++)
            m.sessionArgs[x] = session_params.elementAt(x).toString();

         m.startNewSession();

         for (int x = 1; x < os400_sessions.size(); x++ ) {
            String sel = os400_sessions.elementAt(x).toString();

            if (!m.frame.isVisible())
               m.frame.setVisible(true);

            m.sessionArgs = new String[NUM_PARMS];
            m.parseArgs(sessions.getProperty(sel),m.sessionArgs);
            m.newSession(sel,m.sessionArgs);
         }
         // 2001/09/19 natural computing MR
         // END
      }
      else {
         m.startNewSession();
      }

   }

   private void centerFrame() {

      if (packFrame)
         frame.pack();
      else
         frame.validate();

      //Center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = frame.getSize();
      if (frameSize.height > screenSize.height)
         frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
         frameSize.width = screenSize.width;
      frame.setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height) / 2);


   }

   private void setDefaultLocale () {

      if (sessions.containsKey("emul.locale")) {
         Locale.setDefault(parseLocal((String)sessions.getProperty("emul.locale")));
      }

   }

   static private String getParm(String parm, String[] args) {

      for (int x = 0; x < args.length; x++) {

         if (args[x].equals(parm))
            return args[x+1];

      }
      return null;
   }

   static boolean isSpecified(String parm, String[] args) {

      if (args == null)
         return false;

      for (int x = 0; x < args.length; x++) {

         if (args[x] != null && args[x].equals(parm))
            return true;

      }
      return false;
   }

   static String getDefaultSession() {

      if (sessions.containsKey("emul.default")) {
         return (String)sessions.getProperty("emul.default");
      }
      else {
         return null;
      }
   }

   void startNewSession() {

      int result = 2;
      String sel = "";

      if (sessionArgs != null && !sessionArgs[0].startsWith("-"))
         sel = sessionArgs[0];
      else {
         sel = getDefaultSession();
      }
      Sessions sess = manager.getSessions();

      if (sel != null && sess.getCount() == 0
                  && sessions.containsKey(sel)){
         sessionArgs = new String[NUM_PARMS];
         parseArgs((String)sessions.getProperty(sel), sessionArgs);
      }

      if (sessionArgs == null  || sess.getCount() > 0) {

         sel = getConnectSession();

         if (sel != null) {
            String selArgs = sessions.getProperty(sel);
            sessionArgs = new String[NUM_PARMS];
            parseArgs(selArgs, sessionArgs);
            if (!frame.isVisible())
               frame.setVisible(true);

            newSession(sel,sessionArgs);
         }
      }
      else {
         if (!frame.isVisible())
            frame.setVisible(true);

         newSession(sel,sessionArgs);

      }
   }

   private String getConnectSession () {

      Connect sc = new Connect(frame,LangTool.getString("ss.title"),sessions);

      // load the new session information from the session property file
      loadSessions();
      return sc.getConnectKey();
   }

   public void onSessionChanged(SessionChangeEvent changeEvent) {

      Session ses = (Session)changeEvent.getSource();
      System.out.println(changeEvent.getState() + " " +
                        ses.getAllocDeviceName());

      if (changeEvent.getState() == STATE_CONNECTED) {
         final String d = ses.getAllocDeviceName();

         if (d != null) {
            final int index = frame.sessionPane.indexOfComponent(ses);
            Runnable tc = new Runnable () {
               public void run() {
                  frame.sessionPane.setTitleAt(index,d);
               }
            };
            SwingUtilities.invokeLater(tc);

         }
      }
   }

   synchronized void newSession(String sel,String[] args) {

      Properties sesProps = new Properties();

      String propFileName = null;
      String session = args[0];

      // Start loading properties
      sesProps.put(SESSION_HOST,session);

      if (isSpecified("-e",args))
         sesProps.put(SESSION_TN_ENHANCED,"1");

      if (isSpecified("-p",args)) {
         sesProps.put(SESSION_HOST_PORT,getParm("-p",args));
      }

      if (isSpecified("-f",args))
         propFileName = getParm("-f",args);

      if (isSpecified("-cp",args))
         sesProps.put(SESSION_CODE_PAGE ,getParm("-cp",args));

      if (isSpecified("-gui",args))
         sesProps.put(SESSION_USE_GUI,"1");

      if (isSpecified("-132",args))
         sesProps.put(SESSION_SCREEN_SIZE,SCREEN_SIZE_27X132_STR);
      else
         sesProps.put(SESSION_SCREEN_SIZE,SCREEN_SIZE_24X80_STR);

      // are we to use a socks proxy
      if (isSpecified("-usp",args)) {

         // socks proxy host argument
         if (isSpecified("-sph",args)) {
            sesProps.put(SESSION_PROXY_HOST ,getParm("-sph",args));
         }

         // socks proxy port argument
         if (isSpecified("-spp",args))
            sesProps.put(SESSION_PROXY_PORT ,getParm("-spp",args));
      }

      // check if device name is specified
      if (isSpecified("-dn",args))
         sesProps.put(SESSION_DEVICE_NAME ,getParm("-dn",args));

      Session s = manager.openSession(sesProps,propFileName,sel);

      if (isSpecified("-t",args))
         frame.sessionPane.addTab(sel,focused,s);
      else
         frame.sessionPane.addTab(session,focused,s);

      frame.sessionPane.setForegroundAt(frame.sessionPane.getSelectedIndex(),Color.black);
      frame.sessionPane.setIconAt(frame.sessionPane.getSelectedIndex(),unfocused);


      frame.sessionPane.setSelectedIndex(frame.sessionPane.getTabCount()-1);
      frame.sessionPane.setForegroundAt(frame.sessionPane.getSelectedIndex(),Color.blue);
      frame.sessionPane.setIconAt(frame.sessionPane.getSelectedIndex(),focused);
      s.addSessionListener(this);
      s.connect();


   }

   public void stateChanged(ChangeEvent e) {

      JTabbedPane p = (JTabbedPane)e.getSource();
      p.setForegroundAt(selectedIndex,Color.black);
      p.setIconAt(selectedIndex,unfocused);

      Session sg = (Session)p.getComponentAt(selectedIndex);
      sg.setVisible(false);

      sg = (Session)p.getSelectedComponent();
      sg.setVisible(true);

      sg.requestFocus();

      selectedIndex = p.getSelectedIndex();
      p.setForegroundAt(selectedIndex,Color.blue);
      p.setIconAt(selectedIndex,focused);

   }

   public void nextSession() {

      int index = frame.sessionPane.getSelectedIndex();
      frame.sessionPane.setForegroundAt(index,Color.black);
      frame.sessionPane.setIconAt(index,unfocused);

      if (index < frame.sessionPane.getTabCount() - 1) {
         frame.sessionPane.setSelectedIndex(++index);
         frame.sessionPane.setForegroundAt(index,Color.blue);
         frame.sessionPane.setIconAt(index,focused);

      }
      else {
         frame.sessionPane.setSelectedIndex(0);
         frame.sessionPane.setForegroundAt(0,Color.blue);
         frame.sessionPane.setIconAt(0,focused);

      }

   }

   public void prevSession() {

      int index = frame.sessionPane.getSelectedIndex();
      frame.sessionPane.setForegroundAt(index,Color.black);
      frame.sessionPane.setIconAt(index,unfocused);

      if (index == 0) {
         frame.sessionPane.setSelectedIndex(frame.sessionPane.getTabCount() - 1);
         frame.sessionPane.setForegroundAt(frame.sessionPane.getSelectedIndex(),Color.blue);
         frame.sessionPane.setIconAt(frame.sessionPane.getSelectedIndex(),focused);

      }
      else {
         frame.sessionPane.setSelectedIndex(--index);
         frame.sessionPane.setForegroundAt(index,Color.blue);
         frame.sessionPane.setIconAt(index,focused);

      }
   }

   void closingDown() {

      closingDown(frame);
   }

   void closingDown(JFrame gui) {

      Session jf = null;
      tnvt tvt = null;
      Sessions sess = manager.getSessions();

      System.out.println("number of active sessions we have " + sessionsV.size());
      int x = 0;
      while (sess.getCount() > 0) {

         jf = sess.item(0);

         System.out.println("session found and closing down");
         manager.closeSession(jf);
         System.out.println("disconnecting socket");
         System.out.println("socket closed");
         jf = null;

      }

      System.out.println("number of active sessions we have after shutting down " + sess.getCount());

      try {
         FileOutputStream out = new FileOutputStream("sessions");
         // save off the width and height to be restored later
         sessions.setProperty("emul.width",Integer.toString(gui.getWidth()));
         sessions.setProperty("emul.height",Integer.toString(gui.getHeight()));

         sessions.store(out,"------ Defaults --------");
      }
      catch (FileNotFoundException fnfe) {}
      catch (IOException ioe) {}

      if (sessionsV.size() == 0) {
         if (strapper != null) {
            strapper.interrupt();
         }
         System.exit(0);
      }


   }

   protected void parseArgs(String theStringList, String[] s) {
      int x = 0;
      StringTokenizer tokenizer = new StringTokenizer(theStringList, " ");
      while (tokenizer.hasMoreTokens()) {
         s[x++] = tokenizer.nextToken();
      }
   }

   protected static Locale parseLocal(String localString) {
      int x = 0;
      String[] s = {"","",""};
      StringTokenizer tokenizer = new StringTokenizer(localString, "_");
      while (tokenizer.hasMoreTokens()) {
         s[x++] = tokenizer.nextToken();
      }
      return new Locale(s[0],s[1],s[2]);
   }

   protected static void loadSessions() {

      FileInputStream in = null;
      try {
         in = new FileInputStream("sessions");
         sessions.load(in);

      }
      catch (FileNotFoundException fnfe) {System.out.println(fnfe.getMessage());}
      catch (IOException ioe) {System.out.println(ioe.getMessage());}

   }

   protected void closeSession(Session targetSession) {

      int tabs = frame.sessionPane.getTabCount();
      Sessions sessions = manager.getSessions();
      Session session = null;

      if (tabs > 1) {

         if ((sessions.item(targetSession)) != null) {

            int index = frame.sessionPane.indexOfComponent(targetSession);
            System.out.println("session found and closing down " + index);
            frame.sessionPane.remove(index);
            manager.closeSession(targetSession);
            targetSession = null;
            if (index < (tabs - 2)) {
               frame.sessionPane.setSelectedIndex(index);
               frame.sessionPane.setForegroundAt(index,Color.blue);
               frame.sessionPane.setIconAt(index,focused);
            }
            else {

               frame.sessionPane.setSelectedIndex(0);
               frame.sessionPane.setForegroundAt(0,Color.blue);
               frame.sessionPane.setIconAt(0,focused);

            }

         }
      }
      else {
         closingDown(frame);
      }
   }

   /**
    * This routine will extract image resources from jar file and create
    * an ImageIcon
    */
   protected ImageIcon createImageIcon (String image) {
      URL file=null;

      try {
         ClassLoader cl = this.getClass().getClassLoader();
         file = cl.getResource(image);

      }
      catch (Exception e) {
         System.err.println(e);
      }
      return new ImageIcon( file);
   }

   /**
    * This routine will read our jar file and extract the image data from
    *    the jar file.  If the file is found it will then create the image
    *    data from the data bytes read
    *
    * I think I got this example from the JavaWorld site or maybe from another
    * magazine I can not remember.  I took it from another program I had written
    * which had the same functionality.
    *
    */
//   protected ImageIcon createImageIcon(String image) {
//
//      String jarFileName = "my5250.jar";
//      int zeSize = 0;
//      byte[] b= null;
//      ImageIcon ii = null;
//
//      try {
//
//         ZipFile zf = new ZipFile("my5250.jar");
//
//         Enumeration e = zf.entries();
//         ZipEntry ze = null;
//
//         while (e.hasMoreElements()) {
//
//            ze = (ZipEntry)e.nextElement();
//            if (ze.getName().equals(image)) {
//               zeSize = (int)ze.getSize();
//            }
//         }
//
//         // extract the resource if found
//         FileInputStream fis=new FileInputStream(jarFileName);
//         BufferedInputStream bis=new BufferedInputStream(fis);
//         ZipInputStream zis=new ZipInputStream(bis);
//         ze=null;
//
//         while ((ze=zis.getNextEntry())!=null) {
//            if (ze.isDirectory()) {
//               continue;
//            }
//
//            // check if the resource entry read is the one we are looking for
//            if (ze.getName().equals(image)) {
//               int size=(int)ze.getSize();
//
//               // -1 means unknown size so default to one found from above.
//               if (size==-1) {
//                  size = zeSize;
//               }
//
//               b=new byte[(int)size];
//               int rb=0;
//               int chunk=0;
//               while (((int)size - rb) > 0) {
//                  chunk=zis.read(b,rb,(int)size - rb);
//                  if (chunk==-1) {
//                     break;
//                  }
//                  rb+=chunk;
//               }
//               ii = new ImageIcon(b);
//            }
//         }
//         zis.close();
//         fis.close();
//
//      }
//      catch( ZipException zexc) {
//         System.out.println("ze " + zexc.getMessage());
//      }
//      catch( IOException ioe) {
//         System.out.println("ioe " + ioe.getMessage());
//      }
//
//      return ii;
//   }

}