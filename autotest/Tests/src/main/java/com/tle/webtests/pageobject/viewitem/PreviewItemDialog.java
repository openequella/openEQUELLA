package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class PreviewItemDialog extends AbstractPage<PreviewItemDialog> {

  // Preview Item Pop-up
  public PreviewItemDialog(PageContext context) {
    super(context, By.xpath("//h3[text()='Preview']"));
  }

  // Access Moderation History Page in Preview Item Pop-up
  public SummaryPage currentItem() {
    return ExpectWaiter.waiter(
            ExpectedConditions.frameToBeAvailableAndSwitchToIt("itemContent"),
            new SummaryPage(context))
        .get();
  }
}
