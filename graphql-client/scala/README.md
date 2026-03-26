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
