/**
*
* <p>Title: ProtocolBean</p>
* <p>Description: TN5250 Session as a bean with auto signon features</p>
* <p>Copyright: Copyright (c) 2000 - 2004</p>
* <p>
*
* @author Luc Gorrens
* @version 1.0
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
* </p>
*/

package org.tn5250j.beans;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;

import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.Session5250;
import org.tn5250j.event.ScreenListener;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.SessionConfig;
import org.tn5250j.framework.common.*;


public class ProtocolBean implements TN5250jConstants {

//      // ===========================================================================
//      //                      C o n s t r u c t o r s
//      // ===========================================================================

  public ProtocolBean(String configurationResource,
                     String sessionName)
  {
    this(new Properties(),
         configurationResource,
         sessionName);
  }


  public ProtocolBean(Properties sessionProperties,
                     String configurationResource,
                     String sessionName)
  {

      session = new Session5250(sessionProperties, null, sessionName,
         new SessionConfig(configurationResource, sessionName));
      this.sessionProperties = sessionProperties;
      SessionManager.instance().addSession(session);
  }

  public Session5250 getSession() {

     return session;

  }

  // ===========================================================================
  //               E m u l a t o r   I m p l e m e n t a t i o n
  // ===========================================================================
  public void setHostName(String hostName)
      throws UnknownHostException, IllegalStateException
  {
    failIfConnected();

    //props.put(SESSION_HOST, hostName);
    this.setIPAddress(InetAddress.getByName(hostName).getHostAddress());
  }


  public void setIPAddress(String ipAddress)
      throws IllegalStateException
  {
    failIfConnected();

    if (isSignificant(ipAddress))
      sessionProperties.put(SESSION_HOST, ipAddress);
  }


  public void setIPPort(int ipPort)
  {
    failIfConnected();

    if (ipPort < 0)
      throw new IllegalArgumentException("An IP port must be greate or equal to 0!");

    sessionProperties.put(SESSION_HOST_PORT, Integer.toString(ipPort));
  }


  public void setCodePage(String charEncoding)
  {
    failIfConnected();

    if (isSignificant(charEncoding))
    {
      if (charEncoding.startsWith("Cp"))
        charEncoding = charEncoding.substring(2);
      sessionProperties.put(SESSION_CODE_PAGE, charEncoding);
    }
  }

  public void setDeviceName(String deviceName)
  {
    failIfConnected();

    if (isSignificant(deviceName))
      sessionProperties.put(SESSION_DEVICE_NAME, deviceName);
  }


  public void setScreenSize(String screenSize)
  {
    failIfConnected();

    if ("27x132".equals(screenSize))
      sessionProperties.put(SESSION_SCREEN_SIZE, SCREEN_SIZE_27X132_STR);
    else
      sessionProperties.put(SESSION_SCREEN_SIZE, SCREEN_SIZE_24X80_STR);
  }


  public void setSignonEmbedded(boolean embed)
  {
    failIfConnected();

    this.embeddedSignon = embed;
  }


  public void setSignonUser(String user)
  {
    failIfConnected();
    failIfNot10(user);

    this.user = user;
  }


  public void setSignonPassword(String password)
  {
    failIfConnected();
    failIfNot10(password);

    this.password = password;
  }


  public void setSignonLibrary(String library)
  {
    failIfConnected();
    failIfNot10(library);

    this.library = library;
  }


  public void setSignonMenu(String menu)
  {
    failIfConnected();
    failIfNot10(menu);

    this.menu = menu;
  }


  public void setSignonProgram(String program)
  {
    failIfConnected();
    failIfNot10(program);

    this.program = program;
  }


  public void setAfterSignonMacro(String macro)
  {
    failIfConnected();

    this.afterSignon = macro;
  }


  public void setInitialCommand(String command)
  {
    failIfConnected();

    this.initialCommand = command;
  }

