/*
 * @(#)Wizard.java
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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.tn5250j.event.WizardEvent;
import org.tn5250j.event.WizardListener;

/**
 * Class to create and manage a <i>Wizard</i> style framework.  Create and add
 * <code>WizardPages</code> and add <code>WizardListener</code>'s for all your Wizard fun.
 */
public class Wizard extends JPanel {

   private static final long serialVersionUID = 1L;
/** layout used */
   protected CardLayout cardLayout;
   /** list of wizard listeners registered with the bean */
   transient protected Vector<WizardListener> listeners;

   /**
    * Create a <code>Wizard</code> component.
    */
   public Wizard() {
      setLayout(new CardLayout());
      cardLayout = (CardLayout) getLayout();
      addContainerListener(containerListener);
   }

   /**
    * Moves to the wizard page with the specified name.
    * @param name Name of the page when it was added to the JCWizard.
    * @see #first
    * @see #last
    */
   public void show(String name) {
      cardLayout.show(this, name);
   }

   /**
    * Moves to the first page in the Wizard.
    * @see #last
    * @see #show
    */
   public void first() {
      cardLayout.first(this);
   }

   /**
    * Moves to the last page in the Wizard.
    * @see #first
    * @see #show
    */
   public void last() {
      cardLayout.last(this);
   }

   /**
    * Advance to the next page
    * @see #previous
    * @see #finish
    * @see #cancel
    */
   public boolean next() {
      boolean is_last_page = false;
      Component current_page = null,
         next_page = null;

      int ncomponents = getComponentCount();
      for (int i = 0 ; i < ncomponents ; i++) {
         Component comp = getComponent(i);
         if (comp.isVisible()) {
            current_page = comp;
            if (i == ncomponents - 1) {
               is_last_page = true;
               next_page = getComponent(0);
            }
            else {
               next_page = getComponent(i + 1);
            }
            break;
         }
      }

      WizardEvent event = new WizardEvent(this, current_page, next_page,
                                    is_last_page, !is_last_page);

      // in the preceding constructor, by default, we want
      // to prevent wraparound to first card from the
      // last card so we set "allow_change" to be the
      // opposite of "is_last_page"

      //
      // post nextBegin event
      //
      Enumeration<WizardListener> e = listeners.elements();
      for (; e.hasMoreElements(); ) {
    	  WizardListener listener =
    		  e.nextElement();
    	  listener.nextBegin(event);
      }

      if (event.getAllowChange() == false) {
         return false;
      }

      //
      // advance to the next page unless a new page has been specified
      //
      if (next_page != event.getNewPage()) {
         cardLayout.show(this, event.getNewPage().getName());
      } else {
         cardLayout.next(this);
      }

      //
      // Post nextComplete event
      //
      e = listeners.elements();
      for (; e.hasMoreElements(); ) {
    	  WizardListener listener =
    		  e.nextElement();
    	  listener.nextComplete(event);
      }

      return true;
   }

   /**
    * Move to the previous page
    * @see #next
    * @see #finish
    * @see #cancel
    */
   public boolean previous() {
      boolean is_last_page = false;
      boolean is_first_page = false;
      Component current_page = null,
         previous_page = null;

      int ncomponents = getComponentCount();
      for (int i = 0 ; i < ncomponents ; i++) {
         Component comp = getComponent(i);
         if (comp.isVisible()) {
            current_page = comp;
            if (i == ncomponents - 1) {
               is_last_page = true;
            }
            if (i == 0) {
               previous_page = getComponent(ncomponents - 1);
               is_first_page = true;
            }
            else {
               previous_page = getComponent(i - 1);
            }
            break;
         }
      }

      WizardEvent event = new WizardEvent(this, current_page,	previous_page,
                                    is_last_page, !is_first_page);
      // in the preceding constructor, by default, we want
      // to prevent wraparound to the last card from the
      // first card so we set "allow_change" to be the
      // opposite of "is_first_page"

      Enumeration<WizardListener> e;
      //
      // post previousBegin event
      //
      e = listeners.elements();
      for (; e.hasMoreElements(); ) {
    	  WizardListener listener =
    		  e.nextElement();
    	  listener.previousBegin(event);
      }

      if (event.getAllowChange() == false) {
         return false;
      }

      //
      // Advance to previous page unless a new page has been specified
      //
      if (previous_page != event.getNewPage()) {
         cardLayout.show(this, event.getNewPage().getName());
      } else {
         cardLayout.previous(this);
      }

      //
      // Post previousComplete event
      //
      e = listeners.elements();
      for (; e.hasMoreElements(); ) {
    	  WizardListener listener =
    		  e.nextElement();
    	  listener.previousComplete(event);
      }
      return true;
   }

   /**
    * Invokes the registered "finish" action.
    * @see #next
    * @see #previous
    * @see #cancel
    */
   public boolean finish() {
      boolean is_last_page = false;
      Component comp = getCurrentPage();
      if (comp == getComponent(getComponentCount() - 1)) {
         is_last_page = true;
      }

      WizardEvent event = new WizardEvent(this, comp, null,
                                    is_last_page, true);

      //
      // Post finished event
      //
      Enumeration<WizardListener> e = listeners.elements();
      for (; e.hasMoreElements(); ) {
    	  WizardListener listener =
    		  e.nextElement();
    	  listener.finished(event);
      }

      return event.getAllowChange();
   }

