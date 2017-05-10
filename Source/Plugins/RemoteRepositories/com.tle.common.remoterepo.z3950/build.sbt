lazy val Z3950 = config("z3950") describedAs("Z3950 jars")

libraryDependencies += "org.jafer" % "z3950" % "1.1" % Z3950

excludeDependencies := Seq(
  "commons-collections" % "commons-collections",
  "xalan" % "xalan",
  "xalan" % "serializer",
  "xml-apis" % "xml-apis"
)

ivyConfigurations := overrideConfigs(Z3950)(ivyConfigurations.value)

jpfLibraryJars := Classpaths.managedJars(Z3950, Set("jar"), update.value)
