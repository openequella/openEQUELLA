scalacOptions ++= Seq(
  "-Werror",
  "-Wunused",
  "-Xlint"
)

libraryDependencies += "org.mockito" % "mockito-core" % "5.23.0" % Test