   /**
    * Invokes the registered "cancel" action.
    * @see #next
    * @see #previous
    * @see #finish
    */
   public boolean cancel() {
      boolean is_last_page = false;
      Component comp = getCurrentPage();
      if (comp == getComponent(getComponentCount() - 1)) {
         is_last_page = true;
      }

      WizardEvent event = new WizardEvent(this, comp, null,
                                    is_last_page, true);

      //
      // Post Canceled event
      //
      Enumeration<WizardListener> e = listeners.elements();
      for (; e.hasMoreElements(); ) {
    	  WizardListener listener =
    		  e.nextElement();
    	  listener.canceled(event);
      }

      return event.getAllowChange();
   }

   /**
    * Invokes the registered "help" action.
    * @see #next
    * @see #previous
    * @see #finish
    * @see #cancel
    */
   public void help() {
      boolean is_last_page = false;
      Component comp = getCurrentPage();
      if (comp == getComponent(getComponentCount() - 1)) {
         is_last_page = true;
      }

      WizardEvent event = new WizardEvent(this, comp, null,
                                    is_last_page, true);
      //
      // Post Help event
      //
      Enumeration<WizardListener> e = listeners.elements();
      for (; e.hasMoreElements(); ) {
    	  WizardListener listener =
    		  e.nextElement();
    	  listener.help(event);
         }
   }

   /**
    * Retrieves the current visible page.
    */
   protected Component getCurrentPage() {
      int ncomponents = getComponentCount();
      for (int i = 0 ; i < ncomponents ; i++) {
         Component comp = getComponent(i);
         if (comp.isVisible()) {
            return comp;
         }
      }

      return null;
   }

   /**
    * Adds a new <code>WizardListener</code> to the list.
    */
   public void addWizardListener(WizardListener l) {
      if (listeners == null)
         listeners = new Vector<WizardListener>(3);

      listeners.add(l);
   }

   /**
    * Removes a <code>ValidateListener</code> from the list.
    */
   public void removeWizardListener(WizardListener l) {
         if (listeners == null) {
             return;
         }
         listeners.removeElement(l);
   }

   /**
    * Adds a page child.
    */
   public Component add(Component page) {
      add(page, page.getName());
      return page;
   }

   /**
    * A listener on the "next" button that is implemented as an anonymous
    * inner class that simply invokes the containing classes "next()"
    * method.
    * @see #next
    */
   transient protected ActionListener nextListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
         next();
      }
   };

   /**
    * A listener on the "previous" button that is implemented as an anonymous
    * inner class that simply invokes the containing classes "previous()"
    * method.
    * @see #previous
    */
   transient protected ActionListener previousListener = new ActionListener() {
      public void actionPerformed(ActionEvent e) {
         previous();
      }
   };

   /**
    * A listener on the "finish" button that is implemented as an anonymous
    * inner class that simply invokes the containing classes "finish()"
    * method.
    * @see #finish
    */
   transient protected ActionListener finishListener = new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
         finish();
      }
   };

   /**
    * A listener on the "cancel" button that is implemented as an anonymous
    * inner class that simply invokes the containing classes "cancel()"
    * method.
    * @see #cancel
    */
   transient protected ActionListener cancelListener = new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
         cancel();
      }
   };

   /**
    * A listener on the "help" button that is implemented as an anonymous
    * inner class that simply invokes the containing classes "help()"
    * method.
    * @see #help
    */
   transient protected ActionListener helpListener = new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
         help();
      }
   };


   /**
    * Container listner that listens for new pages that are added, and adds
    * listeners to the buttons of the children so that the container knows
    * when to post the proper "Wizard" events.
    */
   transient protected ContainerListener containerListener
       = new ContainerListener() {
      public void componentAdded(ContainerEvent e) {
         if (e.getChild() instanceof WizardPage) {
            WizardPage wp = (WizardPage)e.getChild();
            JButton b;
            b = wp.getNextButton();
            if (b != null) {
               b.addActionListener(nextListener);
            }
            b = wp.getPreviousButton();
            if (b != null) {
               b.addActionListener(previousListener);
            }
            b = wp.getFinishButton();
            if (b != null) {
               b.addActionListener(finishListener);
            }
            b = wp.getCancelButton();
            if (b != null) {
               b.addActionListener(cancelListener);
            }
            b = wp.getHelpButton();
            if (b != null) {
               b.addActionListener(helpListener);
            }
         }
      }

      public void componentRemoved(ContainerEvent e) {
         if (e.getChild() instanceof WizardPage) {
            WizardPage wp = (WizardPage)e.getChild();
            JButton b;
            b = wp.getNextButton();
            if (b != null) {
               b.removeActionListener(nextListener);
            }
            b = wp.getPreviousButton();
            if (b != null) {
               b.removeActionListener(previousListener);
            }
            b = wp.getFinishButton();
            if (b != null) {
               b.removeActionListener(finishListener);
            }
            b = wp.getCancelButton();
            if (b != null) {
               b.removeActionListener(cancelListener);
            }
            b = wp.getHelpButton();
            if (b != null) {
               b.removeActionListener(helpListener);
            }
         }
      }
   };

}