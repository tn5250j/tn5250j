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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.DefaultDesktopManager;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionListener;
import org.tn5250j.interfaces.GUIViewInterface;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

public class Gui5250MDIFrame extends GUIViewInterface implements
                                                    ChangeListener,
                                                    SessionListener,
                                                    SessionJumpListener {

   private static final long serialVersionUID = 1L;
BorderLayout borderLayout1 = new BorderLayout();
//   My5250 me;
//   private SessionManager manager;
   private ImageIcon focused = null;
   private ImageIcon unfocused = null;
   private int selectedIndex = 0;
   private JDesktopPane desktop;
   static int openFrameCount = 0;
   private Vector myFrameList;
   private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

   //Construct the frame
   public Gui5250MDIFrame(My5250 m) {
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

      desktop = new JDesktopPane();
      // Install our custom desktop manager
      desktop.setDesktopManager(new MyDesktopMgr());
      setContentPane(desktop);
      myFrameList = new Vector(3);

      if (sequence > 0)
         setTitle("tn5250j <" + sequence + ">- " + TN5250jConstants.tn5250jRelease + TN5250jConstants.tn5250jVersion + TN5250jConstants.tn5250jSubVer);
      else
         setTitle("tn5250j - " + TN5250jConstants.tn5250jRelease + TN5250jConstants.tn5250jVersion + TN5250jConstants.tn5250jSubVer);

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

         case TN5250jConstants.JUMP_PREVIOUS:
            prevSession();
            break;
         case TN5250jConstants.JUMP_NEXT:
            nextSession();
            break;
      }
   }

   private MyInternalFrame getNextInternalFrame() {


      JInternalFrame[] frames = desktop.getAllFrames();
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
		 log.debug(" current index " + x + " count " + frames.length + " has focus " +
                        mif.isActive() + " title " + mif.getTitle() + " seq " + seq +
                       " id " + mif.getInternalId());

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

            if (mif.isIcon()) {
               mif.setIcon(false);
            }
            mif.setSelected(true);

         }
         catch (java.beans.PropertyVetoException e) {
            log.warn(e.getMessage());
         }
      }
//      System.out.println(" current index " + index + " count " + desktop.getComponentCount());

   }

   private void prevSession() {

      JInternalFrame[] frames = desktop.getAllFrames();
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
		 log.debug(" current index " + x + " count " + frames.length + " has focus " +
                        mif.isActive() + " title " + mif.getTitle() + " seq " + seq +
                        " id " + mif.getInternalId());

         if (mix.equals(mif)) {
            index = x - 1;
            break;
         }
      }

      if (index < 0) {
         index = myFrameList.size() - 1;
      }

      try {
         MyInternalFrame mif = (MyInternalFrame)myFrameList.get(index);
         if (mif.isIcon()) {
            mif.setIcon(false);
         }
         mif.setSelected(true);

      }
      catch (java.beans.PropertyVetoException e) {
         log.warn(e.getMessage());
      }
//      System.out.println(" current index " + index + " count " + desktop.getComponentCount());

   }

   public void setIcons(ImageIcon focused, ImageIcon unfocused) {

   }

   public void stateChanged(ChangeEvent e) {


   }

   public void addSessionView(String tabText,SessionGUI session) {

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
         log.warn("Can not set maximum " + pve.getMessage());
      }

   }

   public void removeSessionView(SessionGUI targetSession) {

      int index = getIndexOfSession(targetSession);
      MyInternalFrame nextMIF = getNextInternalFrame();
      log.info("session found and closing down " + index);
      targetSession.removeSessionListener(this);
      targetSession.removeSessionJumpListener(this);
      JInternalFrame[] frames = desktop.getAllFrames();
      MyInternalFrame mif = (MyInternalFrame)frames[index];
      int count = getSessionViewCount();
	  log.debug(" num of frames before removal " + myFrameList.size());
      myFrameList.remove(mif);
	  log.debug(" num of frames left " + myFrameList.size());
      desktop.remove(index);

      if (nextMIF != null) {
         try {

            nextMIF.setSelected(true);


         }
         catch (java.beans.PropertyVetoException e) {
            log.warn(e.getMessage());
         }
      }

      this.repaint();

   }

   public int getSessionViewCount() {

      return desktop.getAllFrames().length;
   }

   public SessionGUI getSessionAt( int index) {

      JInternalFrame[] frames = desktop.getAllFrames();
      SessionGUI s = (SessionGUI)frames[index].getContentPane();

      return s;
   }

   public void onSessionChanged(SessionChangeEvent changeEvent) {

      Session5250 ses5250 = (Session5250)changeEvent.getSource();
      SessionGUI ses = ses5250.getGUI();

      switch (changeEvent.getState()) {
         case TN5250jConstants.STATE_CONNECTED:

            final String d = ses.getAllocDeviceName();
            if (d != null) {
               log.info(changeEvent.getState() + " " + d);
               final int index = getIndexOfSession(ses);

			   log.debug(" index of session " + index + " num frames " + desktop.getAllFrames().length);
               if (index == -1)
                  return;
               Runnable tc = new Runnable () {
                  public void run() {
                     JInternalFrame[] frames = desktop.getAllFrames();
                     int id = ((MyInternalFrame)frames[index]).getInternalId();
                     frames[index].setTitle("#" + id + " " + d);
                  }
               };
               SwingUtilities.invokeLater(tc);

            }
            break;
      }

   }

   public boolean containsSession(SessionGUI session) {

      return getIndexOfSession(session) >= 0;

   }

   public int getIndexOfSession(SessionGUI session) {

      JInternalFrame[] frames = desktop.getAllFrames();
      int index = -1;

      for (int idx = 0; idx < frames.length; idx++) {
         SessionGUI ses = (SessionGUI)frames[idx].getContentPane();
         if (ses.equals(session)) {
            index = idx;
            return index;
         }
      }

      return index;

   }

