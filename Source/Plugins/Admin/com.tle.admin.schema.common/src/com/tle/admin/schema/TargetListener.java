package com.tle.admin.schema;

import java.util.EventListener;

/**
 * Interface for listening to events from the target choosers.
 * 
 * @author Nicholas Read
 */
public interface TargetListener extends EventListener
{
	/**
	 * Fired when a new target is added.
	 */
	void targetAdded(String target);

	/**
	 * Fired when a target is removed.
	 */
	void targetRemoved(String target);
}
