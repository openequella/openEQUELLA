package com.tle.webtests.remotetest.integration.blackboard;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.SkipException;
import org.testng.annotations.Test;

import com.tle.common.Check;
import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.connectors.EditBlackboardConnectorPage;
import com.tle.webtests.pageobject.connectors.ShowBlackboardConnectorsPage;
import com.tle.webtests.pageobject.connectors.ShowConnectorsPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardContentPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardCoursePage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardLoginPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardMyInstitutionPage;
import com.tle.webtests.pageobject.integration.blackboard.BlackboardProxyPage;
import com.tle.webtests.pageobject.reporting.ConnectorReportOptionsPage;
import com.tle.webtests.pageobject.reporting.ConnectorReportPage;
import com.tle.webtests.pageobject.reporting.ReportingPage;
import com.tle.webtests.pageobject.searching.BulkSection;
import com.tle.webtests.pageobject.searching.EditExternalResourcePage;
import com.tle.webtests.pageobject.searching.ExternalMoveDialog;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.searching.ManageExternalResourcePage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.settings.ScheduledTasksPage;
import com.tle.webtests.pageobject.viewitem.AdminTabPage;
import com.tle.webtests.pageobject.viewitem.FindUsesPage;
import com.tle.webtests.pageobject.viewitem.LMSExportPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.viewitem.VersionsPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.remotetest.integration.ConnectorHelper;

@Test(groups = "blackboardTest")
@TestInstitution("blackboard")
public class BlackboardConnectorTest extends AbstractBlackboardTest
{
	private static final String FOLDER1 = "BlackboardConnectorTest Folder1";
	private static final String FOLDER2 = "BlackboardConnectorTest Folder2";

	private static final String COURSE_NAME = "EQUELLA TEST COURSE";
	private static final String SECTION_NAME = "Content";
	private static final String SECTION_NAME_2 = "Information";

	@Name(value = "Blackboard", group = "cats")
	private static PrefixedName BLACKBOARD_CONNECTOR_NAME;
	@Name(value = "Blackboard2", group = "cats")
	private static PrefixedName BLACKBOARD_CONNECTOR_NAME2;

	private static final String ARCHIVED_COURSE_NAME = "Archived Course";
	private static final String WEIRD_ITEM = "UnicodeItem - Name with <>'\" ? & chars";

	private String password;
	private ConnectorHelper connectorHelper;

