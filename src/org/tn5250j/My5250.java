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
package org.tn5250j;

import java.util.*;
import java.io.*;
import javax.swing.UIManager;
import javax.swing.*;

import java.awt.*;
import java.net.*;

import org.apache.log4j.Logger;
import org.tn5250j.tools.*;
import org.tn5250j.tools.system.OperatingSystem;
import org.tn5250j.event.*;
import org.tn5250j.gui.TN5250jSplashScreen;
import org.tn5250j.interfaces.GUIViewInterface;
import org.tn5250j.interfaces.ConfigureFactory;

public class My5250 implements BootListener,TN5250jConstants,SessionListener,
                                 EmulatorActionListener {

   protected GUIViewInterface frame1;
   private String[] sessionArgs = null;
   private static Properties sessions = new Properties();
   private static BootStrapper strapper = null;
   protected SessionManager manager;
   private static Vector frames;
   private static boolean useMDIFrames;
   private TN5250jSplashScreen splash;
   private int step;
   private static String jarClassPaths;	
   private Logger log = Logger.getLogger(this.getClass());
   
   My5250 () {

//      classLoader = this.getClass().getClassLoader();

      splash = new TN5250jSplashScreen("tn5250jSplash.jpg");
      splash.setSteps(5);
      splash.setVisible(true);

      loadLookAndFeel();


      loadSessions();
      splash.updateProgress(++step);

      initJarPaths();

      initScripting();

      // sets the starting frame type.  At this time there are tabs which is
      //    default and Multiple Document Interface.
      startFrameType();

      frames = new Vector();

      newView();

      setDefaultLocale();
      manager = SessionManager.instance();
      splash.updateProgress(++step);

   }


   private void loadLookAndFeel() {

      // we only want to try and load the Kunststoff look and feel if it is not
      //  for the MAC operating system.
      if (!OperatingSystem.isMacOS()) {

         try  {
            UIManager.setLookAndFeel("com.incors.plaf.kunststoff.KunststoffLookAndFeel");
         }
         catch(Exception e) {
            try {
               UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException e1) {
               e1.printStackTrace();
            } catch (InstantiationException e1) {
               e1.printStackTrace();
            } catch (IllegalAccessException e1) {
               e1.printStackTrace();
            } catch (UnsupportedLookAndFeelException e1) {
               e1.printStackTrace();
            }
         }

      }
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
	  log.info(" boot options received " + bootEvent.getNewSessionOptions());
      //System.out.println(" boot options received " + bootEvent.getNewSessionOptions());

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

      if (isSpecified("-MDI",args)) {
         useMDIFrames = true;
      }

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
            int width = m.frame1.getWidth();
            int height = m.frame1.getHeight();

            if (isSpecified("-width",args)) {
               width = Integer.parseInt(My5250.getParm("-width",args));
            }
            if (isSpecified("-height",args)) {
               height = Integer.parseInt(My5250.getParm("-height",args));
            }

            m.frame1.setSize(width,height);
            m.frame1.centerFrame();


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
            }
            LangTool.init();

               if (isSpecified("-s",args))
                  m.sessionArgs = args;
               else
                  m.sessionArgs = null;
//            }
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

      if (sessionArgs == null  || sess.getCount() > 0
               || sessions.containsKey("emul.showConnectDialog")) {

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

         newSession(sel,sessionArgs);

      }
   }

   void startDuplicateSession(Session ses) {

      loadSessions();
      if (ses == null) {
         Sessions sess = manager.getSessions();
         for (int x = 0; x < sess.getCount(); x++) {

            if (sess.item(x).isVisible()) {

               ses = sess.item(x);
               break;
            }
         }
      }

      String selArgs = sessions.getProperty(ses.getSessionName());
      sessionArgs = new String[NUM_PARMS];
      parseArgs(selArgs, sessionArgs);

      newSession(ses.getSessionName(),sessionArgs);
   }

   private String getConnectSession () {

      Connect sc = new Connect(frame1,LangTool.getString("ss.title"),sessions);

      // load the new session information from the session property file
      loadSessions();
      return sc.getConnectKey();
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

      // are we to use a ssl and if we are what type
      if (isSpecified("-sslType",args)) {

         sesProps.put(SSL_TYPE,getParm("-sslType",args));
      }


      // check if device name is specified
      if (isSpecified("-dn=hostname",args)){
         String dnParam;

         // use IP address as device name
         try{
            dnParam = InetAddress.getLocalHost().getHostName();
          }
          catch(UnknownHostException uhe){
            dnParam = "UNKNOWN_HOST";
          }

        sesProps.put(SESSION_DEVICE_NAME ,dnParam);
      }
      else if (isSpecified("-dn",args)){

         sesProps.put(SESSION_DEVICE_NAME ,getParm("-dn",args));
      }

      if (isSpecified("-hb",args))
         sesProps.put(SESSION_HEART_BEAT,"1");

      Session s = manager.openSession(sesProps,propFileName,sel);

      if (!frame1.isVisible()) {
         splash.updateProgress(++step);
         frame1.setVisible(true);
         splash.setVisible(false);
         frame1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
      }
      else {
         if (isSpecified("-noembed",args)) {
            splash.updateProgress(++step);
            newView();
            frame1.setVisible(true);
            splash.setVisible(false);
            frame1.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

         }
      }

      if (isSpecified("-t",args))
         frame1.addSessionView(sel,s);
      else
         frame1.addSessionView(session,s);

      s.connect();

      s.addEmulatorActionListener(this);
   }

   void newView() {

      // we will now to default the frame size to take over the whole screen
      //    this is per unanimous vote of the user base
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

      int width = screenSize.width;
      int height = screenSize.height;

      if (sessions.containsKey("emul.width"))
         width = Integer.parseInt(sessions.getProperty("emul.width"));
      if (sessions.containsKey("emul.height"))
         height = Integer.parseInt(sessions.getProperty("emul.height"));

      if (useMDIFrames)
         frame1 = new Gui5250MDIFrame(this);
//         frame1 = new Gui5250SplitFrame(this);
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

//      frame1.setIcons(focused,unfocused);

      frames.add(frame1);

   }

   private void restoreFrame(GUIViewInterface frame,String location) {

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

   void closingDown(GUIViewInterface view) {

      Session jf = null;
      Sessions sess = manager.getSessions();

      log.info("number of active sessions we have " + sess.getCount());
      int x = 0;

      while (view.getSessionViewCount() > 0) {

         jf = view.getSessionAt(0);

         log.info("session found and closing down");
         view.removeSessionView(jf);
         manager.closeSession(jf);
         log.info("disconnecting socket");
         log.info("socket closed");
         jf = null;

      }

      sessions.setProperty("emul.frame" + view.getFrameSequence(),
                                    view.getX() + "," +
                                    view.getY() + "," +
                                    view.getWidth() + "," +
                                    view.getHeight());

      frames.remove(view);
      view.dispose();

      log.info("number of active sessions we have after shutting down " + sess.getCount());

      if (sess.getCount() == 0) {

         sessions.setProperty("emul.width",Integer.toString(view.getWidth()));
         sessions.setProperty("emul.height",Integer.toString(view.getHeight()));

         // save off the session settings before closing down
         ConfigureFactory.getInstance().saveSettings(ConfigureFactory.SESSIONS,
                                                      ConfigureFactory.SESSIONS,
                                                      "------ Defaults --------");
         if (strapper != null) {
            strapper.interrupt();
         }
         System.exit(0);
      }


   }

   protected void closeSession(Session targetSession) {

      GUIViewInterface f = getParentView(targetSession);
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

      sessions = ((ConfigureFactory)ConfigureFactory.getInstance()).getProperties(
                     ConfigureFactory.SESSIONS);
   }

   public void onSessionChanged(SessionChangeEvent changeEvent) {

      Session ses = (Session)changeEvent.getSource();

      switch (changeEvent.getState()) {
         case STATE_REMOVE:
            closeSession(ses);
            break;
      }
   }

   public void onEmulatorAction(EmulatorActionEvent actionEvent) {

      Session ses = (Session)actionEvent.getSource();

      switch (actionEvent.getAction()) {
         case EmulatorActionEvent.CLOSE_SESSION:
            closeSession(ses);
            break;
         case EmulatorActionEvent.CLOSE_EMULATOR:
            closingDown(ses);
            break;
         case EmulatorActionEvent.START_NEW_SESSION:
            startNewSession();
            break;
         case EmulatorActionEvent.START_DUPLICATE:
            startDuplicateSession(ses);
            break;
      }
   }

   public GUIViewInterface getParentView(Session session) {

      GUIViewInterface f = null;

      for (int x = 0; x < frames.size(); x++) {
         f = (GUIViewInterface)frames.get(x);
         if (f.containsSession(session))
            return f;
      }

      return null;

   }

   /**
    * Initializes the scripting environment if the jython interpreter exists
    * in the classpath
    */
   private void initScripting() {

      try {
         Class.forName("org.tn5250j.scripting.JPythonInterpreterDriver");
      }
      catch (java.lang.NoClassDefFoundError ncdfe) {
         log.warn("Information Message: Can not find scripting support"
                           + " files, scripting will not be available: "
                           + "Failed to load interpreter drivers " + ncdfe);
      }
      catch (Exception ex) {
         log.warn("Information Message: Can not find scripting support"
                           + " files, scripting will not be available: "
                           + "Failed to load interpreter drivers " + ex);
      }

      splash.updateProgress(++step);

   }

   /**
    * Sets the jar path for the available jars.
    * Sets the python.path system variable to make the jython jar available
    * to scripting process.
    *
    * This needs to be rewritten to loop through and obtain all jars in the
    * user directory.  Maybe also additional paths to search.
    */
   private void initJarPaths() {

      jarClassPaths = System.getProperty("python.path")
                        + File.pathSeparator + "jython.jar"
                        + File.pathSeparator + "jythonlib.jar"
                        + File.pathSeparator + "jt400.jar"
                        + File.pathSeparator + "itext.jar";

      if (sessions.containsKey("emul.scriptClassPath")) {
         jarClassPaths += File.pathSeparator + sessions.getProperty("emul.scriptClassPath");
      }

      if (jarClassPaths != null)
         System.setProperty("python.path",jarClassPaths);

      splash.updateProgress(++step);

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
