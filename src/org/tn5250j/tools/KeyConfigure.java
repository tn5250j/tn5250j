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
 * MERCHANTABILreITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
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
import org.tn5250j.encoding.CodePage;
import org.tn5250j.scripting.InterpreterDriverManager;
import org.tn5250j.*;

public class KeyConfigure extends JDialog implements ActionListener,
                                                         TN5250jConstants {

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

            KeyDescription kd = (KeyDescription)lm.getElementAt(index);
            strokeDesc.setText(mapper.getKeyStrokeDesc(mnemonicData[kd.getIndex()]));
         }
         else {
            if (macros) {
               Object o = lm.getElementAt(index);
               if (o instanceof String) {
                  System.out.println((String)o);
                  strokeDesc.setText(mapper.getKeyStrokeDesc((String)o));
               }
               else
                  if (o instanceof Macro) {

                     Macro m = (Macro)o;
                     strokeDesc.setText(mapper.getKeyStrokeDesc(m.getFullName()));
                  }
            }

            if (special) {
               System.out.println((String)lm.getElementAt(index));
               String k = parseSpecialCharacter((String)lm.getElementAt(index));
               strokeDesc.setText(mapper.getKeyStrokeDesc(k));
            }
         }
      }
      catch (ArrayIndexOutOfBoundsException ar) {
         System.out.println("ar at index " + index + " - " + ar.getMessage());
      }

   }

   private String parseSpecialCharacter(String value) {

      StringTokenizer tokenizer = new StringTokenizer(value, "-");

      if (tokenizer.hasMoreTokens()) {
         String first = tokenizer.nextToken();
         return String.valueOf(value.charAt(first.length() + 2));
      }

      return "";
   }

   private void loadList(String which) {

      lm.clear();
      lm.removeAllElements();


      if (which.equals(LangTool.getString("key.labelKeys"))) {
         Vector lk = new Vector(mnemonicData.length);
         for (int x = 0; x < mnemonicData.length; x++) {
            lk.addElement(new KeyDescription(LangTool.getString("key."+mnemonicData[x]),x));
         }

         Collections.sort(lk,new KeyDescriptionCompare());

         for (int x = 0; x < mnemonicData.length; x++) {
            lm.addElement(lk.get(x));
         }
         macros = false;
         special = false;
      }
      else {
         if (which.equals(LangTool.getString("key.labelMacros"))) {
            Vector macrosVector = new Vector();
            if (macrosList != null)
               for (int x = 0; x < macrosList.length; x++) {
                  macrosVector.add(macrosList[x]);
               }
            scriptDir("scripts",macrosVector);
            loadListModel(lm,macrosVector,null,0);
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
               char ac = codePage.ebcdic2uni(x);
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
         if (mods) {
            mapper.saveKeyMap();
            mapper.fireKeyChangeEvent();
         }
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

      String function;

      if (functions.getSelectedValue() instanceof String)
         function = (String)functions.getSelectedValue();
      else
         if (functions.getSelectedValue() instanceof Macro) {
            function = ((Macro)functions.getSelectedValue()).toString();
         }
         else
            function = ((KeyDescription)functions.getSelectedValue()).toString();

      kg.setText(LangTool.getString("key.labelMessage") +
                        function);
      kgp.add(kg);

      String[] options = new String[1];
      options[0] = LangTool.getString("key.labelClose");

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
         int index = ((KeyDescription)functions.getSelectedValue()).getIndex();

         mapper.removeKeyStroke(mnemonicData[index]);
         strokeDesc.setText(mapper.getKeyStrokeDesc(
                           mnemonicData[index]));

      }
      else {

         if (macros) {
            Object o = functions.getSelectedValue();
            String name;
            if (o instanceof Macro) {
               name = ((Macro)o).getFullName();
            }
            else {
               name = (String)o;
            }
            mapper.removeKeyStroke(name);
            strokeDesc.setText(mapper.getKeyStrokeDesc(
                              name));
         }
         if (special) {
            String k = "";
            k += ((String)functions.getSelectedValue()).charAt(7);
            mapper.removeKeyStroke(k);
            strokeDesc.setText(mapper.getKeyStrokeDesc(
                              (String)functions.getSelectedValue()));

         }
      }
      mods = true;

   }

   private void setNewKeyStrokes(KeyEvent ke) {

      if (!macros && !special) {
         int index = ((KeyDescription)functions.getSelectedValue()).getIndex();
         if (isLinux)
            mapper.setKeyStroke(mnemonicData[index],ke,isAltGr);
         else
            mapper.setKeyStroke(mnemonicData[index],ke);
         strokeDesc.setText(mapper.getKeyStrokeDesc(
                           mnemonicData[index]));
      }
      else {
         if (macros) {
            Object o = functions.getSelectedValue();
            String macro;
            if (o instanceof Macro)
               macro = ((Macro)o).getFullName();
            else
               macro = (String)o;

            System.out.println(macro);
            if (isLinux)
               mapper.setKeyStroke(macro,ke,isAltGr);
            else
               mapper.setKeyStroke(macro,ke);

            strokeDesc.setText(mapper.getKeyStrokeDesc(
                              macro));
         }
         if (special) {
            System.out.println((String)functions.getSelectedValue());
            String k = parseSpecialCharacter((String)functions.getSelectedValue());
//            k += ((String)functions.getSelectedValue()).charAt(7);
            mapper.removeKeyStroke(k);
            if (isLinux) {
               mapper.setKeyStroke(k,ke,isAltGr);
            }
            else {
               mapper.setKeyStroke(k,ke);
            }
            strokeDesc.setText(mapper.getKeyStrokeDesc(k));


         }

      }

      mods = true;
   }

   private static class KeyDescriptionCompare implements Comparator {
      public int compare(Object one, Object two) {
         String s1 = one.toString();
         String s2 = two.toString();
         return s1.compareToIgnoreCase(s2);
      }

   }

   private class KeyDescription {

      private int index;
      private String text;

      public KeyDescription(String text,int index) {

         this.text = text;
         this.index = index;

      }

      public String toString() {

         return text;
      }

      public int getIndex() {
         return index;
      }
   }


   public static void scriptDir(String pathName, Vector scripts) {

      File root = new File(pathName);

      try {

         loadScripts(scripts,root.getCanonicalPath(),root);

      }
      catch (IOException ioe) {
         System.out.println(ioe.getMessage());

      }


   }

   /**
    * Recursively read the scripts directory and add them to our macros vector
    *    holding area
    *
    * @param vector
    * @param path
    * @param directory
    */
   private static void loadScripts(Vector vector,
                                    String path,
                                    File directory) {

      Macro macro;

      File[] macroFiles = directory.listFiles();
      if(macroFiles == null || macroFiles.length == 0)
         return;

      Arrays.sort(macroFiles,new MacroCompare());

      for(int i = 0; i < macroFiles.length; i++) {
         File file = macroFiles[i];
         String fileName = file.getName();
         if(file.isHidden()) {
            /* do nothing! */
            continue;
         }
         else if(file.isDirectory()) {
            Vector subvector = new Vector();
            subvector.addElement(fileName.replace('_',' '));
            loadScripts(subvector,path + fileName + '/',file);
            // if we do not want empty directories to show up uncomment this
            //    line.  It is uncommented here.
            if(subvector.size() != 1)
               vector.addElement(subvector);
         }
         else {
            if (InterpreterDriverManager.isScriptSupported(fileName)) {
               String fn = fileName.replace('_',' ');
               int index = fn.lastIndexOf('.');
               if (index > 0) {
                  fn = fn.substring(0,index);
               }

               macro = new Macro (fn, file.getAbsolutePath(),fileName);
               vector.addElement(macro);
            }
         }
      }

   }

   /**
    * Load the ListModel with the scripts from the vector of macros provided
    *
    * @param menu
    * @param vector
    * @param start
    */
   private static void loadListModel(DefaultListModel lm,
                                          Vector vector,
                                          String prefix,
                                          int start) {

      for (int i = start; i < vector.size(); i++) {
         Object obj = vector.elementAt(i);
         if (obj instanceof Macro) {
            Macro m = (Macro)obj;
            m.setPrefix(prefix);
            lm.addElement(m);
         }
         else
            if (obj instanceof Vector) {
               Vector subvector = (Vector)obj;
               String name = (String)subvector.elementAt(0);
               if (prefix != null)
                  loadListModel(lm,subvector,prefix + '/' + name + '/',1);
               else
                  loadListModel(lm,subvector,name + '/',1);
            }
            else {
               if (obj instanceof String) {

                  lm.addElement((String)obj);

               }
            }
         }

   }

   private static class Macro {

      String name;
      String path;
      String prefix;
      String fileName;

      Macro (String name, String path, String fileName) {

         this.name = name;
         this.path = path;
         this.fileName = fileName;
      }

      /**
       * Setst the directory prefix
       *
       * @param prefix before the name
       */
      public void setPrefix(String prefix) {
         this.prefix = prefix;
      }

      /**
       * This function gets the full name representation of the macro
       *
       * @return the full name non prettied up
       */
      public String getFullName() {
         if (prefix != null)
            return prefix + fileName;
         else
            return fileName;
      }

      /**
       * This function is used for display of the macro name prettied up
       *
       * @return pretty string
       */
      public String toString() {

         if (prefix != null)
            return prefix + name;
         else
            return name;
      }
   }

   public static class MacroCompare implements Comparator {
      public int compare(Object one, Object two) {
         String s1 = one.toString();
         String s2 = two.toString();
         return s1.compareToIgnoreCase(s2);
      }

   }


   /**
    * This class extends label so that we can display text as well as capture
    * the key stroke(s) to assign to keys.
    */
   private class KeyGetter extends JLabel {

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

        String charString, keyCodeString, modString, tmpString,isString,locString;

        char c = e.getKeyChar();
        int keyCode = e.getKeyCode();
        int modifiers = e.getModifiers();
//        int location = e.getKeyLocation();

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

        locString = "location = (UNKNOWN)";

//        switch (location) {
//            case KeyEvent.KEY_LOCATION_LEFT:
//               locString = "location = " + location + " (LEFT)";
//               break;
//            case KeyEvent.KEY_LOCATION_NUMPAD:
//               locString = "location = " + location + " (NUM_PAD)";
//               break;
//            case KeyEvent.KEY_LOCATION_RIGHT:
//               locString = "location = " + location + " (RIGHT)";
//               break;
//            case KeyEvent.KEY_LOCATION_STANDARD:
//               locString = "location = " + location + " (STANDARD)";
//               break;
//            default:
//               locString = "location = " + location + " (UNKNOWN)";
//               break;
//
//        }

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
                           + "    " + locString + newline
                           + "    " + isString + newline);

       }

      private void processVTKeyPressed(KeyEvent e){

         displayInfo(e,"Pressed ");
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

          displayInfo(e,"Typed ");
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
            displayInfo(e,"Released ");
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
