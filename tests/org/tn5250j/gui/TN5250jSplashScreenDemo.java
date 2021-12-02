/**
 *
 */
package org.tn5250j.gui;

import java.util.concurrent.atomic.AtomicInteger;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TN5250jSplashScreenDemo {
    public static void main(final String[] args) {
        //init FX
        SwingToFxUtils.initFx();
        Platform.runLater(() -> launch());
    }

    private static void launch() {
        final TN5250jSplashScreen splashScreen = new TN5250jSplashScreen("tn5250jSplash.jpg");

        final int total = 10;
        splashScreen.setSteps(total);

        final AtomicInteger step = new AtomicInteger();
        final Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Splash screen Swing demo");

        final BorderPane content = new BorderPane();
        dialog.getDialogPane().setContent(content);

        final Insets insets = new Insets(10);
        content.setCenter(createCenterPanel(splashScreen, step, total));
        BorderPane.setMargin(content.getCenter(), insets);

        content.setBottom(createBottomPane(splashScreen));
        BorderPane.setMargin(content.getBottom(), insets);

        dialog.setResizable(false);

        splashScreen.setVisible(true);
        showDialog(dialog);
    }

    private static Pane createCenterPanel(final TN5250jSplashScreen splashScreen,
            final AtomicInteger step, final int total) {
        final HBox pane = new HBox(10);
        pane.setAlignment(Pos.TOP_CENTER);
        pane.getChildren().add(createPreviousButton(splashScreen, step, total));
        pane.getChildren().add(createNextButton(splashScreen, step, total));
        return pane;
    }

    private static Pane createBottomPane(final TN5250jSplashScreen splashScreen) {
        final Button show = new Button("Show");
        show.setOnAction(e -> splashScreen.setVisible(true));

        final Button hide = new Button("Hide");
        hide.setOnAction(e -> splashScreen.setVisible(false));

        final Button exit = new Button("Exit");
        exit.setOnAction(e -> Platform.exit());

        final GridPane pane = new GridPane();
        pane.setHgap(10);

        pane.add(show, 0, 0);
        pane.add(hide, 1, 0);
        pane.add(exit, 2, 0);

        return pane;
    }

    private static void showDialog(final Dialog<?> dialog) {
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        final double x = (bounds.getWidth() - dialog.getWidth()) / 2;
        dialog.setX(x);
        dialog.show();
    }

    private static Button createNextButton(final TN5250jSplashScreen splashScreen, final AtomicInteger step, final int total) {
        final Button button = new Button(" >> ");
        button.setOnAction(e -> {
            if (step.get() < total) {
                step.incrementAndGet();
                splashScreen.updateProgress(step.get());
            }
        });
        return button;
    }

    private static Button createPreviousButton(final TN5250jSplashScreen splashScreen, final AtomicInteger step, final int total) {
        final Button button = new Button(" << ");
        button.setOnAction(e -> {
            if (step.get() > 0) {
                step.decrementAndGet();
                splashScreen.updateProgress(step.get());
            }
        });
        return button;
    }
}
