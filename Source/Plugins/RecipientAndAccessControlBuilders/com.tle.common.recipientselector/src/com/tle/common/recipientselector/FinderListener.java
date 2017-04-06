package com.tle.common.recipientselector;

import java.util.EventListener;

/**
 * @author Nicholas Read
 */
public interface FinderListener extends EventListener
{
	/**
	 * Called whenever the value of the selection changes.
	 * 
	 * @param e the event that characterizes the change.
	 */
	void valueChanged(FinderEvent e);
}
