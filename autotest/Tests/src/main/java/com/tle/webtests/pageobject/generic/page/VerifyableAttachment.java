package com.tle.webtests.pageobject.generic.page;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;

public class VerifyableAttachment extends AbstractPage<VerifyableAttachment> {
  public VerifyableAttachment(PageContext context) {
    super(context, By.xpath("//p[text()='This is a verifiable attachment']"));
  }

  public VerifyableAttachment(PageContext context, String bodyText) {
    super(context, By.xpath("//body//*[contains(text(), " + quoteXPath(bodyText) + ")]"));
  }

  public boolean isVerified() {
    return isPresent(loadedBy);
  }
}
