#!/usr/bin/env bash
# Helper script for interacting with the openEQUELLA GraphQL API.
#
# Subcommands:
#   config                  Print resolved server and institution URLs
#   institutions            List available institutions on the server
#   login [user] [pass]     Authenticate and save session cookie
#   schema                  Fetch the GraphQL schema (SDL format)
#   query <gql> [vars]      Execute a GraphQL query or mutation
#
# Must be run from the repo root.

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"
CONFIG_FILE="${REPO_ROOT}/Dev/learningedge-config/mandatory-config.properties"
GQL_CLIENT_LOCAL="${REPO_ROOT}/graphql-client/scala/local.properties"
COOKIE_FILE="${REPO_ROOT}/.graphql-cookies.txt"

# Default credentials (from graphql-client TestHelper.scala CREDENTIALS_ADMIN)
DEFAULT_USERNAME="TLE_ADMINISTRATOR"
DEFAULT_PASSWORD="autotestpassword"

# Default institution name (from graphql-client TestHelper.scala INSTITUTION_REST)
DEFAULT_INSTITUTION="rest"

# ---------------------------------------------------------------------------
# Read a value from a .properties file, stripping whitespace.
# Usage: read_prop_from <file> <key>
# ---------------------------------------------------------------------------
read_prop_from() {
  local file="$1" key="$2"
  grep -E "^${key}\s*=" "${file}" 2>/dev/null | sed 's/^[^=]*=\s*//' | tr -d '[:space:]'
}

# ---------------------------------------------------------------------------
# Read a value from the mandatory-config.properties file.
# ---------------------------------------------------------------------------
read_prop() {
  read_prop_from "${CONFIG_FILE}" "$1"
}

# ---------------------------------------------------------------------------
# Resolve the server root URL from mandatory-config. Ensures trailing slash.
# This is the admin URL (server root), NOT an institution URL.
# ---------------------------------------------------------------------------
get_server_root() {
  local url
  url="$(read_prop "admin.url")"
  if [[ -z "${url}" ]]; then
    local port
    port="$(read_prop "http.port")"
    port="${port:-8080}"
    url="http://localhost:${port}/"
  fi
  # Ensure trailing slash
  [[ "${url}" != */ ]] && url="${url}/"
  echo "${url}"
}

# ---------------------------------------------------------------------------
# Resolve the institution URL. This is the base for all API requests.
#
# Priority:
#   1. OEQ_INSTITUTION_URL env var (full URL)
#   2. graphql-client/scala/local.properties → oeq.institution.url
#   3. Constructed from server root + default institution name ("rest")
# ---------------------------------------------------------------------------
get_institution_url() {
  # 1. Environment variable override
  if [[ -n "${OEQ_INSTITUTION_URL:-}" ]]; then
    local url="${OEQ_INSTITUTION_URL}"
    [[ "${url}" != */ ]] && url="${url}/"
    echo "${url}"
    return
  fi

  # 2. graphql-client local.properties
  if [[ -f "${GQL_CLIENT_LOCAL}" ]]; then
    local from_file
    from_file="$(read_prop_from "${GQL_CLIENT_LOCAL}" "oeq.institution.url")"
    if [[ -n "${from_file}" ]]; then
      [[ "${from_file}" != */ ]] && from_file="${from_file}/"
      echo "${from_file}"
      return
    fi
  fi

  # 3. Fallback: server root + default institution
  local server_root
  server_root="$(get_server_root)"
  echo "${server_root}${DEFAULT_INSTITUTION}/"
}

# ---------------------------------------------------------------------------
# config: Print resolved URLs and configuration.
# ---------------------------------------------------------------------------
cmd_config() {
  local server_root institution_url
  server_root="$(get_server_root)"
  institution_url="$(get_institution_url)"

  echo "Server root:      ${server_root}"
  echo "Institution URL:  ${institution_url}"
  echo "GraphQL endpoint: ${institution_url}graphql"
  echo "Schema endpoint:  ${institution_url}graphql/schema"
  echo "Cookie file:      ${COOKIE_FILE}"
  echo "Default user:     ${DEFAULT_USERNAME}"
  echo ""
  echo "To override the institution, set OEQ_INSTITUTION_URL or edit"
  echo "graphql-client/scala/local.properties (oeq.institution.url)."
}

# ---------------------------------------------------------------------------
# institutions: List available institutions on the server.
# Parses the institution list from /institutions.do HTML page.
# ---------------------------------------------------------------------------
cmd_institutions() {
  local server_root
  server_root="$(get_server_root)"
  local inst_url="${server_root}institutions.do"

  echo "Fetching institutions from ${inst_url}..."
  echo ""

  local response
  response="$(curl -s "${inst_url}" 2>/dev/null)" || {
    echo "ERROR: Could not reach ${inst_url}. Is the server running?"
    return 1
  }

  # Institution links are in <li class="badge-row"><a href="URL"> elements
  # with a <div class="nameEllipsis">Name</div> inside.
  local results
  results="$(echo "${response}" \
    | grep -oP 'badge-row"><a href="\K[^"]+(?=">.*?nameEllipsis">[^<]+)' \
    | sort -u)"

  if [[ -z "${results}" ]]; then
    echo "No institutions found. The page format may have changed."
    echo "Try opening ${inst_url} in a browser instead."
  else
    echo "Available institutions:"
    # Also extract the institution names for display
    echo "${response}" \
      | grep -oP 'badge-row"><a href="\K[^"]+">.*?nameEllipsis">([^<]+)' \
      | sed 's/">.*nameEllipsis">/  /' \
      | sort -t/ -k4
  fi
}

