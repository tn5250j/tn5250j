package org.tn5250j.tools;
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
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.text.*;
import org.tn5250j.tools.CodePage;
import org.tn5250j.*;

public class KeyConfigure extends JDialog implements ActionListener {

   Properties props;
   JPanel keyPanel = new JPanel();
   JPanel options = new JPanel();
   JTextArea strokeDesc = new JTextArea();
   JList functions;
   KeyMapper mapper;
   JFrame jf = null;
   JDialog dialog;
   boolean mods;
   private String[] macrosList;
   DefaultListModel lm = new DefaultListModel();
   private boolean macros;
   private boolean special;
   private CodePage codePage;
   private boolean isLinux;
   private boolean isAltGr;

   private static final String keyMnemonic[] = {
        "[backspace]", "[backtab]", "[up]", "[down]", "[left]",
        "[right]", "[delete]", "[tab]", "[eof]", "[eraseeof]",
        "[erasefld]", "[insert]", "[home]", "[keypad0]", "[keypad1]",
        "[keypad2]", "[keypad3]", "[keypad4]", "[keypad5]", "[keypad6]",
        "[keypad7]", "[keypad8]", "[keypad9]", "[keypad.]", "[keypad,]",
        "[keypad-]", "[fldext]", "[field+]", "[field-]", "[bof]",
        "[enter]","[pf1]","[pf2]","[pf3]","[pf4]",
        "[pf5]","[pf6]","[pf7]","[pf8]","[pf9]",
        "[pf10]","[pf11]","[pf12]","[pf13]","[pf14]",
        "[pf15]","[pf16]","[pf17]","[pf18]","[pf19]",
        "[pf20]","[pf21]","[pf22]","[pf23]","[pf24]",
        "[clear]", "[help]", "[pgup]", "[pgdown]", "[rollleft]",
        "[rollright]", "[hostprint]", "[pa1]", "[pa2]", "[pa3]",
        "[sysreq]","[reset]","[nextword]", "[prevword]", "[copy]",
        "[paste]","[attn]","[markup]", "[markdown]", "[markleft]",
        "[markright]"
   };

   public KeyConfigure(Frame parent, String[] macros, CodePage cp) {

      super(parent);

      codePage = cp;
      macrosList = macros;
//      String os = System.getProperty("os.name");
      if (System.getProperty("os.name").toLowerCase().indexOf("linux") != -1) {
//         System.out.println("using os " + os);
         isLinux = true;
      }

      try {
         jbInit();
         pack();
      }
      catch(Exception ex) {
         ex.printStackTrace();
      }
   }

   void jbInit() throws Exception {

      // create some reusable borders and layouts
      BorderLayout borderLayout = new BorderLayout();

      mapper = new KeyMapper();
      mapper.init();

      functions = new JList(lm);

      // add list selection listener to our functions list so that we
      //   can display the mapped key(s) to the function when a new
      //   function is selected.
      functions.addListSelectionListener(new ListSelectionListener() {
         public void valueChanged(ListSelectionEvent lse) {
            if (!lse.getValueIsAdjusting()) {
               setKeyDescription(functions.getSelectedIndex());
            }
         }
      });

      loadList(LangTool.getString("key.labelKeys"));

      functions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      JScrollPane functionsScroll = new JScrollPane(functions);

      JPanel fp = new JPanel();

      JComboBox whichKeys = new JComboBox();
      whichKeys.addItem(LangTool.getString("key.labelKeys"));
      whichKeys.addItem(LangTool.getString("key.labelMacros"));
      whichKeys.addItem(LangTool.getString("key.labelSpecial"));

      whichKeys.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {

                JComboBox cb = (JComboBox)e.getSource();
                loadList((String)cb.getSelectedItem());
            }
        });

      fp.setBorder(BorderFactory.createTitledBorder(
                                    LangTool.getString("key.labelDesc")));
      fp.setLayout(new BoxLayout(fp,BoxLayout.Y_AXIS));

      fp.add(whichKeys);
      fp.add(functionsScroll);

      strokeDesc.setColumns(30);
      strokeDesc.setBackground(functions.getBackground());
      strokeDesc.setEditable(false);

      JPanel dp = new JPanel();
      dp.setBorder(BorderFactory.createTitledBorder(
                                    LangTool.getString("key.labelMapTo")));

      dp.add(strokeDesc);


      keyPanel.setLayout(borderLayout);
      keyPanel.add(fp,BorderLayout.WEST);
      keyPanel.add(dp,BorderLayout.CENTER);

      // add the panels to our dialog
      getContentPane().add(keyPanel,BorderLayout.CENTER);
      getContentPane().add(options, BorderLayout.SOUTH);


