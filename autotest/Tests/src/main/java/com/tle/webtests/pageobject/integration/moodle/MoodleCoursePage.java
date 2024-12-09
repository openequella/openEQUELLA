package com.tle.webtests.pageobject.integration.moodle;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.framework.factory.DontCache;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import java.util.Set;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class MoodleCoursePage extends MoodleBasePage<MoodleCoursePage> {
  private final String courseName;
  private String mainWindow;

  @FindBy(className = "course-content")
  private WebElement contentDiv;

  @FindBy(xpath = "id('add_block')//select")
  private WebElement addBlock;

  public MoodleCoursePage(PageContext context, String courseName) {
    super(context);
    this.courseName = courseName;
  }

  @Override
  protected WebElement findLoadedElement() {
    return contentDiv;
  }

  public MoodleCoursePage addItem(int week, String search) {
    return addItem(week, search, null);
  }

  private WebElement getSectionElement(int section) {
    WebElement sectionElement = contentDiv.findElement(By.id("section-" + section));
    return sectionElement;
  }

  private MoodleSelectionPage clickEquellaResource(int week) {
    WebElement sectionElement = getSectionElement(week);

    this.setActivityChooser(true);
    WebElement findElement = sectionElement.findElement(By.className("section-modchooser"));
    WebElement link =
        waitForElement(findElement, By.xpath(".//a/span[text()='Add an activity or resource']/.."));
    scrollIntoViewAndClick(waitForElement(link, By.tagName("span")));
    ActivityDialog activityDialog = new ActivityDialog().get();
    activityDialog.chooseEquella();

    return new MoodleSelectionPage(this).get();
  }

  public MoodleCoursePage addItem(int week, String search, String attachment) {
    return clickEquellaResource(week).addItem(week, search, attachment);
  }

  public MoodleCoursePage addPackage(int week, String search) {
    return clickEquellaResource(week).addPackage(week, search);
  }

  public MoodleCoursePage addItemFromSearchResult(int week, String search) {
    return clickEquellaResource(week).addItemFromSearchResult(week, search);
  }

  public boolean hasResource(int section, String name) {
    return !getSectionElement(section)
        .findElements(By.xpath(xpathForResource(name) + "/a/span"))
        .isEmpty();
  }

  public boolean hasLTIResource(int section, String name) {
    return isPresent(getSectionElement(section), By.xpath(xpathForLTILink(name) + "/a/span"));
  }

  public MoodleResourcePage clickLTIResource(int section, String name) {
    getSectionElement(section).findElement(By.xpath(xpathForLTILink(name) + "/a")).click();
    return new MoodleResourcePage(this, "mod-lti-view").get();
  }

  public MoodleCoursePage deleteResource(int section, String name) {
    return new ResourceRow(getSectionElement(section), By.xpath(xpathForResource(name)))
        .get()
        .delete();
  }

  public MoodleCoursePage deleteLTIResource(int section, String name) {
    return new ResourceRow(getSectionElement(section), By.xpath(xpathForLTILink(name)))
        .get()
        .delete();
  }

  public MoodleCoursePage deleteResourceStartingWith(int section, String name) {
    ResourceRow resourceRow =
        new ResourceRow(getSectionElement(section), By.xpath(xpathForPartial(name)));

    while (resourceRow.isLoaded()) {
      resourceRow.delete();
    }
    return this;
  }

  public MoodleEditResourcePage editResource(int section, String name) {
    String editXPath = "/span/a[@title='Update']";
    if (getMoodleVersion() == 25) {
      editXPath = "/../" + editXPath;
    } else if (getMoodleVersion() >= 26) {
      scrollIntoViewAndClick(
          getSectionElement(section)
              .findElement(
                  By.xpath(
                      xpathForResource(name) + "/../span//a[text()=" + quoteXPath("Edit") + "]")));
      editXPath = "/../span//a[@data-action=" + quoteXPath("update") + "]";
    }
    WebElement editAction =
        getSectionElement(section).findElement(By.xpath(xpathForResource(name) + editXPath));
    waiter.until(ExpectedConditions.elementToBeClickable(editAction));
    editAction.click();

    return new MoodleEditResourcePage(this).get();
  }

  public MoodleResourcePage clickResource(int section, String name) {
    scrollIntoViewAndClick(
        getSectionElement(section).findElement(By.xpath(xpathForResource(name) + "/a")));
    return new MoodleResourcePage(this, "mod-equella-view").get();
  }

  public <T extends AbstractPage<T>> T clickResourcePopup(int section, String name, T page) {
    getSectionElement(section).findElement(By.xpath(xpathForResource(name) + "/a")).click();
    final String currentWindowHandle = driver.getWindowHandle();
    mainWindow = currentWindowHandle;
    waiter.until(
        new ExpectedCondition<Boolean>() {
          @Override
          public Boolean apply(WebDriver d) {
            Set<String> windowList = driver.getWindowHandles();
            for (String windowHandle : windowList) {
              if (!Check.isEmpty(windowHandle) && !windowHandle.equals(currentWindowHandle)) {
                driver.switchTo().window(windowHandle);
                return Boolean.TRUE;
              }
            }
            return Boolean.FALSE;
          }
        });

    return page.get();
  }

  public MoodleCoursePage closePopup() {
    driver.close();
    driver.switchTo().window(mainWindow);
    return get();
  }

  public String xpathForLTILink(String name) {
    return ".//li[contains(@class, 'lti')]//a/span[normalize-space(text())="
        + quoteXPath(name)
        + "]/../..";
  }

  public String xpathForResource(String name) {
    return ".//li[contains(@class, 'equella')]//a/span[normalize-space(text())="
        + quoteXPath(name)
        + "]/../..";
  }

  public String xpathForPartial(String name) {
    return ".//li[contains(@class, 'equella')]//a/span[contains(text(), "
        + quoteXPath(name)
        + ")]/../..";
  }

  public MoodleCoursePage setEditing(boolean on) {
    By onBy = By.xpath("//input[@value='Turn editing on']");
    By offBy = By.xpath("//input[@value='Turn editing off']");
    if (on && isPresent(onBy)) {
      driver.findElement(onBy).click();
      waitForElement(offBy);

      waiter.until(ExpectedConditions2.elementAttributeToContain(contentDiv, "id", "yui"));
    } else if (!on && isPresent(offBy)) {
      driver.findElement(offBy).click();
      waitForElement(onBy);
    }
    if (isPresent(By.id("dndupload-status"))) {
      waitForElementInvisibility(driver.findElement(By.id("dndupload-status")));
    }
    return get();
  }

  public void setActivityChooser(boolean on) {
    By onBy = By.xpath("//a[text()='Activity chooser on']");
    By offBy = By.xpath("//a[text()='Activity chooser off']");
    if (on && isPresent(onBy)) {
      driver.findElement(onBy).click();
      waitForElement(offBy);
    } else if (!on && isPresent(offBy)) {
      driver.findElement(offBy).click();
      waitForElement(onBy);
    }
  }

  public String dropDownLocation() {
    return driver.findElement(By.xpath("//option[text()='EQUELLA Resource']/../option")).getText();
  }

  public MoodleSelectionPage selectEquellaResource(int week) {
    return clickEquellaResource(week);
  }

  public void deleteAllForWeek(int week) {
    WebElement section = getSectionElement(week);
    ResourceRow row =
        new ResourceRow(
            section,
            By.xpath(
                ".//li[contains(@class, 'equella') and contains(@class,"
                    + " 'activity')]//div[@class='activityinstance']"));

    while (row.isLoaded()) {
      row.delete();
    }
  }

  public String getCourseName() {
    return courseName;
  }

  public MoodleSearchBlockPage addEquellaSearchBlock() {
    if (!hasSearchBlock()) {
      Select select = new Select(driver.findElement(By.xpath("id('add_block')//select")));
      select.selectByVisibleText("EQUELLA Search");
      waitForElement(By.xpath("//div[contains(@class, 'block_equella_search')]"));
    }
    return searchBlock();
  }

  public boolean hasSearchBlock() {
    return !driver
        .findElements(By.xpath("//div[contains(@class, 'block_equella_search')]"))
        .isEmpty();
  }

  public MoodleCoursePage deleteSearchBlock() {
    WebElement element;
    if (this.getMoodleVersion() >= 26) {
      String xpath =
          "//div[contains(@class, 'block_equella_search')]//a[contains(@class, 'toggle-display')]";
      WebElement editLink = driver.findElement(By.xpath(xpath));
      editLink.click();
      element =
          driver.findElement(
              By.xpath(
                  "//div[contains(@class, 'block_equella_search')]//a//span[text()='Delete EQUELLA"
                      + " Search block']"));
      waiter.until(ExpectedConditions.elementToBeClickable(element));
      element.click();
    } else {
      element =
          driver.findElement(
              By.xpath(
                  "//div[contains(@class, 'block_equella_search')]//a[@title='Delete EQUELLA Search"
                      + " block']"));
      element.sendKeys(Keys.RETURN);
    }

    return ExpectWaiter.waiter(
            removalCondition(element),
            new MoodleNoticePage<MoodleCoursePage>(MoodleCoursePage.this, "Yes"))
        .get();
  }

  public MoodleSearchBlockPage searchBlock() {
    return new MoodleSearchBlockPage(context).get();
  }

  public MoodleCoursePage addEquellaTasksBlock() {
    if (!hasTasksBlock()) {
      Select select = new Select(addBlock);
      select.selectByVisibleText("EQUELLA Tasks");
      waitForElement(By.xpath("//div[contains(@class, 'block_equella_tasks')]"));
    }
    return get();
  }

  public boolean hasTasksBlock() {
    return !driver
        .findElements(By.xpath("//div[contains(@class, 'block_equella_tasks')]"))
        .isEmpty();
  }

  public MoodleCoursePage deleteTasksBlock() {
    WebElement element;
    if (this.getMoodleVersion() >= 26) {
      String xpath =
          "//div[contains(@class, 'block_equella_tasks')]//a[contains(@class, 'toggle-display')]";
      WebElement editLink = driver.findElement(By.xpath(xpath));
      editLink.click();
      element =
          driver.findElement(
              By.xpath(
                  "//div[contains(@class, 'block_equella_tasks')]//a//span[text()='Delete EQUELLA"
                      + " Tasks block']"));
      element.click();
    } else {
      element =
          driver.findElement(
              By.xpath(
                  "//div[contains(@class, 'block_equella_tasks')]//a[@title='Delete EQUELLA Tasks"
                      + " block']"));
      element.sendKeys(Keys.RETURN);
    }

    return ExpectWaiter.waiter(
            removalCondition(element),
            new MoodleNoticePage<MoodleCoursePage>(MoodleCoursePage.this, "Yes"))
        .get();
  }

  public MoodleTasksBlockPage tasksBlock() {
    return new MoodleTasksBlockPage(context).get();
  }

  public MoodleCourseSettingsBlock settingsBlock() {
    return new MoodleCourseSettingsBlock(context).get();
  }

  public MoodleAddExternalToolPage addExternalTool(int week) {
    WebElement sectionElement = getSectionElement(week);

    WebElement findElement = sectionElement.findElement(By.className("section-modchooser"));
    waitForElement(findElement, By.xpath(".//a")).click();
    ActivityDialog activityDialog = new ActivityDialog().get();
    activityDialog.chooseExternalTool();

    return new MoodleAddExternalToolPage(context).get();
  }

  public MoodleAddExternalToolPage editLTILink(int section, String name) {
    getSectionElement(section)
        .findElement(By.xpath(xpathForResource(name) + "/span/a[@title='Update']"))
        .click();
    return new MoodleAddExternalToolPage(context).get();
  }

  public class ResourceRow extends AbstractPage<ResourceRow> {
    @DontCache
    @FindBy(xpath = "./..//span/a[@title='Delete']")
    private WebElement deleteButton;

    private int moodleVersion;

    public ResourceRow(SearchContext searchContext, By by) {
      super(MoodleCoursePage.this.context, searchContext, by);
      moodleVersion = MoodleCoursePage.this.getMoodleVersion();
    }

    @Override
    public SearchContext getSearchContext() {
      return loadedElement;
    }

    public MoodleCoursePage delete() {
      if (moodleVersion >= 26) {
        scrollIntoViewAndClick(
            getLoadedElement()
                .findElement(By.xpath("..//span//a[contains(@class, 'toggle-display')]")));
        deleteButton =
            getLoadedElement()
                .findElement(By.xpath("..//span//a[@data-action=" + quoteXPath("delete") + "]"));
        waiter.until(ExpectedConditions.elementToBeClickable(deleteButton));
      }
      deleteButton.click();

      // sometimes there is an alert 2.3+ ....sometimes
      try {
        driver.switchTo().alert().accept();
        return MoodleCoursePage.this.get();
      } catch (NoAlertPresentException e) {
        if (moodleVersion >= 27) {
          driver
              .findElement(By.xpath("//div[@class='confirmation-buttons']/input[@value='Yes']"))
              .click();
          return MoodleCoursePage.this.get();
        }
        return new MoodleNoticePage<MoodleCoursePage>(MoodleCoursePage.this, "Yes").get();
      }
    }
  }

  public class ActivityDialog extends AbstractPage<ActivityDialog> {
    @FindBy(id = "module_equella")
    private WebElement equellaRadio;

    @FindBy(xpath = "id('module_equella')/following-sibling::span[contains(@class, 'typesummary')]")
    private WebElement equellaSummary;

    @FindBy(id = "module_lti")
    private WebElement ltiRadio;

    @FindBy(xpath = "id('module_lti')/following-sibling::span[contains(@class, 'typesummary')]")
    private WebElement ltiSummary;

    @FindBy(name = "submitbutton")
    private WebElement submitV24;

    public ActivityDialog() {
      super(MoodleCoursePage.this.context, By.className("moodle-dialogue-content"));
    }

    public void chooseExternalTool() {
      ltiRadio.click();
      waitForElement(ltiSummary);
      submit();
    }

    @Override
    public SearchContext getSearchContext() {
      return loadedElement;
    }

    public void chooseEquella() {
      equellaRadio.click();
      waitForElement(equellaSummary);
      submit();
    }

    private void submit() {
      submitV24.click();
    }
  }
}
