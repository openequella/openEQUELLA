lazy val OldRhino = config("oldrhino")
lazy val CustomCompile = config("compile") extend OldRhino

libraryDependencies ++= Seq(
  "rhino" % "js" % "1.7R2"
).map(_ % OldRhino)

ivyConfigurations := overrideConfigs(OldRhino, CustomCompile)(ivyConfigurations.value)

jpfLibraryJars := Classpaths.managedJars(OldRhino, Set("jar"), update.value)
