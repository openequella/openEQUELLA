import java.util

import scala.collection.JavaConverters._

class OrderedProperties extends java.util.Properties {
  override def keys: util.Enumeration[Object] = {
    super.keys.asScala.toSeq.sortBy(_.toString).toIterator.asJavaEnumeration
  }
}
