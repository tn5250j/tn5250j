/**
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001-2003
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.4
 * <p>
 * Description:
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
package org.tn5250j;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.tn5250j.event.SessionChangeEvent;
import org.tn5250j.event.SessionJumpEvent;
import org.tn5250j.event.SessionJumpListener;
import org.tn5250j.event.SessionListener;
import org.tn5250j.gui.GenericTn5250JFrame;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.WindowEvent;

/**
 * This is the main {@link javax.swing.JFrame}, which contains multiple tabs.
 *
 * @see GUIViewInterface
 */
public class Gui5250Frame extends GenericTn5250JFrame implements
        SessionListener, SessionJumpListener {

    private BorderPane contentPane;
    private TabPane sessTabbedPane = new TabPane();
    private boolean hideTabBar = false;
    private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());
    protected static int sequence;
    protected int frameSeq;
    private My5250 me;
    private final AtomicBoolean tabChangeEventsEnabled = new AtomicBoolean(true);

    //Construct the frame
    public Gui5250Frame(final My5250 m) {
        this.me = m;
        stage.setOnCloseRequest(e -> windowClosing(e));

        try {
            jbInit();
        } catch (final Exception e) {
            log.warn("Error during initializing!", e);
        }
    }

    public int getFrameSequence() {
        return frameSeq;
    }

    //Component initialization
    private void jbInit() throws Exception {
        contentPane = new BorderPane();
        stage.setScene(new Scene(contentPane));

        // update the frame sequences
        frameSeq = sequence++;

        sessTabbedPane.getSelectionModel().selectedItemProperty().addListener(
                (src, old, value) -> selectedTabChanged(value));
        sessTabbedPane.getTabs().addListener(this::tabsChanged);

        final Properties props = ConfigureFactory.getInstance().
                getProperties(ConfigureFactory.SESSIONS);

        if (props.getProperty("emul.hideTabBar", "no").equals("yes"))
            hideTabBar = true;

        if (!hideTabBar) {
            contentPane.setCenter(sessTabbedPane);
        }

        if (packFrame)
            stage.sizeToScene();
    }

    private void tabsChanged(final ListChangeListener.Change<? extends Tab> e) {
        if (tabChangeEventsEnabled.get()) {
            e.next();
            final List<? extends Tab> removed = e.getRemoved();
            for (final Tab tab : removed) {
                final SessionGui sesspanel = (SessionGui) tab.getUserData();
                sesspanel.confirmCloseSession(true);
            }
        }
    }

    private void selectedTabChanged(final Tab tab) {
        if (tab != null) {
            setSessionTitle((SessionGui) tab.getUserData());
        }
    }

    //Overridden so we can exit on System Close
    private void windowClosing(final WindowEvent e) {
        if (e.getEventType() == WindowEvent.WINDOW_CLOSE_REQUEST) {
            boolean close = true;

            final ObservableList<Tab> tabs = sessTabbedPane.getTabs();
            if (hideTabBar && tabs.size() == 0) {
                final SessionGui sesspanel = (SessionGui) contentPane.getCenter();
                close &= sesspanel.confirmCloseSession(false);
            } else {
                final int oldidx = sessTabbedPane.getSelectionModel().getSelectedIndex();
                for (final Tab tab : tabs) {
                    sessTabbedPane.getSelectionModel().select(tab);
                    updateSessionTitle();
                    final SessionGui sesspanel = (SessionGui) tab.getUserData();
                    close &= sesspanel.confirmCloseSession(false);
                }

                if (!close) {
                    // restore old selected index
                    sessTabbedPane.getSelectionModel().select(oldidx);
                    updateSessionTitle();
                    e.consume();
                }
            }

            if (close) {
                // process regular window closing ...
                Platform.runLater(() -> me.closingDown(this));
            }
        }
    }

    @Override
    public void onSessionJump(final SessionJumpEvent jumpEvent) {

        switch (jumpEvent.getJumpDirection()) {

            case TN5250jConstants.JUMP_PREVIOUS:
                prevSession();
                break;
            case TN5250jConstants.JUMP_NEXT:
                nextSession();
                break;
        }
    }

    private void nextSession() {
        final int index = sessTabbedPane.getSelectionModel().getSelectedIndex();
        final int tabCount = sessTabbedPane.getTabs().size();

        Platform.runLater(() -> {
            sessTabbedPane.getSelectionModel().select(index >= tabCount - 1 ? 0 : index);
            updateSessionTitle();
        });

    }

    private void prevSession() {
        final int index = sessTabbedPane.getSelectionModel().getSelectedIndex();
        final int tabCount = sessTabbedPane.getTabs().size();

        Platform.runLater(() -> {
            sessTabbedPane.getSelectionModel().select(index > 0 ? index : tabCount - 1);
            updateSessionTitle();
        });
    }

    /**
     * Sets the frame title to the same as the newly selected tab's title.
     *
     * @param session can be null, but then nothing happens ;-)
     */
    private void setSessionTitle(final SessionGui session) {
        if (session != null && session.isVtConnected()) {
            final String name = determineTabName(session);
            if (sequence - 1 > 0)
                stage.setTitle(name + " - tn5250j <" + sequence + ">");
            else
                stage.setTitle(name + " - tn5250j");
        } else {
            if (sequence - 1 > 0)
                stage.setTitle("tn5250j <" + sequence + ">");
            else
                stage.setTitle("tn5250j");
        }
    }

    /**
     * Determines the name, which is configured for one tab ({@link SessionPanelSwing})
     *
     * @param sessiongui
     * @return
     * @NotNull
     */
    private String determineTabName(final SessionGui sessiongui) {
        assert sessiongui != null;
        final String name;
        if (sessiongui.getSession().isUseSystemName()) {
            name = sessiongui.getSessionName();
        } else {
            if (sessiongui.getAllocDeviceName() != null) {
                name = sessiongui.getAllocDeviceName();
            } else {
                name = sessiongui.getHostName();
            }
        }
        return name;
    }

    /**
     * Sets the main frame title to the same as the current selected tab's title.
     * @see {@link #setSessionTitle(SessionPanelSwing)}
     */
    private void updateSessionTitle() {
        final SessionGui selectedComponent = (SessionGui) this.sessTabbedPane.getSelectionModel()
                .getSelectedItem().getUserData();
        setSessionTitle(selectedComponent);
    }

    /* (non-Javadoc)
     * @see org.tn5250j.interfaces.GUIViewInterface#addSessionView(java.lang.String, org.tn5250j.SessionGUI)
     */
    public void addSessionView(final String tabText, final SessionGui sesspanel) {
        final int tabCount = sessTabbedPane.getTabs().size();

        if (hideTabBar && tabCount == 0 && !(contentPane.getCenter() instanceof SessionGui)) {
            // put Session just in the main content window and don't create any tabs
            contentPane.setCenter((Node) sesspanel);
            sesspanel.addSessionListener(this);
            sesspanel.resizeMe();

            if (packFrame) {
                stage.sizeToScene();
            }

            ((Node) sesspanel).requestFocus();
            setSessionTitle(sesspanel);
        } else {

            if (hideTabBar && tabCount == 0) {
                // remove first component in the main window,
                // create first tab and put first session into first tab
                final SessionGui firstsesgui = (SessionGui) contentPane.getCenter();
                contentPane.getChildren().remove(firstsesgui);
                contentPane.setCenter(sessTabbedPane);
                createTabWithSessionContent(determineTabName(firstsesgui), firstsesgui, false);
            }

            createTabWithSessionContent(tabText, sesspanel, true);
        }
    }

    /**
     * @param tabText
     * @param sesgui
     * @param focus TRUE is the new tab should be focused, otherwise FALSE
     */
    private final void createTabWithSessionContent(final String tabText, final SessionGui sesgui, final boolean focus) {
        final Tab tab = new Tab(tabText, (Node) sesgui);
        tab.setUserData(sesgui);

        Platform.runLater(() -> {
            sessTabbedPane.getTabs().add(tab);

            // add listeners
            sesgui.addSessionListener(this);
            sesgui.addSessionJumpListener(this);
            if (focus) {
                sessTabbedPane.getSelectionModel().select(tab);
                sesgui.requestFocus();
            }
        });
    }

    /* (non-Javadoc)
     * @see org.tn5250j.interfaces.GUIViewInterface#removeSessionView(org.tn5250j.SessionGUI)
     */
    public void removeSessionView(final SessionGui targetSession) {
        UiUtils.runInFxAndWait(() -> {
            if (hideTabBar && sessTabbedPane.getTabs().isEmpty()) {
                contentPane.setCenter(null);
            } else {
                final Tab tab = getTabOf(targetSession);
                log.info("session found and closing down " + tab.getText());
                targetSession.removeSessionListener(this);
                targetSession.removeSessionJumpListener(this);

                tabChangeEventsEnabled.set(false);
                try {
                    sessTabbedPane.getTabs().remove(tab);
                } finally {
                    tabChangeEventsEnabled.set(true);
                }
            }
            return null;
        });
    }

    /* (non-Javadoc)
     * @see org.tn5250j.interfaces.GUIViewInterface#getSessionViewCount()
     */
    public int getSessionViewCount() {
        if (hideTabBar && sessTabbedPane.getTabs().isEmpty()) {
            return contentPane.getCenter() instanceof SessionGui ? 1 : 0;
        }
        return sessTabbedPane.getTabs().size();
    }

    /* (non-Javadoc)
     * @see org.tn5250j.interfaces.GUIViewInterface#getSessionAt(int)
     */
    public SessionGui getSessionAt(final int index) {
        final ObservableList<Tab> tabs = sessTabbedPane.getTabs();
        if (hideTabBar && tabs.isEmpty()) {
            if (contentPane.getCenter() instanceof SessionGui) {
                return (SessionGui) contentPane.getCenter();
            }
            return null;
        }

        if (index < 0 || index >= tabs.size()) {
            return null;
        }

        return (SessionGui) sessTabbedPane.getTabs().get(index).getUserData();
    }

    /* (non-Javadoc)
     * @see org.tn5250j.interfaces.GUIViewInterface#onSessionChanged(org.tn5250j.event.SessionChangeEvent)
     */
    @Override
    public void onSessionChanged(final SessionChangeEvent changeEvent) {

        final Session5250 ses5250 = (Session5250) changeEvent.getSource();
        final SessionGui sesgui = ses5250.getGUI();
        final Tab tab = getTabOf(sesgui);
        if (tab != null && changeEvent.getState() == TN5250jConstants.STATE_CONNECTED) {
            final String devname = sesgui.getAllocDeviceName();
            if (devname != null) {
                if (log.isDebugEnabled()) {
                    this.log.debug("SessionChangedEvent: " + changeEvent.getState() + " " + devname);
                }
                Platform.runLater(() -> {
                    tab.setText(determineTabName(sesgui));
                    updateSessionTitle();
                });
            }
        }
    }

    private Tab getTabOf(final SessionGui sesgui) {
        for (final Tab tab: this.sessTabbedPane.getTabs()) {
            if (sesgui == tab.getUserData()) {
                return tab;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.tn5250j.interfaces.GUIViewInterface#containsSession(org.tn5250j.SessionGUI)
     */
    public boolean containsSession(final SessionGui session) {
        return getTabOf(session) != null || contentPane.getCenter() instanceof SessionGui;
    }
}
