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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

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

            final JCheckBox checkBox = new JCheckBox("Set keyboard visible");
            checkBox.addActionListener(e -> {
                final String value = checkBox.isSelected() ?  SessionConfig.YES : SessionConfig.NO;
                session.getConfiguration().setProperty("keypad", value);
                final SessionConfigEvent event = new SessionConfigEvent(e,
                        SessionConfig.CONFIG_KEYPAD_ENABLED, null, value);
                sessgui.onConfigChanged(event);
            });

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
            main.add(checkBox, BorderLayout.NORTH);
            frame.setContentPane(main);
            frame.setVisible(true);
            sessgui.connect();

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
