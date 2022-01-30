package org.tn5250j.spoolfile;
/**
 * Title: SpoolExporter.java
 * Copyright:   Copyright (c) 2002
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.5
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

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class SpoolFilterPane extends TabPane {

    private UserTabPanel user;
    private OutputQueueTabPanel queue;
    private SpoolNameTabPanel spoolName;
    private UserDataTabPanel userData;

    public SpoolFilterPane() {
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        getStyleClass().add("etched-border");

        user = new UserTabPanel();
        queue = new OutputQueueTabPanel();
        spoolName = new SpoolNameTabPanel();
        userData = new UserDataTabPanel();

        this.addTab("User", user);
        this.addTab("Output Queue", queue);
        this.addTab("Spool Name", spoolName);
        this.addTab("User Data", userData);
    }

    private Tab addTab(final String title, final QueueFilterInterface content) {
        final Tab tab = new Tab(title);
        tab.setContent((Node) content);
        getTabs().add(tab);
        return tab;
    }

    public String getUser() {
        return user.getUser();
    }

    public void setUser(final String filter) {
        user.setUser(filter);
        setSelectedComponent(user);
    }

    public String getQueue() {
        return queue.getQueue();
    }

    public String getLibrary() {

        return queue.getLibrary();

    }

    public String getJobName() {
        return " ";
    }

    public String getJobUser() {
        return " ";

    }

    public String getJobNumber() {
        return " ";

    }

    @Override
    public String getUserData() {
        return userData.getUserData();

    }

    public void setUserData(final String filter) {

        userData.setUserData(filter);
        setSelectedComponent(userData);
    }

    public String getSpoolName() {
        return spoolName.getSpoolName();

    }

    public void setSpoolName(final String filter) {
        spoolName.setSpoolName(filter);
        setSelectedComponent(spoolName);
    }

    private void setSelectedComponent(final QueueFilterInterface filter) {
        for (final Tab tab : getTabs()) {
            if (tab.getContent() == filter) {
                getSelectionModel().select(tab);
                break;
            }
        }
    }

    /**
     * Reset the values in the current panel to default values
     */
    public void resetCurrent() {
        ((QueueFilterInterface) this.getSelectionModel().getSelectedItem().getContent()).reset();
    }

    /**
     * Reset the values in all filter panels to default values
     */
    public void resetAll() {
        for (final Tab tab : getTabs()) {
            final QueueFilterInterface filter = (QueueFilterInterface) tab.getContent();
            filter.reset();
        }
    }
}
