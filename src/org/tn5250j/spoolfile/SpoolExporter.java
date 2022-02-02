/**
 * Title: SpoolExporter.java
 * Copyright:   Copyright (c) 2002
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
package org.tn5250j.spoolfile;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.JFrame;

import org.tn5250j.SessionGui;
import org.tn5250j.framework.tn5250.tnvt;
import org.tn5250j.gui.GenericTn5250JFrame;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.PrintObjectListEvent;
import com.ibm.as400.access.PrintObjectListListener;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.access.SpooledFileList;
import com.ibm.as400.vaccess.SpooledFileViewer;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public class SpoolExporter extends GenericTn5250JFrame {

    SpoolFilterPane filter;

    // table of spools to work on
    private final TableView<SpooledFileBean> spools = new TableView<>();

    // status line
    private final Label status = new Label();

    // AS400 connection
    AS400 system;

    // Connection vt
    private final tnvt vt;
    private final SessionGui session;

    SpooledFileList splfList;

    public SpoolExporter(final tnvt vt, final SessionGui session) {

        this.vt = vt;
        this.session = session;

        try {
            jbInit();
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {

        final BorderPane content = new BorderPane();

        final Scene scene = new Scene(content);
        scene.getStylesheets().add("/application.css");
        stage.setScene(scene);

        stage.setTitle(LangTool.getString("spool.title"));

        content.setTop(createFilterPanel());

//        UiUtils.setBackground(spools, Color.WHITE);
        spools.setTableMenuButtonVisible(true);
        spools.getColumns().addAll(createColumns());

        // create our mouse listener on the table
        spools.addEventHandler(MouseEvent.MOUSE_CLICKED, this::spools_mouseClicked);
        spools.addEventHandler(MouseEvent.MOUSE_PRESSED, this::showPopupMenu);
        //Create the scroll pane and add the table to it.
        spools.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        spools.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        content.setCenter(spools);

        status.setText("0 " + LangTool.getString("spool.count"));
        status.getStyleClass().add("etched-border");

        status.setMaxWidth(Double.POSITIVE_INFINITY);
        content.setBottom(status);

        packFrame = true;

        stage.setOnCloseRequest(e -> {
            // close the system connection
            if (system != null) {
                // close the spool file list if allocated
                if (splfList != null) {
                    splfList.close();
                    splfList = null;
                }

                system.disconnectAllServices();
            }
        });
    }

    private List<TableColumn<SpooledFileBean, ?>> createColumns() {
        final List<TableColumn<SpooledFileBean, ?>> columns = new LinkedList<>();

        //  Spool Name|100"
        columns.add(createTableColumn(bean -> bean.getPropertySpoolName(), "Spool Name", 100, 0));
        //  Spool Number|90"
        columns.add(createTableColumn(bean -> bean.getPropertyNumber(), "Spool Number", 90, 1));
        //  Job Name|100|"
        columns.add(createTableColumn(bean -> bean.getPropertyJobName(), "Job Name", 100, 2));
        //  Job User|100"
        columns.add(createTableColumn(bean -> bean.getPropertyJobUser(), "Job User", 100, 3));
        //  Job Number|90"
        columns.add(createTableColumn(bean -> bean.getPropertyjobNumber(), "Job Number", 90, 4));
        //  Queue|200"
        columns.add(createTableColumn(bean -> bean.getPropertyQueue(), "Queue", 200, 5));
        //  User Data|100"
        columns.add(createTableColumn(bean -> bean.getPropertyUserData(), "User Data", 100, 6));
        //  Status|100"
        columns.add(createTableColumn(bean -> bean.getPropertyStatus(), "Status", 100, 7));
        //  Total Pages|90"
        columns.add(createTableColumn(bean -> bean.getPropertyTotalPages(), "Total Pages", 90, 8));
        //  Current Page|90"
        columns.add(createTableColumn(bean -> bean.getPropertyCurrentPage(), "Current Page", 90, 9));
        //  Copies|90"
        columns.add(createTableColumn(bean -> bean.getPropertyCopies(), "Copies", 90, 10));
        //  Form Type|100"
        columns.add(createTableColumn(bean -> bean.getPropertyFormType(), "Form Type", 100, 11));
        //  Priority|40"
        columns.add(createTableColumn(bean -> bean.getPropertyPriority(), "Priority", 40, 12));
        //  Creation Date/Time|175"
        columns.add(createTableColumn(bean -> bean.getPropertyCreationDate(), "Creation Date/Time", 175, 13));
        //  Size|120";
        columns.add(createTableColumn(bean -> bean.getPropertySize(), "Size", 120, 14));

        return columns;
    }

    private <V> TableColumn<SpooledFileBean, V> createTableColumn(
            final Function<SpooledFileBean, ObservableValue<V>> accessor,
            final String title, final int prefSize, final int initColumnNumber) {
        final TableColumn<SpooledFileBean, V> col = new TableColumn<>(title);
        col.setPrefWidth(prefSize);
        col.setEditable(false);
        col.setSortable(true);
        col.setCellValueFactory(t -> accessor.apply(t.getValue()));
        col.setResizable(true);
        col.setUserData(initColumnNumber);
        return col;
    }

    private Pane createFilterPanel() {
        // create filter panel
        final TitledBorderedPane fp = new TitledBorderedPane();
        fp.setTitle(LangTool.getString("spool.filterTitle"));

        final BorderPane content = new BorderPane();
        content.setStyle("-fx-padding: 1em 0 0 0;");
        fp.getChildren().add(content);

        filter = new SpoolFilterPane();

        // create button selection panel
        final VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-padding: 0.5em 0.5em 0.5em 0.5em;");

        final HBox bp = new HBox();
        bp.setAlignment(Pos.CENTER);
        vbox.getChildren().add(bp);
        bp.setSpacing(5.);

        final Button load = new Button(LangTool.getString("spool.load"));
        final Button resetAll = new Button(LangTool.getString("spool.resetAll"));
        final Button reset = new Button(LangTool.getString("spool.resetPanel"));

        bp.getChildren().add(load);
        load.setOnAction(e -> runLoader());

        bp.getChildren().add(reset);
        reset.setOnAction(e -> filter.resetCurrent());

        bp.getChildren().add(resetAll);
        resetAll.setOnAction(e -> filter.resetAll());

        filter.setStyle("-fx-padding: 0.5em 0.5em 0.5em 0.5em;");

        content.setCenter(filter);
        content.setBottom(vbox);

        return fp;
    }

    private void runLoader() {
        final Runnable loader = new Runnable() {
            @Override
            public void run() {
                loadSpoolFiles();
            }
        };

        final Thread t = new Thread(loader);
        t.setDaemon(true);
        t.start();
    }

    private void loadSpoolFiles() {
        spools.setCursor(Cursor.WAIT);

        if (splfList != null) {
            splfList.removePrintObjectListListener(listener);
            splfList.close();
            splfList = null;
        }

        // clear our data
        spools.getItems().clear();

        try {
            updateStatus(LangTool.getString("spool.working"));

            // get a system object
            if (system == null)
                system = new AS400(vt.getHostName());

            // create a spoolfile list
            splfList = new SpooledFileList(system);

            // set the filters for the spoolfile list
            splfList.setUserFilter(filter.getUser());
            splfList.setQueueFilter("/QSYS.LIB/" + filter.getLibrary() + ".LIB/" +
                    filter.getQueue() + ".OUTQ");

            if (filter.getUserData().length() > 0)
                splfList.setUserDataFilter(filter.getUserData());

            splfList.addPrintObjectListListener(listener);
            splfList.openAsynchronously();

            // if we have something update the status
            if (splfList != null) {
                updateStatus(splfList.size() + " " + LangTool.getString("spool.count"));
            }

        } catch (final Exception erp) {
            spools.setCursor(Cursor.DEFAULT);
            updateStatus(erp.getMessage(), true);
        }
    }

    /**
     * Show the popup menu of actions for the current table row.
     * @param me
     */
    private void showPopupMenu(final MouseEvent me) {
        if (me.getButton() != MouseButton.SECONDARY) {
            return;
        }
        final TableCell<?, ?> cell = getCell(me);
        if (cell == null) {
            return;
        }

        final ContextMenu popup = new ContextMenu();

        final int row = cell.getTableRow().getIndex();
        final int col = (Integer) cell.getTableColumn().getUserData();

        //      System.out.println(" column clicked " + col);
//      System.out.println(" column clicked to model " + spools.convertColumnIndexToModel(col));
        popup.getItems().add(createMenuItem("spool.optionView", cmd -> {
            System.out.println(row + " is selected ");
            spools.setCursor(Cursor.WAIT);
            Platform.runLater(() -> displayViewer(getSpooledFile(row)));
        }));

        popup.getItems().add(createMenuItem("spool.optionProps", cmd -> UiUtils.showWarning("Not Available yet", "Not yet")));
        popup.getItems().add(new SeparatorMenuItem());

        popup.getItems().add(createMenuItem("spool.optionProps", cmd -> {
            SpoolExportWizard sew;
            try {
                sew = new SpoolExportWizard(getSpooledFile(row),
                        session);
                sew.setVisible(true);
            } catch (final Exception e) {
                UiUtils.showError(e, "Error");
            }
        }));
        popup.getItems().add(new SeparatorMenuItem());

        switch (col) {
            case 0:
            case 3:
            case 6:
                popup.getItems().add(createMenuItem("spool.labelFilter", cmd -> setFilter(row, col)));
                popup.getItems().add(new SeparatorMenuItem());
                break;
        }

        popup.getItems().add(createMenuItem("spool.optionHold", cmd -> doSpoolStuff(getSpooledFile(row), cmd)));
        popup.getItems().add(createMenuItem("spool.optionRelease", cmd -> doSpoolStuff(getSpooledFile(row), cmd)));
        popup.getItems().add(createMenuItem("spool.optionDelete", cmd -> doSpoolStuff(getSpooledFile(row), cmd)));

        popup.show(this.getWindow(), me.getScreenX(), me.getScreenY());
    }
    private MenuItem createMenuItem(final String key, final Consumer<String> action) {
        final String name = LangTool.getString(key);
        final MenuItem mi = new MenuItem(name);
        mi.setOnAction(e -> action.accept(name));
        return mi;
    }

    /**
     * Return the spooledfile from the row given from the table
     *
     * @param row from the data vector to retreive from
     * @return Spooled File of selected row
     */
    private SpooledFile getSpooledFile(final int row) {
        final SpooledFileBean bean = this.spools.getItems().get(row);
        return bean.getFile();
    }

    /**
     * Take the appropriate action on the selected spool file
     * @param splf Spooled File to work on
     * @param action Action to take on the spooled file
     */
    private void doSpoolStuff(final SpooledFile splf, final String action) {

        try {
            if (action.equals(LangTool.getString("spool.optionHold")))
                splf.hold(null);

            if (action.equals(LangTool.getString("spool.optionRelease")))
                splf.release();

            if (action.equals(LangTool.getString("spool.optionDelete")))
                splf.delete();
        } catch (final Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    /**
     * Process the mouse event on the table
     * @param e Mouse event passed
     */
    void spools_mouseClicked(final MouseEvent e) {
        if (e.getClickCount() > 1) {
            final TableCell<?, ?> cell = getCell(e);
            if (cell != null) {
                final int row = cell.getTableRow().getIndex();
                spools.setCursor(Cursor.WAIT);
                Platform.runLater(() -> displayViewer(getSpooledFile(row)));
            }
        }
    }

    /**
     * Display the spooled file using the internal AS400 toolbox viewer
     *
     * @param splf SpooledFile to view
     */
    private void displayViewer(final SpooledFile splf) {
        //FIXME change to FX
        // Create the spooled file viewer
        final SpooledFileViewer sfv = new SpooledFileViewer(splf, 1);
        try {
            sfv.load();
            final JFrame viewer = new JFrame(LangTool.getString("spool.viewerTitle"));
            viewer.setIconImages(GUIGraphicsUtils.getApplicationIcons());

            viewer.getContentPane().add(sfv);
            viewer.pack();
            viewer.setVisible(true);
        } catch (final Exception exc) {
            updateStatus(exc.getMessage(), true);
        }

        spools.setCursor(Cursor.DEFAULT);
    }

    /**
     * Calls the filter object to set the appropriate filter in the filter options
     *
     * @param row
     * @param col
     */
    private void setFilter(final int row, final int col) {
        final SpooledFileBean bean = spools.getItems().get(row);

        switch (col) {
            case 0:
                filter.setSpoolName(bean.getSpoolName());
                break;
            case 3:
                filter.setUser(bean.getJobUser());
                break;
            case 6:
                filter.setUserData(bean.getUserData());
                break;
            default:
                break;

        }
    }

    /**
     * Update the status bar with the text.  If it is an error then change the
     * text color red else use black
     *
     * @param stat Message to display
     * @param error Whether it is an error message or not
     */
    private void updateStatus(final String stat, final boolean error) {
        Platform.runLater(() -> {
            if (error) {
                status.setTextFill(Color.RED);
            } else {
                status.setTextFill(Color.BLACK);
            }
            status.setText(stat);
        });
    }

    /**
     * Update the status bar with the text in normal color
     *
     * @param stat Message to display
     */
    private void updateStatus(final String stat) {
        updateStatus(stat, false);
    }

    /**
     * Custom table model used to display the spooled file list with the
     * attributes.
     *
     */
    private final PrintObjectListListener listener = new PrintObjectListListener() {

        @Override
        public void listClosed(final PrintObjectListEvent e) {
        }

        @Override
        public void listCompleted(final PrintObjectListEvent e) {
            spools.setCursor(Cursor.DEFAULT);
        }

        @Override
        public void listErrorOccurred(final PrintObjectListEvent e) {
            System.err.println("list error occurred : " + e.getException().getMessage());
        }

        @Override
        public void listObjectAdded(final PrintObjectListEvent e) {
            final boolean spoolFilter = filter.getSpoolName().length() > 0;
            final String spoolName = filter.getSpoolName();
            final SpooledFile p = (SpooledFile) e.getObject();

            // do not process if the name is not equal to the filter.
            if (spoolFilter && !spoolName.equals(p.getName()))
                return;

            spools.getItems().add(new SpooledFileBean(p));

            Platform.runLater(() -> {
                updateStatus(spools.getItems().size() + " " + LangTool.getString("spool.count"));
            });
        }

        @Override
        public void listOpened(final PrintObjectListEvent e) {
            System.out.println("list opened");
        }
    };

    private TableCell<?, ?> getCell(final MouseEvent e) {
        final Point2D p = new Point2D(e.getSceneX(), e.getSceneY());
        for (final Node n : spools.lookupAll(".table-cell")) {
            if (n.contains(n.sceneToLocal(p))) {
                return (TableCell<?,?>) n;
            }
        }

        return null;
    }
}
