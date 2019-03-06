package equellatests.pages.search

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.TitledPage

case class HierarchyPage(topic: String, title: String)(val ctx: PageContext) extends TitledPage(title, "hierarchy.do?topic="+topic)

object HierarchyPage {
  def Root : PageContext => HierarchyPage = HierarchyPage("ALL", "Browse")
}