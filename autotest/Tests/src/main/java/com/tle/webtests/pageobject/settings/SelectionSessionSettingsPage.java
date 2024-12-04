package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.Assert;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SelectionSessionSettingsPage extends AbstractPage<SelectionSessionSettingsPage> {
  public static final String SELECTION_SESSIONS_HEADER =
      "Quick contribute"; // original property key: quickcontribute.title
  public static final String VERSION_VIEW_OPTIONS_PREFIX =
      "_versionViewOptions"; // individual ids: _versionViewOptions_0, etc

  @FindBy(id = "_fins")
  private WebElement collectionDropDown;

  @FindBy(id = "_saveButton")
  private WebElement saveButton;

  @FindBy(id = "_e")
  private WebElement disableCheckBox;

  public SelectionSessionSettingsPage(PageContext context) {
    super(context, By.xpath("//h2[text()=" + quoteXPath(SELECTION_SESSIONS_HEADER) + ']'));
  }

  public String getSelectedCollection() {
    EquellaSelect collectionList = new EquellaSelect(context, collectionDropDown);
    // value is the collection guid uuid, text is the name.
    return collectionList.getSelectedText();
  }

  public void selectCollectionByIndex(int index) {
    EquellaSelect collectionList = new EquellaSelect(context, collectionDropDown);
    collectionList.selectByIndex(index);
  }

  /**
   * getSelectableHyperlinks initiates a clickOn() but leaves the drop down open, so we close the
   * dropdown again before returning.
   *
   * @return
   */
  public int countAvailableCollections() {
    EquellaSelect collectionList = new EquellaSelect(context, collectionDropDown);
    int numLinks = collectionList.getSelectableHyperinks().size();
    collectionList.clickOn();
    return numLinks;
  }

  public int getVersionViewOptionsSize() {
    return getVersionViewOptions().size();
  }

  public List<WebElement> getVersionViewOptions() {
    return driver.findElements(By.xpath(".//input[@name='" + VERSION_VIEW_OPTIONS_PREFIX + "']"));
  }

  /**
   * We allow for the possibility that there is no checked view option selected, but there cannot be
   * more than one.
   *
   * @return
   */
  private WebElement getCheckedViewOption() {
    List<WebElement> allChecked =
        driver.findElements(
            By.xpath(
                ".//input[@name='" + VERSION_VIEW_OPTIONS_PREFIX + "' and @checked='checked']"));
    int allCheckedSize = allChecked.size();
    Assert.assertTrue(
        allCheckedSize == 0 || allCheckedSize == 1,
        "Expected either one or none checked, but there are "
            + allCheckedSize
            + " View Options checked");
    if (allCheckedSize == 1) return allChecked.get(0);
    else return null;
  }

  public String getCheckedViewOptionValue() {
    WebElement checkedOption = getCheckedViewOption();
    if (checkedOption != null) return checkedOption.getAttribute("value");
    else return null;
  }

  public void selectVersionViewOptionByIndex(int index) {
    getVersionViewOptions().get(index).click();
    get();
  }

  public SelectionSessionSettingsPage save() {
    saveButton.click();
    return get();
  }

  public void selectDisableBox() {
    if (!disableCheckBox.isSelected()) {
      disableCheckBox.click();
    }
  }

  public void unselectDisableBox() {
    if (disableCheckBox.isSelected()) {
      disableCheckBox.click();
    }
  }
}
