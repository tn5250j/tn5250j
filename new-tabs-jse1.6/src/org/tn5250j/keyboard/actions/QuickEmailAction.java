/**
 * Title: QuickEmailAction.java
 * Copyright:   Copyright (c) 2001,2002
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
package org.tn5250j.keyboard.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JFrame;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.tn5250j.SessionPanel;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.keyboard.KeyMapper;
import org.tn5250j.mailtools.SendEMailDialog;

/**
 * Quick Email Action
 */
public class QuickEmailAction extends EmulatorAction {

   private static final long serialVersionUID = 1L;

public QuickEmailAction(SessionPanel session, KeyMapper keyMap) {
      super(session,
    		  TN5250jConstants.MNEMONIC_QUICK_MAIL,
            KeyStroke.getKeyStroke(KeyEvent.VK_F,KeyEvent.ALT_MASK),
            keyMap);

   }

   public void actionPerformed(ActionEvent e) {

      Runnable emailIt = new Runnable() {
         public void run() {
            new SendEMailDialog((JFrame)SwingUtilities.getRoot(session),
                  session,false);
         }

      };

      SwingUtilities.invokeLater(emailIt);

   }
}