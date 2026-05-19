---
name: oeq-graphql
description: >
  Interact with the openEQUELLA GraphQL API during development. Use this skill
  to authenticate, inspect the schema, execute queries and mutations, and
  iterate on GraphQL changes. Delegates server lifecycle to the oeq-dev-server
  skill.
---

## Overview

This skill provides tools for working with the openEQUELLA GraphQL API via
cURL. It has a helper script with simple subcommands for authentication, schema
inspection, and query execution.

Use this skill when:
- Testing GraphQL queries or mutations against the running dev server
- Inspecting the current GraphQL schema (SDL format)
- Iterating on schema changes (modify → recompile → restart → verify)
- Debugging GraphQL errors or authentication issues

---

## Prerequisites

### Server Must Be Running

Before using this skill, ensure the openEQUELLA dev server is running. Use the
**oeq-dev-server** skill to check status, start, or stop the server. Do not use
`./sbt` directly for server lifecycle — always delegate to that skill.

### Required Tools

- `curl` — for HTTP requests
- `jq` — for JSON formatting and constructing request bodies (required)

---

## Key Concept: Institution URL

GraphQL requests are scoped to an **institution**, not to the server root. The
server's admin URL (e.g. `http://localhost:9090/`) is the server root, but
GraphQL endpoints live under a specific institution:

```
http://localhost:9090/rest/graphql        ← GraphQL endpoint for "rest" institution
http://localhost:9090/vanilla/graphql     ← GraphQL endpoint for "vanilla" institution
```

The helper script resolves the institution URL automatically (see Configuration
Resolution below). If you need to discover which institutions exist on the
server, use the `institutions` subcommand.

---

## Configuration Resolution

The helper script resolves configuration from existing project files. **No
additional configuration is needed in most cases.**

### Institution URL Resolution (in priority order)

1. **`OEQ_INSTITUTION_URL` environment variable** — if set, used directly
   (e.g. `http://localhost:9090/vanilla`)
2. **`graphql-client/scala/local.properties`** — reads `oeq.institution.url`
   if the file exists (this is the developer's local config for the GraphQL
   client project)
