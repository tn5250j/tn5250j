/**
 *
 */
package org.tn5250j.rubberband;

import org.tn5250j.RubberBandFX;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class FxRubberBandAndCursorPane extends Application {
    public static void main(final String[] args) {
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        final Pane stackPane = new Pane();

        final ImageView imageView = new ImageView("file:///home/soldatov/Изображения/6.jpg");
        stackPane.getChildren().add(imageView);

        final RubberBandFX rb = new RubberBandFX();
        stackPane.getChildren().add(rb.getComponent());
        rb.startListen(imageView);

        final BorderPane content = new BorderPane();
        content.setCenter(stackPane);

        stage.setScene(new Scene(content));
        stage.show();
    }
}
