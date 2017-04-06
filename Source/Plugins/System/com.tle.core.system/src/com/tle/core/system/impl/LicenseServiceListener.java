package com.tle.core.system.impl;

import com.dytech.edge.common.valuebean.License;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.events.listeners.ApplicationListener;

public interface LicenseServiceListener extends ApplicationListener
{
	void newLicense(License license);

	public static class LicenseServiceEvent extends ApplicationEvent<LicenseServiceListener>
	{
		private static final long serialVersionUID = 1L;

		private License license;

		public LicenseServiceEvent(License license)
		{
			super(PostTo.POST_TO_OTHER_CLUSTER_NODES);
			this.license = license;
		}

		@Override
		public void postEvent(LicenseServiceListener listener)
		{
			listener.newLicense(license);
		}

		@Override
		public Class<LicenseServiceListener> getListener()
		{
			return LicenseServiceListener.class;
		}
	}
}
