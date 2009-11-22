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

import static org.tn5250j.TN5250jConstants.*;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.CollationKey;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultListModel;
import javax.swing.InputMap;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.interfaces.OptionAccessFactory;
import org.tn5250j.keyboard.configure.KeyConfigure;
import org.tn5250j.mailtools.SendEMailDialog;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.LoadMacroMenu;
import org.tn5250j.tools.Macronizer;
import org.tn5250j.tools.SendScreenImageToFile;
import org.tn5250j.tools.SendScreenToFile;
import org.tn5250j.tools.XTFRFile;

/**
 * Custom
 */
public class SessionPopup {

   private Screen5250 screen;
   private SessionGUI session;
   private tnvt vt;

	public SessionPopup(SessionGUI ses, MouseEvent me) {

      JMenuItem menuItem;
      Action action;
      JPopupMenu popup = new JPopupMenu();
      session = ses;
      vt = session.getSession().getVT();
      screen = session.getScreen();

      final int pos = session.getPosFromView(me.getX(),me.getY());

      if (!session.rubberband.isAreaSelected() && screen.isInField(pos,false) ) {
         action = new AbstractAction(LangTool.getString("popup.copy")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  screen.copyField(pos);
                  session.getFocusForMe();
               }
           };

         popup.add(createMenuItem(action,MNEMONIC_COPY));


         action = new AbstractAction(LangTool.getString("popup.paste")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  screen.pasteMe(false);
                  session.getFocusForMe();
               }
           };
         popup.add(createMenuItem(action,MNEMONIC_PASTE));

