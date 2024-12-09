package com.tle.webtests.pageobject.recipientseletor;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.AbstractPage;
import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.ExpectedConditions2;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

@SuppressWarnings("unused")
public class RecipientSelectorPage extends AbstractPage<RecipientSelectorPage> {
  @FindBy(xpath = "//ul[@id='myTab']/li/a[text() ='Home']")
  private WebElement homeLink;

  @FindBy(xpath = "//ul[@id='myTab']/li/a[text() ='Other']")
  private WebElement otherLink;

  @FindBy(id = "ecr_userSelector_es_q")
  private WebElement searchField;

  @FindBy(id = "ecr_userSelector_es_s")
  private WebElement searchButton;

  @FindBy(id = "ecr_userSelector_es_type_0")
  private WebElement usersRadioButton;

  @FindBy(id = "ecr_userSelector_es_type_1")
  private WebElement groupsRadioButton;

  @FindBy(id = "ecr_userSelector_es_type_2")
  private WebElement rolesRadioButton;

  @FindBy(id = "ecr_userSelector_es_agl")
  private WebElement filterByGroupLink;

  @FindBy(id = "ecr_userSelector_es_as")
  private WebElement addSelectedButton;

  @FindBy(id = "ecr_userSelector_es_aob")
  private WebElement addOtherButton;

  @FindBy(id = "ecr_userSelector_es_ag")
  private WebElement addGroupingButton;

  @FindBy(id = "ecr_userSelector_ok")
  private WebElement okButton;

  @FindBy(id = "search-result-list")
  private WebElement resultDiv;

  @FindBy(id = "ecr_userSelector_es_sau")
  private WebElement selectAllUserLink;

  @FindBy(id = "right-cloumn")
  private WebElement rightSide;

  public RecipientSelectorPage(PageContext context) {
    super(context);
  }

  public enum TypeValue {
    USER(0),
    GROUP(1),
    ROLE(2);

    private int value;

    private TypeValue(int value) {
      this.value = value;
    }
  };

  @Override
  protected WebElement findLoadedElement() {
    return homeLink;
  }

  public void clickOtherLink() {
    otherLink.click();
    waitForElement(By.id("other-types"));
  }

  public void chooseGroupingOption(String groupingId, String option) {
    rightSide.findElement(By.xpath("//li[@id=" + quoteXPath(groupingId) + "]/div/div")).click();
    rightSide
        .findElement(
            By.xpath(
                "//li[@id="
                    + quoteXPath(groupingId)
                    + "]/div/ul/li/a[text()="
                    + quoteXPath(option)
                    + "]"))
        .click();
  }

  public WebElement selectGrouping(String groupingId) {
    WebElement findElement = rightSide.findElement(By.cssSelector("li[id=" + groupingId + "]"));
    new Actions(driver).moveToElement(findElement, 3, 2).click().perform();
    return findElement;
  }

  protected List<WebElement> getSelectedResults() {
    List<WebElement> findElements =
        driver.findElements(By.xpath("//ul[@class='search-result']/li"));
    List<WebElement> selections = new ArrayList<>();
    for (WebElement res : findElements) {
      WebElement cb = res.findElement(By.xpath(".//input[@type='checkbox']"));
      if (cb.isSelected()) {
        // WebElement label = res.findElement(By.xpath(".//label"));
        // selections.add(label.getText());
        selections.add(res);
      }
    }
    return selections;
  }

  protected List<WebElement> getAllResults() {
    return driver.findElements(By.xpath("//ul[@class='search-result']/li"));
  }

  protected WebElement getSelectedGroup() {
    WebElement findElement = rightSide.findElement(By.xpath("//li[contains(@class, 'selected')]"));
    return findElement;
  }

  protected List<WebElement> getGroupElements(WebElement group) {
    final WebElement groupParent = group.findElement(By.xpath(".."));
    final List<WebElement> selectedGroupElements =
        groupParent.findElements(By.xpath("./li[div/@class='expression-text']"));
    return selectedGroupElements;
  }

  public void addGrouping(String groupingId) {
    final WebElement grouping = selectGrouping(groupingId);
    final String nextGroupingId = getNextGroupingId();
    addGroupingButton.click();
    // need to wait for a new sibling of the parent UL, with grouping ID = highest grouping + 1
    waitForElement(By.id(nextGroupingId));
  }

  private String getNextGroupingId() {
    int highestId = 0;
    final List<WebElement> findElements =
        rightSide.findElements(By.xpath("//li[starts-with(@id, 'grouping')]"));
    for (WebElement elem : findElements) {
      String id = elem.getAttribute("id");
      int index = Integer.parseInt(id.substring("grouping".length()));
      if (index > highestId) {
        highestId = index;
      }
    }
    return "grouping" + (highestId + 1);
  }

  public void addOtherOptionByName(String option) {
    driver
        .findElement(
            By.xpath("//div[@id='tab2']/div/ul/li/label[text()=" + quoteXPath(option) + "]"))
        .click();
    final WebElement selectedGroupParent = getSelectedGroup().findElement(By.xpath(".."));
    final int oldChildCount = selectedGroupParent.findElements(By.xpath("./*")).size();
    addOtherButton.click();
    // Wait for another sibling of the selected group
    final int expectedChildCount = oldChildCount + 1;
    getWaiter().until(ExpectedConditions2.childCount(selectedGroupParent, expectedChildCount));
  }

