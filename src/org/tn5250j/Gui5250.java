package org.tn5250j;
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

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.beans.*;
import org.tn5250j.tools.*;
import org.tn5250j.mailtools.*;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.event.KeyChangeListener;

public class Gui5250 extends JPanel implements ComponentListener,
                                                      ActionListener,
                                                      TN5250jConstants,
                                                      PropertyChangeListener ,
                                                      RubberBandCanvasIF,
                                                      KeyChangeListener,
                                                      SessionListener {

   BorderLayout borderLayout1 = new BorderLayout();
   Properties defaultProps = null;
   Screen5250 screen = null;
   String propFileName = null;
   tnvt vt = null;
   My5250 me;
   TNRubberBand rubberband;
   JPanel s = new JPanel();
   KeyPad keyPad = new KeyPad();
   private JPopupMenu popup = null;
   boolean keyProcessed = false;
   boolean gui = false;
   KeyMapper keyMap;
   Macronizer macros;
   String lastKeyStroke = null;
   StringBuffer recordBuffer;
   boolean recording;
   String newMacName;
   boolean isLinux;
   boolean isAltGr;
   private Vector listeners = null;
   private SessionJumpEvent jumpEvent;
   private boolean macroRunning;
   private boolean stopMacro;
   private boolean doubleClick;

   public Gui5250 () {

   }

   //Construct the frame
   public Gui5250(My5250 m) {

      this(m,null,false);
   }

   //Construct the frame
   public Gui5250(My5250 m,String properties, boolean useGui) {

      me = m;
      gui = useGui;
      propFileName = properties;

      String os = System.getProperty("os.name");
      if (os.toLowerCase().indexOf("linux") != -1) {
         System.out.println("using os " + os);
         isLinux = true;
      }

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
      this.setLayout(borderLayout1);

//      this.setOpaque(false);
      setDoubleBuffered(true);
      s.setOpaque(false);
      s.setDoubleBuffered(false);

      loadProps();
      screen = new Screen5250(this,defaultProps);
      this.addComponentListener(this);

      if (!defaultProps.containsKey("width") ||
         !defaultProps.containsKey("height"))
         // set the initialize size
         this.setSize(screen.getPreferredSize());
      else {

         this.setSize(Integer.parseInt((String)defaultProps.get("width")),
                     Integer.parseInt((String)defaultProps.get("height"))

         );
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

      addKeyListener(new KeyAdapter() {

         public void keyTyped(KeyEvent e) {
               processVTKeyTyped(e);

         }

         public void keyPressed(KeyEvent ke) {

            processVTKeyPressed(ke);
         }

         public void keyReleased(KeyEvent e) {


            processVTKeyReleased(e);

         }

      });
      keyMap = new KeyMapper();
      keyMap.init();

      keyMap.addKeyChangeListener(this);

      /**
       * this is taken out right now look at the method for description
       */
      initKeyBindings();

      macros = new Macronizer();
      macros.init();

      keyPad.addActionListener(this);
      if (getStringProperty("keypad").equals("Yes"))
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
      if (getStringProperty("doubleClick").equals("Yes"))
         doubleClick = true;
      else
         doubleClick = false;

   }

   /**
    * This is here for keybindings using the swing input map - the preferred
    *    way to use the keyboard.
    *
    * Unfortunantely I could not get this working correctly on linux so have
    *    abandoned it for now.  Also under different JVM's was having different
    *    sparatic results like the bindings not firing at all.
    */
   private void initKeyBindings() {

      KeyStroke ks;

      Action newSession = new AbstractAction(MNEMONIC_OPEN_NEW) {
            public void actionPerformed(ActionEvent e) {
               me.startNewSession();
            }
        };

      if (!keyMap.isKeyStrokeDefined(MNEMONIC_OPEN_NEW)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_N,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_OPEN_NEW);
      }

      getInputMap().put(ks,MNEMONIC_OPEN_NEW);
      getActionMap().put(MNEMONIC_OPEN_NEW,newSession );

      Action chgSession = new AbstractAction(MNEMONIC_TOGGLE_CONNECTION) {
            public void actionPerformed(ActionEvent e) {
               changeConnection();
            }
        };

      if (!keyMap.isKeyStrokeDefined(MNEMONIC_TOGGLE_CONNECTION)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_X,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_TOGGLE_CONNECTION);
      }

      getInputMap().put(ks,MNEMONIC_TOGGLE_CONNECTION);
      getActionMap().put(MNEMONIC_TOGGLE_CONNECTION,chgSession );

      Action nxtSession = new AbstractAction(MNEMONIC_JUMP_NEXT) {
            public void actionPerformed(ActionEvent e) {
               nextSession();
            }
        };

      if (!keyMap.isKeyStrokeDefined(MNEMONIC_JUMP_NEXT)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_JUMP_NEXT);
      }

      getInputMap().put(ks,MNEMONIC_JUMP_NEXT);
      getActionMap().put(MNEMONIC_JUMP_NEXT,nxtSession );

      Action prevSession = new AbstractAction(MNEMONIC_JUMP_PREV) {
            public void actionPerformed(ActionEvent e) {
               prevSession();
            }
        };
      if (!keyMap.isKeyStrokeDefined(MNEMONIC_JUMP_PREV)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_JUMP_PREV);
      }
      getInputMap().put(ks,MNEMONIC_JUMP_PREV);
      getActionMap().put(MNEMONIC_JUMP_PREV,prevSession );

      Action hotSpots = new AbstractAction(MNEMONIC_HOTSPOTS) {
            public void actionPerformed(ActionEvent e) {
               screen.toggleHotSpots();
               System.out.println(MNEMONIC_HOTSPOTS);
            }
        };
      if (!keyMap.isKeyStrokeDefined(MNEMONIC_HOTSPOTS)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_HOTSPOTS);
      }

      getInputMap().put(ks,MNEMONIC_HOTSPOTS);
      getActionMap().put(MNEMONIC_HOTSPOTS,hotSpots );

      Action gui = new AbstractAction(MNEMONIC_GUI) {
            public void actionPerformed(ActionEvent e) {
               screen.toggleGUIInterface();
            }
        };

      if (!keyMap.isKeyStrokeDefined(MNEMONIC_GUI)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_G,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_GUI);
      }
      getInputMap().put(ks,MNEMONIC_GUI);
      getActionMap().put(MNEMONIC_GUI,gui );

      Action msg = new AbstractAction(MNEMONIC_DISP_MESSAGES) {
            public void actionPerformed(ActionEvent e) {
               vt.systemRequest('4');
            }
        };
      if (!keyMap.isKeyStrokeDefined(MNEMONIC_DISP_MESSAGES)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_M,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_DISP_MESSAGES);
      }
      getInputMap().put(ks,MNEMONIC_DISP_MESSAGES);
      getActionMap().put(MNEMONIC_DISP_MESSAGES,msg );

      Action attr = new AbstractAction(MNEMONIC_DISP_ATTRIBUTES) {
            public void actionPerformed(ActionEvent e) {
               doAttributes();

            }
        };
      if (!keyMap.isKeyStrokeDefined(MNEMONIC_DISP_ATTRIBUTES)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_D,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_DISP_ATTRIBUTES);
      }
      getInputMap().put(ks,MNEMONIC_DISP_ATTRIBUTES);
      getActionMap().put(MNEMONIC_DISP_ATTRIBUTES,attr );

      Action print = new AbstractAction(MNEMONIC_PRINT_SCREEN) {
            public void actionPerformed(ActionEvent e) {
               printMe();

            }
        };
      if (!keyMap.isKeyStrokeDefined(MNEMONIC_PRINT_SCREEN)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_P,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_PRINT_SCREEN);
      }
      getInputMap().put(ks,MNEMONIC_PRINT_SCREEN);
      getActionMap().put(MNEMONIC_PRINT_SCREEN,print );

      Action cursor = new AbstractAction(MNEMONIC_CURSOR) {
            public void actionPerformed(ActionEvent e) {
               screen.crossHair();
            }
        };
      if (!keyMap.isKeyStrokeDefined(MNEMONIC_CURSOR)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_L,KeyEvent.ALT_MASK,false);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_CURSOR);
      }
      getInputMap().put(ks,MNEMONIC_CURSOR);
      getActionMap().put(MNEMONIC_CURSOR,cursor );

      Action debug = new AbstractAction(MNEMONIC_DEBUG) {
            public void actionPerformed(ActionEvent e) {
               vt.toggleDebug();
            }
        };
      if (!keyMap.isKeyStrokeDefined(MNEMONIC_DEBUG)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_O,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_DEBUG);
      }
      getInputMap().put(ks,MNEMONIC_DEBUG);
      getActionMap().put(MNEMONIC_DEBUG,debug );

      Action close = new AbstractAction(MNEMONIC_CLOSE) {
            public void actionPerformed(ActionEvent e) {
               closeSession();
            }
        };
      if (!keyMap.isKeyStrokeDefined(MNEMONIC_CLOSE)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_Q,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_CLOSE);
      }
      getInputMap().put(ks,MNEMONIC_CLOSE);
      getActionMap().put(MNEMONIC_CLOSE,close );

      Action transfer = new AbstractAction(MNEMONIC_FILE_TRANSFER) {
            public void actionPerformed(ActionEvent e) {
               doMeTransfer();
            }
        };
      if (!keyMap.isKeyStrokeDefined(MNEMONIC_FILE_TRANSFER)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_T,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_FILE_TRANSFER);
      }
      getInputMap().put(ks,MNEMONIC_FILE_TRANSFER);
      getActionMap().put(MNEMONIC_FILE_TRANSFER,transfer );

      Action e_mail = new AbstractAction(MNEMONIC_E_MAIL) {
            public void actionPerformed(ActionEvent e) {
               sendScreenEMail();
            }
        };

      if (!keyMap.isKeyStrokeDefined(MNEMONIC_E_MAIL)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_E,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_E_MAIL);
      }
      getInputMap().put(ks,MNEMONIC_E_MAIL);
      getActionMap().put(MNEMONIC_E_MAIL,e_mail );

      Action runScript = new AbstractAction(MNEMONIC_RUN_SCRIPT) {
            public void actionPerformed(ActionEvent e) {
               runScript();
            }
        };

      if (!keyMap.isKeyStrokeDefined(MNEMONIC_RUN_SCRIPT)) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_R,KeyEvent.ALT_MASK);
      }
      else {
         ks = keyMap.getKeyStroke(MNEMONIC_RUN_SCRIPT);
      }
      getInputMap().put(ks,MNEMONIC_RUN_SCRIPT);
      getActionMap().put(MNEMONIC_RUN_SCRIPT,runScript );

      Action spclDump = new AbstractAction("special dump") {
            public void actionPerformed(ActionEvent e) {
               dumpStuff(new Throwable());
            }
        };

