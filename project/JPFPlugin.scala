import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import CommonSettings.autoImport._
import JPFRunnerPlugin.readPluginId

import scala.collection.JavaConverters._

object JPFPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = noTrigger

  override def requires: Plugins = JvmPlugin

  object autoImport {
    lazy val jpfCodeDirs = settingKey[Seq[File]]("JPF runtime code")
    lazy val jpfResourceDirs = settingKey[Seq[File]]("JPF runtime resources")
    lazy val jpfLibraryJars = taskKey[Classpath]("JPF runtime jars")
    lazy val jpfWriteDevJars = taskKey[Unit]("Write JPF library jars for dev")
    lazy val jpfRuntime = taskKey[JPFRuntime]("JPF runtime")
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    scalaVersion := "2.12.4",
    javacOptions ++= Seq("-source", "1.8"),
    jpfCodeDirs := Seq((classDirectory in Compile).value),
    resourceDirectory in Compile := baseDirectory.value / "resources",
    jpfResourceDirs := (resourceDirectories in Compile).value,
    jpfRuntime := JPFRuntime(baseDirectory.value / "plugin-jpf.xml", jpfCodeDirs.value, jpfResourceDirs.value,
      jpfLibraryJars.value.files, baseDirectory.value.getParentFile.getName),
    jpfLibraryJars := Seq(),
    managedClasspath in Compile ++= jpfLibraryJars.value,
    javaSource in Compile := baseDirectory.value / "src",
    javaSource in Test := baseDirectory.value / "test",
    scalaSource in Compile := baseDirectory.value / "scalasrc",
    updateOptions := updateOptions.value.withCachedResolution(true),
    jpfWriteDevJars := {
      val outBase = target.value / "jpflibs"
      IO.delete(outBase)
      val jarFiles = jpfLibraryJars.value.files
      val log = sLog.value
      if (jarFiles.nonEmpty) {
        log.info(s"Writing jpf jars for ${name.value}")
        IO.copy(jarFiles.pair(flat(outBase)))
      }
    },
    langStrings := {
      val doc = Common.saxBuilder.build(jpfRuntime.value.manifest)
      val rootElem = doc.getRootElement
      val pfx = rootElem.getAttributeValue("id")+"."
      rootElem.getChildren("extension").asScala.collect {
        case e if "com.tle.common.i18n" == e.getAttributeValue("plugin-id") && "bundle" == e.getAttributeValue("point-id") =>
          val paramMap = e.getChildren("parameter").asScala.map(e => (e.getAttributeValue("id"), e.getAttributeValue("value"))).toMap
          val fp = paramMap("file")
          val group = paramMap.getOrElse("group", "resource-centre")
          Common.loadLangProperties((resourceDirectory in Compile).value / fp, pfx, group)
      }
    }
  )
}