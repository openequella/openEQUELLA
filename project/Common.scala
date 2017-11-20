import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import org.jdom2.Element
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaders
import sbt._

import scala.annotation.tailrec
import scala.collection.JavaConverters._

object Common {

  def toSbtPrj(s: String) = s.replace('.', '_').replace('-', '_')

  def saxBuilder = {
    val sb = new SAXBuilder(XMLReaders.NONVALIDATING)
    sb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    sb.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
    sb
  }

  private val defaultConfig = ConfigFactory.parseFile(file("project/build-defaults.conf"))
  private val configFile = sys.props.get("config.file").getOrElse("build.conf")
  val buildConfig = ConfigFactory.load(ConfigFactory.parseFile(file(configFile))).withFallback(defaultConfig)

  def loadLangProperties(f: File, prefix: String, group: String) : LangStrings = {
    val p = new Properties()
    Using.fileInputStream(f) { finp =>
      val xml = f.getName.endsWith(".xml")
      if (xml) p.loadFromXML(finp) else p.load(finp)
      val s = p.asScala.map {
        case (k, v) if k.startsWith("/") => k.substring(1) -> v
        case (k, v) => prefix+k -> v
      }
      LangStrings(group, xml, s.toMap)
    }
  }

  val pluginElemOrder = Seq("doc", "attributes", "requires", "runtime", "extension-point", "extension")

  def insertPluginChildElement(pluginElem: Element, childName: String): Element = {

    val orderNum = pluginElemOrder.indexOf(childName)

    @tailrec
    def insertionPoint(elems: List[Element], insertIndex: Int): Int = elems match {
      case elem :: tail if pluginElemOrder.indexOf(elem.getName) < orderNum => insertionPoint(tail, pluginElem.indexOf(elem)+1)
      case _ => insertIndex
    }
    val rt = new Element(childName)
    pluginElem.addContent(insertionPoint(pluginElem.getChildren.asScala.toList, 0), rt)
    rt
  }
}