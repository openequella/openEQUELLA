package com.tle.web.htmleditor.tinymce.addon.tle.guice;

import com.google.inject.name.Names;
import com.tle.web.htmleditor.tinymce.addon.tle.selection.FileUploadSection;
import com.tle.web.sections.equella.guice.SectionsModule;

public class TinyMCEModule extends SectionsModule
{
	@SuppressWarnings("nls")
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("com.tle.web.htmleditor.tinymce.addon.tle.FileUpload"))
			.toProvider(node(FileUploadSection.class));
	}

}
