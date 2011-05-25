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
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.WindowEvent;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionListener;
import org.tn5250j.event.TabClosedListener;
import org.tn5250j.gui.TN5250jTabbedPane;
import org.tn5250j.gui.ConfirmTabCloseDialog;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.interfaces.GUIViewInterface;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

/**
 * This is the main {@link javax.swing.JFrame}, which contains multiple tabs.
 * 
 * @see GUIViewInterface
 */
public class Gui5250Frame extends GUIViewInterface implements
                                                    ChangeListener,
                                                    SessionListener,
                                                    TabClosedListener,
                                                    SessionJumpListener {

	private static final long serialVersionUID = 1L;

	private TN5250jTabbedPane sessionPane = new TN5250jTabbedPane();
	private int selectedIndex = 0;
	private boolean embedded = false;
	private boolean hideTabBar = false;
	
	public static volatile int count = 0;
	
	private transient final TN5250jLogger log = TN5250jLogFactory.getLogger (this.getClass());

   //Construct the frame
   public Gui5250Frame(My5250 m) {
      super(m);
      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      try  {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
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
      sessionPane.addtabCloseListener(this);

      Properties props = ConfigureFactory.getInstance().
                           getProperties(ConfigureFactory.SESSIONS);

      if (props.getProperty("emul.hideTabBar","no").equals("yes"))
         hideTabBar = true;

      if (!hideTabBar) {
         this.getContentPane().add(sessionPane, BorderLayout.CENTER);
      }

      if (count == 0) 
         setSessionTitle();

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
      sessionPane.setForegroundAt(index,Color.black);
      sessionPane.setIconAt(index,unfocused);


      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            int index1 = index;
            if (index1 < sessionPane.getTabCount() - 1) {
               sessionPane.setSelectedIndex(++index1);
               sessionPane.setForegroundAt(index1,Color.blue);
               sessionPane.setIconAt(index1,focused);

            }
            else {
               sessionPane.setSelectedIndex(0);
               sessionPane.setForegroundAt(0,Color.blue);
               sessionPane.setIconAt(0,focused);

            }

            ((SessionGUI)sessionPane.getComponent(sessionPane.getSelectedIndex())).grabFocus();

            setSessionTitle();
       }
      });

   }

   private void prevSession() {

      final int index = sessionPane.getSelectedIndex();
      sessionPane.setForegroundAt(index,Color.black);
      sessionPane.setIconAt(index,unfocused);

      SwingUtilities.invokeLater(new Runnable() {
         public void run() {
            int index1 = index;
            if (index1 == 0) {
               sessionPane.setSelectedIndex(sessionPane.getTabCount() - 1);
               sessionPane.setForegroundAt(sessionPane.getSelectedIndex(),Color.blue);
               sessionPane.setIconAt(sessionPane.getSelectedIndex(),focused);

            }
            else {
               sessionPane.setSelectedIndex(--index1);
               sessionPane.setForegroundAt(index1,Color.blue);
               sessionPane.setIconAt(index1,focused);

            }

            ((SessionGUI)sessionPane.getComponent(sessionPane.getSelectedIndex())).grabFocus();
            setSessionTitle();
       }
      });
   }

   public void stateChanged(ChangeEvent e) {

      TN5250jTabbedPane p = (TN5250jTabbedPane)e.getSource();
      if (selectedIndex < p.getTabCount()) {
    	  p.setForegroundAt(selectedIndex,Color.black);
    	  p.setIconAt(selectedIndex,unfocused);

    	  SessionGUI sg = (SessionGUI)p.getComponentAt(selectedIndex);
    	  sg.setVisible(false);

    	  sg = (SessionGUI)p.getSelectedComponent();

    	  if (sg == null)
    		  return;

    	  sg.setVisible(true);
    	  sg.grabFocus();

    	  selectedIndex = p.getSelectedIndex();
    	  p.setForegroundAt(selectedIndex,Color.blue);
    	  p.setIconAt(selectedIndex,focused);

    	  setSessionTitle();
      }

   }

   private void setSessionTitle() {

      SessionGUI ses = getSessionAt(selectedIndex);

	  if (ses != null && ses.isConnected()) {
		 final String name = determineTabName(ses);
         if (sequence - 1 > 0)
            setTitle(name + " - tn5250j <" + sequence + "> - " + TN5250jConstants.tn5250jRelease + TN5250jConstants.tn5250jVersion + TN5250jConstants.tn5250jSubVer);
         else
            setTitle(name + " - tn5250j - " + TN5250jConstants.tn5250jRelease + TN5250jConstants.tn5250jVersion + TN5250jConstants.tn5250jSubVer);
      }
      else {

         if (sequence - 1 > 0)
            setTitle("tn5250j <" + sequence + "> - " + TN5250jConstants.tn5250jRelease + TN5250jConstants.tn5250jVersion + TN5250jConstants.tn5250jSubVer);
         else
            setTitle("tn5250j - " + TN5250jConstants.tn5250jRelease + TN5250jConstants.tn5250jVersion + TN5250jConstants.tn5250jSubVer);
      }

		count +=1;

   }

   /**
    * Determines the name, which is configured for one tab ({@link SessionGUI})
    * 
    * @param sessiongui
    * @return
    * @NotNull
    */
   private String determineTabName(final SessionGUI sessiongui) {
   	assert sessiongui != null;
   	final String name;
   	if (sessiongui.getSession().isUseSystemName()) {
   		name = sessiongui.getSessionName();
   	} else {
   		if (sessiongui.getAllocDeviceName() != null) {
   			name = sessiongui.getAllocDeviceName();
   		} else {
   			name = sessiongui.getHostName();
   		}
   	}
   	return name;
   }

   public void addSessionView(String tabText,SessionGUI sessionView) {

      final SessionGUI session = sessionView;

      if (hideTabBar && sessionPane.getTabCount() == 0 && !embedded) {

         this.getContentPane().add(session, BorderLayout.CENTER);
         session.addSessionListener(this);

         session.resizeMe();
         repaint();
         if (packFrame)
            pack();
         else
            validate();
         embedded = true;
         session.grabFocus();
         setSessionTitle();
      }
      else {

         if (hideTabBar && sessionPane.getTabCount() == 0 ) {
            SessionGUI ses = null;
            for (int x=0; x < this.getContentPane().getComponentCount(); x++) {

               if (this.getContentPane().getComponent(x) instanceof SessionGUI) {
                  ses = (SessionGUI)(this.getContentPane().getComponent(x));
                  this.getContentPane().remove(x);
                  break;
               }
            }

            sessionPane.addTab(tabText,focused,ses);
            final SessionGUI finalSession = ses;

            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  finalSession.resizeMe();
                  finalSession.repaint();
               }
            });


            sessionPane.setTitleAt(0,determineTabName(ses));

            ses.addSessionListener(this);
            ses.addSessionJumpListener(this);

            this.getContentPane().add(sessionPane, BorderLayout.CENTER);
            SwingUtilities.invokeLater(new Runnable() {
               public void run() {
                  repaint();
                  finalSession.grabFocus();
               }
            });
         }

         sessionPane.addTab(tabText,focused,session);

         sessionPane.setForegroundAt(sessionPane.getSelectedIndex(),Color.black);
         sessionPane.setIconAt(sessionPane.getSelectedIndex(),unfocused);

         sessionPane.setSelectedIndex(sessionPane.getTabCount()-1);
         sessionPane.setForegroundAt(sessionPane.getSelectedIndex(),Color.blue);
         sessionPane.setIconAt(sessionPane.getSelectedIndex(),focused);

         session.addSessionListener(this);
         session.addSessionJumpListener(this);

         SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               session.resizeMe();
               session.repaint();
               session.grabFocus();
            }
         });
      }
   }

	/* (non-Javadoc)
	 * @see org.tn5250j.event.TabClosedListener#onTabClosed(int)
	 */
	public void onTabClosed(int tabToBeClosed) {
		final SessionGUI sessionAt = this.getSessionAt(tabToBeClosed);

		final ConfirmTabCloseDialog tabclsdlg = new ConfirmTabCloseDialog(sessionAt);

		SessionConfig sesConfig = sessionAt.session.getConfiguration();

		boolean close = true;

		if (sesConfig.isPropertyExists("confirmTabClose")) {
			if(sesConfig.getStringProperty("confirmTabClose").equals("Yes")) {
				if(!tabclsdlg.show()) {
					close = false;
				}
			}
		}

		if (close) {
			me.closeSession(sessionAt);
		}

	}


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
         
         // get the tab count to be used later
         int tabs = sessionPane.getTabCount();

         // if the index removed is the same as the number of tabs
         //   we need to decrement the index offset or we will get
         //   an error because we went over the tab limit which starts
         //   at zero offset.
         if (tabs == index)
            index--;

         if (tabs > 0 && index < tabs) {
            sessionPane.setSelectedIndex(index);
            sessionPane.setForegroundAt(index,Color.blue);
            sessionPane.setIconAt(index,focused);
            ((SessionGUI)sessionPane.getComponentAt(index)).requestFocus();
         }
         else {

            if (tabs > 0) {
               sessionPane.setSelectedIndex(0);
               sessionPane.setForegroundAt(0,Color.blue);
               sessionPane.setIconAt(0,focused);
               ((SessionGUI)sessionPane.getComponentAt(0)).requestFocus();
            }

         }
      }
   }

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

   public void onSessionChanged(SessionChangeEvent changeEvent) {

      Session5250 ses5250 = (Session5250)changeEvent.getSource();
      final SessionGUI ses = ses5250.getGUI();

      switch (changeEvent.getState()) {
         case TN5250jConstants.STATE_CONNECTED:

            final String d = ses.getAllocDeviceName();
            if (d != null) {
            	if (this.log.isDebugEnabled()) {
            		this.log.debug(changeEvent.getState() + " " + d);
            	}
               final int index = sessionPane.indexOfComponent(ses);
               if (index >= 0) {
                  Runnable tc = new Runnable () {
                     public void run() {
                        sessionPane.setTitleAt(index,determineTabName(ses));
                     }
                  };
                  SwingUtilities.invokeLater(tc);
               }
               setSessionTitle();

            }
            break;
      }
   }

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
