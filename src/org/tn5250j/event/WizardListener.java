/*
 * @(#)WizardListener.java
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

package org.tn5250j.event;

/**
 * The listener interface for those interested in
 * receiving Wizard events.
 */
public interface WizardListener {

   /**
    * Invoked <i>before</i> advancing to the next page.
    * Calling <code>e.setAllowChange(false)</code> will prevent the
    * next page from being advanced too.
    * Check the <code>e.isLastPage()</code> to see if you are on the
    * last page.
    */
   public void nextBegin(WizardEvent e);

   /**
    * Invoked after advancing to the next page.
    */
   public void nextComplete(WizardEvent e);

   /**
    * Invoked <i>before</i> advancing to the previous page.
    * Calling <code>e.setAllowChange(false)</code> will prevent the
    * previous page from being advanced too.
    */
   public void previousBegin(WizardEvent e);

   /**
    * Invoked after advancing to the previous page.
    */
   public void previousComplete(WizardEvent e);

   /**
    * Invoked if a "finish" action is triggered.
    */
   public void finished(WizardEvent e);

   /**
    * Invoked if the Cancel action is triggered.
    */
   public void canceled(WizardEvent e);

   /**
    * Invoked if the Help action is triggered.
    */
   public void help(WizardEvent e);

}

