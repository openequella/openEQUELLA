package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.WaitingPageObject;

public interface AttachmentType<T extends AttachmentType<T, E>, E extends AttachmentEditPage>
    extends WaitingPageObject<T>, PageObject {
  String getType();

  E edit();
}
