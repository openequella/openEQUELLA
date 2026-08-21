scalacOptions ++= Seq(
  "-Werror",
  "-Wunused",
  "-Xlint"
)

libraryDependencies += "org.mockito" % "mockito-core" % "5.23.0" % Test

// The GraphQL-backed services in scalasrc/ use the Scala GraphQL client, which the root build
// compiles from source - see `graphqlClient` in project/CommonSettings.scala.
dependsOn(graphqlClient)
