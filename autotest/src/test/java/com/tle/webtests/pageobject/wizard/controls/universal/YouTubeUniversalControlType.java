package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.ExpectWaiter;
import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;
import com.tle.webtests.pageobject.generic.component.EquellaSelect;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

public class YouTubeUniversalControlType
    extends AbstractUniversalControlType<YouTubeUniversalControlType> {
  private WebElement getSearchField() {
    return byWizId("_dialog_yh_query");
  }

  private WebElement getSearchButton() {
    return byWizId("_dialog_yh_search");
  }

  private WebElement getMainDiv() {
    return byDialogXPath("//div[contains(@class,'youTubeHandler')]");
  }

  public WebElement getNameField() {
    return byWizId("_dialog_yh_displayName");
  }

  private WebElement getChannelList() {
    return byWizId("_dialog_yh_channelList");
  }

  public YouTubeUniversalControlType(UniversalControl control) {
    super(control);
  }

  public YouTubeUniversalControlType search(String searchTerm, String channelName) {
    getSearchField().clear();
    getSearchField().sendKeys(searchTerm);

    if (channelName != null) {
      WaitingPageObject<YouTubeUniversalControlType> submitWaiter = submitWaiter();
      EquellaSelect select = new EquellaSelect(context, getChannelList());
      select.selectByVisibleText(channelName);
      submitWaiter.get();
    }

    WaitingPageObject<YouTubeUniversalControlType> submitWaiter = submitWaiter();
    getSearchButton().click();
    return submitWaiter.get();
  }

  public UniversalControl addVideos(int... indexes) {
    return selectVideos(control.attachmentCountExpectation(indexes.length), indexes);
  }

  private <T extends PageObject> T selectVideos(WaitingPageObject<T> returnTo, int... indexes) {
    for (int i = 0; i < indexes.length; i++) {
      WaitingPageObject<YouTubeUniversalControlType> submitWaiter = submitWaiter();
      driver
          .findElement(By.id(page.subComponentId(ctrlnum, "dialog_yh_results_" + (indexes[i] - 1))))
          .click();
      submitWaiter.get();
    }
    WaitingPageObject<T> disappearWaiter =
        ExpectWaiter.waiter(removalCondition(getAddButton()), returnTo);
    getAddButton().click();
    return disappearWaiter.get();
  }

  public UniversalControl selectVideo(int index, String name) {
    return selectVideos(control.attachNameWaiter(name, false), index);
  }

  @Override
  public String getType() {
    return "YouTube";
  }

  @Override
  public WebElement getFindElement() {
    return getMainDiv();
  }
}
