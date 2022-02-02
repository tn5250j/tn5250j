package org.tn5250j.spoolfile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JPanel;

import org.tn5250j.SessionGui;
import org.tn5250j.event.WizardEvent;
import org.tn5250j.event.WizardListener;
import org.tn5250j.gui.GenericTn5250JFrame;
import org.tn5250j.gui.SwingToFxUtils;
import org.tn5250j.gui.TN5250jFileFilterBuilder;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.gui.Wizard;
import org.tn5250j.gui.WizardPage;
import org.tn5250j.mailtools.SendEMailDialog;
import org.tn5250j.tools.LangTool;

import com.ibm.as400.access.AS400;
import com.ibm.as400.access.IFSFileOutputStream;
import com.ibm.as400.access.PrintObject;
import com.ibm.as400.access.PrintObjectTransformedInputStream;
import com.ibm.as400.access.PrintParameterList;
import com.ibm.as400.access.SpooledFile;
import com.ibm.as400.vaccess.IFSFileDialog;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.FileChooser;

/**
 *
 */
public class SpoolExportWizard extends GenericTn5250JFrame implements WizardListener {
    BorderPane contentPane;
    Label statusBar = new Label();

    BorderPane spoolPanel = new BorderPane();
    GridPane spoolData = createGridPane();
    BorderPane spoolOptions = new BorderPane();

    JPanel destPanel = new JPanel();

    ComboBox<String> cvtType;
    TextField pcPathInfo;
    TextField ifsPathInfo;
    Button pcSave;
    Button ifsSave;

    RadioButton pc;
    RadioButton ifs;
    RadioButton email;

    // PDF Properties
    TextField title;
    TextField subject;
    TextField author;

    // PDF Options
    TextField fontSize;
    ComboBox<String> pageSize;
    RadioButton portrait;
    RadioButton landscape;

    // Text Options
    CheckBox openAfter;
    TextField editor;
    Button getEditor;

    // Spooled File
    SpooledFile splfile;

    // Session object
    SessionGui session;

    BorderPane twoPDF;
    BorderPane twoText;

    // Wizard
    Wizard wizard;
    WizardPage page;
    WizardPage pagePDF;
    WizardPage pageText;
    Button nextButton;

    // pdf variables
    private PdfWriter bos;
    private Document document;
    private com.lowagie.text.Font font;

    // output stream
    private FileOutputStream fw;
    private IFSFileOutputStream ifsfw;

    // conical path of file
    private String conicalPath;

    // exporting worker thread
    private Thread workingThread;

    //Construct the frame
    public SpoolExportWizard(final SpooledFile splfile, final SessionGui session) throws Exception {
        this.splfile = splfile;
        this.session = session;

        final BorderPane contentPane = new BorderPane();
        stage.setScene(new Scene(contentPane));

        // create ourselves a new wizard
        wizard = new Wizard();

        // create the event handler as being this module
        wizard.addWizardListener(this);

        // add our wizard to the frame
        contentPane.setCenter(wizard.getView());

        // create the first wizard page
        page = new WizardPage(WizardPage.NEXT |
                WizardPage.FINISH |
                WizardPage.CANCEL |
                WizardPage.HELP);

        page.setName(LangTool.getString("spool.titlePage1"));

        stage.setTitle(page.getName());

        // get the next button so we can set it enabled or disabled depending
        // on output type.
        nextButton = page.getNextButton();

        page.getContentPane().setCenter(pageOne());

        wizard.add(page);

        pagePDF = new WizardPage(WizardPage.PREVIOUS |
                WizardPage.FINISH |
                WizardPage.CANCEL |
                WizardPage.HELP);
        pagePDF.setName(LangTool.getString("spool.titlePage2PDF"));

        pagePDF.getContentPane().setCenter(pageTwoPDF());
        wizard.add(pagePDF);

        pageText = new WizardPage(WizardPage.PREVIOUS |
                WizardPage.FINISH |
                WizardPage.CANCEL |
                WizardPage.HELP);
        pageText.setName(LangTool.getString("spool.titlePage2Txt"));

        pageText.getContentPane().setCenter(pageTwoText());
        wizard.add(pageText);

        wizard.show(page.getName());
    }

