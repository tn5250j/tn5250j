/*
 * Title: AttributesPanel
 * Copyright:   Copyright (c) 2001,2002
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
package org.tn5250j.sessionsettings;

import org.tn5250j.SessionConfig;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.LangTool;

import javafx.fxml.Initializable;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Base class for all attribute panels
 */
public abstract class AbstractAttributesController implements AttributesPanel, Initializable {
    private static final String nodePrefix = "sa.node";

    private String name;
    protected final SessionConfig changes;

    public AbstractAttributesController(final SessionConfig config) {
        this(config, "", nodePrefix);
    }

    public AbstractAttributesController(final SessionConfig config, final String name) {
        this(config, name, nodePrefix);
    }

    public AbstractAttributesController(final SessionConfig config, final String name, final String prefix) {
        super();
        changes = config;
        this.name = LangTool.getString(prefix + name);
    }

    @Override
    public abstract void applyAttributes();

    @SuppressWarnings("deprecation")
    protected final String getStringProperty(final String prop) {

        if (isPropertyExists(prop))
            return changes.getStringProperty(prop);
        else
            return "";

    }

    /**
     * @param prop property name.
     * @return true if exists property with given name, false otherwise
     */
    protected boolean isPropertyExists(final String prop) {
        return changes.isPropertyExists(prop);
    }

    protected final String getStringProperty(final String prop, final String defaultValue) {

        if (isPropertyExists(prop)) {
            final String p = changes.getStringProperty(prop);
            if (p.length() > 0)
                return p;
            else
                return defaultValue;
        } else
            return defaultValue;

    }

    protected final Color getColorProperty(final String prop) {
        return getColorProperty(prop, null);
    }

    protected Color getColorProperty(final String prop, final Color defColor) {
        if (isPropertyExists(prop)) {
            return UiUtils.rgb(changes.getIntegerProperty(prop));
        } else
            return defColor;
    }

    protected final boolean getBooleanProperty(final String prop, final boolean dflt) {

        if (isPropertyExists(prop)) {
            final String b = changes.getStringProperty(prop).toLowerCase();
            if (b.equals("yes") || b.equals("true"))
                return true;
            else
                return false;
        } else
            return dflt;

    }

    protected Rectangle2D getRectangleProperty(final String key) {
        return changes.getRectangleProperty(key);
    }

    protected void setRectangleProperty(final String key, final Rectangle2D rect) {
        changes.setRectangleProperty(key, rect);
    }

    protected final void setProperty(final String key, final String val) {
        changes.setProperty(key, val);
    }

    @Override
    public String toString() {
        return name;
    }

    protected void fireStringPropertyChanged(final String name, final String value) {
        changes.firePropertyChange(this, name, getStringProperty(name), value);
        setProperty(name, value);
    }

    /**
     * @return view.
     */
    public abstract Region getView();
}
