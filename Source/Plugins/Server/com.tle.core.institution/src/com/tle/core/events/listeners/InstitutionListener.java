package com.tle.core.events.listeners;

import com.tle.core.events.InstitutionEvent;

/**
 * @author Nicholas Read
 */
public interface InstitutionListener extends ApplicationListener
{
	void institutionEvent(InstitutionEvent event);
}
