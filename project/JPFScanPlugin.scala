import Common._
import JPFPlugin.autoImport._
import sbt.Keys._
import sbt._

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._

object JPFScanPlugin extends AutoPlugin {
  val serverRef = LocalProject("equellaserver")
  val adminRef  = LocalProject("adminTool")

  case class ParsedJPF(
      baseDir: File,
      group: String,
      id: String,
      internalDeps: Set[(String, Boolean)],
      externalDeps: Set[(String, Boolean)],
      adminConsole: Boolean
  ) {
    def isExtensionOnly: Boolean = group == "Extensions"
  }

  def parseJPF(f: File): ParsedJPF = {
    val x        = saxBuilder.build(f)
    val root     = x.getRootElement
    val pluginId = root.getAttribute("id").getValue
    val (extDeps, deps) = root
      .getChildren("requires")
      .asScala
      .flatMap(_.getChildren("import").asScala)
      .map { e =>
        (e.getAttributeValue("plugin-id"), e.getAttributeValue("exported", "false") == "true")
      }
      .partition(_._1.contains(":"))

    val adminConsole = root
      .getChildren("attributes")
      .asScala
      .flatMap(a => a.getChildren("attribute").asScala)
      .find {
        _.getAttributeValue("id") == "type"
      }
      .exists(_.getAttributeValue("value") == "admin-console")

    ParsedJPF(
      f.getParentFile,
      f.getParentFile.getParentFile.getName,
      pluginId,
      deps.toSet,
      extDeps.toSet,
      adminConsole
    )
  }

  def toLocalProject(pluginId: String) = LocalProject(toSbtPrj(pluginId))

  def parentForPlugin(pjpf: ParsedJPF): LocalProject =
    if (pjpf.adminConsole) adminRef else serverRef

  def convertAllPlugins(parsedMap: Map[String, ParsedJPF], pId: Iterable[String]): List[Project] = {

    def classpathDep(pluginId: String): Option[ClasspathDep[ProjectReference]] = {
      parsedMap.get(pluginId).filterNot(_.isExtensionOnly) map { jpf =>
        ClasspathDependency(toLocalProject(pluginId), None)
      }
    }

    def convertAll(
        already: Set[String],
        processed: List[Project],
        pId: Iterable[String]
    ): (Set[String], List[Project]) = {
      pId.foldLeft((already, processed)) { case ((a, p), c) =>
        convertOne(a, p, c)
      }
    }

    @tailrec
    def depsWithExports(d: Set[String], added: Set[String]): Set[String] = {
      val newDeps = d &~ added
      if (newDeps.isEmpty) added
      else {
        val exportedNew = newDeps.flatMap(s =>
          parsedMap.get(s).map(_.internalDeps.filter(_._2).map(_._1)).getOrElse(Set.empty)
        )
        depsWithExports(exportedNew, added ++ newDeps)
      }
    }

    def convertOne(
        already: Set[String],
        processed: List[Project],
        pId: String
    ): (Set[String], List[Project]) = {
      if (already.contains(pId)) (already, processed)
      else {
        parsedMap
          .get(pId)
          .map {
            case pjpf: ParsedJPF if pjpf.isExtensionOnly => (already + pId, processed)
            case pjpf @ ParsedJPF(baseDir, group, _, internalDeps, _, _) =>
              val deps    = internalDeps.map(_._1)
              val (a, l)  = convertAll(already + pId, processed, deps)
              val prjDeps = deps.toSeq.flatMap(classpathDep)
              val prj = Project(toSbtPrj(pId), baseDir)
                .dependsOn(prjDeps: _*)
                .settings(
                  (Compile / managedClasspath) ++= (parentForPlugin(
                    pjpf
                  ) / Compile / managedClasspath).value,
                  (Compile / managedClasspath) ++= {
                    jpfLibraryJars
                      .all(
                        ScopeFilter(
                          inProjects(depsWithExports(deps, Set.empty).map(toLocalProject).toSeq: _*)
                        )
                      )
                      .value
                      .flatten
                  },
                  (Test / managedClasspath) ++= (Compile / managedClasspath).value
                )
                .enablePlugins(JPFPlugin)
              (a, prj :: l)
          }
          .getOrElse {
            System.err.println(s"Could not find plugin for id $pId")
            (already, processed)
          }
      }
    }
    convertAll(Set.empty, Nil, pId)._2
  }

  lazy val minimumPlugins = Seq(
    "com.tle.platform.swing",
    "com.tle.platform.equella",
    "com.tle.webstart.admin",
    "com.tle.platform.common",
    "com.tle.platform.equella",
    "com.tle.web.adminconsole"
  )

  override def trigger = noTrigger

  override def derivedProjects(proj: ProjectDefinition[_]): Seq[Project] = {
    val baseDir = proj.base
    val allManifests = (baseDir / "Source/Plugins" * "*" * "*" / "plugin-jpf.xml").get ++
      (baseDir / "Platform/Plugins" * "*" / "plugin-jpf.xml").get ++
      (baseDir / "Interface/Plugins" * "*" / "plugin-jpf.xml").get ++
      // Also include UpgradeInstallation and UpgradeManager
      (baseDir / "Source/Tools" * "Upgrade*" / "plugin-jpf.xml").get
    val manifestMap = allManifests.map(parseJPF).map(p => (p.id, p)).toMap

//    val adminPlugins = manifestMap.values.filter(_.adminConsole).map(_.id).toSet
    val pluginList = (if (buildConfig.hasPath("plugin.whitelist"))
                        buildConfig.getStringList("plugin.whitelist").asScala.toSet
                      else manifestMap.keySet) ++ minimumPlugins
    val projects = convertAllPlugins(manifestMap, pluginList)
    val allPlugins = Project("allPlugins", baseDir / "Source/Plugins")
      .aggregate(projects.map(Project.projectToRef): _*)
    allPlugins +: projects
  }

}
