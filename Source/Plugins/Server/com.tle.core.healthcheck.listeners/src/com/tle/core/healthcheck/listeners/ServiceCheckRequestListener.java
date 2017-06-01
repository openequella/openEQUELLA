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
