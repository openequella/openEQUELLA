resourceGenerators in Compile += Def.task {
  val base = (resourceManaged in Compile).value
  IO.copy(Some(versionProperties.value -> base / "com/tle/core/services/version.properties")).toSeq
}.taskValue