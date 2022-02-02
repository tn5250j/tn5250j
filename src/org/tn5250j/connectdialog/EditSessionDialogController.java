/**
 *
 */
package org.tn5250j.connectdialog;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.tn5250j.TN5250jConstants;
import org.tn5250j.encoding.CharMappings;
import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class EditSessionDialogController implements Initializable {
    @FXML
    Pane view;

    //tabs
    @FXML
    Tab tabGeneral;
    @FXML
    Tab tabTCP;
    @FXML
    Tab tabOptions;
    @FXML
    Tab tabProxy;

    //system name pane
    @FXML
    Label systemNameLabel;

    @FXML
    TextField systemName;
    @FXML
    CheckBox noEmbed;
    @FXML
    CheckBox deamon;
    @FXML
    CheckBox newJVM;

    //TCP IP
    @FXML
    TitledBorderedPane sip;
    @FXML
    Label systemIdLabel;
    @FXML
    TextField systemId;
    @FXML
    Label portLabel;
    @FXML
    TextField port; //length 5
    @FXML
    Label deviceNameLabel;
    @FXML
    TextField deviceName;
    @FXML
    CheckBox sdn;
    @FXML
    Label sslTypeLabel;
    @FXML
    ComboBox<String> sslType;
    @FXML
    CheckBox heartBeat;

    //options panel
    @FXML
    TitledBorderedPane op;
    @FXML
    TitledBorderedPane fp;
    @FXML
    TextField fpn; // 20
    @FXML
    TitledBorderedPane sdp;
    @FXML
    RadioButton sdNormal;
    @FXML
    RadioButton sdBig;

    // code page panel
    @FXML
    TitledBorderedPane cp;
    @FXML
    ComboBox<String> cpb;
    @FXML
    CheckBox jtb;

    // emulation mode panel
    @FXML
    TitledBorderedPane ep;
    @FXML
    CheckBox ec;

    @FXML
    TitledBorderedPane tp;
    @FXML
    CheckBox tc;

    //proxy
    @FXML
    Label proxyHostLabel;
    @FXML
    TextField proxyHost; //length 20
    @FXML
    CheckBox useProxy;
    @FXML
    Label proxyPortLabel;
    @FXML
    TextField proxyPort;

    //buttons
    @FXML
    Button ok;
    @FXML
    Button cancel;

    private String result;

    private final Properties properties;

    public EditSessionDialogController() {
        properties = ConfigureFactory.getInstance().getProperties(
                ConfigureFactory.SESSIONS);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        initTab(tabGeneral, "conf.tabGeneral");
        initTab(tabTCP, "conf.tabTCP");
        initTab(tabOptions, "conf.tabOptions");
        initTab(tabProxy, "conf.tabProxy");

        //System Name panel
        systemNameLabel.setText(LangTool.getString("conf.labelSystemName"));
        systemName.setEditable(false);

        noEmbed.setText(LangTool.getString("conf.labelEmbed"));
        deamon.setText(LangTool.getString("conf.labelDeamon"));
        newJVM.setText(LangTool.getString("conf.labelNewJVM"));
        systemName.textProperty().addListener((src, old, value) -> systemNameTextChanged());

        //System ID panel
        sip.setTitle(LangTool.getString("conf.labelSystemIdTitle"));

        heartBeat.setText(LangTool.getString("conf.labelHeartBeat"));
        systemIdLabel.setText(LangTool.getString("conf.labelSystemId"));
        portLabel.setText(LangTool.getString("conf.labelPort"));
        deviceNameLabel.setText(LangTool.getString("conf.labelDeviceName"));
        sdn.setText(LangTool.getString("conf.labelUseHostName"));
        sdn.selectedProperty().addListener(e -> sdnSelectionChanged());
        sslTypeLabel.setText(LangTool.getString("conf.labelSSLType"));

        for (final String type : TN5250jConstants.SSL_TYPES) {
            sslType.getItems().add(type);
        }
        sslType.getSelectionModel().select(TN5250jConstants.SSL_TYPE_NONE);

        // options panel
        op.setTitle(LangTool.getString("conf.labelOptionsTitle"));

        // file name panel
        fp.setTitle(LangTool.getString("conf.labelConfFile"));

        // screen dimensions panel
        sdp.setTitle(LangTool.getString("conf.labelDimensions"));

        // Group the radio buttons.
        final ToggleGroup sdGroup = new ToggleGroup();
        sdNormal.setToggleGroup(sdGroup);
        sdNormal.setText(LangTool.getString("conf.label24"));

        sdBig.setToggleGroup(sdGroup);
        sdBig.setText(LangTool.getString("conf.label27"));

        // code page panel
        cp.setTitle(LangTool.getString("conf.labelCodePage"));
        doCPStateChanged();

        jtb.setText("AS/400 Toolbox");
        jtb.selectedProperty().addListener(e -> doCPStateChanged());

        // emulation mode panel
        ep.setTitle(LangTool.getString("conf.labelEmulateMode"));
        ec.setText(LangTool.getString("conf.labelEnhanced"));

        // title to be use panel
        tc.setText(LangTool.getString("conf.labelUseSystemName"));

        //System Id panel
        useProxy.setText(LangTool.getString("conf.labelUseProxy"));
        proxyHost.setText(LangTool.getString("conf.labelProxyHost"));

        proxyPort.setText("1080");
        proxyPort.setPrefColumnCount(5);
        proxyPortLabel.setText(LangTool.getString("conf.labelProxyPort"));

        //buttons buttons
        addOptButton(cancel, "conf.optCancel", this::cancel, true);
        addOptButton(ok, null, this::ok, false);
    }

    private void sdnSelectionChanged() {
        deviceName.setDisable(!sdn.isSelected());
    }

    private void addOptButton(final Button button, final String labelKey, final Runnable listener, final boolean enabled) {
        UiUtils.addOptButton(button, labelKey, e -> listener.run()).setDisable(!enabled);
    }

    private void ok() {
        result = systemName.getText();
        if (result != null && result.trim().length() == 0) {
            result = null;
        }

        if (result == null) {
            properties.put(result, toArgString());
        } else {
            properties.setProperty(result, toArgString());
        }
        cancel();
    }

    private void cancel() {
        result = null;
        UiUtils.closeMe(view.getScene());
    }

    public void setSystemName(final String name) {
        result = name;
        final boolean isNew = name == null;

        systemName.setText(name == null ? "" : name);
        systemName.setEditable(isNew);
        ok.setDisable(!isNew);
        newJVM.setDisable(isNew);
        noEmbed.setDisable(isNew);
        deamon.setDisable(isNew);

        systemNameTextChanged();

        if (isNew) {
            UiUtils.setLabel(ok, "conf.addEntryATitle");
            port.setText("23");
            deviceName.setText("");
            fpn.setText("");
            proxyHost.setText("");
            proxyPort.setText("1080");

            ec.setSelected(true);
            tc.setSelected(true);
            jtb.setSelected(false);
            sdNormal.setSelected(true);
            deamon.setSelected(true);
        } else {
            ok.setText("conf.addEntryATitle");
            UiUtils.setLabel(ok, "conf.optEdit");

            final String[] args = parseArgs(properties.getProperty(name));

            if (isSpecified("-p", args)) {
                port.setText(getParm("-p", args));
            } else {
                port.setText("23");
            }

            if (isSpecified("-sslType", args)) {
                setSelectedItem(sslType, getParm("-sslType", args));
            }

            if (isSpecified("-sph", args)) {
                proxyHost.setText(getParm("-sph", args));
            } else {
                proxyHost.setText("");
            }

            if (isSpecified("-f", args)) {
                fpn.setText(getParm("-f", args));
            } else {
                fpn.setText("");
            }

            if (isSpecified("-cp", args)) {
                final String codepage = getParm("-cp", args);

                jtb.setSelected(true);
                for (final String acp : CharMappings.getAvailableCodePages()) {
                    if (acp.equals(codepage)) {
                        jtb.setSelected(false);
                    }
                }

                setSelectedItem(cpb, codepage);
            }

            ec.setSelected(isSpecified("-e", args));
            tc.setSelected(isSpecified("-t", args));

            if (isSpecified("-132", args) ) {
                sdBig.setSelected(true);
            } else {
                sdNormal.setSelected(true);
            }

            if (isSpecified("-dn", args)) {
                deviceName.setText(getParm("-dn", args));
            } else {
                deviceName.setText("");
            }

            if (isSpecified("-dn=hostname", args)) {
                sdn.setSelected(true);
                deviceName.setDisable(true);
            } else {
                sdn.setSelected(false);
                deviceName.setDisable(false);
            }

            if (isSpecified("-spp", args)) {
                proxyPort.setText(getParm("-spp", args));
            } else {
                proxyPort.setText("1080");
            }

            useProxy.setSelected(isSpecified("-usp", args));
            noEmbed.setSelected(isSpecified("-noembed", args));
            deamon.setSelected(isSpecified("-d", args));
            newJVM.setSelected(isSpecified("-nc", args));
            heartBeat.setSelected(isSpecified("-hb", args));
        }

        sdnSelectionChanged();
        doCPStateChanged();
    }

    /**
     * Select equals item.
     * @param <T> parameter.
     * @param comboBox combobox.
     * @param selection selection
     */
    private static <T> void setSelectedItem(final ComboBox<T> comboBox, final T selection) {
        for (final T i : comboBox.getItems()) {
            if (selection.equals(i)) {
                comboBox.getSelectionModel().select(i);
                break;
            }
        }
    }

    private String toArgString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(systemId.getText());

        // port
        if (port.getText() != null && port.getText().trim().length() > 0)
            sb.append(" -p " + port.getText().trim());

        if (fpn.getText() != null && fpn.getText().length() > 0)
            sb.append(" -f " + fpn.getText());
        if (!LangTool.getString("conf.labelDefault").equals(cpb.getSelectionModel().getSelectedItem()))
            sb.append(" -cp " + cpb.getSelectionModel().getSelectedItem());

        if (!TN5250jConstants.SSL_TYPE_NONE.equals(sslType.getSelectionModel().getSelectedItem()))
            sb.append(" -sslType " + sslType.getSelectionModel().getSelectedItem());

        if (ec.isSelected())
            sb.append(" -e");

        if (tc.isSelected())
            sb.append(" -t");

        if (!sdNormal.isSelected())
            sb.append(" -132");

        if (deviceName.getText() != null && !sdn.isSelected())
            if (deviceName.getText().trim().length() > 0)
                if (deviceName.getText().trim().length() > 10)
                    sb.append(" -dn " + deviceName.getText().trim().substring(0, 10).toUpperCase());
                else
                    sb.append(" -dn " + deviceName.getText().trim().toUpperCase());

        if (sdn.isSelected())
            sb.append(" -dn=hostname");

        if (useProxy.isSelected())
            sb.append(" -usp");

        if (proxyHost.getText() != null)
            if (proxyHost.getText().length() > 0)
                sb.append(" -sph " + proxyHost.getText());

        if (proxyPort.getText() != null)
            if (proxyPort.getText().length() > 0)
                sb.append(" -spp " + proxyPort.getText());

        if (noEmbed.isSelected())
            sb.append(" -noembed ");

        if (deamon.isSelected())
            sb.append(" -d ");

        if (newJVM.isSelected())
            sb.append(" -nc ");

        if (heartBeat.isSelected())
            sb.append(" -hb ");

        return sb.toString();
    }

    private void doCPStateChanged() {
        final String defaultOption = LangTool.getString("conf.labelDefault");

        cpb.getItems().clear();
        cpb.getItems().add(defaultOption);
        for (final String codePage : CharMappings.getAvailableCodePages()) {
            cpb.getItems().add(codePage);
        }

        cpb.getSelectionModel().select(defaultOption);
    }

    public static String[] parseArgs(final String theStringList) {
        final List<String> s = new LinkedList<>();

        final StringTokenizer tokenizer = new StringTokenizer(theStringList, " ");
        while (tokenizer.hasMoreTokens()) {
            s.add(tokenizer.nextToken());
        }

        return s.toArray(new String[s.size()]);
    }

    private void initTab(final Tab tab, final String titleKey) {
        tab.setText(LangTool.getString(titleKey));
        tab.setClosable(false);
    }

    private static String getParm(final String parm, final String[] args) {
        for (int x = 0; x < args.length; x++) {
            if (args[x].equals(parm)) {
                return args[x + 1];
            }
        }
        return null;
    }

    private static boolean isSpecified(final String parm, final String[] args) {
        for (int x = 0; x < args.length; x++) {
            if (parm.equals(args[x])) {
                return true;
            }
        }
        return false;
    }

    private void systemNameTextChanged() {
        final String text = systemName.getText();
        final boolean disable = result == null && (text == null || text.trim().isEmpty());

        tabTCP.setDisable(disable);
        tabOptions.setDisable(disable);
        tabProxy.setDisable(disable);
        ok.setDisable(disable);
        newJVM.setDisable(disable);
        noEmbed.setDisable(disable);
        deamon.setDisable(disable);
    }

    /**
     * @return session name
     */
    public String getSystemName() {
        return result;
    }
}
