package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;

public class MoodleNoticePage<T extends PageObject> extends AbstractPage<T> {
  private WaitingPageObject<T> returnTo;

  public MoodleNoticePage(WaitingPageObject<T> returnTo) {
    this(returnTo, "Continue");
  }

  public MoodleNoticePage(WaitingPageObject<T> returnTo, String buttonText) {
    super(returnTo.getContext());
    this.returnTo = returnTo;
    if (StringUtils.isBlank(buttonText)) {
      loadedBy = By.xpath("id('notice')//input[@type='submit']");
    } else {
      loadedBy = By.xpath("//input[normalize-space(@value)=" + quoteXPath(buttonText) + "]");
    }
  }

  @Override
  protected T actualPage() {
    loadedElement.click();
    return returnTo.get();
  }
}
