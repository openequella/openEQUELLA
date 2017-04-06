package com.tle.web.controls.universal;

import com.tle.core.guice.PluginTrackerModule;
import com.tle.core.plugins.PluginTracker;
import com.tle.web.controls.universal.handlers.fileupload.PackageAttachmentHandler;

public class UniversalModule extends PluginTrackerModule
{

	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bindTracker(AttachmentHandler.class, "universalResourceHandler", "class").setIdParam(
			PluginTracker.LOCAL_ID_FOR_KEY).orderByParameter("order");

		bindTracker(PackageAttachmentHandler.class, "packageAttachmentHandler", "class").setIdParam("type");
	}

}
