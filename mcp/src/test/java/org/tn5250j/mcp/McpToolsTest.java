package org.tn5250j.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.json.McpJsonDefaults;

class McpToolsTest {

    @Test
    void registersTheEightTools() {
        McpTools tools = new McpTools(null, null, McpJsonDefaults.getMapper());

        List<String> names = tools.specifications().stream().map(spec -> spec.tool().name()).toList();

        assertEquals(List.of(McpTools.GET_SCREEN, McpTools.GET_FIELDS, McpTools.GET_CURSOR, McpTools.SET_FIELD,
                McpTools.SEND_KEYS, McpTools.WAIT_FOR_READY, McpTools.START_RECORDING, McpTools.STOP_RECORDING),
                names);
    }

    @Test
    void recordedTransactionSerializesWithRecordComponentNames() throws Exception {
        FieldSnapshot field = new FieldSnapshot(6, 20, 2, "1", false, true, true, false, 3);
        ScreenSnapshot screen = new ScreenSnapshot(List.of("MAIN MENU"), List.of(field), new Position(6, 21), false);
        RecordedTransaction transaction = new RecordedTransaction(screen, List.of(field), new Position(6, 21),
                "[enter]", null);

        String json = McpJsonDefaults.getMapper().writeValueAsString(transaction);

        assertEquals("{\"before\":{\"lines\":[\"MAIN MENU\"],\"fields\":[{\"row\":6,\"col\":20,\"length\":2,"
                + "\"value\":\"1\",\"protectedField\":false,\"numeric\":true,\"modified\":true,\"hidden\":false,"
                + "\"fieldId\":3}],\"cursor\":{\"row\":6,\"col\":21},\"keyboardLocked\":false},"
                + "\"changedFields\":[{\"row\":6,\"col\":20,\"length\":2,\"value\":\"1\",\"protectedField\":false,"
                + "\"numeric\":true,\"modified\":true,\"hidden\":false,\"fieldId\":3}],"
                + "\"cursor\":{\"row\":6,\"col\":21},\"aid\":\"[enter]\",\"after\":null}", json);
    }

    @Test
    void integerArgumentAcceptsWholeNumbersOfAnyNumericType() {
        assertEquals(7, McpTools.requiredInt(Map.of("row", 7), "row"));
        assertEquals(7, McpTools.requiredInt(Map.of("row", 7L), "row"));
        assertEquals(7, McpTools.requiredInt(Map.of("row", 7.0), "row"));
    }

    @Test
    void integerArgumentRejectsFractionsStringsAndMissing() {
        assertThrows(IllegalArgumentException.class, () -> McpTools.requiredInt(Map.of("row", 7.5), "row"));
        assertThrows(IllegalArgumentException.class, () -> McpTools.requiredInt(Map.of("row", "7"), "row"));
        assertThrows(IllegalArgumentException.class, () -> McpTools.requiredInt(Map.of(), "row"));
    }

    @Test
    void stringArgumentRejectsNonStrings() {
        assertEquals("[enter]", McpTools.requiredString(Map.of("keys", "[enter]"), "keys"));
        assertThrows(IllegalArgumentException.class, () -> McpTools.requiredString(Map.of("keys", 1), "keys"));
    }

    @Test
    void timeoutDefaultsAndIsBounded() {
        assertEquals(10_000, McpTools.timeoutMs(Map.of()));
        assertEquals(500, McpTools.timeoutMs(Map.of("timeout_ms", 500)));
        assertThrows(IllegalArgumentException.class, () -> McpTools.timeoutMs(Map.of("timeout_ms", -1)));
        assertThrows(IllegalArgumentException.class, () -> McpTools.timeoutMs(Map.of("timeout_ms", 120_001)));
    }
}
