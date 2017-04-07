package com.tle.web.sections.registry.handler;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.convert.Conversion;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.EventGeneratorListener;
import com.tle.web.sections.events.js.ParameterizedEvent;

@Bind
@Singleton
public class EventFactoryHandler extends CachedScannerHandler<AnnotatedEventsScanner>
{
	@Inject
	private Conversion conversion;

	@Override
	public void registered(String id, SectionTree tree, Section section)
	{
		AnnotatedEventsScanner eventFactoryHandler = getForClass(section.getClass());
		EventGenerator generator = eventFactoryHandler.registerEventFactories(section, id, tree);
		if( generator != null )
		{
			EventGeneratorListener listener = EventGeneratorListener.getForTree(tree);
			for( ParameterizedEvent pevent : generator.getEventsToRegister() )
			{
				listener.registerHandler(pevent);
			}
		}
	}

	@Override
	protected AnnotatedEventsScanner newEntry(Class<?> clazz)
	{
		return new AnnotatedEventsScanner(clazz, this, conversion);
	}
}
