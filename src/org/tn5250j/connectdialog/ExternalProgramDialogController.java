/**
 *
 */
package org.tn5250j.connectdialog;

import java.net.URL;
import java.util.ResourceBundle;

import org.tn5250j.ExternalProgramConfig;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.LangTool;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ExternalProgramDialogController implements Initializable {
    @FXML
    Pane view;

    @FXML
    Label nameLabel;
    @FXML
    Label wCommandLabel;
    @FXML
    Label uCommandLabel;

    @FXML
    TextField name;
    @FXML
    TextField wCommand;
    @FXML
    TextField uCommand;

    @FXML
    Button ok;
    @FXML
    Button cancel;

    private boolean isNew = true;
    private final ExternalProgramConfig config = ExternalProgramConfig.getInstance();

    @Override
    public void initialize(final URL location, final ResourceBundle resources) {
        nameLabel.setText(LangTool.getString("customized.name"));
        wCommandLabel.setText(LangTool.getString("customized.window"));
        uCommandLabel.setText(LangTool.getString("customized.unix"));
        cancel.setText(LangTool.getString("conf.optCancel"));

        cancel.setOnAction(e -> cancel());
        cancel.setDisable(false);
        ok.setOnAction(e -> ok());
        ok.setDisable(true);

        name.textProperty().addListener((src, old, value) -> textChanged());
    }

    public void setProgram(final ExternalProgram program) {
        this.isNew = program == null;
        if (!isNew) {
            name.setText(program.getName());
            wCommand.setText(program.getWCommand());
            uCommand.setText(program.getUCommand());
            ok.setText(LangTool.getString("conf.optEdit"));
        } else {
            name.setText("");
            wCommand.setText("");
            uCommand.setText("");
            ok.setText(LangTool.getString("conf.optAdd"));
        }

        name.setEditable(isNew);
    }

    private void textChanged() {
        final String text = name.getText();
        ok.setDisable(!isNew && (text == null || text.trim().isEmpty()));
    }

    private void cancel() {
        UiUtils.closeMe(view.getScene());
    }

    private void ok() {
        final ExternalProgram program = new ExternalProgram(name.getText(), wCommand.getText(), uCommand.getText());
        config.programUpdated(program);
        cancel();
    }
}