    /**
     * Create the second page of the wizard pages for PDF
     *
     * @return
     */
    private BorderPane pageTwoPDF() {

        twoPDF = new BorderPane();

        final TitledBorderedPane docPropsBorder = new TitledBorderedPane();
        docPropsBorder.setTitle(LangTool.getString("spool.labelProps"));

        final GridPane docProps = createGridPane();
        docPropsBorder.setContent(docProps);

        title = new TextField();
        addPdfProperty(docProps, "spool.labelPropsTitle", title, 0);

        subject = new TextField();
        addPdfProperty(docProps, "spool.labelPropsSubject", subject, 1);

        author = new TextField();
        addPdfProperty(docProps, "spool.labelPropsAuthor", author, 2);

        final TitledBorderedPane optionsBorder = new TitledBorderedPane();
        optionsBorder.setTitle(LangTool.getString("spool.labelOpts"));

        final GridPane options = createGridPane();
        optionsBorder.setContent(options);

        //font size
        options.getChildren().add(setDefaultConstraints(
                new Label(LangTool.getString("spool.labelOptsFontSize")), 0, 0));

        fontSize = new TextField();
        fontSize.setPrefColumnCount(5);
        setDefaultConstraints(fontSize, 0, 1);
        options.getChildren().add(fontSize);

        //page size
        options.getChildren().add(setDefaultConstraints(
                new Label(LangTool.getString("spool.labelOptsPageSize")), 1, 0));

        pageSize = new ComboBox<String>();
        setDefaultConstraints(pageSize, 1, 1);
        options.getChildren().add(pageSize);

        pageSize.getItems().add("A3");
        pageSize.getItems().add("A4");
        pageSize.getItems().add("A5");
        pageSize.getItems().add("LETTER");
        pageSize.getItems().add("LEGAL");
        pageSize.getItems().add("LEDGER");
        pageSize.getSelectionModel().select(1);

        // prortrait / landscape
        portrait = new RadioButton(LangTool.getString("spool.labelOptsPortrait"));
        setDefaultConstraints(portrait, 2, 0);
        options.getChildren().add(portrait);

        landscape = new RadioButton(LangTool.getString("spool.labelOptsLandscape"));
        setDefaultConstraints(landscape, 2, 1);
        options.getChildren().add(landscape);

        final ToggleGroup orientation = new ToggleGroup();
        orientation.getToggles().add(portrait);
        orientation.getToggles().add(landscape);

        landscape.setSelected(true);

        twoPDF.setTop(docPropsBorder);
        twoPDF.setCenter(optionsBorder);

        return twoPDF;
    }

    private static GridPane createGridPane() {
        final GridPane gridPane = new GridPane();
        gridPane.setHgap(5.);
        gridPane.setVgap(5.);
        return gridPane;
    }

    private static void addPdfProperty(final GridPane container, final String labelKey,
            final TextField textField, final int row) {

        final Label label = new Label();
        label.setText(LangTool.getString(labelKey));
        setDefaultConstraints(label, row, 0);
        container.getChildren().add(label);

        textField.setPrefColumnCount(40);
        setDefaultConstraints(textField, row, 1);
        GridPane.setHgrow(textField, Priority.ALWAYS);

        container.getChildren().add(textField);
    }

    private static void addSpoolDataRow(final GridPane container, final String leftLabelKey,
            final String rightLabelText, final int row) {
        final Label leftLabel = new Label();
        leftLabel.setText(LangTool.getString(leftLabelKey));
        setDefaultConstraints(leftLabel, row, 0);

        container.getChildren().add(leftLabel);

        final Label rightLabel = new Label();
        rightLabel.setText(rightLabelText);
        setDefaultConstraints(rightLabel, row, 1);

        container.getChildren().add(rightLabel);
    }

