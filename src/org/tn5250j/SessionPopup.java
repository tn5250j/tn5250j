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

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.tn5250j.framework.tn5250.Screen5250;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.gui.HexCharMapDialog;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.interfaces.OptionAccessFactory;
import org.tn5250j.keyboard.KeyMnemonic;
import org.tn5250j.keyboard.actions.EmulatorAction;
import org.tn5250j.keyboard.configure.KeyConfigureController;
import org.tn5250j.mailtools.SendEMailDialog;
import org.tn5250j.spoolfile.SpoolExporter;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.LoadMacroMenu;
import org.tn5250j.tools.Macronizer;
import org.tn5250j.tools.SendScreenImageToFile;
import org.tn5250j.tools.SendScreenToFile;
import org.tn5250j.tools.XTFRFile;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCodeCombination;

/**
 * Custom
 */
public class SessionPopup {

    private final Screen5250 screen;
    private final SessionGui sessiongui;
    private final tnvt vt;
    private final TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

    public SessionPopup(final SessionGui ses, final double x, final double y) {

        final ContextMenu popup = new ContextMenu();
        this.sessiongui = ses;
        vt = sessiongui.getSession().getVT();
        screen = sessiongui.getScreen();

        final int pos = sessiongui.getPosFromView(x, y);

        if (!sessiongui.getRubberband().isAreaSelected() && screen.isInField(pos, false)) {
            popup.getItems().add(createMenuItem(LangTool.getString("popup.copy"), () -> copy(pos), COPY));
            popup.getItems().add(createMenuItem(LangTool.getString("popup.paste"), () -> paste(false), PASTE));
            popup.getItems().add(createMenuItem(LangTool.getString("popup.pasteSpecial"), () -> paste(true)));

            popup.getItems().add(new SeparatorMenuItem()); // ------------------
            popup.getItems().add(createMenuItem(LangTool.getString("popup.hexMap"), this::showHexMap, ""));
        } else {
            popup.getItems().add(createMenuItem(LangTool.getString("popup.copy"), this::sessionGuiCopy, COPY));
            popup.getItems().add(createMenuItem(LangTool.getString("popup.paste"), () -> paste(false), PASTE));
            popup.getItems().add(createMenuItem(LangTool.getString("popup.pasteSpecial"), () -> paste(true)));

            if (sessiongui.getRubberband().isAreaSelected()) {

                // get the bounded area of the selection
                final Rectangle2D workR = sessiongui.getBoundingArea();

                popup.getItems().add(new SeparatorMenuItem());

                popup.getItems().add(new CustomMenuItem(new Label(LangTool.getString("popup.selectedColumns")
                        + " " + (int) workR.getWidth())));
                popup.getItems().add(new CustomMenuItem(new Label(LangTool.getString("popup.selectedRows")
                        + " " + (int) workR.getHeight())));

                final Menu sumMenu = new Menu(LangTool.getString("popup.calc"));
                popup.getItems().add(sumMenu);

                sumMenu.getItems().add(createMenuItem(LangTool.getString("popup.calcGroupCD"), () -> sumArea(true)));
                sumMenu.getItems().add(createMenuItem(LangTool.getString("popup.calcGroupDC"), () -> sumArea(false)));
            }

            popup.getItems().add(new SeparatorMenuItem());
            popup.getItems().add(createMenuItem(LangTool.getString("popup.printScreen"), this::printMe, PRINT_SCREEN));

            popup.getItems().add(new SeparatorMenuItem());

            final Menu kbMenu = new Menu(LangTool.getString("popup.keyboard"));

            popup.getItems().add(kbMenu);

            kbMenu.getItems().add(createMenuItem(LangTool.getString("popup.mapKeys"), this::mapMeKeys));
            kbMenu.getItems().add(new SeparatorMenuItem());

            createKeyboardItem(kbMenu, ATTN);
            createKeyboardItem(kbMenu, RESET);
            createKeyboardItem(kbMenu, SYSREQ);

            if (screen.getOIA().isMessageWait() &&
                    OptionAccessFactory.getInstance().isValidOption(DISP_MESSAGES.mnemonic)) {

                kbMenu.getItems().add(createMenuItem(LangTool.getString("popup.displayMessages"), () -> vt.systemRequest('4'), DISP_MESSAGES));
            }

            kbMenu.getItems().add(new SeparatorMenuItem());

            createKeyboardItem(kbMenu, DUP_FIELD);
            createKeyboardItem(kbMenu, HELP);

            createKeyboardItem(kbMenu, ERASE_EOF);

            createKeyboardItem(kbMenu, FIELD_PLUS);

            createKeyboardItem(kbMenu, FIELD_MINUS);

            createKeyboardItem(kbMenu, NEW_LINE);

            if (OptionAccessFactory.getInstance().isValidOption(PRINT.mnemonic)) {
                kbMenu.getItems().add(createMenuItem(LangTool.getString("popup.hostPrint"), () -> vt.hostPrint(1), PRINT));
            }

            createShortCutItems(kbMenu);

            if (screen.getOIA().isMessageWait() &&
                    OptionAccessFactory.getInstance().isValidOption(DISP_MESSAGES.mnemonic)) {

                popup.getItems().add(createMenuItem(LangTool.getString("popup.displayMessages"),
                        () -> vt.systemRequest('4'), DISP_MESSAGES));
            }

            popup.getItems().add(new SeparatorMenuItem());

            popup.getItems().add(createMenuItem(LangTool.getString("popup.hexMap"), this::showHexMap, ""));
            popup.getItems().add(createMenuItem(LangTool.getString("popup.mapKeys"), this::mapMeKeys, ""));

            if (OptionAccessFactory.getInstance().isValidOption(DISP_ATTRIBUTES.mnemonic)) {
                popup.getItems().add(createMenuItem(LangTool.getString("popup.settings"), this::actionAttributes, DISP_ATTRIBUTES));
            }

            popup.getItems().add(new SeparatorMenuItem());

            if (sessiongui.isMacroRunning()) {
                popup.getItems().add(createMenuItem(LangTool.getString("popup.stopScript"),
                        sessiongui::setStopMacroRequested));
            } else {

                final Menu macMenu = new Menu(LangTool.getString("popup.macros"));

                if (sessiongui.isSessionRecording()) {
                    macMenu.getItems().add(createMenuItem(LangTool.getString("popup.stop"), this::stopRecordingMe));
                } else {
                    macMenu.getItems().add(createMenuItem(LangTool.getString("popup.record"), this::startRecordingMe));
                }
                if (Macronizer.isMacrosExist()) {
                    // this will add a sorted list of the macros to the macro menu
                    addMacros(macMenu);
                }
                popup.getItems().add(macMenu);
            }

            popup.getItems().add(new SeparatorMenuItem());

            final Menu xtfrMenu = new Menu(LangTool.getString("popup.export"));

            if (OptionAccessFactory.getInstance().isValidOption(FILE_TRANSFER.mnemonic)) {
                xtfrMenu.getItems().add(createMenuItem(LangTool.getString("popup.xtfrFile"),
                        this::doMeTransfer, FILE_TRANSFER));
            }

            if (OptionAccessFactory.getInstance().isValidOption(SPOOL_FILE.mnemonic)) {
                xtfrMenu.getItems().add(createMenuItem(LangTool.getString("popup.xtfrSpool"), this::doMeSpool));
            }

            popup.getItems().add(xtfrMenu);

            final Menu sendMenu = new Menu(LangTool.getString("popup.send"));
            popup.getItems().add(sendMenu);

            if (OptionAccessFactory.getInstance().isValidOption(QUICK_MAIL.mnemonic)) {
                sendMenu.getItems().add(createMenuItem(LangTool.getString("popup.quickmail"), this::sendQuickEMail, QUICK_MAIL));
            }

            if (OptionAccessFactory.getInstance().isValidOption(E_MAIL.mnemonic)) {
                sendMenu.getItems().add(createMenuItem(LangTool.getString("popup.email"), this::sendScreenEMail, E_MAIL));
            }

            sendMenu.getItems().add(createMenuItem(LangTool.getString("popup.file"), this::sendMeToFile));
            sendMenu.getItems().add(createMenuItem(LangTool.getString("popup.toImage"), this::sendMeToImageFile));
        }

        popup.getItems().add(new SeparatorMenuItem()); // ------------------

        if (OptionAccessFactory.getInstance().isValidOption(OPEN_NEW.mnemonic)) {
            popup.getItems().add(createMenuItem(LangTool.getString("popup.connections"), sessiongui::startNewSession, OPEN_NEW));
        }

        popup.getItems().add(new SeparatorMenuItem());

        if (OptionAccessFactory.getInstance().isValidOption(TOGGLE_CONNECTION.mnemonic)) {

            if (vt.isConnected()) {
                popup.getItems().add(createMenuItem(LangTool.getString("popup.disconnect"), this::toggleConnection,
                        TOGGLE_CONNECTION));
            } else {
                popup.getItems().add(createMenuItem(LangTool.getString("popup.connect"), this::toggleConnection,
                        TOGGLE_CONNECTION));
            }
        }

        if (OptionAccessFactory.getInstance().isValidOption(CLOSE.mnemonic)) {
            popup.getItems().add(createMenuItem(LangTool.getString("popup.close"),
                    () -> sessiongui.confirmCloseSession(true), CLOSE));

        }

        popup.setAnchorX(x);
        popup.setAnchorY(y);

        popup.show(((Node) sessiongui).getScene().getWindow());
    }

