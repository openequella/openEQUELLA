import java.util.Properties

import com.typesafe.config.{Config, ConfigFactory}
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaders
import sbt._

import scala.collection.JavaConverters._
import scala.sys.process.Process

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

  def runYarn(script: String, dir: File): Unit = {
    val os = sys.props("os.name").toLowerCase
    val precmd = os match {
      case x if x contains "windows" => Seq("cmd", "/C")
      case _ => Seq.empty
    }
    if (Process(precmd ++ Seq("yarn", "--mutex", "network", "run", script), dir).! > 0)
      sys.error(s"Running yarn script '$script' in dir ${dir.absolutePath} failed")
  }
}