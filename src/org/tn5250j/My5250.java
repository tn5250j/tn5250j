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
import org.tn5250j.gui.TN5250jSplashScreen;
import java.net.*;


public class My5250 implements BootListener,TN5250jConstants,SessionListener {

   protected Gui5250Frame frame1;
   private String[] sessionArgs = null;
   private static Properties sysConfig = new Properties(); // Sys configuration
   private static Properties sessions = new Properties();
   private static Properties sesProps = new Properties();
   private static ImageIcon focused;
   private static ImageIcon unfocused;
   private static ImageIcon tnicon;

   private static BootStrapper strapper = null;
   private SessionManager manager;
   private static Vector frames;
   private static boolean useMDIFrames;
   private static TN5250jSplashScreen splash;
   private int step;

   My5250 () {

      splash = new TN5250jSplashScreen(createImageIcon("tn5250jSplash.jpg"));
      splash.setSteps(4);
      splash.setVisible(true);

      focused = createImageIcon("focused.gif");
      unfocused = createImageIcon("unfocused.gif");
      tnicon = createImageIcon("tnicon.jpg");

      try  {
         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      }
      catch(Exception e) {
      }

      splash.updateProgress(++step);
      try {
         Class.forName("org.tn5250j.scripting.JPythonInterpreterDriver");
      }
      catch (java.lang.NoClassDefFoundError ncdfe) {
         System.out.println("Information Message: Can not find scripting support"
                           + " files, scripting will not be available: "
                           + "Failed to load interpreter drivers " + ncdfe);
      }
      catch (Exception ex) {
         System.out.println("Information Message: Can not find scripting support"
                           + " files, scripting will not be available: "
                           + "Failed to load interpreter drivers " + ex);
      }

      splash.updateProgress(++step);

      // sets the starting frame type.  At this time there are tabs which is
      //    default and Multiple Document Interface.
      startFrameType();

      frames = new Vector();

      newView();

      setDefaultLocale();
      manager = new SessionManager();
      manager.setController(this);
      splash.updateProgress(++step);

   }

