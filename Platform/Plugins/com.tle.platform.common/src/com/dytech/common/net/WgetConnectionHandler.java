/*
 * Created on Oct 1, 2004
 */
package com.dytech.common.net;

import java.net.URLConnection;

public interface WgetConnectionHandler
{
	void connectionMade(URLConnection connection);
}