package com.tle.webtests.test.viewing;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.Date;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.viewitem.DisplayNodesPage;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.ShuffleListControl;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("fiveo")
public class DisplayNodesTest extends AbstractCleanupTest
{

	private static final String ITEM_NAME = "Node Types";
	private static final String COLLECTION_NAME = "Display Nodes Collection";

	@Test
	public void testDisplayNodeTypes()
	{
		logon("AutoTest", "automated");
		WizardPageTab wizard = new ContributePage(context).load().openWizard(COLLECTION_NAME);
		wizard.editbox(1, context.getFullName(ITEM_NAME));
		wizard.editbox(2, "<b>BOLD</b>");
		ShuffleListControl list = wizard.shuffleList(3);
		list.add("value1");
		list.add("value2");
		wizard.calendar(5).setDate(new Date(0l));
		wizard.editbox(6, "http://google.com.au/");
		SummaryPage summary = wizard.save().publish();
		ItemId itemId = summary.getItemId();
		DisplayNodesPage displayNodes = summary.displayNodes();
		assertEquals(displayNodes.getTextByName("HTML"), "BOLD");
		assertEquals(displayNodes.getTextByName("Text"), "<b>BOLD</b>");
		assertEquals(displayNodes.getLinkTextByName("URL"), "http://google.com.au/");
		assertTrue(displayNodes.isHalfSizeByName("HTML"));
		assertTrue(displayNodes.isHalfSizeByName("Text"));
		assertEquals(displayNodes.getTextByName("Split"), "value1\"value2");
		assertEquals(displayNodes.getTextByName("Date"), "January 1, 1970");
		assertEquals(displayNodes.getTextByName("UUID"), itemId.getUuid());
	}
}
