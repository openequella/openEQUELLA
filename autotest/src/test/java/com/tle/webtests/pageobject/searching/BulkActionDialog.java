package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class BulkActionDialog extends AbstractPage<BulkActionDialog> {
  @FindBy(id = "bss_bulkDialog_operationList")
  private WebElement operationList;

  @FindBy(id = "bss_bulkDialog_okButton")
  private WebElement executeButton;

  @FindBy(id = "bss_bulkDialog_nextButton")
  private WebElement nextButton;

  @FindBy(id = "bss_bulkDialog_previewButton")
  private WebElement previewButton;

  @FindBy(id = "bss_bulkDialogfooter")
  private WebElement footer;

  public BulkActionDialog(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return footer;
  }

  public BulkActionDialog selectOp(String op) {
    EquellaSelect opSelect = new EquellaSelect(context, operationList);
    if (!opSelect.getSelectedValue().equals(op)) {
      WaitingPageObject<BulkActionDialog> updater = ajaxUpdate(footer);
      opSelect.selectByValue(op);
      return updater.get();
    }
    return this;
  }

  public BulkResultsPage execute() {
    visibilityWaiter(executeButton).get();
    executeButton.click();
    acceptConfirmation();
    return new BulkResultsPage(context).get();
  }

  public BulkPreviewPage preview() {
    visibilityWaiter(previewButton).get();
    previewButton.click();
    return new BulkPreviewPage(context).get();
  }

  public void next() {
    waitForElement(nextButton).click();
  }
}
