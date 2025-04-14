import Common._
import CommonSettings.autoImport.buildTimestamp
import JPFPlugin.autoImport._
import org.jdom2.Element
import org.jdom2.output.{Format, XMLOutputter}
import sbt.Keys._
import sbt._
import Path.{flatRebase, rebase}

import java.nio.file.Files
import scala.jdk.CollectionConverters._
import scala.util.Try

object JPFRunnerPlugin extends AutoPlugin {

  case class JPFLibrary(id: String, libType: String, path: String, export: Option[String])

  override def trigger: PluginTrigger = noTrigger

  object autoImport {
    lazy val writeJars         = taskKey[Iterable[ManifestWritten]]("Write JPF jars")
    lazy val additionalPlugins = taskKey[Iterable[JPFRuntime]]("Additional JPF plugins to include")

    case class ManifestWritten(file: File, pluginId: String, group: String)

    def runnerTasks(aggregate: ProjectReference) = {
      val scope = ScopeFilter(inAggregates(aggregate, includeRoot = false))
      Seq(
        writeJars := {
          val compileAll  = (Compile / fullClasspath).all(scope).value
          val allRuntimes = jpfRuntime.all(scope).value ++ additionalPlugins.value
          val outBase     = target.value / "jpfjars"
          IO.delete(outBase)
          allRuntimes.map { r =>
            val allCode =
              r.code.flatMap(f => (f ** "*.class").pair(rebase(f, "classes/"), errorIfNone = false))
            val allResources =
              r.resources.flatMap(f =>
                (f ** "*").pair(rebase(f, "resources/"), errorIfNone = false)
              )
            val allJars = r.jars.flatMap(f => flatRebase("lib/").apply(f).map((f, _)))
            val libs =
              allCode.headOption.map(_ => JPFLibrary("code", "code", "classes/", Some("*"))) ++
                allResources.headOption.map(_ =>
                  JPFLibrary("resources", "resources", "resources/", None)
                ) ++
                allJars.map(f => JPFLibrary(f._1.getName, "code", f._2, Some("*")))
            val (id, manifest) = writeJPF(r.manifest, libs)
            val outJar         = outBase / s"$id.jar"
            IO.withTemporaryFile("jpf", "xml") { tf =>
              IO.write(tf, manifest)
              val allFiles = (tf, "plugin-jpf.xml") +: (allCode ++ allResources ++ allJars)

              IO.zip(allFiles, outJar, Option((ThisBuild / buildTimestamp).value))
            }
            ManifestWritten(outJar, id, r.group)
          }
        }
      )
    }
  }

  def isDirectoryEmpty(f: File): Boolean = {
    Try {
      val ds = Files.newDirectoryStream(f.toPath)
      val b  = ds.iterator.hasNext
      ds.close()
      !b
    }.getOrElse(true)
  }

  def readPluginId(f: File): String = {
    val x    = saxBuilder.build(f)
    val root = x.getRootElement
    root.getAttribute("id").getValue
  }

  def writeJPF(f: File, jars: Iterable[JPFLibrary]): (String, String) = {
    val x        = saxBuilder.build(f)
    val root     = x.getRootElement
    val pluginId = root.getAttribute("id").getValue
    Option(root.getChild("requires")).foreach { r =>
      val newchildren =
        r.getChildren().asScala.filterNot(_.getAttribute("plugin-id").getValue.contains(":"))
      if (newchildren.isEmpty) root.removeChild("requires") else r.setContent(newchildren.asJava)
    }
    if (jars.nonEmpty) {

      val rt = Option(root.getChild("runtime")).getOrElse(insertPluginChildElement(root, "runtime"))
      rt.removeContent()
      jars.foreach { f =>
        val lib = new Element("library")
        f.export.foreach(p => lib.addContent(new Element("export").setAttribute("prefix", p)))
        lib.setAttribute("id", f.id)
        lib.setAttribute("type", f.libType)
        lib.setAttribute("path", f.path)
        rt.addContent(lib)
      }
    } else root.removeChild("runtime")
    val pluginXml = new XMLOutputter(Format.getPrettyFormat).outputString(x)
    (pluginId, pluginXml)
  }
}
