/*
 * @(#)Wizard.java
 * Copyright:    Copyright (c) 2001
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software; see the file COPYING.  If not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 *
 */

package org.tn5250j.gui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.tn5250j.event.WizardEvent;
import org.tn5250j.event.WizardListener;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

/**
 * Class to create and manage a <i>Wizard</i> style framework.  Create and add
 * <code>WizardPages</code> and add <code>WizardListener</code>'s for all your Wizard fun.
 */
public class Wizard {
    /**
     * list of wizard listeners registered with the bean
     */
    protected final List<WizardListener> listeners = new LinkedList<>();
    private final Map<WizardPage, Integer> childIndexes = new HashMap<>();
    private WizardPage currentPage;
    private final BorderPane content = new BorderPane();
    private final StackPane stackPane = new StackPane();

    /**
     * Create a <code>Wizard</code> component.
     */
    public Wizard() {
        super();
        stackPane.setMaxHeight(Double.POSITIVE_INFINITY);
        stackPane.setMaxWidth(Double.POSITIVE_INFINITY);
        content.setCenter(stackPane);
    }

    /**
     * Moves to the wizard page with the specified name.
     *
     * @param name Name of the page when it was added to the JCWizard.
     * @see #first
     * @see #last
     */
    public void show(final String name) {
        for (final WizardPage node : getWizardPages()) {
            if (node.getName().equals(name)) {
                setCurrentPage(node);
                break;
            }
        }
    }

    /**
     * @param wp new current page.
     */
    private void setCurrentPage(final WizardPage wp) {
        currentPage = wp;
        wp.toFront();
    }

    /**
     * Moves to the first page in the Wizard.
     *
     * @see #last
     * @see #show
     */
    public void first() {
        getPage(0).toFront();
    }

    private WizardPage getPage(final int index) {
        for (final WizardPage node : getWizardPages()) {
            if (childIndexes.get(node).intValue() == index) {
                return node;
            }
        }
        return null;
    }

    /**
     * Moves to the last page in the Wizard.
     *
     * @see #first
     * @see #show
     */
    public void last() {
        getPage(childIndexes.size() - 1).toFront();
    }

    /**
     * Advance to the next page
     *
     * @see #previous
     * @see #finish
     * @see #cancel
     */
    public boolean next() {
        final List<WizardPage> pages = getWizardPages();
        final int index = pages.indexOf(currentPage);

        final boolean isLastPage = index == pages.size() - 1;

        final WizardPage nextPage = isLastPage ? null : pages.get(index + 1);
        if (nextPage == null) {
            return false;
        }

        final WizardEvent event = new WizardEvent(this, currentPage, nextPage,
                isLastPage, !isLastPage);

        // in the preceding constructor, by default, we want
        // to prevent wraparound to first card from the
        // last card so we set "allow_change" to be the
        // opposite of "is_last_page"

        //
        // post nextBegin event
        //
        for (final WizardListener l : listeners) {
            l.nextBegin(event);
        }

        if (event.getAllowChange() == false) {
            return false;
        }

        //
        // advance to the next page unless a new page has been specified
        //
        setCurrentPage(event.getNewPage());

        //
        // Post nextComplete event
        //
        for (final WizardListener l : listeners) {
            l.nextComplete(event);
        }

        return true;
    }

    /**
     * Move to the previous page
     *
     * @see #next
     * @see #finish
     * @see #cancel
     */
    public boolean previous() {
        final List<WizardPage> pages = getWizardPages();
        final int index = pages.indexOf(currentPage);

        final boolean isLastPage = index == pages.size() - 1;
        final boolean isFirstPage = index == 0;

        final WizardPage previousPage = isFirstPage ? null : pages.get(index - 1);
        if (previousPage == null) {
            return false;
        }

        final WizardEvent event = new WizardEvent(this, currentPage, previousPage,
                isLastPage, !isFirstPage);

        // in the preceding constructor, by default, we want
        // to prevent wraparound to the last card from the
        // first card so we set "allow_change" to be the
        // opposite of "is_first_page"

        for (final WizardListener l : listeners) {
            //
            // post previousBegin event
            //
            l.previousBegin(event);
        }

        if (!event.getAllowChange()) {
            return false;
        }

        setCurrentPage(event.getNewPage());

        //
        // Post previousComplete event
        //
        for (final WizardListener l : listeners) {
            l.previousComplete(event);
        }
        return true;
    }

    /**
     * Invokes the registered "finish" action.
     *
     * @see #next
     * @see #previous
     * @see #cancel
     */
    public boolean finish() {
        final WizardPage comp = getCurrentPage();
        final WizardEvent event = new WizardEvent(this, comp, null,
                isLastPage(comp), true);

        //
        // Post finished event
        //
        for (final WizardListener l : listeners) {
            l.finished(event);
        }

        return event.getAllowChange();
    }

