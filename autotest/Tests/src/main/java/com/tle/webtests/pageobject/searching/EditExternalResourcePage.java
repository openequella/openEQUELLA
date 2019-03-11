package com.tle.webtests.pageobject.searching;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class EditExternalResourcePage extends AbstractPage<EditExternalResourcePage> {

  private final ItemSearchResult result;

  @FindBy(id = "cmr_editDialog_nameField")
  private WebElement nameField;

  @FindBy(id = "cmr_editDialog_descriptionField")
  private WebElement descriptionField;

  @FindBy(id = "cmr_editDialog_ok")
  private WebElement saveButton;

  public EditExternalResourcePage(PageContext context, ItemSearchResult result) {
    super(context, By.id("cmr_editDialog"));
    this.result = result;
  }

  public EditExternalResourcePage setName(String name) {
    nameField.clear();
    nameField.sendKeys(name);
    return get();
  }

  public EditExternalResourcePage setDescription(String description) {
    descriptionField.clear();
    descriptionField.sendKeys(description);
    return get();
  }

  public ItemSearchResult save() {
    WaitingPageObject<ItemSearchResult> ajaxUpdate = result.ajaxUpdate(result.getLoadedElement());
    saveButton.click();
    return ajaxUpdate.get();
  }
}
