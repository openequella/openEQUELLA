package com.tle.webtests.pageobject.reporting;

import com.tle.common.Check;
import com.tle.webtests.framework.PageContext;
import com.tle.webtests.pageobject.PrefixedName;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ConnectorReportOptionsPage
    extends AbstractReportWindow<ConnectorReportPage, ConnectorReportOptionsPage> {

  @FindBy(id = "c1")
  private WebElement connectorDropDown;

  @FindBy(id = "c2_0")
  private WebElement showArchived;

  private EquellaSelect connectors;

  public ConnectorReportOptionsPage(PageContext context) {
    super(context, new ConnectorReportPage(context));
  }

  @Override
  public void checkLoaded() throws Error {
    super.checkLoaded();
    connectors = new EquellaSelect(context, connectorDropDown);
  }

  public ConnectorReportOptionsPage selectConnector(PrefixedName connector) {
    return selectConnector(connector.toString());
  }

  public ConnectorReportOptionsPage selectConnector(String connector) {
    connectors.selectByVisibleText(connector);
    return get();
  }

  public ConnectorReportOptionsPage showArchived(boolean on) {
    if (on == Check.isEmpty(showArchived.getAttribute("checked"))) {
      showArchived.click();
    }
    return get();
  }
}
