/*
 * @(#)ToggleDocument.java
 * Copyright:    Copyright (c) 2001
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

import java.util.Vector;
import javax.swing.text.PlainDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;

import org.tn5250j.event.ToggleDocumentListener;

public class ToggleDocument extends PlainDocument {

   private static final long serialVersionUID = 1L;
Vector<ToggleDocumentListener> listeners;

   public void insertString(int offs, String str, AttributeSet a)
                                                throws BadLocationException {

      super.insertString(offs, str, a);
      if (getText(0, getLength()).length() > 0)
         fireNotEmpty();
   }

   public void remove(int offs, int len) throws BadLocationException {
      super.remove(offs, len);
      if (getText(0, getLength()).length() == 0)
         fireEmpty();
   }

   /**
    * Add a ToggleDocumentListener to the listener list.
    *
    * @param listener  The ToggleDocumentListener to be added
    */
   public synchronized void addToggleDocumentListener(ToggleDocumentListener listener) {

      if (listeners == null) {
          listeners = new java.util.Vector<ToggleDocumentListener>(3);
      }
      listeners.addElement(listener);

   }

   /**
    * Remove a Toggle Document Listener from the listener list.
    *
    * @param listener  The ToggleDocumentListener to be removed
    */
   public synchronized void removeToggleDocumentListener(ToggleDocumentListener listener) {
      if (listeners == null) {
          return;
      }
      listeners.removeElement(listener);

   }

   /**
    * Notify all registered listeners that the field is no longer empty.
    *
    */
   public void fireNotEmpty() {

      if (listeners != null) {
         int size = listeners.size();
         for (int i = 0; i < size; i++) {
            ToggleDocumentListener target =
                    listeners.elementAt(i);
            target.toggleNotEmpty();
         }
      }
   }

   /**
    * Notify all registered listeners that the field is no longer empty.
    *
    */
   public void fireEmpty() {

      if (listeners != null) {
         int size = listeners.size();
         for (int i = 0; i < size; i++) {
            ToggleDocumentListener target =
                    listeners.elementAt(i);
            target.toggleEmpty();
         }
      }
   }

}

