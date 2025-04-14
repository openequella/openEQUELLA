package equellatests

import com.tle.webtests.framework.{PageContext, ScreenshotTaker}
import com.tle.webtests.pageobject.multidb.InstallPage
import equellatests.GlobalConfig.testConfig

import scala.util.{Failure, Success, Try}

object InstallFirstTime extends App {
  val DEFAULT_SCHEMA = "Default schema"

  import java.util.function.{Function => JavaFunction}

  implicit def scalaFunctionToJavaFunction[From, To](
      function: (From) => To
  ): JavaFunction[From, To] = {
    new java.util.function.Function[From, To] {
      override def apply(input: From): To = function(input)
    }
  }

  TestChecker.withBrowserDriver("install", testConfig) { driver =>
    val context     = new PageContext(driver, testConfig, testConfig.getAdminUrl)
    val emails      = "noreply@equella.com;test@equella.com"
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
