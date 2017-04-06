package com.tle.web.wizard.section;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.wizard.WizardState;

@NonNullByDefault
public interface WizardStateListener
{
	void handleWizardState(SectionInfo info, WizardState state);
}