   /**
    *
    * Initialize system configuration
    */
   static public void initConfig(String[] args) {



     // Start loading properties from file
     loadDefaultSession();

     // Start loading session properties
     boolean loadDefaultParameters = false;

     if (args.length != 0) {
       if (args[0].indexOf("-") == -1 & args[0].length() != 0)
         sesProps.put(SESSION_HOST, args[0]);
       else{
         // No host present on the command line. Load it from default session
         sesProps.put(SESSION_TERM_NAME, getDefaultSession());
         String[] defaultArgs = new String[NUM_PARMS];
         parseArgs(sessions.getProperty(sesProps.getProperty(SESSION_TERM_NAME)), defaultArgs);
         sesProps.put(SESSION_HOST, defaultArgs[0]);
       }
     } else {
         // No parameter has been given in the command line. Load default session
         sesProps.put(SESSION_TERM_NAME, getDefaultSession());
         args = new String[NUM_PARMS];
         parseArgs(sessions.getProperty(sesProps.getProperty(SESSION_TERM_NAME)), args);
         sesProps.put(SESSION_HOST, args[0]);
     }
     if (isSpecified("-MDI",args))
        sesProps.setProperty(GUI_MDI_TYPE, "1");

     if (isSpecified("-width",args))
        sesProps.setProperty(GUI_FRAME_WIDTH, getParm("-width",args));

     if (isSpecified("-L",args))
        sesProps.setProperty(SESSION_LOCALE, getParm("-L",args));

     if (isSpecified("-s",args))
        sesProps.setProperty(SESSION_NAMES_REFS, getParm("-s",args));

     if (isSpecified("-height",args))
        sesProps.setProperty(GUI_FRAME_HEIGHT, getParm("-height",args));

     if (isSpecified("-nc",args))
        sesProps.setProperty(NO_CHECK_RUNNING, "1");

     if (isSpecified("-noembed",args))
       sesProps.put(GUI_NO_TAB,"1");

     if (isSpecified("-t",args))
       sesProps.put(SESSION_TERM_NAME_SYSTEM,"1");

     if (isSpecified("-e",args))
        sesProps.put(SESSION_TN_ENHANCED,"1");

     if (isSpecified("-f",args))
        sesProps.put(SESSION_CONFIG_FILE,getParm("-f",args));

     if (isSpecified("-p",args)) {
        sesProps.put(SESSION_HOST_PORT,getParm("-p",args));
     }

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

     if (isSpecified("-dn",args))
        sesProps.put(SESSION_DEVICE_NAME ,getParm("-dn",args));

     if (isSpecified("-d",args))
        sesProps.put(START_MONITOR_THREAD ,"1");


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
         out.flush();
         out.close();
         boot.close();
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

      // Initialize system configuration
      initConfig(args);

      if (sesProps.getProperty(GUI_MDI_TYPE) != null) {
         useMDIFrames = true;
      }

      if (sesProps.getProperty(NO_CHECK_RUNNING) != null) {

         if (!checkBootStrapper(args)) {

            // if we did not find a running instance and the -d options is
            //    specified start up the bootstrap deamon to allow checking
            //    for running instances
           if (sesProps.getProperty(START_MONITOR_THREAD) != null) {
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

      int width = m.frame1.getWidth();
      int height = m.frame1.getHeight();

      if (sesProps.getProperty(GUI_FRAME_WIDTH) != null) {
         width = Integer.parseInt(sesProps.getProperty(GUI_FRAME_WIDTH));
      }
      if (sesProps.getProperty(GUI_FRAME_HEIGHT) != null) {
         height = Integer.parseInt(sesProps.getProperty(GUI_FRAME_HEIGHT));
      }

      m.frame1.setSize(width,height);
      m.frame1.centerFrame();

      // check if a locale parameter is specified on the command line
      if (sesProps.getProperty(SESSION_LOCALE) != null) //{

      Locale.setDefault(parseLocal(sesProps.getProperty(SESSION_LOCALE)));
      m.splash.updateProgress(++m.step);
      m.frame1.setVisible(true);
      m.splash.setVisible(false);
      m.frame1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

      LangTool.init();

      if (sesProps.getProperty(SESSION_NAMES_REFS) != null) {

        // Init parameters for multisession configurations

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

            if (!m.frame1.isVisible()) {
               m.splash.updateProgress(++m.step);
               m.frame1.setVisible(true);
               m.splash.setVisible(false);
               m.frame1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }

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

   static void startFrameType() {

      if (sessions.containsKey("emul.interface")) {
         String s = (String)sessions.getProperty("emul.interface");
         if (s.equalsIgnoreCase("MDI"))
            useMDIFrames = true;

      }
   }

   void startNewSession() {

      int result = 2;
      String sel = "";

      Sessions sess = manager.getSessions();

      if (sess.getCount() > 0 || sessions.containsKey("emul.showConnectDialog")) {

         sel = getConnectSession();

         if (sel != null) {
            String selArgs = sessions.getProperty(sel);
            sessionArgs = new String[NUM_PARMS];
            parseArgs(selArgs, sessionArgs);

            newSession(sel,sessionArgs);
         }
         else {
            if (sess.getCount() == 0)
               System.exit(0);
         }

      }
      else {

         newSession(sesProps.getProperty(SESSION_HOST), null);

      }
   }

   private String getConnectSession () {

      Connect sc = new Connect(frame1,LangTool.getString("ss.title"),sessions);

      // load the new session information from the session property file
      loadDefaultSession();
      return sc.getConnectKey();
   }

   synchronized void newSession(String sel,String[] args) {


      Session s = manager.openSession(sesProps,
                                      sesProps.getProperty(SESSION_CONFIG_FILE),
                                      sesProps.getProperty(SESSION_TERM_NAME));

      if (!frame1.isVisible()) {
         splash.updateProgress(++step);
         frame1.setVisible(true);
         splash.setVisible(false);
         frame1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
      else {
         if (sesProps.getProperty(GUI_NO_TAB) != null) {
            splash.updateProgress(++step);
            newView();
            frame1.setVisible(true);
            splash.setVisible(false);
            frame1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

         }
      }

      frame1.addSessionView(sesProps.getProperty(SESSION_TERM_NAME),s);

      s.connect();
   }

   void newView() {

      int width = 600;
      int height = 500;

      if (sessions.containsKey("emul.width"))
         width = Integer.parseInt(sessions.getProperty("emul.width"));
      if (sessions.containsKey("emul.height"))
         height = Integer.parseInt(sessions.getProperty("emul.height"));

      if (useMDIFrames)
         frame1 = new Gui5250MDIFrame(this);
      else
         frame1 = new Gui5250Frame(this);

      frame1.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

      if (sessions.containsKey("emul.frame" + frame1.getFrameSequence())) {

         String location = sessions.getProperty("emul.frame" + frame1.getFrameSequence());
//         System.out.println(location + " seq > " + frame1.getFrameSequence() );
         restoreFrame(frame1,location);
      }
      else {
         frame1.setSize(width,height);
         frame1.centerFrame();
      }

      frame1.setIconImage(tnicon.getImage());
      frame1.setIcons(focused,unfocused);

      frames.add(frame1);

   }

   private void restoreFrame(Gui5250Frame frame,String location) {

      StringTokenizer tokenizer = new StringTokenizer(location, ",");
      int x = Integer.parseInt(tokenizer.nextToken());
      int y = Integer.parseInt(tokenizer.nextToken());
      int width = Integer.parseInt(tokenizer.nextToken());
      int height = Integer.parseInt(tokenizer.nextToken());

      frame.setLocation(x,y);
      frame.setSize(width,height);
   }

   void closingDown(Session targetSession) {

      closingDown(getParentView(targetSession));
   }

   void closingDown(Gui5250Frame view) {

      Session jf = null;
      Sessions sess = manager.getSessions();

      System.out.println("number of active sessions we have " + sess.getCount());
      int x = 0;

      while (view.getSessionViewCount() > 0) {

         jf = view.getSessionAt(0);

         System.out.println("session found and closing down");
         view.removeSessionView(jf);
         manager.closeSession(jf);
         System.out.println("disconnecting socket");
         System.out.println("socket closed");
         jf = null;

      }


      sessions.setProperty("emul.frame" + view.getFrameSequence(),
                                    view.getX() + "," +
                                    view.getY() + "," +
                                    view.getWidth() + "," +
                                    view.getHeight());

      frames.remove(view);
      view.dispose();

      System.out.println("number of active sessions we have after shutting down " + sess.getCount());

      if (sess.getCount() == 0) {
         try {
            FileOutputStream out = new FileOutputStream("sessions");
            // save off the width and height to be restored later
            sessions.setProperty("emul.width",Integer.toString(view.getWidth()));
            sessions.setProperty("emul.height",Integer.toString(view.getHeight()));

            sessions.store(out,"------ Defaults --------");
         }
         catch (FileNotFoundException fnfe) {}
         catch (IOException ioe) {}

         if (strapper != null) {
            strapper.interrupt();
         }
         System.exit(0);
      }


   }

   protected void closeSession(Session targetSession) {

      Gui5250Frame f = getParentView(targetSession);
      if (f == null)
         return;
      int tabs = f.getSessionViewCount();
      Sessions sessions = manager.getSessions();
      Session session = null;

      if (tabs > 1) {

         if ((sessions.item(targetSession)) != null) {

            f.removeSessionView(targetSession);
            manager.closeSession(targetSession);
            targetSession = null;

         }
      }
      else {
         closingDown(f);
      }
   }

   static protected void parseArgs(String theStringList, String[] s) {
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

   protected static void loadDefaultSession() {

      FileInputStream in = null;
      try {
         in = new FileInputStream("sessions");
         sessions.load(in);

      }
      catch (FileNotFoundException fnfe) {
         System.out.println(" Information Message: " + fnfe.getMessage()
                           + ".  Default sessions file will"
                           + " be created for first time use.");
      }
      catch (IOException ioe) {System.out.println(ioe.getMessage());}

   }

   public void onSessionChanged(SessionChangeEvent changeEvent) {

      Session ses = (Session)changeEvent.getSource();

      switch (changeEvent.getState()) {
         case STATE_REMOVE:
            closeSession(ses);
            break;
      }
   }


   public Gui5250Frame getParentView(Session session) {

      Gui5250Frame f = null;

      for (int x = 0; x < frames.size(); x++) {
         f = (Gui5250Frame)frames.get(x);
         if (f.containsSession(session))
            return f;
      }

      return null;

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
