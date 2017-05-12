package com.tle.webtests.test.viewing.actions;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import com.tle.webtests.pageobject.searching.BulkSection;
import com.tle.webtests.pageobject.searching.ItemAdminPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.viewitem.MoveCloneDialog;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;

public class CloneItemTest extends BaseCloneMove
{
	private static final int NUM_ITEMS = 2;

	@Test
	@SuppressWarnings("nls")
	public void testCloneToDraft()
	{
		logon("SimpleModerator", "``````");
		String origName = context.getFullName("Item");
		MoveCloneDialog cloneDialog = createItem(origName).adminTab().cloneAction();
		cloneDialog.setTargetCollection("Simple 1 Step");
		WizardPageTab wizard = cloneDialog.execute();
		String clonedName = context.getFullName("Cloned");
		wizard.editbox(1, clonedName);
		wizard.save().draft();

		ItemAdminPage adminPage = setupAdminPage();
		verifyItem(adminPage.load().exactQuery(origName, 1).viewFromTitle(origName), ORIGINAL_COLLECTION, true);
		verifyItem(adminPage.load().exactQuery(clonedName, 1).viewFromTitle(clonedName), "Simple 1 Step", true);
	}

	private void verifyItem(SummaryPage summary, String collectionName, boolean hasAttachment)
	{
		assertEquals(summary.getCollection(), collectionName);
		verifyAttachment(summary, hasAttachment);
	}

	@Test
	public void testBulkCloneNoAttach()
	{
		testBulk(true, "No Workflow", null); //$NON-NLS-1$
	}

	@Test
	public void testBulkClone()
	{
		testBulk(false, "No Workflow", null); //$NON-NLS-1$
	}

	@Test
	public void testBulkCloneSubmit()
	{
		testBulk(false, "No Workflow", null, true, true); //$NON-NLS-1$
	}

	@Test
	public void testBulkCloneSubmitWorkflow()
	{
		testBulk(false, "Simple 1 Step", null, true, false); //$NON-NLS-1$
	}

	@Test
	public void testBulkCloneNoAttachXSLT()
	{
		testBulk(true, "Different Schema", "Standard Schema"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Test
	public void testBulkCloneXSLT()
	{
		testBulk(false, "Different Schema", "Standard Schema"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private void testBulk(boolean noattachments, String newCollection, String transformName)
	{
		testBulk(noattachments, newCollection, transformName, false, false);
	}

	@SuppressWarnings("nls")
	private void testBulk(boolean noattachments, String newCollection, String transformName, boolean submit,
		boolean expectLive)
	{
		logon("SimpleModerator", "``````");

		for( int i = 1; i <= NUM_ITEMS; i++ )
		{
			createItem(context.getFullName("Item" + i));
		}
		ItemAdminPage itemAdminPage = setupAdminPage();
		itemAdminPage.exactQuery(context.getFullName(""));
		BulkSection bulk = itemAdminPage.bulk();
		MoveCloneDialog cloneDialog = bulk.selectAll().clone();

		cloneDialog.setNoattachments(noattachments);
		cloneDialog.setTargetCollection(newCollection);
		if( transformName != null )
		{
			cloneDialog.setTransformation(transformName);
		}
		cloneDialog.setSubmitItems(submit);
		assertTrue(cloneDialog.executeBulk().waitAndFinish(itemAdminPage));

		for( int i = 1; i <= NUM_ITEMS; i++ )
		{
			String itemName = context.getFullName("Item" + i);
			ItemSearchResult result = itemAdminPage.load().exactQuery(itemName, NUM_ITEMS)
				.getResultForTitle(itemName, 1);
			String statusText = result.getStatus();
			boolean draftStatus = statusText.equals("draft");
			boolean liveStatus = statusText.equals("live");
			boolean modStatus = statusText.equals("moderating");
			assertTrue((draftStatus && !submit) || (liveStatus && submit && expectLive)
				|| (modStatus && submit && !expectLive));

			verifyItem(result.viewSummary(), newCollection, !noattachments);
			verifyItem(itemAdminPage.load().exactQuery(itemName).getResultForTitle(itemName, 2).viewSummary(),
				ORIGINAL_COLLECTION, true);
		}
	}

}
