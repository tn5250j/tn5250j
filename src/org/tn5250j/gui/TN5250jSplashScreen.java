/**
 *
 */
package org.tn5250j.gui;

import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class TN5250jSplashScreen {
    private final ProgressBar progressBar = new ProgressBar();
    private final BorderPane content;

    private int progress;
    private int steps;

    private Stage stage;

    public TN5250jSplashScreen(final String imageResource) {
        super();

        final ImageView image = new ImageView(getClass().getClassLoader().getResource(
                imageResource).toExternalForm());

        this.content = new BorderPane();
        content.setCenter(image);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setProgress(0.);
        content.setBottom(progressBar);
    }

    public void setSteps(final int steps) {
        this.steps = steps;
        updateProgress();
    }

    public synchronized void updateProgress(final int prog) {
        progress = prog;
        updateProgress();
    }

    private void updateProgress() {
        final int max = Math.max(steps, 0);
        final int value = Math.min(progress, max);

        if (max > 0 && value >= 0) {
            Platform.runLater(() -> progressBar.setProgress((double) value / max));
        }
    }

    public void setVisible(final boolean visible) {
        Platform.runLater(() -> doSetVisible(visible));
    }

    private void doSetVisible(final boolean visible) {
        Stage currentStage = stage;

        synchronized (this) {
            if (visible && stage != null || !visible && stage == null) {
                return;
            }
            if (!visible) {
                stage = null;
                clearStage(currentStage);
            } else {
                stage = new Stage();
                currentStage = stage;
                currentStage.initStyle(StageStyle.UNDECORATED);

                final Scene scene = new Scene(createRoot());
                stage.setScene(scene);
                scene.setCursor(Cursor.WAIT);
            }
        }

        if (visible) {
            currentStage.show();
        } else {
            currentStage.close();
        }
    }

    private Parent createRoot() {
        final BorderPane bp = new BorderPane();
        bp.setCenter(content);
        return bp;
    }

    private static void clearStage(final Stage currentStage) {
        final BorderPane bp = (BorderPane) currentStage.getScene().getRoot();
        bp.getChildren().clear();
    }
}
