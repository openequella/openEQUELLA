lazy val filemanagerApplet = (project in file("appletsrc"))

(Compile / resourceGenerators) += Def.task {
  val outJar  = (Compile / resourceManaged).value / "web/filemanager.jar"
  val jarFile = (LocalProject("filemanagerApplet") / assembly).value
  (jarSigner.value).apply(jarFile, outJar)
  Seq(outJar)
}.taskValue
