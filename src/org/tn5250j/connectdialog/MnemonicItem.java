/**
 *
 */
package org.tn5250j.connectdialog;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
class MnemonicItem implements Comparable<MnemonicItem> {
    private final String name;
    private final String description;

    MnemonicItem(final String name, final String description) {
        super();
        this.name = name;
        this.description = description;
    }

    String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    @Override
    public int compareTo(final MnemonicItem o) {
        return getDescription().compareTo(o.getDescription());
    }
}
