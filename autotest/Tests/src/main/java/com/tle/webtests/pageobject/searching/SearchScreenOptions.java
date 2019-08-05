package com.tle.webtests.pageobject.searching;

import com.tle.webtests.pageobject.WaitingPageObject;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class SearchScreenOptions extends AbstractSearchPageScreenOptions<SearchScreenOptions> {
  @FindBy(id = "nonlive")
  private WebElement includeNonLive;

  private AbstractResultList<?, ?> resultList;
  private boolean connectorTest = false;

  public SearchScreenOptions(AbstractResultList<?, ?> resultList) {
    super(resultList.getContext());
    this.resultList = resultList;
  }

  // setConnectorTest and closeScreenOption are temporarily used in EquellaConnectorTest,
  // to make the test work for new UI
  public void setConnectorTest(boolean connectorTest) {
    this.connectorTest = connectorTest;
  }

  public void closeScreenOption() {
    close();
  }

  public boolean hasNonLiveOption() {
    return isPresent(By.id("nonlive"));
  }

  public SearchScreenOptions setNonLiveOption(boolean nonLive) {
    if (includeNonLive.isSelected() != nonLive) {
      WaitingPageObject<?> waiter = resultList.getUpdateWaiter();
      includeNonLive.click();
      waiter.get();
    }

    if (!connectorTest) {
      close();
    }
    return this;
  }
}
