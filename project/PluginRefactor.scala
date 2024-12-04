import java.util.Properties

import org.jdom2.filter.{AbstractFilter, Filters}
import org.jdom2.output.{Format, XMLOutputter}
import org.jdom2.{DocType, Document, Element}
import sbt.Keys.Classpath
import sbt._
import Path.rebase
import sbt.io.Using

import scala.annotation.tailrec
import scala.jdk.CollectionConverters._
import scala.collection.mutable

class ElementFilter(f: Element => Boolean) extends AbstractFilter[Element] {
  override def filter(content: scala.Any): Element = content match {
    case e: Element if f(e) => e
    case _                  => null
  }
}

case class PluginDeets(bd: File, libs: Classpath) {
  lazy val rootDoc  = Common.saxBuilder.build(bd / "plugin-jpf.xml")
  lazy val rootElem = rootDoc.getRootElement
  lazy val imports = Option(rootElem.getChild("requires")).toSeq
    .flatMap(_.getChildren("import").asScala)
    .filter(!_.getAttributeValue("plugin-id").contains(":"))
  lazy val importIds = imports.map(_.getAttributeValue("plugin-id"))
  lazy val pId       = rootElem.getAttributeValue("id")
}

case class LangString(group: String, key: String, pluginId: String, value: String)

object PluginRefactor {

  def getPluginId(e: Element): String = e.getAttributeValue("plugin-id")

  val mutualExclusions =
    Map(
      "com.equella.base"       -> Set("com.tle.core.guice"),
      "com.equella.serverbase" -> Set("com.tle.web.sections")
    ).withDefaultValue(Set.empty)

  val mutualKeys = mutualExclusions.keySet

  val platformPlugins =
    Set("com.tle.platform.common", "com.tle.platform.swing", "com.tle.platform.equella")

  val keepPlugins = Set(
    "com.tle.webstart.admin",
    "com.tle.core.application",
    "com.tle.core.security",
    "com.tle.web.sections",
    "com.tle.web.sections.equella",
    "com.tle.core.guice",
    "com.tle.web.adminconsole",
    "com.tle.core.remoterepo.srw"
  ) ++ platformPlugins

  sealed trait CycleCheckResult

  case class CycleFound(involved: List[String]) extends CycleCheckResult

  case class Success(plugins: Set[String]) extends CycleCheckResult

  def cycleChecker(allImports: Iterable[PluginDeets]): Set[String] => CycleCheckResult = {
    val deetMap = allImports.map(p => (p.pId, p)).toMap

    def wouldCauseCycle(_toCheck: Set[String]): CycleCheckResult = {

      val randomCheck = scala.util.Random.shuffle(_toCheck.toList)
      val (actualCheck, toCheck, withExclusions) = randomCheck
        .find(mutualKeys)
        .map { firstMutual =>
          val withOutFirst = mutualKeys - firstMutual
          (
            randomCheck.filterNot(withOutFirst),
            _toCheck -- withOutFirst,
            _toCheck ++ mutualExclusions(firstMutual)
          )
        }
        .getOrElse(randomCheck, _toCheck, _toCheck)

      def checkIter(
          parents: List[String],
          ids: Iterator[String],
          state: Set[String]
      ): Either[Set[String], List[String]] = {

        @tailrec
        def tailRec(checked: Set[String]): Either[Set[String], List[String]] = {
          if (!ids.hasNext) Left(checked)
          else {
            val pId = ids.next()
            if (checked(pId)) tailRec(state)
            else {
              if (withExclusions(pId)) {
                Right(parents)
              } else {
                val p = deetMap(pId)
                checkIter(pId :: parents, p.importIds.iterator, checked + pId) match {
                  case Left(c) => tailRec(c)
                  case r       => r
                }
              }
            }
          }
        }

        tailRec(state)
      }

      @tailrec
      def topLevel(mergeList: List[String], checked: Set[String]): Option[List[String]] =
        mergeList match {
          case Nil => None
          case pId :: mt =>
            checkIter(
              List(pId),
              (deetMap(pId).importIds.toSet -- toCheck).toIterator,
              checked
            ) match {
              case Left(nextChecked) => topLevel(mt, nextChecked)
              case Right(failed)     => Some(failed)
            }
        }

      actualCheck match {
        case singlePlugin :: Nil => CycleFound(List(singlePlugin))
        case _ =>
          topLevel(actualCheck, Set.empty) match {
            case None      => Success(actualCheck.toSet)
            case Some(bad) => CycleFound(bad)
          }
      }
    }

    wouldCauseCycle
  }

