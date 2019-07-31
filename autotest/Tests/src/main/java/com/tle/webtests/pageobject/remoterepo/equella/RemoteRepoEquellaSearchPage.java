package com.tle.webtests.pageobject.remoterepo.equella;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class RemoteRepoEquellaSearchPage extends AbstractPage<RemoteRepoEquellaSearchPage> {
  @FindBy(id = "ers_selectionDialog_opener")
  private WebElement browseButton;

  @FindBy(id = "ers_downloadOptions_0")
  private WebElement metadataAndAttachmentsCheck;

  @FindBy(id = "ers_downloadOptions_1")
  private WebElement metadataCheck;

  @FindBy(id = "ers_startDownloadButton")
  private WebElement downloadButton;

  public RemoteRepoEquellaSearchPage(PageContext context) {
    super(context, By.xpath("//h2[text()='Import an EQUELLA resource']"));
  }

  public RemoteRepoEquellaSearchPage downloadAttachments(boolean attachments) {
    if (attachments) {
      metadataAndAttachmentsCheck.click();
    } else {
      metadataCheck.click();
    }
    return get();
  }

  public SelectionSession browse() {
    browseButton.click();
    return ExpectWaiter.waiter(
            ExpectedConditions2.frameToBeAvailableAndSwitchToIt(
                driver, By.xpath("id('reposession_container')/iframe")),
            new SelectionSession(context))
        .get();
  }

  public RemoteRepoEquellaSearchPage finished() {
    driver.switchTo().defaultContent();
    return get();
  }

  public WizardPageTab download() {
    downloadButton.click();
    return new WizardPageTab(context, 0).get();
  }
}