    private static Node setDefaultConstraints(final Node component, final int row, final int column) {
        GridPane.setRowIndex(component, row);
        GridPane.setColumnIndex(component, column);
        GridPane.setHalignment(component, HPos.LEFT);
        return component;
    }

    /**
     * Create the second page of the wizard pages for Text
     *
     * @return
     */
    private BorderPane pageTwoText() {

        twoText = new BorderPane();

        final TitledBorderedPane textPropsBorder = new TitledBorderedPane();
        textPropsBorder.setTitle(LangTool.getString("spool.labelTextProps"));

        final GridPane textProps = createGridPane();
        textPropsBorder.setContent(textProps);

        openAfter = new CheckBox(LangTool.getString("spool.labelUseExternal"));
        setDefaultConstraints(openAfter, 0, 0);
        GridPane.setColumnSpan(openAfter, 2);

        textProps.getChildren().add(openAfter);

        editor = new TextField();
        editor.setPrefColumnCount(30);
        setDefaultConstraints(editor, 1, 0);
        GridPane.setHgrow(editor, Priority.ALWAYS);

        textProps.getChildren().add(editor);

        getEditor = new Button("Browse");
        setDefaultConstraints(getEditor, 1, 1);
        getEditor.setOnAction(e -> getEditor());

        textProps.getChildren().add(getEditor);

        // see if we have an external viewer defined and if we use it or not
        if (session.getSession().getConfiguration().isPropertyExists("useExternal"))
            openAfter.setDisable(false);

        if (session.getSession().getConfiguration().isPropertyExists("externalViewer"))
            editor.setText(session.getSession().getConfiguration().getStringProperty("externalViewer"));

        twoText.setCenter(textPropsBorder);

        return twoText;
    }

