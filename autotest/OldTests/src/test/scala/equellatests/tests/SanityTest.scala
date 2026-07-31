package equellatests.tests

import com.tle.webtests.framework.PageContext
import equellatests.browserpage.LoadablePage
import equellatests.domain.{Fairness, TestLogon}
import equellatests.instgen.fiveo._
import equellatests.pages.copyright.ManageActivationsPage
import equellatests.pages.moderate.{ManageTasksPage, TaskListPage}
import equellatests.pages.{HomePage, RemoteReposPage, ReportsPage}
import equellatests.pages.search._
import equellatests.pages.wizard.ContributePage
import equellatests.{PropertyBasedBrowserTest, SimpleTestCase, StatefulTest}
import org.scalacheck.Prop
import testng.annotation.NewUIOnly

@NewUIOnly
class SanityTest extends PropertyBasedBrowserTest with StatefulTest with SimpleTestCase {

//    /access/browseby.do
//    /cloud/viewitem.do
//    /institutions.do
//    /viewitem/viewitem.do

  object Pages extends Enumeration {
    val Home, Contribute, ManageTasks, ManageResources, TaskList, Notifications, Reports,
        MyResources, Searching, Hierarchy, Harvester, RemoteRepos, Favourites, ManageActivations =
      Value
  }
  import Pages._
  case class SanityState(completedPages: Pages.ValueSet = Pages.ValueSet.empty)

  override type Command = Pages.Value
  override type State   = SanityState

  override def initialState: SanityState = SanityState()

  override def runCommand(c: Command, s: SanityState): SanityState =
    s.copy(s.completedPages + c)

  override def runCommandInBrowser(c: Command, s: SanityState, b: Browser): Prop = b.verify {
    val lp: PageContext => LoadablePage = c match {
      case Home              => HomePage
      case Contribute        => ContributePage
      case ManageTasks       => ManageTasksPage
      case ManageResources   => ManageResourcesPage
      case TaskList          => TaskListPage
      case Notifications     => NotificationsPage
      case Reports           => ReportsPage
      case MyResources       => MyResourcesPage
      case Searching         => SearchingPage
      case Hierarchy         => HierarchyPage.Root
      case Harvester         => HarvesterPage
      case RemoteRepos       => RemoteReposPage
      case Favourites        => FavouritesPage
      case ManageActivations => ManageActivationsPage
    }
    val page = lp(b.page.ctx).load()
    (page, Prop(page.error.isEmpty).label(c.toString))
  }

  override def logon: TestLogon = autoTestLogon

  statefulTest("go to pages") {
    generateCommands {
      case s if s.completedPages == Pages.values => List()
      case s                                     =>
        Fairness.favourIncomplete(1, 0)(Pages.values.toSeq, s.completedPages.contains).map(List(_))
    }
  }
}
