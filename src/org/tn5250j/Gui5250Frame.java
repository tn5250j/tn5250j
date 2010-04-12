/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001-2003
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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionListener;
import org.tn5250j.event.TabClosedListener;
import org.tn5250j.gui.ButtonTabComponent;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.interfaces.GUIViewInterface;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

/**
 * This is the main {@link javax.swing.JFrame}, which contains multiple tabs.
 * 
 * @see GUIViewInterface
 */
public class Gui5250Frame extends GUIViewInterface implements
                                                    ChangeListener,
                                                    TabClosedListener,
                                                    SessionListener,
                                                    SessionJumpListener {

   private static final long serialVersionUID = 1L;
   
   private JTabbedPane sessionPane = new JTabbedPane();
   private boolean embedded = false;
   private boolean hideTabBar = false;
   private TN5250jLogger log = TN5250jLogFactory.getLogger (this.getClass());


   //Construct the frame
   public Gui5250Frame(My5250 m) {
      super(m);
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      try  {
         jbInit();
      } catch(Exception e) {
         log.warn("Error during initializing!", e);
      }
   }

   //Component initialization
   private void jbInit() throws Exception  {

      this.getContentPane().setLayout(new BorderLayout());

      // update the frame sequences
      frameSeq = sequence++;
      
      sessionPane.setBorder(BorderFactory.createEtchedBorder());
      sessionPane.setBounds(new Rectangle(78, 57, 5, 5));
      sessionPane.setOpaque(true);
      sessionPane.setRequestFocusEnabled(false);
      sessionPane.setDoubleBuffered(false);

      sessionPane.addChangeListener(this);

      Properties props = ConfigureFactory.getInstance().
                           getProperties(ConfigureFactory.SESSIONS);

      if (props.getProperty("emul.hideTabBar","no").equals("yes"))
         hideTabBar = true;

      if (!hideTabBar) {
         this.getContentPane().add(sessionPane, BorderLayout.CENTER);
      }

      if (packFrame)
         pack();
      else
         validate();


   }

   //Overridden so we can exit on System Close
   protected void processWindowEvent(WindowEvent e) {
      super.processWindowEvent(e);
      if(e.getID() == WindowEvent.WINDOW_CLOSING) {
         me.closingDown(this);
      }
   }


   public void update(Graphics g) {
      paint(g);
   }

   public void onSessionJump(SessionJumpEvent jumpEvent) {

      switch (jumpEvent.getJumpDirection()) {

         case TN5250jConstants.JUMP_PREVIOUS:
            prevSession();
            break;
         case TN5250jConstants.JUMP_NEXT:
            nextSession();
            break;
      }
   }

   private void nextSession() {

      final int index = sessionPane.getSelectedIndex();

      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            int index1 = index;
            if (index1 < sessionPane.getTabCount() - 1) {
               sessionPane.setSelectedIndex(++index1);
            }
            else {
               sessionPane.setSelectedIndex(0);
            }
            updateSessionTitle();
       }
      });

   }

   private void prevSession() {

      final int index = sessionPane.getSelectedIndex();

      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            int index1 = index;
            if (index1 == 0) {
               sessionPane.setSelectedIndex(sessionPane.getTabCount() - 1);
            }
            else {
               sessionPane.setSelectedIndex(--index1);
            }
            updateSessionTitle();
       }
      });
   }
   
   /* (non-Javadoc)
    * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
    */
   public void stateChanged(ChangeEvent e) {
      JTabbedPane p = (JTabbedPane)e.getSource();
      setSessionTitle((SessionGUI)p.getSelectedComponent());
   }

   /**
    * Sets the frame title to the same as the newly selected tab's title.
    * 
    * @param session can be null, but then nothing happens ;-)
    */
   private void setSessionTitle(final SessionGUI session) {
      if (session != null) {
         if (session != null && session.getAllocDeviceName() != null && session.isConnected()) {
            if (sequence - 1 > 0)
               setTitle(session.getAllocDeviceName() + " - tn5250j <" + sequence + ">");
            else
               setTitle(session.getAllocDeviceName() + " - tn5250j");
         }
         else {
            if (sequence - 1 > 0)
               setTitle("tn5250j <" + sequence + ">");
            else
               setTitle("tn5250j");
         }
      } else {
         setTitle("tn5250j");
      }
   }
   
   /**
    * Sets the main frame title to the same as the current selected tab's title.
    * @see {@link #setSessionTitle(SessionGUI)}
    */
   private void updateSessionTitle() {
      SessionGUI selectedComponent = (SessionGUI)this.sessionPane.getSelectedComponent();
      setSessionTitle(selectedComponent);
   }

   /* (non-Javadoc)
    * @see org.tn5250j.interfaces.GUIViewInterface#addSessionView(java.lang.String, org.tn5250j.SessionGUI)
    */
   public void addSessionView(final String tabText, final SessionGUI newsesui) {

      if (hideTabBar && sessionPane.getTabCount() == 0 && !embedded) {
         // put Session just in the main content window and don't create any tabs

         this.getContentPane().add(newsesui, BorderLayout.CENTER);
         newsesui.addSessionListener(this);

         newsesui.resizeMe();
         repaint();
         if (packFrame)
            pack();
         else
            validate();
         embedded = true;
         newsesui.requestFocusInWindow();
         setSessionTitle(newsesui);
      }
      else {

         if (hideTabBar && sessionPane.getTabCount() == 0 ) {
            // remove first component in the main window,
            // create first tab and put first session into first tab 

            SessionGUI firstsesgui = null;
            for (int x=0; x < this.getContentPane().getComponentCount(); x++) {

               if (this.getContentPane().getComponent(x) instanceof SessionGUI) {
                  firstsesgui = (SessionGUI)(this.getContentPane().getComponent(x));
                  this.getContentPane().remove(x);
                  break;
               }
            }

            createTabWithSessionContent(tabText, firstsesgui, false);

            if (firstsesgui.getAllocDeviceName() != null)
               sessionPane.setTitleAt(0,firstsesgui.getAllocDeviceName());
            else
               sessionPane.setTitleAt(0,firstsesgui.getSessionName());

            this.getContentPane().add(sessionPane, BorderLayout.CENTER);
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  repaint();
               }
            });
         }

         createTabWithSessionContent(tabText, newsesui, true);
      }
   }

   /**
    * @param tabText
    * @param sesgui
    * @param focus TRUE is the new tab should be focused, otherwise FALSE
    */
   private final void createTabWithSessionContent(final String tabText, final SessionGUI sesgui, final boolean focus) {
      
      sessionPane.addTab(tabText, determineIconForSession(sesgui.session), sesgui);
      final int idx = sessionPane.indexOfComponent(sesgui);
      // add the [x] to the tab
      final ButtonTabComponent bttab = new ButtonTabComponent(this.sessionPane);
      bttab.addTabCloseListener(this);
      sessionPane.setTabComponentAt(idx, bttab);

      // add listeners
      sesgui.addSessionListener(this);
      sesgui.addSessionJumpListener(this);
      sesgui.addSessionListener(bttab);

      // visual cleanups
      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            sesgui.resizeMe();
            sesgui.repaint();
            if (focus) {
               sessionPane.setSelectedIndex(idx);
               sesgui.requestFocusInWindow();
            }
         }
      });
   }

   /* (non-Javadoc)
    * @see org.tn5250j.event.TabClosedListener#onTabClosed(int)
    */
   public void onTabClosed(int tabToBeClosed){
	   final SessionGUI sessionAt = this.getSessionAt(tabToBeClosed);
	   me.closeSession(sessionAt);
   }

   /* (non-Javadoc)
    * @see org.tn5250j.interfaces.GUIViewInterface#removeSessionView(org.tn5250j.SessionGUI)
    */
   public void removeSessionView(SessionGUI targetSession) {
      if (hideTabBar && sessionPane.getTabCount() == 0) {
         for (int x=0; x < getContentPane().getComponentCount(); x++) {
            if (getContentPane().getComponent(x) instanceof SessionGUI) {
               getContentPane().remove(x);
            }
         }
      }
      else {
         int index = sessionPane.indexOfComponent(targetSession);
         log.info("session found and closing down " + index);
         targetSession.removeSessionListener(this);
         targetSession.removeSessionJumpListener(this);
         sessionPane.remove(index);
      }
   }

   /* (non-Javadoc)
    * @see org.tn5250j.interfaces.GUIViewInterface#getSessionViewCount()
    */
   public int getSessionViewCount() {

      if (hideTabBar && sessionPane.getTabCount() == 0) {
         for (int x=0; x < this.getContentPane().getComponentCount(); x++) {

            if (this.getContentPane().getComponent(x) instanceof SessionGUI) {
               return 1;
            }
         }
         return 0;
      }
      return sessionPane.getTabCount();
   }

   /* (non-Javadoc)
    * @see org.tn5250j.interfaces.GUIViewInterface#getSessionAt(int)
    */
   public SessionGUI getSessionAt( int index) {

      if (hideTabBar && sessionPane.getTabCount() == 0) {
         for (int x=0; x < this.getContentPane().getComponentCount(); x++) {

            if (this.getContentPane().getComponent(x) instanceof SessionGUI) {
               return (SessionGUI)getContentPane().getComponent(x);
            }
         }
         return null;
      }
      if (sessionPane.getTabCount() <= 0) return null;
      return (SessionGUI)sessionPane.getComponentAt(index);
   }

   /* (non-Javadoc)
    * @see org.tn5250j.interfaces.GUIViewInterface#onSessionChanged(org.tn5250j.event.SessionChangeEvent)
    */
   public void onSessionChanged(SessionChangeEvent changeEvent) {

      Session5250 ses5250 = (Session5250)changeEvent.getSource();
      SessionGUI ses = ses5250.getGUI();
      final int tabidx = sessionPane.indexOfComponent(ses);
      // be aware, when the first tab is not shown 
      if (tabidx >= 0 && tabidx < sessionPane.getTabCount()) {
         this.sessionPane.setIconAt(tabidx, determineIconForSession(ses5250));
      }
      switch (changeEvent.getState()) {
         case TN5250jConstants.STATE_CONNECTED:

            final String devname = ses.getAllocDeviceName();
            if (devname != null) {
               if (log.isDebugEnabled()) {
               		this.log.debug("SessionChangedEvent: " + changeEvent.getState() + " " + devname);
               }
               if (tabidx >= 0 && tabidx < sessionPane.getTabCount()) {
                  Runnable tc = new Runnable () {
                     public void run() {
                        sessionPane.setTitleAt(tabidx,devname);
                     }
                  };
                  SwingUtilities.invokeLater(tc);
               }
               updateSessionTitle();
            }
            break;
      }
   }

   /**
    * @param ses5250
    * @return Icon or NULL depending on session State
    */
   private static final Icon determineIconForSession(Session5250 ses5250) {
      if (ses5250 != null && ses5250.isSslConfigured()) {
         if (ses5250.isSslSocket()) {
            return GUIGraphicsUtils.getClosedLockIcon();
         } else {
            return GUIGraphicsUtils.getOpenLockIcon();
         }
      }
      return null;
   }
   
   /* (non-Javadoc)
    * @see org.tn5250j.interfaces.GUIViewInterface#containsSession(org.tn5250j.SessionGUI)
    */
   public boolean containsSession(SessionGUI session) {

      if (hideTabBar && sessionPane.getTabCount() == 0) {
         for (int x=0; x < this.getContentPane().getComponentCount(); x++) {

            if (this.getContentPane().getComponent(x) instanceof SessionGUI) {
               return ((SessionGUI)getContentPane().getComponent(x)).equals(session);
            }
         }
         return false;
      }
      return (sessionPane.indexOfComponent(session) >= 0);

   }

}
