import java.util
import scala.jdk.CollectionConverters._

class OrderedProperties extends java.util.Properties {
  override def keys: util.Enumeration[Object] = {
    super.keys.asScala.toSeq.sortBy(_.toString).toIterator.asJavaEnumeration
  }
}
