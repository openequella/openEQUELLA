package com.tle.webtests.pageobject.wizard.controls.universal;

import com.tle.webtests.pageobject.PageObject;
import com.tle.webtests.pageobject.wizard.controls.UniversalControl;

public interface AttachmentEditPage extends PageObject {
  String getName();

  void setName(String name);

  UniversalControl save();

  UniversalControl close();
}
