package com.tle.webtests.pageobject.integration.moodle;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class MoodleEquellaRepoList extends AbstractPage<MoodleEquellaRepoList> {
  @FindBy(id = "id_enablecourseinstances")
  private WebElement courseCheck;

  @FindBy(id = "id_enableuserinstances")
  private WebElement userCheck;

  @FindBy(className = "generaltable")
  private WebElement table;

  @FindBy(id = "id_submitbutton")
  private WebElement save;

  @FindBy(xpath = "//input[@value='Create a repository instance']")
  private WebElement addButton;

  public MoodleEquellaRepoList(PageContext context) {
    super(context);
  }

  @Override
  protected WebElement findLoadedElement() {
    return userCheck;
  }

  public void setCourse(boolean on) {
    if (courseCheck.isSelected() != on) {
      courseCheck.click();
    }
  }

  public void setUser(boolean on) {
    if (userCheck.isSelected() != on) {
      userCheck.click();
    }
  }

  public MoodleManageRepoPage save() {
    save.click();
    return new MoodleManageRepoPage(context).get();
  }

  public boolean hasRepo(String title) {
    return !table.findElements(getByForRowName(title)).isEmpty();
  }

  public void editRepo(String title, String url, String id, String secret) {
    RepoRow repoRow = new RepoRow(table, title).get();
    MoodleEquellaRepoSettings settings = repoRow.settings();
    settings.setFields(title, url, id, secret);
    settings.save();
  }

  public void addRepo(String title, String url, String id, String secret) {
    addButton.click();
    MoodleEquellaRepoSettings settings = new MoodleEquellaRepoSettings(context).get();
    settings.setFields(title, url, id, secret);
    settings.save();
  }

  private static By getByForRowName(String title) {
    return By.xpath("tbody/tr/td[1][text()=" + quoteXPath(title) + "]/..");
  }

  public class RepoRow extends AbstractPage<RepoRow> {
    @FindBy(linkText = "Settings")
    private WebElement settingsLink;

    public RepoRow(SearchContext searchContext, String title) {
      super(MoodleEquellaRepoList.this.context, searchContext, getByForRowName(title));
    }

    public MoodleEquellaRepoSettings settings() {
      settingsLink.click();
      return new MoodleEquellaRepoSettings(context).get();
    }

    @Override
    public SearchContext getSearchContext() {
      return loadedElement;
    }
  }

  public class MoodleEquellaRepoSettings extends AbstractPage<MoodleEquellaRepoSettings> {
    @FindBy(id = "id_name")
    private WebElement name;

    @FindBy(id = "id_equella_url")
    private WebElement url;

    @FindBy(id = "id_equella_shareid")
    private WebElement sharedId;

    @FindBy(id = "id_equella_sharedsecret")
    private WebElement sharedSecret;

    @FindBy(id = "id_equella_manager_shareid")
    private WebElement managerSharedId;

    @FindBy(id = "id_submitbutton")
    private WebElement save;

    public MoodleEquellaRepoSettings(PageContext context) {
      super(context);
    }

    @Override
    protected WebElement findLoadedElement() {
      return name;
    }

    public void setFields(String newName, String newUrl, String newId, String newSecret) {
      name.clear();
      name.sendKeys(newName);

      url.clear();
      url.sendKeys(newUrl);

      sharedId.clear();
      sharedId.sendKeys(newId);

      sharedSecret.clear();
      sharedSecret.sendKeys(newSecret);

      managerSharedId.click();
      // FIXME
      // 			import com.thoughtworks.selenium.webdriven.JavascriptLibrary;
      //			JavascriptLibrary javascript = new JavascriptLibrary();
      //			javascript.callEmbeddedSelenium(driver, "triggerEvent", name, "blur");

      waitForElement(save);
    }

    public MoodleEquellaRepoList save() {
      save.click();
      return new MoodleEquellaRepoList(context).get();
    }
  }
}