# ---------------------------------------------------------------------------
# login: Authenticate via the REST login endpoint and save the session cookie.
# Usage: login [username] [password]
# ---------------------------------------------------------------------------
cmd_login() {
  local username="${1:-${DEFAULT_USERNAME}}"
  local password="${2:-${DEFAULT_PASSWORD}}"
  local institution_url
  institution_url="$(get_institution_url)"
  local login_url="${institution_url}api/auth/login?username=${username}&password=${password}"

  echo "Logging in as '${username}' at ${institution_url}..."

  local http_code
  http_code="$(curl -s -o /dev/null -w '%{http_code}' -c "${COOKIE_FILE}" -X POST "${login_url}")"

  if [[ "${http_code}" == "200" ]]; then
    local jsessionid
    jsessionid="$(grep -oP 'JSESSIONID\s+\K\S+' "${COOKIE_FILE}" 2>/dev/null || true)"
    echo "Login successful."
    if [[ -n "${jsessionid}" ]]; then
      echo "JSESSIONID: ${jsessionid}"
    fi
    echo "Cookie saved to: ${COOKIE_FILE}"
  else
    echo "ERROR: Login failed (HTTP ${http_code})."
    echo "  - Check that the server is running."
    echo "  - Check the institution URL is correct (run 'config' to verify)."
    echo "  - Credentials are passed as query parameters, not JSON body."
    echo "  - Default credentials: ${DEFAULT_USERNAME} / ${DEFAULT_PASSWORD}"
    return 1
  fi
}

# ---------------------------------------------------------------------------
# schema: Fetch and print the GraphQL schema in SDL format.
# ---------------------------------------------------------------------------
cmd_schema() {
  local institution_url
  institution_url="$(get_institution_url)"
  local schema_url="${institution_url}graphql/schema"

  if [[ ! -f "${COOKIE_FILE}" ]]; then
    echo "WARNING: No cookie file found. Run 'login' first if authentication is required." >&2
  fi

  curl -s -b "${COOKIE_FILE}" "${schema_url}"
}

# ---------------------------------------------------------------------------
# query: Execute a GraphQL query or mutation.
# Usage: query <graphql-string> [variables-json]
#
# Examples:
#   query '{ collections { details { id name } } }'
#   query 'mutation StartEdit($id: Long!) { collection { startEdit(id: $id) { entity { details { id name } } } } }' '{"id": 1234}'
# ---------------------------------------------------------------------------
cmd_query() {
  if [[ $# -lt 1 ]]; then
    echo "Usage: graphql-helper.sh query <graphql-string> [variables-json]"
    echo ""
    echo "Examples:"
    echo "  graphql-helper.sh query '{ collections { details { id name } } }'"
    echo "  graphql-helper.sh query 'query(\$id: Long!) { ... }' '{\"id\": 1234}'"
    return 1
  fi

  local gql_query="$1"
  local variables="${2:-}"
  local institution_url
  institution_url="$(get_institution_url)"
  local graphql_url="${institution_url}graphql"

  if [[ ! -f "${COOKIE_FILE}" ]]; then
    echo "ERROR: No cookie file found. Run 'login' first."
    return 1
  fi

  # Build the JSON body
  local body
  if [[ -n "${variables}" ]]; then
    body=$(jq -n --arg q "${gql_query}" --argjson v "${variables}" '{"query": $q, "variables": $v}')
  else
    body=$(jq -n --arg q "${gql_query}" '{"query": $q}')
  fi

  local response
  response="$(curl -s -X POST "${graphql_url}" \
    -H 'Content-Type: application/json' \
    -b "${COOKIE_FILE}" \
    -d "${body}")"

  # Pretty-print with jq if available, otherwise raw
  if command -v jq &>/dev/null; then
    echo "${response}" | jq .
  else
    echo "${response}"
  fi
}

# ---------------------------------------------------------------------------
# Entry point
# ---------------------------------------------------------------------------
case "${1:-}" in
  config)        cmd_config ;;
  institutions)  cmd_institutions ;;
  login)         shift; cmd_login "$@" ;;
  schema)        cmd_schema ;;
  query)         shift; cmd_query "$@" ;;
  *)
    echo "Usage: graphql-helper.sh <config|institutions|login|schema|query>"
    echo ""
    echo "  config               Print resolved server and institution URLs"
    echo "  institutions         List available institutions on the server"
    echo "  login [user] [pass]  Authenticate and save session cookie"
    echo "                       Defaults: ${DEFAULT_USERNAME} / ${DEFAULT_PASSWORD}"
    echo "  schema               Fetch the GraphQL schema (SDL format)"
    echo "  query <gql> [vars]   Execute a GraphQL query or mutation"
    exit 1
    ;;
esac
