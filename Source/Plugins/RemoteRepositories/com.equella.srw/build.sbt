lazy val SRW           = config("srw")
lazy val CustomCompile = config("compile") extend SRW

libraryDependencies += "org.dspace.oclc" % "oclc-srw" % "1.0.20080328" % SRW

ivyConfigurations := overrideConfigs(SRW, CustomCompile)(ivyConfigurations.value)

jpfLibraryJars := Classpaths.managedJars(SRW, Set("jar"), update.value)
