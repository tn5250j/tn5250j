/**
 * $Id$
 * <p>
 * Title: tn5250J
 * Copyright:   Copyright (c) 2001,2009
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
package org.tn5250j.gui;

import java.text.CollationKey;
import java.text.Collator;
import java.util.Set;
import java.util.TreeSet;

import org.tn5250j.encoding.ICodePage;
import org.tn5250j.tools.LangTool;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

/**
 * Shows a dialog, containing all HEX values and their corresponding chars.
 */
public class HexCharMapDialog {

    private final ListView<String> hexList = new ListView<>();

    public HexCharMapDialog(final ICodePage codepage) {
        assert codepage != null : new IllegalArgumentException("A codepage is needed!");

        // we will use a collator here so that we can take advantage of the locales
        final Collator collator = Collator.getInstance();
        CollationKey key = null;

        final Set<CollationKey> set = new TreeSet<CollationKey>();
        final StringBuilder sb = new StringBuilder();
        for (int x = 0; x < 256; x++) {
            final char ac = codepage.ebcdic2uni(x);
            if (!Character.isISOControl(ac)) {
                sb.setLength(0);
                if (Integer.toHexString(ac).length() == 1) {
                    sb.append("0x0" + Integer.toHexString(ac).toUpperCase());
                } else {
                    sb.append("0x" + Integer.toHexString(ac).toUpperCase());
                }

                sb.append(" - " + ac);
                key = collator.getCollationKey(sb.toString());

                set.add(key);
            }
        }

        for (final CollationKey keyc : set) {
            hexList.getItems().add(keyc.getSourceString());
        }
        hexList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        hexList.getSelectionModel().selectFirst();
    }

    /**
     * Displays the dialog
     *
     * @return a String, containing the selected character OR null, if nothing was selected
     */
    public String showModal() {
        final BorderPane scp = new BorderPane();
        scp.setCenter(hexList);

        final Alert dialog = UiUtils.createInputDialog(scp, LangTool.getString("hm.optInsert"),
                LangTool.getString("hm.optCancel"));
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle(LangTool.getString("hm.title"));
        if (dialog.showAndWait().orElse(null) == ButtonType.OK) {
            final String selval = hexList.getSelectionModel().getSelectedItem();
            return selval == null ? null : selval.substring(selval.length() - 1);
        }
        return null;
    }
}
