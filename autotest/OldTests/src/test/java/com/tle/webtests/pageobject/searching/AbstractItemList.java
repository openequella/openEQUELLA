package com.tle.webtests.pageobject.searching;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public abstract class AbstractItemList<
        T extends AbstractItemList<T, SR>, SR extends AbstractItemSearchResult<SR>>
    extends AbstractResultList<T, SR> {
  @FindBy(className = "searchresults")
  private WebElement resultDiv;

  @FindBy(id = "searchresults-stats")
  private WebElement statsDiv;

  public AbstractItemList(PageContext context) {
    super(context);
  }

  @Override
  public WebElement getResultsDiv() {
    return resultDiv;
  }

  public SummaryPage viewFromTitle(PrefixedName title) {
    return viewFromTitle(title.toString());
  }

  public SummaryPage viewFromTitle(String title) {
    return viewFromTitle(title, 1);
  }

  public SummaryPage viewFromTitle(String title, int index) {
    return getResultForTitle(title, index).viewSummary();
  }

  /**
   * Convenient static method to dig out the numeric value (23345) from a string such as "Showing 21
   * to 30 of 23,345 results". We assume the 'real' number is the last sequence of digits (with
   * optional comma separators) from the end.
   *
   * @return the number of results, or 0 if the string is empty or doesn't contain any numbers
   */
  public static int parseAllAvailableFromSummaryString(String summaryString) {
    if (Check.isEmpty(summaryString)) {
      return 0;
    }
    Matcher matcher = Pattern.compile("[\\d,]+").matcher(summaryString);
    String lastMatch = null;
    while (matcher.find()) {
      lastMatch = matcher.group();
    }

    return Optional.ofNullable(lastMatch)
        .map(s -> s.replace(",", ""))
        .map(Integer::parseInt)
        .orElse(0);
  }

  public int getTotalAvailable() {
    if (!isPresent(statsDiv)) {
      return 0;
    }
    String resultsTotalStr = statsDiv.getText();
    int howManyDisTime = parseAllAvailableFromSummaryString(resultsTotalStr);
    return howManyDisTime;
  }
}
