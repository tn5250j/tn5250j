/**
 *
 */
package org.tn5250j;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.tn5250j.event.EmulatorActionListener;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionListener;
import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.keyboard.KeyboardHandler;
import org.tn5250j.keyboard.actions.EmulatorAction;
import org.tn5250j.tools.GUIGraphicsUtils;

import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SessionGuiAdapter extends Pane implements SessionGui {

    private boolean macroRunning;
    private KeyboardHandler keyHandler;
    private final List<SessionJumpListener> sessionJumpListeners = new CopyOnWriteArrayList<>();
    private final List<EmulatorActionListener> actionListeners = new CopyOnWriteArrayList<>();
    private final Map<KeyCodeCombination, EmulatorAction> keyActions = new ConcurrentHashMap<>();
    private final Session5250 session;
    private final tnvt tvt;

    public SessionGuiAdapter() {
        session = DevTools.createSession();
        this.tvt = new tnvt(session, getScreen(), true, true);
        keyHandler = KeyboardHandler.getKeyboardHandlerInstance(this);
    }

    @Override
    public String getAllocDeviceName() {
        return null;
    }

    @Override
    public String getSessionName() {
        return "sess";
    }

    @Override
    public String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (final UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Session5250 getSession() {
        return session;
    }

    @Override
    public void closeDown() {
    }

    @Override
    public boolean isEnabled() {
        return !isDisabled();
    }

    @Override
    public void addSessionListener(final SessionListener l) {
    }

    @Override
    public void removeSessionListener(final SessionListener l) {
    }

    @Override
    public void resizeMe() {
    }

    @Override
    public boolean isVtConnected() {
        return false;
    }

    @Override
    public void actionAttributes() {
        System.out.println("SessionGuiAdapter.actionAttributes()");
    }

    @Override
    public boolean confirmCloseSession(final boolean b) {
        return false;
    }

    @Override
    public void actionCopy() {
        System.out.println("SessionGuiAdapter.actionCopy()");
    }

    @Override
    public void toggleDebug() {
        System.out.println("SessionGuiAdapter.toggleDebug()");
    }

    @Override
    public tnvt getVT() {
        return tvt;
    }

    @Override
    public void sendScreenEMail() {
        System.out.println("SessionGuiAdapter.sendScreenEMail()");
    }

    @Override
    public Screen5250 getScreen() {
        return session.getScreen();
    }

    @Override
    public void toggleHotSpots() {
        System.out.println("SessionGuiAdapter.toggleHotSpots()");
    }

    @Override
    public void startNewSession() {
        System.out.println("SessionGuiAdapter.startNewSession()");
    }

    @Override
    public void toggleConnection() {
        System.out.println("SessionGuiAdapter.toggleConnection()");
    }

    @Override
    public void nextSession() {
        System.out.println("SessionGuiAdapter.nextSession()");
    }

    @Override
    public void prevSession() {
        System.out.println("SessionGuiAdapter.prevSession()");
    }

    @Override
    public void printMe() {
        System.out.println("SessionGuiAdapter.printMe()");
    }

    @Override
    public void crossHair() {
        System.out.println("SessionGuiAdapter.crossHair()");
    }

    @Override
    public void setMacroRunning(final boolean b) {
        macroRunning = b;
    }

    @Override
    public void getFocusForMe() {
        requestFocus();
    }

    @Override
    public void startDuplicateSession() {
        System.out.println("SessionGuiAdapter.startDuplicateSession()");
    }

    @Override
    public void executeMacro(final String lastKeyStroke) {
        System.out.println("SessionGuiAdapter.executeMacro()");
    }

    @Override
    public void actionSpool() {
        System.out.println("SessionGuiAdapter.actionSpool()");
    }

    @Override
    public void doKeyBoundArea(final String lastKeyStroke) {
        System.out.println("Do key bound area: " + lastKeyStroke);
    }

    @Override
    public Dimension2D getDrawingSize() {
        final Rectangle2D area = getBoundingArea();
        return new Dimension2D(area.getWidth(), area.getHeight());
    }

    @Override
    public RubberBand getRubberband() {
        return null;
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
    public boolean isSessionRecording() {
        return keyHandler.isRecording();
    }

    @Override
    public void stopRecordingMe() {
        System.out.println("SessionGuiAdapter.stopRecordingMe()");
        keyHandler.stopRecording();
    }

    @Override
    public int getPosFromView(final double x, final double y) {
        return 0;
    }

    @Override
    public Rectangle2D getBoundingArea() {
        final Bounds area = getBoundsInLocal();
        return new Rectangle2D(0, 0, area.getWidth(), area.getHeight());
    }

    @Override
    public boolean isMacroRunning() {
        return macroRunning;
    }

    @Override
    public void setStopMacroRequested() {
        System.out.println("SessionGuiAdapter.setStopMacroRequested()");
    }

    @Override
    public void startRecordingMe() {
        System.out.println("SessionGuiAdapter.startRecordingMe()");
    }

    @Override
    public List<Double> sumThem(final boolean which) {
        System.out.println("SessionGuiAdapter.sumThem()");
        return new LinkedList<>();
    }

    @Override
    public void connect() {
        System.out.println("SessionGuiAdapter.connect()");
    }

    @Override
    public Point2D translateStart(final Point2D start) {
        return start;
    }

    @Override
    public Point2D translateEnd(final Point2D end) {
        return end;
    }

    @Override
    public KeyboardHandler getKeyHandler() {
        return keyHandler;
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

    public static Stage showWindowWithMe(final SessionGuiAdapter gui) {
        return showWindowWithMe(new Stage(), gui);
    }

    public static Stage showWindowWithMe(final Stage stage, final SessionGuiAdapter gui) {
        final Rectangle2D bounds = Screen.getPrimary().getBounds();

        final double w = bounds.getWidth() * 2 / 3;
        final double h = bounds.getHeight() * 2 / 3;
        stage.setWidth(w);
        stage.setHeight(h);
        stage.centerOnScreen();

        stage.getIcons().addAll(GUIGraphicsUtils.getApplicationIconsFx());
        stage.setScene(new Scene(gui));
        stage.setTitle("Demo");
        stage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, e -> System.exit(0));

        stage.show();
        return stage;
    }
    @Override
    public Window getWindow() {
        return getScene() == null ? null : getScene().getWindow();
    }
}
