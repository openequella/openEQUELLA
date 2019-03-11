package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.myresources.AbstractAuthorWebPage;
import com.tle.webtests.pageobject.myresources.MyResourcesPage;
import com.tle.webtests.pageobject.selection.SelectionSession;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class WebPagesUniversalControlType
    extends AbstractUniversalControlType<WebPagesUniversalControlType> {
  private WebElement getMainDiv() {
    return byDialogXPath("//div[contains(@class,'mypagesHandler')]");
  }

  private WebElement getAddScrap() {
    return byDialogXPath("//a[text()='Import pages from scrapbook']");
  }

  public WebElement getNameField() {
    return byWizId("_dialog_mphmmpe_pageNameField");
  }

  protected WebElement getPreviewCheckBox() {
    return byWizId("_dialog_mphmmpp_previewCheckBox");
  }

  public WebPagesUniversalControlType(UniversalControl universalControl) {
    super(universalControl);
  }

  @Override
  public WebElement getFindElement() {
    return getMainDiv();
  }

  @Override
  public String getType() {
    return "Web pages";
  }

  public UniversalControl addPage(String title, String content) {
    return openPage(title, content, false).add(title);
  }

  public WebPagesUniversalControlType openPage(String title, String content, boolean edit) {
    AuthorWebPage webPage =
        new AuthorWebPage(context, page.subComponentId(ctrlnum, "dialog_mphm"), edit).get();
    webPage.addPage(title, content);
    return this;
  }

  public UniversalControl importPageFromScrapbook(String description, String pageAttachment) {
    getAddScrap().click();

    SelectionSession selectionSession =
        ExpectWaiter.waiter(
                ExpectedConditions.frameToBeAvailableAndSwitchToIt("scrapiframe"),
                new SelectionSession(context))
            .get();
    new MyResourcesPage(context, "scrapbook")
        .results()
        .getResultForTitle(description, 1)
        .setChecked(true);
    selectionSession.finishedSelecting(this);
    return add(pageAttachment);
  }

  public UniversalControl add(String newPage) {
    WaitingPageObject<UniversalControl> waiter = control.attachNameWaiter(newPage, false);
    getAddButton().click();
    return waiter.get();
  }

  public UniversalControl replace(String newPage) {
    WaitingPageObject<UniversalControl> waiter = control.attachNameWaiter(newPage, false);
    driver.findElement(buttonBy(BUTTON_REPLACE)).click();
    return waiter.get();
  }

  public WebPagesUniversalControlType setPreview(boolean b) {
    if (getPreviewCheckBox().isSelected() != b) {
      getPreviewCheckBox().click();
    }
    return this;
  }

  public static class AuthorWebPage extends AbstractAuthorWebPage<AuthorWebPage> {

    public AuthorWebPage(PageContext context, String baseId, boolean edit) {
      super(context, baseId, edit);
    }

    @Override
    protected ExpectedCondition<?> getAddCondition() {
      return ExpectedConditions2.updateOfElement(getAddButton());
    }
  }
}
