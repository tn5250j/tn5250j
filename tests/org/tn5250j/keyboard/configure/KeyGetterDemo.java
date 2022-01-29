/**
 *
 */
package org.tn5250j.keyboard.configure;

import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class KeyGetterDemo extends Application {
    public static void main(final String[] args) {
        LangTool.init();
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
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
            System.out.println("Key charactoer: " + kg.keyevent.getCharacter());
        });
        dialog.show();
    }
}
