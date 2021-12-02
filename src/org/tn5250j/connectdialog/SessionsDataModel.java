/*
 * Title: tn5250J
 * Copyright:   Copyright (c) 2016
 *
 * Description:
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
package org.tn5250j.connectdialog;

import java.util.function.Consumer;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 * Simple data model representing rows within the {@link SessionsTableModel}.
 */
public class SessionsDataModel {
    private final SimpleStringProperty name = new SimpleStringProperty(this, "name");
    private final SimpleStringProperty host = new SimpleStringProperty(this, "host");
    private final SimpleBooleanProperty deflt = new SimpleBooleanProperty(this, "deflt");

    private Consumer<String> defaultStateConsumer;

    public SessionsDataModel(final String name, final String host, final Boolean deflt) {
        this.name.set(name);
        this.host.set(host);
        setDeflt(deflt);

        this.deflt.addListener((src, old, value) -> defltChanged(value));
    }

    public Boolean getDeflt() {
        return deflt.getValue();
    }
    public void setDeflt(final Boolean deflt) {
        this.deflt.set(Boolean.TRUE.equals(deflt));
    }
    public String getName() {
        return name.get();
    }
    public String getHost() {
        return host.get();
    }

    SimpleStringProperty getNameProperty() {
        return name;
    }
    SimpleStringProperty getHostProperty() {
        return host;
    }
    SimpleBooleanProperty getDefltProperty() {
        return deflt;
    }

    public void setDefaultStateConsumer(final Consumer<String> defaultStateConsumer) {
        this.defaultStateConsumer = defaultStateConsumer;
    }

    private void defltChanged(final Boolean value) {
        if (defaultStateConsumer != null && value) {
            defaultStateConsumer.accept(getName());
        }
    }
}
