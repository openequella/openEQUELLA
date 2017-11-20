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

package com.tle.core.healthcheck.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.tle.core.guice.Bind;
import com.tle.core.healthcheck.HealthCheckService;
import com.tle.core.healthcheck.listeners.ServiceCheckRequestListener.CheckServiceRequestEvent;
import com.tle.core.healthcheck.listeners.ServiceCheckResponseListener;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus.ServiceName;
import com.tle.core.events.services.EventService;
import com.tle.core.zookeeper.ZookeeperService;

@Bind(HealthCheckService.class)
@Singleton
public class HealthCheckServiceImpl implements HealthCheckService, ServiceCheckResponseListener
{
	@Inject
	private EventService eventService;
	@Inject
	private ZookeeperService zkService;

	// KEY: nodeId, VALUE: ServiceStatus for each implementer of
	// ServiceCheckRequestListener
	private final Cache<String, List<ServiceStatus>> responseCache = CacheBuilder.newBuilder()
		.expireAfterWrite(1, TimeUnit.MINUTES).softValues().build();

	@Override
	public void startCheckRequest()
	{
		String thisNodeId = zkService.getNodeId();
		if( responseCache.getIfPresent(thisNodeId) == null )
		{
			if( zkService.isCluster() )
			{
				for( String appServer : zkService.getAppServers() )
				{
					responseCache.put(appServer, buildWaitingStatus());
				}
			}
			else
			{
				responseCache.put(thisNodeId, buildWaitingStatus());
			}
			eventService.publishApplicationEvent(new CheckServiceRequestEvent(thisNodeId));
		}
	}

	private List<ServiceStatus> buildWaitingStatus()
	{
		ArrayList<ServiceStatus> waitingStatus = Lists.newArrayList();
		for( ServiceName name : ServiceName.values() )
		{
			waitingStatus.add(new ServiceStatus(name));
		}
		return waitingStatus;
	}

	@Override
	public void serviceCheckResponse(String requesterId, String responderId, ServiceStatus serviceStatus)
	{
		if( requesterId.equals(zkService.getNodeId()) )
		{
			List<ServiceStatus> statuses = responseCache.getIfPresent(responderId);
			if( statuses == null )
			{
				statuses = buildWaitingStatus();
				responseCache.put(responderId, statuses);
			}
			updateStatus(statuses, serviceStatus);
			responseCache.put(responderId, statuses);

		}
	}

	private void updateStatus(List<ServiceStatus> statuses, ServiceStatus serviceStatus)
	{
		for( ServiceStatus current : statuses )
		{
			if( current.getServiceName().equals(serviceStatus.getServiceName()) )
			{
				current.setServiceStatus(serviceStatus.getServiceStatus());
				current.setMoreInfo(serviceStatus.getMoreInfo());
			}
		}
	}

	@Override
	public List<ServiceStatus> retrieveForSingleNode(String nodeId)
	{
		List<ServiceStatus> statuses = responseCache.getIfPresent(nodeId);
		if( statuses == null )
		{
			statuses = buildWaitingStatus();
			responseCache.put(nodeId, statuses);
		}
		return statuses;
	}

	@Override
	public List<ServiceStatus> retrieveForNonCluster()
	{
		return responseCache.getIfPresent(zkService.getNodeId());
	}

}
