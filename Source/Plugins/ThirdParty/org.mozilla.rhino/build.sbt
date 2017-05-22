lazy val Rhino = config("rhino")
lazy val CustomCompile = config("compile") extend Rhino

libraryDependencies += "org.mozilla" % "rhino" % "1.7R4" % Rhino

ivyConfigurations := overrideConfigs(Rhino, CustomCompile)(ivyConfigurations.value)

jpfLibraryJars := Classpaths.managedJars(Rhino, Set("jar"), update.value)
