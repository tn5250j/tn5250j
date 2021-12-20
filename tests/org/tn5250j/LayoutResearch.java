/**
 *
 */
package org.tn5250j;

import org.tn5250j.tools.LangTool;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class LayoutResearch extends Application {

    public static void main(final String[] args) throws Exception {
        LangTool.init();
        launch(args);
    }

    @Override
    public void start(final Stage stage) throws Exception {
        final VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setStyle("-fx-background-color: blue;");

        final HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);
        hbox.setStyle("-fx-background-color: green;");
        vbox.getChildren().add(hbox);

        final BorderPane container = new BorderPane();
        container.setMaxSize(Integer.MAX_VALUE, Integer.MAX_VALUE);
        container.setStyle("-fx-background-color: red;");
        hbox.getChildren().add(container);

        final Canvas canvas = new Canvas(100, 100);
        container.setCenter(canvas);

        //font manipulations
        stage.setScene(new Scene(vbox));
        stage.show();
    }
}