	public BlackboardConnectorTest()
	{
		setDeleteCredentials("AutoTest", "automated");
	}

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		connectorHelper = new ConnectorHelper(context, BLACKBOARD_CONNECTOR_NAME);
	}

	@Test(dependsOnMethods = "setupBlackboard")
	public void appearsInBlackboard()
	{
		logon("TLE_ADMINISTRATOR", "tle010");

		String fullName = context.getFullName("Appears In Blackboard");

		LMSExportPage lms = connectorHelper.createTestItem(fullName).adminTab().lmsPage();
		connectorHelper.selectConnector(lms);
		lms.selectSummary();
		lms.clickCourse(COURSE_NAME).clickSection(SECTION_NAME);
		lms.publish();

		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon(ADMIN_USERNAME,
			ADMIN_PASSWORD);

		BlackboardCoursePage course = indexPage.clickCourse(COURSE_NAME);

		BlackboardContentPage content = course.content();
		assertTrue(content.hasResource(fullName));

		content.deleteResource(fullName);
		assertFalse(content.hasResource(fullName), fullName + " not found");

		ShowConnectorsPage page = new ShowConnectorsPage(context).load();
		assertTrue(page.entityExists(BLACKBOARD_CONNECTOR_NAME2));
		assertFalse(page.isEntityDisabled(BLACKBOARD_CONNECTOR_NAME2));
		page.disableEntity(BLACKBOARD_CONNECTOR_NAME2);
		assertTrue(page.isEntityDisabled(BLACKBOARD_CONNECTOR_NAME2));
		page.deleteEntity(BLACKBOARD_CONNECTOR_NAME2);
		assertFalse(page.entityExists(BLACKBOARD_CONNECTOR_NAME2));
	}

	@Test(dependsOnMethods = "setupBlackboard")
	public void bulkMoveActionTest()
	{
		String moveName1 = context.getFullName("move");
		String moveName2 = context.getFullName("move 2");

		logon("AutoTest", "automated");
		SummaryPage item = connectorHelper.createTestItem(moveName1);
		connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);

		item = connectorHelper.createTestItem(moveName2);
		connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);

		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		BlackboardCoursePage coursePage = indexPage.clickCourse(COURSE_NAME);
		BlackboardContentPage content = coursePage.content();

		assertTrue(content.hasResource(moveName1));
		assertTrue(content.hasResource(moveName2));

		logon("AutoTest", "automated");
		ManageExternalResourcePage external = new ManageExternalResourcePage(context).load();
		connectorHelper.selectConnector(external);

		external.search(moveName1);
		List<ItemSearchResult> results = external.results().getResults();
		assertEquals(results.size(), 2);
		results.get(0).setChecked(true);
		results.get(1).setChecked(true);

		ExternalMoveDialog moveDialog = external.externalBulk().move();

		assertFalse(moveDialog.hasCourse(ARCHIVED_COURSE_NAME));
		moveDialog.showArchived(true);
		assertTrue(moveDialog.hasCourse(ARCHIVED_COURSE_NAME));

		moveDialog.clickCourse(ARCHIVED_COURSE_NAME).clickSection(SECTION_NAME_2);
		assertTrue(moveDialog.execute(external));

		indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		coursePage = indexPage.clickCourse(COURSE_NAME);
		content = coursePage.content();

		assertFalse(content.hasResource(moveName1));
		assertFalse(content.hasResource(moveName2));

		indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		coursePage = indexPage.clickCourse(ARCHIVED_COURSE_NAME);
		content = coursePage.information();

		assertTrue(content.hasResource(moveName1));
		assertTrue(content.hasResource(moveName2));
	}

	@Test(dependsOnMethods = "setupBlackboard")
	public void manageExternalResourcesTest()
	{
		String fullName = context.getFullName("external");
		String nameEdit = fullName + " edited";
		String descriptionEdit = context.getFullName("description");

		logon("AutoTest", "automated");
		SummaryPage item = connectorHelper.createTestItem(fullName);

		connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);
		LMSExportPage lms = connectorHelper.addToCourse(item, ARCHIVED_COURSE_NAME, SECTION_NAME);

		FindUsesPage uses = connectorHelper.selectConnector(lms.findUsesPage());
		uses.showArchived(true);
		assertTrue(uses.hasEntry(COURSE_NAME, SECTION_NAME));
		assertTrue(uses.hasEntry(ARCHIVED_COURSE_NAME, SECTION_NAME));

		ManageExternalResourcePage external = new ManageExternalResourcePage(context).load();
		assertFalse(external.hasResults());
		connectorHelper.selectConnector(external);
		assertTrue(external.hasResults());

		external.search(fullName);
		assertEquals(external.results().getResults().size(), 2);
		external.showArchived(false);
		assertEquals(external.results().getResults().size(), 1);
		ItemSearchResult result = external.results().getResults().get(0);
		assertEquals(result.getDetailText("Course"), COURSE_NAME);
		assertEquals(result.getDetailText("Location"), SECTION_NAME);

		external.showArchived(true);
		assertEquals(external.results().getResults().size(), 2);
		external.selectCourse(ARCHIVED_COURSE_NAME);
		assertEquals(external.results().getResults().size(), 1);
		result = external.results().getResults().get(0);
		assertEquals(result.getDetailText("Course"), ARCHIVED_COURSE_NAME);
		assertEquals(result.getDetailText("Location"), SECTION_NAME);
		result.clickActionConfirmAndRemove("Remove", true, external);
		external.selectCourse(null);
		assertEquals(external.results().getResults().size(), 1);
		result = external.results().getResults().get(0);
		EditExternalResourcePage editPage = result.clickAction("Edit", new EditExternalResourcePage(context, result));
		editPage.setName(nameEdit);
		editPage.setDescription(descriptionEdit);
		result = editPage.save();
		assertEquals(result.getTitle(), nameEdit);

		uses = SearchPage.searchAndView(context, fullName).findUsesPage();
		connectorHelper.selectConnector(uses);
		uses.showArchived(true);
		assertTrue(uses.hasEntry(COURSE_NAME, SECTION_NAME));
		assertFalse(uses.hasEntry(ARCHIVED_COURSE_NAME, SECTION_NAME));

		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		BlackboardCoursePage coursePage = indexPage.clickCourse(COURSE_NAME);
		BlackboardContentPage content = coursePage.content();

		assertTrue(content.hasResource(nameEdit));
		assertEquals(content.editResource(nameEdit).getDescription(), descriptionEdit);

		indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		coursePage = indexPage.clickCourse(ARCHIVED_COURSE_NAME);
		content = coursePage.content();

		assertFalse(content.hasResource(fullName));
	}

	@Test(dependsOnMethods = "setupBlackboard")
	public void blackboardReport()
	{
		logon("AutoTest", "automated");

		String fullName = context.getFullName("report");
		SummaryPage item = connectorHelper.createTestItem(fullName);
		connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);
		connectorHelper.addToCourse(item, ARCHIVED_COURSE_NAME, SECTION_NAME);

		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		BlackboardCoursePage coursePage = indexPage.clickCourse(COURSE_NAME);
		BlackboardContentPage content = coursePage.content();

		assertTrue(content.hasResource(fullName));

		logon("AutoTest", "automated");
		ReportingPage reports = new ReportingPage(context).load();
		assertTrue(reports.isReportExisting("External Report"));

		ConnectorReportOptionsPage optionsPage = reports.getReport("External Report", new ConnectorReportOptionsPage(
			context));
		optionsPage.selectConnector(BLACKBOARD_CONNECTOR_NAME);
		optionsPage.showArchived(false);
		ConnectorReportPage reportPage = optionsPage.execute();

		assertTrue(reportPage.hasResource(fullName, COURSE_NAME));
		assertFalse(reportPage.hasResource(fullName, ARCHIVED_COURSE_NAME));
		reportPage.close();

		reports = new ReportingPage(context).load();
		optionsPage = reports.getReport("External Report", new ConnectorReportOptionsPage(context));
		optionsPage.selectConnector(BLACKBOARD_CONNECTOR_NAME);
		optionsPage.showArchived(true);
		reportPage = optionsPage.execute();

		assertTrue(reportPage.hasResource(fullName, COURSE_NAME));
		assertTrue(reportPage.hasResource(fullName, ARCHIVED_COURSE_NAME));
		reportPage.close();
	}

	@Test(dependsOnMethods = "setupBlackboard")
	public void bulkRemoveActionTest()
	{
		String deleteName1 = context.getFullName("delete");
		String deleteName2 = context.getFullName("delete 2");

		logon("AutoTest", "automated");
		SummaryPage item = connectorHelper.createTestItem(deleteName1);
		connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);

		item = connectorHelper.createTestItem(deleteName2);
		connectorHelper.addToCourse(item, COURSE_NAME, SECTION_NAME);

		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		BlackboardCoursePage coursePage = indexPage.clickCourse(COURSE_NAME);
		BlackboardContentPage content = coursePage.content();

		assertTrue(content.hasResource(deleteName1));
		assertTrue(content.hasResource(deleteName2));

		logon("AutoTest", "automated");
		ManageExternalResourcePage external = new ManageExternalResourcePage(context).load();
		connectorHelper.selectConnector(external);
		external.search(deleteName1);
		List<ItemSearchResult> results = external.results().getResults();
		assertEquals(results.size(), 2);
		BulkSection bulk = external.bulk();
		bulk.selectAll();
		assertTrue(bulk.executeCommand("remove"));

		indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		coursePage = indexPage.clickCourse(COURSE_NAME);
		content = coursePage.content();

		assertFalse(content.hasResource(deleteName1));
		assertFalse(content.hasResource(deleteName2));
	}

	@Test(dependsOnMethods = "setupBlackboard")
	public void appearsInUsage()
	{
		logon("TLE_ADMINISTRATOR", "tle010");
		String itemName = context.getFullName("Appears In Usage");

		LMSExportPage lms = connectorHelper.createTestItem(itemName).adminTab().lmsPage();
		connectorHelper.selectConnector(lms);
		lms.selectSummary();
		lms.clickCourse(COURSE_NAME).clickSection(SECTION_NAME);
		lms = lms.publish();

		FindUsesPage findUses = lms.findUsesPage();
		connectorHelper.selectConnector(findUses);
		assertTrue(findUses.hasEntry(COURSE_NAME, SECTION_NAME));
	}

	@Test(dependsOnMethods = "setupBlackboard")
	public void newVersionShowAllVersions()
	{
		logon("TLE_ADMINISTRATOR", "tle010");

		LMSExportPage lms = connectorHelper.createTestItem(context.getFullName("New version version1")).adminTab()
			.lmsPage();
		connectorHelper.selectConnector(lms);
		lms.selectSummary();
		lms.clickCourse(COURSE_NAME).clickSection(SECTION_NAME);
		lms = lms.publish();

		WizardPageTab wiz = lms.summary().adminTab().newVersion();
		wiz.editbox(1, context.getFullName("New version version2"));

		AdminTabPage admin = wiz.save().publish().adminTab();
		FindUsesPage findUses = admin.findUsesPage();
		connectorHelper.selectConnector(findUses);
		assertFalse(findUses.hasEntry(COURSE_NAME, SECTION_NAME));
		findUses = findUses.showAllVersions(true);

		assertTrue(findUses.hasEntry(COURSE_NAME, SECTION_NAME));
	}

	@Test(dependsOnMethods = "setupBlackboard")
	public void addInBlackboardAppearsOnUses()
	{
		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		BlackboardCoursePage coursePage = indexPage.clickCourse(COURSE_NAME);
		BlackboardContentPage content = coursePage.content().addFolder(FOLDER1).enterFolder(FOLDER1);
		SelectionSession selection = content.addEquellaResource();
		ItemListPage items = selection.getSearchPage().exactQuery("UnicodeItem");
		SummaryPage summary = items.viewFromTitle(WEIRD_ITEM);
		summary.selectItemSummary();
		content = content.finishAddEquellaResource(selection);

		logon("AutoTest", "automated");

		FindUsesPage uses = SearchPage.searchExact(context, "UnicodeItem").getResult(1).viewSummary().findUsesPage();

		connectorHelper.selectConnector(uses);

		assertTrue(uses.hasEntry(COURSE_NAME, FOLDER1));
		uses.showArchived(true);
		assertTrue(uses.hasEntry(COURSE_NAME, FOLDER1));
	}

	@Test(dependsOnMethods = "setupBlackboard")
	public void addInArchivedBlackboardAppearsOnUsesWhenRelevant()
	{
		BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
		BlackboardCoursePage coursePage = indexPage.clickCourse(ARCHIVED_COURSE_NAME);
		BlackboardContentPage content = coursePage.content().addFolder(FOLDER1).addFolder(FOLDER2).enterFolder(FOLDER1);
		SelectionSession selection = content.addEquellaResource();
		ItemListPage items = selection.getSearchPage().exactQuery("UnicodeItem");
		SummaryPage summary = items.viewFromTitle(WEIRD_ITEM);
		summary.selectItemSummary();
		content = content.finishAddEquellaResource(selection);

		logon("AutoTest", "automated");

		FindUsesPage uses = SearchPage.searchExact(context, "UnicodeItem").getResult(1).viewSummary().findUsesPage();

		connectorHelper.selectConnector(uses);

		assertFalse(uses.hasEntry(ARCHIVED_COURSE_NAME, FOLDER1));
		uses.showArchived(true);
		assertTrue(uses.hasEntry(ARCHIVED_COURSE_NAME, FOLDER1));

		assertFalse(uses.hasEntry(ARCHIVED_COURSE_NAME, FOLDER2));

		LMSExportPage lmsPage = uses.lmsPage();
		connectorHelper.selectConnector(lmsPage);
		lmsPage.selectSummary();

		lmsPage.showArchived(false);
		assertFalse(lmsPage.hasCourse(ARCHIVED_COURSE_NAME));
		lmsPage.showArchived(true);
		assertTrue(lmsPage.hasCourse(ARCHIVED_COURSE_NAME));

		lmsPage.clickCourse(ARCHIVED_COURSE_NAME).clickFolder(SECTION_NAME).clickSection(FOLDER2);
		lmsPage.publish();

		uses = lmsPage.findUsesPage();
		connectorHelper.selectConnector(uses);

		uses.showArchived(false);
		assertFalse(uses.hasEntry(ARCHIVED_COURSE_NAME, FOLDER2));

		uses.showArchived(true);
		assertTrue(uses.hasEntry(ARCHIVED_COURSE_NAME, FOLDER2));
	}

	@Test(dependsOnMethods = "setupBlackboard")
	public void selectImsPackage()
	{
		logon("TLE_ADMINISTRATOR", "tle010");
		String fullName = context.getFullName("Select IMS package");
		SummaryPage item = connectorHelper.createTestItem(fullName);
		LMSExportPage lms = item.lmsPage();
		connectorHelper.selectConnector(lms);
		lms.selectPackage();
		lms.clickCourse(COURSE_NAME).clickSection(SECTION_NAME);
		lms = lms.publish();

		FindUsesPage findUses = lms.findUsesPage();
		connectorHelper.selectConnector(findUses);
		// make sure the package appears in details
		// FIXME: when EQ-1604 fixed, change this to the name of the attachment
		// assertTrue(findUses.hasAttachment(COURSE_NAME, SECTION_NAME,
		// PathUtils.urlPath(context.getBaseUrl(), "/integ/gen/etc...")));
	}

	@Test(dependsOnMethods = "setupBlackboard")
	public void selectAttachment()
	{
		logon("TLE_ADMINISTRATOR", "tle010");
		String fullName = context.getFullName("Select an attachment");
		SummaryPage item = connectorHelper.createTestItem(fullName);
		LMSExportPage lms = item.lmsPage();
		connectorHelper.selectConnector(lms);
		String ATTACHMENT = "veronicas_wall1.jpg";
		lms.selectAttachment(ATTACHMENT, false);
		lms.clickCourse(COURSE_NAME).clickSection(SECTION_NAME);
		lms = lms.publish();

		FindUsesPage findUses = lms.findUsesPage();
		connectorHelper.selectConnector(findUses);
		// make sure the attachment appears in details
		// FIXME: when EQ-1604 fixed, change this to the name of the attachment
		// assertTrue(findUses.hasAttachment(COURSE_NAME, SECTION_NAME,
		// PathUtils.urlPath(context.getBaseUrl(), "/integ/gen/etc...")));
	}

	@Test(dependsOnMethods = "setupBlackboard")
	public void newVersionThenOldVersionAddedIntoAnAlwaysLatestCourse()
	{
		logon("TLE_ADMINISTRATOR", "tle010");
		LMSExportPage lms = connectorHelper.createTestItem(context.getFullName("Add Old Version version1")).adminTab()
			.lmsPage();
		connectorHelper.selectConnector(lms);
		lms.selectSummary();
		lms.clickCourse("Always latest items course").clickSection(SECTION_NAME);
		lms = lms.publish();

		WizardPageTab wiz = lms.summary().adminTab().newVersion();
		wiz.editbox(1, context.getFullName("Add Old Version version2"));

		SummaryPage item = wiz.save().publish();
		VersionsPage versions = item.clickShowAllVersion();
		// View version 1
		item = versions.clickVersion(1);

		FindUsesPage findUses = item.adminTab().findUsesPage();
		connectorHelper.selectConnector(findUses);
		assertFalse(findUses.hasEntry("Always latest items course", SECTION_NAME));

		findUses = findUses.showAllVersions(true);

		assertTrue(findUses.hasEntry("Always latest items course", SECTION_NAME));

		// Now view version 2
		versions = findUses.clickShowAllVersion();// Note: not the show all
													// versions checkbox! This
													// is the item version
													// history
		item = versions.clickVersion(2);
		item.adminTab().findUsesPage();
		connectorHelper.selectConnector(findUses);
		assertTrue(findUses.hasEntry("Always latest items course", SECTION_NAME));
	}

	@Test
	public void setupBlackboard()
	{
		if( Check.isEmpty(testConfig.getIntegrationUrl("blackboard")) )
		{
			throw new SkipException("blackboard url not set");
		}

		logon("TLE_ADMINISTRATOR", "tle010");
		ShowConnectorsPage page = new ShowConnectorsPage(context).load();
		ShowBlackboardConnectorsPage.registerProxyTool(page, context.getIntegUrl());

		new BlackboardLoginPage(context).load().logon(ADMIN_USERNAME, ADMIN_PASSWORD);

		BlackboardProxyPage proxyPage = new BlackboardProxyPage(context).load();
		if( proxyPage.hasEquella() )
		{
			password = proxyPage.setAvailable();
		}

		String proxyPassword = ensureProxy();

		logon("TLE_ADMINISTRATOR", "tle010");
		page = new ShowConnectorsPage(context).load();
		ShowBlackboardConnectorsPage.addBlackboardConnection(page, BLACKBOARD_CONNECTOR_NAME, context.getIntegUrl(),
			"testuser", proxyPassword);
		EditBlackboardConnectorPage editPage = page.editConnector(new EditBlackboardConnectorPage(page),
			BLACKBOARD_CONNECTOR_NAME);
		editPage.exportableForAll();
		editPage.viewableForAll();
		editPage.save();

		assertTrue(page.entityExists(BLACKBOARD_CONNECTOR_NAME));

		new ScheduledTasksPage(context).load().runSyncEquellaContent();

		page = new ShowConnectorsPage(context).load();
		ShowBlackboardConnectorsPage.addBlackboardConnection(page, BLACKBOARD_CONNECTOR_NAME2, context.getIntegUrl(),
			"testuser", proxyPassword);

		editPage = page.editConnector(new EditBlackboardConnectorPage(page), BLACKBOARD_CONNECTOR_NAME2);
		editPage.exportableForAll();
		editPage.viewableForAll();
		editPage.save();
	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		super.cleanupAfterClass();
		if( !Check.isEmpty(testConfig.getIntegrationUrl("blackboard")) )
		{
			String partialName = context.getNamePrefix();

			BlackboardMyInstitutionPage indexPage = new BlackboardLoginPage(context).load().logon("AutoTest", "``````");
			BlackboardCoursePage coursePage = indexPage.clickCourse(ARCHIVED_COURSE_NAME);
			coursePage.content().deleteResourceIfExists(FOLDER1).deleteResourceIfExists(FOLDER2);

			logon("TLE_ADMINISTRATOR", "tle010");

			ManageExternalResourcePage external = new ManageExternalResourcePage(context).load();
			connectorHelper.selectConnector(external);
			external.search(partialName);
			if( external.hasResults() )
			{
				BulkSection bulk = external.bulk();
				bulk.commandAll("remove");
			}
			ShowConnectorsPage page = new ShowConnectorsPage(context).load();
			page.deleteAllNamed(getNames("cats"));
		}
	}

	private String ensureProxy()
	{
		if( Check.isEmpty(password) )
		{
			new BlackboardLoginPage(context).load().logon(ADMIN_USERNAME, ADMIN_PASSWORD);
			BlackboardProxyPage proxyPage = new BlackboardProxyPage(context).load();
			assertTrue(proxyPage.hasEquella(), "No EQUELLA proxy tool found");
			password = proxyPage.setAvailable();
		}
		assertTrue(!Check.isEmpty(password));
		return password;
	}
}
