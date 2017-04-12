package com.tle.core.mimetypes;

import com.tle.core.events.ApplicationEvent;

/**
 * @author jmaginnis
 */
public class MimeTypesUpdatedMessage extends ApplicationEvent<MimeTypesUpdatedListener>
{
	public MimeTypesUpdatedMessage()
	{
		super(PostTo.POST_TO_ALL_CLUSTER_NODES);
	}

	@Override
	public Class<MimeTypesUpdatedListener> getListener()
	{
		return MimeTypesUpdatedListener.class;
	}

	@Override
	public void postEvent(MimeTypesUpdatedListener listener)
	{
		listener.clearMimeCache();
	}
}
