package equellatests.pages

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.TitledPage

case class RemoteReposPage(ctx: PageContext)
    extends TitledPage("Remote repositories", "access/remoterepo.do")
