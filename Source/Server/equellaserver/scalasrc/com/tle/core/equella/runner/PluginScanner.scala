/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.core.equella.runner

import java.io.File
import java.net.URL
import java.nio.file.Files

import org.java.plugin.registry.PluginRegistry
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaders
import org.jdom2.output.{Format, XMLOutputter}
import sbt.io.syntax._
import sbt.io.IO

import scala.annotation.tailrec
import scala.collection.JavaConversions._


object PluginScanner {

  def saxBuilder() : SAXBuilder = {
    val sb = new SAXBuilder(XMLReaders.NONVALIDATING)
    sb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    sb.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
    sb
  }

  case class JPFLibrary(id: String, libType: String, path: String, export: Option[String])

  case class ParsedJPF(baseFile: File, manUrl: URL, id: String, internalDeps: Set[(String, Boolean)],
                       externalDeps: Set[(String, Boolean)], adminConsole: Boolean)


  def parseJPF(f: URL, base: File): ParsedJPF = {
    val x = saxBuilder().build(f)
    val root = x.getRootElement
    val pluginId = root.getAttribute("id").getValue
    val (extDeps, deps) = root.getChildren("requires").flatMap(_.getChildren("import")).map { e =>
      (e.getAttributeValue("plugin-id"), e.getAttributeValue("exported", "false") == "true")
    }.partition(_._1.contains(":"))

    val adminConsole = root.getChildren("attributes").flatMap(a => a.getChildren("attribute")).find {
      _.getAttributeValue("id") == "type"
    }.exists(_.getAttributeValue("value") == "admin-console")

    ParsedJPF(base, f, pluginId, deps.toSet, extDeps.toSet, adminConsole)
  }


  def convertAll(parsedMap: Map[String, ParsedJPF], already: Set[String],
                 processed: List[ParsedJPF], pId: Iterable[String]): (Set[String], List[ParsedJPF]) = {

    def convertOne(already: Set[String],
                   processed: List[ParsedJPF], pId: String): (Set[String], List[ParsedJPF]) = {
      if (already.contains(pId)) (already, processed) else {
        parsedMap.get(pId).map {
          case pjpf =>
            val (a, l) = convertAll(parsedMap, already + pId, processed, pjpf.internalDeps.map(_._1))
            (a, pjpf :: l)
        }.getOrElse {
          System.err.println(s"Could not find plugin for id $pId")
          (already, processed)
        }
      }
    }

    pId.foldLeft((already, processed)) {
      case ((a, p), c) => convertOne(a, p, c)
    }
  }

  @tailrec
  def depsWithExports(d: Set[String], parsedMap: Map[String, ParsedJPF], added: Set[String]): Set[String] = {
    val newDeps = d &~ added
    if (newDeps.isEmpty) added else {
      val exportedNew = newDeps.flatMap(s => parsedMap.get(s).map(_.internalDeps.filter(_._2).map(_._1)).getOrElse(Set.empty))
      depsWithExports(exportedNew, parsedMap, added ++ newDeps)
    }
  }


  def scanForPlugins(registry: PluginRegistry, pluginMap: java.util.Map[String, TLEPluginLocation], pluginsLocation: String,
                     devMode: Boolean): Unit = {
    val basePaths = pluginsLocation.split(",").map(file)
    if (devMode) {
      val manifestMap = basePaths.flatMap(f => (f * "*" * "*" / "plugin-jpf.xml").get)
        .map(f => parseJPF(f.toURI.toURL, f.getParentFile)).map(jpf => (jpf.id, jpf)).toMap

      val manDir = IO.temporaryDirectory / "equellaDevManifests"
      IO.delete(manDir)

      convertAll(manifestMap, Set.empty, List.empty, manifestMap.keys)._2.foreach { jpf =>
        val jpfBase = jpf.baseFile
        val classesDir = jpfBase / "target/scala-2.12/classes"
        val codeLibrary = JPFLibrary("code", "code", classesDir.toURI.toString, Some("*"))
        val resourcesDir = Option(jpfBase / "resources").filter(_.isDirectory)
        val resLibrary = resourcesDir.map(d => JPFLibrary("resources", "resources", d.toURI.toString, None))
        val jpfJars = (jpfBase / "target/jpflibs" * "*.jar").get.map(j =>
          JPFLibrary(j.getName, "code", j.toURI.toString, Some("*")))
        val (id, manXml) = modifyManifestLibraries(jpfBase / "plugin-jpf.xml", Seq(codeLibrary) ++ resLibrary ++ jpfJars)
        val manXmlFile = manDir / id / "plugin-jpf.xml"
        val manUrl = manXmlFile.toURI.toURL
        IO.write(manXmlFile, manXml)
        pluginMap.put(id, new TLEPluginLocation(registry.readManifestInfo(manUrl), "plugin-jpf.xml",
          jpfBase.toURI.toURL, manUrl))
      }
    } else {
      val manifestMap = basePaths.flatMap(f => (f ** "*.jar").get)
        .map { jf =>
          val manUrl = new URL("jar", "", jf.toURI + "!/plugin-jpf.xml")
          val jpf = parseJPF(manUrl, jf)
          (jpf.id, jpf)
        }.toMap
      convertAll(manifestMap, Set.empty, List.empty, manifestMap.keys)._2.foreach { jpf =>
        pluginMap.put(jpf.id, new TLEPluginLocation(registry.readManifestInfo(jpf.manUrl), jpf.baseFile.getName,
          new URL("jar", "", jpf.baseFile.toURI + "!/"), jpf.manUrl))
      }
    }
  }

  def modifyManifestLibraries(f: File, jars: Iterable[JPFLibrary]): (String, String) = {
    val x = saxBuilder().build(f)
    val root = x.getRootElement
    val pluginId = root.getAttribute("id").getValue
    Option(root.getChild("requires")).foreach { r =>
      val newchildren = r.getChildren().filterNot(_.getAttribute("plugin-id").getValue.contains(":"))
      if (newchildren.isEmpty) root.removeChild("requires") else r.setContent(newchildren)
    }
    if (jars.nonEmpty) {
      val rt = Option(root.getChild("runtime")).getOrElse {
        val rt = new Element("runtime")
        root.addContent(root.indexOf(root.getChild("requires")) + 1, rt)
        rt
      }
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
