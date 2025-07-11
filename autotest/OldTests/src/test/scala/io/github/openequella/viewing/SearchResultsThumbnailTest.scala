package io.github.openequella.viewing

import com.tle.webtests.framework.TestInstitution
import com.tle.webtests.test.AbstractSessionTest
import io.github.openequella.pages.search.NewSearchPage
import org.testng.annotations.Test
import org.testng.Assert.{assertEquals, assertTrue}
import testng.annotation.NewUIOnly

@TestInstitution("fiveo")
class SearchResultsThumbnailTest extends AbstractSessionTest {

  def assertThumbnail(itemName: String, expectedThumbnailAttUuid: String): Unit = {
    val searchPage: NewSearchPage = new NewSearchPage(context).load()
    searchPage.changeQuery(itemName)
    searchPage.waitForSearchCompleted(1)
    val thumbnailUrl: String       = searchPage.getItemThumbnail.get(0).getAttribute("src")
    val thumbnailAriaLabel: String = searchPage.getItemThumbnail.get(0).getAttribute("aria-label")
    val expectedAriaLabel: String  = "Provided Icon"
    assertTrue(
      thumbnailUrl.contains(expectedThumbnailAttUuid),
      s"Thumbnail URL should contain $expectedThumbnailAttUuid"
    )
    assertEquals(
      thumbnailAriaLabel,
      expectedAriaLabel,
      s"Aria label should be '$expectedAriaLabel'"
    )
  }

  @Test(description =
    "Should display the first viewable attachment as the thumbnail when the item is configured to show default thumbnail"
  )
  @NewUIOnly
  def showFirstViewableThumb(): Unit = {
    logon
    assertThumbnail("Testing Thumbnail with Default", "cb177c0e-41e1-4858-b1e5-68cb35a8be8b")
    logout
  }

  @Test(description =
    "Should display the selected thumbnail when the item is configured to show a selected thumbnail"
  )
  @NewUIOnly
  def showCustomThumb(): Unit = {
    logon
    assertThumbnail(
      "Testing Thumbnail with Custom selected",
      "22c848a5-07f9-4c28-9fd4-b75f40c4ecac"
    )
    logout
  }

  @Test(description =
    "Should skip restricted thumbnail and show first viewable attachment when user lacks permission to view restricted attachments"
  )
  @NewUIOnly
  def skipRestrictedThumbWithoutPermission(): Unit = {
    logon
    assertThumbnail(
      "Testing Thumbnail with Restricted attachment",
      "499e39a9-f27b-48ec-a89f-abbb03ce8e02"
    )
    logout
  }

  @Test(description =
    "Should display the selected restricted attachment as the thumbnail when the user has permission to view restricted attachments"
  )
  @NewUIOnly
  def showRestrictedThumbWithPermission(): Unit = {
    logon("CanViewRestricted", "``````")
    assertThumbnail(
      "Testing Thumbnail with Restricted attachment",
      "a6298219-0b5c-4e1b-b722-07a867fb3886"
    )
    logout
  }

  @Test(description = "Should show nothing when the item is configured to show no thumbnail")
  @NewUIOnly
  def showNoThumbnail(): Unit = {
    logon
    val searchPage: NewSearchPage = new NewSearchPage(context).load()
    searchPage.changeQuery("Testing Thumbnail with None thumbnail")
    searchPage.waitForSearchCompleted(1)
    val thumbnailAriaLabel: String = searchPage.getItemThumbnail.get(0).getAttribute("aria-label")
    assertEquals(thumbnailAriaLabel, "Empty Thumbnail")
    logout
  }

}
