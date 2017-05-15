import java.nio.file.Files

import sbt.Keys._
import sbt.{Def, _}
import JPFPlugin.autoImport._

import scala.util.Try
import Common._
import org.jdom2.Element
import org.jdom2.output.{Format, XMLOutputter}

import scala.collection.JavaConversions._

object JPFRunnerPlugin extends AutoPlugin {

  case class JPFLibrary(id: String, libType: String, path: String, export: Boolean)

  override def trigger: PluginTrigger = noTrigger

  object autoImport {
    lazy val writeDevManifests = taskKey[ManifestsWritten]("Write dev manifests")
    lazy val writeJars = taskKey[ManifestsWritten]("Write JPF jars")

    case class ManifestsWritten(plugins: Iterable[(File, String)])

    def runnerTasks(aggregate: ProjectReference) = {
      val scope = ScopeFilter(inAggregates(aggregate, includeRoot = false))
      Seq(
        writeDevManifests := {
          val allRuntimes = jpfRuntime.all(scope).value
          val manifests = target.value / "manifests"

          IO.delete(manifests)

          ManifestsWritten(
            allRuntimes.map { r =>
              val (pid, plugXml) = writeJPF(r.manifest,
                r.code.filterNot(isDirectoryEmpty).map(f => JPFLibrary(f.getName, "code", f.getAbsolutePath + "/", true)) ++
                  r.jars.map(f => JPFLibrary(f.getName, "code", f.getAbsolutePath, true)) ++
                  r.resources.filterNot(isDirectoryEmpty).map(f => JPFLibrary(f.getName, "resources", f.getAbsolutePath + "/", false)))
              val outMan = manifests / pid / "plugin-jpf.xml"
              IO.write(outMan, plugXml)
              (outMan, pid)
            }
          )
        },
        writeJars := {
          val compileAll = (fullClasspath in Compile).all(scope).value
          val allRuntimes = jpfRuntime.all(scope).value
          val outBase = target.value / "jpfjars"
          IO.delete(outBase)
          ManifestsWritten {
            allRuntimes.map { r =>
              val allCode = r.code.flatMap(f => (f ***).pair(rebase(f, "classes/"), false))
              val allResources = r.resources.flatMap(f => (f ***).pair(rebase(f, "resources/"), false))
              val allJars = r.jars.flatMap(f => flatRebase("lib/").apply(f).map((f, _)))
              val libs = allCode.headOption.map(_ => JPFLibrary("code", "code", "classes/", true)) ++
                          allResources.headOption.map(_ => JPFLibrary("resources", "resources", "resources/", false)) ++
                          allJars.map(f => JPFLibrary(f._1.getName, "code", f._2, true))
              val (id, manifest) = writeJPF(r.manifest, libs)
              val outJar = outBase / s"${id}.jar"
              IO.withTemporaryFile("jpf", "xml") { tf =>
                IO.write(tf, manifest)
                val allFiles = (tf, "plugin-jpf.xml") +: (allCode ++ allResources ++ allJars)
                IO.zip(allFiles, outJar)
              }
              (outJar, id)
            }
          }
        }
      )
    }
  }

  import autoImport._

  def isDirectoryEmpty(f: File): Boolean = {
    Try {
      val ds = Files.newDirectoryStream(f.toPath)
      val b = ds.iterator.hasNext
      ds.close()
      !b
    }.getOrElse(true)
  }

  def writeJPF(f: File, jars: Iterable[JPFLibrary]): (String, String) = {
    val x = saxBuilder.build(f)
    val root = x.getRootElement
    val pluginId = root.getAttribute("id").getValue
    Option(root.getChild("requires")).foreach { r =>
      val newchildren = r.getChildren().filterNot(_.getAttribute("plugin-id").getValue.contains(":"))
      if (newchildren.isEmpty) root.removeChild("requires") else r.setContent(newchildren)
    }
    if (jars.nonEmpty) {
      val rt = Option(root.getChild("runtime")).getOrElse {
        val rt = new Element("runtime")
        root.addContent(0, rt)
        rt
      }
      rt.removeContent()
      jars.foreach { f =>
        val lib = new Element("library")
        if (f.export) lib.addContent(new Element("export").setAttribute("prefix", "*"))
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