import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import CommonSettings.autoImport._
import Path.flat
import scala.jdk.CollectionConverters._

object JPFPlugin extends AutoPlugin {
  override def trigger: PluginTrigger = noTrigger

  override def requires: Plugins = JvmPlugin

  object autoImport {
    lazy val jpfCodeDirs     = settingKey[Seq[File]]("JPF runtime code")
    lazy val jpfResourceDirs = settingKey[Seq[File]]("JPF runtime resources")
    lazy val jpfLibraryJars  = taskKey[Classpath]("JPF runtime jars")
    lazy val jpfWriteDevJars = taskKey[Unit]("Write JPF library jars for dev")
    lazy val jpfRuntime      = taskKey[JPFRuntime]("JPF runtime")
  }

  import autoImport._

  override def projectSettings: Seq[Def.Setting[_]] = Seq(
    scalaVersion                  := "2.13.13",
    jpfCodeDirs                   := Seq((Compile / classDirectory).value),
    (Compile / resourceDirectory) := baseDirectory.value / "resources",
    jpfResourceDirs               := (Compile / resourceDirectories).value,
    jpfRuntime := JPFRuntime(
      baseDirectory.value / "plugin-jpf.xml",
      jpfCodeDirs.value,
      jpfResourceDirs.value,
      jpfLibraryJars.value.files,
      baseDirectory.value.getParentFile.getName
    ),
    jpfLibraryJars := Seq(),
    (Compile / managedClasspath) ++= jpfLibraryJars.value,
    (Compile / javaSource)     := baseDirectory.value / "src",
    (Test / javaSource)        := baseDirectory.value / "test/java",
    (Test / scalaSource)       := baseDirectory.value / "test/scala",
    (Test / resourceDirectory) := baseDirectory.value / "test/resources",
    (Compile / scalaSource)    := baseDirectory.value / "scalasrc",
    updateOptions              := updateOptions.value.withCachedResolution(true),
    jpfWriteDevJars := {
      val outBase = target.value / "jpflibs"
      IO.delete(outBase)
      val jarFiles = jpfLibraryJars.value.files
      val log      = sLog.value
      if (jarFiles.nonEmpty) {
        log.info(s"Writing jpf jars for ${name.value}")
        IO.copy(jarFiles.pair(flat(outBase)))
      }
    },
    langStrings := {
      val doc      = Common.saxBuilder.build(jpfRuntime.value.manifest)
      val rootElem = doc.getRootElement
      val pfx      = rootElem.getAttributeValue("id") + "."
      rootElem.getChildren("extension").asScala.collect {
        case e
            if "com.tle.common.i18n" == e.getAttributeValue("plugin-id") && "bundle" == e
              .getAttributeValue("point-id") =>
          val paramMap = e
            .getChildren("parameter")
            .asScala
            .map(e => (e.getAttributeValue("id"), e.getAttributeValue("value")))
            .toMap
          val fp    = paramMap("file")
          val group = paramMap.getOrElse("group", "resource-centre")
          Common.loadLangProperties((Compile / resourceDirectory).value / fp, pfx, group)
      }
    }
  )
}
