package com.tle.webtests.test.reporting;

import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.reporting.AbstractReportWindow;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class CascadingParametersReportParametersPage
    extends AbstractReportWindow<
        CascadingParametersReportPage, CascadingParametersReportParametersPage> {
  @FindBy(id = "c1")
  private WebElement itemSelect;

  @FindBy(id = "c2")
  private WebElement versionSelect;

  @FindBy(id = "c3")
  private WebElement descriptionSelect;

  @FindBy(id = "report-params")
  private WebElement updateDiv;

  public CascadingParametersReportParametersPage(PageContext context) {
    super(context, new CascadingParametersReportPage(context));
  }

  public CascadingParametersReportParametersPage selectItem(String item) {
    return doSelect(itemSelect, item);
  }

  public CascadingParametersReportParametersPage selectVersion(int version) {
    return doSelect(versionSelect, "" + version);
  }

  public CascadingParametersReportParametersPage selectDescription(String description) {
    return doSelect(descriptionSelect, description);
  }

  private CascadingParametersReportParametersPage doSelect(WebElement list, String toSelect) {
    WaitingPageObject<CascadingParametersReportParametersPage> ajaxUpdate = ajaxUpdate(updateDiv);
    new EquellaSelect(context, list).selectByVisibleText(toSelect);
    return ajaxUpdate.get();
  }
}
