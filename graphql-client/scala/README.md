# Scala GraphQL Client for openEQUELLA

This library is currently intended to be built and published locally before you then run the oEQ
build. Long term, the idea will be to publish this to a public Maven repository.

> **Prerequisites:** Ensure you have the openEQUELLA development environment set up as described
> in the root [CONTRIBUTING.md](../../CONTRIBUTING.md) before following these instructions.

Guide for later on how to do publishing: <https://www.scala-sbt.org/1.x/docs/Publishing.html>

To build and publish locally, run the following command from the `graphql-client/scala` directory:

```bash
sbt publishLocal
```

This places the library in your local Ivy repository, where the main oEQ build can then find it.

## Code Generation

First, download the latest schema from a local instance of openEQUELLA:

```bash
wget http://localhost:8080/vanilla/graphql/schema
```

> **Note:** This assumes your institution URL is `http://localhost:8080/vanilla` and that the
> GraphQL schema endpoint is enabled. To enable it, set `graphql.schema=true` in
> `optional-config.properties`.

Move the downloaded `schema` file into `src/main/resources/` and rename it to `schema.graphql`.

Alternatively, the following SBT task handles the download and places the file in the correct
location automatically, replacing the manual steps above:

```bash
sbt downloadSchema
```

By default the task targets `http://localhost:8080/vanilla`. To use a different institution URL,
configure it in order of precedence (lowest → highest):

1. **`local.properties` file** — copy `local.properties.sample` to `local.properties` (gitignored)
   and set:
   ```properties
   oeq.institution.url=http://localhost:9090/myinst
   ```
2. **CLI `-D` flag** — override for a single run:
   ```bash
   sbt -Doeq.institution.url=http://localhost:9090/myinst downloadSchema
   ```

Once the schema is in place, open the SBT console and run the following task to generate the client
classes:

```sbt
calibanGenClient
  src/main/resources/schema.graphql
  src/main/scala/io/github/openequella/graphql/client/
  --splitFiles true
  --packageName io.github.openequella.graphql.client
  --scalarMappings LocalDateTime:java.time.LocalDateTime
```

> **Note:** The trailing slash on the output path is required.

The generated files are stored in `src/main/scala/io/github/openequella/graphql/client/` and
committed to the repository, making it easy to review exactly what changed in the generated API
when the schema is updated.

After code generation, run the SBT `compile` task to automatically add license headers to the
generated files:

```bash
sbt compile
```

This uses the [sbt-header plugin](https://github.com/sbt/sbt-header), which automatically applies
the header set in `build.sbt` to all source files during compilation.

## Test Configuration

The test suite runs integration tests against a local openEQUELLA instance on the `rest` institution
(the standard test institution used by CI). By default, it assumes the instance is on
`localhost:8080`. You can customize the port for local development:

The `vanilla` institution must also be present. The cross-institution security tests source an entity
from `rest` and confirm it is invisible to a `vanilla` session — entity IDs are globally unique, so a
by-ID lookup which is not institution filtered would return another institution's entity. Both
institutions are in the standard dev fixture set (`autotest/institutions/<name>/institution`). Those
tests authenticate to `vanilla` as `TLE_ADMINISTRATOR`, so no user or ACL setup is needed there.

### Quick Start (CLI override only)

Run tests with a different server port:

```bash
sbt -Doeq.test.port=9090 test
```

### Persistent Configuration (recommended for IntelliJ)

To avoid creating multiple IntelliJ run configurations:

1. Copy the sample configuration file:

   ```bash
   cp src/test/resources/test.properties.sample src/test/resources/test.properties
   ```

2. Edit `src/test/resources/test.properties` and set your local port:

   ```properties
   oeq.test.port=9090
   ```

3. Run tests normally — your configuration is automatically loaded:

   ```bash
   sbt test
   ```

**Note:** `test.properties` is gitignored — do not commit it. Only `.sample` is committed.

### Configuration Precedence

Settings are applied in this order (later values override earlier ones):

1. **Default value:** `localhost:8080`
2. **`test.properties` file:** If present in `src/test/resources/`
3. **CLI `-D` options:** Highest priority — e.g., `-Doeq.test.port=9090`

This means:
- CI uses the default port 8080 (no local test.properties file)
- Local development can use `test.properties` for convenience
- Individual test runs can still override via CLI if needed

Tests run against the `rest` institution (the standard test institution) to ensure consistent test
expectations and data fixtures. The only exception is the cross-institution security tests, which
additionally require `vanilla` — see [Test Configuration](#test-configuration).

## IntelliJ Setup

### Add the graphql-client module

Because this Scala GraphQL client is a standalone SBT project — not a sub-project of the root
openEQUELLA build — it must be manually added to IntelliJ to work on it within the same IDE
instance. To do this:

1. Open `File > Project Structure` (or `Ctrl+Alt+Shift+S`).
2. Navigate to `Project Settings > Modules`.
3. Click the **Add** button and select **Import Module**.
4. Select the `graphql-client/scala` directory and click **OK**.
5. Set the module JDK to match that of the main project.
6. Click **OK** in the **Project Structure** dialog — IntelliJ will handle the rest.

### Mark the Caliban `ArgBuilder` import as used

Caliban relies heavily on macros at compile time. One import required for this is
`caliban.schema.ArgBuilder.auto._`. IntelliJ will always flag this as unused, so running
**Optimize Imports** will silently remove it and break compilation. To prevent this, mark the
import as always used:

1. Open **Settings**.
2. Navigate to `Editor > Code Style > Scala`.
3. Select the **Imports** tab.
4. In the **Imports always marked as used** section (bottom right), add
   `caliban.schema.ArgBuilder.auto._`.

### Dedicated working directory

> **Temporary:** This section is only relevant while the GraphQL feature branch has not yet been
> merged to `develop`. It can be removed once that merge is complete.

Switching between the main branches (e.g. `develop`) and the GraphQL feature branches
(e.g. `component/admin-comms`) can cause issues due to the additional module. The recommended
approach is to maintain a dedicated working directory:

1. Use [git worktrees](https://git-scm.com/docs/git-worktree) to create an additional working
   directory — this avoids the overhead of a full clone.
2. Create a separate local database for the new working directory.
3. In the new working directory, run `./sbt prepareDevConfig` and then adjust the generated
   configuration:
  - Point it at your new database.
  - Set a different port (e.g. `9090`) so it doesn't clash with your main working directory.

> **Note on git hooks:** Pre-commit hooks are shared across worktrees because there is only one
> real `.git` directory (in the original working directory). In practice this is usually fine —
> Husky delegates to tooling in the active working directory — but it is worth keeping in mind if
> anything unexpected happens with the pre-commit hooks.

## Code Quality

This project enforces strict code quality standards:

### Compiler Warnings

The Scala compiler is configured with strict warning settings via `scalacOptions`:

- `-Werror`: Treat all compiler warnings as errors, ensuring code quality is maintained
- `-Wunused`: Warn about unused imports, variables, and other unused declarations
- `-Xlint`: Enable additional linting checks for best practices

These settings ensure that code quality issues are caught early in the development process and must
be resolved before code can compile.

### Static Analysis with Scapegoat

[Scapegoat](https://github.com/scapegoat-scala/scapegoat) is integrated as a static code analyzer.
It runs during compilation to detect potential bugs and code smells. Generated files in
`src/main/scala/io/github/openequella/graphql/client/` are excluded from Scapegoat analysis since
they are automatically generated.
