package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class QuickContributeAndVersionPage extends AbstractPage<QuickContributeAndVersionPage> {

  @FindBy(id = "_fins")
  private WebElement collectionSelect;

  @FindBy(id = "_saveButton")
  private WebElement saveButton;

  public QuickContributeAndVersionPage(PageContext context) {
    super(context, By.xpath("//h2[contains(text(), 'Quick contribute')]"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "access/quickcontributeandversionsettings.do");
  }

  public QuickContributeAndVersionPage selectCollection(String collectionName) {
    EquellaSelect dropdown = new EquellaSelect(context, collectionSelect);
    dropdown.selectByVisibleText(collectionName);
    return this;
  }

  public String getCollection() {
    EquellaSelect dropdown = new EquellaSelect(context, collectionSelect);
    return dropdown.getSelectedText();
  }

  public boolean save() {
    saveButton.click();

    // Wait for a response to the save request
    final String confirmationDivXpath = "//div[@id='receipt-message']/span";
    waitForElement(By.xpath(confirmationDivXpath));

    return true;
  }
}
