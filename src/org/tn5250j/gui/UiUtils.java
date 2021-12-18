/**
 *
 */
package org.tn5250j.gui;

import java.awt.Rectangle;
import java.io.IOException;
import java.net.URL;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.tn5250j.tools.LangTool;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
                (int) Math.round(c.getRed() * 255.),
                (int) Math.round(c.getGreen() * 255.),
                (int) Math.round(c.getGreen() * 255.),
                (int) Math.round(c.getBlue() * 255.));
    }

    public static Color fromAwtColor(final java.awt.Color c) {
        return Color.rgb(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha() / 255.);
    }

    public static java.awt.Color toAwtColor(final Color colorBg) {
        return new java.awt.Color(toRgb(colorBg));
    }

    public static java.awt.Font toAwtFont(final Font font) {
        return new java.awt.Font(font.getName(), java.awt.Font.PLAIN, (int) Math.round(font.getSize()));
    }

    public static Rectangle toAwtRectangle(final Rectangle2D bounds) {
        return new Rectangle(
            (int) bounds.getMinX(),
            (int) bounds.getMinY(),
            (int) bounds.getWidth(),
            (int) bounds.getHeight()
        );
    }
}
