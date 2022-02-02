/**
 *
 */
package org.tn5250j.connectdialog;

import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

import org.tn5250j.gui.TitledBorderedPane;
import org.tn5250j.interfaces.ConfigureFactory;
import org.tn5250j.tools.LangTool;
import org.tn5250j.tools.logging.TN5250jLogFactory;
import org.tn5250j.tools.logging.TN5250jLogger;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LoggingPaneController implements Initializable {
    @FXML
    Pane view;

    @FXML
    TitledBorderedPane logLevelPanel;
    @FXML
    TitledBorderedPane logAppenderPanel;

    //log levels
    @FXML
    RadioButton intOFF;
    @FXML
    RadioButton intDEBUG;
    @FXML
    RadioButton intINFO;
    @FXML
    RadioButton intWARN;
    @FXML
    RadioButton intERROR;
    @FXML
    RadioButton intFATAL;

    //appender
    @FXML
    RadioButton intConsole;
    @FXML
    RadioButton intFile;
    @FXML
    RadioButton intBoth;

    private final SimpleIntegerProperty logLevelProperty = new SimpleIntegerProperty();
    private final SimpleObjectProperty<LogAppender> logAppenderProperty = new SimpleObjectProperty<>();

    public LoggingPaneController() {
        final Properties properties = ConfigureFactory.getInstance().getProperties(
                ConfigureFactory.SESSIONS);

        final int logLevel = Integer.parseInt(properties.getProperty("emul.logLevel",
                Integer.toString(TN5250jLogger.INFO)));
        this.logLevelProperty.setValue(logLevel);
        this.logAppenderProperty.setValue(LogAppender.Console);
    }

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        view.setUserData(this);

        // levelPanel
        logLevelPanel.setTitle(LangTool.getString("logscr.Level"));
        logLevelPanel.setLabelPosition(Pos.TOP_CENTER);

        final ToggleGroup levelGroup = new ToggleGroup();
        initLogLevelButton(intOFF, levelGroup, LangTool.getString("logscr.Off"),
                true, TN5250jLogger.OFF);
        initLogLevelButton(intDEBUG, levelGroup, LangTool.getString("logscr.Debug"),
                false, TN5250jLogger.DEBUG);
        initLogLevelButton(intINFO, levelGroup, LangTool.getString("logscr.Info"),
                false, TN5250jLogger.INFO);
        initLogLevelButton(intWARN, levelGroup, LangTool.getString("logscr.Warn"),
                false, TN5250jLogger.WARN);
        initLogLevelButton(intERROR, levelGroup, LangTool.getString("logscr.Error"),
                false, TN5250jLogger.ERROR);
        initLogLevelButton(intFATAL, levelGroup, LangTool.getString("logscr.Fatal"),
                false, TN5250jLogger.FATAL);

        showLogLevel();

        logAppenderPanel.setTitle(LangTool.getString("logscr.Appender"));

        final ToggleGroup appenderGroup = new ToggleGroup();
        initAppenderButton(intConsole, appenderGroup, LangTool.getString("logscr.Console"),
                true, LogAppender.Console);
        initAppenderButton(intFile, appenderGroup, LangTool.getString("logscr.File"),
                false, LogAppender.File);
        initAppenderButton(intBoth, appenderGroup, LangTool.getString("logscr.Both"),
                false, LogAppender.Both);

        logLevelProperty.addListener((src, old, value) -> logLevelChanged(src, old, value));

        //set initial state
        logLevelChanged(logLevelProperty, null, logLevelProperty.getValue());
    }

    private void showLogLevel() {
        switch (logLevelProperty.getValue().intValue()) {
          case TN5250jLogger.OFF:
            intOFF.setSelected(true);
            break;
          case TN5250jLogger.DEBUG:
            intDEBUG.setSelected(true);
            break;
          case TN5250jLogger.INFO:
            intINFO.setSelected(true);
            break;
          case TN5250jLogger.WARN:
            intWARN.setSelected(true);
            break;
          case TN5250jLogger.ERROR:
            intERROR.setSelected(true);
            break;
          case TN5250jLogger.FATAL:
            intFATAL.setSelected(true);
            break;
          default:
            intINFO.setSelected(true);
        }
    }

    private void logLevelChanged(final ObservableValue<? extends Number> observable,
            final Number oldValue, final Number newValue) {
        final boolean oldIsOff = oldValue != null && oldValue.intValue() == TN5250jLogger.OFF;
        final boolean newIsOff = newValue != null && newValue.intValue() == TN5250jLogger.OFF;

        if (oldIsOff && TN5250jLogFactory.isLog4j()) {
            intConsole.setDisable(false);
            intFile.setDisable(false);
            intBoth.setDisable(false);
        } else if (newIsOff) {
            intConsole.setDisable(true);
            intConsole.setSelected(true);
            intFile.setDisable(true);
            intBoth.setDisable(true);
        }
    }

    private void initLogLevelButton(final RadioButton btn, final ToggleGroup group, final String title,
            final boolean selected, final int selectedLogLevel) {
        btn.setText(title);
        btn.setToggleGroup(group);
        btn.setSelected(selected);
        btn.selectedProperty().addListener((observable, oldValue, newValue) -> {
            //if button selected, should change log level property
            if (newValue) {
                logLevelProperty.setValue(selectedLogLevel);
                TN5250jLogFactory.setLogLevels(selectedLogLevel);
            }
        });
    }

    private void initAppenderButton(final RadioButton btn, final ToggleGroup group, final String title,
            final boolean selected, final LogAppender appender) {
        btn.setText(title);
        btn.setToggleGroup(group);
        btn.setSelected(selected);
        btn.selectedProperty().addListener((observable, oldValue, newValue) -> {
            //if button selected, should change log appender property
            if (newValue) {
                logAppenderProperty.setValue(appender);
            }
        });
    }

    public SimpleIntegerProperty logLevelProperty() {
        return logLevelProperty;
    }

    void saveState() {
        final Integer level = logLevelProperty.getValue();
        if (level != null) {
            final Properties properties = ConfigureFactory.getInstance().getProperties(
                    ConfigureFactory.SESSIONS);
            properties.setProperty("emul.logLevel", level.toString());
        }
    }
}
