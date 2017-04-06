package com.tle.core.mimetypes;

import com.tle.core.events.listeners.ApplicationListener;

public interface MimeTypesUpdatedListener extends ApplicationListener
{
	void clearMimeCache();
}