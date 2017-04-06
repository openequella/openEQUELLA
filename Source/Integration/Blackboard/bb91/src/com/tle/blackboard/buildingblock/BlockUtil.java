package com.tle.blackboard.buildingblock;

import javax.servlet.http.HttpServletRequest;

import com.tle.blackboard.common.PathUtils;

@SuppressWarnings("nls")
public abstract class BlockUtil
{
	private BlockUtil()
	{
		throw new Error();
	}

	public static String getBBRootUrl(HttpServletRequest request)
	{
		// Eww
		StringBuilder serverUrl = new StringBuilder("http").append((request.isSecure() ? "s" : "")).append("://")
			.append(request.getServerName());
		final int port = request.getServerPort();
		if( port != 80 && port != 443 )
		{
			serverUrl.append(":").append(port);
		}
		serverUrl.append("/");
		return serverUrl.toString();
	}

	public static String getBbUrl(HttpServletRequest request, String path)
	{
		return PathUtils.urlPath(getBBRootUrl(request), path);
	}
}
