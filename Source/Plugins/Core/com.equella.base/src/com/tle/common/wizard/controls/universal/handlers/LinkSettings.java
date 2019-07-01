package com.tle.common.wizard.controls.universal.handlers;

import com.dytech.edge.wizard.beans.control.CustomControl;
import com.tle.common.wizard.controls.universal.UniversalSettings;

public class LinkSettings extends UniversalSettings {
  private static final String DUPLICATIONCHECK = "LINK_DUPLICATIONCHECK";

  public LinkSettings(CustomControl customControl) {
    super(customControl);
  }

  public LinkSettings(UniversalSettings settings) {
    super(settings.getWrapped());
  }

  public boolean isDuplicationCheck() {
    return wrapped.getBooleanAttribute(DUPLICATIONCHECK, false);
  }

  public void setDuplicationCheck(boolean duplicationCheck) {
    wrapped.getAttributes().put(DUPLICATIONCHECK, duplicationCheck);
  }
}
