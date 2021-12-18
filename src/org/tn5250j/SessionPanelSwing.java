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

import static org.tn5250j.SessionConfig.CONFIG_KEYPAD_ENABLED;
import static org.tn5250j.SessionConfig.CONFIG_KEYPAD_FONT_SIZE;
import static org.tn5250j.SessionConfig.CONFIG_KEYPAD_MNEMONICS;
import static org.tn5250j.SessionConfig.YES;
import static org.tn5250j.keyboard.KeyMnemonic.ENTER;
import static org.tn5250j.keyboard.KeyMnemonic.PAGE_DOWN;
import static org.tn5250j.keyboard.KeyMnemonic.PAGE_UP;

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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Vector;

import javax.swing.JComponent;
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
import org.tn5250j.gui.SwingToFxUtils;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.keyboard.KeyMnemonicSerializer;
import org.tn5250j.keyboard.KeyboardHandler;
import org.tn5250j.mailtools.SendEMailDialog;
import org.tn5250j.sessionsettings.SessionSettings;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.Macronizer;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.geometry.Rectangle2D;

/**
 * A host GUI session
 * (Hint: old name was SessionGUI)
 */
public class SessionPanelSwing extends JPanel implements
        SessionGui, SessionConfigListener, SessionListener {

    private static final long serialVersionUID = 1L;

    private boolean firstScreen;
    private char[] signonSave;

    private Screen5250 screen;
    protected Session5250 session;
    private GuiGraphicBufferSwing guiGraBuf;
    protected RubberBandSwing rubberband;
    private KeypadPanel keypadPanel;
    private JComponent keypadPanelContainer;
    private String newMacName;
    private Vector<SessionJumpListener> sessionJumpListeners = null;
    private Vector<EmulatorActionListener> actionListeners = null;
    private boolean macroRunning;
    private boolean stopMacro;
    private boolean doubleClick;
    protected SessionConfig sesConfig;
    protected KeyboardHandler keyHandler;

    private final MouseWheelListener scroller = this::sessionPanelScrolled;

    private final TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

    public SessionPanelSwing(final Session5250 session) {
        this.keypadPanel = new KeypadPanel(session.getConfiguration().getConfig());
        this.session = session;

        sesConfig = session.getConfiguration();

        try {
            jbInit();
        } catch (final Exception e) {
            log.warn("Error in constructor: " + e.getMessage());
        }

        session.getConfiguration().addSessionConfigListener(this);
        session.addSessionListener(this);
    }

    //Component initialization
    private void jbInit() throws Exception {
        this.setLayout(new BorderLayout());
        session.setGUI(this);
        screen = session.getScreen();

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(final ComponentEvent e) {
                resizeMe();
            }
        });

        ensureGuiGraphicBufferInitialized();

        setRubberBand(new RubberBandSwing(this));
        keyHandler = KeyboardHandler.getKeyboardHandlerInstance(session);

        if (!sesConfig.isPropertyExists("width") ||
                !sesConfig.isPropertyExists("height"))
            // set the initialize size
            this.setSize(guiGraBuf.getPreferredSize());
        else {

            this.setSize(getIntegerProperty("width"),
                    getIntegerProperty("height"));
        }

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(final MouseEvent e) {
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
            public void mouseClicked(final MouseEvent e) {

                if (SwingUtilities.isRightMouseButton(e)) {
                    return;
                }

                if (e.getClickCount() == 2 & doubleClick) {
                    screen.sendKeys(ENTER);
                } else {
                    final int pos = guiGraBuf.getPosFromView(e.getX(), e.getY());
                    if (log.isDebugEnabled()) {
                        log.debug((screen.getRow(pos)) + "," + (screen.getCol(pos)));
                        log.debug(e.getX() + "," + e.getY() + "," + guiGraBuf.columnWidth + ","
                                + guiGraBuf.rowHeight);
                    }

                    final boolean moved = screen.moveCursor(pos);
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

        if (YES.equals(getStringProperty("mouseWheel"))) {
            removeMouseWheelListener(scroller);
            addMouseWheelListener(scroller);
        }

        log.debug("Initializing macros");
        Macronizer.init();

        keypadPanel.addActionListener(txt -> {
            screen.sendKeys(txt);
            getFocusForMe();
        });

        keypadPanelContainer = SwingToFxUtils.createSwingPanel(keypadPanel);
        keypadPanelContainer.setVisible(sesConfig.getConfig().isKeypadEnabled());
        this.add(keypadPanelContainer, BorderLayout.SOUTH);

        this.requestFocus();

        doubleClick = YES.equals(getStringProperty("doubleClick"));
    }

    @SuppressWarnings("deprecation")
    private String getStringProperty(final String name) {
        return sesConfig.getStringProperty(name);
    }

    @SuppressWarnings("deprecation")
    private int getIntegerProperty(final String name) {
        return sesConfig.getIntegerProperty(name);
    }

    public void setRunningHeadless(final boolean headless) {
        if (headless) {
            screen.getOIA().removeOIAListener(guiGraBuf);
            screen.removeScreenListener(guiGraBuf);
        } else {
            screen.getOIA().addOIAListener(guiGraBuf);
            screen.addScreenListener(guiGraBuf);
        }
    }

    @Override
    public void processKeyEvent(final KeyEvent evt) {

        keyHandler.processKeyEvent(evt);

        if (!evt.isConsumed())
            super.processKeyEvent(evt);
    }

    public void sessionPanelScrolled(final MouseWheelEvent e) {
        final int notches = e.getWheelRotation();
        if (notches < 0) {
            screen.sendKeys(PAGE_UP);
        } else {
            screen.sendKeys(PAGE_DOWN);
        }
    }

    @Override
    public void sendScreenEMail() {
        new SendEMailDialog((JFrame) SwingUtilities.getRoot(this), this);
    }

    /**
     * This routine allows areas to be bounded by using the keyboard
     *
     * @param ke
     * @param last
     */
    @Override
    public void doKeyBoundArea(final KeyEvent ke, final String last) {

        final Point p = new Point();

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
                        screen.getCurrentCol() - 2,
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
            final MouseEvent me = new MouseEvent(this,
                    MouseEvent.MOUSE_PRESSED,
                    System.currentTimeMillis(),
                    InputEvent.BUTTON1_MASK,
                    p.x, p.y,
                    1, false);
            dispatchEvent(me);

        }

        guiGraBuf.getPointFromRowCol(screen.getCurrentRow() - 1,
                screen.getCurrentCol() - 1,
                p);
        //	      rubberband.getCanvas().translateEnd(p);
        final MouseEvent me = new MouseEvent(this,
                MouseEvent.MOUSE_DRAGGED,
                System.currentTimeMillis(),
                InputEvent.BUTTON1_MASK,
                p.x, p.y,
                1, false);
        dispatchEvent(me);

    }


    /**
     * @param reallyclose TRUE if session/tab should be closed;
     *                    FALSE, if only ask for confirmation
     * @return True if closed; False if still open
     */
    @Override
    public boolean confirmCloseSession(final boolean reallyclose) {
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
     * @return true if tab should be closed, false if not
     */
    @SuppressWarnings("deprecation")
    private boolean confirmTabClose() {
        boolean result = true;
        if (session.getConfiguration().isPropertyExists("confirmTabClose")) {
            this.requestFocus();
            final ConfirmTabCloseDialog tabclsdlg = new ConfirmTabCloseDialog(this);
            if (YES.equals(session.getConfiguration().getStringProperty("confirmTabClose"))) {
                if (!tabclsdlg.show()) {
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
                YES.equals(getStringProperty("confirmSignoff"))) {
            this.requestFocus();
            final int result = JOptionPane.showConfirmDialog(
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

    @Override
    public void getFocusForMe() {
        this.grabFocus();
    }

    @Override
    public boolean isFocusTraversable() {
        return true;
    }

    // Override to inform focus manager that component is managing focus changes.
    //    This is to capture the tab and shift+tab keys.
    @Override
    public boolean isManagingFocus() {
        return true;
    }

    @Override
    public void onConfigChanged(final SessionConfigEvent configEvent) {
        final String configName = configEvent.getPropertyName();

        if (CONFIG_KEYPAD_ENABLED.equals(configName)) {
            keypadPanelContainer.setVisible(YES.equals(configEvent.getNewValue()));
            this.validate();
        }

        if (CONFIG_KEYPAD_MNEMONICS.equals(configName)) {
            keypadPanel.reInitializeButtons(new KeyMnemonicSerializer().deserialize((String) configEvent.getNewValue()));
        }

        if (CONFIG_KEYPAD_FONT_SIZE.equals(configName)) {
            keypadPanel.updateButtonFontSize(Float.parseFloat((String) configEvent.getNewValue()));
        }

        if ("doubleClick".equals(configName)) {
            doubleClick = YES.equals(configEvent.getNewValue());
        }

        if ("mouseWheel".equals(configName)) {
            removeMouseWheelListener(scroller);

            if (YES.equals(configEvent.getNewValue())) {
                addMouseWheelListener(scroller);
            }
        }

        resizeMe();
        repaint();
    }

    @Override
    public tnvt getVT() {

        return session.getVT();

    }

    @Override
    public void toggleDebug() {
        session.getVT().toggleDebug();
    }

    @Override
    public void startNewSession() {
        fireEmulatorAction(EmulatorActionEvent.START_NEW_SESSION);
    }

    @Override
    public void startDuplicateSession() {
        fireEmulatorAction(EmulatorActionEvent.START_DUPLICATE);
    }

    /**
     * Toggles connection (connect or disconnect)
     */
    @Override
    public void toggleConnection() {

        if (isConnected()) {
            // special case, no SignonScreen than confirm signing off
            final boolean disconnect = confirmTabClose() && (isOnSignOnScreen() || confirmSignOffClose());
            if (disconnect) {
                session.getVT().disconnect();
            }
        } else {
            // lets set this puppy up to connect within its own thread
            final Runnable connectIt = new Runnable() {
                @Override
                public void run() {
                    session.getVT().connect();
                }

            };

            // now lets set it to connect within its own daemon thread
            //    this seems to work better and is more responsive than using
            //    swingutilities's invokelater
            final Thread ct = new Thread(connectIt);
            ct.setDaemon(true);
            ct.start();

        }

    }

    @Override
    public void nextSession() {
        fireSessionJump(TN5250jConstants.JUMP_NEXT);
    }

    @Override
    public void prevSession() {
        fireSessionJump(TN5250jConstants.JUMP_PREVIOUS);
    }

    /**
     * Notify all registered listeners of the onSessionJump event.
     *
     * @param dir The direction to jump.
     */
    private void fireSessionJump(final int dir) {
        if (sessionJumpListeners != null) {
            final int size = sessionJumpListeners.size();
            final SessionJumpEvent jumpEvent = new SessionJumpEvent(this);
            jumpEvent.setJumpDirection(dir);
            for (int i = 0; i < size; i++) {
                final SessionJumpListener target = sessionJumpListeners.elementAt(i);
                target.onSessionJump(jumpEvent);
            }
        }
    }

    /**
     * Notify all registered listeners of the onEmulatorAction event.
     *
     * @param action The action to be performed.
     */
    protected void fireEmulatorAction(final int action) {

        if (actionListeners != null) {
            final int size = actionListeners.size();
            for (int i = 0; i < size; i++) {
                final EmulatorActionListener target = actionListeners.elementAt(i);
                final EmulatorActionEvent sae = new EmulatorActionEvent(this);
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

    @Override
    public void setMacroRunning(final boolean mr) {
        macroRunning = mr;
        if (macroRunning)
            screen.getOIA().setScriptActive(true);
        else
            screen.getOIA().setScriptActive(false);

        stopMacro = !macroRunning;
    }

    public void setStopMacroRequested() {
        setMacroRunning(false);
    }

    @Override
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
     */
    @Override
    public void actionAttributes() {
        new SessionSettings((Frame) SwingUtilities.getRoot(this), sesConfig).showIt();
        getFocusForMe();
    }

    private void actionPopup(final MouseEvent me) {
        new SessionPopup(this, me);
    }

    @Override
    public void actionSpool() {

        try {
            final org.tn5250j.spoolfile.SpoolExporter spooler =
                    new org.tn5250j.spoolfile.SpoolExporter(session.getVT(), this);
            spooler.setVisible(true);
        } catch (final NoClassDefFoundError ncdfe) {
            JOptionPane.showMessageDialog(this,
                    LangTool.getString("messages.noAS400Toolbox"),
                    "Error",
                    JOptionPane.ERROR_MESSAGE, null);
        }

    }

    public void executeMacro(final ActionEvent ae) {
        executeMacro(ae.getActionCommand());
    }

    @Override
    public void executeMacro(final String macro) {
        Macronizer.invoke(macro, this);
    }

    protected void stopRecordingMe() {
        if (keyHandler.getRecordBuffer().length() > 0) {
            Macronizer.setMacro(newMacName, keyHandler.getRecordBuffer());
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

    @Override
    public void resizeMe() {
        final Rectangle2D r = getDrawingBounds();
        if (guiGraBuf != null) {
            guiGraBuf.resizeScreenArea((int) r.getWidth(), (int) r.getHeight(), false);
        }
        screen.repaintScreen();
        final Graphics g = getGraphics();
        if (g != null) {
            g.setClip(0, 0, this.getWidth(), this.getHeight());
        }
        repaint(0, 0, getWidth(), getHeight());
    }

    @Override
    public Rectangle2D getDrawingBounds() {

        final Rectangle r = this.getBounds();
        if (keypadPanelContainer != null && keypadPanelContainer.isVisible())
            //	         r.height -= (int)(keyPad.getHeight() * 1.25);
            r.height -= (keypadPanelContainer.getHeight());

        r.setSize(r.width, r.height);

        return new Rectangle2D(r.x, r.y, r.width, r.height);
    }

    @Override
    protected void paintComponent(final Graphics g) {
        log.debug("paint from screen");

        ensureGuiGraphicBufferInitialized();

        final Graphics2D g2 = (Graphics2D) g;
        if (rubberband.isAreaSelected() && !rubberband.isDragging()) {
            rubberband.erase();
            //   //         rubberband.draw();
        }

        //Rectangle r = g.getClipBounds();

        g2.setColor(UiUtils.toAwtColor(guiGraBuf.colorBg));
        g2.fillRect(0, 0, getWidth(), getHeight());

        guiGraBuf.drawImageBuffer(g2);

        if (rubberband.isAreaSelected() && !rubberband.isDragging()) {
            //	         rubberband.erase();
            rubberband.draw();
        }

        //	      keyPad.repaint();

    }

    @Override
    public void update(final Graphics g) {
        log.info("update paint from gui");
        paint(g);

    }

    @Override
    public void toggleHotSpots() {
        guiGraBuf.hotSpots = !guiGraBuf.hotSpots;
    }

    /**
     * @todo: Change to be mnemonic key.
     * <p>
     * This toggles the ruler line.
     */
    @Override
    public void crossHair() {
        screen.setCursorActive(false);
        guiGraBuf.crossHair++;
        if (guiGraBuf.crossHair > 3)
            guiGraBuf.crossHair = 0;
        screen.setCursorActive(true);
    }

    private void ensureGuiGraphicBufferInitialized() {
        if (guiGraBuf == null) {
            guiGraBuf = new GuiGraphicBufferSwing(screen, this, sesConfig);
            setFont(guiGraBuf.getFont());
            setBackground(UiUtils.toAwtColor(guiGraBuf.getBackground()));

            guiGraBuf.getImageBuffer(0, 0);
        }
    }

    /**
     * Copy & Paste start code
     */
    @Override
    public final void actionCopy() {
        final Rect area = getBoundingArea();
        rubberband.reset();
        screen.repaintScreen();
        final String textcontent = screen.copyText(area);
        final Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
        final StringSelection contents = new StringSelection(textcontent);
        cb.setContents(contents, null);
    }

    /**
     * Sum them
     *
     * @param which formatting option to use
     * @return vector string of numeric values
     */
    protected final Vector<Double> sumThem(final boolean which) {
        log.debug("Summing");
        return screen.sumThem(which, getBoundingArea());
    }

    /**
     * This routine is responsible for setting up a PrinterJob on this component
     * and initiating the print session.
     */
    @Override
    public final void printMe() {

        final Thread printerThread = new PrinterThread(screen, guiGraBuf.font, screen.getColumns(),
                screen.getRows(), Color.black, true, this);

        printerThread.start();

    }

    /**
     * Add a SessionJumpListener to the listener list.
     *
     * @param listener The SessionListener to be added
     */
    @Override
    public synchronized void addSessionJumpListener(final SessionJumpListener listener) {

        if (sessionJumpListeners == null) {
            sessionJumpListeners = new java.util.Vector<SessionJumpListener>(3);
        }
        sessionJumpListeners.addElement(listener);

    }

    /**
     * Remove a SessionJumpListener from the listener list.
     *
     * @param listener The SessionJumpListener to be removed
     */
    @Override
    public synchronized void removeSessionJumpListener(final SessionJumpListener listener) {
        if (sessionJumpListeners == null) {
            return;
        }
        sessionJumpListeners.removeElement(listener);

    }

    /**
     * Add a EmulatorActionListener to the listener list.
     *
     * @param listener The EmulatorActionListener to be added
     */
    public synchronized void addEmulatorActionListener(final EmulatorActionListener listener) {

        if (actionListeners == null) {
            actionListeners = new java.util.Vector<EmulatorActionListener>(3);
        }
        actionListeners.addElement(listener);

    }

    /**
     * Remove a EmulatorActionListener from the listener list.
     *
     * @param listener The EmulatorActionListener to be removed
     */
    public synchronized void removeEmulatorActionListener(final EmulatorActionListener listener) {
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
     */
    public Graphics createDrawingGraphics() {
        return guiGraBuf.getDrawingArea();
    }

    protected final void setRubberBand(final RubberBandSwing newValue) {
        rubberband = newValue;
    }

    public Rect getBoundingArea() {
        final Rectangle awt_rect = new Rectangle();
        guiGraBuf.getBoundingArea(awt_rect);
        final Rect result = new Rect();
        result.setBounds(awt_rect.x, awt_rect.y, awt_rect.width, awt_rect.height);
        return result;
    }

    public Point translateStart(final Point start) {
        return guiGraBuf.translateStart(start);
    }

    public Point translateEnd(final Point end) {
        return guiGraBuf.translateEnd(end);
    }

    public int getPosFromView(final int x, final int y) {
        return guiGraBuf.getPosFromView(x, y);
    }

    public void getBoundingArea(final Rectangle bounds) {
        guiGraBuf.getBoundingArea(bounds);
    }

    public void areaBounded(final RubberBandSwing band, final int x1, final int y1, final int x2, final int y2) {


        //	      repaint(x1,y1,x2-1,y2-1);
        repaint();
        if (log.isDebugEnabled()) {
            log.debug(" bound " + band.getEndPoint());
        }
    }

    public boolean canDrawRubberBand(final RubberBandSwing b) {

        // before we get the row col we first have to translate the x,y point
        //   back to screen coordinates because we are translating the starting
        //   point to the 5250 screen coordinates
        //	      return !screen.isKeyboardLocked() && (screen.isWithinScreenArea(b.getStartPoint().x,b.getStartPoint().y));
        return guiGraBuf.isWithinScreenArea(b.getStartPoint().x, b.getStartPoint().y);

    }

    public Point getInitialPoint() {
        final Point p = new Point(0, 0);
        guiGraBuf.getPointFromRowCol(0, 0, p);
        return p;
    }

    @Override
    public Session5250 getSession() {
        return this.session;
    }

    public void setSession(final Session5250 session) {
        this.session = session;
    }


    @Override
    public boolean isConnected() {

        return session.getVT() != null && session.getVT().isConnected();

    }

    public boolean isOnSignOnScreen() {

        // check to see if we should check.
        if (firstScreen) {

            final char[] so = screen.getScreenAsChars();

            final Rectangle region = UiUtils.toAwtRectangle(this.sesConfig.getRectangleProperty("signOnRegion"));

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
                for (int c = fromCol; c <= toCol; c++) {
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
    @Override
    public String getSessionName() {
        return session.getSessionName();
    }

    @Override
    public String getAllocDeviceName() {
        if (session.getVT() != null) {
            return session.getVT().getAllocatedDeviceName();
        }
        return null;
    }

    @Override
    public String getHostName() {
        if (session.getVT() != null) {
            return session.getVT().getHostName();
        }
        return session.getConnectionProperties().getProperty(TN5250jConstants.SESSION_HOST);
    }

    @Override
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
    public void onSessionChanged(final SessionChangeEvent changeEvent) {

        switch (changeEvent.getState()) {
            case TN5250jConstants.STATE_CONNECTED:
                // first we check for the signon save or now
                if (!firstScreen) {
                    firstScreen = true;
                    signonSave = screen.getScreenAsChars();
                    //               System.out.println("Signon saved");
                }

                // check for on connect macro
                final String mac = getStringProperty("connectMacro");
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
     * @param listener The SessionListener to be added
     */
    @Override
    public synchronized void addSessionListener(final SessionListener listener) {

        session.addSessionListener(listener);

    }

    /**
     * Remove a SessionListener from the listener list.
     *
     * @param listener The SessionListener to be removed
     */
    @Override
    public synchronized void removeSessionListener(final SessionListener listener) {
        session.removeSessionListener(listener);

    }

    @Override
    public RubberBand getRubberband() {
        return rubberband;
    }
}
