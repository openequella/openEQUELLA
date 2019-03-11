package com.tle.webtests.pageobject.viewitem;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.searching.BulkActionDialog;
import com.tle.webtests.pageobject.searching.BulkResultsPage;
import com.tle.webtests.pageobject.wizard.WizardPageTab;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MoveCloneDialog extends AbstractPage<MoveCloneDialog> {
  @FindBy(id = "scll_c")
  private WebElement targetCollection;

  @FindBy(id = "scll_proceedButton")
  private WebElement okButton;

  @FindBy(id = "scll_o_1")
  private WebElement noAttachmentsRadio;

  @FindBy(id = "scll_so_1")
  private WebElement submitItemsRadio;

  @FindBy(id = "scll_s")
  private WebElement transformationDropDown;

  @FindBy(id = "collectionOptions")
  private WebElement optionsDiv;

  private BulkActionDialog dialog;

  public MoveCloneDialog(BulkActionDialog dialog) {
    super(dialog.getContext(), By.xpath("//h3[text()='Select a collection']"));
    this.dialog = dialog;
  }

  public MoveCloneDialog(PageContext context) {
    super(context, By.id("scll_proceedButton"));
  }

  public void setTargetCollection(String target) {
    if (dialog == null) {
      WaitingPageObject<MoveCloneDialog> waiter =
          ajaxUpdateExpect(
              optionsDiv,
              optionsDiv.findElement(
                  By.xpath(".//h3[text()='Select a schema transform (optional)']")));
      new EquellaSelect(context, targetCollection).selectByVisibleText(target);
      waiter.get();
    } else {
      WaitingPageObject<MoveCloneDialog> waiter = ajaxUpdate(optionsDiv);
      new EquellaSelect(context, targetCollection).selectByVisibleText(target);
      waiter.get();
    }
  }

  public WizardPageTab execute() {
    okButton.click();
    return new WizardPageTab(context, 0).get();
  }

  public void setNoattachments(boolean noattachments) {
    if (noattachments) {
      noAttachmentsRadio.click();
    }
  }

  public void setTransformation(String transformName) {
    new EquellaSelect(context, transformationDropDown).selectByVisibleText(transformName);
  }

  public void setSubmitItems(boolean submit) {
    if (submit) {
      submitItemsRadio.click();
    }
  }

  public BulkResultsPage executeBulk() {
    dialog.execute();
    return new BulkResultsPage(context).get();
  }

  public String getBreadcrumbs() {
    return driver.findElement(By.xpath("//div[@id='breadcrumbs']")).getText();
  }
}
