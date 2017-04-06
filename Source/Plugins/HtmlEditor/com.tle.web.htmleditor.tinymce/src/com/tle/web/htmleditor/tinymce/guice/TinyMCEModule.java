package com.tle.web.htmleditor.tinymce.guice;

import com.google.inject.name.Names;
import com.tle.web.htmleditor.tinymce.actions.TinyMceActionSection;
import com.tle.web.sections.equella.guice.SectionsModule;

@SuppressWarnings("nls")
public class TinyMCEModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("mceaction")).toProvider(node(TinyMceActionSection.class));
	}
}
