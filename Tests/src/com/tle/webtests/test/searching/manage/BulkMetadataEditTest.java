package com.tle.webtests.test.searching.manage;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.searching.BulkPreviewPage;
import com.tle.webtests.pageobject.searching.EditMetadataDialog;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.DisplayNodesPage;
import com.tle.webtests.pageobject.viewitem.ItemXmlPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("manageresources")
public class BulkMetadataEditTest extends AbstractCleanupTest
{
	private final String COLLECTION = "Metadata testing collection";
	private final String SCHEMA = "Control Test Schema";
	private final String SET_ALWAYS = "setalways";
	private final String SET_EXISTS = "setexists";
	private final String SET_NOT_EXIST = "setnotexist";
	private final String ADD_NODE = "<added><child>findme</child></added>";
	private final String DESCRIPTION_1 = "ghost";
	private final String DESCRIPTION_2 = "you are a bad host";
	private final String FIND = "host";
	private final String REPLACE = "oats";

	@Name("item 1")
	private static PrefixedName ITEM_1;
	@Name("item 2")
	private static PrefixedName ITEM_2;

	@Test
	public void testBulkMetadataEdit()
	{
		logon("autotest", "automated");

		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION);
		wizard.editbox(1, ITEM_1);
		wizard.editbox(2, DESCRIPTION_1);
		wizard.selectDropDown(3, "2");
		wizard.setCheck(4, "a", true);
		wizard.save().publish();
		wizard = new ContributePage(context).load().openWizard(COLLECTION);
		wizard.editbox(1, ITEM_2);
		wizard.editbox(2, DESCRIPTION_2);
		wizard.save().publish();

		ItemAdminPage manageResources = new ItemAdminPage(context).load();
		manageResources.search(namePrefix);
		EditMetadataDialog dialog = manageResources.bulk().selectAll().editMetadata();
		// find/replace
		dialog.addModification().selectSchema(SCHEMA).selectNode("description").chooseAction()
			.findAndReplace(FIND, REPLACE);
		// set
		dialog.addModification().selectSchema(SCHEMA).selectNode("listbox").chooseAction().setText(SET_EXISTS, 2);
		dialog.addModification().selectSchema(SCHEMA).selectNode("listbox").chooseAction().setText(SET_NOT_EXIST, 3);
		dialog.addModification().selectSchema(SCHEMA).selectRepeatedNode("value", 1).chooseAction()
			.setText(SET_ALWAYS, 1);
		// add
		dialog.addModification().selectSchema(SCHEMA).selectNode("control").chooseAction().addNode(ADD_NODE);
		// preview
		BulkPreviewPage preview = dialog.preview();
		assertTrue(preview.getNodeContents("description").equals("goats"));
		assertTrue(preview.getNodeContents("listbox").equals(SET_EXISTS));
		assertTrue(preview.getNodeContents("value").equals(SET_ALWAYS));
		assertTrue(preview.getNodeContents("child").equals("findme"));
		preview.execute().waitAndFinish(manageResources);

		// item 1
		SummaryPage item = SearchPage.searchAndView(context, ITEM_1.toString());
		DisplayNodesPage nodes = item.displayNodes();
		assertEquals(nodes.getTextByName("description"), DESCRIPTION_1.replace(FIND, REPLACE));
		assertEquals(nodes.getTextByName("listbox"), SET_EXISTS);
		assertEquals(nodes.getTextByName("checkbox"), SET_ALWAYS);
		// item 2
		item = SearchPage.searchAndView(context, ITEM_2.toString());
		nodes = item.displayNodes();
		assertEquals(nodes.getTextByName("description"), DESCRIPTION_2.replace(FIND, REPLACE));
		assertEquals(nodes.getTextByName("listbox"), SET_NOT_EXIST);
		assertEquals(nodes.getTextByName("checkbox"), SET_ALWAYS);
		ItemXmlPage xml = item.itemXml();
		assertTrue(xml.nodeHasValue("item/control/added/child", "findme"));
		context.getDriver().navigate().back();
		logout();
	}

	@Test(dependsOnMethods = {"testBulkMetadataEdit"})
	public void testActions()
	{
		logon("autotest", "automated");
		ItemAdminPage manageResources = new ItemAdminPage(context).load();
		manageResources.search(namePrefix);
		EditMetadataDialog dialog = manageResources.bulk().selectAll().editMetadata();
		dialog.addModification();
		dialog.chooseActionNoNodes();
		dialog.selectSchema(SCHEMA);
		dialog.selectNode("listbox");
		dialog.chooseAction();
		dialog.findAndReplace("initial", "overwrite");
		dialog.addModification();
		dialog.selectSchema(SCHEMA);
		dialog.selectNode("listbox");
		dialog.selectNode("description");
		dialog.chooseAction();
		dialog.setText("initial", 1);
		// action edit
		dialog.editAction(2).chooseAction().setText(" - edited", 1);
		// action delete
		dialog.addModification().selectSchema(SCHEMA).selectNode("listbox").chooseAction().setText("delete", 1);
		dialog.deleteModification(3);
		// action ordering
		dialog.changeActionOrder(2, true);
		dialog.execute().waitAndFinish(manageResources);

		SummaryPage item = SearchPage.searchAndView(context, ITEM_1.toString());
		DisplayNodesPage nodes = item.displayNodes();
		assertEquals(nodes.getTextByName("description"), "initial - edited");
		assertEquals(nodes.getTextByName("listbox"), "overwrite - edited");

	}

}
