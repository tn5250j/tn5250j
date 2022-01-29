/**
 *
 */
package org.tn5250j.sessionsettings;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class ColorPickerDemo extends Application {
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage primaryStage) throws Exception {
        final FlowPane pane = new FlowPane();
        pane.getChildren().add(new ColorPicker());

        final Scene scene = new Scene(pane);
        primaryStage.setScene(scene);

        primaryStage.show();
    }
}
