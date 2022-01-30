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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.tn5250j.event.EmulatorActionEvent;
import org.tn5250j.event.EmulatorActionListener;
import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.event.SessionConfigListener;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.gui.ResizablePane;
import org.tn5250j.gui.SwingToFxUtils;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.keyboard.KeyMnemonicSerializer;
import org.tn5250j.keyboard.KeyboardHandler;
import org.tn5250j.keyboard.actions.EmulatorAction;
import org.tn5250j.mailtools.SendEMailDialog;
import org.tn5250j.sessionsettings.SessionSettings;
import org.tn5250j.spoolfile.SpoolExporter;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.Macronizer;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Shape;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * A host GUI session
 * (Hint: old name was SessionGUI)
 */
public class SessionPanel extends BorderPane implements
        SessionGui, SessionConfigListener, SessionListener {
    private boolean firstScreen;
    private char[] signonSave;

    private Screen5250 screen;
    protected Session5250 session;
    private GuiGraphicBuffer guiGraBuf;
    private final RubberBand rubberband = new RubberBand(this);
    private KeypadPanel keypadPanel;
    private String newMacName;
    private final List<SessionJumpListener> sessionJumpListeners = new CopyOnWriteArrayList<>();
    private final List<EmulatorActionListener> actionListeners = new CopyOnWriteArrayList<>();
    private boolean macroRunning;
    private boolean stopMacro;
    private boolean doubleClick;
    protected SessionConfig sesConfig;
    protected KeyboardHandler keyHandler;

    private final EventHandler<ScrollEvent> scroller = this::sessionPanelScrolled;

    private final TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

    private final Canvas canvas = new Canvas();
    private final CompoundCursor cursor = new CompoundCursor();
    private final Map<KeyCodeCombination, EmulatorAction> keyActions = new ConcurrentHashMap<>();

    public SessionPanel(final Session5250 session) {
        this.keypadPanel = new KeypadPanel(session.getConfiguration().getConfig());
        this.session = session;
        this.keyHandler = KeyboardHandler.getKeyboardHandlerInstance(this);

        sesConfig = session.getConfiguration();

        try {
            jbInit();
        } catch (final Exception e) {
            e.printStackTrace();
            log.warn("Error in constructor: " + e.getMessage());
        }

        session.getConfiguration().addSessionConfigListener(this);
        session.addSessionListener(this);

        addEventHandler(KeyEvent.ANY, this::processKeyEvent);
    }

    //Component initialization
    private void jbInit() throws Exception {
        session.setGUI(this);
        screen = session.getScreen();

        final VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        setCenter(vbox);

        final HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        vbox.getChildren().add(hbox);

        final Pane container = createContainer(canvas);
        container.getChildren().add(rubberband.getSelectionComponent());
        for (final Shape comp : this.cursor.getComponents()) {
            container.getChildren().add(comp);
        }

        hbox.getChildren().add(container);

        widthProperty().addListener((src, old, value) -> resizeMe());
        heightProperty().addListener((src, old, value) -> resizeMe());

        installMouseListeners(container);
        rubberband.startListen(container);

        keyHandler = KeyboardHandler.getKeyboardHandlerInstance(this);

        guiGraBuf = new GuiGraphicBuffer(screen, this, sesConfig, canvas, cursor);
        UiUtils.setBackground(this, guiGraBuf.getBackground());

        final double width;
        final double height;
        if (!sesConfig.isPropertyExists("width") ||
                !sesConfig.isPropertyExists("height")) {
            // set the initialize size
            final Dimension2D buffPrefSize = guiGraBuf.getPreferredSize();
            width = buffPrefSize.getWidth();
            height = buffPrefSize.getHeight();
        } else {

            width = getIntegerProperty("width");
            height = getIntegerProperty("height");
        }
        guiGraBuf.resize(width, height);

        log.debug("Initializing macros");
        Macronizer.init();

        keypadPanel.addActionListener(txt -> {
            screen.sendKeys(txt);
            getFocusForMe();
        });

        keypadPanel.setVisible(sesConfig.getConfig().isKeypadEnabled());
        setBottom(keypadPanel);

        this.requestFocus();

        doubleClick = YES.equals(getStringProperty("doubleClick"));
        resizeMe();
    }

    private void installMouseListeners(final Pane container) {
        container.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                actionPopup(e.getScreenX(), e.getScreenY());
            }
        });
        container.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (!rubberband.isAreaSelected() && e.getButton() == MouseButton.PRIMARY) {
                if (e.getClickCount() == 2 & doubleClick) {
                    screen.sendKeys(ENTER);
                } else {
                    final int pos = guiGraBuf.getRowColFromPoint(e.getX(), e.getY());
                    if (log.isDebugEnabled()) {
                        log.debug((screen.getRow(pos)) + "," + (screen.getCol(pos)));
                        log.debug(e.getX() + "," + e.getY() + "," + guiGraBuf.columnWidth + ","
                                + guiGraBuf.rowHeight);
                    }

                    screen.moveCursor(pos);
                    getFocusForMe();
                }
            }
        });

        if (YES.equals(getStringProperty("mouseWheel"))) {
            container.setOnScroll(null);
            container.setOnScroll(scroller);
        }
    }

    private Pane createContainer(final Canvas canvas) {
        final ResizablePane pane = new ResizablePane();

        canvas.widthProperty().addListener(e -> pane.setWidth(canvas.getWidth()));
        canvas.heightProperty().addListener(e -> pane.setHeight(canvas.getHeight()));
        pane.getChildren().add(canvas);
        return pane;
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

    private void processKeyEvent(final KeyEvent evt) {
        keyHandler.processKeyEvent(evt);

        if (!evt.isConsumed()) {
            final EmulatorAction action = getKeyAction(evt);
            if (action != null) {
                action.handle(new ActionEvent(this, null));
                evt.consume();
            }
        }
    }

    @Override
    public void addKeyAction(final KeyCodeCombination ks, final EmulatorAction emulatorAction) {
        keyActions.put(ks, emulatorAction);
    }

    @Override
    public void clearKeyActions() {
        keyActions.clear();
    }

    @Override
    public Map<KeyCodeCombination, EmulatorAction> getKeyActions() {
        return keyActions;
    }

    @Override
    public EmulatorAction getKeyAction(final KeyEvent event) {
        for (final Map.Entry<KeyCodeCombination, EmulatorAction> e : keyActions.entrySet()) {
            if (e.getKey().match(event)) {
                return e.getValue();
            }
        }
        return null;
    }

    public void sessionPanelScrolled(final ScrollEvent e) {
        final double notches = e.getTotalDeltaY();
        if (notches < 0) {
            screen.sendKeys(PAGE_UP);
        } else if (notches > 0) {
            screen.sendKeys(PAGE_DOWN);
        }
    }

    @Override
    public void sendScreenEMail() {
        new SendEMailDialog(this);
    }

    /**
     * This routine allows areas to be bounded by using the keyboard
     * @param ke
     * @param last
     */
    @Override
    public void doKeyBoundArea(final String last) {

        Point2D p = Point2D.ZERO;

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
                p = guiGraBuf.getPointFromRowCol(screen.getCurrentRow() - 1,
                        screen.getCurrentCol() + 1);
            // mark right will mark the current position to the left of the
            // current cursor position
            if (last.equals("[markright]"))
                p = guiGraBuf.getPointFromRowCol(screen.getCurrentRow() - 1,
                        screen.getCurrentCol() - 2);

            if (last.equals("[markup]"))
                p = guiGraBuf.getPointFromRowCol(screen.getCurrentRow() + 1,
                        screen.getCurrentCol() - 1);
            // mark down will mark the current position minus the current
            // row.
            if (last.equals("[markdown]"))
                p = guiGraBuf.getPointFromRowCol(screen.getCurrentRow() - 2,
                        screen.getCurrentCol() - 1);

            dispatchEvent(MouseEvent.MOUSE_PRESSED, p.getX(), p.getY());
        }

        p = guiGraBuf.getPointFromRowCol(screen.getCurrentRow() - 1,
                screen.getCurrentCol() - 1);

        //	      rubberband.getCanvas().translateEnd(p);
        dispatchEvent(MouseEvent.MOUSE_DRAGGED, p.getX(), p.getY());
    }

    private void dispatchEvent(final EventType<MouseEvent> type, final double x, final double y) {
        final MouseEvent e = new MouseEvent(this, this, type, x, y, x, y, MouseButton.PRIMARY,
                1, false, false, false, false, true, false, false, true, false, false, null);
        Event.fireEvent(this, e);
    }

    /**
     * @param reallyclose TRUE if session/tab should be closed;
     *                    FALSE, if only ask for confirmation
     * @return True if closed; False if still open
     */
    @Override
    public boolean confirmCloseSession(final boolean reallyclose) {
        // regular, only ask on connected sessions
        boolean close = !isVtConnected() || confirmTabClose();
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
            if (YES.equals(session.getConfiguration().getStringProperty("confirmTabClose"))) {
                final Alert tabclsdlg = new Alert(AlertType.CONFIRMATION);
                tabclsdlg.setTitle(LangTool.getString("sa.confirmTabClose"));
                tabclsdlg.setContentText("Are you sure you want to close this tab?");

                tabclsdlg.getButtonTypes().add(ButtonType.CLOSE);
                tabclsdlg.getButtonTypes().add(ButtonType.CANCEL);

                tabclsdlg.showAndWait();
                if (tabclsdlg.getResult() != ButtonType.CLOSE) {
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

            final Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setTitle(LangTool.getString("cs.title"));
            alert.setContentText(LangTool.getString("messages.signOff"));
            alert.getButtonTypes().clear();
            alert.getButtonTypes().add(ButtonType.OK);
            alert.getButtonTypes().add(ButtonType.CANCEL);

            alert.initOwner(getScene().getWindow());

            alert.showAndWait();
            if (alert.getResult() != ButtonType.CANCEL) {
                return true;
            }

            return false;
        }
        return true;
    }

    @Override
    public void getFocusForMe() {
        this.requestFocus();
    }

    @Override
    public void onConfigChanged(final SessionConfigEvent configEvent) {
        final String configName = configEvent.getPropertyName();

        if (CONFIG_KEYPAD_ENABLED.equals(configName)) {
            keypadPanel.setVisible(YES.equals(configEvent.getNewValue()));
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
            removeEventHandler(ScrollEvent.SCROLL, scroller);

            if (YES.equals(configEvent.getNewValue())) {
                addEventHandler(ScrollEvent.SCROLL, scroller);
            }
        }

        resizeMe();
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

        if (isVtConnected()) {
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
            final SessionJumpEvent jumpEvent = new SessionJumpEvent(this);
            jumpEvent.setJumpDirection(dir);

            for (final SessionJumpListener l : sessionJumpListeners) {
                l.onSessionJump(jumpEvent);
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
            for (final EmulatorActionListener target : actionListeners) {
                final EmulatorActionEvent sae = new EmulatorActionEvent(this);
                sae.setAction(action);
                target.onEmulatorAction(sae);
            }
        }
    }

    @Override
    public boolean isMacroRunning() {

        return macroRunning;
    }

    public boolean isStopMacroRequested() {

        return stopMacro;
    }

    @Override
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

    @Override
    public void setStopMacroRequested() {
        setMacroRunning(false);
    }

    @Override
    public void closeDown() {

        sesConfig.saveSessionProps(getParent());
        if (session.getVT() != null) session.getVT().disconnect();
        // Added by Luc to fix a memory leak. The keyHandler was still receiving
        //   events even though nothing was really attached.
        keyHandler.sessionClosed();
        keyHandler = null;

    }

    /**
     * Show the session attributes screen for modification of the attribute/
     * settings of the session.
     */
    @Override
    public void actionAttributes() {
        new SessionSettings((Stage) getScene().getWindow(), sesConfig).showIt();
        getFocusForMe();
    }

    private void actionPopup(final double x, final double y) {
        new SessionPopup(this, (int) x, (int) y);
    }

    @Override
    public void actionSpool() {

        try {
            final SpoolExporter spooler = new SpoolExporter(session.getVT(), this);
            spooler.setVisible(true);
        } catch (final NoClassDefFoundError ncdfe) {
            final Alert tabclsdlg = new Alert(AlertType.ERROR);
            tabclsdlg.setTitle(LangTool.getString("sa.confirmTabClose"));
            tabclsdlg.setContentText(LangTool.getString("messages.noAS400Toolbox"));
        }
    }

    @Override
    public void executeMacro(final String macro) {
        Macronizer.invoke(macro, this);
    }

    @Override
    public void stopRecordingMe() {
        if (keyHandler.getRecordBuffer().length() > 0) {
            Macronizer.setMacro(newMacName, keyHandler.getRecordBuffer());
            log.debug(keyHandler.getRecordBuffer());
        }

        keyHandler.stopRecording();
    }

    @Override
    public void startRecordingMe() {
        final TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(LangTool.getString("macro.title"));
        dialog.setHeaderText(LangTool.getString("macro.message"));

        String macName = dialog.showAndWait().orElse(null);
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
        final Dimension2D r = getDrawingSize();
        if (guiGraBuf != null) {
            guiGraBuf.resizeScreenArea((int) r.getWidth(), (int) r.getHeight(), false);
        }
        screen.repaintScreen();
    }

    @Override
    public Dimension2D getDrawingSize() {
        final Bounds r = getBoundsInLocal();
        double height = r.getHeight();
        if (keypadPanel.isVisible())
            //	         r.height -= (int)(keyPad.getHeight() * 1.25);
            height -= (keypadPanel.getHeight());

        return new Dimension2D(r.getWidth(), height);
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
        cursor.shift();
    }

    /**
     * Copy & Paste start code
     */
    @Override
    public final void actionCopy() {
        final Rectangle2D area;
        if (rubberband.isAreaSelected()) {
            area = rubberband.getBoundingArea();
        } else {
            area = new Rectangle2D(0, 0, canvas.getWidth(),  canvas.getHeight());
        }

        rubberband.reset();
        final String textcontent = screen.copyText(this.guiGraBuf.getBoundingArea(area));

        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(textcontent);
        clipboard.setContent(content);
    }

    /**
     * Sum them
     *
     * @param which formatting option to use
     * @return vector string of numeric values
     */
    @Override
    public final List<Double> sumThem(final boolean which) {
        log.debug("Summing");
        return screen.sumThem(which, getBoundingArea());
    }

    /**
     * This routine is responsible for setting up a PrinterJob on this component
     * and initiating the print session.
     */
    @Override
    public final void printMe() {

        final Thread printerThread = new PrinterThread(screen, SwingToFxUtils.toAwtFont(guiGraBuf.font),
                screen.getColumns(), screen.getRows(), SwingToFxUtils.toAwtColor(Color.BLACK), true, this);

        printerThread.start();

    }

    /**
     * Add a SessionJumpListener to the listener list.
     *
     * @param listener The SessionListener to be added
     */
    @Override
    public synchronized void addSessionJumpListener(final SessionJumpListener listener) {
        sessionJumpListeners.add(listener);
    }

    /**
     * Remove a SessionJumpListener from the listener list.
     *
     * @param listener The SessionJumpListener to be removed
     */
    @Override
    public synchronized void removeSessionJumpListener(final SessionJumpListener listener) {
        sessionJumpListeners.remove(listener);
    }

    /**
     * Add a EmulatorActionListener to the listener list.
     *
     * @param listener The EmulatorActionListener to be added
     */
    @Override
    public synchronized void addEmulatorActionListener(final EmulatorActionListener listener) {
        actionListeners.remove(listener);

    }

    /**
     * Remove a EmulatorActionListener from the listener list.
     *
     * @param listener The EmulatorActionListener to be removed
     */
    public synchronized void removeEmulatorActionListener(final EmulatorActionListener listener) {
        actionListeners.add(listener);
    }

    @Override
    public Rectangle2D getBoundingArea() {
        return guiGraBuf.getBoundingArea();
    }

    @Override
    public Point2D translateStart(final Point2D start) {
        return guiGraBuf.translateStart(start.getX(), start.getY());
    }

    @Override
    public Point2D translateEnd(final Point2D end) {
        return guiGraBuf.translateEnd(end.getX(), end.getY());
    }

    @Override
    public int getPosFromView(final double x, final double y) {
        return guiGraBuf.getRowColFromPoint(x, y);
    }

    public Point2D getInitialPoint() {
        return guiGraBuf.getPointFromRowCol(0, 0);
    }

    @Override
    public Session5250 getSession() {
        return this.session;
    }

    public void setSession(final Session5250 session) {
        this.session = session;
    }

    @Override
    public boolean isVtConnected() {
        return session.getVT() != null && session.getVT().isConnected();
    }

    public boolean isOnSignOnScreen() {
        // check to see if we should check.
        if (firstScreen) {

            final char[] so = screen.getScreenAsChars();

            final Rectangle2D region = this.sesConfig.getRectangleProperty("signOnRegion");

            int fromRow = UiUtils.round(region.getMinX());
            int fromCol = UiUtils.round(region.getMinY());
            int toRow = UiUtils.round(region.getWidth());
            int toCol = UiUtils.round(region.getHeight());

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


    @Override
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

    @Override
    public boolean isEnabled() {
        return !isDisable();
    }

    @Override
    public void setDefaultCursor() {
        setCursor(Cursor.DEFAULT);
    }

    @Override
    public void setWaitCursor() {
        setCursor(Cursor.WAIT);
    }

    @Override
    public KeyboardHandler getKeyHandler() {
        return keyHandler;
    }

    @Override
    public Window getWindow() {
        return getScene().getWindow();
    }
}
