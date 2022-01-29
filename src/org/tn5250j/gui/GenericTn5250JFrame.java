/*
 * @(#)TN5250jFrame.java
 * Copyright:    Copyright (c) 2001 , 2002
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

package org.tn5250j.gui;

import org.tn5250j.tools.GUIGraphicsUtils;

import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class GenericTn5250JFrame {

    protected boolean packFrame = false;
    protected final Stage stage = new Stage();

    public GenericTn5250JFrame() {
        super();
        stage.getIcons().addAll(GUIGraphicsUtils.getApplicationIconsFx());
    }

    public void centerStage() {
        if (packFrame) {
            stage.sizeToScene();
        }

        final Rectangle2D bounds = Screen.getPrimary().getBounds();

        final double w = Math.min(stage.getWidth(), bounds.getWidth());
        final double h = Math.min(stage.getHeight(), bounds.getHeight());
        stage.setWidth(w);
        stage.setHeight(h);

        stage.setX((bounds.getWidth() - w) / 2);
        stage.setY((bounds.getHeight() - h) / 2);
    }

    /**
     * @return width of stage
     */
    public double getWidth() {
        return stage.getWidth();
    }

    /**
     * @return height of stage
     */
    public double getHeight() {
        return stage.getHeight();
    }

    /**
     * @param width width
     * @param height height
     */
    public void setSize(final double width, final double height) {
        stage.setWidth(width);
        stage.setHeight(height);
    }

    /**
     * @param x x coordinate.
     * @param y y coordinate.
     */
    public void setLocation(final double x, final double y) {
        stage.setX(x);
        stage.setY(y);
    }

    /**
     * @return x coordinate.
     */
    public double getX() {
        return stage.getX();
    }

    /**
     * @return y coordinate.
     */
    public double getY() {
        return stage.getY();
    }

    /**
     * @return true if the scene is visible.
     */
    public boolean isVisible() {
        return stage != null && stage.isShowing();
    }

    /**
     * @param visible
     */
    public void setVisible(final boolean visible) {
        if (visible) {
            stage.show();
        } else {
            stage.hide();
        }
    }

    /**
     * @param cursor cursor to set.
     */
    public void setCursor(final Cursor cursor) {
        stage.getScene().getRoot().setCursor(cursor);
    }

    /**
     * @return window.
     */
    public Window getWindow() {
        return stage;
    }

    /**
     * closes the stage
     */
    public void dispose() {
        stage.close();
    }
}
