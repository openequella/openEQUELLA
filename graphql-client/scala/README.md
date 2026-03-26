# Scala GraphQL Client for openEQUELLA

This library is currently intended to be built and published locally before you then run the oEQ
build. Long term, the idea will be to publish this publicly.

Guide for later on how to do publishing: <https://www.scala-sbt.org/1.x/docs/Publishing.html>

For now, the following commands should be run in the `graphql-client/scala` directory:

```
sbt publishLocal
```

This will then place the library in your local Ivy repository.

## Code Generation

First, download the latest schema from a local instance of openEQUELLA. This can be done by running
the following command:

```
wget http://localhost:8080/vanilla/graphql/schema
```

(This assumes that your institution URL is `http://localhost:8080/vanilla` and that you have enabled
the GraphQL schema endpoint. To do this set the `graphql.schema` property to `true` in the
`optional-config.properties` file.)

Now place the `schema` file you downloaded in the `src/main/resources` directory. And rename it to
`schema.graphql` so that the following instructions work.

Alternatively, you can use the SBT task:

```
sbt downloadSchema
```

Lastly, run the following command in SBT to generate the client classes:

```
calibanGenClient
  src/main/resources/schema.graphql
  src/main/scala/io/github/openequella/graphql/client/
  --splitFiles true
  --packageName io.github.openequella.graphql.client
  --scalarMappings LocalDateTime:java.time.LocalDateTime
```

**NOTE:** Important to include the trailing slash for the output path.

### Storage of generated files

The generated files are stored in the `src/main/scala/io/github/openequella/graphql/client` directory.
We then also keep this in the repository so that it is straightforward to see what has changed.

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

Switching between the main branches (e.g. `develop`) and the GraphQL feature branches
(e.g. `component/admin-comms`) can cause issues due to the additional module. Until the feature
branch is merged back to `develop`, the recommended approach is to maintain a dedicated working
directory:

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

