/**
 * Title: tn5250J
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

package org.tn5250j;

import java.awt.*;
import java.awt.event.*;
//import java.awt.dnd.*;
//import java.awt.datatransfer.*;
import javax.swing.*;
import java.util.*;


import org.tn5250j.tools.logging.*;
import org.tn5250j.tools.*;
import org.tn5250j.mailtools.*;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.interfaces.SessionScrollerInterface;
import org.tn5250j.keyboard.KeyboardHandler;
import org.tn5250j.event.EmulatorActionListener;
import org.tn5250j.event.EmulatorActionEvent;
import org.tn5250j.framework.tn5250.*;

public class Gui5250 extends JPanel implements ComponentListener,
                                                      ActionListener,
                                                      TN5250jConstants,
                                                      RubberBandCanvasIF,
                                                      //SessionListener,
                                                      SessionConfigListener {
//                                                      DropTargetListener {

   BorderLayout borderLayout1 = new BorderLayout();
   Screen5250 screen;
   String propFileName;
   protected Session5250 session;
   GuiGraphicBuffer bi;
//   tnvt vt;
   TNRubberBand rubberband;
   JPanel s = new JPanel();
   KeyPad keyPad = new KeyPad();
   private JPopupMenu popup;
   Macronizer macros;
   String newMacName;
   private Vector listeners = null;
   private Vector actionListeners = null;
   private SessionJumpEvent jumpEvent;
   private boolean macroRunning;
   private boolean stopMacro;
   private boolean doubleClick;
   protected SessionConfig sesConfig;
   protected KeyboardHandler keyHandler;
   protected SessionScrollerInterface scroller;

   private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());
   private TN5250jLogger graphics = TN5250jLogFactory.getLogger("GFX");

   public Gui5250 () {

   }

   //Construct the frame
   public Gui5250(Session5250 session) {
   //, SessionConfig config) {

      this.session = session;
      propFileName = session.getConfigurationResource();

      sesConfig = session.getConfiguration();

      try  {
         jbInit();
      }
      catch(Exception e) {
         log.warn("Error in constructor: "+e.getMessage());
      }

   }

   //Component initialization
  private void jbInit() throws Exception  {
      this.setLayout(borderLayout1);

//      this.setOpaque(false);
//      setDoubleBuffered(true);
      s.setOpaque(false);
      s.setDoubleBuffered(false);

      session.setGUI((SessionGUI)this);

//      screen = new Screen5250(this,sesConfig);
      screen = session.getScreen();

      this.addComponentListener(this);

      if (bi == null) {
         checkOffScreenImage();
      }

      setRubberBand(new TNRubberBand(this));
      keyHandler = KeyboardHandler.getKeyboardHandlerInstance(session);

      if (!sesConfig.isPropertyExists("width") ||
         !sesConfig.isPropertyExists("height"))
         // set the initialize size
         this.setSize(bi.getPreferredSize());
      else {

         this.setSize(sesConfig.getIntegerProperty("width"),
                        sesConfig.getIntegerProperty("height"));
      }

      addMouseListener(new MouseAdapter() {
         public void mousePressed(MouseEvent e) {
            /** @todo check for popup trigger on linux
             *
             */
