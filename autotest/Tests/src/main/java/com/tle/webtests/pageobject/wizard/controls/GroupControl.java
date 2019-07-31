package com.tle.webtests.pageobject.wizard.controls;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.wizard.AbstractWizardControlPage;
import com.tle.webtests.pageobject.wizard.SubWizardPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class GroupControl extends AbstractWizardControl<GroupControl> {
  private final int ctrlOffset;
  private final int treeOffset;

  public GroupControl(
      PageContext context, AbstractWizardControlPage<?> page, int ctrlNum, int treeOffset) {
    super(context, ctrlNum, page);
    this.ctrlOffset = ctrlNum;
    this.treeOffset = treeOffset;
  }

  public SubWizardPage getGroupItem(int groupNum, int controlOffset) {
    return new SubWizardPage(context, page, treeOffset + groupNum, controlOffset + ctrlOffset);
  }

  private WebElement getCheckBox(String groupValue) {
    return driver.findElement(
        By.xpath(
            "//input[@name="
                + quoteXPath(page.subComponentId(ctrlnum, "selected"))
                + " and @value="
                + quoteXPath(groupValue)
                + "]"));
  }

  public void toggleGroup(String groupValue) {
    getCheckBox(groupValue).click();
  }

  public void setGroupEnabled(String groupValue, boolean checked) {
    WebElement checkBox = getCheckBox(groupValue);
    if (checkBox.isSelected() != checked) {
      checkBox.click();
    }
  }

  @Override
  protected WebElement findLoadedElement() {
    throw new Error("findLoadedElement not overridden for GroupControl");
  }

  public boolean isEnabled() {
    return getCheckBox("true").isSelected();
  }
}
