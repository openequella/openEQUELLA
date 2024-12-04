package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.searching.SearchPage;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NotFoundException;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;

public class MoodleEditResourcePage extends MoodleBasePage<MoodleEditResourcePage> {
  @FindBy(id = "id_name")
  private WebElement nameField;

  @FindBy(id = "id_url")
  private WebElement urlField;

  @FindBy(id = "id_introeditor")
  private WebElement descriptionField;

  @FindBy(id = "id_windowpopup")
  private WebElement displaySelect;

  @FindBy(id = "id_submitbutton")
  private WebElement submit;

  @FindBy(id = "id_submitbutton2")
  private WebElement submit2;

  private final MoodleCoursePage coursePage;

  public MoodleEditResourcePage(MoodleCoursePage coursePage) {
    super(coursePage.getContext(), By.xpath("//h2[contains(text(),'Updating EQUELLA Resource')]"));
    this.coursePage = coursePage;
  }

  @Override
  public void checkLoaded() throws NotFoundException {
    super.checkLoaded();
  }

  public void setName(String name) {
    nameField.clear();
    nameField.sendKeys(name);
    // FIXME
    //		import com.thoughtworks.selenium.webdriven.JavascriptLibrary;
    //		JavascriptLibrary javascript = new JavascriptLibrary();
    //		javascript.callEmbeddedSelenium(driver, "triggerEvent", nameField, "blur");
  }

  public void setDescription(String description) {
    waiter.until(ExpectedConditions.frameToBeAvailableAndSwitchToIt("id_introeditor_ifr"));
    waitAndClick(By.tagName("body"));
    driver.switchTo().activeElement();
    ((JavascriptExecutor) driver)
        .executeScript("document.body.innerHTML = " + quoteXPath(description));
    driver.switchTo().defaultContent();
  }

  // This is rubbish and needs rewriting, like most of the moodle stuff
  public MoodleEditResourcePage insertImage(String item, String image, String alt) {
    driver.findElement(By.id("id_introeditor_image")).click();

    waiter.until(
        ExpectedConditions2.frameToBeAvailableAndSwitchToIt(
            driver, By.xpath("//div[@class='mceMiddle']//iframe")));

    waitAndClick(By.id("srcbrowser_link"));
    driver.switchTo().defaultContent();

    FilePicker filePicker = new FilePicker().get();
    SelectFileDialog selectFileDialog = filePicker.searchAndSelect(item, image);
    selectFileDialog.selectFile();

    waiter.until(
        ExpectedConditions2.frameToBeAvailableAndSwitchToIt(
            driver, By.xpath("//div[@class='mceMiddle']//iframe")));
    waitForElement(By.id("previewImg"));
    driver.findElement(By.id("alt")).sendKeys(alt);

    // http://code.google.com/p/selenium/issues/detail?id=2703
    if (context.getTestConfig().isChromeDriverSet()) {
      driver.switchTo().defaultContent();
      ((JavascriptExecutor) driver)
          .executeScript(
              "var tempVar = tinymce.DOM.setAttrib; tinymce.DOM.setAttrib = function(id, attr, val)"
                  + " { if (attr == 'src' &&"
                  + " val.trim().match(/javascript\\s*:\\s*(\"\\s*\"|'\\s*')/)) {return;} else"
                  + " {tempVar.apply(this, arguments);} }");
      waiter.until(
          ExpectedConditions2.frameToBeAvailableAndSwitchToIt(
              driver, By.xpath("//div[@class='mceMiddle']//iframe")));
    }
    waitForElement(By.id("insert")).click();
    driver.switchTo().defaultContent();
    waiter.until(
        ExpectedConditions.invisibilityOfElementLocated(
            By.xpath("//div[@class='mceMiddle']//iframe")));
    return get();
  }

  private void waitAndClick(By by) {
    waitForElement(by);
    driver.findElement(by).click();
  }

  public void setUrl(String url) {
    urlField.clear();
    urlField.sendKeys(url);
  }

  public String getUrl() {
    return urlField.getAttribute("value");
  }

  public String getDescription() {
    return descriptionField.getAttribute("value");
  }

  public MoodleResourcePage submitAndView() {
    submit.click();
    return new MoodleResourcePage(coursePage, "mod-equella-view").get();
  }

  public MoodleCoursePage submit() {
    submit2.click();
    return coursePage.get();
  }

  public void setDisplay(String location) {
    new Select(displaySelect).selectByVisibleText(location);
  }

  public class FilePicker extends AbstractPage<FilePicker> {
    @FindBy(xpath = ".//span[text()='EQUELLA repository']")
    private WebElement equellaReop;

    @FindBy(id = "container_object")
    private WebElement objectFrame;

    public FilePicker() {
      super(
          MoodleEditResourcePage.this.context,
          MoodleEditResourcePage.this.driver,
          By.className("fp-generallayout"));
    }

    @Override
    public SearchContext getSearchContext() {
      return loadedElement;
    }

    public SelectFileDialog searchAndSelect(String item, String attachment) {
      equellaReop.click();
      if (context.getTestConfig().isChromeDriverSet()) {
        waiter.until(ChromeHacks.convertObjectToiFrame(context, waitForElement(objectFrame)));
      } else {
        driver.switchTo().frame(0);
      }
      SearchPage searchPage = new SearchPage(context).get();
      SummaryPage viewSummary =
          searchPage.exactQuery(item).getResultForTitle(item, 1).viewSummary();
      return viewSummary.attachments().selectAttachment(attachment, new SelectFileDialog());
    }
  }

  public class SelectFileDialog extends AbstractPage<SelectFileDialog> {
    @FindBy(xpath = ".//tr[contains(@class, 'fp-saveas')]/td/input")
    private WebElement nameField;

    @FindBy(className = "fp-select-confirm")
    private WebElement selectButton;

    public SelectFileDialog() {
      super(
          MoodleEditResourcePage.this.context,
          MoodleEditResourcePage.this.driver,
          By.className("fp-select"));
    }

    @Override
    public SearchContext getSearchContext() {
      return loadedElement;
    }

    public void selectFile() {
      selectButton.click();
    }

    @Override
    public SelectFileDialog get() {
      driver.switchTo().defaultContent();
      return super.get();
    }
  }
}
