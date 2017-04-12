package com.tle.web.kaltura.guice;

import com.google.inject.name.Names;
import com.tle.web.kaltura.section.KalturaServerEditorSection;
import com.tle.web.kaltura.section.KalturaServerListSection;
import com.tle.web.kaltura.section.RootKalturaServerSection;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class KalturaModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/access/kalturasettings")).toProvider(kalturaTree());
	}

	private NodeProvider kalturaTree()
	{
		NodeProvider node = node(RootKalturaServerSection.class);
		node.innerChild(KalturaServerEditorSection.class);
		node.child(KalturaServerListSection.class);
		return node;
	}
}