  public void connect()
  {
    failIfConnected();

    if (!isSignificant(this.user))
    {
      connectSimple();
    }
    else
    {
      if (this.embeddedSignon)
        connectEmbedded();
      else
        connectSimulated();

      Runnable runnable = new Runnable()
      {
        int tryConnection;
        public void run()
        {
          if ((tryConnection++ < 30) &&     //If it is still not connected after 3 seconds,
                                          //stop with trying
              (session.isConnected() == false))
          {
            try
            {
              Thread.currentThread().sleep(100);
            }
            catch (InterruptedException ex)
            {
              ;
            }
            SwingUtilities.invokeLater(this);
          }
          else
          {
            doAfterSignon();
            doInitialCommand();
          }
        }
      };
      runnable.run();
    }
  }

  public void signoff()
  {
    if (session.getVT() != null)
    {
      if (session.isConnected())
        session.getVT().systemRequest("90");
    }
  }

  public void systemRequest(String srCode)
  {
    session.getVT().systemRequest(srCode);
  }

  //============================================================================
  //            P r i v a t e   M e t h o d s   a n d   F i e l d s
  //============================================================================
  private void connectSimple()
  {
    session.connect();
  }

  private void connectEmbedded()
  {

	 // We will now see if there are any bypass signon parameters to be
	 //    processed. The system properties override these parameters so
	 //    have precidence if specified.
//    Properties props = session.getConfiguration().getProperties();

    if (isSignificant(user))
      sessionProperties.put("SESSION_CONNECT_USER", user);

    if (password != null)
      sessionProperties.put("SESSION_CONNECT_PASSWORD", password);

    if (isSignificant(program))
      sessionProperties.put("SESSION_CONNECT_PROGRAM", program);

    if (isSignificant(menu))
      sessionProperties.put("SESSION_CONNECT_MENU", menu);

    if (isSignificant(library))
      sessionProperties.put("SESSION_CONNECT_LIBRARY", library);

    session.connect();
  }


  private void connectSimulated()
  {
    StringBuffer sb = new StringBuffer();

    if (isSignificant(user))
      sb.append(user);
    if (!isFieldLength(user))
      sb.append(MNEMONIC_TAB);

    if (isSignificant(password))
      sb.append(password);
    if (!isFieldLength(password))
      sb.append(MNEMONIC_TAB);

    // First we test if we have something signicant to send.
    // If so, we presume we have a standard IBM login screen!
    if (isSignificant(program) || isSignificant(menu) || isSignificant(library))
    {
      if (isSignificant(program))
        sb.append(program);
      if (!isFieldLength(program))
        sb.append(MNEMONIC_TAB);

      if (isSignificant(menu))
        sb.append(menu);
      if (!isFieldLength(menu))
        sb.append(MNEMONIC_TAB);

      if (isSignificant(library))
        sb.append(library);
    }
    sb.append(MNEMONIC_ENTER);

    session.connect();

    session.getScreen().sendKeys(sb.toString());
  }


  private void doAfterSignon()
  {
    if (isSignificant(afterSignon))
      session.getScreen().sendKeys(afterSignon);
  }


  private void doInitialCommand()
  {
    if (isSignificant(initialCommand))
    {
      session.getScreen().sendKeys(initialCommand + MNEMONIC_ENTER);
    }
  }


  private boolean isFieldLength(String param)
  {
    return ( (param != null) && (param.length() == 10) );
  }

  private void failIfConnected()
  {
    if ((session != null) && (session.getVT() != null) && (session.isConnected()))
      throw new IllegalStateException("Cannot change property after being connected!");
  }


  private void failIfNot10(String param)
  {
    if ( (param != null) && (param.length() > 10))
      throw new IllegalArgumentException("The length of the parameter cannot exceed 10 positions!");
  }


  private Properties sessionProperties;
  private Session5250 session;

  private boolean embeddedSignon;
  private String user;
  private String password;
  private String library;
  private String menu;
  private String program;
  private String initialCommand;
  private String afterSignon;
  private int visibilityInterval;

  //============================================================================
  //                    U t i l i t y   M e t h o d s
  //============================================================================
  private static boolean isSignificant(String param)
  {
    if ( (param != null) && (param.length() != 0))
      return true;

    return false;
  }


}