//            if (e.isPopupTrigger()) {
            // using SwingUtilities because popuptrigger does not work on linux
            if (SwingUtilities.isRightMouseButton(e)) {
               actionPopup(e);
            }

         }
         public void mouseReleased(MouseEvent e) {
//            System.out.println("Mouse Released");

         }

         public void mouseClicked(MouseEvent e) {

               if (SwingUtilities.isRightMouseButton(e)) {
                  return;
               }

               if (e.getClickCount() == 2 & doubleClick) {

                  screen.sendKeys("[enter]");
               }
               else {
         			int pos = bi.getPosFromView(e.getX(), e.getY());
                  if (log.isDebugEnabled()) {
                     log.debug((screen.getRow(pos)) + "," + (screen.getCol(pos)));
                     log.debug(e.getX() + "," + e.getY() + "," + bi.columnWidth + ","
                           + bi.rowHeight);
                  }

                  boolean moved = screen.moveCursor(e, pos);
				      // this is a note to not execute this code here when we
      				// implement the remain after edit function option.
                  if (moved) {
                     if (rubberband.isAreaSelected()) {
                        rubberband.reset();
                     }
                     screen.repaintScreen();
                  }
                  getFocusForMe();
               }
         }

      });

      scroller = new SessionScroller().getScrollerInstance((SessionGUI)this);
      if (!sesConfig.getStringProperty("mouseWheel").equals("Yes"))
         scroller.removeMouseWheelListener((SessionGUI)this);

		log.debug("Initializing macros");
      macros = new Macronizer();
      Macronizer.init();

      keyPad.addActionListener(this);
      if (sesConfig.getStringProperty("keypad").equals("Yes"))
         keyPad.setVisible(true);
      else
         keyPad.setVisible(false);

      // Warning do not change the the order of the adding of keypad and
      //    the screen.  This will cause resizing problems because it will
      //    resize the screen first and during the resize we need to calculate
      //    the bouding area based on the height of the keyPad.
      //    See resizeMe() and getDrawingBounds()
      this.add(keyPad,BorderLayout.SOUTH);
      this.add(s,BorderLayout.CENTER);

      this.requestFocus();
      jumpEvent = new SessionJumpEvent(this);


      // check if double click sends enter
      if (sesConfig.getStringProperty("doubleClick").equals("Yes"))
         doubleClick = true;
      else
         doubleClick = false;

