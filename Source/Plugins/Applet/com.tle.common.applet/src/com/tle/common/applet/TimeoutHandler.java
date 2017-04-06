/*
 * Created on Jan 6, 2005
 */
package com.tle.common.applet;

/**
 * @author Nicholas Read
 */
public interface TimeoutHandler
{
	/**
	 * Called in the event that the session has timed out, and allows the user
	 * to log back in.
	 * 
	 * @param service a direct interface to the soap methods.
	 * @param oldSession the old session ID.
	 * @return a new session ID.
	 */
	void sessionTimeout();
}