    /**
     * Invokes the registered "cancel" action.
     *
     * @see #next
     * @see #previous
     * @see #finish
     */
    public boolean cancel() {
        final WizardPage comp = getCurrentPage();
        final WizardEvent event = new WizardEvent(this, comp, null,
                isLastPage(comp), true);

        //
        // Post Canceled event
        //
        for (final WizardListener l : listeners) {
            l.canceled(event);
        }

        return event.getAllowChange();
    }

    /**
     * Invokes the registered "help" action.
     *
     * @see #next
     * @see #previous
     * @see #finish
     * @see #cancel
     */
    public void help() {
        final WizardPage comp = getCurrentPage();
        final WizardEvent event = new WizardEvent(this, comp, null, isLastPage(comp), true);
        //
        // Post Help event
        //
        for (final WizardListener l : listeners) {
            l.help(event);
        }
    }

    private boolean isLastPage(final WizardPage comp) {
        if (comp == null) {
            return false;
        }
        return childIndexes.get(comp).intValue() == childIndexes.size() - 1;
    }

    /**
     * Retrieves the current visible page.
     */
    protected WizardPage getCurrentPage() {
        return currentPage;
    }

    /**
     * Adds a new <code>WizardListener</code> to the list.
     */
    public void addWizardListener(final WizardListener l) {
        listeners.add(l);
    }

    /**
     * Removes a <code>ValidateListener</code> from the list.
     */
    public void removeWizardListener(final WizardListener l) {
        listeners.remove(l);
    }

    /**
     * A listener on the "next" button that is implemented as an anonymous
     * inner class that simply invokes the containing classes "next()"
     * method.
     *
     * @see #next
     */
    protected final EventHandler<ActionEvent> nextListener = e -> next();

    /**
     * A listener on the "previous" button that is implemented as an anonymous
     * inner class that simply invokes the containing classes "previous()"
     * method.
     *
     * @see #previous
     */
    protected final EventHandler<ActionEvent> previousListener = e -> previous();

    /**
     * A listener on the "finish" button that is implemented as an anonymous
     * inner class that simply invokes the containing classes "finish()"
     * method.
     *
     * @see #finish
     */
    protected final EventHandler<ActionEvent> finishListener = e -> finish();

    /**
     * A listener on the "cancel" button that is implemented as an anonymous
     * inner class that simply invokes the containing classes "cancel()"
     * method.
     *
     * @see #cancel
     */
    protected final EventHandler<ActionEvent> cancelListener = e -> cancel();

    /**
     * A listener on the "help" button that is implemented as an anonymous
     * inner class that simply invokes the containing classes "help()"
     * method.
     *
     * @see #help
     */
    protected final EventHandler<ActionEvent> helpListener = e -> help();

    private void startListen(final Button button, final EventHandler<ActionEvent> handler) {
        if (button != null) {
            button.setOnAction(handler);
        }
    }

    private void stopListen(final Button button) {
        if (button != null) {
            button.setOnAction(null);
        }
    }

    public void add(final WizardPage wp) {
        stackPane.getChildren().add(wp);
        startListen(wp.getNextButton(), nextListener);
        startListen(wp.getPreviousButton(), previousListener);
        startListen(wp.getFinishButton(), finishListener);
        startListen(wp.getCancelButton(), cancelListener);
        startListen(wp.getHelpButton(), helpListener);

        childIndexes.put(wp, getWizardPagesCount() - 1);
    }

    public void remove(final WizardPage wp) {
        stackPane.getChildren().remove(wp);
        if (currentPage == wp) {
            currentPage = null;
        }

        stopListen(wp.getNextButton());
        stopListen(wp.getPreviousButton());
        stopListen(wp.getFinishButton());
        stopListen(wp.getCancelButton());
        stopListen(wp.getHelpButton());

        childIndexes.remove(wp);

        //reset orders
        final List<WizardPage> nodes = getWizardPages();

        int index = 0;
        for (final WizardPage n : nodes) {
            childIndexes.put(n, index);
            index++;
        }
    }

    private List<WizardPage> getWizardPages() {
        final List<WizardPage> pages = new LinkedList<>();
        for (final Node node : stackPane.getChildren()) {
            if (node instanceof WizardPage) {
                pages.add((WizardPage) node);
            }
        }
        pages.sort((n1, n2) -> childIndexes.get(n1).compareTo(childIndexes.get(n2)));
        return pages;
    }
    private int getWizardPagesCount() {
        int count = 0;
        for (final Node node : stackPane.getChildren()) {
            if (node instanceof WizardPage) {
                count++;
            }
        }
        return count;
    }

    public Pane getView() {
        return content;
    }
}
