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

package com.tle.web.institution;

import java.util.Collections;
import java.util.List;

import com.tle.web.institution.section.TabsSection;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.result.util.KeyLabel;

@SuppressWarnings("nls")
public abstract class AbstractInstitutionTab<T> extends AbstractPrototypeSection<T>
	implements
		HtmlRenderer,
		Tabable,
		Tab
{
	private PluginResourceHelper urlHelper = ResourcesService.getResourceHelper(getClass());

	@EventFactory
	private EventGenerator events;

	@TreeLookup
	protected InstitutionSection institutionSection;
	@TreeLookup
	private TabsSection tabSection;

	private JSHandler clickHandler;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		return new SimpleSectionResult("HELLOTAB");
	}

	@Override
	public List<Tab> getTabs(SectionInfo info)
	{
		if( isTabVisible(info) )
		{
			return Collections.singletonList((Tab) this);
		}
		return Collections.emptyList();
	}

	@Override
	public boolean shouldDefault(SectionInfo info)
	{
		return true;
	}

	protected abstract boolean isTabVisible(SectionInfo info);

	@Override
	public void gainedFocus(SectionInfo info, String tabId)
	{
		// not much
	}

	@Override
	public void lostFocus(SectionInfo info, String tabId)
	{
		SectionUtils.clearModel(info, this);
	}

	@EventHandlerMethod
	public void clicked(SectionInfo info)
	{
		info.clearModel(this);
		tabSection.changeTab(info, getSectionId());
	}

	@Override
	public JSHandler getClickHandler()
	{
		return clickHandler;
	}

	@Override
	public String getId()
	{
		return getSectionId();
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		clickHandler = events.getNamedHandler("clicked");
	}

	@Override
	public Label getName()
	{
		return new KeyLabel(urlHelper.key("tab." + getSectionId()));
	}
}
