/*
 * @(#)GUIViewInterface.java
 * Copyright:    Copyright (c) 2002
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
package org.tn5250j.interfaces;

import org.tn5250j.My5250;
import org.tn5250j.SessionPanel;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.gui.GenericTn5250JFrame;

/**
 * Abstract class for all main GUI interfaces.<br>
 * Direct known subclasses:
 * <ul>
 * <li>{@link org.tn5250j.Gui5250Frame} which shows a window with multiple tabs</li>
 * <li>{@link org.tn5250j.Gui5250MDIFrame}</li>
 * </ul>
 */
public abstract class GUIViewInterface extends GenericTn5250JFrame {

   private static final long serialVersionUID = 1L;
   protected static My5250 me;
   protected static int sequence;
   protected int frameSeq;

   public GUIViewInterface(My5250 m) {
      super();
      me = m;
   }

   public int getFrameSequence() {
      return frameSeq;
   }

   public abstract void addSessionView(String descText, SessionPanel session);
   public abstract void removeSessionView(SessionPanel targetSession);
   public abstract boolean containsSession(SessionPanel session);
   public abstract int getSessionViewCount();
   public abstract SessionPanel getSessionAt(int index);
   public abstract void onSessionJump(SessionJumpEvent jumpEvent);
   public abstract void onSessionChanged(SessionChangeEvent changeEvent);

}