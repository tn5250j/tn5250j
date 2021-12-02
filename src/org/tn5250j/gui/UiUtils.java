/**
 *
 */
package org.tn5250j.gui;

import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.tn5250j.tools.LangTool;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.embed.swing.JFXPanel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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

    /**
     * @param fxml root FXML template
     * @return loader with controllers shared between loaded templates.
     */
    public static FXMLLoader withSharedControllers(final String fxml) {
        final Map<Class<?>, Object> singletons = new ConcurrentHashMap<>();

        final FXMLLoader loader = createLoader(fxml);
        loader.setControllerFactory(cls -> {
            Object obj = singletons.get(cls);
            if (obj == null) {
                obj = newInstance(cls);
                singletons.put(cls, obj);
            }
            return obj;
        });

        return loader;
    }

    private static Object newInstance(final Class<?> cls) {
        try {
            return cls.getConstructor().newInstance();
        } catch (final Exception e) {
            throw new RuntimeException("Failed to instanciate class: " + cls.getName(), e);
        }
    }
}