//         DropTargetAdapter dta = new DropTargetAdapter() {
//            public void drop(DropTargetDropEvent dtde) {
//               Transferable tr = dtde.getTransferable();
//               dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
//               DataFlavor[] dfs = dtde.getCurrentDataFlavors();
//               if(tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
//                  try {
//                     log.debug("dtde drop it2 ");
//
//                     java.util.List fileList =
//                        (java.util.List)tr.getTransferData(DataFlavor.javaFileListFlavor);
//                        // implementation for when we are able to process a list
//                        //   of files.
//   //                   Iterator iterator = fileList.iterator();
//   //                   if (iterator.hasNext()) {
//   //                      File file = (File)iterator.next();
//   //                   }
//                     java.io.File file = (java.io.File)fileList.get(0);
//                     System.out.println(file.toString());
//                     dtde.dropComplete(true);
//                     doTransfer(file);
//                     return;
//                  }
//                  catch (UnsupportedFlavorException ufe) {
//                     log.info("importData: unsupported data flavor");
//                  }
//                  catch (java.io.IOException ieo) {
//                     log.warn("importData: I/O exception");
//                  }
//                  catch (Exception ex) {
//                     log.warn(""+ex.getMessage());
//                  }
//                  finally {
//                     dtde.dropComplete(false);
//                  }
//               }
//            }
//         };
//         DropTarget dt = new DropTarget((JPanel)this,dta);
//
//         setDropTarget(dt);

   }

   public void setRunningHeadless(boolean headless) {
      if (headless) {
         screen.getOIA().removeOIAListener(bi);
         screen.removeScreenListener(bi);
      }
      else {
         screen.getOIA().addOIAListener(bi);
         screen.addScreenListener(bi);

      }

   }

   private void actionTransfer(java.io.File file) {

      try {
         Properties props = new Properties();
         props.load(new java.io.FileInputStream(file));
         org.tn5250j.tools.XTFRFile tfr = new org.tn5250j.tools.XTFRFile(null,
            session.getVT(), (SessionGUI)this,props);
      }
      catch (Exception exc) {
      	log.warn(""+exc.getMessage());
      }

   }

   public void processKeyEvent(KeyEvent evt) {

      keyHandler.processKeyEvent(evt);

      if(!evt.isConsumed())
         super.processKeyEvent(evt);
   }


   private void dumpStuff(Throwable ex) {

      session.getVT().dumpStuff();
      if (null == ex) {
         return;
      }
      ex.printStackTrace();
   }

   public void sendScreenEMail() {

      new SendEMailDialog((JFrame)SwingUtilities.getRoot(this),(SessionGUI)this);
   }

   private void sendMeToFile() {
      // Change sent by LUC - LDC to add a parent frame to be passed
      new SendScreenToFile((JFrame)SwingUtilities.getRoot(this),screen);
   }

   private void sendMeToImageFile() {
      // Change sent by LUC - LDC to add a parent frame to be passed
      new SendScreenImageToFile((JFrame)SwingUtilities.getRoot(this),(SessionGUI)this);
   }
   /**
    * This routine allows areas to be bounded by using the keyboard
    *
    * @param ke
    * @param last
    */
   public void doKeyBoundArea(KeyEvent ke,String last) {

      Point p = new Point();

      // If there is not area selected then we send to the previous position
      // of the cursor because the cursor position has already been updated
      // to the current position.
      //
      // The getPointFromRowCol is 0,0 based so we will take the current row
      // and column and make these calculations ourselves to be passed
      if (!rubberband.isAreaSelected()) {

         // mark left we will mark the column to the right of where the cursor
         // is now.
         if (last.equals("[markleft]"))
            bi.getPointFromRowCol(screen.getCurrentRow() - 1,
                                    screen.getCurrentCol() + 1,
                                    p);
         // mark right will mark the current position to the left of the
         // current cursor position
         if (last.equals("[markright]"))
            bi.getPointFromRowCol(screen.getCurrentRow() - 1,
                                    screen.getCurrentCol()-2,
                                    p);


         if (last.equals("[markup]"))
            bi.getPointFromRowCol(screen.getCurrentRow() + 1,
                                    screen.getCurrentCol() - 1,
                                    p);
         // mark down will mark the current position minus the current
         // row.
         if (last.equals("[markdown]"))
            bi.getPointFromRowCol(screen.getCurrentRow() - 2,
                                    screen.getCurrentCol() - 1,
                                    p);
         MouseEvent me = new MouseEvent(this,
                              MouseEvent.MOUSE_PRESSED,
                              System.currentTimeMillis(),
                              MouseEvent.BUTTON1_MASK,
                              p.x,p.y,
                              1,false);
         dispatchEvent(me);

      }

      bi.getPointFromRowCol(screen.getCurrentRow() - 1,
                                 screen.getCurrentCol() - 1,
                                 p);
//      rubberband.getCanvas().translateEnd(p);
      MouseEvent me = new MouseEvent(this,
                           MouseEvent.MOUSE_DRAGGED,
                           System.currentTimeMillis(),
                           MouseEvent.BUTTON1_MASK,
                           p.x,p.y,
                           1,false);
      dispatchEvent(me);

   }

   public void closeSession() {

      Object[]      message = new Object[1];
      message[0] = LangTool.getString("cs.message");

      String[] options = {LangTool.getString("cs.optThis"),
                           LangTool.getString("cs.optAll"),
                           LangTool.getString("cs.optCancel")};

      int result = JOptionPane.showOptionDialog(
             this.getParent(),            // the parent that the dialog blocks
             message,                           // the dialog message array
             LangTool.getString("cs.title"),    // the title of the dialog window
             JOptionPane.DEFAULT_OPTION,        // option type
             JOptionPane.QUESTION_MESSAGE,      // message type
             null,                              // optional icon, use null to use the default icon
             options,                           // options string array, will be made into buttons//
             options[0]                         // option that should be made into a default button
         );

      if (result == 0) {
         if (!((SessionGUI)this).isOnSignOnScreen()) {

            if (confirmClose()) {
               closeMe();

            }
         }
         else {
            closeMe();
         }

      }
      if (result == 1) {
         fireEmulatorAction(EmulatorActionEvent.CLOSE_EMULATOR);
      }

   }

   /**
    * Check is the parameter to confirm that the Sign On screen is the current
    * screen.  If it is then we check against the saved Signon Screen in memory
    * and take the appropriate action.
    *
    * @return whether or not the signon on screen is the current screen
    */
   private boolean confirmClose() {

      if (sesConfig.isPropertyExists("confirmSignoff") &&
               sesConfig.getStringProperty("confirmSignoff").equals("Yes")) {

         int result = JOptionPane.showConfirmDialog(
                this.getParent(),            // the parent that the dialog blocks
                LangTool.getString("messages.signOff"),  // the dialog message array
                LangTool.getString("cs.title"),    // the title of the dialog window
                JOptionPane.CANCEL_OPTION        // option type
            );

         if (result == 0) {
            return true;
         }

         return false;
      }
      else {
         return true;
      }
   }

   public void getFocusForMe() {
      this.grabFocus();
   }

   public boolean isFocusTraversable () {
      return true;
   }

   // Override to inform focus manager that component is managing focus changes.
   //    This is to capture the tab and shift+tab keys.
   public boolean isManagingFocus() { return true; }

   public JPanel getDrawingCanvas() {

      return s;

   }

   public void actionPerformed(ActionEvent actionevent) {

      Object obj = actionevent.getSource();
      String ac = ((JButton)obj).getActionCommand();

      if (ac.equals("NXTPAD"))
         keyPad.nextPad();
      else
         screen.sendKeys(ac);


      getFocusForMe();

   }

   /**
    * Update the configuration settings
    * @param pce
    */
   public void onConfigChanged(SessionConfigEvent pce) {

      String pn = pce.getPropertyName();

      if (pn.equals("keypad")) {
         if (((String)pce.getNewValue()).equals("Yes")) {
            keyPad.setVisible(true);
         }
         else {
            keyPad.setVisible(false);
         }
         this.validate();
      }

      if (pn.equals("doubleClick")) {
         if (((String)pce.getNewValue()).equals("Yes")) {
            doubleClick = true;
         }
         else {
            doubleClick = false;
         }
      }

      if (pn.equals("mouseWheel")) {
         if (((String)pce.getNewValue()).equals("Yes")) {
            scroller.addMouseWheelListener((SessionGUI)this);
         }
         else {
            scroller.removeMouseWheelListener((SessionGUI)this);
         }
      }

      resizeMe();
      repaint();

   }

   public tnvt getVT() {

      return session.getVT();

   }

   public void toggleDebug() {
      session.getVT().toggleDebug();
   }

   public void startNewSession() {
      fireEmulatorAction(EmulatorActionEvent.START_NEW_SESSION);
   }

   public void startDuplicateSession() {
      fireEmulatorAction(EmulatorActionEvent.START_DUPLICATE);
   }

   public void sendAidKey(int whichOne) {

      session.getVT().sendAidKey(whichOne);
   }

   public void changeConnection() {

      if (session.getVT().isConnected()) {

         session.getVT().disconnect();

      }
      else {
         // lets set this puppy up to connect within its own thread
         Runnable connectIt = new Runnable() {
            public void run() {
               session.getVT().connect();
            }

           };

         // now lets set it to connect within its own daemon thread
         //    this seems to work better and is more responsive than using
         //    swingutilities's invokelater
         Thread ct = new Thread(connectIt);
         ct.setDaemon(true);
         ct.start();

      }

   }

   public void nextSession() {

      fireSessionJump(JUMP_NEXT);

   }

   public void prevSession() {

      fireSessionJump(JUMP_PREVIOUS);

   }

   /**
    * Notify all registered listeners of the onSessionJump event.
    *
    * @param dir  The direction to jump.
    */
   protected void fireSessionJump(int dir) {

      if (listeners != null) {
         int size = listeners.size();
         for (int i = 0; i < size; i++) {
            SessionJumpListener target =
                    (SessionJumpListener)listeners.elementAt(i);
            jumpEvent.setJumpDirection(dir);
            target.onSessionJump(jumpEvent);
         }
      }
   }

   /**
    * Notify all registered listeners of the onEmulatorAction event.
    *
    * @param action  The action to be performed.
    */
   protected void fireEmulatorAction(int action) {

      if (actionListeners != null) {
         int size = actionListeners.size();
         for (int i = 0; i < size; i++) {
            EmulatorActionListener target =
                    (EmulatorActionListener)actionListeners.elementAt(i);
            EmulatorActionEvent sae = new EmulatorActionEvent(this);
            sae.setAction(action);
            target.onEmulatorAction(sae);
         }
      }
   }

   public boolean isMacroRunning() {

      return macroRunning;
   }

   public boolean isStopMacroRequested() {

      return stopMacro;
   }

   public boolean isSessionRecording() {

      return keyHandler.isRecording();
   }

   public void setMacroRunning(boolean mr) {
      macroRunning = mr;
      if (macroRunning)
         screen.getOIA().setScriptActive(true);
      else
         screen.getOIA().setScriptActive(false);

      stopMacro = !macroRunning;
   }

   public void setStopMacroRequested () {
      setMacroRunning(false);
   }

