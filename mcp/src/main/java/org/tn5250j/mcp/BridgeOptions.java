package org.tn5250j.mcp;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.tn5250j.TN5250jConstants;

/**
 * Command-line options. There are deliberately no credential options: the human signs on in the window.
 */
public record BridgeOptions(String host, int port, String codePage, String deviceName, ScreenSize screenSize,
        int mcpPort, boolean tls) {

    public enum ScreenSize {
        SIZE_24X80("24x80", TN5250jConstants.SCREEN_SIZE_24X80_STR),
        SIZE_27X132("27x132", TN5250jConstants.SCREEN_SIZE_27X132_STR);

        final String label;
        final String sessionPropertyValue;

        ScreenSize(String label, String sessionPropertyValue) {
            this.label = label;
            this.sessionPropertyValue = sessionPropertyValue;
        }

        static ScreenSize fromLabel(String label) {
            return Arrays.stream(values())
                    .filter(size -> size.label.equals(label))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "--screen-size must be 24x80 or 27x132, got " + label));
        }
    }

    static final String HOST = "--host";
    static final String PORT = "--port";
    static final String CODE_PAGE = "--codepage";
    static final String DEVICE = "--device";
    static final String SCREEN_SIZE = "--screen-size";
    static final String MCP_PORT = "--mcp-port";
    static final String TLS = "--tls";

    private static final Set<String> KNOWN_OPTIONS = Set.of(HOST, PORT, CODE_PAGE, DEVICE, SCREEN_SIZE, MCP_PORT);

    private static final int DEFAULT_TELNET_PORT = 23;
    private static final int DEFAULT_TLS_PORT = 992;
    private static final String DEFAULT_CODE_PAGE = "37";
    // prod (as400-rpa-core) always opens 27x132 sessions
    private static final ScreenSize DEFAULT_SCREEN_SIZE = ScreenSize.SIZE_27X132;
    private static final int DEFAULT_MCP_PORT = 8765;
    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;

    public static final String USAGE = "usage: tn5250j-mcp --host <ibm-i-host> [--port <port>] [--codepage 37]"
            + " [--tls] [--device <name>] [--screen-size 24x80|27x132] [--mcp-port 8765]";

    public static BridgeOptions parse(String[] args) {
        Map<String, String> values = new HashMap<>();
        boolean tls = false;
        for (int index = 0; index < args.length;) {
            String option = args[index];
            if (option.equals(TLS)) {
                if (tls) throw new IllegalArgumentException(TLS + " may only be specified once");
                tls = true;
                index++;
                continue;
            }
            if (!KNOWN_OPTIONS.contains(option)) {
                throw new IllegalArgumentException("unknown option " + option);
            }
            if (index + 1 >= args.length) {
                throw new IllegalArgumentException(option + " needs a value");
            }
            values.put(option, args[index + 1]);
            index += 2;
        }

        String host = values.get(HOST);
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException(HOST + " is required");
        }

        return new BridgeOptions(
                host,
                portOption(values, PORT, tls ? DEFAULT_TLS_PORT : DEFAULT_TELNET_PORT),
                values.getOrDefault(CODE_PAGE, DEFAULT_CODE_PAGE),
                values.get(DEVICE),
                values.containsKey(SCREEN_SIZE) ? ScreenSize.fromLabel(values.get(SCREEN_SIZE)) : DEFAULT_SCREEN_SIZE,
                portOption(values, MCP_PORT, DEFAULT_MCP_PORT),
                tls);
    }

    private static int portOption(Map<String, String> values, String option, int defaultPort) {
        String raw = values.get(option);
        if (raw == null) {
            return defaultPort;
        }
        int port;
        try {
            port = Integer.parseInt(raw);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(option + " must be a number, got " + raw);
        }
        if (port < MIN_PORT || port > MAX_PORT) {
            throw new IllegalArgumentException(option + " must be between 1 and 65535, got " + port);
        }
        return port;
    }
}
