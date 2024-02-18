package com.tle.webtests.pageobject.searching;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.viewitem.SummaryPage;
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
   * convenient static method to dig out the numeric value (23345) from a string such as "Showing 21
   * to 30 of 23,345 results" We assume the 'real' number is the last sequence of digits from the
   * end
   *
   * @param summaryString
   * @return
   */
  public static int parseAllAvailableFromSummaryString(String summaryString) {
    if (Check.isEmpty(summaryString)) return 0;
    int sum = 0, mult = 1;
    int charIndex = summaryString.length();
    // From the end of the string, bypass any non-digit trailing characters
    while (!Character.isDigit(summaryString.charAt(--charIndex)) && charIndex > 0) {
      // empty loop
    }
    // We have at least one character left in the string
    for (char ch = summaryString.charAt(charIndex);
        ch == ',' || Character.isDigit(ch);
        ch = summaryString.charAt(--charIndex)) {
      if (Character.isDigit(ch)) {
        int digit = Integer.parseInt(Character.toString(ch));
        sum += (digit * mult);
        mult *= 10;
      }
    }
    return sum;
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
