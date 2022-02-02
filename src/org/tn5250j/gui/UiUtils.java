/**
 *
 */
package org.tn5250j.gui;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.tn5250j.framework.tn5250.Rect;
import org.tn5250j.tools.GUIGraphicsUtils;
import org.tn5250j.tools.LangTool;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Region;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public final class UiUtils {
    private UiUtils() {
    }

    public static Button addOptButton(final Button button, final String textKey,
            final EventHandler<ActionEvent> listener) {
        // we check if there was mnemonic specified and if there was then we
        // set it.
        if (textKey != null) {
            setLabel(button, textKey);
        }

        button.setOnAction(listener);
        button.setAlignment(Pos.CENTER);
        button.setTextAlignment(TextAlignment.CENTER);
        return button;
    }

    /**
     * @param button button.
     * @param textKey label to set.
     */
    public static void setLabel(final Button button, final String textKey) {
        final String text = LangTool.getString(textKey);
        final int mnemIdx = text.indexOf("&");
        if (mnemIdx >= 0) {
            button.setMnemonicParsing(true);
            button.setText(text.replace('&', '_'));
        } else {
            button.setText(text);
        }
    }

    public static void setUpCloseListener(final ReadOnlyObjectProperty<Scene> prop, final Consumer<WindowEvent> listener) {
        prop.addListener((src, old, value) -> {
            if (value != null) {
                value.windowProperty().addListener((windowOwner, oldWindow, newWindow) -> {
                    if (newWindow != null) {
                        newWindow.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST, listener::accept);
                    }
                });
            }
        });
    }

    public static Rect toRect(final Rectangle2D bounds) {
        return new Rect(
            round(bounds.getMinX()),
            round(bounds.getMinY()),
            round(bounds.getWidth()),
            round(bounds.getHeight())
        );
    }

    /**
     * @param controller controller.
     * @param template template.
     * @return loader.
     */
    public static Parent loadTempalte(final Object controller, final String template) {
        final FXMLLoader loader = UiUtils.createLoader(template);
        loader.setControllerFactory(cls -> {
            return controller;
        });

        try {
            return loader.load();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load template", e);
        }
    }

    public static FXMLLoader createLoader(final String fxml) {
        final FXMLLoader loader = new FXMLLoader();
        final URL xmlUrl = UiUtils.class.getResource(fxml);
        loader.setLocation(xmlUrl);
        return loader;
    }

    public static void closeMe(final Scene scene) {
        final Window window = scene.getWindow();
        if (window instanceof Stage) { //JavaFX
            ((Stage) window).close();
        } else { //Swing
            final JFXPanel fxPanel = findFxPanel(scene);
            if (fxPanel != null) {
                final java.awt.Window dialog = (java.awt.Window) SwingUtilities.getRoot(fxPanel);
                dialog.setVisible(false);
            }
        }
    }

    private static JFXPanel findFxPanel(final Scene scene) {
        if (scene.getUserData() instanceof JFXPanel) {
            return (JFXPanel) scene.getUserData();
        }

        if (scene.getRoot() != null && scene.getRoot().getScene() != null) {
            return findFxPanel(scene.getRoot().getScene());
        }
        return null;
    }

    public static final <T> void showDialog(final Window owner, final String fxml,
            final String title, final Consumer<T> controllerConsumer) {
        showDialog(owner, createLoader(fxml), title, controllerConsumer);
    }

    public static <T> void showDialog(final Window owner, final FXMLLoader loader, final String title,
            final Consumer<T> controllerConsumer) {
        try {
            final Parent parent = loader.load();

            if (controllerConsumer != null) {
                controllerConsumer.accept(loader.getController());
            }

            final Stage stage = new Stage();
            stage.getIcons().addAll(GUIGraphicsUtils.getApplicationIconsFx());
            stage.setScene(new Scene(parent));
            stage.setTitle(title);

            if (owner != null) {
                stage.initOwner(owner);
                stage.initModality(Modality.WINDOW_MODAL);
            }

            stage.showAndWait();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load dialog template", e);
        }
    }

    public static void beep() {
        final AudioClip audio = new AudioClip(UiUtils.class.getClassLoader().getResource(
                "beep.wav").toExternalForm());
        audio.play();
    }

    public static int toRgb(final int r, final int g, final int b) {
        return toRgb(r, g, b, 255);
    }

    public static int toRgb(final int r, final int g, final int b, final int a) {
        return ((a & 0xFF) << 24) |
                ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                ((b & 0xFF) << 0);
    }

    public static int toRgb(final Color c) {
        return toRgb(
                round(c.getRed() * 255.),
                round(c.getGreen() * 255.),
                round(c.getBlue() * 255.),
                round(c.getOpacity() * 255.));
    }

    public static Color rgb(final int rgb) {
        final int red = (rgb >> 16) & 0xFF;
        final int green =  (rgb >> 8) & 0xFF;
        final int blue = (rgb >> 0) & 0xFF;
        final int alpha = (rgb >> 24) & 0xff;
        return Color.rgb(red, green, blue, alpha / 255.) ;
    }

    public static int round(final double d) {
        return (int) Math.ceil(d);
    }

    public static Font deriveFont(final Font f, final double size) {
        return new Font(f.getName(), size);
    }

    /**
     * @param g graphics context.
     * @param rect rectangle to fill.
     */
    public static void fill(final GraphicsContext g, final Rectangle2D rect) {
        g.fillRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    public static void setBackground(final Region node, final Color bg) {
        node.setBackground(new Background(new BackgroundFill(
                bg, CornerRadii.EMPTY, Insets.EMPTY)));
    }

    public static <T> T runInFxAndWait(final Callable<T> call) {
        try {
            if (Platform.isFxApplicationThread()) {
                return call.call();
            }

            final CompletableFuture<T> feature = new CompletableFuture<>();
            Platform.runLater(() -> {
                try {
                    feature.complete(call.call());
                } catch (final Throwable e) {
                    feature.completeExceptionally(e);
                }
            });
            return feature.get();
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param call callable.
     */
    public static void runInFx(final Callable<?> call) {
        try {
            if (Platform.isFxApplicationThread()) {
                call.call();
            }

            Platform.runLater(() -> {
                try {
                    call.call();
                } catch (final Exception e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Alert createInputDialog(final Node content, final String okButtonText, final String canceButtonText) {
        final Alert alert = new Alert(AlertType.CONFIRMATION);

        if (okButtonText != null) {
            changeButtonText(alert.getDialogPane(), ButtonType.OK, okButtonText);
        }
        if (canceButtonText != null) {
            changeButtonText(alert.getDialogPane(), ButtonType.CANCEL, canceButtonText);
        }

        alert.getDialogPane().setContent(content);
        alert.setHeaderText("");
        return alert;
    }

    public static void changeButtonText(final DialogPane dialogPane, final ButtonType button, final String text) {
        ((Button) dialogPane.lookupButton(button)).setText(text);
    }

    public static void showError(final Throwable exc, final String title) {
        showError(exc.getMessage() == null ? exc.toString() : exc.getMessage(), title);
    }

    public static void showError(final String message, final String title) {
        showAlert(message, title, AlertType.ERROR);
    }

    public static void showWarning(final String message, final String title) {
        showAlert(message, title, AlertType.WARNING);
    }

    private static void showAlert(final String message, final String title, final AlertType alertType) {
        final Alert alert = new Alert(alertType);
        alert.setContentText(message);
        if (title != null) {
            alert.setTitle(title);
        }
        alert.setHeaderText("");
        alert.showAndWait();
    }

    public static boolean showYesNoWarning(final String message, final String title) {
        final Alert alert = new Alert(AlertType.WARNING, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        return alert.showAndWait().orElse(null) == ButtonType.YES;
    }

    public static boolean showYesNoConfirm(final String message, final String title) {
        final Alert alert = new Alert(AlertType.CONFIRMATION, message, ButtonType.YES, ButtonType.NO);
        alert.setTitle(title);
        return alert.showAndWait().orElse(null) == ButtonType.YES;
    }
}
