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

package com.tle.web.mimetypes.search.section;

import com.tle.web.mimetypes.section.MimeTypesEditSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.EquellaButtonExtension;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.annotations.Component;

@SuppressWarnings("nls")
public class AddMimeAction extends AbstractPrototypeSection<Object> implements HtmlRenderer
{
	@EventFactory
	protected EventGenerator events;

	@Component
	@PlugKey("searchtypes.button.add")
	private Button addMimeButton;

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		super.treeFinished(id, tree);

		addMimeButton.setStyleClass("add-mime");
		addMimeButton.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		addMimeButton.setClickHandler(events.getNamedHandler("addMime"));
	}

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return SectionUtils.renderSectionResult(context, addMimeButton);
	}

	@EventHandlerMethod
	public void addMime(SectionInfo info)
	{
		MimeTypesEditSection.newEntry(info);
	}
}
