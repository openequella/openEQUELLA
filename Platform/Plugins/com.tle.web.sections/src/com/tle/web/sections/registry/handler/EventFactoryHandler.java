/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
