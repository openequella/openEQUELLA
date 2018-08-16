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

package com.tle.web.sections.ajax.handler;

import com.google.common.collect.ImmutableSet;
import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.EventGeneratorListener;

@SuppressWarnings("nls")
public final class InnerBodyEvent extends UpdateDomEvent
{
	private static final String BODY_EVENT = "$UP$<BODY>";
	private static InnerBodyEvent INSTANCE = new InnerBodyEvent();

	private InnerBodyEvent()
	{
		super(BODY_EVENT, new UpdateDomKey(null, ImmutableSet.of(AjaxGenerator.AJAXID_BODY), null));
	}

	@Override
	public SectionEvent<?> createEvent(SectionInfo info, String[] params)
	{
		super.createEvent(info, params);
		ParametersEvent event = null;
		if( params.length > 0 )
		{
			MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
			event = new ParametersEvent(EventGeneratorListener.convertToParamMap(params), false);
			minfo.addParametersEvent(event);
		}
		return event;
	}

	@Override
	public int getParameterCount()
	{
		return -1;
	}

	public static void ensureRegistered(SectionTree tree)
	{
		EventGeneratorListener listener = EventGeneratorListener.getForTree(tree);
		synchronized( listener )
		{
			if( listener.getRegisteredHandler(BODY_EVENT) == null )
			{
				listener.registerHandler(INSTANCE);
			}
		}
	}

	public static void ensureRegistered(SectionInfo info)
	{
		MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
		ensureRegistered(minfo.getRootTree());
	}
}