//   public void sendNegResponse2(int ec) {
//
//      vt.sendNegResponse2(ec);
//
//   }

   public void closeDown() {

      sesConfig.saveSessionProps(getParent());

      // Let's stop the cursor blinking as well as it seems to be causing problems
//      if (screen.isBlinkCursor()) {
//         screen.setBlinkCursorStop();
//      }

      session.getVT().disconnect();
      // Added by Luc to fix a memory leak. The keyHandler was still receiving
      //   events even though nothing was really attached.
      keyHandler.sessionClosed((SessionGUI)this);
      keyHandler = null;

   }

   /**
    * Show the session attributes screen for modification of the attribute/
    * settings of the session.
    *
    */
   public void actionAttributes() {

      SessionAttributes sa = new SessionAttributes((Frame)SwingUtilities.getRoot(this),
                                          sesConfig);
      sa.showIt();

      getFocusForMe();
      sa = null;
   }

   private void actionPopup (MouseEvent me) {

      new SessionPopup((SessionGUI)this,me);


   }

   public void actionSpool() {

      try {
         org.tn5250j.spoolfile.SpoolExporter spooler =
                        new org.tn5250j.spoolfile.SpoolExporter(session.getVT(), (SessionGUI)this);
         spooler.setVisible(true);
      }
      catch (NoClassDefFoundError ncdfe) {
         JOptionPane.showMessageDialog(this,
                                       LangTool.getString("messages.noAS400Toolbox"),
                                       "Error",
                                       JOptionPane.ERROR_MESSAGE,null);
      }

   }

   protected void closeMe() {

      fireEmulatorAction(EmulatorActionEvent.CLOSE_SESSION);

   }

   public void executeMacro(ActionEvent ae) {

      executeMacro(ae.getActionCommand());

   }

   public void executeMacro(String macro) {

      Macronizer.invoke(macro,(SessionGUI)this);

   }

   protected void stopRecordingMe() {
      if (keyHandler.getRecordBuffer().length() > 0) {
         Macronizer.setMacro(newMacName,keyHandler.getRecordBuffer());
         log.debug(keyHandler.getRecordBuffer());
      }

      keyHandler.stopRecording();
   }

   protected void startRecordingMe() {

      String macName = (String)JOptionPane.showInputDialog(null,
                                        LangTool.getString("macro.message"),
                                        LangTool.getString("macro.title"),
                                        JOptionPane.PLAIN_MESSAGE);
      if (macName != null) {
         macName = macName.trim();
         if (macName.length() > 0) {
            log.info(macName);
            newMacName = macName;
            keyHandler.startRecording();
         }
      }
   }


   public void resizeMe() {

      Rectangle r = getDrawingBounds();
      if (bi != null)
         bi.resizeScreenArea(r.width, r.height);
      screen.repaintScreen();
      Graphics g = getGraphics();
      if (g != null)
         g.setClip(0,0,this.getWidth(),this.getHeight());
      repaint(0,0,getWidth(),getHeight());
   }

   public Rectangle getDrawingBounds() {

      Rectangle r = this.getBounds();
      if (keyPad != null && keyPad.isVisible())
//         r.height -= (int)(keyPad.getHeight() * 1.25);
         r.height -= (int)(keyPad.getHeight());

      r.setSize(r.width,r.height);

      return r;

   }

   public void componentHidden(ComponentEvent e) {
   }

   public void componentMoved(ComponentEvent e) {
   }

   public void componentResized(ComponentEvent e) {

      resizeMe();
   }

   public void componentShown(ComponentEvent e) {


   }

   protected void paintComponent(Graphics g) {
	  log.debug("paint from screen");

     if (bi == null) {
      checkOffScreenImage();
     }
//      screen.paintComponent3(g);

		Graphics2D g2 = (Graphics2D) g;
         if (rubberband.isAreaSelected() && !rubberband.isDragging()) {
            rubberband.erase();
//   //         rubberband.draw();
         }

		//Rectangle r = g.getClipBounds();

		g2.setColor(bi.colorBg);
		g2.fillRect(0, 0, getWidth(), getHeight());

		bi.drawImageBuffer(g2);

      if (rubberband.isAreaSelected() && !rubberband.isDragging()) {
//         rubberband.erase();
         rubberband.draw();
      }

//      keyPad.repaint();

   }

   public void update(Graphics g) {
	  log.info("update paint from gui");
      paint(g);

   }

	public boolean isHotSpots() {
		return bi.hotSpots;
	}

	public void toggleHotSpots() {
		bi.hotSpots = !bi.hotSpots;
	}

	/**
	 * @todo: Change to be mnemonic key.
	 *
	 * This toggles the ruler line.
	 *
	 *
	 */
	public void crossHair() {
		screen.setCursorActive(false);
		bi.crossHair++;
		if (bi.crossHair > 3)
			bi.crossHair = 0;
		screen.setCursorActive(true);
	}

	/**
	 *
	 * This routine will make sure we have something to draw on
	 *
	 */
	private void checkOffScreenImage() {

		// do we have something already?
		if (bi == null) {

			bi = new GuiGraphicBuffer(screen,this,sesConfig);

//			if (antialiased) {
//				bi.setUseAntialias(true);
//			}

			// allocate a buffer Image with appropriate size
			bi.getImageBuffer(0, 0);

			// fill in the areas
//            tArea = new Rectangle2D.Float(0, 0, 0, 0);
//            cArea = new Rectangle2D.Float(0, 0, 0, 0);
//            aArea = new Rectangle2D.Float(0, 0, 0, 0);
//            sArea = new Rectangle2D.Float(0, 0, 0, 0);
//
//            // Draw Operator Information Area
//            drawOIA();
		}

	}


      /**
       *
       * Copy & Paste start code
       *
       */
      public final void actionCopy() {

         Rectangle workR = new Rectangle();
         // lets get the bounding area using a rectangle that we have already
         // allocated
         getBoundingArea(workR);

         rubberband.reset();

         screen.repaintScreen();

         screen.copyMe(workR);

      }

	/**
	 * Sum them
	 *
	 * @param which
	 *            formatting option to use
	 * @return vector string of numberic values
	 */
	protected final Vector sumThem(boolean which) {

		StringBuffer s = new StringBuffer();

      Rectangle workR = new Rectangle();
		getBoundingArea(workR);

		//      gui.rubberband.reset();
		//      gui.repaint();

		log.debug("Summing");
      return screen.sumThem(which,workR);
	}

	/**
	 *
	 * This routine is responsible for setting up a PrinterJob on this component
	 * and initiating the print session.
	 *
	 */
	public final void printMe() {

		Thread printerThread = new PrinterThread(screen, bi.font, screen.getColumns(),
				screen.getRows(), Color.black, true, (SessionGUI) this);

		printerThread.start();

	}

   /**
    * Add a SessionJumpListener to the listener list.
    *
    * @param listener  The SessionListener to be added
    */
   public synchronized void addSessionJumpListener(SessionJumpListener listener) {

      if (listeners == null) {
          listeners = new java.util.Vector(3);
      }
      listeners.addElement(listener);

   }

   /**
    * Remove a SessionJumpListener from the listener list.
    *
    * @param listener  The SessionJumpListener to be removed
    */
   public synchronized void removeSessionJumpListener(SessionJumpListener listener) {
      if (listeners == null) {
          return;
      }
      listeners.removeElement(listener);

   }

   /**
    * Add a EmulatorActionListener to the listener list.
    *
    * @param listener  The EmulatorActionListener to be added
    */
   public synchronized void addEmulatorActionListener(EmulatorActionListener listener) {

      if (actionListeners == null) {
          actionListeners = new java.util.Vector(3);
      }
      actionListeners.addElement(listener);

   }

   /**
    * Remove a EmulatorActionListener from the listener list.
    *
    * @param listener  The EmulatorActionListener to be removed
    */
   public synchronized void removeEmulatorActionListener(EmulatorActionListener listener) {
      if (actionListeners == null) {
          return;
      }
      actionListeners.removeElement(listener);

   }

   /**
    *
    * RubberBanding start code
    *
    */

   /**
    * Returns a pointer to the graphics area that we can draw on
    *
    */
   public Graphics getDrawingGraphics(){

      return bi.getDrawingArea();
   }

   protected final void setRubberBand(TNRubberBand newValue) {

      rubberband = newValue;

   }

   public Point translateStart(Point start) {
      return bi.translateStart(start);
   }

   public Point translateEnd(Point end) {
      return bi.translateEnd(end);
   }
	public int getPosFromView(int x, int y) {
      return bi.getPosFromView(x,y);
	}

	public void getBoundingArea(Rectangle bounds) {
      bi.getBoundingArea(bounds);
	}

   public void areaBounded(RubberBand band, int x1, int y1, int x2, int y2) {


//      repaint(x1,y1,x2-1,y2-1);
      repaint();
	  log.debug(" bound " + band.getEndPoint());
   }

   public boolean canDrawRubberBand(RubberBand b) {

      // before we get the row col we first have to translate the x,y point
      //   back to screen coordinates because we are translating the starting
      //   point to the 5250 screen coordinates
//      return !screen.isKeyboardLocked() && (screen.isWithinScreenArea(b.getStartPoint().x,b.getStartPoint().y));
      return bi.isWithinScreenArea(b.getStartPoint().x,b.getStartPoint().y);

   }

      /**
       *
       * RubberBanding end code
       *
       */

   public class TNRubberBand extends RubberBand {

      public TNRubberBand(RubberBandCanvasIF c) {
         super(c);
      }

      protected void drawBoundingShape(Graphics g, int startX, int startY, int width, int height) {
         g.drawRect(startX,startY,width,height);
      }

      protected Rectangle getBoundingArea() {

         Rectangle r = new Rectangle();
         getBoundingArea(r);
         return r;
      }

      protected void getBoundingArea(Rectangle r) {

         if((getEndPoint().x > getStartPoint().x) && (getEndPoint().y > getStartPoint().y)) {
            r.setBounds(getStartPoint().x,getStartPoint().y,getEndPoint().x-getStartPoint().x,getEndPoint().y-getStartPoint().y);
         }

         else if((getEndPoint().x < getStartPoint().x) && (getEndPoint().y < getStartPoint().y)) {
            r.setBounds(getEndPoint().x,getEndPoint().y,getStartPoint().x-getEndPoint().x,getStartPoint().y-getEndPoint().y);
         }

         else if((getEndPoint().x > getStartPoint().x) && (getEndPoint().y < getStartPoint().y))  {
            r.setBounds(getStartPoint().x,getEndPoint().y,getEndPoint().x-getStartPoint().x,getStartPoint().y-getEndPoint().y);
         }

         else if((getEndPoint().x < getStartPoint().x) && (getEndPoint().y > getStartPoint().y)) {
            r.setBounds(getEndPoint().x,getStartPoint().y,getStartPoint().x-getEndPoint().x,getEndPoint().y-getStartPoint().y);
         }

//         return r;
      }

      protected Point getEndPoint() {

         if(this.endPoint == null) {
            Point p = new Point(0,0);
            bi.getPointFromRowCol(0,0,p);
            setEndPoint(p);
         }
         return this.endPoint;
      }

      protected Point getStartPoint() {

         if(this.startPoint == null) {
            Point p = new Point(0,0);
            bi.getPointFromRowCol(0,0,p);
            setStartPoint(p);
         }
         return this.startPoint;

      }
   }

}


