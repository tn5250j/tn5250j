/*
 * OperatingSystem.java - OS detection
 * Copyright (C) 2002 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.tn5250j.tools.system;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Properties;

import org.tn5250j.ExternalProgramConfig;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

/**
 * Operating system detection routines.
 * @author Slava Pestov
 * @version $Id$
 * @since jEdit 4.0pre4
 */
public class OperatingSystem
{

   private static final TN5250jLogger LOG =
         TN5250jLogFactory.getLogger("org.tn5250j.tools.system.OperatingSystem");

   public static final Rectangle getScreenBounds()
   {
      int screenX = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
      int screenY = (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
      int x, y, w, h;

      if (isMacOS())
      {
         x = 0;
         y = 22;
         w = screenX;
         h = screenY - y - 4;//shadow size
      }
      else if (isWindows())
      {
         x = -4;
         y = -4;
         w = screenX - 2*x;
         h = screenY - 2*y;
      }
      else
      {
         x = 0;
         y = 0;
         w = screenX;
         h = screenY;
      }

      return new Rectangle(x,y,w,h);
   }

   //{{{ isWindows() method
   /**
    * Returns if we're running Windows 95/98/ME/NT/2000/XP.
    */
   public static final boolean isWindows()
   {
      return os == WINDOWS_9x || os == WINDOWS_NT;
   } //}}}

   //{{{ isUnix() method
   /**
    * Returns if we're running Unix (this includes MacOS X).
    */
   public static final boolean isUnix()
   {
      return os == UNIX || os == MAC_OS_X || os == LINUX;
   } //}}}

   //{{{ isMacOS() method
   /**
    * Returns if we're running MacOS X.
    */
   public static final boolean isMacOS()
   {
      return os == MAC_OS_X;
   } //}}}

   //{{{ isJava14() method
   /**
    * Returns if Java 2 version 1.4 is in use.
    */
   public static final boolean hasJava14()
   {
      return java14;
   }

   /**
    * From JavaWorld Tip 66 -
    * http://www.javaworld.com/javaworld/javatips/jw-javatip66.html Display a
    * file in the system browser. If you want to display a file, you must
    * include the absolute path name.
    *
    * @param url the file's url (the url must start with either "http://",
    *           "https://","mailto:" or "file://").
    */
   public static void displayURL(String url) {
      // Check Customized External Program first
	  if(launchExternalProgram(url)) return;

      // first let's check if we have an external protocol program defined
      String command = null;
      String protocol = "";
      java.net.URL urlUrl = null;

      try {
         urlUrl = new java.net.URL(url);
         protocol = urlUrl.getProtocol();
         if (protocol.startsWith("http"))
            protocol = "http";
      }
      catch (MalformedURLException e) {
         LOG.warn(e.getMessage());
      }

      Properties props = ConfigureFactory.getInstance().getProperties(ConfigureFactory.SESSIONS);

      // We now check if we have a property defined for the external program to
      //   handle this protocol.
      if (props.getProperty("emul.protocol." + protocol,"").trim().length() > 0) {
         String commandTemplate = props.getProperty("emul.protocol." + protocol).trim();

         Object[] urlParm = new Object[1];
         urlParm[0] = url;
         if (commandTemplate.lastIndexOf("{0}") == -1)
            commandTemplate += " {0}";
         java.text.MessageFormat format = new java.text.MessageFormat(commandTemplate);
         try {
            command = format.format(urlParm);
         }
         catch (Exception exx) {
            LOG.warn("Unable to parse the url " + url + " using command " +
                  commandTemplate);
         }
      }

      // execute the command if there was one if not then fall through to generic
      //   processing.
      if (command != null && command.trim().length() > 0) {

         execute(command);

      }
      else {
         boolean windows = isWindows();
         String cmd = null;
         try {
            if (windows) {
               // cmd = 'rundll32 url.dll,FileProtocolHandler http://...'
               cmd = WIN_PATH + " " + WIN_FLAG + " " + url;
               Process p = Runtime.getRuntime().exec(cmd);
            }
            else {
               // Under Unix, Netscape has to be running for the "-remote"
               // command to work. So, we try sending the command and
               // check for an exit value. If the exit command is 0,
               // it worked, otherwise we need to start the browser.
               // cmd = 'netscape -remote openURL(http://www.javaworld.com)'
               cmd = UNIX_PATH + " " + UNIX_FLAG + "(" + url + ")";
               Process p = Runtime.getRuntime().exec(cmd);
               try {
                  // wait for exit code -- if it's 0, command worked,
                  // otherwise we need to start the browser up.
                  int exitCode = p.waitFor();
                  if (exitCode != 0) {
                     // Command failed, start up the browser
                     // cmd = 'netscape http://www.javaworld.com'
                     cmd = UNIX_PATH + " " + url;
                     p = Runtime.getRuntime().exec(cmd);
                  }
               }
               catch (InterruptedException x) {
                  LOG.warn("Error bringing up browser, cmd='" + cmd + "'");
                  LOG.warn("Caught: " + x);
               }
            }
         }
         catch (java.io.IOException x) {
            // couldn't exec browser
            LOG.warn("Could not invoke browser, command=" + cmd);
            LOG.warn("Caught: " + x);
         }
      }
   }

	 /**
	 * @param url
	 * @return true when found external program and has been launched; false when not found external program.
	 */
   	private static boolean launchExternalProgram(String url){
      // first let's check if we have an external protocol program defined
	  try {
		  	Properties properties = ExternalProgramConfig.getInstance().getEtnPgmProps();
			String count = properties.getProperty("etn.pgm.support.total.num");
			if(count != null && count.length() > 0){
				int total = Integer.parseInt(count);
				for(int i=1;i<=total;i++){
					String program = properties.getProperty("etn.pgm."+i+".command.name");
					if(url.toLowerCase().startsWith(program.toLowerCase())){
					  String params = url.substring(program.length() + 1);
					  params = params.replace(',',' ');
					  String command;
					  if (isWindows()) {
						  command=properties.getProperty("etn.pgm."+i+".command.window")+" "+params;
					  }else{
						  command=properties.getProperty("etn.pgm."+i+".command.unix")+" "+params;
					  }
					  LOG.info("Execute External Program: "+command);
					  execute(command);
					  return true;
				    }
				}
			}
	  }catch(Exception exx){
			  LOG.warn("Unable to run External Program: "+ exx.getMessage());
	  }
	  return false;
   }

   public static int execute(String command) {

      int exitCode = -1;

      try {
         LOG.info("Executing command='" + command + "'");

         Process p = Runtime.getRuntime().exec(command);
         exitCode = 0;
         // wait for exit code -- if it's 0, command worked,
//         exitCode = p.waitFor();
//         if (exitCode != 0) {
//            log.warn("Error processing command, command='" + command + "'");
//         }
      }
//      catch (InterruptedException exc) {
//         log.warn("Error processing command, command='" + command + "'");
//         log.warn("Caught: " + exc.getMessage());
//      }
      catch (IOException ioe) {
         LOG.warn("Error processing command, command='" + command + "'");
         LOG.warn("Caught: " + ioe.getMessage());
      }
      return exitCode;
   }
 //}}}

   //{{{ Private members
    // The default system browser under windows.
    private static final String WIN_PATH = "rundll32";
    // The flag to display a url.
    private static final String WIN_FLAG = "url.dll,FileProtocolHandler";
    // The default browser under unix.
    private static final String UNIX_PATH = "netscape";
    // The flag to display a url.
    private static final String UNIX_FLAG = "-remote openURL";
   private static final int UNIX = 0x31337;
   private static final int WINDOWS_9x = 0x640;
   private static final int WINDOWS_NT = 0x666;
   private static final int OS2 = 0xDEAD;
   private static final int MAC_OS_X = 0xABC;
   private static final int UNKNOWN = 0xBAD;
   private static final int LINUX = 0x1337;

   private static int os;
   private static boolean java14;

   //{{{ Class initializer
   static
   {
      if(System.getProperty("mrj.version") != null)
      {
         os = MAC_OS_X;
      }
      else
      {
         String osName = System.getProperty("os.name");
         if(osName.indexOf("Windows 9") != -1
            || osName.indexOf("Windows M") != -1)
         {
            os = WINDOWS_9x;
         }
         else if(osName.indexOf("Windows") != -1)
         {
            os = WINDOWS_NT;
         }
         else if(osName.indexOf("OS/2") != -1)
         {
            os = OS2;
         }
         else if(File.separatorChar == '/')
         {
            os = UNIX;
         }
         else if(osName.toLowerCase().indexOf("linux") != -1)
         {
            os = LINUX;
         }
         else
         {
            os = UNKNOWN;
            LOG.warn("Unknown operating system: " + osName);
         }
      }

      if(System.getProperty("java.version").compareTo("1.4") >= 0)
         java14 = true;

   } //}}}

   //}}}


}
