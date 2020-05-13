package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.HomePage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public abstract class ItemPage<T extends ItemPage<T>> extends AbstractPage<T> {
  private static final String ITEM_DETAILS_XPATH =
      "id('col2')/h3[text()='Details']/following-sibling::div[1]";

  public ItemPage(PageContext context) {
    super(context);
  }

  public String getOwner() {
    return driver.findElement(getOwnerXPath()).getAttribute("title");
  }

  private By getOwnerXPath() {
    return By.xpath(ITEM_DETAILS_XPATH + "/ul/li[contains(text(), 'Owner:')]/span");
  }

  // Check the Owner Existing

  public boolean isOwnerExisting() {
    return isPresent(getOwnerXPath());
  }

  // Check the Collaborators Existing

  public boolean isCollaboratorsExisting() {
    return isPresent(getCollaboratorsXPATH());
  }

  public List<String> getCollaborators() {
    List<String> rv = new ArrayList<String>();
    for (WebElement we : driver.findElements(getCollaboratorsXPATH())) {
      rv.add(we.getAttribute("title"));
    }
    return rv;
  }

  private By getCollaboratorsXPATH() {
    return By.xpath(ITEM_DETAILS_XPATH + "/ul/li[contains(text(), 'Collaborators:')]/span");
  }

  // Check the collection existence
  public boolean isCollectionExisting() {
    return isPresent(getCollectionXPath());
  }

  public String getCollection() {
    WebElement element = driver.findElement(getCollectionXPath());
    return element.getText();
  }

  private By getCollectionXPath() {
    return By.xpath(ITEM_DETAILS_XPATH + "/ul/li[contains(text(), 'Collection:')]/a");
  }

  public SearchPage clickCollection() {
    WebElement element = driver.findElement(getCollectionXPath());
    element.click();
    return new SearchPage(context).get();
  }

  public ItemId getItemId() {
    String currentUrl = driver.getCurrentUrl();
    String itemsUrl = currentUrl.substring(context.getBaseUrl().length());
    Pattern pattern = Pattern.compile(".*(items|gen|bb)/([\\w-]*)/(\\d*)/.*");
    Matcher matcher = pattern.matcher(itemsUrl);
    if (!matcher.matches()) {
      throw new Error("Wrong url:" + currentUrl);
    }
    return new ItemId(matcher.group(2), Integer.parseInt(matcher.group(3)));
  }

  public ItemId getItemIdFromAttachment() {
    String currentUrl = driver.getCurrentUrl();
    String itemsUrl = currentUrl.substring(context.getBaseUrl().length());
    Pattern pattern = Pattern.compile("file/([\\w-]*)/(\\d*)/.*");
    Matcher matcher = pattern.matcher(itemsUrl);
    if (!matcher.matches()) {
      throw new Error("Wrong url:" + currentUrl);
    }
    return new ItemId(matcher.group(1), Integer.parseInt(matcher.group(2)));
  }

  public WizardPageTab edit() {
    return clickAction("Edit this version", new WizardPageTab(context, 0));
  }

  // for testing collections that don't start you at the first page
  public WizardPageTab edit(int pageNum) {
    return clickAction("Edit this version", new WizardPageTab(context, pageNum));
  }

  public ModerationHistoryPage history() {
    return clickAction("Moderation history", new ModerationHistoryPage(context));
  }

  private By getActionBy(String actionText) {
    return By.xpath("//a[normalize-space(text())=" + quoteXPath(actionText) + "]");
  }

  public boolean hasAction(String actionText) {
    return isPresent(getActionBy(actionText));
  }

  private void clickActionOnly(String action) {
    driver.findElement(getActionBy(action)).click();
  }

  protected <P extends PageObject> P clickAction(String action, WaitingPageObject<P> returnTo) {
    clickActionOnly(action);
    return returnTo.get();
  }

  protected <P extends PageObject> P clickActionAccept(
      String action, WaitingPageObject<P> returnTo) {
    clickActionOnly(action);
    acceptConfirmation();
    return returnTo.get();
  }

  public WizardPageTab redraft() {
    return clickAction("Redraft this version", new WizardPageTab(context, 0));
  }

  public T delete() {
    return clickActionAccept("Delete this version", updateWaiter());
  }

  public T archive() {
    return clickAction("Archive this version", updateWaiter());
  }

  public T suspend() {
    return clickActionAccept("Suspend this version", updateWaiter());
  }

  public HomePage purge() {
    return clickActionAccept("Purge from openEQUELLA", new HomePage(context));
  }

  public T review() {
    return clickAction("Mark this for review", updateWaiter());
  }

  public WizardPageTab newVersion() {
    return clickAction("Create a new version", new WizardPageTab(context, 0));
  }

  public String newVersionFail() {
    clickActionOnly("Create a new version");
    Alert alert = getWaiter().until(ExpectedConditions.alertIsPresent());
    String text = alert.getText();
    alert.accept();
    return text;
  }

  public MoveCloneDialog cloneAction() {
    return clickAction("Clone item into a collection", new MoveCloneDialog(context));
  }

  public MoveCloneDialog move(WaitingPageObject<MoveCloneDialog> next) {
    return clickAction("Move item into another collection", next);
  }

  public MoveCloneDialog move() {
    return move(new MoveCloneDialog(context));
  }

  public ModifyKeyResourcePage modifyKeyResource() {
    return clickAction("Modify key resource", new ModifyKeyResourcePage(context));
  }

  public LMSExportPage lmsPage() {
    return clickAction("Add to external system", new LMSExportPage(context));
  }

  public FindUsesPage findUsesPage() {
    return clickAction("Find uses", new FindUsesPage(context));
  }

  public ExportItemPage exportPage() {
    return clickAction("Export", new ExportItemPage(context));
  }

  public TermsOfUsePage termsOfUsePage() {
    return clickAction("Terms of use", new TermsOfUsePage(context));
  }

  public boolean canChangeOwnership() {
    return hasAction("Change ownership");
  }

  public boolean canModifyKeyResource() {
    return hasAction("Modify key resource");
  }

  public ChangeOwnershipPage changeOwnership() {
    return clickAction("Change ownership", new ChangeOwnershipPage(context));
  }

  public ItemXmlPage itemXml() {
    ItemId itemId = getItemId();
    return new ItemXmlPage(context, itemId).load();
  }

  public ItemUrlPage tilde() {
    return new ItemUrlPage(context, getItemId()).viewRoot();
  }

  public SummaryPage summary() {
    driver.findElement(By.xpath("id('breadcrumbs')//a[2]")).click();
    return new SummaryPage(context).get();
  }

  public boolean hasMeta(String name, String content) {
    return isPresent(
        By.xpath(
            "//head/meta[@name="
                + quoteXPath(name)
                + " and @content="
                + quoteXPath(content)
                + "]"));
  }

  public String getTitle() {
    return driver.getTitle();
  }

  // eCommerece Item Summary Verifcation Method
  public boolean isPricingTierTitlePresent(String pricingTierTitle) {
    return driver
        .findElement(By.xpath("//h3[text()=" + quoteXPath(pricingTierTitle) + ']'))
        .isDisplayed();
  }

  // eCommerece Item Summary Verifcation Method
  public boolean isPricingTierPresent(String pricingTier) {
    return isPresent(
        By.xpath("//div[@class='tiers']//tbody//td[text() = " + quoteXPath(pricingTier) + "]"));
  }

  // eCommerece Item Summary Verifcation Method
  public boolean isSubscripitonTierTablePresent() {
    return driver.findElement(By.id("sc_ps_subt")).isDisplayed();
  }

  // eCommerece Item Summary Verifcation Method
  private String xpathForRow(int i) {
    return ".//tbody/tr[contains(@class, 'odd') or contains(@class, 'even')][" + i + "]";
  }

  // eCommerece Item Summary Verifcation Method
  public String verifyPricingTierAddedAtIndex(int i) {
    return driver
        .findElement(By.id("sc_ps_subt"))
        .findElement(By.xpath(xpathForRow(i) + "/td[1]"))
        .getText();
  }

  // eCommerece Item Summary Verifcation Method - Verify Purchase Tier Value
  // Added - Dinuk
  public String verifyPricingTierValueAddedAtIndex(int rowindex, int columnindex) {
    Integer col = new Integer(columnindex);
    String columnid = col.toString();
    return driver
        .findElement(By.id("sc_ps_subt"))
        .findElement(By.xpath(xpathForRow(rowindex) + "/td[" + columnid + "]"))
        .getText();
  }

  public VersionsPage clickShowAllVersion() {
    driver.findElement(By.xpath("//a[@id='idg_sv']")).click();
    return new VersionsPage(context).get();
  }

  public T unlockItem() {
    WaitingPageObject<T> updateWaiter = updateWaiter();
    driver.findElement(By.xpath("//button[text()='Discard editing']")).click();
    acceptConfirmation();
    return updateWaiter.get();
  }

  public T unlockAfterLogout() {
    WaitingPageObject<T> updateWaiter = updateWaiter();
    driver.findElement(By.xpath("//button[text()='Unlock']")).click();
    acceptConfirmation();
    return updateWaiter.get();
  }

  public WizardPageTab resumeItem() {
    driver.findElement(By.xpath("//button[text()='Resume editing']")).click();
    acceptConfirmation();
    return new WizardPageTab(context, 0).get();
  }
}