  public void removeSelectedByName(String selectedName) {
    WebElement element =
        rightSide.findElement(
            By.xpath("//ul/li/div[text()=" + quoteXPath(selectedName) + "]/../a"));
    WaitingPageObject<RecipientSelectorPage> removalWaiter = removalWaiter(element);
    element.click();
    removalWaiter.get();
  }

  public void clickSingleAddIcon(String displayName) {
    clickSingleAddIcon(1, displayName);
  }

  public void clickSingleAddIcon(int index, String displayName) {
    WebElement result =
        resultDiv.findElement(By.xpath("//ul[@class='search-result']/li[" + index + "]"));
    WebElement plusIcon = result.findElement(By.xpath("div[@class='add-user']/a"));
    plusIcon.click();
    waitForElement(
        By.xpath(
            "//ul[contains(@class, 'grouping')]/li/div[@class='expression-text' and"
                + " contains(text(), "
                + quoteXPath(displayName)
                + ")]"));
  }

  public void selectAllResults() {
    getWaiter().until(ExpectedConditions.elementToBeClickable(By.id("ecr_userSelector_es_sau")));
    selectAllUserLink.click();
    final List<WebElement> results = getAllResults();
    final List<WebElement> checkboxes = new ArrayList<>();
    for (WebElement result : results) {
      checkboxes.add(result.findElement(By.xpath("input")));
    }
    // Wait for all checkboxes to be ticked
    for (WebElement cb : checkboxes) {
      getWaiter().until(ExpectedConditions.elementToBeSelected(cb));
    }
  }

  public void addSelectedResults() {
    final WebElement group = getSelectedGroup();
    final WebElement groupParent = group.findElement(By.xpath(".."));
    final List<WebElement> currentGroupElements = getGroupElements(group);
    final int oldGroupSize = currentGroupElements.size();
    final List<String> groupLabels = getGroupLabels(currentGroupElements);
    // Results won't be added to the group twice,
    // so it's possible for a result not to have moved across
    final List<WebElement> selectedResults = getSelectedResults();
    final List<String> resultLabels = getResultLabels(selectedResults);
    resultLabels.removeAll(groupLabels);
    final int resultCountToAdd = resultLabels.size();

    addSelectedButton.click();
    // Wait for all selected boxes to be added (as siblings of selected group)
    final int expectedChildCount = 1 + oldGroupSize + resultCountToAdd;
    getWaiter().until(ExpectedConditions2.childCount(groupParent, expectedChildCount));
  }

  protected List<String> getResultLabels(List<WebElement> resultElements) {
    return new ArrayList<>(
        Lists.transform(
            resultElements,
            new Function<WebElement, String>() {
              @Override
              public String apply(WebElement li) {
                WebElement label = li.findElement(By.xpath("./label"));
                return label.getText();
              }
            }));
  }

  protected List<String> getGroupLabels(List<WebElement> groupElements) {
    return new ArrayList<>(
        Lists.transform(
            groupElements,
            new Function<WebElement, String>() {
              @Override
              public String apply(WebElement li) {
                WebElement div =
                    li.findElement(By.xpath("./div[contains(@class, 'expression-text')]"));
                return div.getText();
              }
            }));
  }

  public <T extends PageObject> T okButtonClick(WaitingPageObject<T> returnPage) {
    WaitingPageObject<T> disappearWaiter =
        ExpectWaiter.waiter(removalCondition(okButton), returnPage);
    okButton.click();
    return disappearWaiter.get();
  }

  public void searchUsersOrGroupsOrRoles(String searchQuery, TypeValue type) {
    WebElement radio = null;
    switch (type) {
      case USER:
        usersRadioButton.click();
        radio = usersRadioButton;
        break;
      case GROUP:
        groupsRadioButton.click();
        radio = groupsRadioButton;
        break;
      case ROLE:
        rolesRadioButton.click();
        radio = rolesRadioButton;
        break;
    }
    waitForElement(radio).isSelected();

    // ummmm.... so it goes into an infinite recursion until it becomes selected?
    // if( radio == null || !radio.isSelected() )
    // {
    //	searchUsersOrGroupsOrRoles(searchQuery, type);
    // }

    searchField.clear();
    searchField.sendKeys(searchQuery);
    final ExpectedCondition<?> ajaxUpdate = ExpectedConditions2.ajaxUpdate(resultDiv);
    searchButton.click();
    // Wait for results field to update
    getWaiter().until(ajaxUpdate);
  }

  public List<String> getResults(TypeValue type) {
    List<String> results = new ArrayList<String>();
    By xpath = By.xpath("//ul[@class='search-result']/li");
    waitForElement(xpath);
    List<WebElement> findElements = resultDiv.findElements(xpath);

    int count = findElements.size();

    for (int i = 1; i <= count; i++) {
      String result = null;
      switch (type) {
        case USER:
          result = getUsers(i);
          break;
        case GROUP:
          result = getGroups(i);
          break;
        case ROLE:
          result = getRoles(i);
          break;
      }
      results.add(result);
    }
    return results;
  }

  public String getUsers(int index) {
    return resultDiv
        .findElement(By.xpath("//ul[@class='search-result']/li[" + index + "]/div/span"))
        .getText();
  }

  public String getGroups(int index) {
    return resultDiv
        .findElement(By.xpath("//ul[@class='search-result']/li[" + index + "]/label"))
        .getText();
  }

  public String getRoles(int index) {
    return resultDiv
        .findElement(By.xpath("//ul[@class='search-result']/li[" + index + "]/label"))
        .getText();
  }
}
