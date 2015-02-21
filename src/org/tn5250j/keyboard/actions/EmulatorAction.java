/**
 * Title: EmulatorAction.java
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

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.KeyStroke;

import org.tn5250j.keyboard.KeyMapper;
import org.tn5250j.SessionPanel;
import org.tn5250j.interfaces.OptionAccessFactory;

/**
 * Base class for all emulator actions
 */
public abstract class EmulatorAction extends AbstractAction {

   private static final long serialVersionUID = 1L;
// content pane to be used if needed by subclasses
   protected SessionPanel session;

   public EmulatorAction(SessionPanel session, String name) {

      super(name);
      this.session = session;
   }

   public EmulatorAction(SessionPanel session, String name, KeyStroke ks, KeyMapper keyMap) {

      this(session,name);

      setKeyStroke(name, ks, keyMap);
   }

   protected void setKeyStroke(String action, KeyStroke ks, KeyMapper keyMap) {

      if (OptionAccessFactory.getInstance().isRestrictedOption(action))
         return;

      if (KeyMapper.isKeyStrokeDefined(action)) {
         ks = KeyMapper.getKeyStroke(action);
      }

      session.getInputMap().put(ks,action);
      session.getActionMap().put(action, this );

      // check for alternate
      if (KeyMapper.isKeyStrokeDefined(action + ".alt2")) {
         ks = KeyMapper.getKeyStroke(action + ".alt2");
         session.getInputMap().put(ks,action);
         session.getActionMap().put(action,this );
      }

   }

   abstract public void actionPerformed(ActionEvent e);
}