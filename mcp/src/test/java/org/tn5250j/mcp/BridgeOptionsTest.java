package org.tn5250j.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BridgeOptionsTest {

    @Test
    void defaultsWhenOnlyHostGiven() {
        BridgeOptions options = BridgeOptions.parse(new String[] {"--host", "ibmi.dev.local"});

        assertEquals("ibmi.dev.local", options.host());
        assertEquals(23, options.port());
        assertFalse(options.tls());
        assertEquals("37", options.codePage());
        assertNull(options.deviceName());
        assertEquals(BridgeOptions.ScreenSize.SIZE_27X132, options.screenSize());
        assertEquals(8765, options.mcpPort());
    }

    @Test
    void tlsUsesSecureTelnetPortUnlessOverridden() {
        BridgeOptions defaults = BridgeOptions.parse(new String[] {"--host", "ibmi.dev.local", "--tls"});
        BridgeOptions custom = BridgeOptions.parse(new String[] {"--host", "ibmi.dev.local", "--tls",
                "--port", "9471"});

        assertTrue(defaults.tls());
        assertEquals(992, defaults.port());
        assertEquals(9471, custom.port());
    }

    @Test
    void allOptions() {
        BridgeOptions options = BridgeOptions.parse(new String[] {
                "--host", "10.0.0.5", "--port", "992", "--codepage", "273", "--device", "DEVMCP01",
                "--screen-size", "24x80", "--mcp-port", "9000"});

        assertEquals(992, options.port());
        assertEquals("273", options.codePage());
        assertEquals("DEVMCP01", options.deviceName());
        assertEquals(BridgeOptions.ScreenSize.SIZE_24X80, options.screenSize());
        assertEquals(9000, options.mcpPort());
    }

    @Test
    void hostIsRequired() {
        assertThrows(IllegalArgumentException.class, () -> BridgeOptions.parse(new String[] {"--port", "23"}));
    }

    @Test
    void unknownOptionIsRejected() {
        assertThrows(IllegalArgumentException.class,
                () -> BridgeOptions.parse(new String[] {"--host", "h", "--password", "secret"}));
    }

    @Test
    void optionWithoutValueIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> BridgeOptions.parse(new String[] {"--host"}));
    }

    @Test
    void portsMustBeInRange() {
        assertThrows(IllegalArgumentException.class,
                () -> BridgeOptions.parse(new String[] {"--host", "h", "--port", "0"}));
        assertThrows(IllegalArgumentException.class,
                () -> BridgeOptions.parse(new String[] {"--host", "h", "--mcp-port", "65536"}));
        assertThrows(IllegalArgumentException.class,
                () -> BridgeOptions.parse(new String[] {"--host", "h", "--port", "telnet"}));
    }

    @Test
    void screenSizeMustBeSupported() {
        assertThrows(IllegalArgumentException.class,
                () -> BridgeOptions.parse(new String[] {"--host", "h", "--screen-size", "43x80"}));
    }
}
