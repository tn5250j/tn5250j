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
import javax.swing.UIManager;
import java.io.File;

import org.tn5250j.tools.logging.*;

/**
 * Operating system detection routines.
 * @author Slava Pestov
 * @version $Id$
 * @since jEdit 4.0pre4
 */
public class OperatingSystem
{

   private static TN5250jLogger log =
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

   //{{{ isDOSDerived() method
   /**
    * Returns if we're running Windows 95/98/ME/NT/2000/XP, or OS/2.
    */
   public static final boolean isDOSDerived()
   {
      return isWindows() || isOS2();
   } //}}}

   //{{{ isWindows() method
   /**
    * Returns if we're running Windows 95/98/ME/NT/2000/XP.
    */
   public static final boolean isWindows()
   {
      return os == WINDOWS_9x || os == WINDOWS_NT;
   } //}}}

   //{{{ isWindows9x() method
   /**
    * Returns if we're running Windows 95/98/ME.
    */
   public static final boolean isWindows9x()
   {
      return os == WINDOWS_9x;
   } //}}}

   //{{{ isWindowsNT() method
   /**
    * Returns if we're running Windows NT/2000/XP.
    */
   public static final boolean isWindowsNT()
   {
      return os == WINDOWS_NT;
   } //}}}

   //{{{ isOS2() method
   /**
    * Returns if we're running OS/2.
    */
   public static final boolean isOS2()
   {
      return os == OS2;
   } //}}}

   //{{{ isUnix() method
   /**
    * Returns if we're running Unix (this includes MacOS X).
    */
   public static final boolean isUnix()
   {
      return os == UNIX || os == MAC_OS_X || os == LINUX;
   } //}}}

   //{{{ isLinux() method
   /**
    * Returns if we're running Linux (this does not includ Unixes).
    */
   public static final boolean isLinux()
   {
      return os == LINUX;
   } //}}}

   //{{{ isMacOS() method
   /**
    * Returns if we're running MacOS X.
    */
   public static final boolean isMacOS()
   {
      return os == MAC_OS_X;
   } //}}}

   //{{{ isMacOSLF() method
        /**
         * Returns if we're running MacOS X and using the native look and feel.
         */
        public static final boolean isMacOSLF()
        {
                return (isMacOS() && UIManager.getLookAndFeel().isNativeLookAndFeel());
        } //}}}

   //{{{ isJava14() method
   /**
    * Returns if Java 2 version 1.4 is in use.
    */
   public static final boolean hasJava14()
   {
      return java14;
   }
   
   public static final boolean hasJava15()
   {
        return java15;
   }

   /**
    * From JavaWorld Tip 66 - http://www.javaworld.com/javaworld/javatips/jw-javatip66.html
    * Display a file in the system browser.  If you want to display a
    * file, you must include the absolute path name.
    *
    * @param url the file's url (the url must start with either "http://" or
    * "file://").
    */
   public static void displayURL(String url) {
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
               // command to work.  So, we try sending the command and
               // check for an exit value.  If the exit command is 0,
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
                       cmd = UNIX_PATH + " "  + url;
                       p = Runtime.getRuntime().exec(cmd);
                   }
               }
               catch(InterruptedException x) {
                   log.warn("Error bringing up browser, cmd='" +
                   cmd + "'");
                   log.warn("Caught: " + x);
               }
           }
       }
       catch(java.io.IOException x) {
           // couldn't exec browser
           log.warn("Could not invoke browser, command=" + cmd);
           log.warn("Caught: " + x);
       }
   }

 //}}}

   //{{{ Private members
    // Used to identify the windows platform.
    private static final String WIN_ID = "Windows";
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
   private static boolean java15;

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
            log.warn("Unknown operating system: " + osName);
         }
      }

      if(System.getProperty("java.version").compareTo("1.4") >= 0)
         java14 = true;
      
      if(System.getProperty("java.version").compareTo("1.5") >= 0)
         java15 = true;
   } //}}}

   //}}}
}