//      if (!keyMap.isKeyStrokeDefined("special dump")) {
         ks = KeyStroke.getKeyStroke(KeyEvent.VK_O,KeyEvent.CTRL_MASK | KeyEvent.SHIFT_MASK);
//      }
//      else {
//         ks = keyMap.getKeyStroke(MNEMONIC_RUN_SCRIPT);
//      }
      getInputMap().put(ks,"special dump");
      getActionMap().put("special dump",spclDump );

//
//         Action ohshit = new AbstractAction() {
//               public void actionPerformed(ActionEvent e) {
//                  System.out.println(e.getActionCommand());
//               }
//           };
//   //         HashMap hm = keyMap.getKeyMap();
//   //
//   //         Collection v = hm.values();
//   //         Set o = hm.keySet();
//   //         Iterator k = o.iterator();
//   //         Iterator i = v.iterator();
//   //
//   //         while (k.hasNext()) {
//   //            KeyStroker ksr = (KeyStroker)k.next();
//   //            String keyVal = (String)i.next();
//   //            Action ohshit = new AbstractAction(keyVal) {
//   //               public void actionPerformed(ActionEvent e) {
//   //                  System.out.println("action command :" + e);
//   //               }
//   //            };
//   //            int mask = 0;
//   //            if (ksr.isAltDown())
//   //               mask |= KeyEvent.ALT_MASK;
//   //            if (ksr.isControlDown())
//   //               mask |= KeyEvent.CTRL_MASK;
//   //            if (ksr.isShiftDown())
//   //               mask |= KeyEvent.SHIFT_MASK;
//   //            if (ksr.isAltGrDown())
//   //               mask |= KeyEvent.META_MASK;
//   //
//   //            ohshit.putValue(Action.NAME, keyVal);
//   //
//   //            ks = KeyStroke.getKeyStroke(ksr.getKeyCode(),mask);
//   //
//   //            System.out.println(keyVal + " " + ks);
//   //            getInputMap().put(ks,keyVal);
//   //            getActionMap().put(keyVal,new MyAction(keyVal) );
//   //
//   //   //         if (keyVal.equals(which)) {
//   //   //            mappedKeys.remove(ks);
//   //   //            mappedKeys.put(new KeyStroker(ke.getKeyCode(),
//   //   //                                          ke.isShiftDown(),
//   //   //                                          ke.isControlDown(),
//   //   //                                          ke.isAltDown(),
//   //   //                                          isAltGr),keyVal);
//   //   //            return;
//   //   //         }
//   //      }


   }

   private void dumpStuff(Throwable ex) {

      vt.dumpStuff();
      if (null == ex) {
         return;
      }
      ex.printStackTrace();
   }
   public void onKeyChanged() {

      getInputMap().clear();
      getActionMap().clear();
      initKeyBindings();

   }
   /**
    * MyAction is used so that I can attach a string command to the action
    *    I tried using just the default versions but it was not returning
    *    correctly or just showing up as null depending on the version of the
    *    JVM.  These are know and reported problems on the Bug Database.
    */
