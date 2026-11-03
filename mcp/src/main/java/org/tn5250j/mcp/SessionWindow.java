package org.tn5250j.mcp;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.tn5250j.Session5250;
import org.tn5250j.SessionPanel;
import org.tn5250j.tools.LangTool;

/** The Swing window the human uses to sign on and work in the session. */
public final class SessionWindow {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;

    private SessionWindow() {
    }

    /** Opens the window on the EDT; {@code onClosed} runs once the window is closed. */
    public static void open(Session5250 session, String title, Runnable onClosed) {
        SwingUtilities.invokeLater(() -> {
            // SessionPanel needs tn5250j's message bundle, which only the standalone app initializes
            LangTool.init();

            JFrame frame = new JFrame(title);
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setContentPane(new SessionPanel(session));
            frame.setSize(WIDTH, HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent event) {
                    onClosed.run();
                }
            });
            frame.setVisible(true);
        });
    }
}