  def choosePlugins(allImports: Seq[PluginDeets], adminPlugins: Boolean): Iterable[String] = {

    val initialPlugins = allImports.filter { p =>
      val r = p.rootElem
      val adminConsole = r
        .getChildren("attributes")
        .asScala
        .flatMap(_.getChildren("attribute").asScala)
        .find {
          _.getAttributeValue("id") == "type"
        }
        .exists(_.getAttributeValue("value") == "admin-console")

      p.libs.isEmpty && (adminConsole == adminPlugins) &&
      r.getChild("requires") != null && !keepPlugins(p.pId)
    }

    val onlyAllowed     = initialPlugins.map(_.pId)
    val wouldCauseCycle = cycleChecker(allImports)

    @tailrec
    def findSubset(
        size: Int,
        baseSet: Set[String],
        soFar: Int,
        stats: Map[String, Int]
    ): Either[String, Set[String]] = {
      val allSubsets = baseSet.subsets(size)

      println(size)

      @tailrec
      def checkSubsets(
          iter: Iterator[Set[String]],
          soFar: Int,
          stats: Map[String, Int]
      ): Either[Either[String, (Int, Map[String, Int])], Set[String]] = {
        if (soFar > 100000) {
          println(stats)
          Left(Left(stats.toSeq.maxBy(_._2)._1))
        } else {
          if (!iter.hasNext) Left(Right(soFar, stats))
          else {
            val nextSet = iter.next()
            wouldCauseCycle(nextSet) match {
              case Success(correct) => Right(correct)
              case CycleFound(badCycle) =>
                val bad = badCycle.last
                checkSubsets(iter, soFar + 1, stats.updated(bad, stats.getOrElse(bad, 0) + 1))
            }
          }
        }
      }

      checkSubsets(allSubsets, soFar, stats) match {
        case Left(Left(failed)) =>
          findSubset(Math.min(baseSet.size - 1, 10), baseSet - failed, 0, Map.empty)
        case Left(Right((sf, s))) => findSubset(size - 1, baseSet, sf, s)
        case Right(success)       => Right(success)
      }
    }

    findSubset(Math.min(onlyAllowed.size - 1, 10), onlyAllowed.toSet, 0, Map.empty) match {
      case Right(ok) => ok
      case Left(f)   => Seq.empty
    }
  }

  def findPluginsToMerge(
      allBaseDirs: Seq[(File, Classpath)],
      adminConsole: Boolean
  ): Iterable[String] = {
    val allPlugins = allBaseDirs.map(t => PluginDeets(t._1, t._2))
    choosePlugins(allPlugins, adminConsole)
  }

