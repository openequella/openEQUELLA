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
```

**NOTE:** Important to include the trailing slash for the output path.

### Storage of generated files

The generated files are stored in the `src/main/scala/io/github/openequella/graphql/client` directory.
We then also keep this in the repository so that it is straightforward to see what has changed.
