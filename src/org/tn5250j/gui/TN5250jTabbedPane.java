/*
 * @(#)TN5250jTabbedPane.java Copyright: Copyright (c) 2001
 * @author Kenneth J. Pouncey
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this software; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

import org.tn5250j.event.TabClosedListener;
import org.tn5250j.gui.images.CloseTabIcon;

/**
 * This class provides an instance of the a JTabbedPane that provides the
 * following functionality:
 *
 *    Paints a closable (X) on each tab when the cursor enters a tab.
 *    Ability to cycle through the tabs using the mouse wheel.
 *    Reordering of tabs via drag-n-drop with a valid drip marker drawn.
 *
 */
public class TN5250jTabbedPane extends JTabbedPane implements MouseListener,
MouseWheelListener, MouseMotionListener {

	private static final long serialVersionUID = 1L;
	// the currectly drawn tab that an X is drawn on
	private int tabNumber;
	// the region that the X is drawn in the tab
	private Rectangle closeRect;

	private List<TabClosedListener> closeListeners = null;
	private final ReadWriteLock closeListenersLock = new ReentrantReadWriteLock();

	// variable to identify the drop position to reorder the tabs
	private int dropIndex = -1;

	// variable to identify if we are in dragging mode or not
	private boolean dragging = false;

	public TN5250jTabbedPane() {
		super();
		// this.setUI(new MyBasicTabbedPaneUI());
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
	}

	@Override
	public void addTab(String title, Icon icon, Component component, String tip) {
		super.addTab(title, new CloseTabIcon(icon), component, tip);
	}

	@Override
	public void addTab(String title, Icon icon, Component component) {
		super.addTab(title, new CloseTabIcon(icon), component);
	}

	@Override
	public void setIconAt(int index, Icon icon) {
		super.setIconAt(index, new CloseTabIcon(icon));
	}	

	public void mouseClicked(MouseEvent e) {
		int tabIndex = getTabIndex(e.getX(), e.getY());

		if (tabIndex < 0)
			return;

		if (closeRect.contains(e.getX(), e.getY())) {
			// the tab is being closed
			fireTabClosed(tabNumber);

		}
	}

	public void mouseExited(MouseEvent e) {
		tabNumber = -1;
		repaint();

	}

	public void mousePressed(MouseEvent e) {

		if (!e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) {
			int tabIndex = getTabIndex(e.getX(), e.getY());
			if (tabIndex != -1) {
				dropIndex = tabIndex;
			}
		}
	}

	public void mouseReleased(MouseEvent e) {
		if (!e.isPopupTrigger() && e.getButton() == MouseEvent.BUTTON1) {
			if (dragging) {
				int tabIndex = getTabIndex(e.getX(), e.getY());
				if (tabIndex != -1 && dropIndex != -1 && tabIndex != dropIndex) {
					reorderTab(dropIndex, tabIndex);
					setSelectedIndex(tabIndex);
				}
				dropIndex = -1;
				dragging = false;
				repaint();
			}
		}
	}

	/**
	 * Well just what it says.  As the mouse is dragged the reordering position
	 * marker is drawn if a valid drop position.
	 */
	public void mouseDragged(MouseEvent e) {

		if (dragging) {
			if (getUI().tabForCoordinate(this, e.getX(), e.getY()) != -1) {
				int stabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY());

				if (stabNumber >= 0) {

					if (stabNumber != tabNumber) {

						repaint();
					}

					tabNumber = stabNumber;

					Rectangle tabRect = getUI().getTabBounds(this, tabNumber);

					// System.out.println(tabNumber + " " + tabRect.x
					// + " " + tabRect.y
					// + " " + tabRect.width
					// + " " + tabRect.height);

					Graphics g = this.getGraphics();

					paintDropPosition(g, tabRect.x, tabRect.y, Color.red);
				}
			}
		}
		else {
			//  Well let's start dragging then
			dragging = true;
		}
	}

	public void mouseMoved(MouseEvent e) {

		int stabNumber = getUI().tabForCoordinate(this, e.getX(), e.getY());

		if (stabNumber >= 0) {

			if (stabNumber != tabNumber) {

				repaint();
			}

			tabNumber = stabNumber;

			//Rectangle tabRect = getUI().getTabBounds(this, tabNumber);

			// System.out.println(tabNumber + " " + tabRect.x
			// + " " + tabRect.y
			// + " " + tabRect.width
			// + " " + tabRect.height);

			Graphics g = this.getGraphics();

			closeRect = ((CloseTabIcon) getIconAt(tabNumber)).getBounds();

			if (closeRect.contains(e.getX(), e.getY()))
				paintClosableX(g, closeRect.x, closeRect.y, Color.red);
			else
				paintClosableX(g, closeRect.x, closeRect.y, Color.black);

		}

		else {
			repaint();
		}
	}

	/**
	 * Here we will cycle through the tabs with the mouse
	 */
	public void mouseWheelMoved(MouseWheelEvent e) {

		int notches = e.getWheelRotation();
		if (notches < 0) {

			// This is UP
			// We will just cycle from the last tab to the first tab
			if (this.getSelectedIndex() == this.getTabCount() - 1) {
				this.setSelectedIndex(0);

			}
			else {
				this.setSelectedIndex(this.getSelectedIndex() + 1);
			}

		}
		else {

			// This is DOWN
			// We will just cycle from the first tab to the last tab
			if (this.getSelectedIndex() == 0) {
				this.setSelectedIndex(this.getTabCount() - 1);

			}
			else {
				this.setSelectedIndex(this.getSelectedIndex() - 1);
			}
		}
	}

	/**
	 * This method paints the closable X on the tab in the correct position
	 *
	 * @param g
	 * @param x
	 * @param y
	 * @param x_color
	 */
	private void paintClosableX(Graphics g, int x, int y, Color x_color) {

		Color col = g.getColor();

		g.setColor(x_color);

		int y_p = y + 2;
		g.drawLine(x + 1, y_p, x + 12, y_p);
		g.drawLine(x + 1, y_p + 13, x + 12, y_p + 13);
		g.drawLine(x, y_p + 1, x, y_p + 12);
		g.drawLine(x + 13, y_p + 1, x + 13, y_p + 12);
		g.drawLine(x + 3, y_p + 3, x + 10, y_p + 10);
		g.drawLine(x + 3, y_p + 4, x + 9, y_p + 10);
		g.drawLine(x + 4, y_p + 3, x + 10, y_p + 9);
		g.drawLine(x + 10, y_p + 3, x + 3, y_p + 10);
		g.drawLine(x + 10, y_p + 4, x + 4, y_p + 10);
		g.drawLine(x + 9, y_p + 3, x + 3, y_p + 9);
		g.setColor(col);

	}

	/**
	 * This method paints the drop position on the tab bar to mark the position
	 * for the tab reordering.
	 *
	 * @param g
	 * @param x
	 * @param y
	 * @param x_color
	 */
	private void paintDropPosition(Graphics g, int x, int y, Color x_color) {

		Color col = g.getColor();

		g.setColor(x_color);

		int w = 10;
		int h = 10;
		int m = w / 2;
		x -= w / 2;
		y -= h / 2;
		Polygon poly = new Polygon();
		poly.addPoint(x, y);
		poly.addPoint(x + w, y);
		poly.addPoint(x + m, y + h);

		g.fillPolygon(poly);

		g.setColor(col);
	}

	/**
	 * Method to save off the current tab information remove it and then insert
	 * into the desired position
	 *
	 * @param newIndex
	 * @param currentTabIndex
	 */
	private void reorderTab(int newIndex, int currentTabIndex) {
		String title = getTitleAt(newIndex);
		Icon icon = getIconAt(newIndex);
		Component component = getComponentAt(newIndex);
		String toolTipText = getToolTipTextAt(newIndex);

		Color background = getBackgroundAt(newIndex);
		Color foreground = getForegroundAt(newIndex);
		Icon disabledIcon = getDisabledIconAt(newIndex);
		int mnemonic = getMnemonicAt(newIndex);
		int displayedMnemonicIndex = getDisplayedMnemonicIndexAt(newIndex);
		boolean enabled = isEnabledAt(newIndex);

		remove(newIndex);
		insertTab(title, icon, component, toolTipText, currentTabIndex);

		setBackgroundAt(currentTabIndex, background);
		setForegroundAt(currentTabIndex, foreground);
		setDisabledIconAt(currentTabIndex, disabledIcon);
		setMnemonicAt(currentTabIndex, mnemonic);
		setDisplayedMnemonicIndexAt(currentTabIndex, displayedMnemonicIndex);
		setEnabledAt(currentTabIndex, enabled);
	}

	/**
	 * Convenience method to obtain the tab index from the coordinates.
	 *
	 * @param x
	 * @param y
	 * @return
	 */
	private int getTabIndex(int x, int y) {
		return getUI().tabForCoordinate(this, x, y);
	}

	public void mouseEntered(MouseEvent e) {

	}

	/**
	 * Add a TabClosedListener to the listener list.
	 *
	 * @param listener The TabClosedListener to be added
	 */
	public final void addtabCloseListener(TabClosedListener listener) {
		closeListenersLock.writeLock().lock();
		try {
			if (closeListeners == null) {
				closeListeners = new ArrayList<TabClosedListener>(3);
			}
			closeListeners.add(listener);
		} finally {
			closeListenersLock.writeLock().unlock();
		}
	}

	/**
	 * Remove a TabClosedListener from the listener list.
	 *
	 * @param listener The TabClosedListener to be removed
	 */
	public final void removeTabCloseListener(TabClosedListener listener) {
		closeListenersLock.writeLock().lock();
		try {
			if (closeListeners != null) {
				closeListeners.remove(listener);
			}
		} finally {
			closeListenersLock.writeLock().unlock();
		}
	}

	/**
	 * Notify all the tab listeners that this specific tab was selected to close.
	 *
	 * @param tabToClose
	 */
	public final void fireTabClosed(int tabToClose) {
		closeListenersLock.readLock().lock();
		try {
			if (this.closeListeners != null) {
				for (TabClosedListener listener : this.closeListeners) {
					listener.onTabClosed(tabToClose);
				}
			}
		} finally {
			closeListenersLock.readLock().unlock();
		}
	}

}