3. **Fallback** — constructs from `admin.url` in
   `Dev/learningedge-config/mandatory-config.properties` + default institution
   name `rest` (matching the graphql-client test suite's default institution)

### Other Settings

| Setting | Source | Details |
|---------|--------|---------|
| Default username | Hardcoded | `TLE_ADMINISTRATOR` (from `TestHelper.scala` `CREDENTIALS_ADMIN`) |
| Default password | Hardcoded | `autotestpassword` (from `TestHelper.scala` `CREDENTIALS_ADMIN`) |

For the `rest` test institution, you can also use the `AutoTest` / `automated`
credentials (`CREDENTIALS_AUTOTEST` in `TestHelper.scala`).

---

## Helper Script Usage

All commands must be run from the **repo root**.

### Print configuration

```bash
bash .agents/skills/oeq-graphql/graphql-helper.sh config
```

Prints the resolved institution URL, GraphQL endpoint, schema endpoint, cookie
file location, and default username. Use this to verify configuration before
making requests.

### List available institutions

```bash
bash .agents/skills/oeq-graphql/graphql-helper.sh institutions
```

Fetches the server's institution listing page and prints the available
institution URLs. Useful when you don't know which institutions are configured
on the running server.

### Authenticate

```bash
# Login with default credentials (TLE_ADMINISTRATOR)
bash .agents/skills/oeq-graphql/graphql-helper.sh login

# Login with specific credentials
bash .agents/skills/oeq-graphql/graphql-helper.sh login AutoTest automated
```

This calls the REST login endpoint with credentials as **query parameters**
(not JSON body — using a JSON body causes a 500 error). The session cookie is
saved to `.graphql-cookies.txt` in the repo root.

### Fetch the schema

```bash
bash .agents/skills/oeq-graphql/graphql-helper.sh schema
```

Returns the full GraphQL schema in SDL format. Use this to:
- Verify that schema changes rendered correctly after a restart
- Discover available types, queries, and mutations
- Check field names and argument types before writing queries

### Execute a query or mutation

```bash
# Simple query
bash .agents/skills/oeq-graphql/graphql-helper.sh query '{ collection { list { id uuid } } }'

# Mutation
bash .agents/skills/oeq-graphql/graphql-helper.sh query 'mutation { collection { startEdit(id: 1234) { entity { details { id uuid } } } } }'

# Query with variables
bash .agents/skills/oeq-graphql/graphql-helper.sh query \
  'mutation StartEdit($id: Long!) { collection { startEdit(id: $id) { entity { details { id uuid } } } } }' \
  '{"id": 1234}'
```

The response is automatically formatted with `jq`. Both queries and mutations
use the `query` subcommand (GraphQL uses POST for everything).

### Override the institution URL

To target a different institution for a single command, set the
`OEQ_INSTITUTION_URL` environment variable:

```bash
OEQ_INSTITUTION_URL=http://localhost:9090/vanilla \
  bash .agents/skills/oeq-graphql/graphql-helper.sh login
```

---

## Common Workflows

### Test a query against the running server

1. Use the **oeq-dev-server** skill to check the server is running (start it if
   needed).
2. Authenticate:
   ```bash
   bash .agents/skills/oeq-graphql/graphql-helper.sh login
   ```
3. Run your query:
   ```bash
   bash .agents/skills/oeq-graphql/graphql-helper.sh query '{ collection { list { id uuid } } }'
   ```

### Iterate on schema changes

When modifying GraphQL types or resolvers:

1. Use the **oeq-dev-server** skill to stop the server.
2. Recompile: `./sbt compile`
3. Use the **oeq-dev-server** skill to start the server.
4. Re-authenticate (previous session is invalidated):
   ```bash
   bash .agents/skills/oeq-graphql/graphql-helper.sh login
   ```
5. Verify schema changes:
   ```bash
   bash .agents/skills/oeq-graphql/graphql-helper.sh schema
   ```
6. Test with queries:
   ```bash
   bash .agents/skills/oeq-graphql/graphql-helper.sh query '{ ... }'
   ```

**Important:** The GraphQL schema is generated at startup, so you **must
recompile and restart** after changing schema types or resolvers.

### Working with entity locks (startEdit / cancelEdit)

When testing mutations that lock entities:

```bash
# Start editing
bash .agents/skills/oeq-graphql/graphql-helper.sh query \
  'mutation { collection { startEdit(id: 1234) { entity { details { id uuid } } } } }'

# Always cancel when done — otherwise the entity stays locked
bash .agents/skills/oeq-graphql/graphql-helper.sh query \
  'mutation { collection { cancelEdit(id: 1234) } }'
```

**Always `cancelEdit` after a `startEdit`** during testing. If you forget, the
entity remains locked and subsequent `startEdit` calls will fail with
`"Entity is locked"`.

---

## Troubleshooting

| Symptom | Cause | Fix |
|---------|-------|-----|
| HTTP 400 on all POST requests | Schema validation failed at startup | Check server logs — likely an invalid type in the schema |
| HTTP 401 / `"Not logged in"` | Missing or expired session | Re-run `login` |
| HTTP 500 on login | Credentials sent as JSON body | This script uses query params correctly — check you're using the script |
| `"Entity is locked"` on `startEdit` | Previous `startEdit` not cancelled | Call `cancelEdit` first |
| Empty/null fields in response | Fields not requested in selection set | GraphQL only returns what you ask for |
| `login` fails with connection error | Server not running | Use the oeq-dev-server skill to start it |
| Schema doesn't reflect your changes | Server not restarted after changes | Stop, recompile, restart (see iteration workflow above) |
| Wrong institution | Institution URL resolved incorrectly | Run `config` to check, then override with `OEQ_INSTITUTION_URL` |

---

## Notes

- The cookie file `.graphql-cookies.txt` is created in the repo root and is
  gitignored. It is not committed.
- All commands must be run from the **repo root**.
- The script depends on `jq` for JSON construction and formatting.
- For the Scala GraphQL client tests (a different testing approach), see
  `graphql-client/scala/README.md`.
