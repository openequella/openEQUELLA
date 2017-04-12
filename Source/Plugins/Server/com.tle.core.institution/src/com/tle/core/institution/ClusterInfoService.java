package com.tle.core.institution;

import java.util.Map;

@SuppressWarnings("nls")
public interface ClusterInfoService
{
	String IP_LIST_ZKPATH = "ips";

	Map<String, String> getIpAddresses();
}
