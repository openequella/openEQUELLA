package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class FindUsesTable extends AbstractPage<FindUsesTable> {
  @FindBy(xpath = "//h3[text()='Where this resource is used']")
  private WebElement titleElement;

  @FindBy(id = "fuc_sv")
  private WebElement showAllVersions;

  @FindBy(id = "fuc_sa")
  private WebElement showUnavailable;

  private FindUsesPage usesPage;

  public FindUsesTable(FindUsesPage usesPage) {
    super(usesPage.getContext());
    this.usesPage = usesPage;
  }

  @Override
  protected WebElement findLoadedElement() {
    return titleElement;
  }

  public FindUsesPage showAllVersions(boolean on) {
    if (on != showAllVersions.isSelected()) {
      WaitingPageObject<FindUsesPage> updateTable = usesPage.updateTable();
      showAllVersions.click();
      updateTable.get();
    }
    return usesPage;
  }

  public FindUsesPage showArchived(boolean on) {
    if (on != showUnavailable.isSelected()) {
      WaitingPageObject<FindUsesPage> updateTable = usesPage.updateTable();
      showUnavailable.click();
      updateTable.get();
    }
    return usesPage;
  }
}
