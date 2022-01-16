/**
 * Title: Macronizer.java
 * Copyright:   Copyright (c) 2001
 * Company:
 *
 * @author Kenneth J. Pouncey
 * @version 0.1
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
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import org.tn5250j.SessionGui;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.scripting.InterpreterDriverManager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class Macronizer {

    private static Properties macros;
    private static boolean macrosExist;

    public static void init() {

        if (macros != null)
            return;

        macrosExist = loadMacros();

    }

    private static boolean loadMacros() {

        macros = ConfigureFactory.getInstance().getProperties(ConfigureFactory.MACROS);
        if (macros != null && macros.size() > 0)
            return true;

        return checkScripts();
    }

    private final static void saveMacros() {

        ConfigureFactory.getInstance().saveSettings(
                ConfigureFactory.MACROS, "------ Macros --------");
    }

    public final static boolean isMacrosExist() {
        return macrosExist;
    }

    public final static int getNumOfMacros() {

        return macros.size();

    }

    public final static String[] getMacroList() {

        final String[] macroList = new String[macros.size()];
        final Set<Object> macroSet = macros.keySet();
        final Iterator<Object> macroIterator = macroSet.iterator();
        String byName = null;
        int x = 0;
        while (macroIterator.hasNext()) {
            byName = (String) macroIterator.next();
            final int period = byName.indexOf(".");
            macroList[x++] = byName.substring(period + 1);
        }

        return macroList;
    }

    public final static String getMacroByNumber(final int num) {
        final String mac = "macro" + num + ".";

        final Set<Object> macroSet = macros.keySet();
        final Iterator<Object> macroIterator = macroSet.iterator();
        String byNum = null;
        while (macroIterator.hasNext()) {
            byNum = (String) macroIterator.next();
            if (byNum.startsWith(mac)) {
                return (String) macros.get(byNum);
            }
        }
        return null;
    }

    public final static String getMacroByName(final String name) {

        final Set<Object> macroSet = macros.keySet();
        final Iterator<Object> macroIterator = macroSet.iterator();
        String byName = null;
        while (macroIterator.hasNext()) {
            byName = (String) macroIterator.next();
            if (byName.endsWith(name)) {
                return (String) macros.get(byName);
            }
        }
        return null;
    }

    public final static void removeMacroByName(final String name) {

        final Set<Object> macroSet = macros.keySet();
        final Iterator<Object> macroIterator = macroSet.iterator();
        String byName = null;
        while (macroIterator.hasNext()) {
            byName = (String) macroIterator.next();
            if (byName.endsWith(name)) {
                macros.remove(byName);
                saveMacros();
                return;
            }
        }
    }

    /**
     * Add the macro keystrokes to the macros list.
     *
     * This method is a destructive where if the macro already exists it will be
     *   overwritten.
     *
     * @param name
     * @param keyStrokes
     */
    public final static void setMacro(final String name, final String keyStrokes) {

        int x = 0;

        // first let's go through all the macros and replace the macro entry if it
        //   already exists.
        if (macrosExist && getMacroByName(name) != null) {
            final Set<Object> macroSet = macros.keySet();
            final Iterator<Object> macroIterator = macroSet.iterator();
            String byName = null;
            String prefix = null;
            while (macroIterator.hasNext()) {
                byName = (String) macroIterator.next();
                if (byName.endsWith(name)) {
                    //  we need to obtain the prefix so that we can replace
                    //   the slot with the new keystrokes.  If not the keymapping
                    //   will not work correctly.
                    prefix = byName.substring(0, byName.indexOf(name));
                    macros.put(prefix + name, keyStrokes);
                }
            }
        } else {
            // If it did not exist and get replaced then we need to find the next
            //  available slot to place the macro in.
            while (getMacroByNumber(++x) != null) {
            }

            macros.put("macro" + x + "." + name, keyStrokes);
            macrosExist = true;
        }
        saveMacros();

    }

    public static void showRunScriptDialog(final SessionGui session) {
        final TextField text = new TextField();
        final BorderPane borderPane = new BorderPane();

        final Label label = new Label("Enter script to run");
        borderPane.setLeft(label);
        BorderPane.setAlignment(label, Pos.CENTER);
        BorderPane.setMargin(label, new Insets(0, 5, 0, 0));

        borderPane.setCenter(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        final HBox content = new HBox(borderPane);

        final Alert alert = UiUtils.createInputDialog(content, "Run", "Cancel");
        alert.setTitle("Run Script");

        // now we can process the value selected
        if (alert.showAndWait().orElse(null) == ButtonType.OK) {
            final String value = text.getText();
            // send option along with system request
            if (value.length() > 0) {
                invoke(value, session);
            }
        }
    }

    public final static void invoke(String macro, final SessionGui session) {

        final String keys = getMacroByName(macro);
        if (keys != null)
            session.getScreen().sendKeys(keys);
        else {
            try {
                if (!macro.endsWith(".py"))
                    macro = macro + ".py";
                InterpreterDriverManager.executeScriptFile(session, "scripts" +
                        File.separatorChar + macro);
            } catch (final Exception ex) {
                System.err.println(ex);
            }
        }
    }

    private static boolean checkScripts() {

        final File directory = new File("scripts");

        final File directory2 = new File(ConfigureFactory.getInstance().getProperty(
                "emulator.settingsDirectory") +
                "scripts");


        return directory.isDirectory() || directory2.isDirectory();

    }

}
