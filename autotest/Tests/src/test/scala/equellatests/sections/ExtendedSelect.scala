package equellatests.sections

import com.tle.webtests.pageobject.generic.component.EquellaSelect

import scala.collection.JavaConverters._

object ExtendedSelect {
  implicit class ExtendedSelect(eq: EquellaSelect) {
    def allOptions: Iterable[(String, String)] =
      eq.getOptionElements.asScala.map(o => (o.getAttribute("value"), o.getText))
  }
}
