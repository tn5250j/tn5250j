package org.tn5250j.interfaces;

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
import javax.swing.*;
import org.tn5250j.Session;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.event.SessionChangeEvent;

public interface GUIViewInterface {


   public abstract void addSessionView(String descText,Session session);
   public abstract void removeSessionView(Session targetSession);
   public abstract boolean containsSession(Session session);
   public abstract int getSessionViewCount();
   public abstract Session getSessionAt( int index);
   public abstract void onSessionJump(SessionJumpEvent jumpEvent);
   public abstract void onSessionChanged(SessionChangeEvent changeEvent);
   public abstract void setIcons(ImageIcon focused, ImageIcon unfocused);

}