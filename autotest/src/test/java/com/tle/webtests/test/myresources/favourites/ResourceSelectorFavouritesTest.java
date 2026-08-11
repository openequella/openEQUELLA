package com.tle.webtests.test.myresources.favourites;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.WaitingPageObject;
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
import io.github.openequella.pages.favourites.FavouritesPage;
import org.testng.annotations.Test;

@TestInstitution("myresources")
public class ResourceSelectorFavouritesTest extends AbstractCleanupAutoTest {
  private static String COLLECTIONS_NAME = "Generic Testing with EQUELLA resources";
  private static String ITEM_TITLE = "Angle";

  @Test
  public void addSelectedMyResourcesToCollection() {
    ContributePage contributePage = new ContributePage(context).load();
    WizardPageTab wizard = contributePage.openWizard(COLLECTIONS_NAME);
    wizard.editbox(1, context.getFullName(COLLECTIONS_NAME + "_item"));
    wizard.editbox(2, "Testing testing 14841, one, two ...");
    // Button finding and clicking done thusly. Add resource is 3rd control
    // on wizard page, hence 'p0c3_addLink'
    UniversalControl control = wizard.universalControl(3);
    ResourceUniversalControlType resource =
        control.addDefaultResource(new ResourceUniversalControlType(control));
    WaitingPageObject<UniversalControl> newAttachWaiter =
        control.attachNameWaiter(ITEM_TITLE, false);

    resource.getSelectionSession().clickShowAllFavourites();
    clickFavouriteItem(ITEM_TITLE);

    SummaryPage stp = new SummaryPage(context).get();
    assertTrue(stp.selectItemPresent(), "Expected summary page to be selectable");

    stp.selectItemNoCheckout();
    stp.finishSelecting(newAttachWaiter);

    ConfirmationDialog publishAndBeDamned = wizard.save();
    publishAndBeDamned.publish();
    SummaryPage postWrapUp = new SummaryPage(context).get();
    if (true) {
      assertTrue(
          postWrapUp.hasAttachmentsSection(),
          "expecting Attachments Section in final summary result");
      // find an attachment and click on it
      AttachmentsPage finalAttachmentsSection = postWrapUp.attachments();
      assertTrue(
          finalAttachmentsSection.attachmentCount() > 0,
          "expecting One or Attachments in final summary result");
      finalAttachmentsSection.viewAttachment(
          finalAttachmentsSection.attachmentOrder().get(0), postWrapUp);
    }
  }

  // Click item in favourites page.
  private void clickFavouriteItem(String itemTitle) {
    if (testConfig.isNewUI()) {
      FavouritesPage favourites = new FavouritesPage(context).get();
      favourites.selectItem(itemTitle);
    } else {
      ItemListPage favourites = new ItemListPage(context).get();
      ItemSearchResult aFavourite = favourites.getResultForTitle(itemTitle);
      favourites.scrollToElement(aFavourite.getLoadedElement());
      aFavourite.clickTitle();
    }
  }
}
