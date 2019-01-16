lazy val Z3950 = config("z3950") describedAs ("Z3950 jars")
lazy val CustomCompile = config("compile") extend Z3950

libraryDependencies += "com.github.equella.legacy" % "z3950" % "1.1" % Z3950
libraryDependencies += "xalan" % "xalan" % "2.7.1" % Z3950

excludeDependencies := Seq(
  "commons-collections" % "commons-collections",
  "xml-apis" % "xml-apis"
)

ivyConfigurations := overrideConfigs(Z3950, CustomCompile)(
  ivyConfigurations.value)

jpfLibraryJars := Classpaths.managedJars(Z3950, Set("jar"), update.value)
