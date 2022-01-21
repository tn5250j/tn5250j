/**
 *
 */
package org.tn5250j;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.tn5250j.gui.UiUtils;
import org.tn5250j.sessionsettings.AbstractAttributesController;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class DevTools {
    private static final String LOCALHOST = "127.0.0.1";

    public static SessionBean createSessionBean() throws Exception {
        final Session5250 session = createSession();
        return createSessionBean(session);
    }

    public static Session5250 createSession() {
        final SessionConfig config = createSessionConfig();
        config.setProperty("font", "Lucida Sans Typewriter Regular"); // example config

        return new Session5250(new Properties(), LOCALHOST, LOCALHOST, config);
    }

    public static SessionConfig createSessionConfig() {
        return new SessionConfig(LOCALHOST, LOCALHOST);
    }

    public static SessionBean createSessionBean(final Session5250 session) throws UnknownHostException {
        final SessionBean sessionBean = new SessionBean(session);

        sessionBean.setHostName(LOCALHOST);
        sessionBean.setCodePage("Cp273");
        sessionBean.setNoSaveConfigFile();
        sessionBean.setScreenSize("27x132");
        sessionBean.setDeviceName("devname");

        return sessionBean;
    }

    public static JFrame createClosableSwingFrame(final String title, final Runnable closingListener) {
        final JFrame frame = new JFrame(title);

        final Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = size.width * 2 / 3;
        final int h = size.height * 2 / 3;

        frame.setLocation((size.width - w) / 2, (size.height - h) / 2);
        frame.setSize(w, h);

        frame.addWindowListener(
            new WindowAdapter() {
                @Override
                public void windowClosing(final WindowEvent e) {
                    if (closingListener != null) {
                        closingListener.run();
                    }
                }
            }
        );

        return frame;
    }

    public static ButtonType showInDialog(final AbstractAttributesController controller, final String template) {
        final Dialog<ButtonType> dialog = createDialog(controller, template);
        return dialog.showAndWait().orElse(null);
    }

    public static Dialog<ButtonType> createDialog(final AbstractAttributesController controller,
            final String template) {
        final FXMLLoader loader = UiUtils.createLoader(template);
        loader.setControllerFactory(cls -> {
            return controller;
        });

        try {
            loader.load();
        } catch (final IOException e) {
            throw new RuntimeException("Failed to load template", e);
        }

        final Dialog<ButtonType> dialog = new Dialog<>();
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.setTitle("Demo");
        dialog.getDialogPane().setContent(controller.getView());
        dialog.setResizable(true);
        return dialog;
    }

    public static Stage createClosableFxFrame(final String title, final Parent node) {
        final Stage frame = new Stage();
        frame.setTitle(title);
        frame.setScene(new Scene(node));
        return frame;
    }

    public static void showInFrame(final JPanel panel) {
        final JFrame frame = createClosableSwingFrame(LOCALHOST, null);
        frame.getContentPane().setLayout(new BorderLayout());
        frame.getContentPane().add(panel, BorderLayout.CENTER);

        frame.setVisible(true);
    }
}
