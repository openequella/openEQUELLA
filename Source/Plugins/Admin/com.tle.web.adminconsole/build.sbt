lazy val adminConsoleJar = project in file("jarsrc")

(Compile / resourceGenerators) += Def.task {
  val outJar  = (Compile / resourceManaged).value / "web/adminconsole.jar"
  val jarFile = (adminConsoleJar / assembly).value
  IO copyFile (jarFile, outJar)
  Seq(outJar)
}.taskValue

clean := {
  clean.value
  (adminConsoleJar / clean).value
}