    private BorderPane pageOne() throws Exception {

        contentPane = new BorderPane();

        statusBar.setText("   ");
        statusBar.setMaxWidth(Double.POSITIVE_INFINITY);
        statusBar.getStyleClass().add("etched-border");

        contentPane.setCenter(spoolPanel);
        contentPane.setBottom(statusBar);

        // create the labels to be used for the spooled file data
        final TitledBorderedPane spoolDataTitle = new TitledBorderedPane();
        spoolDataTitle.setTitle(LangTool.getString("spool.labelSpoolInfo"));
        spoolDataTitle.setContent(spoolData);
        spoolData.setHgap(5);
        spoolData.setVgap(5);

        // create the data fields to be used for the spooled file data
        addSpoolDataRow(spoolData, "spool.labelSystem", splfile.getSystem().getSystemName(), 0);
        addSpoolDataRow(spoolData, "spool.labelSpooledFile", splfile.getName(), 1);
        addSpoolDataRow(spoolData, "spool.labelJobName", splfile.getJobName(), 2);
        addSpoolDataRow(spoolData, "spool.labelJobUser", splfile.getJobUser(), 3);
        addSpoolDataRow(spoolData, "spool.labelJobNumber", splfile.getJobNumber(), 4);
        addSpoolDataRow(spoolData, "spool.labelSpoolNumber", Integer.toString(splfile.getNumber()), 5);
        addSpoolDataRow(spoolData, "spool.labelPages", splfile.getIntegerAttribute(SpooledFile.ATTR_PAGES).toString(), 6);

        spoolPanel.setBottom(spoolOptions);

        // set the spool export panel
        spoolPanel.setCenter(spoolDataTitle);

        final TitledBorderedPane spoolInfoBorder = new TitledBorderedPane();
        spoolInfoBorder.setTitle(LangTool.getString("spool.labelExportInfo"));

        final GridPane spoolInfo = createGridPane();
        spoolInfoBorder.setContent(spoolInfo);

        spoolInfo.getChildren().add(setDefaultConstraints(new Label(LangTool.getString("spool.labelFormat")), 0, 0));

        cvtType = new ComboBox<String>();
        cvtType.getItems().add(LangTool.getString("spool.toPDF"));
        cvtType.getItems().add(LangTool.getString("spool.toText"));
        cvtType.getSelectionModel().selectFirst();

        setDefaultConstraints(cvtType, 0, 1);
        GridPane.setHgrow(cvtType, Priority.ALWAYS);
        spoolInfo.getChildren().add(cvtType);

        pc = new RadioButton(LangTool.getString("spool.labelPCPath"));
        spoolInfo.getChildren().add(setDefaultConstraints(pc, 1, 0));

        pcPathInfo = new TextField();
        pcPathInfo.setPrefColumnCount(30);
        GridPane.setHgrow(pcPathInfo, Priority.ALWAYS);
        spoolInfo.getChildren().add(setDefaultConstraints(pcPathInfo, 1, 1));

        pcSave = new Button("...");
        pcSave.setOnAction(e -> getPCFile());
        spoolInfo.getChildren().add(setDefaultConstraints(pcSave, 1, 2));

        ifs = new RadioButton(LangTool.getString("spool.labelIFSPath"));
        spoolInfo.getChildren().add(setDefaultConstraints(ifs, 2, 0));

        ifsPathInfo = new TextField();
        ifsPathInfo.setPrefColumnCount(30);
        setDefaultConstraints(ifsPathInfo, 2, 1);
        GridPane.setHgrow(ifsPathInfo, Priority.ALWAYS);
        spoolInfo.getChildren().add(ifsPathInfo);

        ifsSave = new Button("...");
        ifsSave.setOnAction(e -> getIFSFile());
        spoolInfo.getChildren().add(setDefaultConstraints(ifsSave, 2, 2));

        email = new RadioButton(LangTool.getString("spool.labelEmail"));
        spoolInfo.getChildren().add(setDefaultConstraints(email, 3, 0));

        final ToggleGroup bg = new ToggleGroup();
        bg.getToggles().add(pc);
        bg.getToggles().add(ifs);
        bg.getToggles().add(email);

        pc.selectedProperty().addListener((src, old, value) -> doItemStateChanged(value));
        ifs.selectedProperty().addListener((src, old, value) -> doItemStateChanged(value));
        email.selectedProperty().addListener((src, old, value) -> doItemStateChanged(value));

        pc.setSelected(true);

        spoolOptions.setCenter(spoolInfoBorder);

        return contentPane;
    }

    /**
     * React on the state change for radio buttons
     *
     * @param value selection state
     */
    private void doItemStateChanged(final Boolean value) {

        pcPathInfo.setDisable(true);
        ifsPathInfo.setDisable(true);
        pcSave.setDisable(true);
        ifsSave.setDisable(true);

        if (Boolean.TRUE.equals(value)) {
            if (pc.isSelected()) {
                pcPathInfo.setDisable(false);
                pcSave.setDisable(false);
                pcPathInfo.requestFocus();
            }

            if (ifs.isSelected()) {
                ifsPathInfo.setDisable(false);
                ifsSave.setDisable(false);
                ifsPathInfo.requestFocus();
            }
        }
    }

    private boolean pagesValid() {

        if (pc.isSelected()) {
            if (pcPathInfo.getText().length() == 0)
                getPCFile();
            if (pcPathInfo.getText().length() == 0)
                return false;
        }

        return true;
    }

    /**
     * Get the local file from a file chooser
     */
    private void getPCFile() {

        final FileChooser pcFileChooser = new FileChooser();
        pcFileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        // set the file filters for the file chooser
        TN5250jFileFilterBuilder filter;

        if (cvtType.getValue().equals(LangTool.getString("spool.toPDF")))
            filter = new TN5250jFileFilterBuilder("pdf", "PDF Files");
        else
            filter = new TN5250jFileFilterBuilder("txt", "Text Files");

        pcFileChooser.setSelectedExtensionFilter(filter.buildFilter());

        final File file = pcFileChooser.showSaveDialog(session.getWindow());

        // check to see if something was actually chosen
        if (file != null) {
            pcPathInfo.setText(filter.setExtension(file));
        }
    }

