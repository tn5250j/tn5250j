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
import javax.swing.event.*;
import javax.swing.text.*;
import java.util.*;
import java.text.*;
import java.beans.*;

import org.tn5250j.tools.*;
import org.tn5250j.encoding.*;
import org.tn5250j.mailtools.*;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.event.KeyChangeListener;
import org.tn5250j.keyboard.KeyboardHandler;
import org.tn5250j.keyboard.DefaultKeyboardHandler;

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
   My5250 me;
   TNRubberBand rubberband;
   JPanel s = new JPanel();
   KeyPad keyPad = new KeyPad();
   private JPopupMenu popup = null;
   Macronizer macros;
   String newMacName;
   private Vector listeners = null;
   private SessionJumpEvent jumpEvent;
   private boolean macroRunning;
   private boolean stopMacro;
   private boolean doubleClick;
   private SessionConfig sesConfig;
   private KeyboardHandler keyHandler;

   public Gui5250 () {

   }

   //Construct the frame
   public Gui5250(My5250 m,SessionConfig config) {

      me = m;

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
      macros.init();

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

   public SessionConfig getConfiguration() {

      return sesConfig;
   }

   public void sendScreenEMail() {

      new SendEMailDialog((JFrame)SwingUtilities.getRoot(this),(Session)this);
   }

   private void sendMeToFile() {
      new SendScreenToFile(screen);

   }

   public void doKeyBoundArea(KeyEvent ke,String last) {

      Point p = new Point();

      if (!rubberband.isAreaSelected()) {
         if (last.equals("[markleft]"))
            screen.getPointFromRowCol(screen.getCurrentRow() - 1,
                                    screen.getCurrentCol()-1,
                                    p);
         if (last.equals("[markright]"))
            screen.getPointFromRowCol(screen.getCurrentRow() - 1,
                                    screen.getCurrentCol()-2,
                                    p);
         if (last.equals("[markup]"))
            screen.getPointFromRowCol(screen.getCurrentRow() - 1,
                                    screen.getCurrentCol() - 1,
                                    p);
         if (last.equals("[markdown]"))
            screen.getPointFromRowCol(screen.getCurrentRow()-2,
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
      rubberband.getCanvas().translateEnd(p);
      MouseEvent me = new MouseEvent(this,
                           MouseEvent.MOUSE_DRAGGED,
                           System.currentTimeMillis(),
                           MouseEvent.BUTTON1_MASK,
                           p.x,p.y,
                           1,false);
      dispatchEvent(me);

   }

   public boolean isOnSignoffScreen() {
      return vt.isOnSignoffScreen();
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
         if (!isOnSignoffScreen()) {

            if (confirmClose()) {
               closeMe();

            }
         }
         else {
            closeMe();
         }

      }
      if (result == 1) {
         me.closingDown((Session)this);
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

   private void getFocusForMe() {
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
//      System.out.println("We got a key pad " + ac);

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
   }

   public void startNewSession() {
      me.startNewSession();
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

   public boolean isMacroRunning() {

      return macroRunning;
   }

   public boolean isStopMacroRequested() {

      return stopMacro;
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
      JMenuItem menuItem;
      Action action;
      popup = new JPopupMenu();
      final Gui5250 g = this;
      JMenuItem mi;

      final int pos = screen.getRowColFromPoint(me.getX(),me.getY());

      if (!rubberband.isAreaSelected() && screen.isInField(pos,false) ) {
         action = new AbstractAction(LangTool.getString("popup.copy")) {
               public void actionPerformed(ActionEvent e) {
                  screen.copyField(pos);
                  getFocusForMe();
               }
           };

         popup.add(createMenuItem(action,MNEMONIC_COPY));


         action = new AbstractAction(LangTool.getString("popup.paste")) {
               public void actionPerformed(ActionEvent e) {
                  screen.sendKeys(MNEMONIC_PASTE);
                  getFocusForMe();
               }
           };
         popup.add(createMenuItem(action,MNEMONIC_PASTE));

         action = new AbstractAction(LangTool.getString("popup.pasteSpecial")) {
               public void actionPerformed(ActionEvent e) {
                  screen.pasteMe(true);
                  getFocusForMe();
               }
           };
         popup.add(action);

         popup.addSeparator();
      }
      else {

         action = new AbstractAction(LangTool.getString("popup.copy")) {
               public void actionPerformed(ActionEvent e) {
                  screen.sendKeys(MNEMONIC_COPY);
                  getFocusForMe();
               }
           };

         popup.add(createMenuItem(action,MNEMONIC_COPY));

         action = new AbstractAction(LangTool.getString("popup.paste")) {
               public void actionPerformed(ActionEvent e) {
                  screen.sendKeys(MNEMONIC_PASTE);
                  getFocusForMe();
               }
           };
         popup.add(createMenuItem(action,MNEMONIC_PASTE));

         action = new AbstractAction(LangTool.getString("popup.pasteSpecial")) {
               public void actionPerformed(ActionEvent e) {
                  screen.pasteMe(true);
                  getFocusForMe();
               }
           };
         popup.add(action);

         Rectangle workR = new Rectangle();
         if (rubberband.isAreaSelected()) {

            rubberband.getBoundingArea(workR);
            // get the width and height
            int ePos = screen.getRowColFromPoint(workR.width + 1 ,
                                       workR.height + 1 );

            popup.addSeparator();

            menuItem = new JMenuItem(LangTool.getString("popup.selectedColumns")
                              + " " + screen.getCol(ePos));
            menuItem.setArmed(false);
            popup.add(menuItem);

            menuItem = new JMenuItem(LangTool.getString("popup.selectedRows")
                              + " " + screen.getRow(ePos));
            menuItem.setArmed(false);
            popup.add(menuItem);

            JMenu sumMenu = new JMenu(LangTool.getString("popup.calc"));
            popup.add(sumMenu);

            action = new AbstractAction(LangTool.getString("popup.calcGroupCD")) {
               public void actionPerformed(ActionEvent e) {
                  sumArea(true);
               }
            };
            sumMenu.add(action);

            action = new AbstractAction(LangTool.getString("popup.calcGroupDC")) {
               public void actionPerformed(ActionEvent e) {
                  sumArea(false);
               }
            };
            sumMenu.add(action);

         }

         popup.addSeparator();

         action = new AbstractAction(LangTool.getString("popup.printScreen")) {
               public void actionPerformed(ActionEvent e) {
                  screen.printMe();
                  getFocusForMe();
               }
           };
         popup.add(createMenuItem(action,MNEMONIC_PRINT_SCREEN));

         popup.addSeparator();

         JMenu kbMenu = new JMenu(LangTool.getString("popup.keyboard"));

         popup.add(kbMenu);

         action = new AbstractAction(LangTool.getString("popup.mapKeys")) {
               public void actionPerformed(ActionEvent e) {

                  mapMeKeys();
               }
           };
         kbMenu.add(action);

         kbMenu.addSeparator();

         action = new AbstractAction(LangTool.getString("key.[attn]")) {
               public void actionPerformed(ActionEvent e) {
                  screen.sendKeys("[attn]");
               }
           };
         kbMenu.add(createMenuItem(action,MNEMONIC_ATTN));

         action = new AbstractAction(LangTool.getString("key.[reset]")) {
               public void actionPerformed(ActionEvent e) {
                  screen.sendKeys("[reset]");
               }
           };
         kbMenu.add(createMenuItem(action,MNEMONIC_RESET));

         action = new AbstractAction(LangTool.getString("key.[sysreq]")) {
               public void actionPerformed(ActionEvent e) {
                  screen.sendKeys("[sysreq]");
               }
           };
         kbMenu.add(createMenuItem(action,MNEMONIC_SYSREQ));

         if (screen.isMessageWait()) {
            action = new AbstractAction(LangTool.getString("popup.displayMessages")) {
                  public void actionPerformed(ActionEvent e) {
                     vt.systemRequest('4');
                  }
              };

            kbMenu.add(createMenuItem(action,MNEMONIC_DISP_MESSAGES));
         }

         kbMenu.addSeparator();

         action = new AbstractAction(LangTool.getString("key.[dupfield]")) {
               public void actionPerformed(ActionEvent e) {
                  screen.sendKeys("[dupfield]");
               }
           };
         kbMenu.add(createMenuItem(action,MNEMONIC_DUP_FIELD));

         action = new AbstractAction(LangTool.getString("key.[help]")) {
               public void actionPerformed(ActionEvent e) {
                  screen.sendKeys("[help]");
               }
           };
         kbMenu.add(createMenuItem(action,MNEMONIC_HELP));

         action = new AbstractAction(LangTool.getString("key.[eraseeof]")) {
               public void actionPerformed(ActionEvent e) {
                  screen.sendKeys("[eraseeof]");
               }
           };
         kbMenu.add(createMenuItem(action,MNEMONIC_ERASE_EOF));

         action = new AbstractAction(LangTool.getString("key.[field+]")) {
               public void actionPerformed(ActionEvent e) {
                  screen.sendKeys("[field+]");
               }
           };
         kbMenu.add(createMenuItem(action,MNEMONIC_FIELD_PLUS));


         action = new AbstractAction(LangTool.getString("key.[field-]")) {
               public void actionPerformed(ActionEvent e) {
                  screen.sendKeys("[field-]");
               }
           };
         kbMenu.add(createMenuItem(action,MNEMONIC_FIELD_MINUS));


         action = new AbstractAction(LangTool.getString("key.[newline]")) {
               public void actionPerformed(ActionEvent e) {
                  screen.sendKeys("[newline]");
               }
           };
         kbMenu.add(createMenuItem(action,MNEMONIC_NEW_LINE));


         action = new AbstractAction(LangTool.getString("popup.hostPrint")) {
               public void actionPerformed(ActionEvent e) {
                  vt.hostPrint(1);
               }
           };
         kbMenu.add(createMenuItem(action,MNEMONIC_PRINT));

         createShortCutItems(kbMenu);

         if (screen.isMessageWait()) {
            action = new AbstractAction(LangTool.getString("popup.displayMessages")) {
                  public void actionPerformed(ActionEvent e) {
                     vt.systemRequest('4');
                  }
              };
            popup.add(createMenuItem(action,MNEMONIC_DISP_MESSAGES));
         }

         popup.addSeparator();

         action = new AbstractAction(LangTool.getString("popup.hexMap")) {
               public void actionPerformed(ActionEvent e) {
                  showHexMap();
                  getFocusForMe();
               }
           };
         popup.add(createMenuItem(action,""));

         action = new AbstractAction(LangTool.getString("popup.mapKeys")) {
               public void actionPerformed(ActionEvent e) {

                  mapMeKeys();
                  getFocusForMe();
               }
           };
         popup.add(createMenuItem(action,""));

         action = new AbstractAction(LangTool.getString("popup.settings")) {
               public void actionPerformed(ActionEvent e) {
                  doAttributes();
                  getFocusForMe();
               }
           };
         popup.add(createMenuItem(action,MNEMONIC_DISP_ATTRIBUTES));


         popup.addSeparator();

         if (isMacroRunning()) {
            action = new AbstractAction(LangTool.getString("popup.stopScript")) {
                  public void actionPerformed(ActionEvent e) {
                     setStopMacroRequested();
                  }
              };
            popup.add(action);
         }
         else {

            JMenu macMenu = new JMenu(LangTool.getString("popup.macros"));

            if (keyHandler.isRecording()) {
               action = new AbstractAction(LangTool.getString("popup.stop")) {
                     public void actionPerformed(ActionEvent e) {
                        stopRecordingMe();
                        getFocusForMe();
                     }
               };

            }
            else {
               action = new AbstractAction(LangTool.getString("popup.record")) {
                     public void actionPerformed(ActionEvent e) {
                        startRecordingMe();
                        getFocusForMe();

                     }
               };
            }
            macMenu.add(action);
            if (macros.isMacrosExist()) {
               // this will add a sorted list of the macros to the macro menu
               addMacros(macMenu);
            }
            popup.add(macMenu);
         }

         popup.addSeparator();

         JMenu xtfrMenu = new JMenu(LangTool.getString("popup.export"));

         action = new AbstractAction(LangTool.getString("popup.xtfrFile")) {
               public void actionPerformed(ActionEvent e) {
                  doMeTransfer();
                  getFocusForMe();
               }
           };

         xtfrMenu.add(createMenuItem(action,MNEMONIC_FILE_TRANSFER));

         action = new AbstractAction(LangTool.getString("popup.xtfrSpool")) {
               public void actionPerformed(ActionEvent e) {
                  doMeSpool();
                  getFocusForMe();
               }
           };

         xtfrMenu.add(action);

         popup.add(xtfrMenu);

         JMenu sendMenu = new JMenu(LangTool.getString("popup.send"));
         popup.add(sendMenu);


         action = new AbstractAction(LangTool.getString("popup.email")) {
               public void actionPerformed(ActionEvent e) {
                  sendScreenEMail();
                  getFocusForMe();
               }
           };
         sendMenu.add(createMenuItem(action,MNEMONIC_E_MAIL));

         action = new AbstractAction(LangTool.getString("popup.file")) {
               public void actionPerformed(ActionEvent e) {
                  sendMeToFile();
               }
           };

         sendMenu.add(action);

         popup.addSeparator();

      }

      action = new AbstractAction(LangTool.getString("popup.connections")) {
            public void actionPerformed(ActionEvent e) {
               doConnections();
            }
        };

      popup.add(createMenuItem(action,MNEMONIC_OPEN_NEW));

      popup.addSeparator();

      if (vt.isConnected()) {
         action = new AbstractAction(LangTool.getString("popup.disconnect")) {
               public void actionPerformed(ActionEvent e) {
                  changeConnection();
                  getFocusForMe();
               }
           };
      }
      else {

         action = new AbstractAction(LangTool.getString("popup.connect")) {
               public void actionPerformed(ActionEvent e) {
                  changeConnection();
                  getFocusForMe();
               }
           };


      }
      popup.add(createMenuItem(action,MNEMONIC_TOGGLE_CONNECTION));

      action = new AbstractAction(LangTool.getString("popup.close")) {
            public void actionPerformed(ActionEvent e) {
               closeSession();
            }
        };

      popup.add(createMenuItem(action,MNEMONIC_CLOSE));

      GUIGraphicsUtils.positionPopup(me.getComponent(),popup,
               me.getX(),me.getY());

   }

   private void addMacros(JMenu menu) {

      LoadMacroMenu.loadMacros((Session)this,macros,menu);
   }

   private JMenuItem createMenuItem(Action action, String accelKey) {
      JMenuItem mi;

      mi =new JMenuItem();
      mi.setAction(action);
      if (keyHandler.isKeyStrokeDefined(accelKey))
         mi.setAccelerator(keyHandler.getKeyStroke(accelKey));
      else {

         InputMap map = getInputMap();
         KeyStroke[] allKeys = map.allKeys();
         for (int x = 0; x < allKeys.length; x++) {

            if (((String)map.get(allKeys[x])).equals(accelKey)) {
               mi.setAccelerator(allKeys[x]);
               break;
            }
         }

      }
      return mi;
   }

   private void createShortCutItems(JMenu menu) {

      JMenuItem mi;
      JMenu sm = new JMenu(LangTool.getString("popup.shortCuts"));
      menu.addSeparator();
      menu.add(sm);

      InputMap map = getInputMap();
      KeyStroke[] allKeys = map.allKeys();
      ActionMap aMap = getActionMap();

      for (int x = 0; x < allKeys.length; x++) {

         mi =new JMenuItem();
         Action a = aMap.get((String)map.get(allKeys[x]));
         mi.setAction(a);
         mi.setText(LangTool.getString("key." + (String)map.get(allKeys[x])));
         mi.setAccelerator(allKeys[x]);
         sm.add(mi);
      }
   }

   private void doConnections() {

      me.startNewSession();
   }

   public void doMeTransfer() {

      XTFRFile xtrf = new XTFRFile((JFrame)SwingUtilities.getRoot(this),
                                    vt,(Session)this);

   }

   private void doMeSpool() {

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

   private void sumArea(boolean which) {


      Vector sumVector = screen.sumThem(which);
      Iterator l = sumVector.iterator();
      double sum = 0.0;
      double inter = 0.0;
      while (l.hasNext()) {

         inter = 0.0;
         try {
            inter = ((Double)l.next()).doubleValue();
         }
         catch (Exception e) {
            System.out.println(e.getMessage());
         }
         System.out.println(inter);

         sum += inter;

      }
      System.out.println("Vector sum " + sum);
      sumVector = null;
      l = null;

      // obtain the decimal format for parsing
      DecimalFormat df =
            (DecimalFormat)NumberFormat.getInstance() ;

      DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();

      if (which) {
         dfs.setDecimalSeparator('.');
         dfs.setGroupingSeparator(',');
      }
      else {
         dfs.setDecimalSeparator(',');
         dfs.setGroupingSeparator('.');
      }

      df.setDecimalFormatSymbols(dfs);
      df.setMinimumFractionDigits(6);

      JOptionPane.showMessageDialog(null,
                                    df.format(sum),
                                    LangTool.getString("popup.calc"),
                                    JOptionPane.INFORMATION_MESSAGE);

   }

   private void closeMe() {

      me.closeSession((Session)this);

//      keyMap.removeKeyChangeListener(this);
   }

   public void executeMeMacro(ActionEvent ae) {

      executeMeMacro(ae.getActionCommand());

   }

   public void executeMeMacro(String macro) {

      macros.invoke(macro,(Session)this);

   }

   private void mapMeKeys() {
      KeyConfigure kc;

      Frame parent = null;

      if (me == null)
         parent = new JFrame();
      else
         parent = me.getParentView((Session)this);

      if (macros.isMacrosExist()) {
         String[] macrosList = macros.getMacroList();
         kc = new KeyConfigure(parent,macrosList,vt.getCodePage());
      }
      else
         kc = new KeyConfigure(parent,null,vt.getCodePage());

   }

   private void stopRecordingMe() {
      if (keyHandler.getRecordBuffer().length() > 0) {
         macros.setMacro(newMacName,keyHandler.getRecordBuffer());
         System.out.println(keyHandler.getRecordBuffer());
      }

      keyHandler.stopRecording();
   }

   private void startRecordingMe() {

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

   public void runScript () {

      Macronizer.showRunScriptDialog((Session)this);
      getFocusForMe();

   }

   private void showHexMap() {

      JPanel srp = new JPanel();
      srp.setLayout(new BorderLayout());
      DefaultListModel listModel = new DefaultListModel();
      StringBuffer sb = new StringBuffer();

      // we will use a collator here so that we can take advantage of the locales
      Collator collator = Collator.getInstance();
      CollationKey key = null;

      Set set = new TreeSet();
      for (int x =0;x < 256; x++) {
         char c = vt.ebcdic2uni(x);
//         char ac = vt.getASCIIChar(x);
         char ac = vt.ebcdic2uni(x);
         if (!Character.isISOControl(ac)) {
            sb.setLength(0);
            if (Integer.toHexString(ac).length() == 1){
               sb.append("0x0" + Integer.toHexString(ac).toUpperCase());
            }
            else {
               sb.append("0x" + Integer.toHexString(ac).toUpperCase());
            }

            sb.append(" - " + c);
            key = collator.getCollationKey(sb.toString());

            set.add(key);
         }
      }

      Iterator iterator = set.iterator();
      while (iterator.hasNext()) {
         CollationKey keyc = (CollationKey)iterator.next();
         listModel.addElement(keyc.getSourceString());
     }

      //Create the list and put it in a scroll pane
      JList hm = new JList(listModel);

      hm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      hm.setSelectedIndex(0);
      JScrollPane listScrollPane = new JScrollPane(hm);
      listScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
      listScrollPane.setSize(40,100);
      srp.add(listScrollPane,BorderLayout.CENTER);
      Object[]      message = new Object[1];
      message[0] = srp;
      String[] options = {LangTool.getString("hm.optInsert"),
                           LangTool.getString("hm.optCancel")};

      int result = 0;

      // if me is null then we are running as an applet and we need to create
      //   a frame to pass
      JFrame parent = null;
      if (me == null)
         parent = new JFrame();
      else
         parent = me.getParentView((Session)this);

      result = JOptionPane.showOptionDialog(
          parent,   // the parent that the dialog blocks
          message,                           // the dialog message array
          LangTool.getString("hm.title"),    // the title of the dialog window
          JOptionPane.DEFAULT_OPTION,        // option type
          JOptionPane.INFORMATION_MESSAGE,      // message type
          null,                              // optional icon, use null to use the default icon
          options,                           // options string array, will be made into buttons//
          options[0]                         // option that should be made into a default button
      );

      switch(result) {
         case 0: // Insert character
            String k = "";
            if (((String)hm.getSelectedValue()).length() > 7)
               k += ((String)hm.getSelectedValue()).charAt(9);
            else
               k += ((String)hm.getSelectedValue()).charAt(7);
            screen.sendKeys(k);
            break;
         case 1: // Cancel
//		      System.out.println("Cancel");
            break;
         default:
            break;
      }


   }

   public void printMe() {

      screen.printMe();
      getFocusForMe();
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

      return !screen.isKeyboardLocked() && (screen.isWithinScreenArea(b.getStartPoint().x,b.getStartPoint().y));

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

   }

}

