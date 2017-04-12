package com.tle.core.healthcheck.listeners;

import com.tle.core.events.ApplicationEvent;
import com.tle.core.events.listeners.ApplicationListener;

public interface ServiceCheckRequestListener extends ApplicationListener
{
	void checkServiceRequest(CheckServiceRequestEvent request);

	public static class CheckServiceRequestEvent extends ApplicationEvent<ServiceCheckRequestListener>
	{
		String requetserNodeId;

		public CheckServiceRequestEvent(String requesterNodeId)
		{
			super(PostTo.POST_TO_ALL_CLUSTER_NODES);
			this.requetserNodeId = requesterNodeId;
		}

		@Override
		public Class<ServiceCheckRequestListener> getListener()
		{
			return ServiceCheckRequestListener.class;
		}

		@Override
		public void postEvent(ServiceCheckRequestListener listener)
		{
			listener.checkServiceRequest(this);
		}

		public String getRequetserNodeId()
		{
			return requetserNodeId;
		}



	}
}
