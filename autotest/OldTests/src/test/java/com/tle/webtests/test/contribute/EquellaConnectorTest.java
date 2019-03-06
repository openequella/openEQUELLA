package com.tle.webtests.test.contribute;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.common.Check;
import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.connectors.ShowConnectorsPage;
import com.tle.webtests.pageobject.connectors.ShowEquellaConnectorsPage;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.searching.ManageExternalResourcePage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.searching.SearchScreenOptions;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.FindUsesPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.ResourceUniversalControlType;
import com.tle.webtests.remotetest.integration.ConnectorHelper;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("contribute")
public class EquellaConnectorTest extends AbstractCleanupTest
{
	@Name("Local resources")
	private static PrefixedName CONNECTOR;
	private ConnectorHelper connectorHelper;

	@Override
	protected void customisePageContext()
	{
		super.customisePageContext();
		connectorHelper = new ConnectorHelper(context, CONNECTOR);
	}

	@Test
	public void setupConnector()
	{
		logon("TLE_ADMINISTRATOR", "tle010");

		ShowConnectorsPage connectors = new ShowConnectorsPage(context).load();
		ShowEquellaConnectorsPage.addEquellaConnection(connectors, CONNECTOR);
	}

	@Test(dependsOnMethods = "setupConnector")
	public void equellaConnector()
	{
		logon("AutoTest", "automated");

		String linkToMe = context.getFullName("link to me");
		String linkToMeDraft = context.getFullName("link to me draft");
		String fullname = context.getFullName("main");
		String fullnameDraft = context.getFullName("main draft");

		WizardPageTab wizard = new ContributePage(context).load().openWizard("Basic Attachments");
		wizard.editbox(1, linkToMe);
		wizard.save().publish();

		wizard = new ContributePage(context).load().openWizard("Basic Attachments");
		wizard.editbox(1, linkToMeDraft);
		wizard.save().draft();

		wizard = new ContributePage(context).load().openWizard("Basic Attachments");
		wizard.editbox(1, fullname);

		UniversalControl universalControl = wizard.universalControl(3);
		ResourceUniversalControlType resource = universalControl.addResource(new ResourceUniversalControlType(
			universalControl));
		SelectionSession session = resource.getSelectionSession();
		ItemListPage search = session.homeExactSearch(linkToMe);
		SearchScreenOptions sso = new SearchScreenOptions(search).open();
		sso.setNonLiveOption(true);

		search.setSelectionChecked(linkToMe, true);
		search.setSelectionChecked(linkToMeDraft, true);
		wizard = session.finishedSelecting(wizard);

		wizard.waitForSelectedItem(linkToMe);
		wizard.save().publish();

		wizard = new ContributePage(context).load().openWizard("Basic Attachments");
		wizard.editbox(1, fullnameDraft);

		resource = wizard.universalControl(3).addResource(resource);
		session = resource.getSelectionSession();
		search = session.homeExactSearch(linkToMe);
		sso = new SearchScreenOptions(search).open();
		sso.setNonLiveOption(true);

		search.setSelectionChecked(linkToMe, true);
		search.setSelectionChecked(linkToMeDraft, true);
		wizard = session.finishedSelecting(wizard);
		wizard.waitForSelectedItem(linkToMe);
		wizard.save().draft();

		SummaryPage summary = SearchPage.searchAndView(context, linkToMe);
		FindUsesPage uses = summary.findUsesPage();
		uses = connectorHelper.selectConnector(uses);
		uses = uses.showArchived(false);

		assertTrue(uses.hasEntry(fullname, linkToMe));
		assertFalse(uses.hasEntry(fullnameDraft, linkToMe));

		uses = uses.showArchived(true);

		assertTrue(uses.hasEntry(fullname, linkToMe));
		assertTrue(uses.hasEntry(fullnameDraft, linkToMe));

		ItemListPage listPage = SearchPage.searchExact(context, linkToMeDraft);
		sso = new SearchScreenOptions(listPage).open();
		sso.setNonLiveOption(true);

		summary = listPage.viewFromTitle(linkToMeDraft);

		uses = summary.findUsesPage();
		uses = connectorHelper.selectConnector(uses);
		uses = uses.showArchived(false);

		assertTrue(uses.hasEntry(fullname, linkToMeDraft));
		assertFalse(uses.hasEntry(fullnameDraft, linkToMeDraft));

		uses = uses.showArchived(true);

		assertTrue(uses.hasEntry(fullname, linkToMeDraft));
		assertTrue(uses.hasEntry(fullnameDraft, linkToMeDraft));

		ManageExternalResourcePage external = new ManageExternalResourcePage(context).load();
		assertFalse(external.hasResults());
		connectorHelper.selectConnector(external);
		assertTrue(external.hasResults());

		external.search(linkToMe);
		assertEquals(external.results().getResults().size(), 4);
		external.showArchived(false);
		assertEquals(external.results().getResults().size(), 2);

		external.setSort("name");
		external.setSortRevese(false);

		ItemSearchResult result = external.results().getResults().get(0);
		assertEquals(result.getDetailText("openEQUELLA resource"), linkToMe);
		assertEquals(result.getDetailText("Linking resource"), fullname);

		external.setSortRevese(true);
		result = external.results().getResults().get(0);
		assertEquals(result.getDetailText("openEQUELLA resource"), linkToMeDraft);
		assertEquals(result.getDetailText("Linking resource"), fullname);

		external.showArchived(true);
		external.search(linkToMeDraft);

		external.setSort("date_added");
		external.setSortRevese(false);
		assertEquals(external.results().getResults().size(), 2);

		result = external.results().getResults().get(0);
		assertEquals(result.getDetailText("openEQUELLA resource"), linkToMeDraft);
		assertEquals(result.getDetailText("Linking resource"), fullnameDraft);

		result = external.results().getResults().get(1);
		assertEquals(result.getDetailText("openEQUELLA resource"), linkToMeDraft);
		assertEquals(result.getDetailText("Linking resource"), fullname);

	}

	@Override
	protected void cleanupAfterClass() throws Exception
	{
		super.cleanupAfterClass();
		if( !Check.isEmpty(testConfig.getIntegrationUrl("moodle")) )
		{
			logon("TLE_ADMINISTRATOR", "tle010");
			ShowConnectorsPage page = new ShowConnectorsPage(context).load();
			page.deleteAllNamed(CONNECTOR);
		}
	}
}
