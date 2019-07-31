package com.tle.webtests.pageobject.institution;

import com.google.common.base.Function;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.WebDriverWait;

public class MigrationProgressDialog extends AbstractPage<MigrationProgressDialog> {
  @FindBy(id = "isdt_progressDialog_ok")
  private WebElement okButton;

  @FindBy(className = "progress-curr-migration")
  private WebElement statusElement;

  private final WebDriverWait longWaiter;

  public MigrationProgressDialog(PageContext context) {
    super(context, By.className("progress-curr-migration"));
    longWaiter = new WebDriverWait(context.getDriver(), 180);
  }

  public void close() {
    okButton.click();
  }

  public void waitForFinish() {
    longWaiter.until(
        new Function<WebDriver, Boolean>() {
          @Override
          public Boolean apply(WebDriver driver) {
            return statusElement.getText().equals("Migrations complete");
          }
        });
  }
}
