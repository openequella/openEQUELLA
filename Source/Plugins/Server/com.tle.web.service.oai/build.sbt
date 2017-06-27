lazy val OAICat = config("rhino")
lazy val CustomCompile = config("compile") extend OAICat

libraryDependencies += "org.oclc.oai" % "oaicat" % "1.5.57" % OAICat

ivyConfigurations := overrideConfigs(OAICat, CustomCompile)(ivyConfigurations.value)

jpfLibraryJars := Classpaths.managedJars(OAICat, Set("jar"), update.value)

