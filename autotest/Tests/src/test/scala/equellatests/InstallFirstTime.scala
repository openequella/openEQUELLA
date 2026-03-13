package equellatests

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.multidb.InstallPage
import equellatests.GlobalConfig.testConfig

object InstallFirstTime extends App {
  private val DEFAULT_SCHEMA    = "Default schema"
  private val EMAILS            = "noreply@equella.com;test@equella.com"
  private val LOCAL_SMTP_SERVER = "localhost"
  private val GMAIL_SMTP_SERVER = "mail.google.com"
  private val NO_REPLY          = "noreply@noreply.com"
  private val INVALID_EMAILS    = "@@"

  TestChecker.withBrowserDriver("install", testConfig) { driver =>
    val context     = new PageContext(driver, testConfig, testConfig.getAdminUrl)
    var installPage = new InstallPage(context).load

    installPage = validateEmptyFormFields(installPage)
    installPage = validateInvalidEmails(installPage)
    completeInstallation(installPage)

    driver.quit()
  }

  private def validateEmptyFormFields(page: InstallPage): InstallPage = {
    fillInstallationForm(
      page = page,
      emails = Some(""),
      smtpServer = Some(""),
      noReply = Some(""),
      password = Some(""),
      passwordConfirm = Some("")
    )

    val updatedPage = page.installAndWait(_.isPasswordError)

    assert(updatedPage.isPasswordError)
    assert(updatedPage.isEmailsError)
    assert(updatedPage.isSmtpError)
    assert(updatedPage.isNoReplyError)

    updatedPage
  }

  private def validateInvalidEmails(page: InstallPage): InstallPage = {
    fillInstallationForm(
      page = page,
      emails = Some(INVALID_EMAILS),
      smtpServer = Some(LOCAL_SMTP_SERVER),
      noReply = Some(NO_REPLY),
      password = Some(testConfig.getAdminPassword),
      passwordConfirm = Some(testConfig.getAdminPassword)
    )

    val updatedPage = page.installAndWait(_.isEmailsError)

    assert(!updatedPage.isPasswordError)
    assert(updatedPage.isEmailsError)

    updatedPage
  }

  private def completeInstallation(page: InstallPage): Unit = {
    fillInstallationForm(
      page = page,
      emails = Some(EMAILS),
      smtpServer = Some(GMAIL_SMTP_SERVER),
      noReply = None,
      password = None,
      passwordConfirm = None
    )

    val dbPage = page.install
    assert(dbPage.containsDatabase(DEFAULT_SCHEMA))

    val dbRow = dbPage.getDatabaseRow(DEFAULT_SCHEMA)
    dbRow.initialise()
    dbRow.waitForMigrate()
  }

  private def fillInstallationForm(
      page: InstallPage,
      emails: Option[String] = None,
      smtpServer: Option[String] = None,
      noReply: Option[String] = None,
      password: Option[String] = None,
      passwordConfirm: Option[String] = None
  ): Unit = {
    emails.foreach(page.setEmails)
    smtpServer.foreach(page.setSmtpServer)
    noReply.foreach(page.setNoReply)
    password.foreach(page.setPassword)
    passwordConfirm.foreach(page.setPasswordConfirm)
  }
}
