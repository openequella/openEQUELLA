package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ActiveCachingPage extends AbstractPage<ActiveCachingPage> {
  public static final String ACTIVE_CACHING_TITLE =
      "Active caching"; // original property key: remotecaching.title
  public static final String EVERYONE_NODENAME =
      "Everyone"; // original property key: remotecaching.default.rootnode.name
  public static final String SETTINGS_SAVED_RECEIPT =
      "Active Caching settings saved successfully"; // original property: settings.save.receipt

  public static final String RECEIPT_MESSAGE_ID = "receipt-message"; // For verification of Save

  @FindBy(id = "_eu")
  private WebElement enableUseCheckbox;

  @FindBy(id = "_ut")
  private WebElement userTree;

  @FindBy(id = "_ag")
  private WebElement addGroupButton;

  @FindBy(id = "_r")
  private WebElement removeButton;

  @FindBy(id = "_s")
  private WebElement saveButton;

  @FindBy(id = "includesexcludesajaxdiv")
  private WebElement collectionsDiv;

  public ActiveCachingPage(PageContext context) {
    super(context, By.xpath("//h2[text()=" + quoteXPath(ACTIVE_CACHING_TITLE) + ']'));
  }

  public boolean getEnableUseChecked() {
    return enableUseCheckbox.isSelected();
  }

  public ActiveCachingPage setEnableUse(boolean setVal) {
    if (enableUseCheckbox.isSelected() != setVal) {
      WaitingPageObject<ActiveCachingPage> waiter;
      if (setVal) {
        waiter = visibilityWaiter(userTree);
      } else {
        waiter = removalWaiter(userTree);
      }
      enableUseCheckbox.click();
      return waiter.get();
    }
    return this;
  }

  public boolean userTreeVisible() {
    // any attempt to access a property, but may as well check actual viewable presence
    return isPresent(userTree);
  }

  public ActiveCacheTreeNode findRootNode() {
    return new ActiveCacheTreeNode(this, userTree, EVERYONE_NODENAME).get();
  }

  public ActiveCacheTreeNode findNamedNode(String nodeName) {
    return new ActiveCacheTreeNode(this, userTree, nodeName).get();
  }

  public boolean doesNodeExist(String nodeName) {
    return new ActiveCacheTreeNode(this, userTree, nodeName).isLoaded();
  }

  public AddGroupPage clickAddGroupButton() {
    addGroupButton.click();
    return new AddGroupPage(context).get();
  }

  public ActiveCachingPage clickRemoveButton() {
    removeButton.click();
    return get();
  }

  public ActiveCachingPage deleteNode(String nodeName) {
    if (doesNodeExist(nodeName)) {
      findNamedNode(nodeName).removeNode();
    }
    return get();
  }

  public boolean removeEnabled() {
    return removeButton.isEnabled();
  }

  /**
   * The remote caching service and the tree control reload can be slow. In Run mode (as opposed to
   * debug) we have to wait for a couple seconds before checking the receipt message.
   */
  public ActiveCachingPage save() {
    saveButton.click();
    return ReceiptPage.waiter(SETTINGS_SAVED_RECEIPT, this).get();
  }

  public WaitingPageObject<ActiveCachingPage> collectionsUpdate() {
    return ajaxUpdate(collectionsDiv);
  }
}
