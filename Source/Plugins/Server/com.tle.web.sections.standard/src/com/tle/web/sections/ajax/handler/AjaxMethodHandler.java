package com.tle.web.sections.ajax.handler;

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.convert.Conversion;
import com.tle.web.sections.events.js.EventGeneratorListener;
import com.tle.web.sections.registry.handler.CachedScannerHandler;

@Bind
@Singleton
public class AjaxMethodHandler extends CachedScannerHandler<AnnotatedAjaxMethodScanner>
{
	@Inject
	private Conversion conversion;

	@Override
	public void registered(String id, SectionTree tree, Section section)
	{
		AnnotatedAjaxMethodScanner eventFactoryHandler = getForClass(section.getClass());
		Collection<AjaxGeneratorImpl> generators = eventFactoryHandler.registerAjaxFactories(section, id, tree);
		if( !generators.isEmpty() )
		{
			EventGeneratorListener listener = EventGeneratorListener.getForTree(tree);
			for( AjaxGeneratorImpl generator : generators )
			{
				generator.registerWithListener(listener);
			}
		}
	}

	@Override
	protected AnnotatedAjaxMethodScanner newEntry(Class<?> clazz)
	{
		return new AnnotatedAjaxMethodScanner(clazz, this, conversion);
	}
}
