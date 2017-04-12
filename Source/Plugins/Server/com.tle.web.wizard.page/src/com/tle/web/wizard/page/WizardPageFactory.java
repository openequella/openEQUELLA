package com.tle.web.wizard.page;

import com.dytech.devlib.PropBagEx;
import com.tle.core.wizard.LERepository;

public interface WizardPageFactory
{
	WizardPage createWizardPage();

	LERepository createRepository(PropBagEx docxml, boolean expert);
}
