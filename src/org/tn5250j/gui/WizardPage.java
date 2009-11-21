/*
 * @(#)WizardPage.java
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

import java.awt.*;
import java.awt.event.*;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.BoxLayout;
import javax.swing.Box;

import org.tn5250j.tools.LangTool;

/**
 * Class to create and manage a Wizard style framework for you.
 */
public class WizardPage extends JPanel {

   private static final long serialVersionUID = 1L;
static public final int NO_BUTTONS = 0x00;
   static public final int PREVIOUS   = 0x01;
   static public final int NEXT       = 0x02;
   static public final int FINISH     = 0x04;
   static public final int CANCEL     = 0x08;
   static public final int HELP       = 0x10;
   static public final int ALL = PREVIOUS | NEXT | FINISH | CANCEL | HELP;

   protected JButton previousButton;
   protected JButton nextButton;
   protected JButton finishButton;
   protected JButton cancelButton;
   protected JButton helpButton;

   private Action nextAction;
   private Action previousAction;
   private Action finishAction;
   private Action cancelAction;
   private Action helpAction;

   protected static final int GROUP_SPACING = 10;
   protected static final int MARGIN = 10;
   protected static final int BUTTON_SPACING = 5;

   // Box containing the buttons used
   protected JPanel buttonPanel;
   protected JSeparator separator;

   // Pane returned by getContentPane.  This is the pane the
   // developer adds his/her code to.
   protected Container contentPane;

   public WizardPage() {
       this(ALL);
   }

   public WizardPage(int button_flags) {

      // set layout as a vertical column
      setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
//      setLayout(new BorderLayout());
//      Box pageBox = Box.createVerticalBox();
      contentPane = new JPanel();

      // add the pages contentpane to our wizard page
      add(contentPane);

      // lets add some glue here but it still does not stop the separator from
      //  moving up and down.
      add(Box.createGlue());

      // create the separator between the panels
      JSeparator js = new JSeparator();
      js.setAlignmentY(Component.BOTTOM_ALIGNMENT);
      add(js);
      add(Box.createRigidArea(new Dimension(10,10)));

      // create the box for the buttons with an x-axis
      buttonPanel = new JPanel();
      buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.X_AXIS));
      buttonPanel.setName("buttonPanel");
      buttonPanel.add(Box.createHorizontalGlue());
      add(buttonPanel);

      if (button_flags == 0) {
         // no buttons to add :-(
         return;
      }

      if ((button_flags & PREVIOUS) != 0) {
         previousAction = new AbstractAction(LangTool.getString("wiz.previous")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
               }
           };

         previousButton = new JButton(previousAction);
         buttonPanel.add(Box.createRigidArea(new Dimension(GROUP_SPACING,0)));
         buttonPanel.add(previousButton);
      }

      if ((button_flags & NEXT) != 0) {
         nextAction = new AbstractAction(LangTool.getString("wiz.next")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
               }
           };

         nextButton = new JButton(nextAction);
         buttonPanel.add(Box.createRigidArea(new Dimension(BUTTON_SPACING,0)));
         buttonPanel.add(nextButton);
      }

      if ((button_flags & FINISH) != 0) {
         finishAction = new AbstractAction(LangTool.getString("wiz.finish")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
               }
           };
         finishButton = new JButton(finishAction);
         buttonPanel.add(Box.createRigidArea(new Dimension(BUTTON_SPACING,0)));
         buttonPanel.add(finishButton);
      }

      if ((button_flags & CANCEL) != 0) {
         cancelAction = new AbstractAction(LangTool.getString("wiz.cancel")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
               }
           };

         cancelButton = new JButton(cancelAction);
         buttonPanel.add(Box.createRigidArea(new Dimension(GROUP_SPACING,0)));
         buttonPanel.add(cancelButton);
         buttonPanel.add(Box.createRigidArea(new Dimension(MARGIN,0)));
      }
      if ((button_flags & HELP) != 0) {
         helpAction = new AbstractAction(LangTool.getString("wiz.help")) {
               private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
               }
           };

         helpButton = new JButton(helpAction);
      }
   }

   public JButton getNextButton() {
      return nextButton;
   }

   public JButton getPreviousButton() {
      return previousButton;
   }

   public JButton getFinishButton() {
      return finishButton;
   }

   public JButton getCancelButton() {
      return cancelButton;
   }

   public JButton getHelpButton() {
      return helpButton;
   }

   public void setContentPane(Container new_pane) {
      if (new_pane == null) {
         throw new NullPointerException("content pane must be non-null");
      }
      // rip out all components and rebuild
      removeAll();
      contentPane = new_pane;
      add(contentPane);
      add(new JSeparator());
      add(buttonPanel);
   }

   /**
    * Overrides normal getContentPane to provide specially
    * managed area
    */
   public Container getContentPane() {
      return contentPane;
   }

}
