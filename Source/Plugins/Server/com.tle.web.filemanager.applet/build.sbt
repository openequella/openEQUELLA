lazy val filemanagerApplet = (project in file("appletsrc"))

resourceGenerators in Compile += Def.task {
  val outJar = (resourceManaged in Compile).value / "web/filemanager.jar"
  val jarFile = (assembly in LocalProject("filemanagerApplet")).value
  (jarSigner.value).apply(jarFile, outJar)
  Seq(outJar)
}.taskValue