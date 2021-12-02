/**
 *
 */
package org.tn5250j.connectdialog;

import javafx.scene.control.ListCell;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
class MnemonicListViewItem extends ListCell<MnemonicItem> {
    MnemonicListViewItem() {
        super();
    }

    @Override
    protected void updateItem(final MnemonicItem item, final boolean empty) {
        super.updateItem(item, empty);
        setGraphic(null);

        if (empty || item == null) {
            setText(null);
        } else {
            setText(item.getDescription());
        }
    }
}