    private void createKeyboardItem(final Menu menu, final KeyMnemonic keyMnemonic) {
        createKeyboardItem(menu, keyMnemonic.mnemonic);
    }

    private void createKeyboardItem(final Menu menu, final String key) {

        if (OptionAccessFactory.getInstance().isValidOption(key)) {
            final String key2 = key;
            menu.getItems().add(createMenuItem(LangTool.getString("key." + key),
                    () -> screen.sendKeys(key2), key));
        }

    }

    private void addMacros(final Menu menu) {

        LoadMacroMenu.loadMacros(sessiongui, menu);
    }

    private MenuItem createMenuItem(final String label, final Runnable action, final KeyMnemonic keyMnemonic) {
        return createMenuItem(label, action, keyMnemonic.mnemonic);
    }

    private MenuItem createMenuItem(final String label, final Runnable action, final String accelKey) {
        final MenuItem mi = createMenuItem(label, action);
        if (sessiongui.getKeyHandler().isKeyStrokeDefined(accelKey)) {
            mi.setAccelerator(sessiongui.getKeyHandler().getKeyStroke(accelKey));
        } else {
            for (final Map.Entry<KeyCodeCombination, EmulatorAction> e : sessiongui.getKeyActions().entrySet()) {
                if (accelKey.equals(e.getValue().getName())) {
                    mi.setAccelerator(e.getKey());
                    break;
                }
            }
        }
        return mi;
    }
    private MenuItem createMenuItem(final String label, final Runnable action) {
        final MenuItem mi = new MenuItem(label);
        mi.setOnAction(e -> action.run());
        return mi;
    }

