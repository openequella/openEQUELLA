package com.tle.web.wizard.page;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.DefaultWizardPage;

public interface WizardPageService
{
	WizardPage createSimplePage(DefaultWizardPage wizardPage, PropBagEx docxml, WebWizardPageState state, boolean expert);
}
