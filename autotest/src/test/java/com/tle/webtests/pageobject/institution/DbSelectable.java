package com.tle.webtests.pageobject.institution;

import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;

public interface DbSelectable<T extends DbSelectable<T>> extends PageObject {
  WaitingPageObject<T> getUpdateWaiter();
}
