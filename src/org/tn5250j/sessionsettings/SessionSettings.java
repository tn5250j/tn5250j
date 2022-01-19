package org.tn5250j.sessionsettings;
/*
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001
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

import java.io.IOException;
import java.util.Properties;

import org.tn5250j.SessionConfig;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.LangTool;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SessionSettings extends DialogPane {
    private static final String SWING_NODE = "SWING_NODE";

    private final Properties props;
    private BorderPane jpm = new BorderPane();

    private final SessionConfig changes;

    private TreeView<AttributesPanel> tree = new TreeView<>();
    private final Stage parent;

    @SuppressWarnings("deprecation")
    public SessionSettings(final Stage parent, final SessionConfig config) {
        super();
        this.parent = parent;
        getButtonTypes().addAll(ButtonType.YES, ButtonType.APPLY, ButtonType.CANCEL);

        parent.getScene().getRoot().setCursor(Cursor.WAIT);

        this.props = config.getProperties();
        changes = config;

        jbInit();
        parent.getScene().getRoot().setCursor(Cursor.DEFAULT);
    }

    /**
     * Component initialization
     */
    private void jbInit() {

        // define default
        final StackPane jp = new StackPane();

        //Create the nodes.
        tree.setShowRoot(false);
        tree.setRoot(new TreeItem<AttributesPanel>(null));
        tree.getRoot().setExpanded(true);
        createNodes(jp);

        tree.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tree.getSelectionModel().selectedItemProperty().addListener((src, old, value) -> treeSelectionChanged(value));

        // define tree selection panel
        final BorderPane jsp = new BorderPane(tree);
        UiUtils.setBackground(jsp, Color.WHITE);

        jpm.setLeft(jsp);
        jpm.setRight(jp);

        setContent(jpm);
        tree.getSelectionModel().selectFirst();
    }

    private void treeSelectionChanged(final TreeItem<AttributesPanel> item) {
        final AttributesPanel value = item.getValue();
        if (value instanceof AbstractAttributesPanelSwing) {
            final Node node = (Node) ((AbstractAttributesPanelSwing) value).getClientProperty(SWING_NODE);
            node.toFront();
        } else if (value instanceof AbstractAttributesController) {
            ((AbstractAttributesController) value).getView().toFront();
        }
    }

    private void createNodes(final StackPane top) {
        createNode(top, loadFromTemplate(new ColorAttributesController(changes), "/fxml/ColorAttributesPane.fxml"));
        createNode(top, new DisplayAttributesPanel(changes));
        createNode(top, new CursorAttributesPanel(changes));
        createNode(top, new FontAttributesPanel(changes));
        createNode(top, new TabAttributesPanel(changes));
        createNode(top, new SignoffAttributesPanel(changes));
        createNode(top, new OnConnectAttributesPanel(changes));
        createNode(top, new MouseAttributesPanel(changes));
        createNode(top, new HotspotAttributesPanel(changes));
        createNode(top, new KeypadAttributesPanel(changes));
        createNode(top, new PrinterAttributesPanel(changes));
        createNode(top, new ErrorResetAttributesPanel(changes));
    }

    private AbstractAttributesController loadFromTemplate(final AbstractAttributesController controller, final String tpl) {
        try {
            final FXMLLoader loader = UiUtils.createLoader(tpl);
            loader.setControllerFactory(cls -> controller);
            loader.load();
            return controller;
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load template: " + tpl, e);
        }
    }

    private void createNode(final StackPane top, final AbstractAttributesPanelSwing ap) {
        final SwingNode swingNode = new SwingNode();
        swingNode.setContent(ap);
        ap.putClientProperty(SWING_NODE, swingNode);

        top.getChildren().add(swingNode);

        final TreeItem<AttributesPanel> item = new TreeItem<>(ap);
        tree.getRoot().getChildren().add(item);
    }

    private void createNode(final StackPane top, final AbstractAttributesController controller) {
        top.getChildren().add(controller.getView());

        final TreeItem<AttributesPanel> item = new TreeItem<>(controller);
        tree.getRoot().getChildren().add(item);
    }

    protected final String getStringProperty(final String prop) {

        if (props.containsKey(prop))
            return (String) props.get(prop);
        else
            return "";

    }

    protected final String getStringProperty(final String prop, final String defaultValue) {

        if (props.containsKey(prop)) {
            final String p = (String) props.get(prop);
            if (p.length() > 0)
                return p;
            else
                return defaultValue;
        } else
            return defaultValue;

    }

    protected final void setProperty(final String key, final String val) {
        props.setProperty(key, val);
    }

    public Properties getAllProperties() {
        return props;
    }

    @Override
    protected Node createButton(final ButtonType buttonType) {
        Button button = (Button) super.createButton(buttonType);

        if (buttonType == ButtonType.APPLY) {
            //change returned button for remove closers
            button = new Button();
            button.setText(LangTool.getString("sa.optApply"));
        } else if (buttonType == ButtonType.CANCEL) {
            button.setText(LangTool.getString("sa.optCancel"));
        } else if (buttonType == ButtonType.YES) {
            button.setText(LangTool.getString("sa.optSave"));
        }

        return button;
    }

    public void showIt() {
        final Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(LangTool.getString("sa.title"));
        dialog.initOwner(parent);

        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.resultProperty().addListener((src, old, value) -> {
            SessionSettings.this.setCursor(Cursor.WAIT);
            try {
                doOptionStuff(value);
            } finally {
                SessionSettings.this.setCursor(Cursor.DEFAULT);
            }
        });

        dialog.setDialogPane(this);
        dialog.setResizable(true); //FIXME possible better to comment it

        Platform.runLater(dialog::show);
    }

    private void doOptionStuff(final ButtonType result) {
        if (result == ButtonType.APPLY) {
            applyAttributes();
        } else if (result == ButtonType.OK) {
            if (props.containsKey("saveme")) {
                props.remove("saveme");
            }
            changes.saveSessionProps();
        }
    }

    private void applyAttributes() {
        final ObservableList<TreeItem<AttributesPanel>> children = tree.getRoot().getChildren();
        for (final TreeItem<AttributesPanel> item : children) {
            item.getValue().applyAttributes();
        }

        setProperty("saveme", "yes");
    }
}
