package org.tn5250j;
/**
 * Title: Gui5250MDIFrame.java
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.5
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
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameListener;
import javax.swing.event.InternalFrameEvent;

public class Gui5250MDIFrame extends Gui5250Frame implements GUIViewInterface,
                                                    ChangeListener,
                                                    TN5250jConstants,
                                                    SessionListener,
                                                    SessionJumpListener {

   BorderLayout borderLayout1 = new BorderLayout();
   My5250 me;
//   private SessionManager manager;
   private ImageIcon focused = null;
   private ImageIcon unfocused = null;
   private int selectedIndex = 0;
   private boolean packFrame = false;
   private int sequence;
   private JDesktopPane desktop;
   static int openFrameCount = 0;
   private Vector myFrameList;

   //Construct the frame
   public Gui5250MDIFrame(My5250 m) {
      super(m);
      me = m;
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

      desktop = new JDesktopPane();
      // Install our custom desktop manager
      desktop.setDesktopManager(new MyDesktopMgr());
      setContentPane(desktop);
      myFrameList = new Vector(3);

      if (sequence > 0)
         setTitle("tn5250j <" + sequence + ">- " + tn5250jRelease + tn5250jVersion + tn5250jSubVer);
      else
         setTitle("tn5250j - " + tn5250jRelease + tn5250jVersion + tn5250jSubVer);

      if (packFrame)
         pack();
      else
         validate();

   }

   public void centerFrame() {

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

   private MyInternalFrame getNextInternalFrame() {


      JInternalFrame[] frames = (JInternalFrame[])desktop.getAllFrames();
      JInternalFrame miv = desktop.getSelectedFrame();

      if (miv == null)
         return null;

      int index = desktop.getIndexOf(miv);

      if (index == -1)
         return null;

      MyInternalFrame mix = (MyInternalFrame)frames[index];

      int seq = mix.getInternalId();
      index  = 0;

      for (int x = 0; x < myFrameList.size(); x++) {

         MyInternalFrame mif = (MyInternalFrame)myFrameList.get(x);
//         System.out.println(" current index " + x + " count " + frames.length + " has focus " +
//                        mif.isActive() + " title " + mif.getTitle() + " seq " + seq +
//                        " id " + mif.getInternalId());

         if (mix.equals(mif)) {
            index = x + 1;
            break;
         }
      }

      if (index > myFrameList.size() - 1) {
         index = 0;
      }

      return (MyInternalFrame)myFrameList.get(index);



   }

   private void nextSession() {


      MyInternalFrame mif = getNextInternalFrame();

      if (mif != null) {
         try {

            mif.setSelected(true);


         }
         catch (java.beans.PropertyVetoException e) {
            System.out.println(e.getMessage());
         }
      }
//      System.out.println(" current index " + index + " count " + desktop.getComponentCount());

   }

   private void prevSession() {

      JInternalFrame[] frames = (JInternalFrame[])desktop.getAllFrames();
      JInternalFrame miv = desktop.getSelectedFrame();

      if (miv == null)
         return;

      int index = desktop.getIndexOf(miv);

      if (index == -1)
         return;

      MyInternalFrame mix = (MyInternalFrame)frames[index];

      int seq = mix.getInternalId();
      index  = 0;

      for (int x = 0; x < myFrameList.size(); x++) {

         MyInternalFrame mif = (MyInternalFrame)myFrameList.get(x);
//         System.out.println(" current index " + x + " count " + frames.length + " has focus " +
//                        mif.isActive() + " title " + mif.getTitle() + " seq " + seq +
//                        " id " + mif.getInternalId());

         if (mix.equals(mif)) {
            index = x - 1;
            break;
         }
      }

      if (index < 0) {
         index = myFrameList.size() - 1;
      }

      try {
         ((MyInternalFrame)myFrameList.get(index)).setSelected(true);
      }
      catch (java.beans.PropertyVetoException e) {
         System.out.println(e.getMessage());
      }
//      System.out.println(" current index " + index + " count " + desktop.getComponentCount());

   }

   public void setIcons(ImageIcon focused, ImageIcon unfocused) {

   }

   public void stateChanged(ChangeEvent e) {


   }

   public void addSessionView(String tabText,Session session) {

      MyInternalFrame frame = new MyInternalFrame();
      frame.setVisible(true);
      desktop.add(frame);
      myFrameList.add(frame);
      selectedIndex = desktop.getComponentCount();
      frame.setContentPane(session);
      try {
         frame.setSelected(true);
      } catch (java.beans.PropertyVetoException e) {}
      session.addSessionListener(this);
      session.addSessionJumpListener(this);
      try {
         frame.setMaximum(true);
      }
      catch (java.beans.PropertyVetoException pve) {
         System.out.println("Can not set maximum " + pve.getMessage());
      }

   }

   public void removeSessionView(Session targetSession) {

      int index = getIndexOfSession(targetSession);
      MyInternalFrame nextMIF = getNextInternalFrame();
      System.out.println("session found and closing down " + index);
      targetSession.removeSessionListener(this);
      targetSession.removeSessionJumpListener(this);
      JInternalFrame[] frames = (JInternalFrame[])desktop.getAllFrames();
      MyInternalFrame mif = (MyInternalFrame)frames[index];
      int count = getSessionViewCount();
//      System.out.println(" num of frames before removal " + myFrameList.size());
      myFrameList.remove(mif);
//      System.out.println(" num of frames left " + myFrameList.size());
      desktop.remove(index);

      if (nextMIF != null) {
         try {

            nextMIF.setSelected(true);


         }
         catch (java.beans.PropertyVetoException e) {
            System.out.println(e.getMessage());
         }
      }

      this.repaint();

   }

   public int getSessionViewCount() {

      return desktop.getAllFrames().length;
   }

   public Session getSessionAt( int index) {

      JInternalFrame[] frames = (JInternalFrame[])desktop.getAllFrames();
      Session s = (Session)frames[index].getContentPane();

      return s;
   }

   public void onSessionChanged(SessionChangeEvent changeEvent) {

      Session ses = (Session)changeEvent.getSource();

      switch (changeEvent.getState()) {
         case STATE_CONNECTED:

            final String d = ses.getAllocDeviceName();
            if (d != null) {
               System.out.println(changeEvent.getState() + " " + d);
               final int index = getIndexOfSession(ses);

               System.out.println(" index of session " + index + " num frames " + desktop.getAllFrames().length);
               if (index == -1)
                  return;
               Runnable tc = new Runnable () {
                  public void run() {
                     JInternalFrame[] frames = desktop.getAllFrames();
                     frames[index].setTitle(frames[index].getTitle() + " " + d);
                  }
               };
               SwingUtilities.invokeLater(tc);

            }
            break;
      }

   }

   public boolean containsSession(Session session) {

      return getIndexOfSession(session) >= 0;

   }

   public int getIndexOfSession(Session session) {

      JInternalFrame[] frames = (JInternalFrame[])desktop.getAllFrames();
      int index = -1;

      for (int idx = 0; idx < frames.length; idx++) {
         Session ses = (Session)frames[idx].getContentPane();
         if (ses.equals(session)) {
            index = idx;
            return index;
         }
      }

      return index;

   }

   public class MyInternalFrame extends JInternalFrame {

      static final int xOffset = 30, yOffset = 30;
      private int internalId = 0;
      private boolean activated;

      public MyInternalFrame() {
         super("#" + (++openFrameCount),
              true, //resizable
              true, //closable
              true, //maximizable
              true);//iconifiable

         internalId = openFrameCount;

         //...Create the GUI and put it in the window...

         //...Then set the window size or call pack...
         setSize(600,500);

         //Set the window's location.
         setLocation(xOffset*openFrameCount, yOffset*openFrameCount);

         addInternalFrameListener(new InternalFrameAdapter() {

            public void internalFrameClosing(InternalFrameEvent e) {
//               displayMessage("Internal frame closing", e);
               disconnectMe();
            }

            public void internalFrameClosed(InternalFrameEvent e) {
//               displayMessage("Internal frame closed", e);
               disconnectMe();
            }

            public void internalFrameOpened(InternalFrameEvent e) {
//               displayMessage("Internal frame opened", e);
            }

            public void internalFrameIconified(InternalFrameEvent e) {
//               displayMessage("Internal frame iconified", e);
            }

            public void internalFrameDeiconified(InternalFrameEvent e) {
//               displayMessage("Internal frame deiconified", e);
            }

            public void internalFrameActivated(InternalFrameEvent e) {
//               displayMessage("Internal frame activated", e);
               activated = true;
               repaint();
            }

            public void internalFrameDeactivated(InternalFrameEvent e) {
               activated = false;

//               displayMessage("Internal frame deactivated", e);
            }


            });

          }
         void displayMessage(String prefix, InternalFrameEvent e) {
            String s = prefix + ": " + e.getSource();
            System.out.println(s + '\n');
         }

         void hideMe() {
            this.setVisible(false);
         }

         public int getInternalId() {

            return internalId;

         }

         public boolean isActive() {

            return activated;

         }

         public void setSelected(boolean selected)
               throws java.beans.PropertyVetoException {
            super.setSelected(selected);
         }

         public void paintme() {
            repaint();
         }
         public void update(Graphics g) {
            paint(g);
         }

         private void disconnectMe() {

            Session s = (Session)getContentPane();
            me.closeSession(s);
//            this.setVisible(false);
//            disconnectMe(s);
         }
   }

   // A DesktopManager that keeps its frames inside the desktop.
     public class MyDesktopMgr extends DefaultDesktopManager {

       // We'll tag internal frames that are being resized using a client
       // property with the name RESIZING.  Used in setBoundsForFrame().
       protected static final String RESIZING = "RESIZING";

       public void beginResizingFrame(JComponent f, int dir) {
         f.putClientProperty(RESIZING, Boolean.TRUE);
       }

       public void endResizingFrame(JComponent f) {
         f.putClientProperty(RESIZING, Boolean.FALSE);
       }

       public void endDraggingFrame(JComponent f) {
//         super.endDraggingFrame(f);
           JInternalFrame frame = (JInternalFrame)f;
//           JDesktopPane desk = frame.getDesktopPane();
//           desk.repaint();
//           frame.validate();
//           frame.repaint();
           ((Gui5250)frame.getContentPane()).getScreen().gg2d = null;

       }

       // This is called any time a frame is moved or resized.  This
       // implementation keeps the frame from leaving the desktop.
       public void setBoundsForFrame(JComponent f, int x, int y, int w, int h) {

         if (f instanceof JInternalFrame == false) {
           super.setBoundsForFrame(f, x, y, w, h); // only deal w/internal frames
         }
         else {
           JInternalFrame frame = (JInternalFrame)f;

           // Figure out if we are being resized (otherwise it's just a move)
           boolean resizing = false;
           Object r = frame.getClientProperty(RESIZING);
           if (r != null && r instanceof Boolean) {
             resizing = ((Boolean)r).booleanValue();
           }

           JDesktopPane desk = frame.getDesktopPane();
           Dimension d = desk.getSize();

           // Nothing all that fancy below, just figuring out how to adjust
           // to keep the frame on the desktop.
           if (x < 0) {              // too far left?
             if (resizing)
               w += x;               // don't get wider!
             x=0;                    // flush against the left side
           }
           else {
             if (x+w>d.width) {      // too far right?
              if (resizing)
                w = d.width-x;       // don't get wider!
              else
                x = d.width-w;       // flush against the right side
             }
           }
           if (y < 0) {              // too high?
             if (resizing)
               h += y;               // don't get taller!
             y=0;                    // flush against the top
           }
           else {
             if (y+h > d.height) {   // too low?
               if (resizing)
                 h = d.height - y;   // don't get taller!
               else
                 y = d.height-h;     // flush against the bottom
             }
           }

           // Set 'em the way we like 'em
           super.setBoundsForFrame(f, x, y, w, h);
         }
       }
//         public JInternalFrame getSelectedFrame() {
//           int i, count;
//           JInternalFrame[] results;
//   //        Vector vResults = new Vector(10);
//           Object next, tmp;
//            System.out.println(" get selected one ");
//           count = getComponentCount();
//           for(i = 0; i < count; i++) {
//               next = getComponent(i);
//               if(next instanceof JInternalFrame) {
//                  JInternalFrame n = (JInternalFrame)next;
//                   if (n.isSelected())
//                     return n;
//               }
//               else if(next instanceof JInternalFrame.JDesktopIcon)  {
//                   JInternalFrame n = ((JInternalFrame.JDesktopIcon)next).getInternalFrame();
//                   if (n != null & n.isSelected())
//                     return n;
//   //                if(tmp != null)
//   //                    vResults.addElement(tmp);
//               }
//           }
//
//           return null;
//
//         }
     }


}
