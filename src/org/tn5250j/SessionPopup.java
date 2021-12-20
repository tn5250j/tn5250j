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
package org.tn5250j;

import static org.tn5250j.keyboard.KeyMnemonic.ATTN;
import static org.tn5250j.keyboard.KeyMnemonic.CLOSE;
import static org.tn5250j.keyboard.KeyMnemonic.COPY;
import static org.tn5250j.keyboard.KeyMnemonic.DISP_ATTRIBUTES;
import static org.tn5250j.keyboard.KeyMnemonic.DISP_MESSAGES;
import static org.tn5250j.keyboard.KeyMnemonic.DUP_FIELD;
import static org.tn5250j.keyboard.KeyMnemonic.ERASE_EOF;
import static org.tn5250j.keyboard.KeyMnemonic.E_MAIL;
import static org.tn5250j.keyboard.KeyMnemonic.FIELD_MINUS;
import static org.tn5250j.keyboard.KeyMnemonic.FIELD_PLUS;
import static org.tn5250j.keyboard.KeyMnemonic.FILE_TRANSFER;
import static org.tn5250j.keyboard.KeyMnemonic.HELP;
import static org.tn5250j.keyboard.KeyMnemonic.NEW_LINE;
import static org.tn5250j.keyboard.KeyMnemonic.OPEN_NEW;
import static org.tn5250j.keyboard.KeyMnemonic.PASTE;
import static org.tn5250j.keyboard.KeyMnemonic.PRINT;
import static org.tn5250j.keyboard.KeyMnemonic.PRINT_SCREEN;
import static org.tn5250j.keyboard.KeyMnemonic.QUICK_MAIL;
import static org.tn5250j.keyboard.KeyMnemonic.RESET;
import static org.tn5250j.keyboard.KeyMnemonic.SPOOL_FILE;
import static org.tn5250j.keyboard.KeyMnemonic.SYSREQ;
import static org.tn5250j.keyboard.KeyMnemonic.TOGGLE_CONNECTION;

import java.awt.Component;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.gui.HexCharMapDialog;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.interfaces.OptionAccessFactory;
import org.tn5250j.keyboard.KeyMnemonic;
import org.tn5250j.keyboard.configure.KeyConfigure;
import org.tn5250j.mailtools.SendEMailDialog;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.LoadMacroMenu;
import org.tn5250j.tools.Macronizer;
import org.tn5250j.tools.SendScreenImageToFile;
import org.tn5250j.tools.SendScreenToFile;
import org.tn5250j.tools.XTFRFile;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

/**
 * Custom
 */
public class SessionPopup {

    private final Screen5250 screen;
    private final SessionGui sessiongui;
    private final tnvt vt;
    private final TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

    public SessionPopup(final SessionGui ses, final int x, final int y) {

        JMenuItem menuItem;
        Action action;
        final JPopupMenu popup = new JPopupMenu();
        this.sessiongui = ses;
        vt = sessiongui.getSession().getVT();
        screen = sessiongui.getScreen();

        final int pos = sessiongui.getPosFromView(x, y);

        if (!sessiongui.getRubberband().isAreaSelected() && screen.isInField(pos, false)) {
            action = new AbstractAction(LangTool.getString("popup.copy")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    final String fcontent = screen.copyTextField(pos);
                    final StringSelection contents = new StringSelection(fcontent);
                    final Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
                    cb.setContents(contents, null);
                    sessiongui.getFocusForMe();
                }
            };

            popup.add(createMenuItem(action, COPY));


            action = new AbstractAction(LangTool.getString("popup.paste")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    paste(false);
                }
            };
            popup.add(createMenuItem(action, PASTE));

