(Compile / resourceGenerators) += Def.task {
  val base = (Compile / resourceManaged).value
  IO.copy(Some(versionProperties.value -> base / "version.properties")).toSeq
}.taskValue
