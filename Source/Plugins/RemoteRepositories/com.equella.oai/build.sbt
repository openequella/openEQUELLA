lazy val OAICat        = config("oaicat")
lazy val CustomCompile = config("compile") extend OAICat

libraryDependencies += "com.github.openequella.legacy" % "oaicat" % "1.5.62" % OAICat

ivyConfigurations := overrideConfigs(OAICat, CustomCompile)(ivyConfigurations.value)

jpfLibraryJars := Classpaths.managedJars(OAICat, Set("jar"), update.value)
