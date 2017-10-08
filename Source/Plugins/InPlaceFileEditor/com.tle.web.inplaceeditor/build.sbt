lazy val inplaceEditorJar = project in file("jarsrc")

resourceGenerators in Compile += Def.task {
  val outJar = (resourceManaged in Compile).value / "web/inplaceedit.jar"
  val jarFile = (assembly in inplaceEditorJar).value
  (jarSigner.value).apply(jarFile, outJar)
  Seq(outJar)
}.taskValue

