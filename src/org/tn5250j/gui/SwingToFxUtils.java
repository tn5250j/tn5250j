/**
 *
 */
package org.tn5250j.gui;

import static org.tn5250j.gui.UiUtils.round;
import static org.tn5250j.gui.UiUtils.toRgb;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.im.InputMethodRequests;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.tn5250j.tools.GUIGraphicsUtils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Dimension2D;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * @author Vyacheslav Soldatov <vyacheslav.soldatov@inbox.ru>
 *
 */
public class SwingToFxUtils {
    public static final JFrame SHARED_FRAME = new JFrame();
    private static JFXPanel PANEL;

    static {
        //init JavaFX
        initFx();
    }

    public static synchronized void initFx() {
        if (PANEL != null) {
            return;
        }

        PANEL = new JFXPanel();
        Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
        Platform.setImplicitExit(false);
    }

    public static <T> T showFxTemplate(final JFrame parent, final String title, final String fxml) {
        // This method is invoked on the EDT thread
        final JDialog frame = new JDialog(parent == null ? SHARED_FRAME : parent, title);
        frame.setIconImages(GUIGraphicsUtils.getApplicationIcons());

        final JFXPanel fxPanel = new JFXPanel() {
            private static final long serialVersionUID = 1L;
            @Override
            public InputMethodRequests getInputMethodRequests() {
                return null;
            }
        };
        frame.add(fxPanel);

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final int w = screen.width * 2 / 3;
        final int h = screen.height * 2 / 3;

        frame.setLocation((screen.width - w) / 2, (screen.height - h) / 2);
        frame.setSize(w, h);
        frame.setModal(true);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        final AtomicReference<T> controller = new AtomicReference<>();
        Platform.runLater(() -> showTemplate(fxPanel, fxml, controller::set));
        frame.setVisible(true);

        return controller.get();
    }

    private static <T> void showTemplate(final JFXPanel fxPanel, final String fxml, final Consumer<T> consumer) {
        try {
            final FXMLLoader loader = UiUtils.createLoader(fxml);
            final Parent root = loader.load();
            final Scene scene = new Scene(root);
            fxPanel.setScene(scene);
            scene.setUserData(fxPanel);

            consumer.accept(loader.getController());
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static JFXPanel createSwingPanel(final Parent node) {
        final JFXPanel fxPane = new JFXPanel();
        final Scene scene = new Scene(node);
        fxPane.setScene(scene);
        return fxPane;
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

    public static Font fromAwtFont(final java.awt.Font font) {
        return new Font(font.getName(), font.getSize());
    }

    /**
     * @param dim dimension.
     * @return int dimension.
     */
    public static Dimension toAwtDimension(final Dimension2D dim) {
        return new Dimension(round(dim.getWidth()), round(dim.getHeight()));
    }

    public static Frame getRoot(final Object object) {
        if (object instanceof Component) {
            return (Frame) SwingUtilities.getRoot((Component) object);
        }
        return SwingToFxUtils.SHARED_FRAME;
    }

    public static Rectangle toAwtRectangle(final Rectangle2D bounds) {
        return new Rectangle(
            round(bounds.getMinX()),
            round(bounds.getMinY()),
            round(bounds.getWidth()),
            round(bounds.getHeight())
        );
    }

    public static Rectangle2D fromAwtRect(final Rectangle rect) {
        return new Rectangle2D(rect.x, rect.y, rect.width, rect.height);
    }

    public static java.awt.geom.Line2D toAwtLine(final Line2D origin) {
        final java.awt.geom.Line2D line = new java.awt.geom.Line2D.Double(
                origin.getStart().getX(),
                origin.getStart().getY(),
                origin.getEnd().getX(),
                origin.getEnd().getY()
        );
        return line ;
    }

    public static Point toAwtPoint(final Point2D p) {
        return new Point(round(p.getX()), round(p.getY()));
    }
}