    /**
     * Get the IFS file from a file chooser
     */
    private void getIFSFile() {

        final IFSFileDialog fd = new IFSFileDialog(SwingToFxUtils.SHARED_FRAME, "Save As", splfile.getSystem());
        final com.ibm.as400.vaccess.FileFilter[] filterList =
                new com.ibm.as400.vaccess.FileFilter[2];
        filterList[0] = new com.ibm.as400.vaccess.FileFilter("All files (*.*)",
                "*.*");

        // Set up the filter based on the type of export specifed
        if (cvtType.getSelectionModel().getSelectedIndex() == 0) {
            filterList[1] = new com.ibm.as400.vaccess.FileFilter("PDF files (*.pdf)",
                    "*.pdf");
        } else {
            filterList[1] = new com.ibm.as400.vaccess.FileFilter("Text files (*.txt)",
                    "*.txt");

        }
        fd.setFileFilter(filterList, 1);

        // show the dialog and obtain the file if selected
        if (fd.showDialog() == IFSFileDialog.OK) {
            ifsPathInfo.setText(fd.getAbsolutePath());
        }
    }

    /**
     * Get the local file from a file chooser
     */
    private void getEditor() {

        final FileChooser pcFileChooser = new FileChooser();
        pcFileChooser.setInitialDirectory(new File(System.getProperty("user.dir")));

        final File file = pcFileChooser.showOpenDialog(session.getWindow());

        // check to see if something was actually chosen
        if (file != null) {
            try {
                editor.setText(file.getCanonicalPath());
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Export the spool file
     */
    private void doExport() {

        if (!pagesValid())
            return;

        workingThread = null;

        if (cvtType.getSelectionModel().getSelectedIndex() == 0)
            workingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    cvtToPDF();
                }
            });
        else
            workingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    cvtToText();
                }
            });

        workingThread.start();
    }

    /**
     * E-mail the information after export
     */
    private void emailMe() {
        new SendEMailDialog(session, conicalPath);
    }

    /**
     * Convert spoolfile to text file
     */
    private void cvtToText() {

        java.io.PrintStream dw;

        try {

            openOutputFile();

            if (ifs.isSelected())
                dw = new java.io.PrintStream(ifsfw);
            else
                dw = new java.io.PrintStream(fw);

            // Create an AS400 object.  The system name was passed
            // as the first command line argument.
            final AS400 system = new AS400(splfile.getSystem().getSystemName());

            final String splfName = splfile.getName();
            final int splfNumber = splfile.getNumber();
            final String _jobName = splfile.getJobName();
            final String _jobUser = splfile.getJobUser();
            final String _jobNumber = splfile.getJobNumber();

            final SpooledFile splF = new SpooledFile(system,
                    splfName,
                    splfNumber,
                    _jobName,
                    _jobUser,
                    _jobNumber);

            final PrintParameterList printParms = new PrintParameterList();
            printParms.setParameter(PrintObject.ATTR_WORKSTATION_CUST_OBJECT,
                    "/QSYS.LIB/QWPDEFAULT.WSCST");
            printParms.setParameter(PrintObject.ATTR_MFGTYPE, "*WSCST");

            // get the text (via a transformed input stream) from the spooled file
            final PrintObjectTransformedInputStream inStream = splF.getTransformedInputStream(printParms);
            //            DataInputStream dis = new DataInputStream(inStream);

            // get the number of available bytes
            int avail = inStream.available();
            byte[] buf = new byte[avail + 1];

            int read = 0;
            int totBytes = 0;
            final StringBuffer sb = new StringBuffer();

            updateStatus("Starting Output");

            // read the transformed spooled file, creating the jobLog String
            while (avail > 0) {
                if (avail > buf.length) {
                    buf = new byte[avail + 1];
                }

                read = inStream.read(buf, 0, avail);

                for (int x = 0; x < read; x++) {
                    switch (buf[x]) {
                        case 0x0:      // 0x00
                            break;
                        // write line feed to the stream
                        case 0x0A:
                            dw.println(sb.toString().toCharArray());
                            sb.setLength(0);
                            break;
                        // we will skip the carrage return
                        case 0x0D:
//                     sb.append('\n');
//                     writeChar("\n");
//                     System.out.println();
                            break;
                        // new page
                        case 0x0C:
//                     writeChar(sb.toString());
//                     dw.write(sb.toString().getBytes());
                            dw.println(sb.toString().toCharArray());
                            sb.setLength(0);

                            break;
                        default:
                            sb.append(byte2char(buf[x], "cp850"));
                    }
                }

                totBytes += read;

                updateStatus("Bytes read " + totBytes);
                //
                // process the data buffer
                //
                avail = inStream.available();
            }

            if (sb.length() > 0)
                dw.println(sb.toString().toCharArray());
            dw.flush();
            dw.close();

            updateStatus("Total bytes converted " + totBytes);

            // if we are to open it afterwards then execute the program with the
            //  text file as a parameter
            if (openAfter.isSelected()) {

                // not sure if this works on linux yet but here we go.
                try {
                    final Runtime rt = Runtime.getRuntime();
                    final String[] cmdArray = {editor.getText(), pcPathInfo.getText()};
                    // We need to probably do some checking here in the future
                    // Process proc = rt.exec(cmdArray);
                    rt.exec(cmdArray);

                    // now we set the field to use external viewer or not
                    if (openAfter.isSelected())
                        session.getSession().getConfiguration().setProperty("useExternal", "");
                    else
                        session.getSession().getConfiguration().removeProperty("useExternal");

                    // now we set the property for external viewer
                    session.getSession().getConfiguration().setProperty("externalViewer",
                            editor.getText());
                    // save it off
                    session.getSession().getConfiguration().saveSessionProps();
                } catch (final Throwable t) {
                    // print a stack trace
                    t.printStackTrace();
                    // throw up the message error
                    UiUtils.showError(t.getMessage(), "error");
                }

            }

            if (email.isSelected())
                emailMe();
        } catch (final Exception e) {
            updateStatus("Error: " + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }

    }

    /**
     * Convert spoolfile to PDF file
     */
    private void cvtToPDF() {

        try {

            openOutputFile();

            // Create the printparameters to be used in the transform of the
            //    input stream
            final PrintParameterList printParms = new PrintParameterList();
            printParms.setParameter(PrintObject.ATTR_WORKSTATION_CUST_OBJECT,
                    "/QSYS.LIB/QWPDEFAULT.WSCST");
            printParms.setParameter(PrintObject.ATTR_MFGTYPE, "*WSCST");

            // get the text (via a transformed input stream) from the spooled file
            final PrintObjectTransformedInputStream inStream = splfile.getTransformedInputStream(printParms);

            // get the number of available bytes
            int avail = inStream.available();
            byte[] buf = new byte[avail + 1];

            int read = 0;
            int totBytes = 0;

            final StringBuffer sb = new StringBuffer();

            updateStatus("Starting Output");

            // read the transformed spooled file, creating the jobLog String
            while (avail > 0) {
                if (avail > buf.length) {
                    buf = new byte[avail + 1];
                }

                read = inStream.read(buf, 0, avail);

                for (int x = 0; x < read; x++) {
                    switch (buf[x]) {
                        case 0x0:      // 0x00
                            break;
                        // write line feed to the stream
                        case 0x0A:
//                     writeChar(sb.toString());
                            sb.append((char) buf[x]);
//                     System.out.print(sb);
//                     sb.setLength(0);
                            break;
                        // we will skip the carrage return
                        case 0x0D:
//                     sb.append('\n');
//                     writeChar("\n");
//                     System.out.println();
                            break;
                        // new page
                        case 0x0C:
                            writeBuffer(sb.toString());
                            document.newPage();
                            sb.setLength(0);
                            break;
                        default:
                            sb.append(byte2char(buf[x], "cp850"));
                    }
                }

                totBytes += read;

                updateStatus("Bytes read " + totBytes);
                //
                // process the data buffer
                //
                avail = inStream.available();
            }
            closeOutputFile();
            updateStatus("Total bytes converted " + totBytes);

            if (email.isSelected())
                emailMe();

        } catch (final Exception e) {
            updateStatus("Error: " + e.getMessage());
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     *
     * @param s
     */
    private void writeBuffer(final String s) {

        if (!document.isOpen())
            document.open();

        try {
            document.add(new Paragraph(s, font));
        } catch (final com.lowagie.text.DocumentException de) {
            System.out.println(de);
        }
    }

    /**
     * Open the correct type of output file depending on selection(s)
     */
    public void openOutputFile() {

        try {

            // update status
            updateStatus("Opening File");

            // default to txt extention
            String suffix = ".txt";
            String fileName = "";

            // if pdf then change to pdf extenstion
            if (cvtType.getSelectionModel().getSelectedIndex() == 0)
                suffix = ".pdf";

            // for e-mailing setup a temporary file
            if (email.isSelected()) {
                final File dir = new File(System.getProperty("user.dir"));

                //  setup the temp file name
                final String tempFile = splfile.getName().trim() + '_' +
                        splfile.getJobName().trim() + '_' +
                        splfile.getJobUser().trim() + '_' +
                        splfile.getNumber() + '_' +
                        splfile.getJobNumber().trim();

                // create the temporary file
                final File f = File.createTempFile(tempFile, suffix, dir);

                System.out.println(f.getName());
                System.out.println(f.getCanonicalPath());

                conicalPath = f.getCanonicalPath();

                // set it to delete on exit
                f.deleteOnExit();

                // create the file
                fw = new FileOutputStream(f);
            } else if (ifs.isSelected()) {
                fileName = ifsPathInfo.getText().trim();
                ifsfw = new IFSFileOutputStream(splfile.getSystem(), fileName);
            } else {
                fileName = pcPathInfo.getText().trim();
                fw = new FileOutputStream(fileName);
            }

            // if not PDF then this is all we have to do so return
            if (cvtType.getSelectionModel().getSelectedIndex() > 0)
                return;

            // On pdf's then we need to create a PDF document
            if (document == null) {

                document = new Document();

                // create the pdf writer based on selection of pc or ifs file
                if (ifs.isSelected()) {
                    bos = PdfWriter.getInstance(document, ifsfw);
                } else {
                    bos = PdfWriter.getInstance(document, fw);
                }

                // create the base font
                final BaseFont bf = BaseFont.createFont("Courier", "Cp1252", false);

                // set the default size of the font to 9.0
                float fontsize = 9.0f;

                // if we have a font selectd then try to use it
                if (fontSize.getText().length() > 0)
                    fontsize = Float.parseFloat(fontSize.getText().trim());

                // create the pdf font to use within the document
                font = new com.lowagie.text.Font(bf, fontsize,
                        com.lowagie.text.Font.NORMAL);

                // set the PDF properties of the supplied properties
                if (author.getText().length() > 0)
                    document.addAuthor(author.getText());
                if (title.getText().length() > 0)
                    document.addTitle(title.getText());
                if (subject.getText().length() > 0)
                    document.addSubject(subject.getText());

                // set the page sizes and the page orientation
                final String ps = pageSize.getValue();

                if (ps.equals("A3")) {
                    if (portrait.isSelected())
                        document.setPageSize(PageSize.A3);
                    else
                        document.setPageSize(PageSize.A3.rotate());

                }

                if (ps.equals("A4")) {
                    if (portrait.isSelected())
                        document.setPageSize(PageSize.A4);
                    else
                        document.setPageSize(PageSize.A4.rotate());
                }

                if (ps.equals("A5")) {
                    if (portrait.isSelected())
                        document.setPageSize(PageSize.A5);
                    else
                        document.setPageSize(PageSize.A5.rotate());
                }
                if (ps.equals("LETTER")) {
                    if (portrait.isSelected())
                        document.setPageSize(PageSize.LETTER);
                    else
                        document.setPageSize(PageSize.LETTER.rotate());
                }
                if (ps.equals("LEGAL")) {
                    if (portrait.isSelected())
                        document.setPageSize(PageSize.LEGAL);
                    else
                        document.setPageSize(PageSize.LEGAL.rotate());
                }
                if (ps.equals("LEDGER")) {
                    if (portrait.isSelected())
                        document.setPageSize(PageSize.LEDGER);
                    else
                        document.setPageSize(PageSize.LEDGER.rotate());
                }
            }
        } catch (final IOException _ex) {
            System.out.println("Cannot open 1 " + _ex.getMessage());

        } catch (final Exception _ex2) {
            System.out.println("Cannot open 2 " + _ex2.getMessage());
        }

    }

    private void closeOutputFile() {

        document.close();
        document = null;

    }

    private void updateStatus(final String stat) {
        Platform.runLater(() -> statusBar.setText(stat));
    }

    @Override
    public void nextBegin(final WizardEvent e) {
        removeStatusBarAlways();

//      System.out.println(e.getCurrentPage().getName() + " Next Begin");
        if (cvtType.getValue().equals(LangTool.getString("spool.toText"))) {
            twoText.setBottom(statusBar);
            e.setNewPage(pageText);
        } else {
            twoPDF.setBottom(statusBar);
            e.setNewPage(pagePDF);
        }
    }

    @Override
    public void nextComplete(final WizardEvent e) {
//      System.out.println(e.getCurrentPage().getName() + " Next Complete");
        stage.setTitle(e.getNewPage().getName());
    }

    @Override
    public void previousBegin(final WizardEvent e) {
//      System.out.println(e.getCurrentPage().getName() + " Prev Begin");
        e.setNewPage(page);

        removeStatusBarAlways();
        contentPane.setBottom(statusBar);
    }

    private void removeStatusBarAlways() {
        twoText.getChildren().remove(statusBar);
        twoPDF.getChildren().remove(statusBar);
        contentPane.getChildren().remove(statusBar);
    }

    @Override
    public void previousComplete(final WizardEvent e) {
//      System.out.println(e.getCurrentPage().getName() + " Prev Complete");
        stage.setTitle(e.getNewPage().getName());
    }

    @Override
    public void finished(final WizardEvent e) {
        doExport();
    }

    @Override
    public void canceled(final WizardEvent e) {
//      System.out.println("It is canceled!");
        if (workingThread != null) {
            workingThread.interrupt();
            workingThread = null;
        }
        this.setVisible(false);
        this.dispose();
    }

    @Override
    public void help(final WizardEvent e) {
        System.out.println(e.getCurrentPage().getName());
    }

    /**
     * Converts a byte to a char
     *
     * @param b the byte to be converted
     * @param charsetName the name of a charset in the which the byte is encoded
     * @return the converted char
     */
    public static char byte2char(final byte b, final String charsetName) {
        char c = ' ';
        try {
            final byte[] bytes = {b};
            c = (new String(bytes, charsetName)).charAt(0);
        } catch (final java.io.UnsupportedEncodingException uee) {
            System.err.println(uee);
            System.err.println("Error while converting byte to char, returning blank...");
        }
        return c;
    }


}
