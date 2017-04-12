/*
 * Created on 29/11/2005
 */

package com.tle.common.applet.client;

import java.net.URL;

import com.tle.common.applet.SessionHolder;

public interface ClientService
{
	void showDocument(URL url, String string);

	void stop();

	String getParameter(String key);

	URL getServerURL();

	SessionHolder getSession();

	/* SERVICES */

	<T> T getService(Class<T> clazz);
}