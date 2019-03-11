package com.tle.webtests.pageobject.myresources;

import com.tle.webtests.pageobject.ExpectedConditions2;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;

public class MyResourcesAuthorWebPage extends AbstractAuthorWebPage<MyResourcesAuthorWebPage> {
  @FindBy(xpath = "//input[contains(@class, 'action-button') and @value='Save']")
  private WebElement saveButton;

  @FindBy(xpath = "//div[@class='minoractions']/a[text()='Cancel']")
  private WebElement cancelButton;

  @FindBy(xpath = "//h2[text()='Author new web pages']")
  private WebElement titleElem;

  @FindBy(id = "pages-table-ajax")
  private WebElement ajaxElem;

  private MyResourcesPage myResourcesPage;

  public MyResourcesAuthorWebPage(MyResourcesPage myResourcesPage) {
    super(myResourcesPage.getContext(), "mpc");
    this.myResourcesPage = myResourcesPage;
  }

  @Override
  protected WebElement findLoadedElement() {
    return titleElem;
  }

  public MyResourcesPage save() {
    saveButton.click();
    return new MyResourcesPage(context, "scrapbook").get();
  }

  @Override
  protected ExpectedCondition<?> getAddCondition() {
    return ExpectedConditions2.ajaxUpdate(ajaxElem);
  }

  public MyResourcesPage cancel() {
    cancelButton.click();
    acceptConfirmation();
    return myResourcesPage.get();
  }
}
