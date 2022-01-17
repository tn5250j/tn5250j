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

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.util.Enumeration;
import java.util.Properties;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;

import org.tn5250j.SessionConfig;
import org.tn5250j.tools.LangTool;

import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SessionSettings extends DialogPane {

    private final String fileName;
    private final Properties props;
    private JPanel jpm = new JPanel(new BorderLayout());

    private final SessionConfig changes;

    private JTree tree = new JTree();
    private CardLayout cardLayout;
    private JPanel jp;
    private final Stage parent;

    public SessionSettings(final Stage parent, final SessionConfig config) {
        super();
        this.parent = parent;
        getButtonTypes().addAll(ButtonType.YES, ButtonType.APPLY, ButtonType.CANCEL);

        parent.getScene().getRoot().setCursor(Cursor.WAIT);

        this.fileName = config.getConfigurationResource();
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
        jp = new JPanel();
        cardLayout = new CardLayout();
        jp.setLayout(cardLayout);

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        final Enumeration<?> e = root.children();
        Object child;
        while (e.hasMoreElements()) {
            child = e.nextElement();
            final Object obj = ((DefaultMutableTreeNode) child).getUserObject();
            if (obj instanceof AttributesPanel) {
                jp.add((AttributesPanel) obj, obj.toString());
            }
        }

        //Create the nodes.
        final DefaultMutableTreeNode top = new DefaultMutableTreeNode(fileName);
        createNodes(top);

        //Create a tree that allows one selection at a time.
        tree = new JTree(top);

        tree.getSelectionModel().setSelectionMode
                (TreeSelectionModel.SINGLE_TREE_SELECTION);

        //Listen for when the selection changes.
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(final TreeSelectionEvent e) {

                final DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        tree.getLastSelectedPathComponent();

                if (node == null)
                    return;
                showPanel(node.getUserObject());
            }
        });


        // define tree selection panel
        final JPanel jsp = new JPanel();
        jsp.setBackground(Color.white);
        jsp.add(tree);

        jpm.add(jp, BorderLayout.EAST);
        jpm.add(jsp, BorderLayout.WEST);

        cardLayout.first(jp);

        final SwingNode adapter = new SwingNode();
        adapter.setContent(jpm);
        setContent(adapter);
    }

    private void showPanel(final Object node) {
        cardLayout.show(jp, node.toString());
    }

    private void createNodes(final DefaultMutableTreeNode top) {
        createNode(top, new ColorAttributesPanel(changes));
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

    private void createNode(final DefaultMutableTreeNode top, final AttributesPanel ap) {

        top.add(new DefaultMutableTreeNode(ap));
        jp.add(ap, ap.toString());

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
        dialog.setResizable(true);
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

        final DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
        final Enumeration<?> e = root.children();
        Object child;
        while (e.hasMoreElements()) {
            child = e.nextElement();
            final Object obj = ((DefaultMutableTreeNode) child).getUserObject();
            if (obj instanceof AttributesPanel) {
                ((AttributesPanel) obj).applyAttributes();
            }
        }

        setProperty("saveme", "yes");

    }

}
