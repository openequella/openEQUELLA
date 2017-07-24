package equellatests

import java.io.File

import com.tle.webtests.pageobject.UndeterminedPage
import com.tle.webtests.pageobject.institution._
import GlobalConfig._
import com.tle.webtests.framework.{PageContext, ScreenshotTaker}
import com.tle.webtests.pageobject.multidb.InstallPage

import scala.util.{Failure, Success, Try}

object SetupForTests extends App {

  val INSTITUTION_FILE = "institution"

  def insts: Seq[File] = {
    val testFolders = new File(testConfig.getTestFolder, "tests").list().toSeq
    Option(testConfig.getProperty("tests.insts"))
      .map(i => testFolders.filter(i.split(",").toSet))
      .getOrElse(testFolders)
      .map(n => new File(testConfig.getTestFolder, s"tests/$n"))
      .filter(f => new File(f, INSTITUTION_FILE).isDirectory)
  }

  TestChecker.withServerAdmin {
    context =>

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
      }
  }
}
