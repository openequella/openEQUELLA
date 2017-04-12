package com.tle.core.pss.notification;

import com.tle.core.guice.BindFactory;

@BindFactory
public interface PearsonScormServicesOperationFactory
{
	NotifyBadPearsonScormServicesOperation notifyContributor();
}
