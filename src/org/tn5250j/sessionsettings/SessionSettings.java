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

import org.tn5250j.SessionConfig;
import org.tn5250j.tools.LangTool;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Enumeration;
import java.util.Properties;

public class SessionSettings extends JDialog {

  private static final long serialVersionUID = 1L;
  private final String fileName;
  private final Properties props;
  private JPanel jpm = new JPanel(new BorderLayout());

  private final SessionConfig changes;

  private JTree tree = new JTree();
  private CardLayout cardLayout;
  private JPanel jp;

  public SessionSettings(Frame parent, SessionConfig config) {
    super(parent);

    parent.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

    this.fileName = config.getConfigurationResource();
    this.props = config.getProperties();
    changes = config;

    try {
      jbInit();
      parent.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**Component initialization*/
  private void jbInit() throws Exception {

    // define default
    jp = new JPanel();
    cardLayout = new CardLayout();
    jp.setLayout(cardLayout);

    DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
    Enumeration<?> e = root.children();
    Object child;
    while (e.hasMoreElements()) {
      child = e.nextElement();
      Object obj = ((DefaultMutableTreeNode) child).getUserObject();
      if (obj instanceof AttributesPanel) {
        jp.add((AttributesPanel) obj, obj.toString());
      }
    }

    //Create the nodes.
    DefaultMutableTreeNode top = new DefaultMutableTreeNode(fileName);
    createNodes(top);

    //Create a tree that allows one selection at a time.
    tree = new JTree(top);

    tree.getSelectionModel().setSelectionMode
        (TreeSelectionModel.SINGLE_TREE_SELECTION);

    //Listen for when the selection changes.
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      @Override
      public void valueChanged(TreeSelectionEvent e) {

        DefaultMutableTreeNode node = (DefaultMutableTreeNode)
            tree.getLastSelectedPathComponent();

        if (node == null)
          return;
        showPanel(node.getUserObject());
      }
    });


    // define tree selection panel
    JPanel jsp = new JPanel();
    jsp.setBackground(Color.white);
    jsp.add(tree);

    jpm.add(jp, BorderLayout.EAST);
    jpm.add(jsp, BorderLayout.WEST);

    cardLayout.first(jp);


  }

  private void showPanel(Object node) {

    cardLayout.show(jp, node.toString());
  }

  private void createNodes(DefaultMutableTreeNode top) {
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

  private void createNode(DefaultMutableTreeNode top, AttributesPanel ap) {

    top.add(new DefaultMutableTreeNode(ap));
    jp.add(ap, ap.toString());

  }

  protected final String getStringProperty(String prop) {

    if (props.containsKey(prop))
      return (String) props.get(prop);
    else
      return "";

  }

  protected final String getStringProperty(String prop, String defaultValue) {

    if (props.containsKey(prop)) {
      String p = (String) props.get(prop);
      if (p.length() > 0)
        return p;
      else
        return defaultValue;
    } else
      return defaultValue;

  }

  protected final void setProperty(String key, String val) {
    props.setProperty(key, val);
  }

  public Properties getProperties() {

    return props;
  }

  public void showIt() {

    Object[] message = new Object[1];
    message[0] = jpm;
    String[] options = {LangTool.getString("sa.optApply"),
        LangTool.getString("sa.optCancel"),
        LangTool.getString("sa.optSave")
    };

    final JOptionPane saOptionPane = new JOptionPane(
        message,                           // the dialog message array
        JOptionPane.PLAIN_MESSAGE,   // message type
        JOptionPane.YES_NO_CANCEL_OPTION,        // option type
        null,                              // optional icon, use null to use the default icon
        options,                           // options string array, will be made into buttons//
        options[1]                         // option that should be made into a default button
    );

    setTitle(LangTool.getString("sa.title"));
    setModal(true);
    setContentPane(saOptionPane);

    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent we) {

      }
    });

    saOptionPane.addPropertyChangeListener(
        new PropertyChangeListener() {
          @Override
          public void propertyChange(PropertyChangeEvent e) {
            String prop = e.getPropertyName();
            if (isVisible()
                && (e.getSource() == saOptionPane)
                && (prop.equals(JOptionPane.VALUE_PROPERTY) ||
                prop.equals(JOptionPane.INPUT_VALUE_PROPERTY))) {

              saOptionPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

              doOptionStuff(saOptionPane);

              saOptionPane.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
          }
        });

    pack();

    //Center the dialog
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension dialogSize = getSize();
    if (dialogSize.height > screenSize.height)
      dialogSize.height = screenSize.height;
    if (dialogSize.width > screenSize.width)
      dialogSize.width = screenSize.width;
    setLocation((screenSize.width - dialogSize.width) / 2,
        (screenSize.height - dialogSize.height) / 2);

    setVisible(true);

  }

  private void doOptionStuff(JOptionPane optionPane) {
    // Warning, do not cast to String, because it also can be Integer!
    Object result = optionPane.getValue();

    if (LangTool.getString("sa.optApply").equals(result)) {

      applyAttributes();
      optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

    }
    if (LangTool.getString("sa.optCancel").equals(result)) {
      setVisible(false);
      dispose();
    }
    if (LangTool.getString("sa.optSave").equals(result)) {

      if (props.containsKey("saveme")) {
        props.remove("saveme");
      }
      changes.saveSessionProps();
      setVisible(false);
      dispose();
    }
  }

  private void applyAttributes() {

    DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
    Enumeration<?> e = root.children();
    Object child;
    while (e.hasMoreElements()) {
      child = e.nextElement();
      Object obj = ((DefaultMutableTreeNode) child).getUserObject();
      if (obj instanceof AttributesPanel) {
        ((AttributesPanel) obj).applyAttributes();
      }
    }

    setProperty("saveme", "yes");

  }

}
