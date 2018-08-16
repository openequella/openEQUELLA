package com.tle.web.sections

import io.lemonlabs.uri.QueryString
import scala.collection.JavaConverters._

object QueryParams {


  def paramString(map: java.util.Map[String, Array[String]]): String =
    QueryString(map.asScala.flatMap {
      case (n, vals) => vals.map(v => n -> Some(v)
)    }.toVector).toString() match {
      case "" => ""
      case o => o.substring(1)
    }
}
