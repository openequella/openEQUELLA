lazy val adminConsoleJar = project in file("jarsrc")

resourceGenerators in Compile += Def.task {
  val outJar  = (resourceManaged in Compile).value / "web/adminconsole.jar"
  val jarFile = (assembly in adminConsoleJar).value
  (jarSigner.value).apply(jarFile, outJar)
  Seq(outJar)
}.taskValue
