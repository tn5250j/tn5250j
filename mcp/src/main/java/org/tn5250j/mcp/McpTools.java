package org.tn5250j.mcp;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.JsonSchema;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;

/** The MCP tool surface: argument parsing, validation, and JSON results. */
public final class McpTools {

    static final String GET_SCREEN = "get_screen";
    static final String GET_FIELDS = "get_fields";
    static final String GET_CURSOR = "get_cursor";
    static final String SET_FIELD = "set_field";
    static final String SEND_KEYS = "send_keys";
    static final String WAIT_FOR_READY = "wait_for_ready";
    static final String START_RECORDING = "start_recording";
    static final String STOP_RECORDING = "stop_recording";

    private static final String ARG_ROW = "row";
    private static final String ARG_COL = "col";
    private static final String ARG_TEXT = "text";
    private static final String ARG_KEYS = "keys";
    private static final String ARG_TIMEOUT_MS = "timeout_ms";

    private static final long DEFAULT_TIMEOUT_MS = 10_000;
    private static final long MAX_TIMEOUT_MS = 120_000;

    private static final String COORDINATES_NOTE = "Coordinates are 1-based (row 1, col 1 is top-left).";

    private final ScreenDriver driver;
    private final TransactionRecorder recorder;
    private final McpJsonMapper jsonMapper;

    public McpTools(ScreenDriver driver, TransactionRecorder recorder, McpJsonMapper jsonMapper) {
        this.driver = driver;
        this.recorder = recorder;
        this.jsonMapper = jsonMapper;
    }

    public List<SyncToolSpecification> specifications() {
        return List.of(
                tool(GET_SCREEN, true,
                        "Current screen: text lines (trailing spaces trimmed, non-display text blanked), input fields,"
                                + " cursor, keyboard lock. " + COORDINATES_NOTE,
                        noArguments(),
                        arguments -> driver.snapshot()),
                tool(GET_FIELDS, true,
                        "Input fields on the current screen with position, length, value and flags. Values of"
                                + " hidden (non-display) fields are never returned. " + COORDINATES_NOTE,
                        noArguments(),
                        arguments -> driver.snapshot().fields()),
                tool(GET_CURSOR, true,
                        "Cursor position. " + COORDINATES_NOTE,
                        noArguments(),
                        arguments -> driver.snapshot().cursor()),
                tool(SET_FIELD, false,
                        "Put text into the input field at row/col (any position inside the field). Fails if there is"
                                + " no input field there, it is protected, the text is too long, or the keyboard is"
                                + " locked. Does not send anything to the host. " + COORDINATES_NOTE,
                        schema(Map.of(
                                ARG_ROW, integerProperty("1-based row"),
                                ARG_COL, integerProperty("1-based column"),
                                ARG_TEXT, stringProperty("text to place in the field")),
                                List.of(ARG_ROW, ARG_COL, ARG_TEXT)),
                        arguments -> Map.of("fieldStart", driver.setField(
                                new Position(requiredInt(arguments, ARG_ROW), requiredInt(arguments, ARG_COL)),
                                requiredString(arguments, ARG_TEXT)))),
                tool(SEND_KEYS, false,
                        "Type text and/or key mnemonics at the cursor, e.g. \"[enter]\", \"[pf3]\", \"[tab]\","
                                + " \"[pgdown]\". Call wait_for_ready after an AID key such as [enter].",
                        schema(Map.of(ARG_KEYS, stringProperty("text and mnemonics")), List.of(ARG_KEYS)),
                        arguments -> {
                            driver.sendKeys(requiredString(arguments, ARG_KEYS));
                            return Map.of("sent", true);
                        }),
                tool(WAIT_FOR_READY, true,
                        "Wait until the host unlocks the keyboard, then return {ready, screen}. ready=false means"
                                + " the timeout passed.",
                        schema(Map.of(ARG_TIMEOUT_MS, integerProperty(
                                "max wait in ms, default " + DEFAULT_TIMEOUT_MS + ", max " + MAX_TIMEOUT_MS)),
                                List.of()),
                        arguments -> driver.waitForReady(timeoutMs(arguments))),
                tool(START_RECORDING, false,
                        "Start recording the transactions a human performs in the session window.",
                        noArguments(),
                        arguments -> {
                            recorder.start();
                            return Map.of("recording", true);
                        }),
                tool(STOP_RECORDING, false,
                        "Stop recording and return the transactions: before screen, changed fields, cursor, AID key"
                                + " mnemonic, after screen. " + COORDINATES_NOTE,
                        noArguments(),
                        arguments -> recorder.stop()));
    }

    private SyncToolSpecification tool(String name, boolean readOnly, String description, JsonSchema inputSchema,
            Function<Map<String, Object>, Object> action) {
        Tool tool = Tool.builder()
                .name(name)
                .description(description)
                .inputSchema(inputSchema)
                .annotations(ToolAnnotations.builder().readOnlyHint(readOnly).build())
                .build();
        return SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((exchange, request) -> call(action,
                        request.arguments() == null ? Map.of() : request.arguments()))
                .build();
    }

    private CallToolResult call(Function<Map<String, Object>, Object> action, Map<String, Object> arguments) {
        try {
            return CallToolResult.builder()
                    .addTextContent(jsonMapper.writeValueAsString(action.apply(arguments)))
                    .build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return error(e.getMessage());
        } catch (IOException e) {
            return error("could not serialize result: " + e.getMessage());
        }
    }

    private static CallToolResult error(String message) {
        return CallToolResult.builder().addTextContent(message).isError(true).build();
    }

    static int requiredInt(Map<String, Object> arguments, String name) {
        Object value = arguments.get(name);
        if (!(value instanceof Number number) || number.doubleValue() != Math.rint(number.doubleValue())) {
            throw new IllegalArgumentException(name + " must be an integer");
        }
        return number.intValue();
    }

    static String requiredString(Map<String, Object> arguments, String name) {
        if (!(arguments.get(name) instanceof String text)) {
            throw new IllegalArgumentException(name + " must be a string");
        }
        return text;
    }

    static long timeoutMs(Map<String, Object> arguments) {
        if (!arguments.containsKey(ARG_TIMEOUT_MS)) {
            return DEFAULT_TIMEOUT_MS;
        }
        int timeout = requiredInt(arguments, ARG_TIMEOUT_MS);
        if (timeout < 0 || timeout > MAX_TIMEOUT_MS) {
            throw new IllegalArgumentException(ARG_TIMEOUT_MS + " must be between 0 and " + MAX_TIMEOUT_MS);
        }
        return timeout;
    }

    private static JsonSchema noArguments() {
        return schema(Map.of(), List.of());
    }

    private static JsonSchema schema(Map<String, Object> properties, List<String> required) {
        return JsonSchema.builder().type("object").properties(properties).required(required)
                .additionalProperties(false).build();
    }

    private static Map<String, Object> integerProperty(String description) {
        return Map.of("type", "integer", "description", description);
    }

    private static Map<String, Object> stringProperty(String description) {
        return Map.of("type", "string", "description", description);
    }
}
