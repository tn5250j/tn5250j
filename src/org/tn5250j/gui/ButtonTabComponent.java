/*
 * Copyright (c) 1995 - 2008 Sun Microsystems, Inc.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Sun Microsystems nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.tn5250j.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

import org.tn5250j.TN5250jConstants;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionListener;
import org.tn5250j.event.TabClosedListener;
import org.tn5250j.tools.LangTool;

/**
 * Component to be used as tabComponent; Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to.<br>
 * <br>
 * Even  
 * <br><br>
 * This class based on the ButtonTabComponent example from
 * Sun Microsystems, Inc. and was modified.
 */
public final class ButtonTabComponent extends JPanel implements SessionListener {

   private static final long serialVersionUID = 1L;
   
   private final JTabbedPane pane;
   private List<TabClosedListener> closeListeners;
   private final JLabel label;
   
   public ButtonTabComponent(final JTabbedPane pane) {
      // unset default FlowLayout' gaps
      super(new FlowLayout(FlowLayout.LEFT, 0, 0));
      if (pane == null) {
         throw new NullPointerException("TabbedPane is null");
      }
      this.pane = pane;
      setOpaque(false);
      
      this.label = new JLabel() {
         private static final long serialVersionUID = 1L;
         public String getText() {
            final int tabIdx = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (tabIdx != -1) {
               return pane.getTitleAt(tabIdx);
            }
            return null;
         }
         public Icon getIcon() {
            final int tabIdx = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (tabIdx != -1) {
               return pane.getIconAt(tabIdx);
            }
            return null;
         }
      };
      add(label);
      // add more space between the label and the button
      label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
      // tab button
      JButton button = new TabButton();
      add(button);
      // add more space to the top of the component
      setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
   }

   @Override
   public void onSessionChanged(SessionChangeEvent changeEvent) {
      if (changeEvent.getState() == TN5250jConstants.STATE_CONNECTED) {
         this.label.setEnabled(true);
         // XXX: When using tool tips, button is no more clickable :-/
         // this.label.setToolTipText(LangTool.getString(SESSION_CONNECTED));
      } else {
         this.label.setEnabled(false);
         // XXX: When using tool tips, button is no more clickable :-/
         // this.label.setToolTipText(LangTool.getString(SESSION_DISCONNECTED));
      }
   }
   
   /**
    * Add a TabClosedListener to the listener list.
    * 
    * @param listener The TabClosedListener to be added
    */
   public synchronized void addTabCloseListener(TabClosedListener listener) {
      if (closeListeners == null) {
         closeListeners = new ArrayList<TabClosedListener>(3);
      }
      closeListeners.add(listener);
   }

   /**
    * Remove a TabClosedListener from the listener list.
    * 
    * @param listener The TabClosedListener to be removed
    */
   public synchronized void removeTabCloseListener(TabClosedListener listener) {
      if (closeListeners == null) {
         return;
      }
      closeListeners.remove(listener);
   }

   /**
    * Notify all the tab listeners that this specific tab was selected to close.
    * 
    * @param tabToClose
    */
   protected void fireTabClosed(int tabToClose) {
      if (closeListeners != null) {
         int size = closeListeners.size();
         for (int i = 0; i < size; i++) {
            TabClosedListener target = closeListeners.get(i);
            target.onTabClosed(tabToClose);
         }
      }
   }

   // =======================================================================

   private final class TabButton extends JButton implements ActionListener {
      private static final long serialVersionUID = 1L;
      public TabButton() {
         int size = 17;
         setPreferredSize(new Dimension(size, size));
         setToolTipText(LangTool.getString("popup.close"));
         // Make the button looks the same for all Laf's
         setUI(new BasicButtonUI());
         // Make it transparent
         setContentAreaFilled(false);
         // No need to be focusable
         setFocusable(false);
         setBorder(BorderFactory.createEtchedBorder());
         setBorderPainted(false);
         // Making nice rollover effect
         // we use the same listener for all buttons
         addMouseListener(buttonMouseListener);
         setRolloverEnabled(true);
         // Close the proper tab by clicking the button
         addActionListener(this);
      }

      public void actionPerformed(ActionEvent e) {
         int i = pane.indexOfTabComponent(ButtonTabComponent.this);
         if (i != -1) {
            fireTabClosed(i);
            // hint: the actual close will be done within the TabbedPane Container
         }
      }

      // we don't want to update UI for this button
      public void updateUI() {
    	  
      }

      // paint the cross
      protected void paintComponent(Graphics g) {
         super.paintComponent(g);
         Graphics2D g2 = (Graphics2D) g.create();
         // shift the image for pressed buttons
         if (getModel().isPressed()) {
            g2.translate(1, 1);
         }
         g2.setStroke(new BasicStroke(2));
         g2.setColor(Color.BLACK);
         if (getModel().isRollover()) {
            g2.setColor(Color.MAGENTA);
         }
         int delta = 6;
         g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
         g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
         g2.dispose();
      }
   }

   private final static MouseListener buttonMouseListener = new MouseAdapter() {
      public void mouseEntered(MouseEvent e) {
         Component component = e.getComponent();
         if (component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            button.setBorderPainted(true);
         }
      }

      public void mouseExited(MouseEvent e) {
         Component component = e.getComponent();
         if (component instanceof AbstractButton) {
            AbstractButton button = (AbstractButton) component;
            button.setBorderPainted(false);
         }
      }
   };

}
