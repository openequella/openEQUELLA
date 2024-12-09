package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class AddGroupPage extends AbstractPage<AddGroupPage> {
  public static final String ADD_GROUP_DIALOG_HEADER =
      "Create a grouping"; // original property key: addgrouping.dialog.title

  @FindBy(id = "_agd_groupingName")
  private WebElement groupNameTextbox;

  @FindBy(id = "_agd_ok")
  private WebElement okButton;

  public AddGroupPage(PageContext context) {
    super(context, By.xpath("//h3[text()='" + ADD_GROUP_DIALOG_HEADER + "']"));
  }

  public AddGroupPage setGroupname(String groupName) {
    this.ensureVisible(groupNameTextbox);
    groupNameTextbox.clear();
    groupNameTextbox.sendKeys(groupName);
    return get();
  }

  public ActiveCacheTreeNode ok(WaitingPageObject<ActiveCacheTreeNode> returnTo) {
    okButton.click();
    return returnTo.get();
  }

  public void okFailure() {
    okButton.click();
    acceptConfirmation();
  }
}
