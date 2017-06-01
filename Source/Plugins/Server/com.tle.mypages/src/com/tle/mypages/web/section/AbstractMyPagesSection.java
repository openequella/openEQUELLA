/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.mypages.web.section;

import com.tle.mypages.web.event.LoadItemEventListener;
import com.tle.mypages.web.event.SaveItemEventListener;
import com.tle.mypages.web.event.SavePageEventListener;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;

/**
 * @author aholland
 */
public abstract class AbstractMyPagesSection<M> extends AbstractPrototypeSection<M>
{
	protected static final PluginResourceHelper RESOURCES = ResourcesService
		.getResourceHelper(AbstractMyPagesSection.class);

	@EventFactory
	protected EventGenerator events;

	@ViewFactory(fixed = false)
	protected FreemarkerFactory viewFactory;

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		if( this instanceof SavePageEventListener )
		{
			tree.addListener(null, SavePageEventListener.class, id);
		}

		if( this instanceof LoadItemEventListener )
		{
			tree.addListener(null, LoadItemEventListener.class, id);
		}

		if( this instanceof SaveItemEventListener )
		{
			tree.addListener(null, SaveItemEventListener.class, id);
		}
	}
}
