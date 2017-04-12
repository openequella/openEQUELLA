package com.tle.web.sections.standard.impl;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.registry.handler.CachedScannerHandler;
import com.tle.web.sections.standard.ComponentFactory;

@Bind
@Singleton
public class ComponentRegistrationHandler extends CachedScannerHandler<ComponentScanner>
{
	@Inject
	private ComponentFactory factory;

	@Override
	protected ComponentScanner newEntry(Class<?> clazz)
	{
		return new ComponentScanner(clazz, this);
	}

	@Override
	public void registered(String id, SectionTree tree, Section section)
	{
		ComponentScanner scanner = getForClass(section.getClass());
		scanner.registerComponents(id, tree, section, factory);
	}
}
