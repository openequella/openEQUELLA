package com.tle.web.resources;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.registry.handler.CachedScannerHandler;

@Bind
@Singleton
public class ResourceHelperHandler extends CachedScannerHandler<AnnotatedResourceHelperScanner>
{

	@Override
	protected AnnotatedResourceHelperScanner newEntry(Class<?> clazz)
	{
		return new AnnotatedResourceHelperScanner(clazz, this);
	}

	@Override
	public void registered(String id, SectionTree tree, Section section)
	{
		getForClass(section.getClass()).setup(section);
	}

}
