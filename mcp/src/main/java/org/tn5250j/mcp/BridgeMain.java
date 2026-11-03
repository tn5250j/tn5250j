package org.tn5250j.mcp;

import java.awt.GraphicsEnvironment;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import org.tn5250j.Session5250;
import org.tn5250j.TN5250jConstants;
import org.tn5250j.framework.common.SessionManager;

import io.modelcontextprotocol.json.McpJsonDefaults;

/** Entry point: session window + MCP server in one JVM. */
public final class BridgeMain {

    private static final String SESSION_NAME = "TN5250J-MCP";
    // prod (as400-rpa-core) enables keep-alive; it also keeps the session up while an agent is thinking
    private static final String HEART_BEAT_ENABLED = "1";
    private static final int EXIT_USAGE = 2;
    private static final int EXIT_FAILURE = 1;

    private BridgeMain() {
    }

    public static void main(String[] args) {
        BridgeOptions options;
        try {
            options = BridgeOptions.parse(args);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println(BridgeOptions.USAGE);
            System.exit(EXIT_USAGE);
            return;
        }
        if (GraphicsEnvironment.isHeadless()) {
            System.err.println("tn5250j-mcp needs a display for the session window");
            System.exit(EXIT_FAILURE);
        }

        Session5250 session = openSession(options);
        McpTools tools = new McpTools(new ScreenDriver(session), new TransactionRecorder(session),
                McpJsonDefaults.getMapper());

        McpHttpServer server;
        try {
            server = McpHttpServer.start(options.mcpPort(), tools.specifications());
        } catch (Exception e) {
            System.err.println("could not start MCP server on " + McpHttpServer.LOOPBACK_ADDRESS + ":"
                    + options.mcpPort() + ": " + e.getMessage());
            System.exit(EXIT_FAILURE);
            return;
        }

        SessionWindow.open(session, "tn5250j-mcp - " + options.host(), shutdownOnce(session, server));
        // Session5250.connect() does its socket work on its own daemon thread
        session.connect();

        System.out.println("MCP endpoint: http://" + McpHttpServer.LOOPBACK_ADDRESS + ":" + options.mcpPort()
                + McpHttpServer.ENDPOINT);
    }

    /** Mirrors as400-rpa-core's AS400RPAConnection.connect() session properties. */
    private static Session5250 openSession(BridgeOptions options) {
        Properties properties = new Properties();
        properties.put(TN5250jConstants.SESSION_HOST, options.host());
        properties.put(TN5250jConstants.SESSION_HOST_PORT, Integer.toString(options.port()));
        properties.put(TN5250jConstants.SESSION_CODE_PAGE, options.codePage());
        properties.put(TN5250jConstants.SESSION_SCREEN_SIZE, options.screenSize().sessionPropertyValue);
        properties.put(TN5250jConstants.SESSION_HEART_BEAT, HEART_BEAT_ENABLED);
        if (options.tls()) {
            properties.put(TN5250jConstants.SSL_TYPE, TN5250jConstants.SSL_TYPE_TLS);
        }
        if (options.deviceName() != null) {
            properties.put(TN5250jConstants.SESSION_DEVICE_NAME, options.deviceName());
        }
        return SessionManager.instance().openSession(properties, null, SESSION_NAME);
    }

    private static Runnable shutdownOnce(Session5250 session, McpHttpServer server) {
        AtomicBoolean done = new AtomicBoolean();
        return () -> {
            if (!done.compareAndSet(false, true)) {
                return;
            }
            try {
                server.close();
            } catch (Exception e) {
                System.err.println("error stopping MCP server: " + e.getMessage());
            }
            if (session.getVT() != null) {
                session.disconnect();
            }
            // tn5250j and Swing can leave non-daemon threads behind
            System.exit(0);
        };
    }
}
