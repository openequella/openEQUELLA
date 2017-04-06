package com.tle.core.url;

import com.tle.core.events.ApplicationEvent;

/**
 * @author Nicholas Read
 */
public class URLEvent extends ApplicationEvent<URLListener>
{
	private static final long serialVersionUID = 1L;

	public static enum URLEventType
	{
		URL_WARNING, URL_DISABLED
	}

	private final URLEventType type;
	private final String url;

	public URLEvent(URLEventType type, String url)
	{
		super(PostTo.POST_TO_SELF_SYNCHRONOUSLY);
		this.type = type;
		this.url = url;
	}

	public URLEventType getType()
	{
		return type;
	}

	public String getUrl()
	{
		return url;
	}

	@Override
	public Class<URLListener> getListener()
	{
		return URLListener.class;
	}

	@Override
	public void postEvent(URLListener listener)
	{
		listener.urlEvent(this);
	}
}
