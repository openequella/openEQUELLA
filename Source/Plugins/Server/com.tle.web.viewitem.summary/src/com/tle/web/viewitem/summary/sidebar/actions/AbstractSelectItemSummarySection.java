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

package com.tle.web.viewitem.summary.sidebar.actions;

import javax.inject.Inject;

import com.tle.beans.item.IItem;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ViewableChildInterface;
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
import com.tle.web.selection.SelectionService;
import com.tle.web.viewable.ViewableItem;

public abstract class AbstractSelectItemSummarySection<I extends IItem<?>, M> extends AbstractPrototypeSection<M>
	implements
		ViewableChildInterface,
		HtmlRenderer
{
	@Inject
	protected SelectionService selectionService;

	@Component
	@PlugKey("summary.sidebar.actions.selectitem.title")
	private Button button;

	@EventFactory
	protected EventGenerator events;

	protected abstract ViewableItem<I> getViewableItem(SectionInfo info);

	protected abstract String getItemExtensionType();

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		if( !canView(context) )
		{
			return null;
		}
		return SectionUtils.renderSectionResult(context, button);
	}

	@Override
	@SuppressWarnings("nls")
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		button.setStyleClass("select");
		button.setDefaultRenderer(EquellaButtonExtension.ACTION_BUTTON);
		button.setClickHandler(events.getNamedHandler("select"));
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		final ViewableItem<I> vitem = getViewableItem(info);
		return selectionService.canSelectItem(info, vitem)
			&& !selectionService.isSelected(info, vitem.getItemId(), null, getItemExtensionType(), false);
	}

	@EventHandlerMethod
	public void select(SectionInfo info)
	{
		selectionService.addSelectedItem(info, getViewableItem(info).getItem(), null, getItemExtensionType());
	}
}
