package com.tle.webtests.pageobject.integration.blackboard;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.URLUtils;
import com.tle.webtests.pageobject.AbstractPage;
import java.util.Map;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class BlackboardEditItemPage extends AbstractPage<BlackboardEditItemPage> {
  @FindBy(name = "name")
  private WebElement nameField;

  @FindBy(name = "description")
  private WebElement descriptionField;

  @FindBy(name = "bottom_Submit")
  private WebElement submitButton;

  @FindBy(className = "info")
  private WebElement link;

  private final BlackboardContentPage content;

  @FindBy(className = "backLink")
  private WebElement backLinkDiv;

  public BlackboardEditItemPage(PageContext context, BlackboardContentPage content) {
    super(context, BlackboardPageUtils.pageTitleBy("Modify Resource Content Object"));
    this.content = content;
  }

  public String getName() {
    return nameField.getAttribute("value");
  }

  public String getDescription() {
    return descriptionField.getAttribute("value");
  }

  public String getUrl() {
    Map<String, String[]> params =
        URLUtils.parseParamUrl(link.getAttribute("href"), context.getIntegUrl());
    return params.get("page")[0];
  }

  public BlackboardContentPage submit() {
    submitButton.click();
    waitForElement(backLinkDiv);
    backLinkDiv.findElement(By.xpath("./a")).click();
    return content.get();
  }
}
