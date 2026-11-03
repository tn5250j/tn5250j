/**
 * $Id$
 * <p>
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
 * Company:
 *
 * @author: master_jaf
 * <p>
 * Description:
 * Tab component for displaying title text and icons.
 * <p>
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package org.tn5250j.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;
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
 * This class based on the ButtonTabComponent example from
 * Sun Microsystems, Inc. and was modified to use tool tips, other layout and stuff.
 */
public final class ButtonTabComponent extends JPanel implements SessionListener {

    private static final long serialVersionUID = 1L;

    private final JTabbedPane pane;
    private List<TabClosedListener> closeListeners;
    private final JLabel label;

    public ButtonTabComponent(final JTabbedPane pane) {
        super(new BorderLayout(0, 0));
        if (pane == null) {
            throw new NullPointerException("TabbedPane is null");
        }
        this.pane = pane;
        setOpaque(false);

        this.label = new TabLabel(); // {
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.CENTER);
        // add more space between the label and the button
        label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));
        // tab button
        JButton button = new TabButton();
        button.setHorizontalAlignment(SwingConstants.TRAILING);
        add(button, BorderLayout.EAST);
        // add more space to the top of the component
        setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));

        pane.addPropertyChangeListener(new PropertyChangeListener() {
            // triggers repaint, so size is recalculated, when title text changes
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if ("indexForTitle".equals(evt.getPropertyName())) {
                    label.revalidate();
                    label.repaint();
                }
            }
        });
    }

    @Override
    public void onSessionChanged(SessionChangeEvent changeEvent) {
        if (changeEvent.getState() == TN5250jConstants.STATE_CONNECTED) {
            this.label.setEnabled(true);
            this.label.setToolTipText(LangTool.getString("ss.state.connected"));
            this.setToolTipText(LangTool.getString("ss.state.connected"));
        } else {
            this.label.setEnabled(false);
            this.label.setToolTipText(LangTool.getString("ss.state.disconnected"));
            this.setToolTipText(LangTool.getString("ss.state.disconnected"));
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

    /**
     * Label delegating icon and text to the corresponding tab.
     * Implementing MouseListener is a workaround, cause when applying
     * a tool tip to the JLabel, clicking the tabs doesn't work
     * (tested on Tn5250j0.6.2; WinXP+WinVista+Win7; JRE 1.6.0_20+).
     */
    private final class TabLabel extends JLabel implements MouseListener {
        private static final long serialVersionUID = 1L;

        public TabLabel() {
            addMouseListener(this);
        }

        public String getText() {
            final int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
                return pane.getTitleAt(i);
            }
            return null;
        }

        public Icon getIcon() {
            final int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
                return pane.getIconAt(i);
            }
            return null;
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseClicked(MouseEvent e) {
            actionSelectTab();
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
         */
        @Override
        public void mousePressed(MouseEvent e) {
            // not needed
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseReleased(MouseEvent e) {
            actionSelectTab();
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseEntered(MouseEvent e) {
            // not needed
        }

        /* (non-Javadoc)
         * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
         */
        @Override
        public void mouseExited(MouseEvent e) {
            // not needed
        }

        private void actionSelectTab() {
            final int i = pane.indexOfTabComponent(ButtonTabComponent.this);
            if (i != -1) {
                pane.setSelectedIndex(i);
            }
        }

    }

    /**
     * Special button displaying an X as close icon.
     */
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
