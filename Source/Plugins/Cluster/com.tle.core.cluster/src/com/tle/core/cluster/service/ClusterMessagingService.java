package com.tle.core.cluster.service;

import java.io.Serializable;

public interface ClusterMessagingService
{
	static final int MAX_MSG_SIZE = 5 * 1024 * 1024;
	static final int MAX_QUEUE_SIZE = 20 * 1024 * 1024;

	void postMessage(Serializable msg);

	void postMessage(String toNodeIdOnly, Serializable msg);
}
