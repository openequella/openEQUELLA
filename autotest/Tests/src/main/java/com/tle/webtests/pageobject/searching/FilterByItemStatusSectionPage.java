package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import org.openqa.selenium.By;
import org.openqa.selenium.NotFoundException;

public class FilterByItemStatusSectionPage extends AbstractPage<FilterByItemStatusSectionPage> {
  private EquellaSelect statusList;
  private AbstractResultList<?, ?> resultsPageObject;

  public FilterByItemStatusSectionPage(
      PageContext context, AbstractResultList<?, ?> resultsPageObject) {
    super(context, By.id("status"));
    setMustBeVisible(false);
    this.resultsPageObject = resultsPageObject;
  }

  @Override
  public void checkLoaded() throws NotFoundException {
    super.checkLoaded();
    statusList = new EquellaSelect(context, driver.findElement(loadedBy));
  }

  public void setItemStatusFilter(String status) {
    String selectedValue = statusList.getSelectedValue();

    if (!status.equals(selectedValue)) {
      WaitingPageObject<?> updateWaiter = resultsPageObject.getUpdateWaiter();
      statusList.selectByValue(status);
      updateWaiter.get();
    }
  }
}
