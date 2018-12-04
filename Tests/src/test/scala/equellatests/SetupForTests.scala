package equellatests

import java.io.File

import com.tle.webtests.pageobject.{LoginPage, SettingsPage, UndeterminedPage}
import com.tle.webtests.pageobject.institution._
import GlobalConfig._
import com.tle.webtests.framework.{PageContext, ScreenshotTaker}
import com.tle.webtests.pageobject.multidb.InstallPage

import scala.util.{Failure, Success, Try}

object ImportInsts {
  val configInstFilter: String => Boolean = {
    Option(testConfig.getProperty("tests.insts")).map(_.split(",").map(_.trim).toSet)
      .getOrElse((_: String) => true)
  }
  val INSTITUTION_FILE = "institution"
}

class ImportInsts(allowed: String => Boolean) {

  import ImportInsts._

  val insts: Seq[File] = {
    val baseTestFolder = new File(testConfig.getTestFolder, "tests")
    baseTestFolder.listFiles.toSeq.filter { testDir =>
      allowed(testDir.getName) && new File(testDir, INSTITUTION_FILE).isDirectory
    }
  }

  def run(): Unit = {
    TestChecker.withServerAdmin { context =>
      insts.foreach { instFolder =>
        val shortName = instFolder.getName
        val instutionUrl = context.getTestConfig.getInstitutionUrl(shortName)

        val listTab = new InstitutionListTab(context)
        var importTab = new ImportTab(context)
        val choice = new UndeterminedPage[InstitutionTabInterface](context, listTab, importTab)
        var currentTab = choice.load
        if (currentTab eq listTab) {
          if (listTab.institutionExists(instutionUrl)) {
            val statusPage = listTab.delete(instutionUrl, choice)
            assert(statusPage.waitForFinish)
            currentTab = statusPage.back
          }
          if (currentTab ne importTab) importTab = listTab.importTab
        }

        assert(importTab.importInstitution(instutionUrl, shortName,
          new File(instFolder, INSTITUTION_FILE).toPath).waitForFinish)
        if (testConfig.isNewUI) {
          val instCtx = new PageContext(context, instutionUrl)
          new LoginPage(instCtx).load.login("TLE_ADMINISTRATOR", testConfig.getAdminPassword)
          val sp = new SettingsPage(instCtx).load()
          sp.enableNewUI()
        }
      }
    }
  }
}

object SetupForTests extends App {
  val instFilter = if (args.isEmpty) {
    ImportInsts.configInstFilter
  } else {
    args.toSet
  }
  new ImportInsts(instFilter).run()
}
