#!/usr/bin/env bash
# Manages the openEQUELLA local dev server: start | stop | status
#
# Config is read at runtime from:
#   Dev/learningedge-config/mandatory-config.properties
#
# PID tracking file (relative to repo root):
#   .oeq-server.pid

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
CONFIG_FILE="${REPO_ROOT}/Dev/learningedge-config/mandatory-config.properties"
PID_FILE="${REPO_ROOT}/.oeq-server.pid"

# ---------------------------------------------------------------------------
# Read a value from the .properties config file, stripping whitespace.
# Usage: read_prop KEY
# ---------------------------------------------------------------------------
read_prop() {
  local key="$1"
  grep -E "^${key}\s*=" "${CONFIG_FILE}" | sed 's/^[^=]*=\s*//' | tr -d '[:space:]'
}

# ---------------------------------------------------------------------------
# Derive the admin URL and port from config.
# ---------------------------------------------------------------------------
get_admin_url() {
  read_prop "admin.url"
}

# ---------------------------------------------------------------------------
# status: Check whether the server is reachable.
# Exits 0 if running, 1 if not.
# ---------------------------------------------------------------------------
cmd_status() {
  local admin_url
  admin_url="$(get_admin_url)"

  if curl --silent --fail --max-time 5 "${admin_url}" > /dev/null 2>&1; then
    echo "openEQUELLA is RUNNING at ${admin_url}"
    return 0
  else
    echo "openEQUELLA is NOT running (checked ${admin_url})"
    return 1
  fi
}

# ---------------------------------------------------------------------------
# start: Launch sbt equellaserver/run in the background and wait for it to
# become reachable.
# ---------------------------------------------------------------------------
cmd_start() {
  local admin_url
  admin_url="$(get_admin_url)"

  # Bail out early if already running.
  if curl --silent --fail --max-time 5 "${admin_url}" > /dev/null 2>&1; then
    echo "openEQUELLA is already running at ${admin_url}"
    return 0
  fi

  echo "Starting openEQUELLA dev server..."
  echo "  Config:    ${CONFIG_FILE}"
  echo "  Admin URL: ${admin_url}"
  echo ""
  echo "NOTE: Server output (stdout/stderr) is not captured — console logging"
  echo "      will appear in the terminal where sbt was launched via sbt directly."
  echo "      This skill starts the server in a detached background process."
  echo ""

  # Start sbt in the background, fully detached from the current session.
  # setsid ensures the process gets its own session so it survives terminal close.
  cd "${REPO_ROOT}"
  setsid ./sbt equellaserver/run > /dev/null 2>&1 &
  local sbt_pid=$!
  echo "${sbt_pid}" > "${PID_FILE}"
  echo "Server process started (PID ${sbt_pid}). Waiting for it to become ready..."

  # Poll the admin URL for up to 3 minutes (180 seconds), checking every 5 s.
  local elapsed=0
  local timeout=180
  local interval=5

  while [[ ${elapsed} -lt ${timeout} ]]; do
    sleep ${interval}
    elapsed=$((elapsed + interval))

    if curl --silent --fail --max-time 5 "${admin_url}" > /dev/null 2>&1; then
      echo ""
      echo "openEQUELLA is READY at ${admin_url} (took ~${elapsed}s)"
      return 0
    fi

    # Check the process is still alive.
    if ! kill -0 "${sbt_pid}" 2>/dev/null; then
      echo ""
      echo "ERROR: The sbt process (PID ${sbt_pid}) has exited unexpectedly."
      echo "Check your sbt/server configuration. You may need to run"
      echo "'./sbt equellaserver/run' manually to see the error output."
      rm -f "${PID_FILE}"
      return 1
    fi

    printf "  Still waiting... (%ds elapsed)\n" "${elapsed}"
  done

  echo ""
  echo "ERROR: Server did not become ready within ${timeout}s."
  echo "The sbt process (PID ${sbt_pid}) may still be starting. Check server"
  echo "output by running './sbt equellaserver/run' manually."
  return 1
}

# ---------------------------------------------------------------------------
# stop: Gracefully stop the running server.
# ---------------------------------------------------------------------------
cmd_stop() {
  local admin_url
  admin_url="$(get_admin_url)"

  # Resolve PID: prefer the PID file, fall back to pgrep.
  local pid=""
  if [[ -f "${PID_FILE}" ]]; then
    pid="$(cat "${PID_FILE}")"
    if ! kill -0 "${pid}" 2>/dev/null; then
      echo "WARNING: PID file exists but process ${pid} is not running."
      rm -f "${PID_FILE}"
      pid=""
    fi
  fi

  if [[ -z "${pid}" ]]; then
    # Fallback: search for the equellaserver sbt process by command line.
    pid="$(pgrep -f "equellaserver" | head -1 || true)"
  fi

  if [[ -z "${pid}" ]]; then
    echo "No running openEQUELLA server process found."
    return 0
  fi

  echo "Stopping openEQUELLA server (PID ${pid})..."

  # Send SIGTERM to the process group to catch sbt + child JVM processes.
  local pgid
  pgid="$(ps -o pgid= -p "${pid}" | tr -d '[:space:]')" || pgid=""

  if [[ -n "${pgid}" && "${pgid}" != "1" ]]; then
    kill -TERM -"${pgid}" 2>/dev/null || true
  else
    kill -TERM "${pid}" 2>/dev/null || true
  fi

  # Wait up to 30 seconds for the server to shut down.
  local elapsed=0
  while [[ ${elapsed} -lt 30 ]]; do
    sleep 2
    elapsed=$((elapsed + 2))
    if ! kill -0 "${pid}" 2>/dev/null; then
      break
    fi
  done

  if kill -0 "${pid}" 2>/dev/null; then
    echo "WARNING: Process did not exit after 30s; sending SIGKILL..."
    kill -KILL "${pid}" 2>/dev/null || true
  fi

  rm -f "${PID_FILE}"

  # Confirm via URL check.
  sleep 2
  if curl --silent --fail --max-time 5 "${admin_url}" > /dev/null 2>&1; then
    echo "WARNING: Server still appears to be responding at ${admin_url}."
    echo "There may be another server process running."
  else
    echo "openEQUELLA server has stopped."
  fi
}

# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------
case "${1:-}" in
  start)  cmd_start  ;;
  stop)   cmd_stop   ;;
  status) cmd_status ;;
  *)
    echo "Usage: manage-server.sh <start|stop|status>"
    echo ""
    echo "  start   Start the openEQUELLA dev server in the background"
    echo "  stop    Stop the running openEQUELLA dev server"
    echo "  status  Check whether the server is currently running"
    exit 1
    ;;
esac
