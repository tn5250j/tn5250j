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
import javax.swing.*;
import java.util.*;
import org.tn5250j.tools.*;
import org.tn5250j.mailtools.*;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.keyboard.KeyboardHandler;
import org.tn5250j.event.EmulatorActionListener;
import org.tn5250j.event.EmulatorActionEvent;

public class Gui5250 extends JPanel implements ComponentListener,
                                                      ActionListener,
                                                      TN5250jConstants,
                                                      RubberBandCanvasIF,
                                                      SessionListener,
                                                      SessionConfigListener {

   BorderLayout borderLayout1 = new BorderLayout();
   Screen5250 screen = null;
   String propFileName = null;
   tnvt vt = null;
   TNRubberBand rubberband;
   JPanel s = new JPanel();
   KeyPad keyPad = new KeyPad();
   private JPopupMenu popup = null;
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

   public Gui5250 () {

   }

   //Construct the frame
   public Gui5250(SessionConfig config) {

      propFileName = config.getConfigurationResource();

      sesConfig = config;

      enableEvents(AWTEvent.WINDOW_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);

      try  {
         jbInit();
      }
      catch(Exception e) {
         e.printStackTrace();
      }

   }

   //Component initialization
  private void jbInit() throws Exception  {
      this.setLayout(borderLayout1);

//      this.setOpaque(false);
//      setDoubleBuffered(true);
      s.setOpaque(false);
      s.setDoubleBuffered(false);

      screen = new Screen5250(this,sesConfig);
      this.addComponentListener(this);

      if (!sesConfig.isPropertyExists("width") ||
         !sesConfig.isPropertyExists("height"))
         // set the initialize size
         this.setSize(screen.getPreferredSize());
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
               doPopup(e);
            }

         }
         public void mouseReleased(MouseEvent e) {
//            System.out.println("Mouse Released");

         }

         public void mouseClicked(MouseEvent e) {

               if (e.getClickCount() == 2 & doubleClick) {

                  screen.sendKeys("[enter]");
               }
               else {
                  screen.moveCursor(e);
                  repaint();
                  getFocusForMe();
               }
         }

      });

      keyHandler = KeyboardHandler.getKeyboardHandlerInstance((Session)this);

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

      setRubberBand(new TNRubberBand(this));
      this.requestFocus();
      jumpEvent = new SessionJumpEvent(this);


      // check if double click sends enter
      if (sesConfig.getStringProperty("doubleClick").equals("Yes"))
         doubleClick = true;
      else
         doubleClick = false;

   }

   public void processKeyEvent(KeyEvent evt) {

      keyHandler.processKeyEvent(evt);

      if(!evt.isConsumed())
         super.processKeyEvent(evt);
   }


   private void dumpStuff(Throwable ex) {

      vt.dumpStuff();
      if (null == ex) {
         return;
      }
      ex.printStackTrace();
   }

   public void sendScreenEMail() {

      new SendEMailDialog((JFrame)SwingUtilities.getRoot(this),(Session)this);
   }

   private void sendMeToFile() {
      new SendScreenToFile(screen);
   }

   private void sendMeToImageFile() {
      new SendScreenImageToFile((Session)this);
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
            screen.getPointFromRowCol(screen.getCurrentRow() - 1,
                                    screen.getCurrentCol() + 1,
                                    p);
         // mark right will mark the current position to the left of the
         // current cursor position
         if (last.equals("[markright]"))
            screen.getPointFromRowCol(screen.getCurrentRow() - 1,
                                    screen.getCurrentCol()-2,
                                    p);


         if (last.equals("[markup]"))
            screen.getPointFromRowCol(screen.getCurrentRow() + 1,
                                    screen.getCurrentCol() - 1,
                                    p);
         // mark down will mark the current position minus the current
         // row.
         if (last.equals("[markdown]"))
            screen.getPointFromRowCol(screen.getCurrentRow() - 2,
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

      screen.getPointFromRowCol(screen.getCurrentRow() - 1,
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
         if (!((Session)this).isOnSignOnScreen()) {

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

   public Screen5250 getScreen() {

      return screen;

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

      screen.propertyChange(pce);

      resizeMe();
      repaint();

   }

   public void onSessionChanged(SessionChangeEvent changeEvent) {

      switch (changeEvent.getState()) {
         case STATE_CONNECTED:

            String mac = sesConfig.getStringProperty("connectMacro");
            if (mac.length() > 0)
               executeMeMacro(mac);
            break;
      }
   }

   protected void setVT(tnvt v) {

      vt = v;
      screen.setVT(vt);

   }

   public tnvt getVT() {

      return vt;

   }

   public void toggleDebug() {
      vt.toggleDebug();
      try {
         vt.sendHeartBeat();
      }
      catch (Exception exc) {
         System.out.println(exc.getMessage());
      }
   }

   public void startNewSession() {
      fireEmulatorAction(EmulatorActionEvent.START_NEW_SESSION);
   }

   public void startDuplicateSession() {
      fireEmulatorAction(EmulatorActionEvent.START_DUPLICATE);
   }

   public void sendAidKey(int whichOne) {

      vt.sendAidKey(whichOne);
   }

   public void changeConnection() {

      if (vt.isConnected()) {

         vt.disconnect();

      }
      else {
         // lets set this puppy up to connect within its own thread
         Runnable connectIt = new Runnable() {
            public void run() {
               vt.connect();
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
         screen.setSRIndicatorOn();
      else
         screen.setSRIndicatorOff();

      stopMacro = !macroRunning;
   }

   public void setStopMacroRequested () {
      setMacroRunning(false);
   }

   public void sendNegResponse2(int ec) {

      vt.sendNegResponse2(ec);

   }

   public void closeDown() {

      sesConfig.saveSessionProps(getParent());
      vt.disconnect();

   }

   /**
    * Show the session attributes screen for modification of the attribute/
    * settings of the session.
    *
    */
   public void doAttributes() {

      SessionAttributes sa = new SessionAttributes(
                                          (JFrame)SwingUtilities.getRoot(this),
                                          sesConfig);
      sa.showIt();

      getFocusForMe();
      sa = null;
   }

   private void doPopup (MouseEvent me) {

      new SessionPopup((Session)this,me);


   }

   public void doMeSpool() {

      try {
         org.tn5250j.spoolfile.SpoolExporter spooler =
                        new org.tn5250j.spoolfile.SpoolExporter(vt, (Session)this);
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

   public void executeMeMacro(ActionEvent ae) {

      executeMeMacro(ae.getActionCommand());

   }

   public void executeMeMacro(String macro) {

      Macronizer.invoke(macro,(Session)this);

   }

   protected void stopRecordingMe() {
      if (keyHandler.getRecordBuffer().length() > 0) {
         Macronizer.setMacro(newMacName,keyHandler.getRecordBuffer());
         System.out.println(keyHandler.getRecordBuffer());
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
            System.out.println(macName);
            newMacName = macName;
            keyHandler.startRecording();
         }
      }
   }


   public void resizeMe() {


      screen.setBounds(getDrawingBounds());

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
//      System.out.println("paint from screen");

      screen.paintComponent3(g);
      if (rubberband.isAreaSelected() && !rubberband.isDragging()) {
         rubberband.erase();
         rubberband.draw();
      }
      keyPad.repaint();

   }

   public void update(Graphics g) {
//      System.out.println("paint from gui");
      paint(g);

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

      return screen.getDrawingArea();
   }

   protected final void setRubberBand(TNRubberBand newValue) {

      rubberband = newValue;

   }

   public Point translateStart(Point start) {
      return screen.translateStart(start);
   }

   public Point translateEnd(Point end) {
      return screen.translateEnd(end);
   }

   public void areaBounded(RubberBand band, int x1, int y1, int x2, int y2) {


//      repaint(x1,y1,x2-1,y2-1);
      repaint();
//      System.out.println(" bound " + band.getEndPoint());
   }

   public boolean canDrawRubberBand(RubberBand b) {

      // before we get the row col we first have to translate the x,y point
      //   back to screen coordinates because we are translating the starting
      //   point to the 5250 screen coordinates
//      return !screen.isKeyboardLocked() && (screen.isWithinScreenArea(b.getStartPoint().x,b.getStartPoint().y));
      return screen.isWithinScreenArea(b.getStartPoint().x,b.getStartPoint().y);

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
            screen.getPointFromRowCol(0,0,p);
            setEndPoint(p);
         }
         return this.endPoint;
      }

      protected Point getStartPoint() {

         if(this.startPoint == null) {
            Point p = new Point(0,0);
            screen.getPointFromRowCol(0,0,p);
            setStartPoint(p);
         }
         return this.startPoint;

      }
   }

}


