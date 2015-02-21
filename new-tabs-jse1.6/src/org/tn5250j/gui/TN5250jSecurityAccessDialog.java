/*
 * @(#)TN5250jSecurityAccessDialog.java
 * Copyright:    Copyright (c) 2001 , 2002
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

package org.tn5250j.gui;

import javax.swing.JOptionPane;

import org.tn5250j.tools.LangTool;
import org.tn5250j.gui.GenericTn5250JFrame;

public class TN5250jSecurityAccessDialog {

   // set so outsiders can not initialize the dialog.
   private TN5250jSecurityAccessDialog() {

   }

   static public void showErrorMessage(SecurityException se) {

      GenericTn5250JFrame parent = new GenericTn5250JFrame();
      JOptionPane.showMessageDialog(parent,LangTool.getString("messages.SADMessage")
                                    + se.getMessage()
                                    ,LangTool.getString("messages.SADTitle"),
                                    JOptionPane.ERROR_MESSAGE);


   }
}