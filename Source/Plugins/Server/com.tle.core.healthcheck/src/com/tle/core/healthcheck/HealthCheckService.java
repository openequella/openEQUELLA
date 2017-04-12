package com.tle.core.healthcheck;

import java.util.List;

import com.tle.core.healthcheck.listeners.bean.ServiceStatus;



public interface HealthCheckService
{
	void startCheckRequest();

	List<ServiceStatus> retrieveForSingleNode(String nodeId);

	List<ServiceStatus> retrieveForNonCluster();
}
