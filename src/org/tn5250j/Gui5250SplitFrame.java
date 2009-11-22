package org.tn5250j;
/**
 * Title: Gui5250SplitFrame
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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionListener;
import org.tn5250j.framework.common.SessionManager;
import org.tn5250j.interfaces.GUIViewInterface;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public class Gui5250SplitFrame extends GUIViewInterface implements
                                                    SessionListener,
                                                    SessionJumpListener {

   private static final long serialVersionUID = 1L;
BorderLayout borderLayout1 = new BorderLayout();
   JSplitPane sessionPane;
   private SessionManager manager;
   private int selectedIndex = 0;

   private Vector sessionList;
   private JList sessionPicker;
   private DefaultListModel lm = new DefaultListModel();
   private JPanel sessionPanel;
   private JScrollPane scroller;
   private JPanel toolsPanel;

   private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

   //Construct the frame
   public Gui5250SplitFrame(My5250 m) {

      super(m);

      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      try  {
         jbInit();
      }
      catch(Exception e) {
         log.warn("In constructor: "+e);
      }
   }

   //Component initialization
   private void jbInit() throws Exception  {

      this.getContentPane().setLayout(borderLayout1);

      if (sequence > 0)
         setTitle("tn5250j <" + sequence + "> - " + TN5250jConstants.tn5250jRelease + TN5250jConstants.tn5250jVersion + TN5250jConstants.tn5250jSubVer);
      else
         setTitle("tn5250j - " + TN5250jConstants.tn5250jRelease + TN5250jConstants.tn5250jVersion + TN5250jConstants.tn5250jSubVer);

      // update the frame sequences
      frameSeq = sequence++;

      sessionList = new Vector(3);

//    note to myself if needed on how to add a listener to the the divider of
//    of a scroll pane.  This is really a pain in the ass to figure out.
//
//      You can use a mouse listener but you cannot add a mouse listener
//         directly to a split pane. You will have to get the splitpanedivider
//          and then add the mouselistener to that.
//
//      (((BasicSplitPaneUI)yoursplitpane.getUI()).getDivider()).addMouseListener(yourmouselistener);      // create the split pane
      sessionPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
      sessionPane.setOneTouchExpandable(true);
      sessionPane.setContinuousLayout(true);

      sessionPane.setBorder(BorderFactory.createEtchedBorder());
      sessionPane.setOpaque(true);
      sessionPane.setRequestFocusEnabled(false);
      sessionPane.setDoubleBuffered(false);

      this.getContentPane().add(sessionPane, BorderLayout.CENTER);
      sessionPanel = new JPanel();
      sessionPanel.setLayout(new BorderLayout());

      sessionPane.setLeftComponent(sessionPanel);


      toolsPanel = new JPanel();
      toolsPanel.setLayout(new BorderLayout());

      sessionPane.setRightComponent(toolsPanel);

      sessionPicker = new JList(lm);
      sessionPicker.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      SessionRenderer renderer = new SessionRenderer();
      sessionPicker.setCellRenderer(renderer);

      // add list selection listener to our functions list so that we
      //   can display the mapped key(s) to the function when a new
      //   function is selected.
      sessionPicker.addListSelectionListener(new ListSelectionListener() {
         public void valueChanged(ListSelectionEvent lse) {
            if (!lse.getValueIsAdjusting()) {
               showSelectedSession(sessionPicker.getSelectedIndex());
            }
         }
      });

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


   void showSelectedSession(int whichOne) {

      if (whichOne < 0 || whichOne > sessionList.size() - 1)
         return;

      SessionGUI session = (SessionGUI)sessionList.get(whichOne);
      sessionPicker.setSelectedIndex(whichOne);

      SessionGUI current = getCurrentViewedSession();

      if (current != null)
         current.setVisible(false);

      // now remove all components from the panel
      sessionPanel.removeAll();
      // set the session to visible so that we get screen updates again
      session.setVisible(true);

      // add the session to the panel so that we can see it.
      sessionPanel.add(session,BorderLayout.CENTER);
      // make sure we update the screen
      sessionPanel.revalidate();
      sessionPanel.repaint();
      // make sure we have the focus after switching.
      session.grabFocus();
   }

   public void update(Graphics g) {
      paint(g);
      sessionPanel.repaint();
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

   /**
    * Helper method to return the currently viewed session
    *
    * @return
    */
   private SessionGUI getCurrentViewedSession() {

      SessionGUI current = null;
      Component[] comps = sessionPanel.getComponents();
      int count = comps.length;
      // make the current session non visible to keep from over writting the
      //  session screens.
      for (int x = 0; x < count; x++) {

         if (comps[x] instanceof SessionGUI) {
            current = (SessionGUI)comps[x];
         }
      }

      return current;

   }

   /**
    * Bring the next session to front
    */
   private void nextSession() {

      SessionGUI current = getCurrentViewedSession();

      int index = sessionList.indexOf(current) + 1;

      int size = sessionList.size() - 1;

      if (index > size)
         showSelectedSession(0);
      else
         showSelectedSession(index);
   }

   /**
    * Bring the previous session to front
    */
   private void prevSession() {

      SessionGUI current = getCurrentViewedSession();

      int index = sessionList.indexOf(current) - 1;

      int size = sessionList.size() - 1;

      if (index < 0)
         showSelectedSession(size);
      else
         showSelectedSession(index);

   }

   /**
    * Add a session to the view
    *
    * @param sessionName
    * @param session
    */
   public void addSessionView(String sessionName,SessionGUI session) {

      lm.addElement(session);

      if (sessionList.size() == 0) {
         sessionList.addElement(session);
         sessionPanel.add(session,BorderLayout.CENTER);
         scroller = new JScrollPane();
         scroller.setViewportView(sessionPicker);
         toolsPanel.add(scroller,BorderLayout.CENTER);
         sessionPane.setDividerLocation(0.90);
      }
      else {
         sessionList.addElement(session);
         sessionPicker.setSelectedIndex(sessionList.size()-1);
      }

      // add ourselves to the listner list
      session.addSessionListener(this);
      session.addSessionJumpListener(this);

      // now show it to the user
      showSelectedSession(sessionList.size()-1);


   }

   public void removeSessionView(SessionGUI targetSession) {


      int index = sessionList.indexOf(targetSession);

      lm.remove(index);

      if (index > 0 || index == sessionList.size() - 1)
         prevSession();
      if (index == 0)
         nextSession();

      sessionList.remove(targetSession);

      updateScrollPane();

   }

   /**
    * Return the count of views contained in the frame
    * @return
    */
   public int getSessionViewCount() {

      return sessionList.size();

   }

   /**
    * Return the Session object at the specific index.
    *
    * @param index
    * @return
    */
   public SessionGUI getSessionAt( int index) {

      return (SessionGUI)sessionList.get(index);
   }

   public void onSessionChanged(SessionChangeEvent changeEvent) {

      updateScrollPane();
   }

   /**
    * Update the scroll pane whenever something changes
    */
   private void updateScrollPane() {

      scroller.invalidate();
      scroller.repaint();
   }

   public boolean containsSession(SessionGUI session) {

      return sessionList.contains(session);

   }

   /**
    * List renderer for sessions
    */
   class SessionRenderer extends JLabel
                       implements ListCellRenderer {

      private static final long serialVersionUID = 1L;

	public SessionRenderer() {
         setOpaque(true);
         setHorizontalAlignment(LEFT);
         setVerticalAlignment(CENTER);
      }

      /*
       * This method finds the image and text corresponding
       * to the selected value and returns the label, set up
       * to display the text and image.
       */
      public Component getListCellRendererComponent(
                                       JList list,
                                       Object value,
                                       int index,
                                       boolean isSelected,
                                       boolean cellHasFocus) {

         //Get the selected index. (The index param isn't
         //always valid, so just use the value.)
         SessionGUI ses = (SessionGUI)value;

         // set the correct focused or unfocused and selected or unselected
         //  colors
         if (isSelected) {
            setIcon(focused);
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
         }
         else {
            setIcon(unfocused);
            setBackground(list.getBackground());
            setForeground(list.getForeground());
         }

         //Set the text of this label.
         if (ses.getAllocDeviceName() != null)
            setText(ses.getAllocDeviceName());
         else
            setText(ses.getSessionName());

         return this;
      }
   }

}
