/*
 * @(#)SessionGUI.java
 * Copyright:    Copyright (c) 2001 - 2004
 * @author Kenneth J. Pouncey
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
package org.tn5250j;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.tn5250j.event.EmulatorActionEvent;
import org.tn5250j.event.EmulatorActionListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionListener;
import org.tn5250j.framework.tn5250.Rect;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.gui.ConfirmTabCloseDialog;
import org.tn5250j.keyboard.KeyboardHandler;
import org.tn5250j.mailtools.SendEMailDialog;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.Macronizer;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

/**
 * A host GUI session
 * (Hint: old name was SessionGUI)
 */
public class SessionPanel extends JPanel implements ComponentListener,
ActionListener,
RubberBandCanvasIF,
SessionConfigListener,
SessionListener {

	private static final long serialVersionUID = 1L;

	private boolean firstScreen;
	private char[] signonSave;

	private BorderLayout borderLayout1 = new BorderLayout();
	private Screen5250 screen;
	protected Session5250 session;
	private GuiGraphicBuffer guiGraBuf;
	protected TNRubberBand rubberband;
	private JPanel s = new JPanel();
	private KeyPad keyPad = new KeyPad();
	private String newMacName;
	private Vector<SessionJumpListener> listeners = null;
	private Vector<EmulatorActionListener> actionListeners = null;
	private SessionJumpEvent jumpEvent;
	private boolean macroRunning;
	private boolean stopMacro;
	private boolean doubleClick;
	protected SessionConfig sesConfig;
	protected KeyboardHandler keyHandler;
	protected final SessionScroller scroller = new SessionScroller();

	private final TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

	public SessionPanel (Session5250 session) {
		//Construct the frame
		//, SessionConfig config) {

		this.session = session;

		sesConfig = session.getConfiguration();

		try  {
			jbInit();
		}
		catch(Exception e) {
			log.warn("Error in constructor: "+e.getMessage());
		}

		session.getConfiguration().addSessionConfigListener(this);
		session.addSessionListener(this);
	}

	//Component initialization
	private void jbInit() throws Exception  {
		this.setLayout(borderLayout1);

		//	      this.setOpaque(false);
		//	      setDoubleBuffered(true);
		s.setOpaque(false);
		s.setDoubleBuffered(false);

		session.setGUI(this);

		//	      screen = new Screen5250(this,sesConfig);
		screen = session.getScreen();

		this.addComponentListener(this);

		if (guiGraBuf == null) {
			checkOffScreenImage();
		}

		setRubberBand(new TNRubberBand(this));
		keyHandler = KeyboardHandler.getKeyboardHandlerInstance(session);

		if (!sesConfig.isPropertyExists("width") ||
				!sesConfig.isPropertyExists("height"))
			// set the initialize size
			this.setSize(guiGraBuf.getPreferredSize());
		else {

			this.setSize(sesConfig.getIntegerProperty("width"),
					sesConfig.getIntegerProperty("height"));
		}

		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				/** @todo check for popup trigger on linux
				 *
				 */
				//	            if (e.isPopupTrigger()) {
				// using SwingUtilities because popuptrigger does not work on linux
				if (SwingUtilities.isRightMouseButton(e)) {
					actionPopup(e);
				}

			}
			@Override
			public void mouseReleased(MouseEvent e) {
				//	            System.out.println("Mouse Released");

			}

			@Override
			public void mouseClicked(MouseEvent e) {

				if (SwingUtilities.isRightMouseButton(e)) {
					return;
				}

				if (e.getClickCount() == 2 & doubleClick) {

					screen.sendKeys("[enter]");
				}
				else {
					int pos = guiGraBuf.getPosFromView(e.getX(), e.getY());
					if (log.isDebugEnabled()) {
						log.debug((screen.getRow(pos)) + "," + (screen.getCol(pos)));
						log.debug(e.getX() + "," + e.getY() + "," + guiGraBuf.columnWidth + ","
								+ guiGraBuf.rowHeight);
					}

					boolean moved = screen.moveCursor(pos);
					// this is a note to not execute this code here when we
					// implement the remain after edit function option.
					if (moved) {
						if (rubberband.isAreaSelected()) {
							rubberband.reset();
						}
						screen.repaintScreen();
					}
					getFocusForMe();
				}
			}

		});

		if (sesConfig.getStringProperty("mouseWheel").equals("Yes")) {
			scroller.addMouseWheelListener(this);
		}

		log.debug("Initializing macros");
		Macronizer.init();

		keyPad.addActionListener(this);
		if (sesConfig.getStringProperty("keypad").equals("Yes"))
			keyPad.setVisible(true);
		else
			keyPad.setVisible(false);

		// Warning do not change the the order of the adding of keypad and
		//    the screen.  This will cause resizing problems because it will
		//    resize the screen first and during the resize we need to calculate
		//    the bouding area based on the height of the keyPad.
		//    See resizeMe() and getDrawingBounds()
		this.add(keyPad,BorderLayout.SOUTH);
		this.add(s,BorderLayout.CENTER);

		this.requestFocus();
		jumpEvent = new SessionJumpEvent(this);


		// check if double click sends enter
		if (sesConfig.getStringProperty("doubleClick").equals("Yes"))
			doubleClick = true;
		else
			doubleClick = false;

	}

	public void setRunningHeadless(boolean headless) {
		if (headless) {
			screen.getOIA().removeOIAListener(guiGraBuf);
			screen.removeScreenListener(guiGraBuf);
		}
		else {
			screen.getOIA().addOIAListener(guiGraBuf);
			screen.addScreenListener(guiGraBuf);

		}

	}

	@Override
	public void processKeyEvent(KeyEvent evt) {

		keyHandler.processKeyEvent(evt);

		if(!evt.isConsumed())
			super.processKeyEvent(evt);
	}

	public void sendScreenEMail() {
		new SendEMailDialog((JFrame)SwingUtilities.getRoot(this),this);
	}

	/**
	 * This routine allows areas to be bounded by using the keyboard
	 *
	 * @param ke
	 * @param last
	 */
	public void doKeyBoundArea(KeyEvent ke,String last) {

		Point p = new Point();

		// If there is not area selected then we send to the previous position
		// of the cursor because the cursor position has already been updated
		// to the current position.
		//
		// The getPointFromRowCol is 0,0 based so we will take the current row
		// and column and make these calculations ourselves to be passed
		if (!rubberband.isAreaSelected()) {

			// mark left we will mark the column to the right of where the cursor
			// is now.
			if (last.equals("[markleft]"))
				guiGraBuf.getPointFromRowCol(screen.getCurrentRow() - 1,
						screen.getCurrentCol() + 1,
						p);
			// mark right will mark the current position to the left of the
			// current cursor position
			if (last.equals("[markright]"))
				guiGraBuf.getPointFromRowCol(screen.getCurrentRow() - 1,
						screen.getCurrentCol()-2,
						p);


			if (last.equals("[markup]"))
				guiGraBuf.getPointFromRowCol(screen.getCurrentRow() + 1,
						screen.getCurrentCol() - 1,
						p);
			// mark down will mark the current position minus the current
			// row.
			if (last.equals("[markdown]"))
				guiGraBuf.getPointFromRowCol(screen.getCurrentRow() - 2,
						screen.getCurrentCol() - 1,
						p);
			MouseEvent me = new MouseEvent(this,
					MouseEvent.MOUSE_PRESSED,
					System.currentTimeMillis(),
					InputEvent.BUTTON1_MASK,
					p.x,p.y,
					1,false);
			dispatchEvent(me);

		}

		guiGraBuf.getPointFromRowCol(screen.getCurrentRow() - 1,
				screen.getCurrentCol() - 1,
				p);
		//	      rubberband.getCanvas().translateEnd(p);
		MouseEvent me = new MouseEvent(this,
				MouseEvent.MOUSE_DRAGGED,
				System.currentTimeMillis(),
				InputEvent.BUTTON1_MASK,
				p.x,p.y,
				1,false);
		dispatchEvent(me);

	}


	/**
	 * @param reallyclose TRUE if session/tab should be closed;
	 *                    FALSE, if only ask for confirmation
	 * @return True if closed; False if still open
	 */
	public boolean confirmCloseSession(boolean reallyclose) {
		// regular, only ask on connected sessions
		boolean close = !isConnected() || confirmTabClose();
		if (close) {
			// special case, no SignonScreen than confirm signing off
			close = isOnSignOnScreen() || confirmSignOffClose();
		}
		if (close && reallyclose) {
			fireEmulatorAction(EmulatorActionEvent.CLOSE_SESSION);
		}
		return close;
	}

	/**
	 * Asks the user to confirm tab close,
	 * only if configured (option 'confirm tab close')
	 *
	 * @param sesConfig
	 * @return true if tab should be closed, false if not
	 */
	private boolean confirmTabClose() {
		boolean result = true;
		if (session.getConfiguration().isPropertyExists("confirmTabClose")) {
			this.requestFocus();
			final ConfirmTabCloseDialog tabclsdlg = new ConfirmTabCloseDialog(this);
			if(session.getConfiguration().getStringProperty("confirmTabClose").equals("Yes")) {
				if(!tabclsdlg.show()) {
					result = false;
				}
			}
		}
		return result;
	}

	/**
	 * Check is the parameter to confirm that the Sign On screen is the current
	 * screen.  If it is then we check against the saved Signon Screen in memory
	 * and take the appropriate action.
	 *
	 * @return whether or not the signon on screen is the current screen
	 */
	private boolean confirmSignOffClose() {

		if (sesConfig.isPropertyExists("confirmSignoff") &&
				sesConfig.getStringProperty("confirmSignoff").equals("Yes")) {
			this.requestFocus();
			int result = JOptionPane.showConfirmDialog(
					this.getParent(),            // the parent that the dialog blocks
					LangTool.getString("messages.signOff"),  // the dialog message array
					LangTool.getString("cs.title"),    // the title of the dialog window
					JOptionPane.CANCEL_OPTION        // option type
			);

			if (result == 0) {
				return true;
			}

			return false;
		}
		return true;
	}

	public void getFocusForMe() {
		this.grabFocus();
	}

	@Override
	public boolean isFocusTraversable () {
		return true;
	}

	// Override to inform focus manager that component is managing focus changes.
	//    This is to capture the tab and shift+tab keys.
	@Override
	public boolean isManagingFocus() { return true; }

	public JPanel getDrawingCanvas() {

		return s;

	}

	@Override
	public void actionPerformed(ActionEvent actionevent) {

		Object obj = actionevent.getSource();
		String ac = ((JButton)obj).getActionCommand();

		if (ac.equals("NXTPAD"))
			keyPad.nextPad();
		else
			screen.sendKeys(ac);


		getFocusForMe();

	}

	/**
	 * Update the configuration settings
	 * @param pce
	 */
	@Override
	public void onConfigChanged(SessionConfigEvent pce) {

		String pn = pce.getPropertyName();

		if (pn.equals("keypad")) {
			if (((String)pce.getNewValue()).equals("Yes")) {
				keyPad.setVisible(true);
			}
			else {
				keyPad.setVisible(false);
			}
			this.validate();
		}

		if (pn.equals("doubleClick")) {
			if (((String)pce.getNewValue()).equals("Yes")) {
				doubleClick = true;
			}
			else {
				doubleClick = false;
			}
		}

		if (pn.equals("mouseWheel")) {
			if (((String)pce.getNewValue()).equals("Yes")) {
				scroller.addMouseWheelListener(this);
			}
			else {
				scroller.removeMouseWheelListener(this);
			}
		}

		resizeMe();
		repaint();

	}

	public tnvt getVT() {

		return session.getVT();

	}

	public void toggleDebug() {
		session.getVT().toggleDebug();
	}

	public void startNewSession() {
		fireEmulatorAction(EmulatorActionEvent.START_NEW_SESSION);
	}

	public void startDuplicateSession() {
		fireEmulatorAction(EmulatorActionEvent.START_DUPLICATE);
	}

	public void sendAidKey(int whichOne) {

		session.getVT().sendAidKey(whichOne);
	}

	/**
	 * Toggles connection (connect or disconnect)
	 */
	public void toggleConnection() {

		if (isConnected()) {
			// special case, no SignonScreen than confirm signing off
			boolean disconnect = confirmTabClose() && (isOnSignOnScreen() || confirmSignOffClose());
			if (disconnect) {
				session.getVT().disconnect();
			}
		} else {
			// lets set this puppy up to connect within its own thread
			Runnable connectIt = new Runnable() {
				@Override
				public void run() {
					session.getVT().connect();
				}

			};

			// now lets set it to connect within its own daemon thread
			//    this seems to work better and is more responsive than using
			//    swingutilities's invokelater
			Thread ct = new Thread(connectIt);
			ct.setDaemon(true);
			ct.start();

		}

	}

	public void nextSession() {

		fireSessionJump(TN5250jConstants.JUMP_NEXT);

	}

	public void prevSession() {

		fireSessionJump(TN5250jConstants.JUMP_PREVIOUS);

	}

	/**
	 * Notify all registered listeners of the onSessionJump event.
	 *
	 * @param dir  The direction to jump.
	 */
	protected void fireSessionJump(int dir) {

		if (listeners != null) {
			int size = listeners.size();
			for (int i = 0; i < size; i++) {
				SessionJumpListener target =
					listeners.elementAt(i);
				jumpEvent.setJumpDirection(dir);
				target.onSessionJump(jumpEvent);
			}
		}
	}

	/**
	 * Notify all registered listeners of the onEmulatorAction event.
	 *
	 * @param action  The action to be performed.
	 */
	protected void fireEmulatorAction(int action) {

		if (actionListeners != null) {
			int size = actionListeners.size();
			for (int i = 0; i < size; i++) {
				EmulatorActionListener target =	actionListeners.elementAt(i);
				EmulatorActionEvent sae = new EmulatorActionEvent(this);
				sae.setAction(action);
				target.onEmulatorAction(sae);
			}
		}
	}

	public boolean isMacroRunning() {

		return macroRunning;
	}

	public boolean isStopMacroRequested() {

		return stopMacro;
	}

	public boolean isSessionRecording() {

		return keyHandler.isRecording();
	}

	public void setMacroRunning(boolean mr) {
		macroRunning = mr;
		if (macroRunning)
			screen.getOIA().setScriptActive(true);
		else
			screen.getOIA().setScriptActive(false);

		stopMacro = !macroRunning;
	}

	public void setStopMacroRequested () {
		setMacroRunning(false);
	}

	public void closeDown() {

		sesConfig.saveSessionProps(getParent());
		if (session.getVT() != null) session.getVT().disconnect();
		// Added by Luc to fix a memory leak. The keyHandler was still receiving
		//   events even though nothing was really attached.
		keyHandler.sessionClosed(this);
		keyHandler = null;

	}

	/**
	 * Show the session attributes screen for modification of the attribute/
	 * settings of the session.
	 *
	 */
	public void actionAttributes() {

		SessionAttributes sa = new SessionAttributes((Frame)SwingUtilities.getRoot(this),
				sesConfig);
		sa.showIt();

		getFocusForMe();
		sa = null;
	}

	private void actionPopup (MouseEvent me) {

		new SessionPopup(this,me);


	}

	public void actionSpool() {

		try {
			org.tn5250j.spoolfile.SpoolExporter spooler =
				new org.tn5250j.spoolfile.SpoolExporter(session.getVT(), this);
			spooler.setVisible(true);
		}
		catch (NoClassDefFoundError ncdfe) {
			JOptionPane.showMessageDialog(this,
					LangTool.getString("messages.noAS400Toolbox"),
					"Error",
					JOptionPane.ERROR_MESSAGE,null);
		}

	}

	public void executeMacro(ActionEvent ae) {

		executeMacro(ae.getActionCommand());

	}

	public void executeMacro(String macro) {

		Macronizer.invoke(macro,this);

	}

	protected void stopRecordingMe() {
		if (keyHandler.getRecordBuffer().length() > 0) {
			Macronizer.setMacro(newMacName,keyHandler.getRecordBuffer());
			log.debug(keyHandler.getRecordBuffer());
		}

		keyHandler.stopRecording();
	}

	protected void startRecordingMe() {

		String macName = JOptionPane.showInputDialog(null,
				LangTool.getString("macro.message"),
				LangTool.getString("macro.title"),
				JOptionPane.PLAIN_MESSAGE);
		if (macName != null) {
			macName = macName.trim();
			if (macName.length() > 0) {
				log.info(macName);
				newMacName = macName;
				keyHandler.startRecording();
			}
		}
	}


	/* default */ void resizeMe() {
		Rectangle r = getDrawingBounds();
		if (guiGraBuf != null) {
			guiGraBuf.resizeScreenArea(r.width, r.height);
		}
		screen.repaintScreen();
		Graphics g = getGraphics();
		if (g != null) {
			g.setClip(0,0,this.getWidth(),this.getHeight());
		}
		repaint(0,0,getWidth(),getHeight());
	}

	public Rectangle getDrawingBounds() {

		Rectangle r = this.getBounds();
		if (keyPad != null && keyPad.isVisible())
			//	         r.height -= (int)(keyPad.getHeight() * 1.25);
			r.height -= (keyPad.getHeight());

		r.setSize(r.width,r.height);

		return r;

	}

	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {

		resizeMe();
	}

	@Override
	public void componentShown(ComponentEvent e) {


	}

	@Override
	protected void paintComponent(Graphics g) {
		log.debug("paint from screen");

		if (guiGraBuf == null) {
			checkOffScreenImage();
		}
		//	      screen.paintComponent3(g);

		Graphics2D g2 = (Graphics2D) g;
		if (rubberband.isAreaSelected() && !rubberband.isDragging()) {
			rubberband.erase();
			//   //         rubberband.draw();
		}

		//Rectangle r = g.getClipBounds();

		g2.setColor(guiGraBuf.colorBg);
		g2.fillRect(0, 0, getWidth(), getHeight());

		guiGraBuf.drawImageBuffer(g2);

		if (rubberband.isAreaSelected() && !rubberband.isDragging()) {
			//	         rubberband.erase();
			rubberband.draw();
		}

		//	      keyPad.repaint();

	}

	@Override
	public void update(Graphics g) {
		log.info("update paint from gui");
		paint(g);

	}

	public boolean isHotSpots() {
		return guiGraBuf.hotSpots;
	}

	public void toggleHotSpots() {
		guiGraBuf.hotSpots = !guiGraBuf.hotSpots;
	}

	/**
	 * @todo: Change to be mnemonic key.
	 *
	 * This toggles the ruler line.
	 *
	 *
	 */
	public void crossHair() {
		screen.setCursorActive(false);
		guiGraBuf.crossHair++;
		if (guiGraBuf.crossHair > 3)
			guiGraBuf.crossHair = 0;
		screen.setCursorActive(true);
	}

	/**
	 *
	 * This routine will make sure we have something to draw on
	 *
	 */
	private void checkOffScreenImage() {

		// do we have something already?
		if (guiGraBuf == null) {

			guiGraBuf = new GuiGraphicBuffer(screen,this,sesConfig);

			//				if (antialiased) {
			//					bi.setUseAntialias(true);
			//				}

			// allocate a buffer Image with appropriate size
			guiGraBuf.getImageBuffer(0, 0);

			// fill in the areas
			//	            tArea = new Rectangle2D.Float(0, 0, 0, 0);
			//	            cArea = new Rectangle2D.Float(0, 0, 0, 0);
			//	            aArea = new Rectangle2D.Float(0, 0, 0, 0);
			//	            sArea = new Rectangle2D.Float(0, 0, 0, 0);
			//
			//	            // Draw Operator Information Area
			//	            drawOIA();
		}

	}

	/**
	 *
	 * Copy & Paste start code
	 *
	 */
	public final void actionCopy() {
		final Rect area = getBoundingArea();
		rubberband.reset();
		screen.repaintScreen();
		final String textcontent = screen.copyText(area);
		Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
		StringSelection contents = new StringSelection(textcontent);
		cb.setContents(contents, null);
	}

	/**
	 * Sum them
	 *
	 * @param which formatting option to use
	 * @return vector string of numeric values
	 */
	protected final Vector<Double> sumThem(boolean which) {
		log.debug("Summing");
		return screen.sumThem(which, getBoundingArea());
	}

	/**
	 *
	 * This routine is responsible for setting up a PrinterJob on this component
	 * and initiating the print session.
	 *
	 */
	public final void printMe() {

		Thread printerThread = new PrinterThread(screen, guiGraBuf.font, screen.getColumns(),
				screen.getRows(), Color.black, true, this);

		printerThread.start();

	}

	/**
	 * Add a SessionJumpListener to the listener list.
	 *
	 * @param listener  The SessionListener to be added
	 */
	public synchronized void addSessionJumpListener(SessionJumpListener listener) {

		if (listeners == null) {
			listeners = new java.util.Vector<SessionJumpListener>(3);
		}
		listeners.addElement(listener);

	}

	/**
	 * Remove a SessionJumpListener from the listener list.
	 *
	 * @param listener  The SessionJumpListener to be removed
	 */
	public synchronized void removeSessionJumpListener(SessionJumpListener listener) {
		if (listeners == null) {
			return;
		}
		listeners.removeElement(listener);

	}

	/**
	 * Add a EmulatorActionListener to the listener list.
	 *
	 * @param listener  The EmulatorActionListener to be added
	 */
	public synchronized void addEmulatorActionListener(EmulatorActionListener listener) {

		if (actionListeners == null) {
			actionListeners = new java.util.Vector<EmulatorActionListener>(3);
		}
		actionListeners.addElement(listener);

	}

	/**
	 * Remove a EmulatorActionListener from the listener list.
	 *
	 * @param listener  The EmulatorActionListener to be removed
	 */
	public synchronized void removeEmulatorActionListener(EmulatorActionListener listener) {
		if (actionListeners == null) {
			return;
		}
		actionListeners.removeElement(listener);

	}

	/**
	 *
	 * RubberBanding start code
	 *
	 */

	/**
	 * Returns a pointer to the graphics area that we can draw on
	 *
	 */
	@Override
	public Graphics getDrawingGraphics(){

		return guiGraBuf.getDrawingArea();
	}

	protected final void setRubberBand(TNRubberBand newValue) {

		rubberband = newValue;

	}

	public Rect getBoundingArea() {
		Rectangle awt_rect = new Rectangle();
		guiGraBuf.getBoundingArea(awt_rect);
		Rect result = new Rect();
		result.setBounds(awt_rect.x, awt_rect.y, awt_rect.width, awt_rect.height);
		return result;
	}

	@Override
	public Point translateStart(Point start) {
		return guiGraBuf.translateStart(start);
	}

	@Override
	public Point translateEnd(Point end) {
		return guiGraBuf.translateEnd(end);
	}
	public int getPosFromView(int x, int y) {
		return guiGraBuf.getPosFromView(x,y);
	}

	public void getBoundingArea(Rectangle bounds) {
		guiGraBuf.getBoundingArea(bounds);
	}

	@Override
	public void areaBounded(RubberBand band, int x1, int y1, int x2, int y2) {


		//	      repaint(x1,y1,x2-1,y2-1);
		repaint();
		if (log.isDebugEnabled()) {
			log.debug(" bound " + band.getEndPoint());
		}
	}

	@Override
	public boolean canDrawRubberBand(RubberBand b) {

		// before we get the row col we first have to translate the x,y point
		//   back to screen coordinates because we are translating the starting
		//   point to the 5250 screen coordinates
		//	      return !screen.isKeyboardLocked() && (screen.isWithinScreenArea(b.getStartPoint().x,b.getStartPoint().y));
		return guiGraBuf.isWithinScreenArea(b.getStartPoint().x,b.getStartPoint().y);

	}

	/**
	 *
	 * RubberBanding end code
	 *
	 */

	public class TNRubberBand extends RubberBand {

		public TNRubberBand(RubberBandCanvasIF c) {
			super(c);
		}

		@Override
		protected void drawBoundingShape(Graphics g, int startX, int startY, int width, int height) {
			g.drawRect(startX,startY,width,height);
		}

		protected Rectangle getBoundingArea() {

			Rectangle r = new Rectangle();
			getBoundingArea(r);
			return r;
		}

		protected void getBoundingArea(Rectangle r) {

			if((getEndPoint().x > getStartPoint().x) && (getEndPoint().y > getStartPoint().y)) {
				r.setBounds(getStartPoint().x,getStartPoint().y,getEndPoint().x-getStartPoint().x,getEndPoint().y-getStartPoint().y);
			}

			else if((getEndPoint().x < getStartPoint().x) && (getEndPoint().y < getStartPoint().y)) {
				r.setBounds(getEndPoint().x,getEndPoint().y,getStartPoint().x-getEndPoint().x,getStartPoint().y-getEndPoint().y);
			}

			else if((getEndPoint().x > getStartPoint().x) && (getEndPoint().y < getStartPoint().y))  {
				r.setBounds(getStartPoint().x,getEndPoint().y,getEndPoint().x-getStartPoint().x,getStartPoint().y-getEndPoint().y);
			}

			else if((getEndPoint().x < getStartPoint().x) && (getEndPoint().y > getStartPoint().y)) {
				r.setBounds(getEndPoint().x,getStartPoint().y,getStartPoint().x-getEndPoint().x,getEndPoint().y-getStartPoint().y);
			}

			//	         return r;
		}

		@Override
		protected Point getEndPoint() {

			if(this.endPoint == null) {
				Point p = new Point(0,0);
				guiGraBuf.getPointFromRowCol(0,0,p);
				setEndPoint(p);
			}
			return this.endPoint;
		}

		@Override
		protected Point getStartPoint() {

			if(this.startPoint == null) {
				Point p = new Point(0,0);
				guiGraBuf.getPointFromRowCol(0,0,p);
				setStartPoint(p);
			}
			return this.startPoint;

		}
	}


	public Session5250 getSession() {
		return this.session;
	}

	public void setSession(Session5250 session) {
		this.session = session;
	}


	public boolean isConnected() {

		return session.getVT() != null && session.getVT().isConnected();

	}

	public boolean isOnSignOnScreen() {

		// check to see if we should check.
		if (firstScreen) {

			char[] so = screen.getScreenAsChars();

			Rectangle region = this.sesConfig.getRectangleProperty("signOnRegion");

			int fromRow = region.x;
			int fromCol = region.y;
			int toRow = region.width;
			int toCol = region.height;

			// make sure we are within range.
			if (fromRow == 0)
				fromRow = 1;
			if (fromCol == 0)
				fromCol = 1;
			if (toRow == 0)
				toRow = 24;
			if (toCol == 0)
				toCol = 80;

			int pos = 0;

			for (int r = fromRow; r <= toRow; r++)
				for (int c =fromCol;c <= toCol; c++) {
					pos = screen.getPos(r - 1, c - 1);
					//               System.out.println(signonSave[pos]);
					if (signonSave[pos] != so[pos])
						return false;
				}
		}

		return true;
	}

	/**
	 * @return
	 * @see org.tn5250j.Session5250#getSessionName()
	 */
	public String getSessionName() {
		return session.getSessionName();
	}

	public String getAllocDeviceName() {
		if (session.getVT() != null) {
			return session.getVT().getAllocatedDeviceName();
		}
		return null;
	}

	public String getHostName() {
		if (session.getVT() != null) {
			return session.getVT().getHostName();
		}
		return session.getConnectionProperties().getProperty(TN5250jConstants.SESSION_HOST);
	}

	public Screen5250 getScreen() {

		return screen;

	}


	public void connect() {

		session.connect();
	}

	public void disconnect() {

		session.disconnect();
	}

	@Override
	public void onSessionChanged(SessionChangeEvent changeEvent) {

		switch (changeEvent.getState()) {
		case TN5250jConstants.STATE_CONNECTED:
			// first we check for the signon save or now
			if (!firstScreen) {
				firstScreen = true;
				signonSave = screen.getScreenAsChars();
				//               System.out.println("Signon saved");
			}

			// check for on connect macro
			String mac = sesConfig.getStringProperty("connectMacro");
			if (mac.length() > 0)
				executeMacro(mac);
			break;
		default:
			firstScreen = false;
			signonSave = null;
		}
	}

	/**
	 * Add a SessionListener to the listener list.
	 *
	 * @param listener  The SessionListener to be added
	 */
	public synchronized void addSessionListener(SessionListener listener) {

		session.addSessionListener(listener);

	}

	/**
	 * Remove a SessionListener from the listener list.
	 *
	 * @param listener  The SessionListener to be removed
	 */
	public synchronized void removeSessionListener(SessionListener listener) {
		session.removeSessionListener(listener);

	}

}