  def mergePlugins(
      allBaseDirs: Seq[(File, Classpath)],
      baseParentDir: File,
      pluginId: String,
      toMerge: Seq[String],
      adminConsole: Boolean
  ): Unit = {
    val allPlugins = allBaseDirs.map(t => PluginDeets(t._1, t._2))

    cycleChecker(allPlugins)(toMerge.toSet) match {
      case CycleFound(cycle) => println(s"Sorry that would cause a cycle: ${cycle}")
      case Success(_) =>
        val baseDir = baseParentDir / "Temporary" / "merged_plugin"
        println("Merging: " + toMerge.sorted.mkString(","))
        IO.delete(baseDir)

        val baseRes = baseDir / "resources"

        val allowedIds = toMerge.toSet
        val imp_exts = allPlugins.collect {
          case p if allowedIds(p.pId) =>
            val exts =
              p.rootElem.getChildren("extension").asScala.toList.map(e => (p.bd, p.pId, e.detach()))
            (p.imports, exts, p.bd, p.pId)
        }

        val imports = imp_exts.map(_._1).reduce(_ ++ _)
        val exts    = imp_exts.map(_._2).reduce(_ ++ _)

        def mkImport(impId: String) = {
          val impElem = new Element("import")
          impElem.setAttribute("plugin-id", impId)
          impElem
        }

        def createNewPluginDoc(newId: String): (Element, Document) = {
          val plugElem = new Element("plugin")
          plugElem.setAttribute("id", newId)
          plugElem.setAttribute("version", "1")
          val doc = new Document()
          doc.setDocType(
            new DocType(
              "plugin",
              "-//JPF//Java Plug-in Manifest 1.0",
              "http://jpf.sourceforge.net/plugin_1_0.dtd"
            )
          )
          doc.setRootElement(plugElem)
          (plugElem, doc)
        }

        val (plugElem, doc) = createNewPluginDoc(pluginId)

        if (adminConsole) {
          val attrsElem = new Element("attributes")
          val attrElem  = new Element("attribute")
          attrElem.setAttribute("id", "type")
          attrElem.setAttribute("value", "admin-console")
          attrsElem.addContent(attrElem)
          plugElem.addContent(attrsElem)
        }

        val guiceExt = new Element("extension")
        guiceExt.setAttribute("plugin-id", "com.tle.core.guice")
        guiceExt.setAttribute("point-id", "module")
        guiceExt.setAttribute("id", "guiceModules")

        val guiceModules = exts.flatMap { case (bd, pId, e) =>
          (getPluginId(e), e.getAttributeValue("point-id")) match {
            case ("com.tle.core.guice", "module") =>
              e.getChildren("parameter").asScala.map(_.getAttributeValue("value"))
            case _ => Seq.empty
          }
        }
        val langStrings = exts.flatMap { case (bd, pId, e) =>
          (getPluginId(e), e.getAttributeValue("point-id")) match {
            case ("com.tle.common.i18n", "bundle") =>
              val params = e.getChildren("parameter").asScala
              params
                .find(_.getAttributeValue("id") == "file")
                .map { fileElem =>
                  val filename = fileElem.getAttributeValue("value")
                  val group = params
                    .find(_.getAttributeValue("id") == "group")
                    .map(_.getAttributeValue("value"))
                    .getOrElse("resource-centre")
                  val propFile  = bd / "resources" / filename
                  val langProps = new Properties()
                  Using.fileInputStream(propFile) { inp =>
                    if (IO.split(filename)._2 == "xml") langProps.loadFromXML(inp)
                    else langProps.load(inp)
                  }
                  langProps.entrySet().asScala.toSeq.map { e =>
                    LangString(group, e.getKey.toString, pId, e.getValue.toString)
                  }
                }
                .getOrElse(Seq.empty)
            case _ => Seq.empty
          }
        }

        val bundles = langStrings.groupBy(_.group).map { case (g, strings) =>
          val props = new OrderedProperties
          strings.foreach { ls =>
            props.put(ls.key, ls.value)
          }
          val fname = s"lang/i18n-$g.properties"
          IO.write(props, g, baseRes / fname)
          val bundleExt = new Element("extension")
          bundleExt.setAttribute("plugin-id", "com.tle.common.i18n")
          bundleExt.setAttribute("point-id", "bundle")
          bundleExt.setAttribute("id", s"strings_$g")
          val groupE = new Element("parameter")
          groupE.setAttribute("id", "group")
          groupE.setAttribute("value", g)
          val fileE = new Element("parameter")
          fileE.setAttribute("id", "file")
          fileE.setAttribute("value", fname)
          bundleExt.addContent(groupE)
          bundleExt.addContent(fileE)
          bundleExt
        }

        def reprefix(pId: String, e: Element, f: String => Boolean): Element = {
          e.getChildren("parameter")
            .asScala
            .filter(p =>
              Option(p.getAttributeValue("id")).exists(f) &&
                p.getAttributeValue("value").startsWith(pId)
            )
            .foreach { p =>
              p.setAttribute("value", pluginId + p.getAttributeValue("value").substring(pId.length))
            }
          e
        }

        def keyParameters(extPlugin: String, ext: String): (Set[String], Set[String]) =
          (extPlugin, ext) match {
            case (_, "portletRenderer" | "resourceViewer" | "connectorType" | "portletType") =>
              (Set("nameKey", "linkKey", "descriptionKey"), Set())
            case ("com.tle.mycontent", "contentHandler") => (Set("nameKey"), Set.empty)
            case ("com.tle.admin.tools", "tool")         => (Set("name"), Set("class"))
            case ("com.tle.admin.controls", "control") =>
              (Set("name"), Set("wrappedClass", "editorClass", "modelClass"))
            case ("com.tle.admin.controls.universal", "editor") =>
              (Set("nameKey"), Set("configPanel"))
            case ("com.tle.admin.fedsearch.tool", "configUI")      => (Set.empty, Set("class"))
            case ("com.tle.admin.usermanagement.tool", "configUI") => (Set("name"), Set("class"))
            case ("com.tle.common.dynacollection", "usages")       => (Set("nameKey"), Set.empty)
            case ("com.tle.common.wizard.controls.resource", "relationTypes") =>
              (Set("nameKey"), Set.empty)
            case ("com.tle.admin.collection.tool", "extra") => (Set("name"), Set("configPanel"))
            case ("com.tle.admin.collection.tool", "summaryDisplay") =>
              (Set("nameKey", "defaultNameKey"), Set("class"))
            case ("com.tle.admin.controls.universal", "universalvalidator") =>
              (Set.empty, Set("id", "class"))
            case ("com.tle.admin.search", "searchSetVirtualiserConfigs") =>
              (Set("nameKey"), Set("configPanel"))
            case ("com.tle.admin.taxonomy.tool", "dataSourceChoice") =>
              (Set("nameKey"), Set("configPanel"))
            case ("com.tle.admin.taxonomy.tool", "displayType") => (Set("nameKey"), Set())
            case ("com.tle.admin.taxonomy.tool", "predefinedTermDataKey") =>
              (Set("name", "description"), Set())
            case ("com.tle.core.migration", "migration") => (Set(), Set("id", "obsoletedby"))
            case ("com.tle.core.institution.convert", "xmlmigration") =>
              (Set(), Set("id", "obsoletedby"))
            case ("com.tle.core.scheduler", "scheduledTask")           => (Set(), Set("id"))
            case ("com.tle.web.settings", "settingsGroupingExtension") => (Set("nameKey"), Set())
            case _ => (Set.empty, Set("class", "listenerClass"))
          }

        val afterExt = exts.flatMap { case (bd, pId, e) =>
          (getPluginId(e), e.getAttributeValue("point-id")) match {
            case ("com.tle.core.guice", "module")  => Seq.empty
            case ("com.tle.common.i18n", "bundle") => Seq.empty
            case (extPlugin, ext) if keyParameters(extPlugin, ext)._1.nonEmpty =>
              Seq(reprefix(pId, e.clone, keyParameters(extPlugin, ext)._1))
            case _ => Seq(e)
          }
        }

        val extIds         = afterExt.map(getPluginId)
        val allowedWithExt = allowedIds -- extIds
        val extImports     = extIds.map(mkImport)

        val sortedImports = (imports ++ extImports)
          .map(e => (getPluginId(e), e))
          .filterNot(v => allowedWithExt(v._1))
          .toMap
          .values
          .toSeq
          .sortBy(getPluginId)
        val req = new Element("requires")
        req.addContent(sortedImports.map(_.clone).asJava)
        plugElem.addContent(req)

        if (guiceModules.nonEmpty) {
          guiceModules.distinct.sorted.foreach { m =>
            val e = new Element("parameter")
            e.setAttribute("id", "class")
            e.setAttribute("value", m)
            guiceExt.addContent(e)
          }
          plugElem.addContent(guiceExt)
        }

        def containsId(el: List[Element])(e: Element) =
          el.exists(e2 => e2.getAttributeValue("id") == e.getAttributeValue("id"))

        plugElem.addContent(
          Uniqueify
            .uniqueSeq[Element](
              (i, e) => e.clone().setAttribute("id", e.getAttributeValue("id") + "_" + i),
              containsId
            )(afterExt ++ bundles)
            .asJava
        )

        val pathsTo = imp_exts.flatMap { case (_, _, bd, pId) =>
          val allRes =
            bd.descendantsExcept("*", "plugin-jpf.xml" | "target") --- (bd / "resources/lang" * "*")
          val relative = allRes.pair(rebase(bd, "")).collect {
            case (f, p) if f.isFile => (p, pId, f.length())
          }
          val mapped = allRes.pair(rebase(bd, baseDir))
          IO.copy(mapped, overwrite = false, true, true)
          relative
        }

        var dupeResource = false
        pathsTo.groupBy(_._1).filter(t => t._2.map(_._3).distinct.size > 1).foreach {
          case (p, pids) =>
            println(s"DUPE FILE:$p=${pids.map(t => t._2 -> t._3)}")
            dupeResource = true
        }

        var dupeKey = false
        langStrings
          .groupBy { case LangString(g, k, _, _) => (g, k) }
          .filter(_._2.map(_.value).distinct.size > 1)
          .toSeq
          .sortBy(_._1)
          .foreach { case (k, dupes) =>
            println(s"DUPE KEY: $k=${dupes.map(_.pluginId).mkString(",")}")
            dupeKey = true
          }

        exts
          .flatMap { case (bd, pId, e) =>
            e.getChildren("parameter").asScala.collect {
              case p
                  if Option(p.getAttributeValue("value")).exists(_.startsWith(pId))
                    && Option(p.getAttributeValue("id")).exists { paramId =>
                      val (keys, nonKeys) =
                        keyParameters(getPluginId(e), e.getAttributeValue("point-id"))
                      !(keys(paramId) || nonKeys(paramId))
                    } =>
                s"SUSPICIOUS:${getPluginId(e)} ${e.getAttributeValue("point-id")} ${p
                    .getAttributeValue("id")} ${p.getAttributeValue("value")}"
            }
          }
          .foreach(println)

        val hasOldStyle = new ElementFilter({ e =>
          getPluginId(e).contains(":")
        })
        val canCommit    = pluginId != "<ID>" && !dupeKey && !dupeResource
        val manifestName = if (canCommit) "plugin-jpf.xml" else "plugin-jpf2.xml"

        def writeManifest(fname: File, outDoc: Document): Unit = {
          val pluginJpf = new XMLOutputter(Format.getPrettyFormat).outputString(outDoc)
          IO.write(fname, pluginJpf)
        }

        allPlugins.foreach {
          case p if allowedIds(p.pId) => {
            val extensions = p.rootElem.getChildren("extension-point").asScala
            if (extensions.nonEmpty) {
              val (extPlugin, extDoc) = createNewPluginDoc(p.pId)
              extensions.foreach(e => extPlugin.addContent(e.clone()))
              val pareDir = baseParentDir / "Extensions" / p.pId
              IO.delete(pareDir)
              writeManifest(pareDir / manifestName, extDoc)
            }
            if (canCommit) {
              IO.delete(p.bd)
            }
          }
          case p => {
            Option(p.rootElem.getChild("requires")).foreach { r =>
              val usedMerged = r.getChildren().asScala.exists(e => allowedIds(getPluginId(e)))

              val needsReplacing = new ElementFilter({ e =>
                val thisId = getPluginId(e)
                allowedIds(thisId) &&
                !(usedMerged && thisId == pluginId) &&
                !p.rootElem.getChildren("extension").asScala.exists(getPluginId(_) == thisId)
              })
              r.removeContent[Element](needsReplacing)
              r.removeContent[Element](hasOldStyle)
              if (usedMerged && !r.getChildren().asScala.exists(getPluginId(_) == pluginId)) {
                r.addContent(mkImport(pluginId))
              }
            }
            if (canCommit) {
              writeManifest(p.bd / "plugin-jpf.xml", p.rootDoc)
            }
          }
        }
        writeManifest(baseDir / manifestName, doc)
        if (canCommit) {
          baseDir.renameTo(baseParentDir / "Core" / pluginId)
        }
    }
  }
}
