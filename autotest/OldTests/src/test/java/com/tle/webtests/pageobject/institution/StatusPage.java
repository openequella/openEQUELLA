package com.tle.webtests.pageobject.institution;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.time.Duration;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class StatusPage<T extends InstitutionTabInterface> extends AbstractPage<StatusPage<T>> {
  private By returnLinkBy = By.id("returnLink");

  private WebElement getReturnLink() {
    return driver.findElement(returnLinkBy);
  }

  @FindBy(id = "error-div")
  private WebElement errorText;

  @FindBy(id = "error-list")
  private WebElement errorContent;

  @FindBy(id = "downloadLink")
  private WebElement downloadLink;

  private final WaitingPageObject<T> tab;

  public StatusPage(PageContext context, WaitingPageObject<T> tab) {
    this(context, tab, 500);
  }

  public StatusPage(PageContext context, WaitingPageObject<T> tab, long timeout) {
    super(context, new WebDriverWait(context.getDriver(), Duration.ofSeconds(timeout)));
    mustBeVisible = false;
    this.tab = tab;
  }

  @Override
  protected WebElement findLoadedElement() {
    return getReturnLink();
  }

  public boolean waitForFinish() {
    waiter.until(
        ExpectedConditions.or(
            ExpectedConditions.elementToBeClickable(returnLinkBy),
            ExpectedConditions.visibilityOfElementLocated(By.id("error-div"))));
    List<WebElement> errors = driver.findElements(By.xpath("id('error-list')/li"));
    return errors.isEmpty();
  }

  public T back() {
    getReturnLink().click();
    return tab.get();
  }

  public String getDownloadLink() {
    return downloadLink.getAttribute("href");
  }

  public String getErrorText() {
    try {
      return errorContent.getText();
    } catch (Exception e) {
      return "An error occurred, check the resource center logs";
    }
  }
}
