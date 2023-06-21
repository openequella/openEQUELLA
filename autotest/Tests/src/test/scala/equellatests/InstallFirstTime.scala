package equellatests

import com.tle.webtests.framework.{PageContext, ScreenshotTaker}
import com.tle.webtests.pageobject.multidb.InstallPage
import equellatests.GlobalConfig.testConfig

import scala.util.{Failure, Success, Try}

object InstallFirstTime extends App {
  val DEFAULT_SCHEMA = "Default schema"

  import java.util.function.{Function => JavaFunction}

  implicit def scalaFunctionToJavaFunction[From, To](
      function: (From) => To): JavaFunction[From, To] = {
    new java.util.function.Function[From, To] {
      override def apply(input: From): To = function(input)
    }
  }

  TestChecker.withBrowserDriver("install", testConfig) { driver =>
    println("Line 1: Start withBrowserDriver")
    val context = new PageContext(driver, testConfig, testConfig.getAdminUrl)
    println("Line 2: Created PageContext")

    val emails = "noreply@equella.com;test@equella.com"
    println("Line 3: Defined emails")

    var installPage = new InstallPage(context).load
    println("Line 4: Loaded InstallPage")

    installPage.setPassword("")
    println("Line 5: Set password")

    installPage.setPasswordConfirm("")
    println("Line 6: Confirmed password")

    installPage.setEmails("")
    println("Line 7: Set emails")

    installPage.setSmtpServer("")
    println("Line 8: Set smtpServer")

    installPage.setNoReply("")
    println("Line 9: Set noReply")

    installPage = installPage.installInvalid(scalaFunctionToJavaFunction(_.isPasswordError))
    println("Line 10: Install invalid")

    assert(installPage.isPasswordError)
    println("Line 11: Asserted password error")

    assert(installPage.isEmailsError)
    println("Line 12: Asserted emails error")

    assert(installPage.isStmpError)
    println("Line 13: Asserted smtp error")

    assert(installPage.isNoReplyError)
    println("Line 14: Asserted noReply error")

    installPage.setPassword(testConfig.getAdminPassword)
    println("Line 15: Set password")

    installPage.setPasswordConfirm(testConfig.getAdminPassword)
    println("Line 16: Confirmed password")

    installPage.setEmails("@@")
    println("Line 17: Set emails")

    installPage.setSmtpServer("localhost")
    println("Line 18: Set smtpServer")

    installPage.setNoReply("noreply@noreply.com")
    println("Line 19: Set noReply")

    installPage = installPage.installInvalid(scalaFunctionToJavaFunction(!_.isPasswordError))
    println("Line 20: Install invalid")

    assert(!installPage.isPasswordError)
    println("Line 21: Asserted no password error")

    assert(installPage.isEmailsError)
    println("Line 22: Asserted emails error")

    installPage.setPassword(testConfig.getAdminPassword)
    println("Line 23: Set password")

    installPage.setEmails(emails)
    println("Line 24: Set emails")

    installPage.setSmtpServer("mail.google.com")
    println("Line 25: Set smtpServer")

    val dbPage = installPage.install
    println("Line 26: Installed")

    assert(dbPage.containsDatabase(DEFAULT_SCHEMA))
    println("Line 27: Asserted contains database")

    val dbRow = dbPage.getDatabaseRow(DEFAULT_SCHEMA)
    println("Line 28: Got database row")

    dbRow.initialise()
    println("Line 29: Initialised dbRow")

    try {
      dbRow.waitForMigrate()
      println("Line 30: Wait for dbRow migration")
    } catch {
      case e: Exception => {
        e.printStackTrace();
      }
    }
    driver.quit()
    println("Line 31: Quit driver")
  }

}
