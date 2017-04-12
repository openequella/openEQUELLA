package com.tle.web.freemarker.annotations;

import javax.inject.Singleton;

import com.google.inject.Inject;
import com.tle.core.guice.Bind;
import com.tle.core.plugins.PluginService;
import com.tle.web.freemarker.CustomTemplateLoader;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.registry.handler.CachedScannerHandler;

@Bind
@Singleton
public class FreemarkerFactoryHandler extends CachedScannerHandler<AnnotatedViewFactoryScanner>
{
	@Inject
	private CustomTemplateLoader templateLoader;
	@Inject
	private PluginService pluginService;

	@Override
	public void registered(String id, SectionTree tree, final Section section)
	{
		final AnnotatedViewFactoryScanner scanner = getForClass(section.getClass());
		scanner.setupFactories(section, templateLoader, pluginService);
	}

	@Override
	protected AnnotatedViewFactoryScanner newEntry(Class<?> clazz)
	{
		return new AnnotatedViewFactoryScanner(clazz, this);
	}

}
