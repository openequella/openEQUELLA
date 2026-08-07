package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.pagefactory.ByChained;

public class MoodleModAdminPage extends MoodleBasePage<MoodleModAdminPage> {

  public MoodleModAdminPage(PageContext context) {
    super(context, By.id("modules"));
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getIntegUrl() + "admin/modules.php");
  }

  public MoodleModAdminPage deleteModule(String name) {
    if (getMoodleVersion() >= 26) {
      driver.findElement(new ByChained(mod(name), By.xpath("td/a[text()='Uninstall']"))).click();
    } else {
      driver.findElement(new ByChained(mod(name), By.xpath("td/a[text()='Delete']"))).click();
    }

    ActivitiesPage activitiesPage =
        new MoodleNoticePage<ActivitiesPage>(new ActivitiesPage()).get();
    return activitiesPage.continueOn();
  }

  public MoodleEquellaSettingsPage equellaSettings() {
    driver
        .findElement(new ByChained(mod("EQUELLA Resource"), By.xpath("td/a[text()='Settings']")))
        .click();

    return new MoodleEquellaSettingsPage(context).get();
  }

  private By mod(String name) {
    String xpath = "//tr[td/span[normalize-space(text())=" + quoteXPath(name) + "]]";
    if (this.getMoodleVersion() >= 26) {
      xpath = "//tr[td[normalize-space(text())=" + quoteXPath(name) + "]]";
    }

    return By.xpath(xpath);
  }

  public boolean moduleExists(String name) {
    return isPresent(mod(name));
  }

  public class ActivitiesPage extends AbstractPage<ActivitiesPage> {
    @FindBy(xpath = "//h2[text()='Activities' or contains(text(), 'Uninstalling')]")
    private WebElement title;

    @FindBy(xpath = "//input[@value='Continue' or @type='submit']")
    private WebElement continueButton;

    @FindBy(xpath = "//input[@value='Cancel']")
    private WebElement cancelButton;

    public ActivitiesPage() {
      super(MoodleModAdminPage.this.context);
    }

    @Override
    protected WebElement findLoadedElement() {
      return title;
    }

    public MoodleModAdminPage continueOn() {
      // This shows up when using a git repo with correct permissions...
      // and it actually deletes all EQUELLA module files which is really
      // annoying
      if (isPresent(By.className("uninstalldeleteconfirmexternal"))) {
        cancelButton.click();
      } else {
        continueButton.click();
      }
      if (MoodleModAdminPage.this.getMoodleVersion() >= 26) {
        // moodle 2.6 directly takes you to upgrade screen
        return null;
      } else {
        return MoodleModAdminPage.this.get();
      }
    }
  }
}
