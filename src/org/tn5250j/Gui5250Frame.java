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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.util.*;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.interfaces.GUIViewInterface;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

public class Gui5250Frame extends JFrame implements GUIViewInterface,
                                                    ChangeListener,
                                                    TN5250jConstants,
                                                    SessionListener,
                                                    SessionJumpListener {

   BorderLayout borderLayout1 = new BorderLayout();
   My5250 me;
   JTabbedPane sessionPane = new JTabbedPane();
   private SessionManager manager;
   private ImageIcon focused = null;
   private ImageIcon unfocused = null;
   private int selectedIndex = 0;
   private boolean packFrame = false;
   private int sequence;

   //Construct the frame
   public Gui5250Frame(My5250 m, int seq) {
      me = m;
      sequence = seq;
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

      this.getContentPane().setLayout(borderLayout1);

      String release = "0";
      String version = ".5";
      String subVer= ".3 RC1";

      if (sequence > 0)
         setTitle("tn5250j <" + sequence + ">- " + release + version + subVer);
      else
         setTitle("tn5250j - " + release + version + subVer);

      sessionPane.setBorder(BorderFactory.createEtchedBorder());
      sessionPane.setBounds(new Rectangle(78, 57, 5, 5));
      sessionPane.setOpaque(false);
      sessionPane.setRequestFocusEnabled(false);
      sessionPane.setDoubleBuffered(true);
      this.getContentPane().add(sessionPane, BorderLayout.CENTER);
      sessionPane.addChangeListener(this);
      centerFrame();
   }

   public void centerFrame() {

      if (packFrame)
         pack();
      else
         validate();

      //Center the window
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = getSize();
      if (frameSize.height > screenSize.height)
         frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
         frameSize.width = screenSize.width;

      setLocation((screenSize.width - frameSize.width) / 2,
                     (screenSize.height - frameSize.height) / 2);


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

         case JUMP_PREVIOUS:
            prevSession();
            break;
         case JUMP_NEXT:
            nextSession();
            break;
      }
   }

   private void nextSession() {

      int index = sessionPane.getSelectedIndex();
      sessionPane.setForegroundAt(index,Color.black);
      sessionPane.setIconAt(index,unfocused);

      if (index < sessionPane.getTabCount() - 1) {
         sessionPane.setSelectedIndex(++index);
         sessionPane.setForegroundAt(index,Color.blue);
         sessionPane.setIconAt(index,focused);

      }
      else {
         sessionPane.setSelectedIndex(0);
         sessionPane.setForegroundAt(0,Color.blue);
         sessionPane.setIconAt(0,focused);

      }

   }

   private void prevSession() {

      int index = sessionPane.getSelectedIndex();
      sessionPane.setForegroundAt(index,Color.black);
      sessionPane.setIconAt(index,unfocused);

      if (index == 0) {
         sessionPane.setSelectedIndex(sessionPane.getTabCount() - 1);
         sessionPane.setForegroundAt(sessionPane.getSelectedIndex(),Color.blue);
         sessionPane.setIconAt(sessionPane.getSelectedIndex(),focused);

      }
      else {
         sessionPane.setSelectedIndex(--index);
         sessionPane.setForegroundAt(index,Color.blue);
         sessionPane.setIconAt(index,focused);

      }
   }

   public void setIcons(ImageIcon focused, ImageIcon unfocused) {

      this.focused = focused;
      this.unfocused = unfocused;
   }

   public void stateChanged(ChangeEvent e) {

      JTabbedPane p = (JTabbedPane)e.getSource();
      p.setForegroundAt(selectedIndex,Color.black);
      p.setIconAt(selectedIndex,unfocused);

      Session sg = (Session)p.getComponentAt(selectedIndex);
      sg.setVisible(false);

      sg = (Session)p.getSelectedComponent();

      if (sg == null)
         return;

      sg.setVisible(true);

      sg.requestFocus();

      selectedIndex = p.getSelectedIndex();
      p.setForegroundAt(selectedIndex,Color.blue);
      p.setIconAt(selectedIndex,focused);

   }

   public void addSessionView(String tabText,Session session) {

      sessionPane.addTab(tabText,focused,session);

      sessionPane.setForegroundAt(sessionPane.getSelectedIndex(),Color.black);
      sessionPane.setIconAt(sessionPane.getSelectedIndex(),unfocused);

      sessionPane.setSelectedIndex(sessionPane.getTabCount()-1);
      sessionPane.setForegroundAt(sessionPane.getSelectedIndex(),Color.blue);
      sessionPane.setIconAt(sessionPane.getSelectedIndex(),focused);

      session.addSessionListener(this);
      session.addSessionJumpListener(this);

   }

   public void removeSessionView(Session targetSession) {

      int index = sessionPane.indexOfComponent(targetSession);
      System.out.println("session found and closing down " + index);
      targetSession.removeSessionListener(this);
      targetSession.removeSessionJumpListener(this);
      sessionPane.remove(index);

      if (index < (sessionPane.getTabCount() - 2)) {
         sessionPane.setSelectedIndex(index);
         sessionPane.setForegroundAt(index,Color.blue);
         sessionPane.setIconAt(index,focused);
      }
      else {

         if (sessionPane.getTabCount() > 0) {
            sessionPane.setSelectedIndex(0);
            sessionPane.setForegroundAt(0,Color.blue);
            sessionPane.setIconAt(0,focused);
         }

      }

   }

   public int getSessionViewCount() {

      return sessionPane.getTabCount();
   }

   public Session getSessionAt( int index) {

      return (Session)sessionPane.getComponentAt(index);
   }

   public void onSessionChanged(SessionChangeEvent changeEvent) {

      Session ses = (Session)changeEvent.getSource();

      switch (changeEvent.getState()) {
         case STATE_CONNECTED:

            final String d = ses.getAllocDeviceName();
            System.out.println(changeEvent.getState() + " " +
                        d);
            if (d != null) {
               final int index = sessionPane.indexOfComponent(ses);
               Runnable tc = new Runnable () {
                  public void run() {
                     sessionPane.setTitleAt(index,d);
                  }
               };
               SwingUtilities.invokeLater(tc);

            }
            break;
      }
   }

   public boolean containsSession(Session session) {

      return (sessionPane.indexOfComponent(session) >= 0);

   }
}
