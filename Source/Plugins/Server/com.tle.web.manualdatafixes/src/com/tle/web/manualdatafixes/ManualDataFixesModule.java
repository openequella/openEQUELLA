package com.tle.web.manualdatafixes;

import com.google.inject.name.Names;
import com.tle.web.manualdatafixes.fixes.AttachmentHashOpSection;
import com.tle.web.manualdatafixes.fixes.GenerateThumbnailOpSection;
import com.tle.web.manualdatafixes.fixes.ReindexOpSection;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class ManualDataFixesModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/manualdatafixes")).toProvider(
			node(RootManualDataFixesSettingsSection.class).placeHolder("FIXES").children(
				GenerateThumbnailOpSection.class, AttachmentHashOpSection.class, ReindexOpSection.class));
	}
}
