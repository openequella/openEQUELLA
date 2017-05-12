package com.tle.webtests.test.myresources.favourites;

import static org.testng.Assert.assertTrue;

import java.util.List;

import org.testng.annotations.Test;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.searching.ItemListPage;
import com.tle.webtests.pageobject.searching.ItemSearchResult;
import com.tle.webtests.pageobject.viewitem.AttachmentsPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.pageobject.wizard.ConfirmationDialog;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.ResourceUniversalControlType;
import com.tle.webtests.test.AbstractCleanupAutoTest;

/**
 * Test Reference: http://time/DTEC/test/editTest.aspx?testId=14841
 * 
 * @author larry
 */
@TestInstitution("myresources")
public class ResourceSelectorFavouritesTest extends AbstractCleanupAutoTest
{
	private static String COLLECTIONS_NAME = "Generic Testing with EQUELLA resources";

	@Test
	public void addSelectedMyResourcesToCollection()
	{
		ContributePage contributePage = new ContributePage(context).load();
		WizardPageTab wizard = contributePage.openWizard(COLLECTIONS_NAME);
		wizard.editbox(1, context.getFullName(COLLECTIONS_NAME + "_item"));
		wizard.editbox(2, "Testing testing 14841, one, two ...");
		// Button finding and clicking done thusly. Add resource is 3rd control
		// on wizard page, hence 'p0c3_addLink'
		UniversalControl control = wizard.universalControl(3);
		ResourceUniversalControlType resource = control.addDefaultResource(new ResourceUniversalControlType(control));
		ItemListPage favourites = resource.getSelectionSession().getShowAllFavourites();
		List<ItemSearchResult> fs = favourites.getResults();
		int resultsSize = fs.size();
		assertTrue(resultsSize > 0, "Test cannot procede unless there's at least one favourite to select");
		// pick the second last (or the only if just one)
		int whichResultIndex = resultsSize > 1 ? resultsSize - 2 : 0;
		ItemSearchResult aFavourite = fs.get(whichResultIndex);
		aFavourite.clickTitle();
		SummaryPage stp = new SummaryPage(context).get();
		assertTrue(stp.selectItemPresent(), "Expected summary page to be selectable");
		// if this selected item has attachments, select from the attachments,
		// otherwise select the whole item
		boolean expectingAttachmentsInFinalResult = false;
		if( stp.hasAttachmentsSection() )
		{
			AttachmentsPage attachmentsSection = stp.attachments();
			int howManyAttachments = attachmentsSection.attachmentCount();
			List<String> attachmentTitles = attachmentsSection.attachmentOrder();
			// If there are attachments, select the first
			if( howManyAttachments >= 1 )
			{
				expectingAttachmentsInFinalResult = true;
				attachmentsSection.selectAttachment(attachmentTitles.get(0));
				// if there's 3 or more, also select the third
				if( howManyAttachments >= 3 )
					attachmentsSection.selectAttachment(attachmentTitles.get(2));
			}
			else
				// no attachments in section? select the whole item
				stp.selectItemNoCheckout();
		}
		else
		{
			stp.selectItemNoCheckout();
		}
		stp.finishSelecting(resource.editPage()).save();

		ConfirmationDialog publishAndBeDamned = wizard.save();
		publishAndBeDamned.publish();
		SummaryPage postWrapUp = new SummaryPage(context).get();
		if( true )
		{
			assertTrue(postWrapUp.hasAttachmentsSection(), "expecting Attachments Section in final summary result");
			// find an attachment and click on it
			AttachmentsPage finalAttachmentsSection = postWrapUp.attachments();
			assertTrue(finalAttachmentsSection.attachmentCount() > 0,
				"expecting One or Attachments in final summary result");
			finalAttachmentsSection.viewAttachment(finalAttachmentsSection.attachmentOrder().get(0), postWrapUp);
		}
	}
}
