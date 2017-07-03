package com.tle.core.institution.events.listeners;

import com.tle.core.events.listeners.ApplicationListener;
import com.tle.core.institution.events.InstitutionEvent;

/**
 * @author Nicholas Read
 */
public interface InstitutionListener extends ApplicationListener
{
	void institutionEvent(InstitutionEvent event);
}