    private void createShortCutItems(final Menu menu) {
        final Menu sm = new Menu(LangTool.getString("popup.shortCuts"));
        menu.getItems().add(new SeparatorMenuItem());
        menu.getItems().add(sm);

        for (final Map.Entry<KeyCodeCombination, EmulatorAction> e : sessiongui.getKeyActions().entrySet()) {
            final EmulatorAction value = e.getValue();
            final MenuItem mi = new MenuItem();
            mi.setOnAction(e.getValue());
            mi.setText(LangTool.getString("key." + value.getName()));
            mi.setAccelerator(e.getKey());
            sm.getItems().add(mi);
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

        final Alert alert = new Alert(AlertType.INFORMATION);
        alert.setContentText(df.format(sum));
        alert.setTitle(LangTool.getString("popup.calc"));
        alert.showAndWait();
    }

    private void printMe() {
        sessiongui.printMe();
        sessiongui.getFocusForMe();
    }

    private void showHexMap() {
        final HexCharMapDialog dlg = new HexCharMapDialog(vt.getCodePage());
        final String key = dlg.showModal();
        if (key != null) {
            screen.sendKeys(key);
        }
        sessiongui.getFocusForMe();
    }

    private void toggleConnection() {
        sessiongui.toggleConnection();
        sessiongui.getFocusForMe();
    }

    private void mapMeKeys() {
        final KeyConfigureController controller;
        if (Macronizer.isMacrosExist()) {
            final String[] macrosList = Macronizer.getMacroList();
            controller = new KeyConfigureController(macrosList, vt.getCodePage());
        } else {
            controller = new KeyConfigureController(null, vt.getCodePage());
        }

        final FXMLLoader loader = UiUtils.createLoader("/fxml/KeyConfigurePane.fxml");
        loader.setControllerFactory(cls -> controller);
        UiUtils.showDialog(sessiongui.getWindow(), loader, LangTool.getString("key.title"), null);

        sessiongui.getFocusForMe();
    }

    private void actionAttributes() {
        sessiongui.actionAttributes();
        sessiongui.getFocusForMe();
    }

    private void stopRecordingMe() {
        sessiongui.stopRecordingMe();
        sessiongui.getFocusForMe();
    }

    private void startRecordingMe() {
        sessiongui.startRecordingMe();
        sessiongui.getFocusForMe();
    }

    private void doMeTransfer() {
        new XTFRFile(vt, sessiongui);
        sessiongui.getFocusForMe();
    }

    private void doMeSpool() {
        try {
            final SpoolExporter spooler = new SpoolExporter(vt, sessiongui);
            spooler.setVisible(true);
        } catch (final NoClassDefFoundError ncdfe) {
            UiUtils.showError(LangTool.getString("messages.noAS400Toolbox"), null);
        }
        sessiongui.getFocusForMe();
    }

    private void sendScreenEMail() {
        new SendEMailDialog(sessiongui);
        sessiongui.getFocusForMe();
    }

    private void sendQuickEMail() {
        new SendEMailDialog(sessiongui, false);
        sessiongui.getFocusForMe();
    }

    private void sendMeToFile() {

        SendScreenToFile.showDialog(sessiongui.getWindow(), screen);
    }

    private void sendMeToImageFile() {
        // Change sent by LUC - LDC to add a parent frame to be passed
        new SendScreenImageToFile(sessiongui.getWindow(), sessiongui);
    }

    private void copy(final int pos) {
        final String fcontent = screen.copyTextField(pos);

        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        content.putString(fcontent);
        clipboard.setContent(content);

        sessiongui.getFocusForMe();
    }

    private void sessionGuiCopy() {
        sessiongui.actionCopy();
        sessiongui.getFocusForMe();
    }

    private void paste(final boolean special) {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            screen.pasteText(clipboard.getString(), special);
        }

        sessiongui.getFocusForMe();
    }
}
