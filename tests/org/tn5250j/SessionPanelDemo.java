package org.tn5250j;
/**
 * $Id$
 * <p>
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2011
 * Company:
 *
 * @author: master_jaf
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
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.tn5250j.event.SessionConfigEvent;
import org.tn5250j.gui.SwingToFxUtils;
import org.tn5250j.interfaces.ConfigureFactory;

import javafx.application.Application;


public class SessionPanelDemo {

    public static void main(final String[] args) {
        SwingToFxUtils.initFx();
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);

        try {
            System.setProperty("emulator.settingsDirectory", File.createTempFile("tn5250j", "settings").getAbsolutePath());
            ConfigureFactory.getInstance();
            org.tn5250j.tools.LangTool.init();

            final Session5250 session = DevTools.createSession();
            final SessionBean sessgui = DevTools.createSessionBean(session);

            final JFrame frame = new JFrame("TN5250j");
            frame.setSize(1024, 768);
            frame.addWindowListener(
                    new WindowAdapter() {
                        @Override
                        public void windowClosing(final WindowEvent e) {
                            sessgui.signoff();
                            sessgui.disconnect();
                        }
                    }
            );

            final JPanel main = new JPanel(new BorderLayout());
            main.add(sessgui, BorderLayout.CENTER);
            main.add(createChangeConfigPanel(sessgui, session), BorderLayout.NORTH);
            frame.setContentPane(main);
            frame.setVisible(true);
            sessgui.connect();

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static JPanel createChangeConfigPanel(final SessionBean sessgui, final Session5250 session) {
        final JTextField keyField = new JTextField(10);
        final JTextField valueField = new JTextField(20);

//        final JPanel keyValue = new JPanel(new GridLayout(1, 2, 10, 10));
        final JPanel keyValue = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        keyValue.add(createLabeledPanel("Key: ", keyField));
        keyValue.add(createLabeledPanel("Value: ", valueField));

        final JButton button = new JButton(" Set ");
        button.addActionListener(e -> {
            final String key = getText(keyField);
            final String value = getText(valueField);

            if (key != null && value != null) {
                session.getConfiguration().setProperty(key, value);
                final SessionConfigEvent event = new SessionConfigEvent(e,
                        key, null, value);
                sessgui.onConfigChanged(event);
            }
        });

        keyValue.add(button);

        final JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        panel.add(keyValue, BorderLayout.CENTER);
        return panel;
    }

    private static JPanel createLabeledPanel(final String label, final JTextField textField) {
        final JPanel pane = new JPanel(new BorderLayout(5, 5));
        pane.add(new JLabel(label), BorderLayout.WEST);
        pane.add(textField, BorderLayout.CENTER);
        return pane;
    }

    private static String getText(final JTextField textField) {
        final String text = textField.getText();
        return (text == null || text.trim().isEmpty()) ? null : text;
    }
}
