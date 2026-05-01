---
name: oeq-dev-server
description: >
  Manages the openEQUELLA local development server lifecycle. Use this skill
  whenever the user asks to start, stop, restart, or check the status of the
  openEQUELLA (oEQ) dev server. Also use it to determine whether the server is
  already running before performing tasks that require a live server.
---

## Overview

This skill manages the openEQUELLA local dev server using the `manage-server.sh`
script located in this skill's directory. The script reads runtime configuration
from `Dev/learningedge-config/mandatory-config.properties` (relative to the repo
root) to determine the server's address.

The key config values are:
- `admin.url` — the full base URL of the running server (e.g. `http://localhost:8080/`)
- `http.port` — the HTTP port (e.g. `8080`)

---

## Starting the Server

Run `manage-server.sh start` from the repo root (so that `./sbt` resolves
correctly):

```bash
bash .agents/skills/oeq-dev-server/manage-server.sh start
```

What this does:
1. Reads `admin.url` from `Dev/learningedge-config/mandatory-config.properties`
2. Checks if the server is already running — if so, exits immediately
3. Launches `./sbt equellaserver/run` in a detached background process using
   `setsid`, so it survives terminal closure
4. Saves the SBT process PID to `.oeq-server.pid` (repo root) for later use by
   the `stop` command
5. Polls the `admin.url` every 5 seconds (up to 3 minutes) until the server
   responds or the process exits unexpectedly

**Important:** Because the server is started detached, its console output (stdout
and stderr) is not captured. Default openEQUELLA dev logging goes to the console
via sbt. If the server fails to start, advise the user to run
`./sbt equellaserver/run` directly in their terminal to see the full output.

---

## Stopping the Server

```bash
bash .agents/skills/oeq-dev-server/manage-server.sh stop
```

What this does:
1. Reads the PID from `.oeq-server.pid` (if present and still alive)
2. Falls back to `pgrep -f "equellaserver"` if no valid PID file exists
3. Sends SIGTERM to the process group (to stop both sbt and the child JVM)
4. Waits up to 30 seconds for a clean shutdown; sends SIGKILL if needed
5. Removes `.oeq-server.pid` and confirms the server is no longer reachable

---

## Checking Server Status

```bash
bash .agents/skills/oeq-dev-server/manage-server.sh status
```

Curls the `admin.url` with a 5-second timeout and prints whether the server is
running or not. Exits 0 if running, 1 if not. Use this before operations that
require the server to be live.

---

## Restarting the Server

There is no dedicated `restart` subcommand. To restart, run `stop` followed by
`start`:

```bash
bash .agents/skills/oeq-dev-server/manage-server.sh stop
bash .agents/skills/oeq-dev-server/manage-server.sh start
```

---

## Notes

- All commands must be run from the **repo root** so that `./sbt` resolves
  correctly.
- The `.oeq-server.pid` file is written to the repo root. It is not committed
  (it should be in `.gitignore`).
- If multiple server processes are somehow running, `stop` will only target the
  one tracked by the PID file (or the first match from `pgrep`). Advise the user
  to check for stray Java processes if this happens.
