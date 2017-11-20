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

package com.tle.web.viewitem.summary.sidebar;

import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import com.google.common.collect.Lists;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.plugins.PluginTracker.ExtensionParamComparator;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.render.HideableFromDRMSection;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.viewitem.section.AbstractParentViewItemSection;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionSection;

@SuppressWarnings("nls")
public class MinorActionsGroupSection
	extends
		AbstractParentViewItemSection<MinorActionsGroupSection.MinorActionsGroupModel>
	implements HideableFromDRMSection
{
	private PluginTracker<GenericMinorActionSection> minorActionsTracker;
	private List<GenericMinorActionSection> minorActionSections;

	@Override
	public SectionResult renderHtml(final RenderEventContext context)
	{
		if( !canView(context) || !getItemInfo(context).getViewableItem().isItemForReal() )
		{
			return null;
		}

		final MinorActionsGroupModel model = getModel(context);
		final List<SectionRenderable> renderables = Lists.newArrayList();

		List<GenericMinorActionSection> sortedSections = Lists.newArrayList(minorActionSections);
		sortedSections.sort(new Comparator<GenericMinorActionSection>()
		{
			@Override
			public int compare(GenericMinorActionSection mas1, GenericMinorActionSection mas2)
			{
				return mas1.getLinkText().compareTo(mas2.getLinkText());
			}
		});

		sortedSections.stream().forEachOrdered(section -> {
			final SectionRenderable renderable = renderSection(context, section);
			if( renderable != null )
			{
				renderables.add(renderable);
			}
		});
		model.setSections(renderables);

		return viewFactory.createResult("viewitem/summary/sidebar/basiclistgroup.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		minorActionSections = Lists.newArrayList(minorActionsTracker.getBeanList());
		minorActionSections.stream().forEach(section -> tree.registerInnerSection(section, id));
	}

	// Called by Freemarker
	public String getGroupTitleKey()
	{
		return "summary.sidebar.actionsgroup.title";
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		return getModel(info).isHide() ? false : Lists.newArrayList(minorActionSections).stream()
			.filter(derp -> derp.canView(info)).findFirst().isPresent();
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "";
	}

	@Override
	public void showSection(SectionInfo info, boolean show)
	{
		getModel(info).setHide(!show);
	}

	@Override
	public Class<MinorActionsGroupModel> getModelClass()
	{
		return MinorActionsGroupModel.class;
	}

	@Inject
	public void setPluginService(PluginService pluginService)
	{
		minorActionsTracker = new PluginTracker<GenericMinorActionSection>(pluginService,
			"com.tle.web.viewitem.summary", "minorAction", "id", new ExtensionParamComparator());
		minorActionsTracker.setBeanKey("class");
	}

	public static class MinorActionsGroupModel
	{
		private List<SectionRenderable> sections;

		private boolean hide;

		public List<SectionRenderable> getSections()
		{
			return sections;
		}

		public void setSections(List<SectionRenderable> sections)
		{
			this.sections = sections;
		}

		public boolean isHide()
		{
			return hide;
		}

		public void setHide(boolean hide)
		{
			this.hide = hide;
		}
	}
}
