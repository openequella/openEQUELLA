package com.tle.webtests.test.searching;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.Name;
import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.SettingsPage;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.ContributePage;
import com.tle.webtests.pageobject.wizard.SelectThumbnailDialog;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import com.tle.webtests.pageobject.wizard.controls.universal.FileUniversalControlType;
import com.tle.webtests.test.AbstractCleanupTest;
import com.tle.webtests.test.files.Attachments;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@TestInstitution("workflow")
public class GallerySearchTest extends AbstractCleanupTest {
  @Name("No images")
  private static PrefixedName NO_IMAGES;

  @Name("No thumb")
  private static PrefixedName NO_THUMB;

  @Name("Non image thumb")
  private static PrefixedName NON_IMAGE_THUMB;

  @Name("Image thumb")
  private static PrefixedName IMAGE_THUMB;

  @Name("Image default")
  private static PrefixedName IMAGE_DEFAULT;

  @Name("Suppressed")
  private static PrefixedName SUPPRESSED_THUMBS;

  @Test(enabled = false)
  public void testManualDataFix() {
    logon("admin", "``````");
    SearchPage searchPage = new SearchPage(context).load();
    searchPage.search("titration");
    searchPage.setResultType("images");
    assertFalse(searchPage.hasResults());
    logout();
    logon("TLE_ADMINISTRATOR", testConfig.getAdminPassword());
    new SettingsPage(context).load().maualDataFixPage().generateMissingThumnails();
    logout();
    logon("admin", "``````");
    searchPage = new SearchPage(context).load();
    searchPage.search("titration");
    searchPage.setResultType("images");
    assertTrue(searchPage.hasResults());
  }

  private static enum ThumbOptions {
    DEFAULT,
    NONE,
    CUSTOM_IMAGE,
    CUSTOM_PDF;
  }

  @DataProvider(name = "imageItems")
  public Object[][] imageItems() {
    return new Object[][] {
      // NO IMAGES - NO SHOW
      {NO_IMAGES, false, ThumbOptions.DEFAULT, false},
      // IMAGE W/NO THUMB - NO SHOW
      {NO_THUMB, true, ThumbOptions.NONE, false},
      // IMAGE W/NON-IMAGE THUMB - NO SHOW
      {NON_IMAGE_THUMB, true, ThumbOptions.CUSTOM_PDF, false},
      // IMAGE W/SELECTED IMAGE THUMB - SHOW
      {IMAGE_THUMB, true, ThumbOptions.CUSTOM_IMAGE, true},
      // IMAGE W/DEFAULT THUMB - SHOW
      {IMAGE_DEFAULT, true, ThumbOptions.DEFAULT, true}
    };
  }

  @Test(dataProvider = "imageItems")
  public void testShowConditions(
      PrefixedName itemName, boolean image, ThumbOptions thumb, boolean shouldExist) {
    logon("admin", "``````");
    createGalleryTestItem(itemName, image, thumb);
    SearchPage search = new SearchPage(context).load().setResultType("images");
    if (shouldExist) {
      search.exactQuery(itemName);
    } else {
      search.exactQueryExpectNothing(itemName);
    }
    assertEquals(
        shouldExist,
        search.hasResults(),
        shouldExist
            ? "Should have results for this item: " + itemName
            : "There shouldn't be results for this item: " + itemName);
  }

  private void createGalleryTestItem(PrefixedName itemName, boolean image, ThumbOptions thumb) {
    WizardPageTab wizard = new ContributePage(context).load().openWizard("No Workflow");
    wizard.editbox(1, itemName);
    if (image) {
      wizard.addSingleFile(3, Attachments.get("veronicas_wall1.jpg"));
    }
    wizard.addSingleFile(3, Attachments.get("Track 2014 Goals_final.pdf"));
    SelectThumbnailDialog thumbnail = wizard.openSelectThumbnailDialog();
    switch (thumb) {
      case DEFAULT:
        thumbnail.selectDefault();
        break;
      case NONE:
        thumbnail.selectNone();
        break;
      case CUSTOM_IMAGE:
        thumbnail.selectCustom();
        break;
      case CUSTOM_PDF:
        thumbnail.selectCustomThumbnail("Track 2014 Goals_final.pdf");
        break;
    }

    thumbnail.saveDialog(wizard);

    wizard.save().publish();
  }

  @Test
  private void testSuppressThumbnails() {
    // TODO test default option
    logon("admin", "``````");
    WizardPageTab wizard = new ContributePage(context).load().openWizard("No Workflow");
    wizard.editbox(1, SUPPRESSED_THUMBS);
    wizard.addSingleFile(3, Attachments.get("veronicas_wall1.jpg"));
    wizard.save().publish();
    final SearchPage search = new SearchPage(context).load().setResultType("images");
    search.exactQuery(SUPPRESSED_THUMBS);
    assertTrue(search.hasResults());
    // Ensure the result type is really changed to "standard"
    search
        .getWaiter()
        .until(
            webDriver -> {
              search.setResultType("standard");
              return search.isResultType("standard");
            });
    wizard = SearchPage.searchAndView(context, SUPPRESSED_THUMBS.toString()).edit();
    UniversalControl control = wizard.universalControl(3);
    control
        .editResource(new FileUniversalControlType(control), "veronicas_wall1.jpg")
        .setThubmnailSuppress(true)
        .save();
    wizard.saveNoConfirm();
    search.load().setResultType("images");
    search.exactQuery(SUPPRESSED_THUMBS);
    assertFalse(search.hasResults());
  }
}
