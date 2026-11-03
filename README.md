# TN5250J
A 5250 terminal emulator for the IBM i (AS/400) written in Java.

Documentation is available at: [tn5250j.github.io](https://tn5250j.github.io/)

[![Build Status](https://travis-ci.org/tn5250j/tn5250j.svg?branch=travis)](https://travis-ci.org/tn5250j/tn5250j)

## History

This project was created because there was no terminal emulator for Linux with features like continued edit fields, gui windows, cursor progression fields, etc.

It was then open sourced to give something back to all those hackers and code churners that work so hard to provide the Linux and Open Source communities with quality work and software.

The original developer wanted it to work on different operating systems, thus Java giving the “J” at the end.

## Hosting

The project was previous hosted at [sourceforge.net](https://sourceforge.net/projects/tn5250j/). But since 2016 has been migrated to GitHub.

## Optional MCP launcher

The `mcp/` subproject provides a separate Java 17+ executable that opens a live terminal
window with a loopback-only MCP server and transaction recorder. The core remains Java 8
compatible, and the normal desktop entry point is still `org.tn5250j.My5250`.

See [the MCP README](mcp/README.md) for prerequisites, the build/test commands, startup,
tools, recording behavior, and security details.
