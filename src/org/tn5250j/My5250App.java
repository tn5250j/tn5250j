package org.tn5250j;

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import javax.swing.*;
import java.util.*;
import org.tn5250j.tools.LangTool;

public class My5250App extends JApplet implements TN5250jConstants {
   boolean isStandalone = true;
   private SessionManager manager;

   /**Get a parameter value*/
   public String getParameter(String key, String def) {

      return isStandalone ? System.getProperty(key, def) :
         (getParameter(key) != null ? getParameter(key) : def);
   }

   /**Construct the applet*/
   public My5250App() {

   }
   /**Initialize the applet*/
   public void init() {
      try {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }
   }

   /**Component initialization*/
   private void jbInit() throws Exception {
      this.setSize(new Dimension(400,300));

      Properties sesProps = new Properties();

      // Start loading properties - Host must exist
      sesProps.put(SESSION_HOST,getParameter("host"));

      if (isSpecified("-e"))
         sesProps.put(SESSION_TN_ENHANCED,"1");

      if (isSpecified("-p")) {
         sesProps.put(SESSION_HOST_PORT,getParameter("-p"));
      }

//      if (isSpecified("-f",args))
//         propFileName = getParm("-f",args);

      if (isSpecified("-cp"))
         sesProps.put(SESSION_CODE_PAGE ,getParameter("-cp"));

      if (isSpecified("-gui"))
         sesProps.put(SESSION_USE_GUI,"1");

      if (isSpecified("-132"))
         sesProps.put(SESSION_SCREEN_SIZE,SCREEN_SIZE_27X132_STR);
      else
         sesProps.put(SESSION_SCREEN_SIZE,SCREEN_SIZE_24X80_STR);

      // socks proxy host argument
      if (isSpecified("-sph")) {
         sesProps.put(SESSION_PROXY_HOST ,getParameter("-sph"));
      }

      // socks proxy port argument
      if (isSpecified("-spp"))
         sesProps.put(SESSION_PROXY_PORT ,getParameter("-spp"));

      // check if device name is specified
      if (isSpecified("-dn"))
         sesProps.put(SESSION_DEVICE_NAME ,getParameter("-dn"));

      if (isSpecified("-L"))
         LangTool.init(parseLocale(getParameter("-L")));
      else
         LangTool.init();

      manager = new SessionManager();
      Session s = manager.openSession(sesProps,"","Test Applet");
      this.getContentPane().add(s);
      s.connect();

   }

   /**Get Applet information*/
   public String getAppletInfo() {
      return "tn5250j - Jave tn5250 Client";
   }

   /**Get parameter info*/
   public String[][] getParameterInfo() {
      return null;
   }

   /**
    * Tests if a parameter was specified or not.
    */
   private boolean isSpecified(String parm) {

      if (getParameter(parm) != null) {
         System.out.println("Parameter " + parm + " is specified as: " + getParameter(parm));
         return true;
      }
      return false;
   }

   /**
    * Returns a local specified by the string localString
    */
   protected static Locale parseLocale(String localString) {
      int x = 0;
      String[] s = {"","",""};
      StringTokenizer tokenizer = new StringTokenizer(localString, "_");
      while (tokenizer.hasMoreTokens()) {
         s[x++] = tokenizer.nextToken();
      }
      return new Locale(s[0],s[1],s[2]);
   }

   //static initializer for setting look & feel
   static {
      try {
         //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         //UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
      }
      catch(Exception e) {
      }
   }
}