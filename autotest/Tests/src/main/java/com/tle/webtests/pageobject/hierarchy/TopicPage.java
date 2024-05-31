package com.tle.webtests.pageobject.hierarchy;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.ReceiptPage;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.searching.AbstractQuerySection;
import com.tle.webtests.pageobject.searching.AbstractQueryableSearchPage;
import com.tle.webtests.pageobject.searching.FavouriteSearchDialog;
import com.tle.webtests.pageobject.searching.SearchPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

public class TopicPage
    extends AbstractQueryableSearchPage<TopicPage, TopicListPage, TopicSearchResult> {
  private WebElement getMainElem() {
    return driver.findElement(
        By.xpath("//div[contains(@class,'browse-topics')]/h2[text() = " + quoteXPath(title) + "]"));
  }

  private String title;

  public TopicPage(PageContext context) {
    this(context, "Browse");
  }

  public TopicPage(PageContext context, String title) {
    super(context);
    this.title = title;
  }

  @Override
  protected AbstractQuerySection<?> createQuerySection() {
    return super.createQuerySection();
  }

  public String getTitleXpath() {
    return quoteXPath(title);
  }

  @Override
  protected WebElement findLoadedElement() {
    return getMainElem();
  }

  @Override
  protected void loadUrl() {
    driver.get(context.getBaseUrl() + "hierarchy.do");
  }

  // Because it will remember the history and jump to the last visited hierarchy page in the old UI,
  // use `topic=All` to force opening the browse page
  public TopicPage loadBrowsePage() {
    driver.get(context.getBaseUrl() + "hierarchy.do?topic=ALL");
    return get();
  }

  public boolean resultsHidden() {
    return !isPresent(By.id("searchresults"));
  }

  public TopicPage clickSubTopic(String title) {
    driver.findElement(By.xpath(getTopicBy(title))).click();
    return new TopicPage(context, title).get();
  }

  public boolean hasSubTopic(String title) {
    return isPresent(By.xpath(getTopicBy(title)));
  }

  public boolean hasSubTopic(String title, int position) {
    return isPresent(
        By.xpath(
            "//div[contains(@class,'browse-topics')]//ul/li["
                + position
                + "]/a[text()="
                + quoteXPath(title)
                + "]"));
  }

  public int topicCount(String title) {
    int count = -1;

    String topicAndCount = driver.findElement(By.xpath(getTopicBy(title) + "/..")).getText().trim();
    if (topicAndCount.length() != title.length()) {
      count =
          Integer.parseInt(topicAndCount.substring(title.length() + 2, topicAndCount.length() - 1));
    }

    return count;
  }

  public boolean topicExists(String title) {
    return isPresent(By.xpath(getTopicBy(title)));
  }

  private String getTopicBy(String title) {
    return "//div[contains(@class,'browse-topics')]//a[text()=\"" + title + "\"]";
  }

  public boolean hasPowerSearch() {
    return isPresent(By.id("hier_advancedSearch"));
  }

  public SearchPage clickPowerSearch() {
    Actions actions = new Actions(driver);
    actions.click(driver.findElement(By.id("hier_advancedSearch")));
    actions.perform();
    return new SearchPage(context).get();
  }

  @Override
  public TopicListPage resultsPageObject() {
    return new TopicListPage(context);
  }

  public <T extends PageObject> T saveSearch(String searchName, WaitingPageObject<T> targetPage) {
    FavouriteSearchDialog dialog = new FavouriteSearchDialog(context, "hfsa").open();
    return dialog.favourite(
        searchName,
        ReceiptPage.waiter("Successfully added this search to your favourites", targetPage));
  }

  public String getSubtopicSectionName() {
    return driver.findElement(By.xpath("//div[contains(@class,'browse-topics')]/div/h4")).getText();
  }

  public TopicPage clickBrowseBreadcrumb() {
    driver.findElement(By.xpath("//div[@id='breadcrumbs']/span//a[text() = 'Browse']")).click();
    return new TopicPage(context).get();
  }
}
