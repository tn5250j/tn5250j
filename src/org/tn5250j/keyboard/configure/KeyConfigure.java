/**
 * Title: KeyConfigure
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
package org.tn5250j.keyboard.configure;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.text.*;
import java.lang.reflect.*;

import org.tn5250j.encoding.CodePage;
import org.tn5250j.scripting.InterpreterDriverManager;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.keyboard.KeyMapper;
import org.tn5250j.keyboard.KeyStroker;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.AlignLayout;
import org.tn5250j.tools.system.OperatingSystem;


public class KeyConfigure extends JDialog implements ActionListener,
                                                         TN5250jConstants {

   Properties props;
   JPanel keyPanel = new JPanel();
   JPanel options = new JPanel();
   JTextArea strokeDesc = new JTextArea();
   JTextArea strokeDescAlt = new JTextArea();
   JLabel strokeLocation = new JLabel();
   JLabel strokeLocationAlt = new JLabel();
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
   private boolean altKey;

   public KeyConfigure(Frame parent, String[] macros, CodePage cp) {

      super(parent);

      codePage = cp;
      macrosList = macros;

      if (OperatingSystem.isUnix() && !OperatingSystem.isMacOS()) {
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


      keyPanel.setLayout(borderLayout);
      keyPanel.add(createFunctionsPanel(),BorderLayout.WEST);

      keyPanel.add(createMappingPanel(),BorderLayout.CENTER);

      // add the panels to our dialog
      getContentPane().add(keyPanel,BorderLayout.CENTER);
      getContentPane().add(options, BorderLayout.SOUTH);


      // add option buttons to options panel
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

   private JPanel createFunctionsPanel() {

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

      return fp;

   }

   private JPanel createMappingPanel () {

      // set the descriptions defaults
      strokeDesc.setColumns(30);
      strokeDesc.setBackground(functions.getBackground());
      strokeDesc.setEditable(false);

      strokeDescAlt.setColumns(30);
      strokeDescAlt.setBackground(functions.getBackground());
      strokeDescAlt.setEditable(false);

      // create main panel
      JPanel dp = new JPanel();
      dp.setBorder(BorderFactory.createTitledBorder(
                                    LangTool.getString("key.labelMapTo")));

      dp.setLayout(new BoxLayout(dp,BoxLayout.Y_AXIS));

      // create primary map panel
      JPanel primeKeyMapPanel = new JPanel();
      primeKeyMapPanel.setLayout(new BorderLayout());

      // create key description panel
      JPanel primeKeyPanel = new JPanel();

      primeKeyPanel.setLayout(new AlignLayout(3,5,5));

      primeKeyPanel.add(strokeDesc);

      // add the option buttons
      addOptButton(LangTool.getString("key.labelMap","Map Key"),"MAP-Prime",
                                       primeKeyPanel,true);
      addOptButton(LangTool.getString("key.labelRemove","Remove"),"REMOVE-Prime",
                                       primeKeyPanel,true);

      // add the description to primary map panel
      primeKeyMapPanel.add(primeKeyPanel,BorderLayout.NORTH);

      // create the location description panel
      JPanel loc1 = new JPanel();
      loc1.setLayout(new BorderLayout());
      loc1.add(strokeLocation,BorderLayout.NORTH);

      // add the location description panel to the primary map panel
      primeKeyMapPanel.add(loc1,BorderLayout.CENTER);


      // create the alternate map panel
      JPanel altKeyMapPanel = new JPanel();
      altKeyMapPanel.setLayout(new BorderLayout());

      // create the alternate description panel
      JPanel altKeyPanel = new JPanel();
      altKeyPanel.setLayout(new AlignLayout(3,5,5));

      altKeyPanel.add(strokeDescAlt);

      // add the options to the description panel
      addOptButton(LangTool.getString("key.labelMap","Map Key"),"MAP-Alt",
                                       altKeyPanel,true);
      addOptButton(LangTool.getString("key.labelRemove","Remove"),"REMOVE-Alt",
                                       altKeyPanel,true);

      // add the description panel to the alternate map panel
      altKeyMapPanel.add(altKeyPanel,BorderLayout.NORTH);

      // create the alternate location description panel
      JPanel locAlt = new JPanel();
      locAlt.setLayout(new BorderLayout());
      locAlt.add(strokeLocationAlt,BorderLayout.NORTH);

      // add the alternate location description panel to alternate map panel
      altKeyMapPanel.add(locAlt,BorderLayout.CENTER);

      // add the map panels for display
      dp.add(primeKeyMapPanel);
      dp.add(altKeyMapPanel);

      return dp;
   }

   private void setKeyDescription(int index) {

      // This try and catch is to fix a problem in JDK1.4-betas
      try {
         if (!macros && !special) {

            KeyDescription kd = (KeyDescription)lm.getElementAt(index);

            setKeyInformation(mnemonicData[kd.getIndex()]);
         }
         else {
            if (macros) {
               Object o = lm.getElementAt(index);
               if (o instanceof String) {
                  System.out.println((String)o);
                  setKeyInformation((String)o);
               }
               else
                  if (o instanceof Macro) {

                     Macro m = (Macro)o;
                     setKeyInformation(m.getFullName());
                  }
            }

            if (special) {
               System.out.println((String)lm.getElementAt(index));
               String k = parseSpecialCharacter((String)lm.getElementAt(index));
               setKeyInformation(k);
            }
         }
      }
      catch (ArrayIndexOutOfBoundsException ar) {
         System.out.println("ar at index " + index + " - " + ar.getMessage());
      }

   }

   private void setKeyInformation(String keyDesc) {

      if (keyDesc.endsWith(KeyStroker.altSuffix)) {

         keyDesc = keyDesc.substring(0,keyDesc.indexOf(KeyStroker.altSuffix));
      }

      strokeDesc.setText(mapper.getKeyStrokeDesc(keyDesc));
      strokeDescAlt.setText(mapper.getKeyStrokeDesc(keyDesc +
                              KeyStroker.altSuffix));

      strokeLocation.setText(getLocationDesc(keyDesc));
      strokeLocationAlt.setText(getLocationDesc(keyDesc + KeyStroker.altSuffix));
   }

   private String getLocationDesc(String keyDesc) {

      String locStr = LangTool.getString("key.labelLocUnknown");

      if (mapper.isKeyStrokeDefined(keyDesc)) {

         switch (mapper.getKeyStroker(keyDesc).getLocation()) {

            case KeyStroker.KEY_LOCATION_LEFT:
               locStr = LangTool.getString("key.labelLocLeft");
               break;

            case KeyStroker.KEY_LOCATION_RIGHT:
               locStr = LangTool.getString("key.labelLocRight");
               break;

            case KeyStroker.KEY_LOCATION_STANDARD:
               locStr = LangTool.getString("key.labelLocStandard");
               break;

            case KeyStroker.KEY_LOCATION_NUMPAD:
               locStr = LangTool.getString("key.labelLocNumPad");
               break;

         }
      }

      return locStr;
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

      if (e.getActionCommand().equals("MAP-Prime")) {
         altKey = false;
         mapIt();
      }
      if (e.getActionCommand().equals("REMOVE-Prime")) {
         altKey = false;
         removeIt();
      }

      if (e.getActionCommand().equals("MAP-Alt")) {
         altKey = true;
         mapIt();
      }
      if (e.getActionCommand().equals("REMOVE-Alt")) {
         altKey = true;
         removeIt();
      }

   }

   private void mapIt() {

      Object[]      message = new Object[1];

      JPanel kgp = new JPanel();
      final KeyGetterInterface kg = getMeAKeyProcessor();
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

      kg.setDialog(dialog);

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

   private KeyGetterInterface getMeAKeyProcessor() {

      ClassLoader loader = KeyConfigure.class.getClassLoader();
      if (loader == null)
         loader = ClassLoader.getSystemClassLoader();

      Class       keyProcessorClass;
      Constructor keyProcessorConstructor;

      try {
         String className = "org.tn5250j.keyboard.configure.KeyGetter";
         if (!OperatingSystem.hasJava14())
            className += "13";

         keyProcessorClass = loader.loadClass(className);
         keyProcessorConstructor = keyProcessorClass.getConstructor(null);
         Object obj = keyProcessorConstructor.newInstance(null);
         return (KeyGetterInterface)obj;
      }
      catch (Throwable t) {

      }

      return new KeyGetter13();
   }

   private void removeIt() {
      if (!macros && !special) {
         int index = ((KeyDescription)functions.getSelectedValue()).getIndex();

         String function = mnemonicData[index];

         if (altKey)
            function += KeyStroker.altSuffix;

         mapper.removeKeyStroke(function);
         setKeyInformation(function);


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

            if (altKey)
               name += KeyStroker.altSuffix;

            mapper.removeKeyStroke(name);
            setKeyInformation(name);
         }
         if (special) {
            String k = "";
            k += ((String)functions.getSelectedValue()).charAt(7);
            if (altKey)
               k += KeyStroker.altSuffix;

            mapper.removeKeyStroke(k);
            setKeyInformation(k);
         }
      }
      mods = true;

   }

   private void setNewKeyStrokes(KeyEvent ke) {

      if (!macros && !special) {
         int index = ((KeyDescription)functions.getSelectedValue()).getIndex();
         String stroke = mnemonicData[index];

         if (altKey)
            stroke += KeyStroker.altSuffix;

         if (isLinux) {
            mapper.setKeyStroke(stroke,ke,isAltGr);
         }
         else {
            mapper.setKeyStroke(stroke,ke);
         }

         setKeyInformation(stroke);

      }
      else {
         if (macros) {
            Object o = functions.getSelectedValue();
            String macro;
            if (o instanceof Macro)
               macro = ((Macro)o).getFullName();
            else
               macro = (String)o;

            if (altKey)
               macro += KeyStroker.altSuffix;

            System.out.println(macro);
            if (isLinux)
               mapper.setKeyStroke(macro,ke,isAltGr);
            else
               mapper.setKeyStroke(macro,ke);

            setKeyInformation(macro);

         }

         if (special) {
            System.out.println((String)functions.getSelectedValue());
            String k = parseSpecialCharacter((String)functions.getSelectedValue());

            if (altKey)
               k += KeyStroker.altSuffix;

            mapper.removeKeyStroke(k);

            if (isLinux) {
               mapper.setKeyStroke(k,ke,isAltGr);
            }
            else {
               mapper.setKeyStroke(k,ke);
            }

            setKeyInformation(k);

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

}
