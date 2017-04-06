/*
 * Created on Jan 13, 2005
 */
package com.tle.client.harness;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Nicholas Read
 */
public class HarnessConfig
{
	private String lastSelectedName;
	private ProxySettings proxy;
	private Collection<ServerProfile> servers;

	public HarnessConfig()
	{
		servers = new ArrayList<ServerProfile>();
	}

	public String getLastSelectedName()
	{
		return lastSelectedName;
	}

	public void setLastSelectedName(String lastSelectedName)
	{
		this.lastSelectedName = lastSelectedName;
	}

	public ProxySettings getProxy()
	{
		return proxy;
	}

	public void setProxy(ProxySettings proxy)
	{
		this.proxy = proxy;
	}

	public Collection<ServerProfile> getServers()
	{
		return servers;
	}

	public void setServers(Collection<ServerProfile> servers)
	{
		this.servers = servers;
	}
}
