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

import java.awt.*;
import javax.swing.*;

import org.tn5250j.gui.TN5250jFrame;
import org.tn5250j.Session;
import org.tn5250j.My5250;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.event.SessionChangeEvent;

public abstract class GUIViewInterface extends TN5250jFrame {

   protected static My5250 me;
   protected static int sequence;
   protected int frameSeq;
   protected ImageIcon focused = null;
   protected ImageIcon unfocused = null;

   public GUIViewInterface(My5250 m) {
      super();
      me = m;
   }

   public int getFrameSequence() {

      return frameSeq;
   }

   /**
    * Set the icons to be used for focused and unfocused
    *
    * @param focused
    * @param unfocused
    */
   public void setIcons(ImageIcon focused, ImageIcon unfocused) {

      this.focused = focused;
      this.unfocused = unfocused;
   }

   public abstract void addSessionView(String descText,Session session);
   public abstract void removeSessionView(Session targetSession);
   public abstract boolean containsSession(Session session);
   public abstract int getSessionViewCount();
   public abstract Session getSessionAt( int index);
   public abstract void onSessionJump(SessionJumpEvent jumpEvent);
   public abstract void onSessionChanged(SessionChangeEvent changeEvent);

}