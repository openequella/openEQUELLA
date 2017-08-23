resourceGenerators in Compile += Def.task {
  val base = (resourceManaged in Compile).value
  IO.copy(Some(versionProperties.value -> base / "web/version.properties")).toSeq
}.taskValue