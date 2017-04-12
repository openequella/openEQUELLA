package com.tle.core.services.impl;


public interface ClusterMessageHandler
{
	/**
	 * @return a runnable to process the message, else null if this handler
	 *         doesn't know how to handle it.
	 */
	Runnable canHandle(Object msg);
}
