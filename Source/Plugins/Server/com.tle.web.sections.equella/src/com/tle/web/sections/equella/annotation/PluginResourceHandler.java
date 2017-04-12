package com.tle.web.sections.equella.annotation;

import javax.inject.Singleton;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.registry.handler.CachedScannerHandler;

@Singleton
public class PluginResourceHandler extends CachedScannerHandler<AnnotatedPlugResourceScanner>
{
	private static PluginResourceHandler me;

	public PluginResourceHandler()
	{
		me = this; // NOSONAR
	}

	public static PluginResourceHandler inst()
	{
		return me;
	}

	@Override
	protected AnnotatedPlugResourceScanner newEntry(Class<?> clazz)
	{
		return new AnnotatedPlugResourceScanner(clazz, this);
	}

	@Override
	public void registered(String id, SectionTree tree, Section section)
	{
		getForClass(section.getClass()).setupLabels(section);
	}

	public static void init(Class<?> callerClass)
	{
		inst().getForClass(callerClass);
	}
}
