package com.tle.webtests.pageobject.cal;

import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.viewitem.AdminTabPage;
import com.tle.webtests.pageobject.viewitem.ItemId;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CALSummaryPage extends AbstractPage<CALSummaryPage> {
  private static final By BY_COPYRIGHT = By.id("copyright-summary");

  @FindBy(id = "cals_activateSelected")
  private WebElement activateSelected;

  private final SummaryPage summaryTab;

  public CALSummaryPage(SummaryPage summaryTab) {
    super(summaryTab.getContext(), BY_COPYRIGHT);
    this.summaryTab = summaryTab;
  }

  public CALSummaryPage activateDefault(int chapter, String attachment, String course) {
    return activateDefault(getChapterNumText(chapter), attachment, course);
  }

  public CALActivatePage<CALSummaryPage> activate(int chapter, String attachment) {
    return activate(getChapterNumText(chapter), attachment, this);
  }

  public <T extends AbstractPage<T>> CALActivatePage<T> activate(
      int chapter, String attachment, T returnTo) {
    return activate(getChapterNumText(chapter), attachment, returnTo);
  }

  public CALActivatePage<CALSummaryPage> activate(String chapterText, String attachment) {
    return activate(chapterText, attachment, this);
  }

  public CALViolationPage activateToViolation(int chapter, String attachment) {
    clickActivate(getChapterNumText(chapter), attachment);
    return new CALViolationPage(context).get();
  }

  public <T extends AbstractPage<T>> CALActivatePage<T> activate(
      String chapterText, String attachment, T returnTo) {
    clickActivate(chapterText, attachment);
    return new CALActivatePage<T>(context, returnTo).get();
  }

  public void add(int chapter, String attachment) {
    clickAdd(chapter, attachment);
  }

  private void clickActivate(String chapterText, String attachment) {
    driver.findElement(activateButtonXPath(findBookRow(chapterText, attachment))).click();
  }

  private void clickAdd(int chapter, String attachment) {
    driver.findElement(addButtonXPath(findBookRow(getChapterNumText(chapter), attachment))).click();
  }

  private By activateButtonXPath(String sectionPath) {
    return By.xpath(
        sectionPath + "//td[@class='sectionAction']/button[contains(text(),'Activate')]");
  }

  private By addButtonXPath(String sectionPath) {
    return By.xpath(sectionPath + "//td[@class='sectionAction']/button[contains(@class,'add')]");
  }

  public CALSummaryPage activateDefault(String chapterText, String attachment, String course) {
    CALActivatePage<CALSummaryPage> activatePage = activate(chapterText, attachment, this);
    activatePage.setCourse(course);
    return (CALSummaryPage) activatePage.activate();
  }

  private String findSection(String portionMatcher, String attachment) {
    return "//table[@id='copyright-table']//tr[td[@class='copyright-portion' and "
        + portionMatcher
        + "]]/following-sibling::tr[td/div[@class='sectionAttachment' and "
        + "*[contains(@class, 'copyright-sectionlink') and normalize-space(text())="
        + quoteXPath(attachment)
        + "]]][1]";
  }

  private String findBookRow(String chapterText, String attachment) {
    return findSection(
        "span[@class='chapterNumber' and normalize-space(text())=" + quoteXPath(chapterText) + "]",
        attachment);
  }

  private String findJournalRow(String heading, String attachment) {
    return findSection("normalize-space(text())=" + quoteXPath(heading), attachment);
  }

  private String getChapterNumText(int chapter) {
    return "Chapter " + chapter + ":";
  }

  public boolean isActive(int chapter, String attachment) {
    return isActive(getChapterNumText(chapter), attachment);
  }

  public boolean isActive(String chapterText, String attachment) {
    return getStatus(findBookRow(chapterText, attachment)).equals("Active");
  }

  private String getStatus(String sectionPath) {
    return driver.findElement(By.xpath(sectionPath + "//td/div[@class='sectionStatus']")).getText();
  }

  public void selectSection(int chapter, String attachment) {
    selectSection(getChapterNumText(chapter), attachment);
  }

  public void selectSection(String chapterText, String attachment) {
    String xpathExpression =
        findBookRow(chapterText, attachment) + "//td/div[@class='sectionCheckbox']/input";

    driver.findElement(By.xpath(xpathExpression)).click();
  }

  public CALActivatePage<CALSummaryPage> activateSelected() {
    activateSelected.click();
    return new CALActivatePage<CALSummaryPage>(context, this).get();
  }

  public String getAttachmentUuid(int chapter, String attachment) {
    WebElement row =
        driver.findElement(By.xpath(findBookRow(getChapterNumText(chapter), attachment)));
    WebElement attachmentLink =
        row.findElement(By.xpath("//td/div/a[contains(@class,'copyright-sectionlink')]"));
    String cssClass = attachmentLink.getAttribute("class");
    return cssClass.substring(8, cssClass.indexOf(" "));
  }

  public boolean isInactive(int chapter, String attachment) {
    return isInactive(getChapterNumText(chapter), attachment);
  }

  public boolean isInactive(String chapterText, String attachment) {
    return getStatus(findBookRow(chapterText, attachment)).equals("Inactive");
  }

  public String getHoldingName() {
    return driver.findElement(By.xpath("//p[@id='copyright-holdinglink']/a")).getText();
  }

  public boolean isTotalShowing() {
    return isPresent(By.xpath("//table[@class='chapter']//tr[.//td[@class='calTotal']][1]"));
  }

  public CALActivatePage<CALSummaryPage> activateJournal(String heading, String attachment) {
    driver.findElement(activateButtonXPath(findJournalRow(heading, attachment))).click();
    return new CALActivatePage<CALSummaryPage>(context, this).get();
  }

  private String getJournalStatus(String heading, String attachment) {
    return getStatus(findJournalRow(heading, attachment));
  }

  public boolean isJournalActive(String heading, String attachment) {
    return getJournalStatus(heading, attachment).equals("Active");
  }

  public boolean isJournalInactive(String heading, String attachment) {
    return getJournalStatus(heading, attachment).equals("Inactive");
  }

  public ActivationsSummaryPage activationsTab() {
    summaryTab.clickSummaryLink("Activations");
    return new ActivationsSummaryPage(context).get();
  }

  public boolean canActivate(int chapter, String attachment) {
    return isPresent(activateButtonXPath(findBookRow(getChapterNumText(chapter), attachment)));
  }

  public boolean canAdd(int chapter, String attachment) {
    return isPresent(addButtonXPath(findBookRow(getChapterNumText(chapter), attachment)));
  }

  public void viewSection(int chapter, String attachment) {
    viewSection(getChapterNumText(chapter), attachment);
  }

  public void viewSection(String chapterText, String attachment) {
    driver
        .findElement(
            By.xpath(
                findBookRow(chapterText, attachment)
                    + "//td/div[@class='sectionAttachment']/a[contains(@class,"
                    + " 'copyright-sectionlink')]"))
        .click();
  }

  public boolean canViewSection(int chapter, String attachment) {
    return isPresent(
        By.xpath(
            findBookRow(getChapterNumText(chapter), attachment)
                + "//td/div[@class='sectionAttachment']/a[contains(@class,"
                + " 'copyright-sectionlink')]"));
  }

  public boolean viewPortionLinkPresent(int chapter, String attachment) {
    return isPresent(
        By.xpath(
            findBookRow(getChapterNumText(chapter), attachment)
                + "//td/div[@class='sectionAttachment']/a[contains(@class,"
                + " 'copyright-portionlink')]"));
  }

  public boolean otherPortionsLinksPresent() {
    return isPresent(By.xpath("//h4[text() = 'Links to other Portions']"));
  }

  public AdminTabPage adminTab() {
    return new AdminTabPage(context).get();
  }

  public boolean isActivationsAvailable() {
    return summaryTab.isSummaryLinkAvailable("Activations");
  }

  public static boolean isDisplayed(SummaryPage summaryTab) {
    return isPresent(summaryTab.getContext().getDriver(), BY_COPYRIGHT);
  }

  public long getTotalPagesFromSummary() {
    WebElement summary = driver.findElement(BY_COPYRIGHT);
    String total = summary.findElement(By.className("copyright-totalvalue")).getText();
    return Long.parseLong(total.replaceAll(",", ""));
  }

  public int getPagesAvailable() {
    WebElement summary = driver.findElement(BY_COPYRIGHT);
    List<WebElement> copyrightTableVals =
        summary.findElements(By.className("copyright-totalvalue"));
    return Integer.parseInt(copyrightTableVals.get(1).getText());
  }

  public ItemId getItemId() {
    String currentUrl = driver.getCurrentUrl();
    String itemsUrl = currentUrl.substring(context.getBaseUrl().length());
    Pattern pattern = Pattern.compile(".*(items|gen|bb)/([\\w-]*)/(\\d*)/.*");
    Matcher matcher = pattern.matcher(itemsUrl);
    if (!matcher.matches()) {
      throw new Error("Wrong url:" + currentUrl);
    }
    return new ItemId(matcher.group(2), Integer.parseInt(matcher.group(3)));
  }

  public boolean hasCitation() {
    return isPresent(By.xpath("//h3[text()='Citation']"));
  }

  public String getCitation() {
    return driver.findElement(By.xpath("//h3[text() = 'Citation']/following-sibling::p")).getText();
  }
}
