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

import java.awt.Color;
import java.awt.Rectangle;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import org.tn5250j.SessionConfig;
import org.tn5250j.gui.SwingToFxUtils;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.geometry.Rectangle2D;

/**
 * Base class for all attribute panels
 */
abstract class AttributesPanel extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final String nodePrefix = "sa.node";

    private final TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

    private String name;
    SessionConfig changes = null;
    // content pane to be used if needed by subclasses
    JPanel contentPane;

    public AttributesPanel(final SessionConfig config) {
        this(config, "", nodePrefix);
    }

    public AttributesPanel(final SessionConfig config, final String name) {
        this(config, name, nodePrefix);
    }

    public AttributesPanel(final SessionConfig config, final String name, final String prefix) {
        super();
        changes = config;
        this.name = LangTool.getString(prefix + name);
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        try {
            initPanel();
        } catch (final Exception e) {
            log.error(e);
        }
    }

    public abstract void initPanel() throws Exception;

    public abstract void applyAttributes();

    protected final String getStringProperty(final String prop) {

        if (changes.isPropertyExists(prop))
            return changes.getStringProperty(prop);
        else
            return "";

    }

    protected final String getStringProperty(final String prop, final String defaultValue) {

        if (changes.isPropertyExists(prop)) {
            final String p = changes.getStringProperty(prop);
            if (p.length() > 0)
                return p;
            else
                return defaultValue;
        } else
            return defaultValue;

    }

    protected final Color getColorProperty(final String prop) {

        if (changes.isPropertyExists(prop)) {
            final Color c = new Color(changes.getIntegerProperty(prop));
            return c;
        } else
            return null;

    }

    protected Color getColorProperty(final String prop, final Color defColor) {

        if (changes.isPropertyExists(prop)) {
            final Color c = new Color(changes.getIntegerProperty(prop));
            return c;
        } else
            return defColor;

    }

    protected final boolean getBooleanProperty(final String prop, final boolean dflt) {

        if (changes.isPropertyExists(prop)) {
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

    protected void setRectangleProperty(final String key, final Rectangle rect) {
        changes.setRectangleProperty(key, SwingToFxUtils.fromAwtRect(rect));
    }

    protected final void setProperty(final String key, final String val) {
        changes.setProperty(key, val);
    }

    @Override
    public String toString() {
        return name;
    }

}
