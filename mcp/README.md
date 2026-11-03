# tn5250j-mcp

A dev-only tool for writing IBM i green-screen RPA scripts faster. It opens one JVM with:

- a **Swing window** running a live 5250 session, where a human signs on and works
- an **MCP server** (Streamable HTTP, `127.0.0.1` only) that lets an agent read and drive that same screen
- a **transaction recorder** that captures what the human does, so an agent can turn it into a Python script

This subproject uses the TN5250J core built from the same repository. MCP starts through
`org.tn5250j.mcp.BridgeMain`; the normal desktop launcher remains `org.tn5250j.My5250`.

## Build

Requires JDK 17+, Maven, and network access for the first dependency download. Running
the terminal also requires a graphical desktop and access to an IBM i host.

**Run every command below from the TN5250J repository root**, the directory containing
`pom.xml`, `src/`, `tests/`, and `mcp/`.

```sh
# Test the core hooks and install the core artifact in your local Maven repository.
mvn -f pom.xml -DskipTests=false -Dtest=AidListenerTest,ScreenFieldTest install

# Run the MCP tests and package the executable.
mvn -f mcp/pom.xml package
```

The order matters: MCP depends on the core snapshot installed by the first command.
The core produces Java 8 bytecode; MCP produces Java 17 bytecode. The first command
selects only the hook tests because the legacy CCSID suite has known failures on modern JDKs.
The executable is `mcp/target/tn5250j-mcp.jar`. Rerun both commands after core changes;
rerun only the second after MCP-only changes.

## Run

```sh
java -jar mcp/target/tn5250j-mcp.jar --host pub400.com --tls
```

| option          | default  |                                            |
|-----------------|----------|--------------------------------------------|
| `--host`        | required | IBM i host                                 |
| `--port`        | `23`     | telnet port (`992` with `--tls`)           |
| `--tls`         | off      | use TLS; changes the default port to 992  |
| `--codepage`    | `37`     | CCSID                                      |
| `--device`      | none     | 5250 device name                           |
| `--screen-size` | `27x132` | `24x80` or `27x132`     |
| `--mcp-port`    | `8765`   | MCP HTTP port, bound to 127.0.0.1          |

The example connects to [PUB400](https://www.pub400.com/) over TLS on port `992`;
replace the host with your IBM i server. The command takes no credentials. Sign on in
the window. Closing it disconnects the session and stops the server.

For TLS certificate setup, see [TLS certificates](#tls-certificates).

Register it with Claude Code:

```sh
claude mcp add --transport http tn5250 http://127.0.0.1:8765/mcp
```

## Tools

All coordinates are **1-based** (row 1, col 1 is the top-left).

| tool              | what it does                                                              |
|-------------------|---------------------------------------------------------------------------|
| `get_screen`      | text lines, input fields, cursor, keyboard lock                           |
| `get_fields`      | input fields: row, col, length, value, protected/numeric/modified/hidden  |
| `get_cursor`      | cursor position                                                           |
| `set_field`       | `row`, `col`, `text`: fill the input field at that position               |
| `send_keys`       | `keys`: text and mnemonics such as `[enter]`, `[pf3]`, `[tab]`, `[pgdown]`|
| `wait_for_ready`  | `timeout_ms`: wait for the keyboard to unlock, returns `{ready, screen}`  |
| `start_recording` | start capturing the human's transactions                                  |
| `stop_recording`  | stop and return the recorded transactions                                 |

Non-display fields such as passwords are masked. Their characters are blanked in `lines`, and their
`value` is always `""` with `hidden: true`.

## Example: record a transaction

1. The agent calls `start_recording`.
2. The human types `1` in the option field at row 6, col 20 and presses Enter.
3. The agent calls `stop_recording`, which returns (screens abbreviated):

```json
[
  {
    "before": { "lines": ["                        MAIN MENU", "..."], "fields": ["..."], "cursor": {"row": 6, "col": 20}, "keyboardLocked": false },
    "changedFields": [
      { "row": 6, "col": 20, "length": 2, "value": "1", "protectedField": false, "numeric": false,
        "modified": true, "hidden": false, "fieldId": 1 }
    ],
    "cursor": { "row": 6, "col": 21 },
    "aid": "[enter]",
    "after": { "lines": ["                     WORK WITH ORDERS", "..."], "fields": ["..."], "cursor": {"row": 20, "col": 7}, "keyboardLocked": false }
  }
]
```

A field is in `changedFields` if its MDT was set when the key was pressed, unless the host had already set
the MDT and the value didn't change. `after` is `null` if recording stopped before the host responded.

## Target script shape

For Jython scripts with `_session` bound to a TN5250J session,
**`findByPosition(row, col)` is 0-based**, so subtract 1 from each MCP coordinate:

```python
# recorded: set (6, 20) = "1", then [enter]
field = _session.getScreen().getScreenFields().findByPosition(6 - 1, 20 - 1)
field.setString("1")
_session.getScreen().sendKeys("[enter]")
```

## Known gaps

- `[sysreq]` and `[attn]` don't go through `sendAidKey`, so they aren't recorded.
- Keystrokes aren't recorded; only field contents at the moment an AID key is pressed.
- Generated scripts aren't run inside the bridge. Replay them through the MCP tools or an application embedding TN5250J.

## Security

- The HTTP server binds to `127.0.0.1` only.
- Requests need a `127.0.0.1` or `localhost` Host header (421 otherwise), and any `Origin` header is rejected
  (403). This guards against DNS rebinding from a browser.
- The server has no authentication. Anything running as a local user can drive the session, so use it on a dev host only.