            action = new AbstractAction(LangTool.getString("popup.pasteSpecial")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    paste(true);
                }
            };
            popup.add(action);

            popup.addSeparator(); // ------------------

            action = new AbstractAction(LangTool.getString("popup.hexMap")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    showHexMap();
                    sessiongui.getFocusForMe();
                }
            };
            popup.add(createMenuItem(action, ""));

            popup.addSeparator(); // ------------------
        } else {

            action = new AbstractAction(LangTool.getString("popup.copy")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    sessiongui.actionCopy();
                    sessiongui.getFocusForMe();
                }
            };

            popup.add(createMenuItem(action, COPY));

            action = new AbstractAction(LangTool.getString("popup.paste")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    paste(false);
                }
            };
            popup.add(createMenuItem(action, PASTE));

            action = new AbstractAction(LangTool.getString("popup.pasteSpecial")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    paste(true);
                }
            };
            popup.add(action);

            Rectangle workR = new Rectangle();
            if (sessiongui.getRubberband().isAreaSelected()) {

                // get the bounded area of the selection
                workR = UiUtils.toAwtRectangle(sessiongui.getBoundingArea());

                popup.addSeparator();

                menuItem = new JMenuItem(LangTool.getString("popup.selectedColumns")
                        + " " + workR.width);
                menuItem.setArmed(false);
                popup.add(menuItem);

                menuItem = new JMenuItem(LangTool.getString("popup.selectedRows")
                        + " " + workR.height);
                menuItem.setArmed(false);
                popup.add(menuItem);

                final JMenu sumMenu = new JMenu(LangTool.getString("popup.calc"));
                popup.add(sumMenu);

                action = new AbstractAction(LangTool.getString("popup.calcGroupCD")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        sumArea(true);
                    }
                };
                sumMenu.add(action);

                action = new AbstractAction(LangTool.getString("popup.calcGroupDC")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        sumArea(false);
                    }
                };
                sumMenu.add(action);

            }

            popup.addSeparator();

            action = new AbstractAction(LangTool.getString("popup.printScreen")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    sessiongui.printMe();
                    sessiongui.getFocusForMe();
                }
            };
            popup.add(createMenuItem(action, PRINT_SCREEN));

            popup.addSeparator();

            final JMenu kbMenu = new JMenu(LangTool.getString("popup.keyboard"));

            popup.add(kbMenu);

            action = new AbstractAction(LangTool.getString("popup.mapKeys")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {

                    mapMeKeys();
                }
            };

            kbMenu.add(action);

            kbMenu.addSeparator();

            createKeyboardItem(kbMenu, ATTN);

            createKeyboardItem(kbMenu, RESET);

            createKeyboardItem(kbMenu, SYSREQ);

            if (screen.getOIA().isMessageWait() &&
                    OptionAccessFactory.getInstance().isValidOption(DISP_MESSAGES.mnemonic)) {

                action = new AbstractAction(LangTool.getString("popup.displayMessages")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        vt.systemRequest('4');
                    }
                };

                kbMenu.add(createMenuItem(action, DISP_MESSAGES));
            }

            kbMenu.addSeparator();

            createKeyboardItem(kbMenu, DUP_FIELD);

            createKeyboardItem(kbMenu, HELP);

            createKeyboardItem(kbMenu, ERASE_EOF);

            createKeyboardItem(kbMenu, FIELD_PLUS);

            createKeyboardItem(kbMenu, FIELD_MINUS);

            createKeyboardItem(kbMenu, NEW_LINE);

            if (OptionAccessFactory.getInstance().isValidOption(PRINT.mnemonic)) {
                action = new AbstractAction(LangTool.getString("popup.hostPrint")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        vt.hostPrint(1);
                    }
                };
                kbMenu.add(createMenuItem(action, PRINT));
            }

            createShortCutItems(kbMenu);

            if (screen.getOIA().isMessageWait() &&
                    OptionAccessFactory.getInstance().isValidOption(DISP_MESSAGES.mnemonic)) {

                action = new AbstractAction(LangTool.getString("popup.displayMessages")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        vt.systemRequest('4');
                    }
                };
                popup.add(createMenuItem(action, DISP_MESSAGES));
            }

            popup.addSeparator();

            action = new AbstractAction(LangTool.getString("popup.hexMap")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    showHexMap();
                    sessiongui.getFocusForMe();
                }
            };
            popup.add(createMenuItem(action, ""));

            action = new AbstractAction(LangTool.getString("popup.mapKeys")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {

                    mapMeKeys();
                    sessiongui.getFocusForMe();
                }
            };
            popup.add(createMenuItem(action, ""));

            if (OptionAccessFactory.getInstance().isValidOption(DISP_ATTRIBUTES.mnemonic)) {

                action = new AbstractAction(LangTool.getString("popup.settings")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        sessiongui.actionAttributes();
                        sessiongui.getFocusForMe();
                    }
                };
                popup.add(createMenuItem(action, DISP_ATTRIBUTES));

            }

            popup.addSeparator();

            if (sessiongui.isMacroRunning()) {
                action = new AbstractAction(LangTool.getString("popup.stopScript")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        sessiongui.setStopMacroRequested();
                    }
                };
                popup.add(action);
            } else {

                final JMenu macMenu = new JMenu(LangTool.getString("popup.macros"));

                if (sessiongui.isSessionRecording()) {
                    action = new AbstractAction(LangTool.getString("popup.stop")) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            sessiongui.stopRecordingMe();
                            sessiongui.getFocusForMe();
                        }
                    };

                } else {
                    action = new AbstractAction(LangTool.getString("popup.record")) {
                        private static final long serialVersionUID = 1L;

                        @Override
                        public void actionPerformed(final ActionEvent e) {
                            sessiongui.startRecordingMe();
                            sessiongui.getFocusForMe();

                        }
                    };
                }
                macMenu.add(action);
                if (Macronizer.isMacrosExist()) {
                    // this will add a sorted list of the macros to the macro menu
                    addMacros(macMenu);
                }
                popup.add(macMenu);
            }

            popup.addSeparator();

            final JMenu xtfrMenu = new JMenu(LangTool.getString("popup.export"));

            if (OptionAccessFactory.getInstance().isValidOption(FILE_TRANSFER.mnemonic)) {

                action = new AbstractAction(LangTool.getString("popup.xtfrFile")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        doMeTransfer();
                        sessiongui.getFocusForMe();
                    }
                };

                xtfrMenu.add(createMenuItem(action, FILE_TRANSFER));
            }

            if (OptionAccessFactory.getInstance().isValidOption(SPOOL_FILE.mnemonic)) {

                action = new AbstractAction(LangTool.getString("popup.xtfrSpool")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        doMeSpool();
                        sessiongui.getFocusForMe();
                    }
                };

                xtfrMenu.add(action);
            }

            popup.add(xtfrMenu);

            final JMenu sendMenu = new JMenu(LangTool.getString("popup.send"));
            popup.add(sendMenu);

            if (OptionAccessFactory.getInstance().isValidOption(QUICK_MAIL.mnemonic)) {

                action = new AbstractAction(LangTool.getString("popup.quickmail")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        sendQuickEMail();
                        sessiongui.getFocusForMe();
                    }
                };
                sendMenu.add(createMenuItem(action, QUICK_MAIL));
            }

            if (OptionAccessFactory.getInstance().isValidOption(E_MAIL.mnemonic)) {

                action = new AbstractAction(LangTool.getString("popup.email")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        sendScreenEMail();
                        sessiongui.getFocusForMe();
                    }
                };

                sendMenu.add(createMenuItem(action, E_MAIL));
            }

            action = new AbstractAction(LangTool.getString("popup.file")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    sendMeToFile();
                }
            };

            sendMenu.add(action);

            action = new AbstractAction(LangTool.getString("popup.toImage")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    sendMeToImageFile();
                }
            };

            sendMenu.add(action);

            popup.addSeparator();

        }

        if (OptionAccessFactory.getInstance().isValidOption(OPEN_NEW.mnemonic)) {

            action = new AbstractAction(LangTool.getString("popup.connections")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    sessiongui.startNewSession();
                }
            };

            popup.add(createMenuItem(action, OPEN_NEW));
        }

        popup.addSeparator();

        if (OptionAccessFactory.getInstance().isValidOption(TOGGLE_CONNECTION.mnemonic)) {

            if (vt.isConnected()) {
                action = new AbstractAction(LangTool.getString("popup.disconnect")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        sessiongui.toggleConnection();
                        sessiongui.getFocusForMe();
                    }
                };
            } else {

                action = new AbstractAction(LangTool.getString("popup.connect")) {
                    private static final long serialVersionUID = 1L;

                    @Override
                    public void actionPerformed(final ActionEvent e) {
                        sessiongui.toggleConnection();
                        sessiongui.getFocusForMe();
                    }
                };


            }

            popup.add(createMenuItem(action, TOGGLE_CONNECTION));
        }

        if (OptionAccessFactory.getInstance().isValidOption(CLOSE.mnemonic)) {

            action = new AbstractAction(LangTool.getString("popup.close")) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    sessiongui.confirmCloseSession(true);
                }
            };

            popup.add(createMenuItem(action, CLOSE));

        }

        GUIGraphicsUtils.positionPopup((Component) ses, popup,
                x, y);

    }

    private void createKeyboardItem(final JMenu menu, final KeyMnemonic keyMnemonic) {
        createKeyboardItem(menu, keyMnemonic.mnemonic);
    }

    private void createKeyboardItem(final JMenu menu, final String key) {

        if (OptionAccessFactory.getInstance().isValidOption(key)) {
            final String key2 = key;
            final Action action = new AbstractAction(LangTool.getString("key." + key)) {
                private static final long serialVersionUID = 1L;

                @Override
                public void actionPerformed(final ActionEvent e) {
                    screen.sendKeys(key2);
                }
            };

            menu.add(createMenuItem(action, key));
        }

    }

    private void addMacros(final JMenu menu) {

        LoadMacroMenu.loadMacros(sessiongui, menu);
    }

    private JMenuItem createMenuItem(final Action action, final KeyMnemonic keyMnemonic) {
        return createMenuItem(action, keyMnemonic.mnemonic);
    }

    private JMenuItem createMenuItem(final Action action, final String accelKey) {
        final JMenuItem mi = new JMenuItem();
        mi.setAction(action);
        if (sessiongui.getKeyHandler().isKeyStrokeDefined(accelKey)) {
            mi.setAccelerator(sessiongui.getKeyHandler().getKeyStroke(accelKey));
        } else {
            final InputMap map = ((JComponent) sessiongui).getInputMap();
            final KeyStroke[] allKeys = map.allKeys();
            for (final KeyStroke keyStroke : allKeys) {
                if (map.get(keyStroke).equals(accelKey)) {
                    mi.setAccelerator(keyStroke);
                    break;
                }
            }
        }
        return mi;
    }

    private void createShortCutItems(final JMenu menu) {

        final JMenu sm = new JMenu(LangTool.getString("popup.shortCuts"));
        menu.addSeparator();
        menu.add(sm);

        final InputMap map = ((JComponent) sessiongui).getInputMap();
        final KeyStroke[] allKeys = map.allKeys();
        final ActionMap aMap = ((JComponent) sessiongui).getActionMap();

        for (final KeyStroke allKey : allKeys) {
            final Action a = aMap.get(map.get(allKey));
            final JMenuItem mi = new JMenuItem();
            mi.setAction(a);
            mi.setText(LangTool.getString("key." + map.get(allKey)));
            mi.setAccelerator(allKey);
            sm.add(mi);
        }
    }

    private void sumArea(final boolean which) {


        final List<Double> sumVector = sessiongui.sumThem(which);
        final Iterator<Double> l = sumVector.iterator();
        double sum = 0.0;
        double inter;
        while (l.hasNext()) {

            inter = 0.0;
            try {
                inter = l.next();
            } catch (final Exception e) {
                log.warn(e);
            }

            sum += inter;

        }
        if (log.isDebugEnabled()) {
            log.debug("Vector sum " + sum);
        }

        // obtain the decimal format for parsing
        final DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();

        final DecimalFormatSymbols dfs = df.getDecimalFormatSymbols();

        if (which) {
            dfs.setDecimalSeparator('.');
            dfs.setGroupingSeparator(',');
        } else {
            dfs.setDecimalSeparator(',');
            dfs.setGroupingSeparator('.');
        }

        df.setDecimalFormatSymbols(dfs);
        df.setMinimumFractionDigits(6);

        JOptionPane.showMessageDialog(null,
                df.format(sum),
                LangTool.getString("popup.calc"),
                JOptionPane.INFORMATION_MESSAGE);

    }

    private void showHexMap() {
        final HexCharMapDialog dlg = new HexCharMapDialog(((JComponent) sessiongui), vt.getCodePage());
        final String key = dlg.showModal();
        if (key != null) {
            screen.sendKeys(key);
        }
    }

    private void mapMeKeys() {

        final Frame parent = (Frame) SwingUtilities.getRoot(((JComponent) sessiongui));

        if (Macronizer.isMacrosExist()) {
            final String[] macrosList = Macronizer.getMacroList();
            new KeyConfigure(parent, macrosList, vt.getCodePage());
        } else {
            new KeyConfigure(parent, null, vt.getCodePage());
        }

    }

    private void doMeTransfer() {

        new XTFRFile((Frame) SwingUtilities.getRoot(((JComponent) sessiongui)), vt, sessiongui);

    }

    private void doMeSpool() {

        try {
            final org.tn5250j.spoolfile.SpoolExporter spooler =
                    new org.tn5250j.spoolfile.SpoolExporter(vt, sessiongui);
            spooler.setVisible(true);
        } catch (final NoClassDefFoundError ncdfe) {
            JOptionPane.showMessageDialog(((JComponent) sessiongui),
                    LangTool.getString("messages.noAS400Toolbox"),
                    "Error",
                    JOptionPane.ERROR_MESSAGE, null);
        }

    }

    private void sendScreenEMail() {

        new SendEMailDialog((Frame) SwingUtilities.getRoot(((JComponent) sessiongui)), sessiongui);
    }

    private void sendQuickEMail() {

        new SendEMailDialog((Frame) SwingUtilities.getRoot(((JComponent) sessiongui)), sessiongui, false);
    }

    private void sendMeToFile() {

        SendScreenToFile.showDialog(SwingUtilities.getRoot(((JComponent) sessiongui)), screen);
    }

    private void sendMeToImageFile() {
        // Change sent by LUC - LDC to add a parent frame to be passed
        new SendScreenImageToFile((Frame) SwingUtilities.getRoot(((JComponent) sessiongui)), sessiongui);
    }

    private void paste(final boolean special) {
        try {
            final Clipboard cb = Toolkit.getDefaultToolkit().getSystemClipboard();
            final Transferable transferable = cb.getContents(this);
            if (transferable != null) {
                final String content = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                screen.pasteText(content, special);
                sessiongui.getFocusForMe();
            }
        } catch (final HeadlessException e1) {
            log.debug("HeadlessException", e1);
        } catch (final UnsupportedFlavorException e1) {
            log.debug("the requested data flavor is not supported", e1);
        } catch (final IOException e1) {
            log.debug("data is no longer available in the requested flavor", e1);
        }

    }

}
