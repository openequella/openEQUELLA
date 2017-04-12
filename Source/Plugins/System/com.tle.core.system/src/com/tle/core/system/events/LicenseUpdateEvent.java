package com.tle.core.system.events;

import com.tle.core.events.ApplicationEvent;

/**
 * @author Nicholas Read
 */
public class LicenseUpdateEvent extends ApplicationEvent<LicenseChangeListener>
{
	private static final long serialVersionUID = 1L;

	public LicenseUpdateEvent()
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
	}

	@Override
	public Class<LicenseChangeListener> getListener()
	{
		return LicenseChangeListener.class;
	}

	@Override
	public void postEvent(LicenseChangeListener listener)
	{
		listener.licenseUpdated();
	}
}
