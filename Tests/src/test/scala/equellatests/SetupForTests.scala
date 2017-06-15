package equellatests

import java.io.File

import com.tle.webtests.pageobject.UndeterminedPage
import com.tle.webtests.pageobject.institution._
import GlobalConfig._
import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.multidb.InstallPage

object SetupForTests extends App {

  import java.util.function.{Function => JavaFunction}
  implicit def scalaFunctionToJavaFunction[From, To](function: (From) => To): JavaFunction[From, To] = {
    new java.util.function.Function[From, To] {
      override def apply(input: From): To = function(input)
    }
  }
  val INSTITUTION_FILE = "institution"
  val DEFAULT_SCHEMA = "Default schema"

  def insts: Seq[File] = {
    val testFolders = new File(testConfig.getTestFolder, "tests").list().toSeq
    Option(testConfig.getProperty("tests.insts"))
      .map(i => testFolders.filter(i.split(",").toSet))
      .getOrElse(testFolders)
      .map(n => new File(testConfig.getTestFolder, s"tests/$n"))
      .filter(f => new File(f, INSTITUTION_FILE).isDirectory)
  }

  if (testConfig.getBooleanProperty("tests.install", false)) {
    TestChecker.withBrowserDriver(testConfig) { driver =>
      val context = new PageContext(driver, testConfig, testConfig.getAdminUrl)
      val emails = "noreply@equella.com;test@equella.com"
      var installPage = new InstallPage(context).load
      installPage.setPassword("")
      installPage.setPasswordConfirm("")
      installPage.setEmails("")
      installPage.setSmtpServer("")
      installPage.setNoReply("")
      installPage = installPage.installInvalid(scalaFunctionToJavaFunction(_.isPasswordError))
      assert(installPage.isPasswordError)
      assert(installPage.isEmailsError)
      assert(installPage.isStmpError)
      assert(installPage.isNoReplyError)
      installPage.setPassword(testConfig.getAdminPassword)
      installPage.setPasswordConfirm(testConfig.getAdminPassword)
      installPage.setEmails("@@")
      installPage.setSmtpServer("localhost")
      installPage.setNoReply("noreply@noreply.com")
      installPage = installPage.installInvalid(scalaFunctionToJavaFunction(!_.isPasswordError))
      assert(!installPage.isPasswordError)
      assert(installPage.isEmailsError)
      installPage.setPassword(testConfig.getAdminPassword)
      installPage.setEmails(emails)
      installPage.setSmtpServer("mail.google.com")
      val dbPage = installPage.install
      assert(dbPage.containsDatabase(DEFAULT_SCHEMA))
      val dbRow = dbPage.getDatabaseRow(DEFAULT_SCHEMA)
      dbRow.initialise()
      dbRow.waitForMigrate()
      driver.quit()
    }
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