         action = new AbstractAction(LangTool.getString("popup.pasteSpecial")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  screen.pasteMe(true);
                  session.getFocusForMe();
               }
           };
         popup.add(action);

         popup.addSeparator();
      }
      else {

         action = new AbstractAction(LangTool.getString("popup.copy")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  session.actionCopy();
                  session.getFocusForMe();
               }
           };

         popup.add(createMenuItem(action,MNEMONIC_COPY));

         action = new AbstractAction(LangTool.getString("popup.paste")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  screen.pasteMe(false);
                  session.getFocusForMe();
               }
           };
         popup.add(createMenuItem(action,MNEMONIC_PASTE));

         action = new AbstractAction(LangTool.getString("popup.pasteSpecial")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  screen.pasteMe(true);
                  session.getFocusForMe();
               }
           };
         popup.add(action);

         Rectangle workR = new Rectangle();
         if (session.rubberband.isAreaSelected()) {

            // get the bounded area of the selection
            session.getBoundingArea(workR);

            popup.addSeparator();

            menuItem = new JMenuItem(LangTool.getString("popup.selectedColumns")
                              + " " + workR.width);
            menuItem.setArmed(false);
            popup.add(menuItem);

            menuItem = new JMenuItem(LangTool.getString("popup.selectedRows")
                              + " " + workR.height);
            menuItem.setArmed(false);
            popup.add(menuItem);

            JMenu sumMenu = new JMenu(LangTool.getString("popup.calc"));
            popup.add(sumMenu);

            action = new AbstractAction(LangTool.getString("popup.calcGroupCD")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  sumArea(true);
               }
            };
            sumMenu.add(action);

            action = new AbstractAction(LangTool.getString("popup.calcGroupDC")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  sumArea(false);
               }
            };
            sumMenu.add(action);

         }

         popup.addSeparator();

         action = new AbstractAction(LangTool.getString("popup.printScreen")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  session.printMe();
                  session.getFocusForMe();
               }
           };
         popup.add(createMenuItem(action,MNEMONIC_PRINT_SCREEN));

         popup.addSeparator();

         JMenu kbMenu = new JMenu(LangTool.getString("popup.keyboard"));

         popup.add(kbMenu);

         action = new AbstractAction(LangTool.getString("popup.mapKeys")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {

                  mapMeKeys();
               }
           };

         kbMenu.add(action);

         kbMenu.addSeparator();

         createKeyboardItem(kbMenu,MNEMONIC_ATTN);

         createKeyboardItem(kbMenu,MNEMONIC_RESET);

         createKeyboardItem(kbMenu,MNEMONIC_SYSREQ);

         if (screen.getOIA().isMessageWait() &&
               OptionAccessFactory.getInstance().isValidOption(MNEMONIC_DISP_MESSAGES)) {

            action = new AbstractAction(LangTool.getString("popup.displayMessages")) {
                  private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
                     vt.systemRequest('4');
                  }
              };

            kbMenu.add(createMenuItem(action,MNEMONIC_DISP_MESSAGES));
         }

         kbMenu.addSeparator();

         createKeyboardItem(kbMenu,MNEMONIC_DUP_FIELD);

         createKeyboardItem(kbMenu,MNEMONIC_HELP);

         createKeyboardItem(kbMenu,MNEMONIC_ERASE_EOF);

         createKeyboardItem(kbMenu,MNEMONIC_FIELD_PLUS);

         createKeyboardItem(kbMenu,MNEMONIC_FIELD_MINUS);

         createKeyboardItem(kbMenu,MNEMONIC_NEW_LINE);

         if (OptionAccessFactory.getInstance().isValidOption(MNEMONIC_PRINT)) {
            action = new AbstractAction(LangTool.getString("popup.hostPrint")) {
                  private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
                     vt.hostPrint(1);
                  }
              };
            kbMenu.add(createMenuItem(action,MNEMONIC_PRINT));
         }

         createShortCutItems(kbMenu);

         if (screen.getOIA().isMessageWait() &&
            OptionAccessFactory.getInstance().isValidOption(MNEMONIC_DISP_MESSAGES)) {

            action = new AbstractAction(LangTool.getString("popup.displayMessages")) {
                  private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
                     vt.systemRequest('4');
                  }
              };
            popup.add(createMenuItem(action,MNEMONIC_DISP_MESSAGES));
         }

         popup.addSeparator();

         action = new AbstractAction(LangTool.getString("popup.hexMap")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  showHexMap();
                  session.getFocusForMe();
               }
           };
         popup.add(createMenuItem(action,""));

         action = new AbstractAction(LangTool.getString("popup.mapKeys")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {

                  mapMeKeys();
                  session.getFocusForMe();
               }
           };
         popup.add(createMenuItem(action,""));

         if (OptionAccessFactory.getInstance().isValidOption(MNEMONIC_DISP_ATTRIBUTES)) {

            action = new AbstractAction(LangTool.getString("popup.settings")) {
                  private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
                     session.actionAttributes();
                    session.getFocusForMe();
                  }
              };
            popup.add(createMenuItem(action,MNEMONIC_DISP_ATTRIBUTES));

         }

         popup.addSeparator();

         if (session.isMacroRunning()) {
            action = new AbstractAction(LangTool.getString("popup.stopScript")) {
                  private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
                     session.setStopMacroRequested();
                  }
              };
            popup.add(action);
         }
         else {

            JMenu macMenu = new JMenu(LangTool.getString("popup.macros"));

            if (session.isSessionRecording()) {
               action = new AbstractAction(LangTool.getString("popup.stop")) {
                     private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e) {
                        session.stopRecordingMe();
                        session.getFocusForMe();
                     }
               };

            }
            else {
               action = new AbstractAction(LangTool.getString("popup.record")) {
                     private static final long serialVersionUID = 1L;

					public void actionPerformed(ActionEvent e) {
                        session.startRecordingMe();
                        session.getFocusForMe();

                     }
               };
            }
            macMenu.add(action);
            if (Macronizer.isMacrosExist()) {
               // this will add a sorted list of the macros to the macro menu
               addMacros(macMenu);
            }
            popup.add(macMenu);
         }

         popup.addSeparator();

         JMenu xtfrMenu = new JMenu(LangTool.getString("popup.export"));

         if (OptionAccessFactory.getInstance().isValidOption(MNEMONIC_FILE_TRANSFER)) {

            action = new AbstractAction(LangTool.getString("popup.xtfrFile")) {
                  private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
                     doMeTransfer();
                     session.getFocusForMe();
                  }
              };

            xtfrMenu.add(createMenuItem(action,MNEMONIC_FILE_TRANSFER));
         }

         if (OptionAccessFactory.getInstance().isValidOption(MNEMONIC_SPOOL_FILE)) {

            action = new AbstractAction(LangTool.getString("popup.xtfrSpool")) {
                  private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
                     doMeSpool();
                     session.getFocusForMe();
                  }
              };

            xtfrMenu.add(action);
         }

         popup.add(xtfrMenu);

         JMenu sendMenu = new JMenu(LangTool.getString("popup.send"));
         popup.add(sendMenu);

         if (OptionAccessFactory.getInstance().isValidOption(MNEMONIC_QUICK_MAIL)) {

            action = new AbstractAction(LangTool.getString("popup.quickmail")) {
                  private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
                    sendQuickEMail();
                    session.getFocusForMe();
                  }
               };
   		   sendMenu.add(createMenuItem(action,MNEMONIC_QUICK_MAIL));
         }

         if (OptionAccessFactory.getInstance().isValidOption(MNEMONIC_E_MAIL)) {

            action = new AbstractAction(LangTool.getString("popup.email")) {
                  private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
                     sendScreenEMail();
                     session.getFocusForMe();
                  }
              };

            sendMenu.add(createMenuItem(action,MNEMONIC_E_MAIL));
         }

         action = new AbstractAction(LangTool.getString("popup.file")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  sendMeToFile();
               }
           };

         sendMenu.add(action);

         action = new AbstractAction(LangTool.getString("popup.toImage")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  sendMeToImageFile();
               }
           };

         sendMenu.add(action);

         popup.addSeparator();

      }

      if (OptionAccessFactory.getInstance().isValidOption(MNEMONIC_OPEN_NEW)) {

         action = new AbstractAction(LangTool.getString("popup.connections")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  session.startNewSession();
               }
           };

         popup.add(createMenuItem(action,MNEMONIC_OPEN_NEW));
      }

      popup.addSeparator();

      if (OptionAccessFactory.getInstance().isValidOption(MNEMONIC_TOGGLE_CONNECTION)) {

         if (vt.isConnected()) {
            action = new AbstractAction(LangTool.getString("popup.disconnect")) {
                  private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
                     changeConnection();
                     session.getFocusForMe();
                  }
              };
         }
         else {

            action = new AbstractAction(LangTool.getString("popup.connect")) {
                  private static final long serialVersionUID = 1L;

				public void actionPerformed(ActionEvent e) {
                     changeConnection();
                    session.getFocusForMe();
                  }
              };


         }

         popup.add(createMenuItem(action,MNEMONIC_TOGGLE_CONNECTION));
      }

      if (OptionAccessFactory.getInstance().isValidOption(MNEMONIC_CLOSE)) {

         action = new AbstractAction(LangTool.getString("popup.close")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  session.closeSession();
               }
           };

         popup.add(createMenuItem(action,MNEMONIC_CLOSE));

      }

      GUIGraphicsUtils.positionPopup(me.getComponent(),popup,
               me.getX(),me.getY());

   }

   private void createKeyboardItem (JMenu menu, String key) {

      if (OptionAccessFactory.getInstance().isValidOption(key)) {
         final String key2 = key;
         Action action = new AbstractAction(LangTool.getString("key." + key)) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
                  screen.sendKeys(key2);
               }
           };

         menu.add(createMenuItem(action,key));
      }

   }

   private void addMacros(JMenu menu) {

      LoadMacroMenu.loadMacros(session, menu);
   }

   private JMenuItem createMenuItem(Action action, String accelKey) {

      JMenuItem mi;

      mi = new JMenuItem();
      mi.setAction(action);
      if (session.keyHandler.isKeyStrokeDefined(accelKey))
         mi.setAccelerator(session.keyHandler.getKeyStroke(accelKey));
      else {

         InputMap map = session.getInputMap();
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

      InputMap map = session.getInputMap();
      KeyStroke[] allKeys = map.allKeys();
      ActionMap aMap = session.getActionMap();

      for (int x = 0; x < allKeys.length; x++) {

         mi =new JMenuItem();
         Action a = aMap.get(map.get(allKeys[x]));
         mi.setAction(a);
         mi.setText(LangTool.getString("key." + (String)map.get(allKeys[x])));
         mi.setAccelerator(allKeys[x]);
         sm.add(mi);
      }
   }

   private void sumArea(boolean which) {


      List<Double> sumVector = session.sumThem(which);
      Iterator<Double> l = sumVector.iterator();
      double sum = 0.0;
      double inter = 0.0;
      while (l.hasNext()) {

         inter = 0.0;
         try {
            inter = l.next().doubleValue();
         }
         catch (Exception e) {
            System.out.println(e.getMessage());
         }

         sum += inter;

      }
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

   private void showHexMap() {

      JPanel srp = new JPanel();
      srp.setLayout(new BorderLayout());
      DefaultListModel listModel = new DefaultListModel();
      StringBuffer sb = new StringBuffer();

      // we will use a collator here so that we can take advantage of the locales
      Collator collator = Collator.getInstance();
      CollationKey key = null;

      Set<CollationKey> set = new TreeSet<CollationKey>();
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

      Iterator<?> iterator = set.iterator();
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

      Frame parent = (Frame)SwingUtilities.getRoot(session);

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
            if (((String)hm.getSelectedValue()).length() > 8)
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
   
/* *** NEVER USED LOCALLY ************************************************** */
//   private void printMe() {
//
//      session.printMe();
//      session.getFocusForMe();
//   }

/* *** NEVER USED LOCALLY ************************************************** */
//   private void executeMeMacro(ActionEvent ae) {
//
//      executeMeMacro(ae.getActionCommand());
//
//   }

/* *** NEVER USED LOCALLY ************************************************** */
//   private void executeMeMacro(String macro) {
//
//      Macronizer.invoke(macro,session);
//
//   }

   private void mapMeKeys() {

      Frame parent = (Frame)SwingUtilities.getRoot(session);

      if (Macronizer.isMacrosExist()) {
         String[] macrosList = Macronizer.getMacroList();
         new KeyConfigure(parent,macrosList,vt.getCodePage());
      } else {
         new KeyConfigure(parent,null,vt.getCodePage());
      }

   }
   
/* *** NEVER USED LOCALLY ************************************************** */
//   private void runScript () {
//
//      Macronizer.showRunScriptDialog(session);
//     session.getFocusForMe();
//
//   }

   private void doMeTransfer() {

      new XTFRFile((Frame)SwingUtilities.getRoot(session), vt, session);

   }

   private void doMeSpool() {

      try {
         org.tn5250j.spoolfile.SpoolExporter spooler =
                        new org.tn5250j.spoolfile.SpoolExporter(vt, session);
         spooler.setVisible(true);
      }
      catch (NoClassDefFoundError ncdfe) {
         JOptionPane.showMessageDialog(session,
                                       LangTool.getString("messages.noAS400Toolbox"),
                                       "Error",
                                       JOptionPane.ERROR_MESSAGE,null);
      }

   }

   private void sendScreenEMail() {

      new SendEMailDialog((Frame)SwingUtilities.getRoot(session),session);
   }

   private void sendQuickEMail() {

      new SendEMailDialog((Frame)SwingUtilities.getRoot(session),session,false);
   }

   private void sendMeToFile() {
      
      SendScreenToFile.showDialog(SwingUtilities.getRoot(session),screen);
   }

   private void sendMeToImageFile() {
      // Change sent by LUC - LDC to add a parent frame to be passed
      new SendScreenImageToFile((Frame)SwingUtilities.getRoot(session),session);
   }

   private void changeConnection() {

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

}
