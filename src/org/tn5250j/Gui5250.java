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
import javax.swing.event.*;
import javax.swing.text.*;
import java.io.*;
import java.util.*;
import java.text.*;
import java.beans.*;
import org.tn5250j.tools.*;
import org.tn5250j.mailtools.*;

public class Gui5250 extends JPanel implements ComponentListener,
                                                      ActionListener,
                                                      PropertyChangeListener ,
                                                      RubberBandCanvasIF {

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
      s.setDoubleBuffered(true);

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

               if (e.getClickCount() == 2) {

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

      /**
       * this is taken out right now look at the method for description
       */
//      initKeyBindings();

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

//      if (keyCode == KeyEvent.VK_N &&
//            e.isAltDown() && !e.isControlDown()) {
//
//         me.startNewSession();
//         return;
//      }
      Action newSession = new AbstractAction("newSession") {
            public void actionPerformed(ActionEvent e) {
               me.startNewSession();
            }
        };

      ks = KeyStroke.getKeyStroke(KeyEvent.VK_N,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"newSession");
      getActionMap().put("newSession",newSession );

//      if (keyCode == KeyEvent.VK_X &&
//            e.isAltDown() && !e.isControlDown()) {
//         changeConnection();
//         return;
//      }

      Action chgSession = new AbstractAction("chgSession") {
            public void actionPerformed(ActionEvent e) {
               changeConnection();
            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_X,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"chgSession");
      getActionMap().put("chgSession",chgSession );

//      if (keyCode == KeyEvent.VK_PAGE_UP &&
//            e.isAltDown() && !e.isControlDown()) {
//
//         nextSession();
//         return;
//      }

      Action nxtSession = new AbstractAction("nxtSession") {
            public void actionPerformed(ActionEvent e) {
               nextSession();
            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"nxtSession");
      getActionMap().put("nxtSession",nxtSession );

//      if (keyCode == KeyEvent.VK_PAGE_DOWN &&
//            e.isAltDown() && !e.isControlDown()) {
//
//         prevSession();
//         return;
//      }

      Action prevSession = new AbstractAction("prevSession") {
            public void actionPerformed(ActionEvent e) {
               prevSession();
            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"prevSession");
      getActionMap().put("prevSession",prevSession );

//      if (keyCode == KeyEvent.VK_S &&
//            e.isAltDown() && !e.isControlDown()) {
//
//         screen.toggleHotSpots();
//         return;
//      }

      Action hotSpots = new AbstractAction("hotSpots") {
            public void actionPerformed(ActionEvent e) {
               screen.toggleHotSpots();
               System.out.println("hotspots");
            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_S,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"hotSpots");
      getActionMap().put("hotSpots",hotSpots );

//      if (keyCode == KeyEvent.VK_G &&
//            e.isAltDown() && !e.isControlDown()) {
//
//         screen.toggleGUIInterface();
////         repaint();
//         return;
//      }
      Action gui = new AbstractAction("gui") {
            public void actionPerformed(ActionEvent e) {
               screen.toggleGUIInterface();
            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_G,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"gui");
      getActionMap().put("gui",gui );

//      if (keyCode == KeyEvent.VK_M &&
//            e.isAltDown() && !e.isControlDown()) {
//         vt.systemRequest('4');
//         return;
//      }

      Action msg = new AbstractAction("msg") {
            public void actionPerformed(ActionEvent e) {
               vt.systemRequest('4');
            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_M,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"msg");
      getActionMap().put("msg",msg );

//      if (keyCode == KeyEvent.VK_D &&
//            e.isAltDown() && !e.isControlDown()) {
//         doAttributes();
//         return;
//      }

      Action attr = new AbstractAction("attr") {
            public void actionPerformed(ActionEvent e) {
               doAttributes();

            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_D,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"attr");
      getActionMap().put("attr",attr );

//      if (keyCode == KeyEvent.VK_P &&
//            e.isAltDown() && !e.isControlDown()) {
//         screen.printMe();
//         return;
//      }

      Action print = new AbstractAction("print") {
            public void actionPerformed(ActionEvent e) {
               printMe();

            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_P,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"print");
      getActionMap().put("print",print );

//      if (keyCode == KeyEvent.VK_L &&
//            e.isAltDown() && !e.isControlDown()) {
//         screen.crossHair();
//         return;
//      }
      Action cursor = new AbstractAction("cursor") {
            public void actionPerformed(ActionEvent e) {
               screen.crossHair();
            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_L,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"cursor");
      getActionMap().put("cursor",cursor );
//
//      if (keyCode == KeyEvent.VK_O &&
//            e.isAltDown() && !e.isControlDown()) {
//         vt.toggleDebug();
//         return;
//      }
      Action debug = new AbstractAction("debug") {
            public void actionPerformed(ActionEvent e) {
               vt.toggleDebug();
            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_O,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"debug");
      getActionMap().put("debug",debug );
//
//      if (keyCode == KeyEvent.VK_Q &&
//            e.isAltDown() && !e.isControlDown()) {
//         closeSession();
//         return;
//      }
      Action close = new AbstractAction("close") {
            public void actionPerformed(ActionEvent e) {
               closeSession();
            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_Q,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"close");
      getActionMap().put("close",close );
//
//      // file transfer
//      if (keyCode == KeyEvent.VK_T &&
//            e.isAltDown() && !e.isControlDown()) {
//         doMeTransfer();
//         return;
//      }
      Action transfer = new AbstractAction("transfer") {
            public void actionPerformed(ActionEvent e) {
               doMeTransfer();
            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_T,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"transfer");
      getActionMap().put("transfer",transfer );
//
//      // send screen via e-mail
//         if (keyCode == KeyEvent.VK_E &&
//               e.isAltDown() && !e.isControlDown()) {
//            sendScreenEMail();
//            return;
//         }
//
      Action e_mail = new AbstractAction("e-mail") {
            public void actionPerformed(ActionEvent e) {
               sendScreenEMail();
            }
        };
      ks = KeyStroke.getKeyStroke(KeyEvent.VK_E,KeyEvent.ALT_MASK);
      getInputMap().put(ks,"e-mail");
      getActionMap().put("e-mail",e_mail );

      Action ohshit = new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
               System.out.println(e.getActionCommand());
            }
        };
//         HashMap hm = keyMap.getKeyMap();
//
//         Collection v = hm.values();
//         Set o = hm.keySet();
//         Iterator k = o.iterator();
//         Iterator i = v.iterator();
//
//         while (k.hasNext()) {
//            KeyStroker ksr = (KeyStroker)k.next();
//            String keyVal = (String)i.next();
//            Action ohshit = new AbstractAction(keyVal) {
//               public void actionPerformed(ActionEvent e) {
//                  System.out.println("action command :" + e);
//               }
//            };
//            int mask = 0;
//            if (ksr.isAltDown())
//               mask |= KeyEvent.ALT_MASK;
//            if (ksr.isControlDown())
//               mask |= KeyEvent.CTRL_MASK;
//            if (ksr.isShiftDown())
//               mask |= KeyEvent.SHIFT_MASK;
//            if (ksr.isAltGrDown())
//               mask |= KeyEvent.META_MASK;
//
//            ohshit.putValue(Action.NAME, keyVal);
//
//            ks = KeyStroke.getKeyStroke(ksr.getKeyCode(),mask);
//
//            System.out.println(keyVal + " " + ks);
//            getInputMap().put(ks,keyVal);
//            getActionMap().put(keyVal,new MyAction(keyVal) );
//
//   //         if (keyVal.equals(which)) {
//   //            mappedKeys.remove(ks);
//   //            mappedKeys.put(new KeyStroker(ke.getKeyCode(),
//   //                                          ke.isShiftDown(),
//   //                                          ke.isControlDown(),
//   //                                          ke.isAltDown(),
//   //                                          isAltGr),keyVal);
//   //            return;
//   //         }
//      }


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

   private void processVTKeyPressed(KeyEvent e){

      keyProcessed = true;
//      displayInfo(e,"Pressed " + keyProcessed);
      int keyCode = e.getKeyCode();

      if (isLinux && keyCode == e.VK_ALT_GRAPH) {

         isAltGr = true;
      }

//      if (linux)
//      if (keyCode == e.VK_UNDEFINED ||
      if (      keyCode == e.VK_CAPS_LOCK ||
            keyCode == e.VK_SHIFT ||
            keyCode == e.VK_ALT ||
            keyCode == e.VK_ALT_GRAPH
         ) {
//         displayInfo(e,"Pressed ");

         return;
      }
//      displayInfo(e,"Pressed " + keyProcessed);

      if (keyCode == KeyEvent.VK_N &&
            e.isAltDown() && !e.isControlDown()) {

         me.startNewSession();
         return;
      }

      if (keyCode == KeyEvent.VK_X &&
            e.isAltDown() && !e.isControlDown()) {
         changeConnection();
         return;
      }

      if (keyCode == KeyEvent.VK_PAGE_UP &&
            e.isAltDown() && !e.isControlDown()) {

         nextSession();
         return;
      }

      if (keyCode == KeyEvent.VK_PAGE_DOWN &&
            e.isAltDown() && !e.isControlDown()) {

         prevSession();
         return;
      }

      if (keyCode == KeyEvent.VK_S &&
            e.isAltDown() && !e.isControlDown()) {

         screen.toggleHotSpots();
         return;
      }

      if (keyCode == KeyEvent.VK_G &&
            e.isAltDown() && !e.isControlDown()) {

         screen.toggleGUIInterface();
//         repaint();
         return;
      }

      if (keyCode == KeyEvent.VK_M &&
            e.isAltDown() && !e.isControlDown()) {
         vt.systemRequest('4');
         return;
      }

      if (keyCode == KeyEvent.VK_D &&
            e.isAltDown() && !e.isControlDown()) {
         doAttributes();
         return;
      }

      if (keyCode == KeyEvent.VK_P &&
            e.isAltDown() && !e.isControlDown()) {
         screen.printMe();
         return;
      }

      if (keyCode == KeyEvent.VK_L &&
            e.isAltDown() && !e.isControlDown()) {
         screen.crossHair();
         return;
      }

      if (keyCode == KeyEvent.VK_O &&
            e.isAltDown() && !e.isControlDown()) {
         vt.toggleDebug();
         return;
      }

      if (keyCode == KeyEvent.VK_Q &&
            e.isAltDown() && !e.isControlDown()) {
         closeSession();
         return;
      }

      // file transfer
      if (keyCode == KeyEvent.VK_T &&
            e.isAltDown() && !e.isControlDown()) {
         doMeTransfer();
         return;
      }

      // send screen via e-mail
      if (keyCode == KeyEvent.VK_E &&
            e.isAltDown() && !e.isControlDown()) {
         sendScreenEMail();
         return;
      }


//      if (!keyMap.isEqualLast(e))
      if (isLinux)
         lastKeyStroke = keyMap.getKeyStrokeText(e,isAltGr);
      else
         lastKeyStroke = keyMap.getKeyStrokeText(e);
//      System.out.println("lastKeyStroke " + lastKeyStroke);

      if (lastKeyStroke != null) {

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
      if (Character.isISOControl(kc) || keyProcessed)
//      if (keyProcessed)
         return;
//         displayInfo(e,"Typed ");
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
//      displayInfo(e,"Released " + keyProcessed);

      if (keyProcessed || e.isConsumed())
         return;


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

         JPanel semp = new JPanel();
         semp.setLayout(new AlignLayout(2,5,5));
         JLabel tol = new JLabel(LangTool.getString("em.to"));
         JTextField tot = new JTextField(30);
         JLabel subl = new JLabel(LangTool.getString("em.subject"));
         JTextField subt = new JTextField(30);
         JLabel fnl = new JLabel(LangTool.getString("em.fileName"));
         JTextField fnt = new JTextField("tn5250j.txt",30);
         semp.add(tol);
         semp.add(tot);
         semp.add(subl);
         semp.add(subt);
         semp.add(fnl);
         semp.add(fnt);

         Object[]      message = new Object[1];
         message[0] = semp;
         String[] options = {LangTool.getString("em.optSendLabel"),
                              LangTool.getString("em.optCancelLabel"),
                              LangTool.getString("em.optConfigureLabel")};

         int result = 0;
            result = JOptionPane.showOptionDialog(
                me.frame,                              // the parent that the dialog blocks
                message,                           // the dialog message array
                LangTool.getString("em.title"),    // the title of the dialog window
                JOptionPane.DEFAULT_OPTION,        // option type
                JOptionPane.QUESTION_MESSAGE,      // message type
                null,                              // optional icon, use null to use the default icon
                options,                           // options string array, will be made into buttons//
                options[0]                         // option that should be made into a default button
            );

            switch(result) {
               case 0: // Send it
                  SendEMail sem = new SendEMail();
                  sem.setConfigFile("SMTPProperties.cfg");
                  sem.setTo(tot.getText());
                  sem.setSubject(subt.getText());
                  if (fnt.getText().length() > 0)
                     sem.setFileName(fnt.getText());

                  StringBuffer sb = new StringBuffer();
                  char[] s = screen.getScreenAsChars();
                  int c = screen.getCols();
                  int l = screen.getRows() * c;
                  int col = 0;
                  for (int x = 0; x < l; x++,col++) {
                     sb.append(s[x]);
                     if (col == c) {
                        sb.append('\n');
                        col = 0;
                     }
                  }


                  sem.setMessage(sb.toString());
                  try {
                     sem.send();
                  }
                  catch (IOException ioe) {
                     System.out.println(ioe.getMessage());
                  }
                  sem.release();
                  sem = null;
                  System.out.println("Message sent");

                  break;
               case 1: // Cancel
      //		      System.out.println("Cancel");
                  break;
               default:
                  break;
            }

   }

   private void doKeyBoundArea(KeyEvent ke,String last) {

      Point p = new Point();

      if (!rubberband.isAreaSelected()) {
         if (last.equals("[markleft]"))
            screen.getPointFromRowCol(screen.getCurrentRow(),
                                    screen.getCurrentCol()+1,
                                    p);
         if (last.equals("[markright]"))
            screen.getPointFromRowCol(screen.getCurrentRow(),
                                    screen.getCurrentCol()-1,
                                    p);
         if (last.equals("[markup]"))
            screen.getPointFromRowCol(screen.getCurrentRow()+1,
                                    screen.getCurrentCol(),
                                    p);
         if (last.equals("[markdown]"))
            screen.getPointFromRowCol(screen.getCurrentRow()-1,
                                    screen.getCurrentCol(),
                                    p);
         MouseEvent me = new MouseEvent(this,
                              MouseEvent.MOUSE_PRESSED,
                              System.currentTimeMillis(),
                              MouseEvent.BUTTON1_MASK,
                              p.x,p.y,
                              1,false);
         dispatchEvent(me);

      }

      screen.getPointFromRowCol(screen.getCurrentRow(),
                                 screen.getCurrentCol(),
                                 p);
      rubberband.getCanvas().translateEnd(p);
      MouseEvent me = new MouseEvent(this,
                           MouseEvent.MOUSE_DRAGGED,
                           System.currentTimeMillis(),
                           MouseEvent.BUTTON1_MASK,
                           p.x,p.y,
                           1,false);
      dispatchEvent(me);
//      Rectangle workR = new Rectangle();
//      rubberband.getBoundingArea(workR);
//      System.out.println(" area "  + rubberband.getEndPoint() + " " + workR +
//                           " from " );

   }

   private void closeSession() {

      Object[]      message = new Object[1];
      message[0] = LangTool.getString("cs.message");

      String[] options = {LangTool.getString("cs.optThis"),
                           LangTool.getString("cs.optAll"),
                           LangTool.getString("cs.optCancel")};

      int result = JOptionPane.showOptionDialog(
             me.frame,                              // the parent that the dialog blocks
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
         me.closingDown();
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

      resizeMe();
      repaint();

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

      me.nextSession();

   }

   protected void prevSession() {

      me.prevSession();

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

         int result = JOptionPane.showConfirmDialog(this,message);

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

         defaultProps.load(in);

      }
      catch (IOException ioe) {System.out.println(ioe.getMessage());}
      catch (SecurityException se) {
         System.out.println(se.getMessage());
      }

   }

   private void doAttributes() {

      SessionAttributes sa = new SessionAttributes(propFileName,
                                       defaultProps,
                                       me.frame);
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


      final int pos = screen.getRowColFromPoint(me.getX(),me.getY()) -
                     (screen.getCols()-1);

      if (!rubberband.isAreaSelected() && screen.isInField(pos,false) ) {
         action = new AbstractAction(LangTool.getString("popup.copy")) {
               public void actionPerformed(ActionEvent e) {
                  screen.copyField(pos);
                  getFocusForMe();
               }
           };
         popup.add(action);
         action = new AbstractAction(LangTool.getString("popup.paste")) {
               public void actionPerformed(ActionEvent e) {
                  screen.pasteMe(false);
                  getFocusForMe();
               }
           };
         popup.add(action);

         action = new AbstractAction(LangTool.getString("popup.pasteSpecial")) {
               public void actionPerformed(ActionEvent e) {
                  screen.pasteMe(true);
                  getFocusForMe();
               }
           };
         popup.add(action);

      }
      else {

         action = new AbstractAction(LangTool.getString("popup.copy")) {
               public void actionPerformed(ActionEvent e) {
                  screen.copyMe();
                  getFocusForMe();
               }
           };
         popup.add(action);

         action = new AbstractAction(LangTool.getString("popup.paste")) {
               public void actionPerformed(ActionEvent e) {
                  screen.pasteMe(false);
                  getFocusForMe();
               }
           };
         popup.add(action);

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
            int ePos = screen.getRowColFromPoint(workR.width ,
                                       workR.height );

            popup.addSeparator();

            menuItem = new JMenuItem(LangTool.getString("popup.selectedColumns")
                              + " " + screen.getCol(ePos));
            menuItem.setArmed(false);
            popup.add(menuItem);

            menuItem = new JMenuItem(LangTool.getString("popup.selectedRows")
                              + " " + screen.getRow(ePos));
            menuItem.setArmed(false);
            popup.add(menuItem);

//            JMenu sumMenu = new JMenu(LangTool.getString("popup.macros"));
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
         popup.add(action);

         popup.addSeparator();

         action = new AbstractAction(LangTool.getString("popup.systemRequest")) {
               public void actionPerformed(ActionEvent e) {
                  vt.systemRequest();
               }
           };
         popup.add(action);

         action = new AbstractAction(LangTool.getString("popup.help")) {
               public void actionPerformed(ActionEvent e) {
                  vt.sendHelpRequest();
               }
           };
         popup.add(action);

         action = new AbstractAction(LangTool.getString("popup.hostPrint")) {
               public void actionPerformed(ActionEvent e) {
                  vt.hostPrint(1);
               }
           };
         popup.add(action);

         if (screen.isMessageWait()) {
            action = new AbstractAction(LangTool.getString("popup.displayMessages")) {
                  public void actionPerformed(ActionEvent e) {
                     vt.systemRequest('4');
                  }
              };
            popup.add(action);
         }

         popup.addSeparator();

         action = new AbstractAction(LangTool.getString("popup.hexMap")) {
               public void actionPerformed(ActionEvent e) {
                  showHexMap();
               }
           };
         popup.add(action);

         action = new AbstractAction(LangTool.getString("popup.mapKeys")) {
               public void actionPerformed(ActionEvent e) {

                  mapMeKeys();
               }
           };
         popup.add(action);

         action = new AbstractAction(LangTool.getString("popup.settings")) {
               public void actionPerformed(ActionEvent e) {
                  doAttributes();
               }
           };
         popup.add(action);


         popup.addSeparator();

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
                  }
            };
         }
         macMenu.add(action);
         if (macros.isMacrosExist()) {

            macMenu.addSeparator();

            String[] macrosList = macros.getMacroList();
            for (int x = 0; x < macrosList.length; x++) {
               action = new AbstractAction(macrosList[x]) {
                     public void actionPerformed(ActionEvent e) {
                        executeMeMacro(e);
                     }
                 };
               macMenu.add(action);

            }
         }

         popup.add(macMenu);

         popup.addSeparator();

         action = new AbstractAction(LangTool.getString("popup.xtfrFile")) {
               public void actionPerformed(ActionEvent e) {
                  doMeTransfer();
               }
           };
         popup.add(action);

         action = new AbstractAction(LangTool.getString("popup.send")) {
               public void actionPerformed(ActionEvent e) {
                  sendScreenEMail();
               }
           };
         popup.add(action);

         popup.addSeparator();

         if (vt.isConnected()) {
            action = new AbstractAction(LangTool.getString("popup.disconnect")) {
                  public void actionPerformed(ActionEvent e) {
                     vt.disconnect();
                     getFocusForMe();
                  }
              };
         }
         else {

            action = new AbstractAction(LangTool.getString("popup.connect")) {
                  public void actionPerformed(ActionEvent e) {
                     vt.connect();
                     getFocusForMe();
                  }
              };


         }
         popup.add(action);
      }

      action = new AbstractAction(LangTool.getString("popup.close")) {
            public void actionPerformed(ActionEvent e) {
               closeSession();
            }
        };

      popup.add(action);

      popup.show(me.getComponent(),
               me.getX(),me.getY());

   }


   private void doMeTransfer() {

      XTFRFile xtrf = new XTFRFile(me.frame,vt);
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

   }

   private void executeMeMacro(ActionEvent ae) {

      executeMeMacro(ae.getActionCommand());
//      String keys = macros.getMacroByName(ae.getActionCommand());
//      if (keys != null)
//         screen.sendKeys(keys);

   }

   private void executeMeMacro(String macro) {

      String keys = macros.getMacroByName(macro);
      if (keys != null)
         screen.sendKeys(keys);

   }

   private void mapMeKeys() {
      KeyConfigure kc;

      if (macros.isMacrosExist()) {
         String[] macrosList = macros.getMacroList();
         kc = new KeyConfigure(me.frame,macrosList,vt.getCodePage());
      }
      else
         kc = new KeyConfigure(me.frame,null,vt.getCodePage());

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
      result = JOptionPane.showOptionDialog(
          null,                               // the parent that the dialog blocks
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


      repaint(x1,y1,x2+1,y2+1);
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
