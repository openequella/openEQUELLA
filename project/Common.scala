import com.typesafe.config.{Config, ConfigFactory}
import org.jdom2.input.SAXBuilder
import org.jdom2.input.sax.XMLReaders
import sbt._

object Common {
  def toSbtPrj(s: String) = s.replace('.', '_').replace('-', '_')

  val saxBuilder = {
    val sb = new SAXBuilder(XMLReaders.NONVALIDATING)
    sb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    sb.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
    sb
  }

  private val defaultConfig = ConfigFactory.parseFile(file("project/build-defaults.conf"))
  val buildConfig = ConfigFactory.parseFile(file("project/build-dev.conf")).withFallback(defaultConfig)
}