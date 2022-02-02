/*
 * Title: KeyConfigure
 * Copyright:   Copyright (c) 2001
 * Company:
 * @author  Kenneth J. Pouncey
 * @version 0.4
 *
 * Description:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILreITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */
package org.tn5250j.keyboard.configure;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.CollationKey;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.tn5250j.encoding.ICodePage;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.keyboard.KeyMapper;
import org.tn5250j.keyboard.KeyMnemonicResolver;
import org.tn5250j.keyboard.KeyStroker;
import org.tn5250j.scripting.InterpreterDriverManager;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.system.OperatingSystem;

import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class KeyConfigureController implements Initializable {
    private static final SortedMap<Integer, String> colorMap = new TreeMap<Integer, String>();

    private final KeyMnemonicResolver keyMnemonicResolver = new KeyMnemonicResolver();

    @FXML
    private BorderPane view;
    @FXML
    private TitledBorderedPane mapToPanel;
    @FXML
    private TitledBorderedPane descriptionPanel;

    @FXML
    private TextField strokeDesc;
    @FXML
    private TextField strokeDescAlt;
    @FXML
    private Label strokeLocation;
    @FXML
    private Label strokeLocationAlt;

    @FXML
    private ListView<Object> functions;
    @FXML
    private ComboBox<String> whichKeys;

    @FXML
    private Button primaryKeyMapButton;
    @FXML
    private Button primaryRemoveButton;
    @FXML
    private Button doneButton;
    @FXML
    private Button altKeyMapButton;
    @FXML
    private Button altRemoveButton;

    private boolean mods;
    private String[] macrosList;
    private boolean macros;
    private boolean special;
    private ICodePage codePage;
    private boolean isLinux;
    private boolean isAltGr;
    private boolean altKey;

    static {
        colorMap.put(0x20, "Green");
        colorMap.put(0x21, "Green RI");
        colorMap.put(0x22, "White");
        colorMap.put(0x23, "White RI");
        colorMap.put(0x24, "Green UL");
        colorMap.put(0x25, "Green RI UL");
        colorMap.put(0x26, "White UL");
        colorMap.put(0x27, "NonDisplay");
        colorMap.put(0x28, "Red");
        colorMap.put(0x29, "Red RI");
        colorMap.put(0x2A, "Red BL");
        colorMap.put(0x2B, "Red RI BL");
        colorMap.put(0x2C, "Red UL");
        colorMap.put(0x2D, "Red UL RI");
        colorMap.put(0x2E, "Red UL BL");
        colorMap.put(0x30, "Turquoise CS");
        colorMap.put(0x31, "Turquoise CS RI");
        colorMap.put(0x32, "Yellow CS");
        colorMap.put(0x33, "Yellow CS RI");
        colorMap.put(0x34, "Turquoise UL");
        colorMap.put(0x35, "Turquoise UL RI ");
        colorMap.put(0x36, "Yellow UL");
        colorMap.put(0x38, "Pink");
        colorMap.put(0x39, "Pink RI");
        colorMap.put(0x3A, "Blue");
        colorMap.put(0x3B, "Blue RI");
        colorMap.put(0x3C, "Pink UL");
        colorMap.put(0x3D, "Pink UL RI");
        colorMap.put(0x3E, "Blue UL");
    }

    public KeyConfigureController(final String[] macros, final ICodePage cp) {

        codePage = cp;
        macrosList = macros;

        if (OperatingSystem.isUnix() && !OperatingSystem.isMacOS()) {
            isLinux = true;
        }

        try {
            KeyMapper.init();
        } catch (final Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        initFunctionsPanel();
        initMappingPanel();

        // add option buttons to options panel
        initOptButton(doneButton, LangTool.getString("key.labelDone", "Done"), "DONE", true);
    }

    private void initFunctionsPanel() {
        // add list selection listener to our functions list so that we
        //   can display the mapped key(s) to the function when a new
        //   function is selected.
        final ListChangeListener<Object> listener = e -> {
            final Object selectedItem = functions.getSelectionModel().getSelectedItem();
            if (selectedItem != null) {
                setKeyDescription(selectedItem);
            }
        };

        functions.getSelectionModel().getSelectedItems().addListener(listener);

        loadList(LangTool.getString("key.labelKeys"));

        functions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        whichKeys.getItems().add(LangTool.getString("key.labelKeys"));
        whichKeys.getItems().add(LangTool.getString("key.labelMacros"));
        whichKeys.getItems().add(LangTool.getString("key.labelSpecial"));

        whichKeys.getSelectionModel().selectFirst();
        whichKeys.setOnAction(e -> {
            final String value = whichKeys.getValue();
            if (value != null) {
                loadList(value);
            }
        });

        descriptionPanel.setTitle(LangTool.getString("key.labelDesc"));
    }

    private void initMappingPanel() {

        // set the descriptions defaults
        strokeDesc.setEditable(false);
        strokeDescAlt.setEditable(false);

        // create main panel
        mapToPanel.setTitle(LangTool.getString("key.labelMapTo"));

        // add the option buttons
        initOptButton(primaryKeyMapButton, LangTool.getString("key.labelMap", "Map Key"), "MAP-Prime", true);
        initOptButton(primaryRemoveButton, LangTool.getString("key.labelRemove", "Remove"), "REMOVE-Prime", true);

        // create the alternate description panel
        // add the options to the description panel
        initOptButton(altKeyMapButton, LangTool.getString("key.labelMap", "Map Key"), "MAP-Alt", true);
        initOptButton(altRemoveButton, LangTool.getString("key.labelRemove", "Remove"), "REMOVE-Alt", true);
    }

    private void setKeyDescription(final Object item) {
        if (!macros && !special) {
            final KeyDescription kd = (KeyDescription) item;
            setKeyInformation(keyMnemonicResolver.getMnemonics()[kd.getIndex()]);
        } else {
            if (macros) {
                if (item instanceof String) {
                    System.out.println((String) item);
                    setKeyInformation((String) item);
                } else if (item instanceof Macro) {
                    final Macro m = (Macro) item;
                    setKeyInformation(m.getFullName());
                }
            }

            if (special) {
                System.out.println(item);
                final String k = parseSpecialCharacter((String) item);
                setKeyInformation(k);
            }
        }
    }

    private void setKeyInformation(String keyDesc) {

        if (keyDesc.endsWith(KeyStroker.altSuffix)) {

            keyDesc = keyDesc.substring(0, keyDesc.indexOf(KeyStroker.altSuffix));
        }

        strokeDesc.setText(KeyMapper.getKeyStrokeDesc(keyDesc));
        strokeDescAlt.setText(KeyMapper.getKeyStrokeDesc(keyDesc +
                KeyStroker.altSuffix));

        strokeLocation.setText(getLocationDesc(keyDesc));
        strokeLocationAlt.setText(getLocationDesc(keyDesc + KeyStroker.altSuffix));
    }

    private String getLocationDesc(final String keyDesc) {

        String locStr = LangTool.getString("key.labelLocUnknown");

        if (KeyMapper.isKeyStrokeDefined(keyDesc)) {

            switch (KeyMapper.getKeyStroker(keyDesc).getLocation()) {

                case KeyStroker.KEY_LOCATION_LEFT:
                    locStr = LangTool.getString("key.labelLocLeft");
                    break;

                case KeyStroker.KEY_LOCATION_RIGHT:
                    locStr = LangTool.getString("key.labelLocRight");
                    break;

                case KeyStroker.KEY_LOCATION_STANDARD:
                    locStr = LangTool.getString("key.labelLocStandard");
                    break;

                case KeyStroker.KEY_LOCATION_NUMPAD:
                    locStr = LangTool.getString("key.labelLocNumPad");
                    break;

            }
        }

        return locStr;
    }

    private String parseSpecialCharacter(final String value) {

        final StringTokenizer tokenizer = new StringTokenizer(value, "-");

        if (tokenizer.hasMoreTokens()) {
            final String first = tokenizer.nextToken();
            return String.valueOf(value.charAt(first.length() + 2));
        }

        return "";
    }

    private void loadList(final String which) {
        functions.getItems().clear();

        if (which.equals(LangTool.getString("key.labelKeys"))) {
            final List<KeyDescription> keys = new LinkedList<>();
            int index = 0;
            for (final String key : keyMnemonicResolver.getMnemonics()) {
                keys.add(new KeyDescription(LangTool.getString("key." + key), index));
                index++;
            }

            Collections.sort(keys, new KeyDescriptionCompare());
            functions.getItems().addAll(keys);

            macros = false;
            special = false;
        } else {
            if (which.equals(LangTool.getString("key.labelMacros"))) {
                final List<Object> macrosVector = new LinkedList<>();
                if (macrosList != null) {
                    for (int x = 0; x < macrosList.length; x++) {
                        macrosVector.add(macrosList[x]);
                    }
                }

                scriptDir("scripts", macrosVector);
                loadListModel(macrosVector, null, 0);

                macros = true;
                special = false;
            } else {

                // we will use a collator here so that we can take advantage of the locales
                final Collator collator = Collator.getInstance();
                CollationKey key = null;
                final StringBuffer sb = new StringBuffer();

                final Set<CollationKey> set = new TreeSet<CollationKey>();

                supportAplColorCodesInSEU(collator, sb, set);

                for (int x = 0; x < 256; x++) {
                    final char c = codePage.ebcdic2uni(x);
                    final char ac = codePage.ebcdic2uni(x);
                    if (!Character.isISOControl(c)) {
                        sb.setLength(0);
                        if (Integer.toHexString(ac).length() == 1) {
                            sb.append("0x0" + Integer.toHexString(ac).toUpperCase());
                        } else {
                            sb.append("0x" + Integer.toHexString(ac).toUpperCase());
                        }

                        sb.append(" - " + c + " - " + getUnicodeString(c));
                        key = collator.getCollationKey(sb.toString());

                        set.add(key);
                    }
                }

                for (final CollationKey keyc : set) {
                    functions.getItems().add(keyc.getSourceString());
                }

                macros = false;
                special = true;

            }
        }
        if (!functions.getItems().isEmpty()) {
            functions.getSelectionModel().selectFirst();
        }
    }

    private void supportAplColorCodesInSEU(final Collator collator, final StringBuffer sb, final Set<CollationKey> set) {
        for (final Entry<Integer, String> color : colorMap.entrySet()) {
            final int keyVal = color.getKey().intValue();
            final char c = (char) ('\uff00' + keyVal);

            sb.setLength(0);
            sb.append("0FF" + Integer.toHexString(keyVal).toUpperCase());
            sb.append(" - " + c + " - " + color.getValue());
            final CollationKey key = collator.getCollationKey(sb.toString());

            set.add(key);
        }
    }

    private String getUnicodeString(final char c) {

        String s = Integer.toHexString(c).toUpperCase();
        final int len = s.length();
        switch (len) {

            case 2:
                s = "'\\u00" + s + "'";
                break;
            case 3:
                s = "'\\u0" + s + "'";
                break;
            default:
                s = "'\\u" + s + "'";

        }

        return s;
    }

    private void initOptButton(final Button button, final String text, final String ac, final boolean enabled) {
        button.setDisable(!enabled);
        button.setUserData(ac);
        button.setOnAction(this::actionPerformed);
        button.setText(text);
    }

    private void actionPerformed(final ActionEvent e) {
        final Object cmd = ((Button) e.getSource()).getUserData();

        if ("DONE".equals(cmd)) {
            if (mods) {
                KeyMapper.saveKeyMap();
                KeyMapper.fireKeyChangeEvent();
            }
            closeOwnedWindow();
        } else if ("MAP".equals(cmd)) {
            mapIt();
        } else if ("REMOVE".equals(cmd)) {
            removeIt();
        } else if ("MAP-Prime".equals(cmd)) {
            altKey = false;
            mapIt();
        } else if ("REMOVE-Prime".equals(cmd)) {
            altKey = false;
            removeIt();
        } else if ("MAP-Alt".equals(cmd)) {
            altKey = true;
            mapIt();
        } else if ("REMOVE-Alt".equals(cmd)) {
            altKey = true;
            removeIt();
        }
    }

    private void closeOwnedWindow() {
        view.getScene().getWindow().hide();
    }

    private void mapIt() {
        final DialogPane dialogPane = new DialogPane();
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.setHeaderText("");

        final KeyGetter kg = new KeyGetter();
        kg.setTextFill(Color.BLUE);
        kg.setText(LangTool.getString("key.labelMessage") + "functions");

        dialogPane.setContent(kg);

        UiUtils.changeButtonText(dialogPane, ButtonType.CLOSE, LangTool.getString("key.labelClose"));

        final Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(LangTool.getString("sa.title"));

        dialog.setDialogPane(dialogPane);
        dialog.setWidth(400);
        dialog.setHeight(400);
        kg.setDialog(dialog);
        kg.requestFocus();

        dialog.setOnCloseRequest(e -> {
            if (isAvailable(kg.keyevent)) {
                setNewKeyStrokes(kg.keyevent);
            }
        });
        dialog.show();
    }

    private boolean isAvailable(final KeyEvent ke) {

        boolean exists = true;

        if (isLinux) {
            exists = KeyMapper.isKeyStrokeDefined(ke, isAltGr);
        } else {
            exists = KeyMapper.isKeyStrokeDefined(ke);
        }

        if (exists) {

            final String message = LangTool.messageFormat("messages.mapKeyWarning", getKeyDescription(ke));
            final Alert alert = new Alert(AlertType.WARNING, message, ButtonType.YES, ButtonType.NO);
            alert.setTitle(LangTool.getString("key.labelKeyExists"));
            alert.setHeaderText("");

            return alert.showAndWait().orElse(null) == ButtonType.YES;
        }
        return !exists;
    }

    private String getKeyDescription(final KeyEvent ke) {

        String desc;

        if (isLinux)
            desc = KeyMapper.getKeyStrokeMnemonic(ke, isAltGr);
        else
            desc = KeyMapper.getKeyStrokeMnemonic(ke, isAltGr);

        if (desc != null && desc.length() > 1 && desc.startsWith("["))
            desc = LangTool.getString("key." + desc);

        return desc;
    }

    private void removeIt() {
        if (!macros && !special) {
            final int index = ((KeyDescription) functions.getSelectionModel().getSelectedItem()).getIndex();

            String function = keyMnemonicResolver.getMnemonics()[index];

            if (altKey)
                function += KeyStroker.altSuffix;

            KeyMapper.removeKeyStroke(function);
            setKeyInformation(function);


        } else {

            if (macros) {
                final Object o = functions.getSelectionModel().getSelectedItem();
                String name;
                if (o instanceof Macro) {
                    name = ((Macro) o).getFullName();
                } else {
                    name = (String) o;
                }

                if (altKey)
                    name += KeyStroker.altSuffix;

                KeyMapper.removeKeyStroke(name);
                setKeyInformation(name);
            }
            if (special) {
                String k = "";
                k += ((String) functions.getSelectionModel().getSelectedItem()).charAt(7);
                if (altKey)
                    k += KeyStroker.altSuffix;

                KeyMapper.removeKeyStroke(k);
                setKeyInformation(k);
            }
        }
        mods = true;

    }

    private void setNewKeyStrokes(final KeyEvent ke) {

        if (!macros && !special) {
            final int index = ((KeyDescription) functions.getSelectionModel().getSelectedItem()).getIndex();
            String stroke = keyMnemonicResolver.getMnemonics()[index];

            if (altKey)
                stroke += KeyStroker.altSuffix;

            if (isLinux) {
                KeyMapper.setKeyStroke(stroke, ke, isAltGr);
            } else {
                KeyMapper.setKeyStroke(stroke, ke);
            }

            setKeyInformation(stroke);

        } else {
            if (macros) {
                final Object o = functions.getSelectionModel().getSelectedItem();
                String macro;
                if (o instanceof Macro)
                    macro = ((Macro) o).getFullName();
                else
                    macro = (String) o;

                if (altKey)
                    macro += KeyStroker.altSuffix;

                System.out.println(macro);
                if (isLinux)
                    KeyMapper.setKeyStroke(macro, ke, isAltGr);
                else
                    KeyMapper.setKeyStroke(macro, ke);

                setKeyInformation(macro);

            }

            if (special) {
                System.out.println((String) functions.getSelectionModel().getSelectedItem());
                String k = parseSpecialCharacter((String) functions.getSelectionModel().getSelectedItem());

                if (altKey)
                    k += KeyStroker.altSuffix;

                KeyMapper.removeKeyStroke(k);

                if (isLinux) {
                    KeyMapper.setKeyStroke(k, ke, isAltGr);
                } else {
                    KeyMapper.setKeyStroke(k, ke);
                }

                setKeyInformation(k);

            }

        }

        mods = true;
    }

    private static class KeyDescriptionCompare implements Comparator<KeyDescription> {

        @Override
        public int compare(final KeyDescription one, final KeyDescription two) {
            final String s1 = one.toString();
            final String s2 = two.toString();
            return s1.compareToIgnoreCase(s2);
        }

    }

    private class KeyDescription {

        private int index;
        private String text;

        public KeyDescription(final String text, final int index) {

            this.text = text;
            this.index = index;

        }

        @Override
        public String toString() {

            return text;
        }

        public int getIndex() {
            return index;
        }
    }


    public static void scriptDir(final String pathName, final List<Object> scripts) {

        final File root = new File(pathName);

        try {

            loadScripts(scripts, root.getCanonicalPath(), root);

        } catch (final IOException ioe) {
            System.out.println(ioe.getMessage());

        }


    }

    /**
     * Recursively read the scripts directory and add them to our macros vector
     * holding area
     *
     * @param vector
     * @param path
     * @param directory
     */
    private static void loadScripts(final List<Object> vector,
                                    final String path,
                                    final File directory) {

        Macro macro;

        final File[] macroFiles = directory.listFiles();
        if (macroFiles == null || macroFiles.length == 0)
            return;

        Arrays.sort(macroFiles, new MacroCompare());

        for (int i = 0; i < macroFiles.length; i++) {
            final File file = macroFiles[i];
            final String fileName = file.getName();
            if (file.isHidden()) {
                /* do nothing! */
                continue;
            } else if (file.isDirectory()) {
                final List<Object> subvector = new LinkedList<>();
                subvector.add(fileName.replace('_', ' '));
                loadScripts(subvector, path + fileName + '/', file);
                // if we do not want empty directories to show up uncomment this
                //    line.  It is uncommented here.
                if (subvector.size() != 1)
                    vector.add(subvector);
            } else {
                if (InterpreterDriverManager.isScriptSupported(fileName)) {
                    String fn = fileName.replace('_', ' ');
                    final int index = fn.lastIndexOf('.');
                    if (index > 0) {
                        fn = fn.substring(0, index);
                    }

                    macro = new Macro(fn, file.getAbsolutePath(), fileName);
                    vector.add(macro);
                }
            }
        }

    }

    /**
     * Load the ListModel with the scripts from the vector of macros provided
     * @param vector
     * @param start
     * @param menu
     */
    private void loadListModel(final List<Object> vector,
                                      final String prefix,
                                      final int start) {

        for (final Object obj : vector) {
            if (obj instanceof Macro) {
                ((Macro) obj).setPrefix(prefix);
                functions.getItems().add(obj);
            } else if (obj instanceof List) {
                @SuppressWarnings("unchecked")
                final List<Object> subvector = (List<Object>) obj;
                final String name = (String) subvector.get(0);
                if (prefix != null) {
                    loadListModel(subvector, prefix + '/' + name + '/', 1);
                } else {
                    loadListModel(subvector, name + '/', 1);
                }
            } else if (obj instanceof String) {
                functions.getItems().add(obj);
            }
        }
    }

    private static class Macro {

        String name;
        String path;
        String prefix;
        String fileName;

        Macro(final String name, final String path, final String fileName) {

            this.name = name;
            this.path = path;
            this.fileName = fileName;
        }

        /**
         * Setst the directory prefix
         *
         * @param prefix before the name
         */
        public void setPrefix(final String prefix) {
            this.prefix = prefix;
        }

        /**
         * This function gets the full name representation of the macro
         *
         * @return the full name non prettied up
         */
        public String getFullName() {
            if (prefix != null)
                return prefix + fileName;
            else
                return fileName;
        }

        /**
         * This function is used for display of the macro name prettied up
         *
         * @return pretty string
         */
        @Override
        public String toString() {

            if (prefix != null)
                return prefix + name;
            else
                return name;
        }
    }

    public static class MacroCompare implements Comparator<File> {
        @Override
        public int compare(final File one, final File two) {
            final String s1 = one.toString();
            final String s2 = two.toString();
            return s1.compareToIgnoreCase(s2);
        }

    }
}