//   private class MyAction extends AbstractAction {
//
//      private String de;
//      public MyAction (String desc) {
//
//         super(desc);
//         de = desc;
//
//      }
//      public void actionPerformed (ActionEvent ae) {
//         System.out.println("MyAction : " + de);
//      }
//   }

    /*
     * We have to jump through some hoops to avoid
     * trying to print non-printing characters
     * such as Shift.  (Not only do they not print,
     * but if you put them in a String, the characters
     * afterward won't show up in the text area.)
     */
    protected void displayInfo(KeyEvent e, String s){
        String charString, keyCodeString, modString, tmpString,isString;

        char c = e.getKeyChar();
        int keyCode = e.getKeyCode();
        int modifiers = e.getModifiers();

        if (Character.isISOControl(c)) {
            charString = "key character = "
                       + "(an unprintable control character)";
        } else {
            charString = "key character = '"
                       + c + "'";
        }

        keyCodeString = "key code = " + keyCode
                        + " ("
                        + KeyEvent.getKeyText(keyCode)
                        + ")";
         if(keyCode == KeyEvent.VK_PREVIOUS_CANDIDATE) {

            keyCodeString += " previous candidate ";

         }

         if(keyCode == KeyEvent.VK_DEAD_ABOVEDOT ||
               keyCode == KeyEvent.VK_DEAD_ABOVERING ||
               keyCode == KeyEvent.VK_DEAD_ACUTE ||
               keyCode == KeyEvent.VK_DEAD_BREVE ||
               keyCode == KeyEvent.VK_DEAD_CIRCUMFLEX

            ) {

            keyCodeString += " dead key ";

         }

        modString = "modifiers = " + modifiers;
        tmpString = KeyEvent.getKeyModifiersText(modifiers);
        if (tmpString.length() > 0) {
            modString += " (" + tmpString + ")";
        } else {
            modString += " (no modifiers)";
        }

        isString = "isKeys = isActionKey (" + e.isActionKey() + ")" +
                         " isAltDown (" + e.isAltDown() + ")" +
                         " isAltGraphDown (" + e.isAltGraphDown() + ")" +
                         " isAltGraphDownLinux (" + isAltGr + ")" +
                         " isControlDown (" + e.isControlDown() + ")" +
                         " isMetaDown (" + e.isMetaDown() + ")" +
                         " isShiftDown (" + e.isShiftDown() + ")";


         String newline = "\n";
        System.out.println(s + newline
                           + "    " + charString + newline
                           + "    " + keyCodeString + newline
                           + "    " + modString + newline
                           + "    " + isString + newline);

    }

   protected boolean emulatorAction(KeyStroke ks, KeyEvent e){
//					int condition, boolean pressed) {
//	InputMap map = getInputMap(condition, false);
      InputMap map = getInputMap();
      ActionMap am = getActionMap();

      if(map != null && am != null && isEnabled()) {
         Object binding = map.get(ks);
         Action action = (binding == null) ? null : am.get(binding);
         if (action != null) {
            return true;
         }
      }
      return false;
   }

   private void processVTKeyPressed(KeyEvent e){

      keyProcessed = true;
//      displayInfo(e,"Pressed " + keyProcessed);
      int keyCode = e.getKeyCode();

      if (isLinux && keyCode == e.VK_ALT_GRAPH) {

         isAltGr = true;
      }

//      if (linux)
//      if (keyCode == e.VK_UNDEFINED ||
//      if (keyCode == e.VK_ALT) {
//         System.out.println(" cursor active " + screen.cursorActive);
//         e.consume();
//         return;
//      }

      if (      keyCode == e.VK_CAPS_LOCK ||
            keyCode == e.VK_SHIFT ||
            keyCode == e.VK_ALT ||
            keyCode == e.VK_ALT_GRAPH
         ) {
//         displayInfo(e,"Pressed ");

         return;
      }

//      displayInfo(e,"Pressed " + keyProcessed);

      KeyStroke ks = KeyStroke.getKeyStroke(e.getKeyCode(),e.getModifiers(),false);
      if (emulatorAction(ks,e)) {

         return;
      }

      if (isLinux)
         lastKeyStroke = keyMap.getKeyStrokeText(e,isAltGr);
      else
         lastKeyStroke = keyMap.getKeyStrokeText(e);

      //System.out.println("lastKeyStroke " + lastKeyStroke);

      if (lastKeyStroke != null && !lastKeyStroke.equals("null")) {

         if (lastKeyStroke.startsWith("[") || lastKeyStroke.length() == 1) {

            screen.sendKeys(lastKeyStroke);
            if (recording)
               recordBuffer.append(lastKeyStroke);
         }
         else {
            executeMeMacro(lastKeyStroke);
         }
         if (lastKeyStroke.equals("[markleft]") ||
               lastKeyStroke.equals("[markright]") ||
               lastKeyStroke.equals("[markup]") ||
               lastKeyStroke.equals("[markdown]")) {
            doKeyBoundArea(e,lastKeyStroke);
         }
      }
      else
         keyProcessed = false;

      if (keyProcessed)
         e.consume();


   }

   private void processVTKeyTyped(KeyEvent e){

      char kc = e.getKeyChar();
//      displayInfo(e,"Typed processed " + keyProcessed);
      // Hack to make german umlauts work under Linux
      // The problem is that these umlauts don't generate a keyPressed event
      // and so keyProcessed is true (even if is hasn't been processed)
      // so we check if it's a letter (with or without shift) and skip return
      if (isLinux) {

         if (!((Character.isLetter(kc))  && (e.getModifiers() == 0
            || e.getModifiers() == Event.SHIFT_MASK))) {

            if (Character.isISOControl(kc) || keyProcessed) {
               return;
            }
         }
      }
      else {
         if (Character.isISOControl(kc) || keyProcessed) {
            return;
         }
      }
//      displayInfo(e,"Typed processed " + keyProcessed);
      String s = "";
//      if (isLinux) {
//         lastKeyStroke = keyMap.getKeyStrokeText(e,isAltGr);
//         System.out.println("last " + lastKeyStroke);
//         if (lastKeyStroke != null) {
//            s = lastKeyStroke;
//            System.out.println("last " + s);
//         }
//         else
//            s +=kc;
//      }
//      else
         s += kc;
      if (!vt.isConnected()   )
         return;
      screen.sendKeys(s);
      if (recording)
         recordBuffer.append(s);
      keyProcessed = true;
      e.consume();
   }

   private void processVTKeyReleased(KeyEvent e){


      if (isLinux && e.getKeyCode() == e.VK_ALT_GRAPH) {

         isAltGr = false;
      }

      if (Character.isISOControl(e.getKeyChar()) || keyProcessed || e.isConsumed() )
         return;

//      displayInfo(e,"Released " + keyProcessed);

      String s = keyMap.getKeyStrokeText(e);

      if (s != null) {

         if (s.startsWith("[")) {
            screen.sendKeys(s);
            if (recording)
               recordBuffer.append(s);
         }
         else
            executeMeMacro(s);

      }
      else
         keyProcessed = false;

      if (keyProcessed)
         e.consume();
   }

   private void sendScreenEMail() {

      SendEMailDialog semd = new SendEMailDialog(me.getParentView((Session)this),screen);
   }

   private void sendMeToFile() {
      new SendScreenToFile(screen);

   }

   private void doKeyBoundArea(KeyEvent ke,String last) {

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

   private void closeSession() {

      vt.isOnSignoffScreen();

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
         closeMe();
      }
      if (result == 1) {
         me.closingDown((Session)this);
      }

   }

   private void getFocusForMe() {
      this.requestFocus();
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

   protected final String getStringProperty(String prop) {

      if (defaultProps.containsKey(prop))
         return (String)defaultProps.get(prop);
      else
         return "";

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

   public void propertyChange(PropertyChangeEvent pce) {

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

      resizeMe();
      repaint();

   }

   public void onSessionChanged(SessionChangeEvent changeEvent) {

      switch (changeEvent.getState()) {
         case STATE_CONNECTED:

            String mac = getStringProperty("connectMacro");
            if (mac.length() > 0)
               executeMeMacro(mac);
            break;
      }
   }

   protected void setVT(tnvt v) {

      vt = v;
      screen.setVT(vt);

   }

   public void sendAidKey(int whichOne) {

      vt.sendAidKey(whichOne);
   }

   protected void changeConnection() {

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

   protected void nextSession() {

      fireSessionJump(JUMP_NEXT);

   }

   protected void prevSession() {

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

      if (defaultProps.containsKey("saveme")) {

         defaultProps.remove("saveme");

         Object[] args = {propFileName};
         String message = MessageFormat.format(
                           LangTool.getString("messages.saveSettings"),
                           args);

         int result = JOptionPane.showConfirmDialog(getParent(),message);

         if (result == JOptionPane.OK_OPTION) {
            try {
               FileOutputStream out = new FileOutputStream(propFileName);
                  // save off the width and height to be restored later
               defaultProps.store(out,"------ Defaults --------");
            }
            catch (FileNotFoundException fnfe) {}
            catch (IOException ioe) {}
         }


      }
      vt.disconnect();

   }

   private void loadProps() {

      defaultProps = new Properties();
      if (propFileName == null || propFileName == "")
         propFileName = "TN5250JDefaults.props";

      try {
         FileInputStream in = new FileInputStream(propFileName);
         //InputStream in = getClass().getClassLoader().getResourceAsStream(propFileName);
         defaultProps.load(in);

      }
      catch (IOException ioe) {
         System.out.println("Information Message: Properties file is being "
                              + "created for first time use:  File name "
                              + propFileName);
      }
      catch (SecurityException se) {
         System.out.println(se.getMessage());
      }

   }

   private void doAttributes() {

      SessionAttributes sa = null;

      // if me is null then we must be called from an applet so we create
      //   a new frame object.
      if (me == null)
         sa = new SessionAttributes(propFileName,
                                       defaultProps,
                                       new JFrame());
      else
         sa = new SessionAttributes(propFileName,
                                       defaultProps,
                                       (Frame)me.getParentView((Session)this));
      sa.addPropertyChangeListener(screen);
      sa.addPropertyChangeListener(this);
      sa.showIt();
      defaultProps = sa.getProperties();
      sa.removePropertyChangeListener(screen);
      sa.removePropertyChangeListener(this);
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

            if (recording) {
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

         action = new AbstractAction(LangTool.getString("popup.xtfrFile")) {
               public void actionPerformed(ActionEvent e) {
                  doMeTransfer();
                  getFocusForMe();
               }
           };
         popup.add(createMenuItem(action,MNEMONIC_FILE_TRANSFER));

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

      popup.show(me.getComponent(),
               me.getX(),me.getY());

   }

   private void addMacros(JMenu menu) {

      LoadMacroMenu.loadMacros((Session)this,macros,menu);
   }

   private JMenuItem createMenuItem(Action action, String accelKey) {
      JMenuItem mi;

      mi =new JMenuItem();
      mi.setAction(action);
      if (keyMap.isKeyStrokeDefined(accelKey))
         mi.setAccelerator(keyMap.getKeyStroke(accelKey));
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

   private void doMeTransfer() {

      XTFRFile xtrf = new XTFRFile(me.getParentView((Session)this),vt);
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
      keyMap.removeKeyChangeListener(this);
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
      recording = false;
      if (recordBuffer.length() > 0) {
         macros.setMacro(newMacName,recordBuffer.toString());
         System.out.println(recordBuffer);
      }
      recordBuffer = null;
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
            recording = true;
            recordBuffer = new StringBuffer();
         }
      }
   }

   private void runScript () {

         JPanel rsp = new JPanel();
         rsp.setLayout(new BorderLayout());
         JLabel jl = new JLabel("Enter script to run");
         final JTextField rst = new JTextField();
         rsp.add(jl,BorderLayout.NORTH);
         rsp.add(rst,BorderLayout.CENTER);
         Object[]      message = new Object[1];
         message[0] = rsp;
         String[] options = {"Run","Cancel"};

         final JOptionPane pane = new JOptionPane(

                message,                           // the dialog message array
                JOptionPane.QUESTION_MESSAGE,      // message type
                JOptionPane.DEFAULT_OPTION,        // option type
                null,                              // optional icon, use null to use the default icon
                options,                           // options string array, will be made into buttons//
                options[0]);                       // option that should be made into a default button


         // create a dialog wrapping the pane
         final JDialog dialog = pane.createDialog(null, // parent frame
                           "Run Script"  // dialog title
                           );

         // add the listener that will set the focus to
         // the desired option
         dialog.addWindowListener( new WindowAdapter() {
            public void windowOpened( WindowEvent e) {
               super.windowOpened( e );

               // now we're setting the focus to the desired component
               // it's not the best solution as it depends on internals
               // of the OptionPane class, but you can use it temporarily
               // until the bug gets fixed
               // also you might want to iterate here thru the set of
               // the buttons and pick one to call requestFocus() for it

               rst.requestFocus();
            }
         });
         dialog.show();

         // now we can process the value selected
         String value = (String)pane.getValue();

         if (value.equals(options[0])) {
            // send option along with system request
            if (rst.getText().length() > 0) {
               macros.invoke(rst.getText(),(Session)this);
            }
         }
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
         char ac = vt.getASCIIChar(x);
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

   private void printMe() {

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
//         System.out.println("shape");
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
