lazy val adminConsoleJar = project in file("jarsrc")

resourceGenerators in Compile += Def.task {
  val outJar  = (resourceManaged in Compile).value / "web/adminconsole.jar"
  val jarFile = (assembly in adminConsoleJar).value
  (jarSigner.value).apply(jarFile, outJar)
  Seq(outJar)
}.taskValue

assemblyMergeStrategy in assembly := {
  // Post SpringHib5 upgrade, the following error was thrown on build:
  // deduplicate: different file contents found in the following:
  // [error] .../com.fasterxml/classmate/bundles/classmate-1.5.1.jar:module-info.class
  // [error] .../com.sun.istack/istack-commons-runtime/jars/istack-commons-runtime-3.0.7.jar:module-info.class
  // [error] .../com.sun.xml.fastinfoset/FastInfoset/jars/FastInfoset-1.2.15.jar:module-info.class
  // [error] .../javax.xml.bind/jaxb-api/jars/jaxb-api-2.3.1.jar:module-info.class
  // [error] .../org.glassfish.jaxb/jaxb-runtime/jars/jaxb-runtime-2.3.1.jar:module-info.class
  // [error] .../org.glassfish.jaxb/txw2/jars/txw2-2.3.1.jar:module-info.class
  // [error] .../org.jvnet.staxex/stax-ex/jars/stax-ex-1.8.jar:module-info.class
  // As per https://stackoverflow.com/questions/54834125/sbt-assembly-deduplicate-module-info-class , discarding is OK for Java 8
  case "module-info.class" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
