package org.tn5250j.tools;
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

import javax.swing.*;
import java.util.*;
import java.io.*;
import java.awt.event.*;

import org.tn5250j.*;
import org.tn5250j.scripting.ExecuteScriptAction;
import org.tn5250j.scripting.InterpreterDriverManager;
import org.tn5250j.tools.LangTool;

public final class LoadMacroMenu {

   private static Vector macroVector;

   static {
      macroVector = new Vector();
   }

   public static void loadMacros(Session session, Macronizer macros, JMenu menu) {

      final Session ses = session;
      Vector mv = new Vector();
      Action action;

      menu.addSeparator();


      String[] macrosList = Macronizer.getMacroList();


      for (int x = 0; x < macrosList.length; x++) {
         mv.add(macrosList[x]);
      }

      Collections.sort(mv);


      for (int x = 0; x < mv.size(); x++) {
         action = new AbstractAction((String)mv.get(x)) {
               public void actionPerformed(ActionEvent e) {
                  ses.executeMeMacro(e);
               }
           };

         JMenuItem mi = menu.add(action);

         final Gui5250 ji = (Gui5250)session;
         mi.addMouseListener(new MouseAdapter() {

            public void mouseReleased(MouseEvent e) {
               if (SwingUtilities.isRightMouseButton(e)) {
                  doOptionsPopup(e,ses);
               }
            }

            public void mousePressed(MouseEvent e) {
               if (SwingUtilities.isRightMouseButton(e)) {
                  doOptionsPopup(e,ses);
               }
            }
            public void mouseClicked(MouseEvent e) {
               if (SwingUtilities.isRightMouseButton(e)) {
                  doOptionsPopup(e,ses);
               }
            }
       });

      }

      scriptDir("scripts",menu,session);

      String conPath = "";
      String conPath2 = "";

      try {
         conPath = new File("scripts").getCanonicalPath();
         conPath2 = new File(GlobalConfigure.getInstance().getProperty(
                              "emulator.settingsDirectory") +
                              "scripts").getCanonicalPath();
      }
      catch (IOException ioe ) {

      }

      // lets not load the menu again if they point to the same place
      if (!conPath.equals(conPath2))
         scriptDir(GlobalConfigure.getInstance().getProperty(
                              "emulator.settingsDirectory") +
                              "scripts",menu,session);
   }

   private static void doOptionsPopup(MouseEvent e, Session session) {

      Action action;

      JPopupMenu j = new JPopupMenu("Macro Options");
      action = new AbstractAction(LangTool.getString("popup.delete")
                     + " " + ((JMenuItem)e.getSource()).getText()) {
         public void actionPerformed(ActionEvent e) {
            StringBuffer macro = new StringBuffer(((JMenuItem)e.getSource()).getText());
            macro.delete(0,LangTool.getString("popup.delete").length()+1);
            Macronizer.removeMacroByName(macro.toString());
         }
      };

      j.add(action);

      final Session ses = session;
      action = new AbstractAction(LangTool.getString("popup.execute")
                     + " " + ((JMenuItem)e.getSource()).getText()) {
         public void actionPerformed(ActionEvent e) {
            StringBuffer macro = new StringBuffer(((JMenuItem)e.getSource()).getText());
            macro.delete(0,LangTool.getString("popup.execute").length()+1);
            Macronizer.invoke(macro.toString(),ses);
         }
      };

      j.add(action);
      MouseEvent et = SwingUtilities.convertMouseEvent((JMenuItem)e.getSource(),e,session);
      GUIGraphicsUtils.positionPopup(session,j,et.getX(),et.getY());

   }

   public static void scriptDir(String pathName, JMenu menu,Session session) {

      File root = new File(pathName);

      try {

         macroVector = new Vector();

         loadScripts(macroVector,root.getCanonicalPath(),root, session);
         createScriptsMenu(menu,macroVector,0);

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
    * @param session
    */
   private static void loadScripts(Vector vector,
                                    String path,
                                    File directory,
                                    Session session) {

      ExecuteScriptAction action;

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
            Vector submenu = new Vector();
            submenu.addElement(fileName.replace('_',' '));
            loadScripts(submenu,path + fileName + '/',file,session);
            // if we do not want empty directories to show up uncomment this
            //    line.
//            if(submenu.size() != 1)
               vector.addElement(submenu);
         }
         else {
            if (InterpreterDriverManager.isScriptSupported(fileName)) {
               String fn = fileName.replace('_',' ');
               int index = fn.lastIndexOf('.');
               if (index > 0) {
                  fn = fn.substring(0,index);
               }
               action = new ExecuteScriptAction(fn,
                                          file.getAbsolutePath(),
                                          session) {
                 };
               vector.addElement(action);
            }
         }
      }
   }

   /**
    * Create the scripts menu(s) from the vector of macros provided
    *
    * @param menu
    * @param vector
    * @param start
    */
   private static void createScriptsMenu(JMenu menu, Vector vector, int start) {

      JPopupMenu jpop = new JPopupMenu();
      jpop.add("Delete");

      for (int i = start; i < vector.size(); i++) {
         Object obj = vector.elementAt(i);
         if (obj instanceof ExecuteScriptAction) {
            JMenuItem mi = menu.add((ExecuteScriptAction)obj);
         }
         else
            if (obj instanceof Vector) {
               Vector subvector = (Vector)obj;
               String name = (String)subvector.elementAt(0);
               JMenu submenu = new JMenu(name);
               createScriptsMenu(submenu,subvector,1);
               if(submenu.getMenuComponentCount() == 0) {
                  submenu.add(LangTool.getString("popup.noScripts"));
               }
               menu.add(submenu);
         }
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
