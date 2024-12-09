package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.generic.component.SelectGroupDialog;
import com.tle.webtests.pageobject.generic.component.SelectUserDialog;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class DiagnosticsPage extends AbstractPage<DiagnosticsPage> {
  public static final String DIAGNOSTICS_PAGE_SETTINGS_SECTION_TITLE = "Diagnostics";

  @FindBy(id = "dul_userGroupsDialog_opener")
  private WebElement selectUser;

  @FindBy(id = "dul_selectGroupDialog_opener")
  private WebElement selectGroup;

  @FindBy(id = "usergroups")
  private WebElement userGroupsDiv;

  @FindBy(id = "groupmembers")
  private WebElement groupMembersDiv;

  public DiagnosticsPage(PageContext context) {
    super(
        context,
        By.xpath("//h2[text()=" + quoteXPath(DIAGNOSTICS_PAGE_SETTINGS_SECTION_TITLE) + ']'));
  }

  public void clickSelectUserButton() {
    selectUser.click();
  }

  public void clickSelectGroupButton() {
    selectGroup.click();
  }

  public void searchGivenUserName(String searchName, String userName) {
    this.clickSelectUserButton();

    ExpectedCondition<?> ajaxUpdate = ExpectedConditions2.ajaxUpdate(userGroupsDiv);
    SelectUserDialog dlg = new SelectUserDialog(context, "dul_userGroupsDialog").get();
    if (searchName != null) {
      dlg.search(searchName);
      dlg.select(userName);
    }
    dlg.finish(this);
    getWaiter().until(ajaxUpdate);
  }

  public void searchGivenGroupName(String searchName, String groupName) {
    this.clickSelectGroupButton();

    ExpectedCondition<?> ajaxUpdate = ExpectedConditions2.ajaxUpdate(groupMembersDiv);
    SelectGroupDialog dlg = new SelectGroupDialog(context, "dul_selectGroupDialog").get();
    if (searchName != null) {
      dlg.search(searchName);
      dlg.select(groupName);
    }
    dlg.finish(this);
    getWaiter().until(ajaxUpdate);
  }

  public boolean memberExists(String member) {
    return isPresent(By.xpath("//span[@title='" + member + "']"));
  }

  public boolean groupExists(String groupName) {
    return isPresent(By.xpath("//td[normalize-space(text())='" + groupName + "']"));
  }
}