//      functions.setSelectedIndex(0);

      // add option buttons to options panel
      addOptButton(LangTool.getString("key.labelMap","Map Key"),"MAP",options,true);
      addOptButton(LangTool.getString("key.labelRemove","Remove"),"REMOVE",options,true);
      addOptButton(LangTool.getString("key.labelDone","Done"),"DONE",options,true);

      this.setModal(true);
      this.setTitle(LangTool.getString("key.title"));

      // pack it and center it on the screen
      pack();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = getSize();
      if (frameSize.height > screenSize.height)
         frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
         frameSize.width = screenSize.width;
      setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);

      // now show the world what we can do
      show();

   }

   private void setKeyDescription(int index) {

      // This try and catch is to fix a problem in JDK1.4-betas
      try {
         if (!macros && !special) {
            strokeDesc.setText(mapper.getKeyStrokeDesc(keyMnemonic[index]));
         }
         else {
            if (macros) {
               System.out.println((String)lm.getElementAt(index));
               strokeDesc.setText(mapper.getKeyStrokeDesc((String)lm.getElementAt(index)));
            }
            if (special) {
               System.out.println((String)lm.getElementAt(index));
               String k = "";
               k += ((String)lm.getElementAt(index)).charAt(7);
               strokeDesc.setText(mapper.getKeyStrokeDesc(k));
            }
         }
      }
      catch (ArrayIndexOutOfBoundsException ar) {
         System.out.println("ar at index " + index + " - " + ar.getMessage());
      }

   }

   private void loadList(String which) {

      lm.clear();
      lm.removeAllElements();
      if (which.equals(LangTool.getString("key.labelKeys"))) {
         for (int x = 0; x < keyMnemonic.length; x++) {
            lm.addElement(LangTool.getString("key."+keyMnemonic[x]));
         }
         macros = false;
         special = false;
      }
      else {
         if (which.equals(LangTool.getString("key.labelMacros"))) {
            if (macrosList != null)
               for (int x = 0; x < macrosList.length; x++) {
                  lm.addElement(macrosList[x]);
               }
            macros = true;
            special = false;
         }
         else {

            // we will use a collator here so that we can take advantage of the locales
            Collator collator = Collator.getInstance();
            CollationKey key = null;
            StringBuffer sb = new StringBuffer();

            Set set = new TreeSet();
            for (int x =0;x < 256; x++) {
               char c = codePage.ebcdic2uni(x);
               char ac = codePage.getASCIIChar(x);
               if (!Character.isISOControl(c)) {
                  sb.setLength(0);
                  if (Integer.toHexString(ac).length() == 1){
                     sb.append("0x0" + Integer.toHexString(ac).toUpperCase());
                  }
                  else {
                     sb.append("0x" + Integer.toHexString(ac).toUpperCase());
                  }

                  sb.append(" - " + c + " - " + getUnicodeString(c));
                  key = collator.getCollationKey(sb.toString());

                  set.add(key);
               }
            }

            Iterator iterator = set.iterator();
            while (iterator.hasNext()) {
               CollationKey keyc = (CollationKey)iterator.next();
               lm.addElement(keyc.getSourceString());
            }

            macros = false;
            special = true;

         }
      }
      if (!lm.isEmpty())
         functions.setSelectedIndex(0);
   }

   private String getUnicodeString(char c) {

      String s = Integer.toHexString(c).toUpperCase();
      int len = s.length();
      switch (len) {

         case 2:
            s = "'\\u00" + s + "'";
            break;
         case 3:
            s = "'\\u0" + s + "'";
            break;
         default:
            s = "'\\u" + s + "'";

      }

      return s;
   }

   private JButton addOptButton(String text,
                              String ac,
                              Container container,
                              boolean enabled) {

      JButton button = new JButton(text);
      button.setEnabled(enabled);
      button.setActionCommand(ac);
      button.addActionListener(this);
      button.setAlignmentX(Component.CENTER_ALIGNMENT);
      container.add(button);

      return button;
   }

   public void actionPerformed(ActionEvent e) {

      if (e.getActionCommand().equals("DONE")) {
         if (mods)
            mapper.saveKeyMap();
         setVisible(false);
      }

      if (e.getActionCommand().equals("MAP")) {
         mapIt();
      }
      if (e.getActionCommand().equals("REMOVE")) {
         removeIt();
      }

   }

   private void mapIt() {

      Object[]      message = new Object[1];
      JPanel kgp = new JPanel();
      final KeyGetter kg = new KeyGetter();
      kg.setForeground(Color.blue);
      message[0] = kgp;
      kg.setText(LangTool.getString("key.labelMessage") +
                        (String)functions.getSelectedValue());
      kgp.add(kg);

      String[] options = new String[1];
      options[0] = LangTool.getString("key.labelClose");
      JPanel kp = new JPanel();

      JOptionPane opain = new JOptionPane(message,
                        JOptionPane.PLAIN_MESSAGE,
                        JOptionPane.DEFAULT_OPTION,        // option type
                        null,
                        options,
                        options[0]);

      dialog = opain.createDialog(this, getTitle());

      // add window listener to the dialog so that we can place focus on the
      //   key getter label instead of default and set the new key value when
      //   the window is closed.
      dialog.addWindowListener(new WindowAdapter() {
         boolean gotFocus = false;
         public void windowClosed(WindowEvent we) {
            setNewKeyStrokes(kg.keyevent);
         }

         public void windowActivated(WindowEvent we) {
            // Once window gets focus, set initial focus to our KeyGetter
            //    component
            if (!gotFocus) {
               kg.grabFocus();
               gotFocus = true;
            }
         }
      });

      dialog.setVisible(true);

   }

   private void removeIt() {
      if (!macros && !special) {
         mapper.removeKeyStroke(keyMnemonic[functions.getSelectedIndex()]);
         strokeDesc.setText(mapper.getKeyStrokeDesc(
                           keyMnemonic[functions.getSelectedIndex()]));

      }
      else {

         if (macros) {
            mapper.removeKeyStroke((String)functions.getSelectedValue());
            strokeDesc.setText(mapper.getKeyStrokeDesc(
                              (String)functions.getSelectedValue()));
         }
         if (special) {
            String k = "";
            k += ((String)functions.getSelectedValue()).charAt(7);
            mapper.removeKeyStroke((String)functions.getSelectedValue());
            strokeDesc.setText(mapper.getKeyStrokeDesc(
                              (String)functions.getSelectedValue()));

         }
      }

   }

   private void setNewKeyStrokes(KeyEvent ke) {

      if (!macros && !special) {
         if (isLinux)
            mapper.setKeyStroke(keyMnemonic[functions.getSelectedIndex()],ke,isAltGr);
         else
            mapper.setKeyStroke(keyMnemonic[functions.getSelectedIndex()],ke);
         strokeDesc.setText(mapper.getKeyStrokeDesc(
                           keyMnemonic[functions.getSelectedIndex()]));
      }
      else {
         if (macros) {
            System.out.println((String)functions.getSelectedValue());
            if (isLinux)
               mapper.setKeyStroke((String)functions.getSelectedValue(),ke,isAltGr);
            else
               mapper.setKeyStroke((String)functions.getSelectedValue(),ke);

            strokeDesc.setText(mapper.getKeyStrokeDesc(
                              (String)functions.getSelectedValue()));
         }
         if (special) {
            System.out.println((String)functions.getSelectedValue());
            String k = "";
            k += ((String)functions.getSelectedValue()).charAt(7);
            if (isLinux)
               mapper.setKeyStroke(k,ke,isAltGr);
            else
               mapper.setKeyStroke(k,ke);

            strokeDesc.setText(mapper.getKeyStrokeDesc(k));


         }

      }

      mods = true;
   }

   /**
    * This class extends label so that we can display text as well as capture
    * the key stroke(s) to assign to keys.
    */
   public class KeyGetter extends JLabel {

      KeyEvent keyevent;

      public KeyGetter() {
         super();
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

      }

      public boolean isFocusTraversable () {
         return true;
      }

      /**
       * Override to inform focus manager that component is managing focus changes.
       * This is to capture the tab and shift+tab keys.
       */
      public boolean isManagingFocus() {
         return true;
      }

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

//         displayInfo(e,"Pressed ");
         int keyCode = e.getKeyCode();

         if (isLinux && keyCode == e.VK_ALT_GRAPH) {

            isAltGr = true;
         }
         // be careful with the control key
         if (keyCode == e.VK_UNDEFINED ||
               keyCode == e.VK_CAPS_LOCK ||
               keyCode == e.VK_SHIFT ||
               keyCode == e.VK_ALT ||
               keyCode == e.VK_ALT_GRAPH ||
               keyCode == e.VK_CONTROL
            ) {

            return;
         }

         // be careful with the control key !!!!!!
         if (!e.isAltDown() ||
            !e.isShiftDown() ||
            !e.isControlDown() ||
            keyCode != KeyEvent.VK_CONTROL &&  // be careful about removing this line
            !e.isActionKey()) {

//            if (keyCode == KeyEvent.VK_ESCAPE ||
//               keyCode == KeyEvent.VK_CONTROL ||
//               keyCode == KeyEvent.VK_BACK_SPACE) {
//               displayInfo(e,"Pressed added");
               keyevent = e;
               dialog.setVisible(false);
               dialog.dispose();
//            }
         }


      }

      private void processVTKeyTyped(KeyEvent e){

//          displayInfo(e,"Typed ");
         int keycode = e.getKeyCode();
         if (e.isAltDown() ||
            e.isShiftDown() ||
            e.isControlDown() ||
            e.isActionKey() ||
            keycode == KeyEvent.VK_CONTROL) {

            keyevent = e;
//            displayInfo(e,"Released added ");
            dialog.setVisible(false);
            dialog.dispose();
         }

      }

      private void processVTKeyReleased(KeyEvent e){
//            displayInfo(e,"Released ");
         if (isLinux && e.getKeyCode() == e.VK_ALT_GRAPH) {

            isAltGr = false;
         }
         int keycode = e.getKeyCode();
         if (e.isAltDown() ||
            e.isShiftDown() ||
            e.isControlDown() ||
            e.isActionKey() ||
            keycode == KeyEvent.VK_CONTROL) {


            keyevent = e;
//            displayInfo(e,"Released added");
            dialog.setVisible(false);
            dialog.dispose();
         }
     }


   }
}
