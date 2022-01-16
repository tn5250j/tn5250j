/**
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
package org.tn5250j.tools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPopupMenu;

import org.tn5250j.SessionGui;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.scripting.ExecuteScriptAction;
import org.tn5250j.scripting.InterpreterDriverManager;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

public final class LoadMacroMenu {

    private static List<Object> macroVector = new LinkedList<>();

    private static final TN5250jLogger log = TN5250jLogFactory.getLogger(LoadMacroMenu.class);

    public static void loadMacros(final SessionGui ses, final Menu menu) {

        menu.getItems().add(new SeparatorMenuItem());

        final String[] macrosList = Macronizer.getMacroList();
        Arrays.sort(macrosList);

        for (final String macros : macrosList) {
            final MenuItem mi = new MenuItem(macros);
            mi.setOnAction(e -> ses.executeMacro(macros));

            menu.getItems().add(mi);

            final EventHandler<MouseEvent> handler = e -> {
                if (e.getButton() == MouseButton.SECONDARY) {
                    doOptionsPopup(e, ses, macros);
                }
            };

            mi.addEventHandler(MouseEvent.MOUSE_PRESSED, handler);
        }

        scriptDir("scripts", menu, ses);

        String conPath = "";
        String conPath2 = "";

        try {
            conPath = new File("scripts").getCanonicalPath();
            conPath2 = new File(ConfigureFactory.getInstance().getProperty(
                    "emulator.settingsDirectory") +
                    "scripts").getCanonicalPath();
        } catch (final IOException ioe) {

        }

        // lets not load the menu again if they point to the same place
        if (!conPath.equals(conPath2))
            scriptDir(ConfigureFactory.getInstance().getProperty(
                    "emulator.settingsDirectory") +
                    "scripts", menu, ses);
    }

    private static void doOptionsPopup(final MouseEvent e, final SessionGui ses, final String macro) {
        final ContextMenu j = new ContextMenu();
        j.getItems().add(new CustomMenuItem(new Label("Macro Options"), false));

        MenuItem item = new MenuItem(LangTool.getString("popup.delete") + " " + macro);
        item.setOnAction(evt -> Macronizer.removeMacroByName(macro));

        j.getItems().add(item);

        item = new MenuItem(LangTool.getString("popup.execute") + " " + macro);
        item.setOnAction(evt -> Macronizer.invoke(macro.toString(), ses));

        j.getItems().add(item);

        j.show((Node) e.getSource(), e.getScreenX(), e.getScreenY());
    }

    public static void scriptDir(final String pathName, final Menu menu, final SessionGui session) {

        final File root = new File(pathName);

        try {

            macroVector = new LinkedList<>();

            loadScripts(macroVector, root.getCanonicalPath(), root, session);
            createScriptsMenu(menu, macroVector, 0);

        } catch (final IOException ioe) {
            System.out.println(ioe.getMessage());

        }

    }

    /**
     * Recursively read the scripts directory and add them to our macros vector
     *    holding area
     *
     * @param vector
     * @param path
     * @param directory
     * @param session
     */
    private static void loadScripts(final List<Object> vector, final String path, final File directory,
                                    final SessionGui session) {

        ExecuteScriptAction action;

        final File[] macroFiles = directory.listFiles();
        if (macroFiles == null || macroFiles.length == 0)
            return;

        Arrays.sort(macroFiles, new MacroCompare());

        //KJP - We will wrap this in a try catch block to catch security
        // exceptions
        for (int i = 0; i < macroFiles.length; i++) {
            try {
                final File file = macroFiles[i];
                final String fileName = file.getName();
                if (file.isHidden()) {
                    /* do nothing! */
                    continue;
                } else if (file.isDirectory()) {
                    final List<Object> submenu = new LinkedList<>();
                    submenu.add(fileName.replace('_', ' '));
                    loadScripts(submenu, path + fileName + '/', file, session);
                    // if we do not want empty directories to show up uncomment
                    // this line.
                    // if(submenu.size() != 1)
                    vector.add(submenu);
                } else {
                    if (InterpreterDriverManager.isScriptSupported(fileName)) {
                        String fn = fileName.replace('_', ' ');
                        final int index = fn.lastIndexOf('.');
                        if (index > 0) {
                            fn = fn.substring(0, index);
                        }
                        action = new ExecuteScriptAction(fn, file
                                .getAbsolutePath(), session);
                        vector.add(action);
                    }
                }
            } catch (final SecurityException se) {
                log.warn(se.getMessage());
            }
        }
    }

    /**
     * Create the scripts menu(s) from the vector of macros provided
     *
     * @param menu
     * @param vector
     * @param start
     */
    private static void createScriptsMenu(final Menu menu, final List<Object> vector, final int start) {

        final JPopupMenu jpop = new JPopupMenu();
        jpop.add("Delete");

        for (final Object obj : vector) {
            if (obj instanceof ExecuteScriptAction) {
                final ExecuteScriptAction action = (ExecuteScriptAction) obj;
                final MenuItem item = new MenuItem(action.getName());
                item.setOnAction(action);

                menu.getItems().add(item);
            } else if (obj instanceof List) {
                @SuppressWarnings("unchecked")
                final List<Object> subvector = (List<Object>) obj;
                if (!subvector.isEmpty()) {
                    final String name = (String) subvector.get(0);
                    final Menu submenu = new Menu(name);
                    createScriptsMenu(submenu, subvector, 1);
                    if (submenu.getItems().isEmpty()) {
                        submenu.getItems().add(new CustomMenuItem(new Label(LangTool.getString("popup.noScripts"))));
                    }
                    menu.getItems().add(submenu);
                }
            }
        }
    }

    public static class MacroCompare implements Comparator<File> {
        @Override
        public int compare(final File s1, final File s2) {
            return s1.getPath().compareToIgnoreCase(s2.getPath());
        }
    }
}
