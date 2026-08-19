package equellatests

import com.tle.webtests.framework.PageContext
import com.tle.webtests.pageobject.institution._
import com.tle.webtests.pageobject.{LoginPage, SettingsPage, UndeterminedPage}
import equellatests.GlobalConfig._
import org.slf4j.LoggerFactory

import java.io.File

object ImportInsts {
  val configInstFilter: String => Boolean = {
    Option(testConfig.getProperty("tests.insts"))
      .map(_.split(",").map(_.trim).toSet)
      .getOrElse((_: String) => true)
  }
  val INSTITUTION_FILE = "institution"
  val DEFAULT_SCHEMA   = "Default schema"
}

class ImportInsts(allowed: String => Boolean) {
  private val LOGGER = LoggerFactory.getLogger(classOf[ImportInsts])

  import ImportInsts._

  val insts: Seq[File] = {
    val baseTestFolder = new File(testConfig.getTestFolder, "tests")
    baseTestFolder.listFiles.toSeq.filter { testDir =>
      allowed(testDir.getName) && new File(testDir, INSTITUTION_FILE).isDirectory
    }
  }

  def run(): Unit = {
    TestChecker.withServerAdmin(
      "import",
      { context =>
        // Confirm the system is healthy before importing: on a fresh/slow instance the Default
        // schema migration may still be running, in which case no schema is available and the
        // import tab renders "No available schemas". Wait for it to come online first.
        val schemaRow = new DatabasesPage(context).load().getDatabaseRow(DEFAULT_SCHEMA)
        schemaRow.waitForMigrate()
        schemaRow.assertOnline()

        insts.foreach { instFolder =>
          val shortName      = instFolder.getName
          val institutionUrl = context.getTestConfig.getInstitutionUrl(shortName)

          val listTab     = new InstitutionListTab(context)
          val importTab   = new ImportTab(context)
          val databaseTab = new DatabasesPage(context)

          val possibleAdminPage =
            new UndeterminedPage[InstitutionTabInterface](context, listTab, importTab, databaseTab)

          // Try to load the institution admin page, it could be one of the three (list, import, database) pages
          // depending on the current system state.
          val possibleTab = possibleAdminPage.load

          possibleTab match {
            case tab: InstitutionListTab =>
              val currentTab = deleteExistingInstitution(tab, institutionUrl, possibleAdminPage)
              navigateToImportTab(currentTab, listTab)
            case tab: DatabasesPage =>
              // Redirect to institution list page.
              val listPage   = tab.clickTab(listTab)
              val currentTab =
                deleteExistingInstitution(listPage, institutionUrl, possibleAdminPage)
              navigateToImportTab(currentTab, listTab)
            case _: ImportTab => () // Ready to proceed.
            case _            =>
              throw new IllegalStateException(s"Unexpected page type: ${possibleTab.getClass}")
          }

          assert(
            importTab
              .importInstitution(
                institutionUrl,
                shortName,
                new File(instFolder, INSTITUTION_FILE).toPath
              )
              .waitForFinish
          )
          if (testConfig.isNewUI) {
            val instCtx = new PageContext(context, institutionUrl)
            // Currently the homepage is still in old UI, set newUI to false.
            new LoginPage(instCtx).load
              .login("TLE_ADMINISTRATOR", testConfig.getAdminPassword, false)
            val sp = new SettingsPage(instCtx).load()
            sp.setNewUI(true)
          }
        }
      }
    )
  }

  /** Deletes an institution if it exists and then back to the original page (which should be the
    * institution list tab but could also be other page depending on the current system state).
    *
    * @param currentTab
    *   The current institution tab interface, which should be InstitutionListTab.
    * @param institutionUrl
    *   The URL of the institution to check and delete.
    * @param possibleAdminPage
    *   UndeterminedPage for navigation after deletion.
    * @return
    *   The import tab page object ready for importing.
    */
  private def deleteExistingInstitution(
      currentTab: InstitutionListTab,
      institutionUrl: String,
      possibleAdminPage: UndeterminedPage[InstitutionTabInterface]
  ): InstitutionTabInterface = {
    if (currentTab.institutionExists(institutionUrl)) {
      LOGGER.info(s"Deleting existing institution: $institutionUrl")
      val statusPage = currentTab.delete(institutionUrl, possibleAdminPage)
      assert(statusPage.waitForFinish, s"Failed to delete institution: $institutionUrl")
      LOGGER.info(s"Successfully deleted institution: $institutionUrl")
      statusPage.back
    } else {
      currentTab
    }
  }

  private def navigateToImportTab(
      currentTab: InstitutionTabInterface,
      listTab: InstitutionListTab
  ): ImportTab = {
    currentTab match {
      case tab: ImportTab => tab
      // Direct navigation from DatabasesPage can be flaky in CI.
      // Navigate via InstitutionListTab as a workaround.
      case tab: DatabasesPage     => tab.clickTab(listTab).importTab()
      case tab: InstitutionTab[_] => tab.importTab()
      case _ => throw new IllegalStateException(s"Unexpected page type: ${currentTab.getClass}")
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
