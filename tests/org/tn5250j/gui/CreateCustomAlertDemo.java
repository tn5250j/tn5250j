/**
 *
 */
package org.tn5250j.gui;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class CreateCustomAlertDemo extends Application {
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final TextField text = new TextField();
        final BorderPane borderPane = new BorderPane();

        final Label label = new Label("Введите вводную:");
        borderPane.setLeft(label);
        BorderPane.setAlignment(label, Pos.CENTER);
        BorderPane.setMargin(label, new Insets(0, 5, 0, 0));

        borderPane.setCenter(text);
        BorderPane.setAlignment(text, Pos.CENTER);

        final HBox content = new HBox(borderPane);

        final Alert alert = UiUtils.createInputDialog(content, "Поехали", "Передумал");
        alert.setTitle("Demo");
        System.out.println(alert.showAndWait().orElse(null));
    }
}
