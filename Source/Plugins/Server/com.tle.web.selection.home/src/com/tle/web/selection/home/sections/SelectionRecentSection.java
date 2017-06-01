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

package com.tle.web.selection.home.sections;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourceHelper;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.ViewableChildInterface;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.component.Box;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.js.generic.ReloadHandler;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.SingleSelectionList;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.event.ValueSetListener;
import com.tle.web.sections.standard.model.SimpleHtmlListModel;
import com.tle.web.sections.standard.model.SimpleOption;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.home.RecentSelectionsSegment;

public class SelectionRecentSection
	extends
		AbstractPrototypeSection<SelectionRecentSection.SelectionRecentSectionModel>
	implements
		HtmlRenderer,
		ViewableChildInterface,
		ValueSetListener<Set<String>>
{
	@PlugKey("recent.title")
	private static Label TITLE_LABEL;

	@ResourceHelper
	private PluginResourceHelper helper;

	@Inject
	private SelectionService selectionService;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component(register = false)
	private Box box;
	@Component
	private SingleSelectionList<String> recentType;

	private String layout;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		if( !canView(context) )
		{
			return null;
		}

		SelectionSession session = selectionService.getCurrentSession(context);
		SimpleHtmlListModel<String> listModel = new SimpleHtmlListModel<String>();

		List<SectionId> children = context.getChildIds(context);
		for( SectionId string : children )
		{
			SectionId sectionForId = context.getSectionForId(string);
			if( sectionForId instanceof RecentSelectionsSegment )
			{
				String title = ((RecentSelectionsSegment) sectionForId).getTitle(context, session);
				listModel.add(new SimpleOption<String>(title, helper.key(title), string.getSectionId()));
			}
		}
		recentType.setListModel(listModel);
		NestedRenderable result = (NestedRenderable) SectionUtils.renderSectionResult(context, box);

		SectionRenderable menu = viewFactory.createResult("selectionrecent.ftl", this); //$NON-NLS-1$
		String recentId = getModel(context).getRecentId();
		if( Check.isEmpty(recentId) )
		{
			recentId = children.get(0).getSectionId();
		}
		SectionRenderable list = SectionUtils.renderSection(context, recentId);

		result.setNestedRenderable(new CombinedRenderer(menu, list));

		return result;
	}

	public Box getBox()
	{
		return box;
	}

	@Override
	public boolean canView(SectionInfo info)
	{
		final SelectionSession session = selectionService.getCurrentSession(info);
		return session != null && (session.isAllCollections() || !Check.isEmpty(session.getCollectionUuids()));
	}

	@Override
	@SuppressWarnings("nls")
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(box, id);
		recentType.addChangeEventHandler(new ReloadHandler());
		recentType.setAlwaysSelect(true);
		recentType.setValueSetListener(this);

		box.setNoMinMaxOnHeader(true);
		box.setStyleClass("recent-portal");
		box.setLabel(TITLE_LABEL);

		if( !Check.isEmpty(layout) )
		{
			tree.setLayout(id, layout);
		}
	}

	@Override
	public void valueSet(SectionInfo info, Set<String> value)
	{
		String sectionId = recentType.getSelectedValue(info);
		getModel(info).setRecentId(sectionId);
	}

	public SingleSelectionList<String> getRecentType()
	{
		return recentType;
	}

	@Override
	public Class<SelectionRecentSectionModel> getModelClass()
	{
		return SelectionRecentSectionModel.class;
	}

	public void setLayout(String layout)
	{
		this.layout = layout;
	}

	public String getLayout()
	{
		return layout;
	}

	public static class SelectionRecentSectionModel
	{
		private String recentId;

		public void setRecentId(String recentId)
		{
			this.recentId = recentId;
		}

		public String getRecentId()
		{
			return recentId;
		}

	}

}
