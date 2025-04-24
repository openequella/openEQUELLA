package com.tle.webtests.test.myresources.favourites;

import static org.testng.Assert.assertTrue;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.pageobject.IntegrationTesterPage;
import com.tle.webtests.pageobject.IntegrationTesterReturnPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import com.tle.webtests.test.AbstractIntegrationTest;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

@TestInstitution("myresources")
public class FavouritesInIntegrationTest extends AbstractIntegrationTest {
  /** Institution primed and saved with his sharedid & secret */
  private static String SHAREDID = "favininteg";

  private static String SECRET = "favininteg";
  private static String ACTION = "selectOrAdd";

  /** Institution, and AutoTest user, presumed to contain an item with this title as a favourite. */
  private static String FAVOURITE_PRESUMED_TO_EXIST = "Hal's eying Flickr";

  /**
   * Preserved in the exported contributions - tests/myresources/institution.tar.gz being the uuid
   * for the "Generic Testing Collection".
   */
  private static String GENERIC_TESTING_COLLECTIONS_ID_FROM_TEST_INSTITUTION =
      "ef19a911-0b3c-85ed-efd9-a56e56edcf37";

  public FavouritesInIntegrationTest() {
    setDeleteCredentials(AUTOTEST_LOGON, AUTOTEST_PASSWD);
  }

  @Test
  public void findFavourites() {
    IntegrationTesterPage itp = new IntegrationTesterPage(context, SHAREDID, SECRET).load();
    itp.setItemXml("");
    itp.setPowerXml("");

    // 3rd parameter for courseId not required here.
    String returnedUrl =
        itp.getSignonUrl(
            ACTION,
            AUTOTEST_LOGON,
            "",
            "contributionCollectionIds=" + GENERIC_TESTING_COLLECTIONS_ID_FROM_TEST_INSTITUTION);

    assertTrue(returnedUrl != null && returnedUrl.length() > 0);

    SelectionSession session = itp.clickPostToUrlButton(new SelectionSession(context));
    WebElement favContribSelectedBox = context.getDriver().findElement(By.id("srsh_box"));
    // We can expect Favourites to be already selected from the links 'Favourites | Contributed |
    // Selected',
    // but we may as well make sure
    boolean clicked = session.setRecentTab("Favourites");
    if (clicked) {
      session = new SelectionSession(context).get();
      favContribSelectedBox = context.getDriver().findElement(By.id("srsh_box"));
    }

    List<WebElement> linksToFavItems =
        favContribSelectedBox.findElements(By.xpath(".//div[@class='alt-links']/a"));
    assertTrue(linksToFavItems.size() > 0, "Expected to see some links to favourites");

    boolean foundExpectedFavItem = false;
    for (WebElement linkToFavItem : linksToFavItems) {
      String linkName = linkToFavItem.getText();
      if (linkName.equals(FAVOURITE_PRESUMED_TO_EXIST)) {
        foundExpectedFavItem = true;
        linkToFavItem.click();
        contributeFavourite();
        break;
      }
    }
    assertTrue(foundExpectedFavItem, "Expected to see '" + FAVOURITE_PRESUMED_TO_EXIST + "'");
  }

  private void contributeFavourite() {
    SummaryPage spat = new SummaryPage(context).get();
    IntegrationTesterReturnPage returnPage =
        spat.selectItem(new IntegrationTesterReturnPage(context));
    String itSays = returnPage.returnedRow("result");
    assertTrue(
        itSays.toLowerCase().contains("success"),
        "Instead of result = 'success', it says " + itSays);
  }
}
