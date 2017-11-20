/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
