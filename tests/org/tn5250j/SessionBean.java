/**
*
* <p>Title: SessionBean</p>
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

package org.tn5250j;

import static org.tn5250j.TN5250jConstants.*;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.plaf.ComponentUI;


public class SessionBean extends SessionGUI {
	
	private static final long serialVersionUID = 1L;

//      // ===========================================================================
//      //                      C o n s t r u c t o r s
//      // ===========================================================================
  public SessionBean(String configurationResource,
                     String sessionName)
  {
    this(new Properties(),
         configurationResource,
         sessionName);
  }


  public SessionBean(Properties sessionProperties,
                     String configurationResource,
                     String sessionName)
  {

//   Session52520 session = new Session5250(sessionProperties, null, sessionName, new SessionConfig(configurationResource, sessionName));
   this(new Session5250(sessionProperties, null, sessionName,
         new SessionConfig(configurationResource, sessionName)));
   //    super(sessionProperties, null, sessionName, new SessionConfig(configurationResource, sessionName));
//    this.sessionProperties = sessionProperties;
//    this.sessionProperties.put(SESSION_LOCALE, Locale.getDefault());
//    this.getConfiguration().addSessionConfigListener(this);
  }

  public SessionBean(Session5250 session) {

    super(session);
    this.sessionProperties = session.sesProps;
    this.sessionProperties.put(SESSION_LOCALE, Locale.getDefault());
    this.getSession().getConfiguration().addSessionConfigListener(this);
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


  public void setVisibilityInterval(int interval)
  {
    failIfConnected();

    this.visibilityInterval = interval;

//    if (this.visibilityInterval <= 0)
//      this.setVisible(true);
//    else
//      this.setVisible(false);
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
              (isConnected() == false))
          {
            try
            {
              Thread.sleep(100);
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
            doVisibility();
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
      if (this.isConnected())
        this.session.getVT().systemRequest("90");
    }
  }

  public void systemRequest(String srCode)
  {
    this.session.getVT().systemRequest(srCode);
  }

  // ===========================================================================
  //             J C o m p o n e n t   I m p l e m e n t a t i o n
  // ===========================================================================
  /**
   * Causes this component to lay out its components. Overruled in this case
   * dynamically adjust the font size to the new layout.
   *
   * @see Container#doLayout
   */
  public void doLayout()
  {
    super.doLayout();
    Rectangle rect = this.getBounds();
    if (prevRect == null)
    {
      prevRect = rect;
      this.resizeMe(); //necessary when it is the first time:  compute the fontsize
      return;
    }
//    if ( (rect.getHeight() < prevRect.getHeight())
//        || (rect.getWidth() < prevRect.getWidth())
//       )
    if ( (rect.getHeight() != prevRect.getHeight())
        || (rect.getWidth() != prevRect.getWidth())
       )
    {
//      //only necessary when it's going smaller
      this.resizeMe();
    }
    prevRect = rect;
  }


  /**
   * If the <code>preferredSize</code> has been set to a
   * non-<code>null</code> value just returns it.
   * Otherwise, the preferred size is calculated from the font size to fill
   * a rectangle of <code>80 x 24</code> character.
   *
   * So this overrules the normal behaviour to delegate the preferred size first
   * to the UI component or the layoutmanager in case the UI returns nothing.
   *
   * @return the value of the <code>preferredSize</code> property
   * @see #setPreferredSize
   * @see ComponentUI
   */
  public Dimension getPreferredSize()
  {
    if (preferredSize == null)
      this.setPreferredSize(SessionBean.deriveOptimalSize(this, this.getFont(), 80, 24));

    return super.getPreferredSize();
  }


  /**
   * Sets the preferred size of this component.
   *
   * @param preferredSize to use when laying out this component.
   *                      If <code>preferredSize</code> is <code>null</code>,
   *                      the UI will be asked for the preferred size.
   *
   * @beaninfo
   *   preferred: true
   *       bound: true
   * description: The preferred size of the component.
   */
  public void setPreferredSize(Dimension preferredSize)
  {
    this.preferredSize = preferredSize;
    super.setPreferredSize(preferredSize);
  }


  //============================================================================
  //            P r i v a t e   M e t h o d s   a n d   F i e l d s
  //============================================================================
  private void connectSimple()
  {
    super.connect();
  }

  private void connectEmbedded()
  {
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

    super.connect();
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

    super.connect();

    this.getScreen().sendKeys(sb.toString());
  }


  private void doAfterSignon()
  {
    if (isSignificant(afterSignon))
      this.getScreen().sendKeys(afterSignon);
  }


  private void doInitialCommand()
  {
    if (isSignificant(initialCommand))
    {
      this.getScreen().sendKeys(initialCommand + MNEMONIC_ENTER);
//      this.getScreen().sendKeys(MNEMONIC_ENTER);
    }
  }


  private void doVisibility()
  {
    if (!isVisible() && (visibilityInterval > 0))
    {
      Timer t = new Timer(visibilityInterval, new DoVisible());
      t.setRepeats(false);
      t.start();
    }
    else if (!isVisible())
    {
//      this.setVisible(true);
      new DoVisible().run();
    }
  }


  private boolean isFieldLength(String param)
  {
    return ( (param != null) && (param.length() == 10) );
  }

  private void failIfConnected()
  {
    if ((session.getVT() != null) && (isConnected()))
      throw new IllegalStateException("Cannot change property after being connected!");
  }


  private void failIfNot10(String param)
  {
    if ( (param != null) && (param.length() > 10))
      throw new IllegalArgumentException("The length of the parameter cannot exceed 10 positions!");
  }


  private Dimension preferredSize;
  private Rectangle prevRect;
  private Properties sessionProperties;

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


  private static Dimension deriveOptimalSize(JComponent comp, Font f
                                             , int nrChars, int nrLines)
  {
    return deriveOptimalSize(comp, f, comp.getBorder(), nrChars, nrLines);
  }


  private static Dimension deriveOptimalSize(JComponent comp, Font f
                                             , Border brdr, int nrChars
                                             , int nrLines)
  {
    if (comp == null)
      return null;

    FontMetrics fm = null;
    Graphics g = comp.getGraphics();

    if (g != null)
      fm = g.getFontMetrics(f);
    else
      fm = comp.getFontMetrics(f);

    Insets insets = (brdr == null) ? new Insets(0, 0, 0, 0)
                                   : brdr.getBorderInsets(comp);
    int height = (fm.getHeight() * nrLines) + insets.top+ insets.bottom;
    int width = (nrChars * fm.charWidth('M')) + insets.left + insets.right;

    return new Dimension(width + 2, height);
  }

  public void setNoSaveConfigFile()
  {
    this.sesConfig.removeProperty("saveme");
//     sessionProperties.remove("saveme");
  }


  private class DoVisible
      implements ActionListener, Runnable
  {
    public void actionPerformed(ActionEvent event)
    {
      SwingUtilities.invokeLater(this);
    }


    public void run()
    {
      SessionBean.this.setVisible(true);
      SessionBean.this.resizeMe();
      SessionBean.this.requestFocusInWindow();
    }
  }
}
