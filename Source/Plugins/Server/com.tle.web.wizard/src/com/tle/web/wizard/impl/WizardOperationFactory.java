package com.tle.web.wizard.impl;

import com.tle.core.guice.BindFactory;
import com.tle.web.wizard.WizardState;

@BindFactory
public interface WizardOperationFactory
{

	WizardStateOperation state(WizardState state);
}
