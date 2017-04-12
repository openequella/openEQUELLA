package com.tle.web.portal.events;

import com.tle.core.events.ApplicationEvent;

/**
 * @author aholland
 */
public class PortletsUpdatedEvent extends ApplicationEvent<PortletsUpdatedEventListener>
{
	public enum PortletUpdateEventType
	{
		MOVED, EDITED, CREATED,
		/**
		 * Closed or deleted
		 */
		REMOVED
	}

	private static final long serialVersionUID = 1L;

	private final String userId;
	private final String portletUuid;
	private final boolean institutional;
	private final PortletUpdateEventType type;

	public PortletsUpdatedEvent(String userId, String portletUuid, boolean institutional, PortletUpdateEventType type)
	{
		super(PostTo.POST_TO_OTHER_CLUSTER_NODES);
		this.userId = userId;
		this.portletUuid = portletUuid;
		this.institutional = institutional;
		this.type = type;
	}

	/**
	 * @return May be null, in which case all users are (or potentially are)
	 *         affected
	 */
	public String getUserId()
	{
		return userId;
	}

	/**
	 * @return The portlet that was moved or removed. May be null if the portlet
	 *         that triggered the event is not relevant.
	 */
	public String getPortletUuid()
	{
		return portletUuid;
	}

	public PortletUpdateEventType getType()
	{
		return type;
	}

	public boolean isInstitutional()
	{
		return institutional;
	}

	@Override
	public Class<PortletsUpdatedEventListener> getListener()
	{
		return PortletsUpdatedEventListener.class;
	}

	@Override
	public void postEvent(PortletsUpdatedEventListener listener)
	{
		listener.portletsUpdated(this, null);
	}
}