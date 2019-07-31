package com.tle.webtests.pageobject.viewitem;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.cal.CALSummaryPage;
import com.tle.webtests.pageobject.searching.FavouriteItemDialog;
import com.tle.webtests.pageobject.selection.SelectionCheckoutPage;
import com.tle.webtests.pageobject.selection.SelectionStatusPage;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SummaryPage extends ItemPage<SummaryPage> {
  @FindBy(id = "sis_button")
  private WebElement selectItem;

  @FindBy(id = "swol_button")
  private WebElement shareButton;

  @FindBy(xpath = "//div[@class='itemsummary-layout']")
  private WebElement summaryDiv;

  @FindBy(xpath = "//div[@class='selectitem']/button")
  private WebElement selectItemSummaryButton;

  public SummaryPage(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return summaryDiv;
  }

  public AttachmentsPage attachments() {
    return new AttachmentsPage(context).get();
  }

  public boolean hasAttachmentsSection() {
    return isPresent(By.className("attachments"));
  }

  public ModerationTab moderationTab() {
    clickSummaryLink("Moderation progress");
    return new ModerationTab(context).get();
  }

  // Access Moderation History
  public ModerationHistoryPage viewItemModerationHistory() {
    clickSummaryLink("Moderation history");
    return new ModerationHistoryPage(context).get();
  }

  public String getItemTitle() {
    return driver
        .findElement(
            By.xpath(
                "id('col1')//div[@class='item-title-and-desc']/h2[contains(@class, 'item-title')]"))
        .getText();
  }

  public String getItemDescription() {
    return driver.findElement(By.xpath("id('col1')//p[@class='item-description']")).getText();
  }

  public boolean hasActions() {
    return new AdminTabPage(context).isLoaded();
  }

  public AdminTabPage adminTab() {
    return new AdminTabPage(context).get();
  }

  public void clickSummaryLink(String title) {
    driver.findElement(summaryLinkBy(title)).click();
  }

  private By summaryLinkBy(String title) {
    return By.xpath(
        "//h3[text()='Details']/following-sibling::div[@id='adjacentuls']/ul/li/a[text()="
            + quoteXPath(title)
            + "]");
  }

  public CommentsSection commentsSection() {
    return new CommentsSection(context).get();
  }

  public boolean isSummaryLinkAvailable(String title) {
    return isPresent(summaryLinkBy(title));
  }

  public <T extends PageObject> T selectItem(WaitingPageObject<T> returnTo) {
    clickSelectItem();
    SelectionCheckoutPage checkout = new SelectionCheckoutPage(context).get();
    return checkout.returnSelection(returnTo);
  }

  public SummaryPage selectItemNoCheckout() {
    return clickAndRemove(selectItem);
  }

  public void selectItemSummary() {
    selectItemSummaryButton.click();
  }

  public boolean badgeIsADiv() {
    return driver.findElements(By.xpath("id('header-inner')/div[@class='badge']")).size() != 0;
  }

  public boolean bannerIsADiv() {
    return driver.findElements(By.xpath("id('header-inner')/div[@class='banner']")).size() != 0;
  }

  public <T extends PageObject> T finishSelecting(WaitingPageObject<T> returnTo) {
    return new SelectionStatusPage(context).get().finishSelections().returnSelection(returnTo);
  }

  public <T extends PageObject> T finishSelecting(WaitingPageObject<T> returnTo, String frame) {
    return new SelectionStatusPage(context)
        .get()
        .finishSelections()
        .returnSelection(frame, returnTo);
  }

  public <T extends PageObject> T finishSelecting(WaitingPageObject<T> returnTo, WebElement frame) {
    return new SelectionStatusPage(context)
        .get()
        .finishSelections()
        .returnSelection(returnTo, frame);
  }

  public <T extends PageObject> T selectMultipleItem(WaitingPageObject<T> returnTo) {
    clickSelectItem();
    return finishSelecting(returnTo);
  }

  public boolean selectItemPresent() {
    return isPresent(selectItem);
  }

  private void clickSelectItem() {
    selectItem.click();
  }

  public String getBreadcrumbs() {
    return driver.findElement(By.xpath("//div[@id='breadcrumbs']")).getText();
  }

  public DisplayNodesPage displayNodes() {
    return new DisplayNodesPage(context).get();
  }

  public ShareWithOthersPage share() {
    shareButton.click();
    return new ShareWithOthersPage(context).get();
  }

  public boolean isItemFree() {
    return hasTierType("Free");
  }

  public boolean hasPurchase() {
    return hasTierType("Purchase");
  }

  public boolean hasSubscription() {
    return hasTierType("Subscription");
  }

  private boolean hasTierType(String tierType) {
    final List<WebElement> tiers =
        driver.findElements(By.xpath("//div[contains(@class,'pricing')]/div/h4"));
    final int numberOfTiers = tiers.size();
    if (numberOfTiers == 3) {
      return true;
    } else if (numberOfTiers == 1 || numberOfTiers == 2) {
      for (WebElement tier : tiers) {
        if (tier.getText().equals(tierType)) {
          return true;
        }
      }
    }
    return false;
  }

  public CALSummaryPage cal() {
    return new CALSummaryPage(this).get();
  }

  /**
   * Given that scripts can customise the output in an arbitrary fashion, this method allows the
   * caller to pass in a n arbitrary class attribute name, and the text value of all (if any)
   * matching elements is accumulated. No conclusions may be drawn from the order, as empty
   * strings/null are ignored.
   *
   * @param classAttrib
   * @returnArrayList of String being the text values of matching elements if any.
   */
  public List<String> getValuesByCustomClass(String classAttrib) {
    List<String> vals = new ArrayList<String>();
    List<WebElement> elems = driver.findElements(By.className(classAttrib));
    for (WebElement elem : elems) {
      String elemText = elem.getText();
      if (!Check.isEmpty(elemText)) {
        vals.add(elemText);
      }
    }
    return vals;
  }

  public String loggedInUser() {
    return driver.findElement(By.id("temptopbar_editUserLink")).getText().toLowerCase();
  }

  public FavouriteItemDialog<SummaryPage> addToFavourites() {
    driver.findElement(By.xpath("//input[@value='Add to favourites']")).click();
    return new FavouriteItemDialog<SummaryPage>(context, updateWaiter()).get();
  }

  public boolean hasFavouriteOption() {
    return isPresent(By.xpath("//input[@value='Add to favourites']"));
  }

  public SummaryPage removeFavourite() {
    driver.findElement(By.xpath("//input[@value='Remove favourite']")).click();
    return ReceiptPage.waiter("Successfully removed from favourites", this).get();
  }

  public boolean isFavouriteItem() {
    return isPresent(By.xpath("//input[@value='Remove favourite']"));
  }

  public boolean isItemLocked() {
    return isPresent(By.xpath("//div[contains(@class,'unlock-group')]"));
  }

  public List<String> getValuesByCustomDisplay() {
    List<String> vals = new ArrayList<String>();

    List<WebElement> elems =
        driver.findElements(By.xpath("//div[contains(@class,'displayNodeFull')]//p[text()]"));

    for (WebElement elem : elems) {
      String elemText = elem.getText();
      if (!Check.isEmpty(elemText)) {
        vals.add(elemText);
      }
    }
    return vals;
  }

  public boolean hasCommentSection(String section) {
    return isPresent(By.id("comments-" + section));
  }
}
