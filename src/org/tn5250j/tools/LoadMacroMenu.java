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

public final class LoadMacroMenu {

   public static void loadMacros(Session session, Macronizer macros, JMenu menu) {

      final Session ses = session;
      Vector mv = new Vector();
      Action action;

      menu.addSeparator();


      String[] macrosList = macros.getMacroList();


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
         menu.add(action);

      }

      scriptDir("scripts",menu,session);
   }

   public static void scriptDir(String pathName, JMenu menu,Session session) {

      File root = new File(pathName);
      File directory;

      ExecuteScriptAction action;

      if (root.isDirectory()) {
         try {
            directory = new File(root.getCanonicalPath());
            String[] files = directory.list();
            for (int x = 0; x < files.length; x++) {

               File f = new File(directory,files[x]);
               if (f.isFile()) {

                  action = new ExecuteScriptAction(f.getName(),
                                             f.getAbsolutePath(),
                                             session) {
                    };
                  menu.add(action);

               }

            }

         }
         catch (IOException ioe) {
            System.out.println(ioe.getMessage());

         }
      }

   }
}