/* *** NEVER USED ********************************************************** */
//   private void calculateVisibility() {
//      JInternalFrame[] frames = desktop.getAllFrames();
//      for (int i = 0; i < frames.length; i++) {
//          JInternalFrame frame = frames[i];
//          if (!frame.isIcon()) {
//              Component[] c = frame.getContentPane().getComponents();
//              for (int j = 0; j < c.length; j++) {
//                  Component component = c[j];
//                  if (desktop.getBounds().intersects(calculateBoundsInFrame(component))) {
//                      component.setVisible(true);
//                  }
//                  else {
//                      //off desktop
//                      component.setVisible(false);
//                  }
//              }
//          }
//      }
//
//      for (int i = 0; i < frames.length; i++) {
//          JInternalFrame frame1 = frames[i];
//          if (!frame1.isIcon()) {
//              for (int j = 0; j < frames.length; j++) {
//                  JInternalFrame frame2 = frames[j];
//                  if (!frame2.isIcon()) {
//                      //Is frame 1 in front of frame 2?
//                      if (desktop.getIndexOf(frame1) < desktop.getIndexOf(frame2)) {
//                          Component[] c = frame2.getContentPane().getComponents();
//                          for (int k = 0; k < c.length; k++) {
//                              Component component = c[k];
//                              if (frame1.getBounds().contains(calculateBoundsInFrame(component))) {
//                                  component.setVisible(false);
//                                  }
//                              }
//                          }
//                      }
//                  }
//              }
//          }
//      }

/* *** NEVER USED ********************************************************** */
//   private Rectangle calculateBoundsInFrame(Component component) {
//     Rectangle componentBoundsInFrame = component.getBounds();
//     //This only works in the simplest case, need to recurse to JInternalFrame
//     Point p1 = component.getParent().getLocation();
//     Point p2 = component.getParent().getParent().getLocation();
//     Point p3 = component.getParent().getParent().getParent().getLocation();
//     Point p4 = component.getParent().getParent().getParent().getParent().getLocation();
//     componentBoundsInFrame = new Rectangle(p1.x + p2.x + p3.x + p4.x, p1.y + p2.y + p3.y + p4.y, componentBoundsInFrame.width, componentBoundsInFrame.height);
//     componentBoundsInFrame = componentBoundsInFrame.intersection(desktop.getBounds());
//     return componentBoundsInFrame;
//   }

   public class MyInternalFrame extends JInternalFrame {

      private static final long serialVersionUID = 1L;
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
//               calculateVisibility();
               disconnectMe();
            }

            public void internalFrameClosed(InternalFrameEvent e) {
//               displayMessage("Internal frame closed", e);
               disconnectMe();
//               calculateVisibility();
            }

            public void internalFrameOpened(InternalFrameEvent e) {
//               displayMessage("Internal frame opened", e);
//               calculateVisibility();
            }

            public void internalFrameIconified(InternalFrameEvent e) {
//               displayMessage("Internal frame iconified", e);
//               calculateVisibility();
//               e.getInternalFrame().getContentPane().setVisible(false);
            }

            public void internalFrameDeiconified(InternalFrameEvent e) {
//               displayMessage("Internal frame deiconified", e);
//               calculateVisibility();
//               e.getInternalFrame().getContentPane().setVisible(true);
            }

            public void internalFrameActivated(InternalFrameEvent e) {
//               displayMessage("Internal frame activated", e);
               activated = true;
//               calculateVisibility();
            }

            public void internalFrameDeactivated(InternalFrameEvent e) {
               activated = false;
//               calculateVisibility();
//               displayMessage("Internal frame deactivated", e);
            }


            });

          }
         void displayMessage(String prefix, InternalFrameEvent e) {
            String s = prefix + ": " + e.getSource();
            System.out.println(s + '\n');
         }

//         void hideMe() {
//            this.setVisible(false);
//
//         }

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

            SessionGUI s = (SessionGUI)getContentPane();
            me.closeSession(s);
         }

         public void resizeMe() {

            if (getContentPane() instanceof SessionGUI) {
               SessionGUI s = (SessionGUI)getContentPane();
               s.resizeMe();
            }
         }

         public void setBounds(int newX, int newY, int newWidth, int newHeight) {

            boolean didResize = (getWidth() != newWidth || getHeight() != newHeight);
            super.setBounds(newX, newY, newWidth, newHeight);
            if (didResize)
               resizeMe();

         }

   }

   // A DesktopManager that keeps its frames inside the desktop.
     public class MyDesktopMgr extends DefaultDesktopManager {

       private static final long serialVersionUID = 1L;
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
           JInternalFrame frame = (JInternalFrame)f;
//           ((Gui5250)frame.getContentPane()).getScreen().controllersG2D = null;
            f.validate();
       }

      // workaround for bug 4326562
      public void deiconifyFrame(JInternalFrame f) {
          super.deiconifyFrame(f);
          f.toFront();
      }

       // This is called any time a frame is moved or resized.  This
       // implementation keeps the frame from leaving the desktop.
       public void setBoundsForFrame(JComponent f, int x, int y, int w, int h) {

         log.info(" we are adjusting ");
         if (f instanceof MyInternalFrame == false) {
           super.setBoundsForFrame(f, x, y, w, h); // only deal w/internal frames
         }
         else {
           MyInternalFrame frame = (MyInternalFrame)f;

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
//           ((MyInternalFrame)f).resizeMe();
         }

       }

     }
}
