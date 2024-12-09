package com.tle.webtests.pageobject.settings;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class HarvesterSkipDrmPage extends AbstractPage<HarvesterSkipDrmPage> {
  public static final String HARVESTERSKIPDRMSETTINGS_TITLE =
      "Harvester"; // original property is harvesterskipdrmsettings.title

  @FindBy(id = "sdk")
  private WebElement skipDrmCheckbox;

  @FindBy(id = "_saveButton")
  private WebElement save;

  public HarvesterSkipDrmPage(PageContext context) {
    super(context, By.xpath("//h2[text()=" + quoteXPath(HARVESTERSKIPDRMSETTINGS_TITLE) + ']'));
  }

  public boolean getSkipDrmChecked() {
    return skipDrmCheckbox.isSelected();
  }

  public void toggleSkipDrmChecked() {
    skipDrmCheckbox.click();
  }

  public void save() {
    save.click();
  }
}
