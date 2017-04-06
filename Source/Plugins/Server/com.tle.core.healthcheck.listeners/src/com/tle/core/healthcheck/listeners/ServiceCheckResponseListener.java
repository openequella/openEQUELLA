package com.tle.core.healthcheck.listeners;

import com.tle.core.events.ApplicationEvent;
import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus;

public interface ServiceCheckResponseListener extends ApplicationListener
{
	void serviceCheckResponse(String requesterNodeId, String responderNodeId,
		ServiceStatus serviceStatus);

	public static class CheckServiceResponseEvent extends ApplicationEvent<ServiceCheckResponseListener>
	{
		String requesterNodeId;
		String responderNodeId;
		String eventId;
		ServiceStatus serviceStatus;

		public CheckServiceResponseEvent(String requesterNodeId, String responderNodeId,
			ServiceStatus serviceStatus)
		{
			super(PostTo.POST_TO_ALL_CLUSTER_NODES);
			this.requesterNodeId = requesterNodeId;
			this.responderNodeId = responderNodeId;
			this.serviceStatus = serviceStatus;
		}

		@Override
		public Class<ServiceCheckResponseListener> getListener()
		{
			return ServiceCheckResponseListener.class;
		}

		@Override
		public void postEvent(ServiceCheckResponseListener listener)
		{
			listener.serviceCheckResponse(requesterNodeId, responderNodeId, serviceStatus);
		}
	}
}
