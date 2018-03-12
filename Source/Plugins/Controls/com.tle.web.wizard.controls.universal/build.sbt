import sbt.IO

resourceGenerators in Compile += Def.task {
  val baseJs = baseDirectory.value / "js"
  Common.runYarn("build", baseJs)
  val outDir = (resourceManaged in Compile).value
  val baseJsTarget = baseJs / "target"
  IO.copy((baseJsTarget ** ("*.js"|"*.css"|"*.json")).pair(rebase(baseJsTarget, outDir))).toSeq
}.taskValue
