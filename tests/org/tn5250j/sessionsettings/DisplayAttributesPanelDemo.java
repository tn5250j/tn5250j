/**
 *
 */
package org.tn5250j.sessionsettings;

import java.io.IOException;

import org.tn5250j.DevTools;
import org.tn5250j.gui.UiUtils;
import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DisplayAttributesPanelDemo  extends Application {
    public static void main(final String[] args) {
        LangTool.init();
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        stage.setTitle("Display Attributres");

        final FXMLLoader loader = UiUtils.createLoader("/fxml/DisplayAttributesPane.fxml");
        final DisplayAttributesController controller = new DisplayAttributesController(DevTools.createSessionConfig());
        loader.setControllerFactory(cls -> {
            return controller;
        });

        try {
            loader.load();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load template", e);
        }

        stage.setScene(new Scene(controller.getView()));
        stage.show();
    }
}
