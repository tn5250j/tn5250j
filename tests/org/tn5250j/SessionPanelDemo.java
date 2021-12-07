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

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.tn5250j.gui.SwingToFxUtils;
import org.tn5250j.interfaces.ConfigureFactory;


public class SessionPanelDemo {

    public static void main(final String[] args) {
        SwingToFxUtils.initFx();

        try {
            System.setProperty("emulator.settingsDirectory", File.createTempFile("tn5250j", "settings").getAbsolutePath());
            ConfigureFactory.getInstance();
            org.tn5250j.tools.LangTool.init();

            final Session5250 session = TestUtils.createSession();
            session.getConfiguration().setProperty("keypad", "Yes");

            final SessionBean sb = TestUtils.createSessionBean();

            final JFrame frame = new JFrame("TN5250j");
            frame.setSize(1024, 768);
            frame.addWindowListener(
                    new WindowAdapter() {
                        @Override
                        public void windowClosing(final WindowEvent e) {
                            sb.signoff();
                            sb.disconnect();
                        }
                    }
            );

            final SessionPanel sessgui = new SessionPanel(sb.getSession());
            final JPanel main = new JPanel(new BorderLayout());
            main.add(sessgui, BorderLayout.CENTER);
            frame.setContentPane(main);
            frame.setVisible(true);
            sb.connect();

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }
}
