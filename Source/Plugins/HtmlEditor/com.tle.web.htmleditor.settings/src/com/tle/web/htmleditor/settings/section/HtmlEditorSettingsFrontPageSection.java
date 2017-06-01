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

package com.tle.web.htmleditor.settings.section;

import java.util.List;

import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.htmleditor.settings.section.ModalHtmlEditorSettingsSection.SettingInfo;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.BeforeEventsListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.registry.handler.CollectInterfaceHandler;
import com.tle.web.sections.render.BrRenderer;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.LinkRenderer;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@TreeIndexed
public class HtmlEditorSettingsFrontPageSection
	extends
		AbstractPrototypeSection<HtmlEditorSettingsFrontPageSection.HtmlEditorSettingsFrontPageModel>
	implements
		HtmlRenderer,
		BeforeEventsListener
{
	@PlugKey("settings.front.table.heading")
	private static Label LABEL_TABLE_HEADING;

	@TreeLookup
	private RootHtmlEditorSettingsSection root;

	@Component(name = "st")
	private Table settingsTable;
	private CollectInterfaceHandler<ModalHtmlEditorSettingsSection> subSettings;

	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory view;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		final List<ModalHtmlEditorSettingsSection> subPages = subSettings.getAllImplementors(context);
		for( ModalHtmlEditorSettingsSection subPage : subPages )
		{
			final SettingInfo settingInfo = subPage.getSettingInfo(context);
			if( settingInfo != null )
			{
				final HtmlLinkState link = new HtmlLinkState(settingInfo.getLinkTitle(), events.getNamedHandler(
					"linkClicked", settingInfo.getId()));
				settingsTable.addRow(context, CombinedRenderer.combineMultipleResults(new LinkRenderer(link),
					new BrRenderer(), new LabelRenderer(settingInfo.getBlurb())));
			}
		}
		return view.createResult("setting/front.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		settingsTable.setColumnHeadings(LABEL_TABLE_HEADING);
		settingsTable.setColumnSorts(Sort.PRIMARY_ASC);
		settingsTable.setWrap(true);
		settingsTable.setFilterable(true);

		subSettings = new CollectInterfaceHandler<ModalHtmlEditorSettingsSection>(ModalHtmlEditorSettingsSection.class);
		tree.addRegistrationHandler(subSettings);
	}

	@Override
	public void beforeEvents(SectionInfo info)
	{
		final HtmlEditorSettingsFrontPageModel model = getModel(info);
		final String link = model.getLink();
		final ModalHtmlEditorSettingsSection modalSection = getModalSection(info, link);
		root.setModalSection(info, modalSection);
	}

	@EventHandlerMethod
	public void linkClicked(SectionInfo info, String link)
	{
		final HtmlEditorSettingsFrontPageModel model = getModel(info);
		model.setLink(link);
		final ModalHtmlEditorSettingsSection modalSection = getModalSection(info, link);
		if( modalSection != null )
		{
			modalSection.startSession(info);
		}
	}

	private ModalHtmlEditorSettingsSection getModalSection(SectionInfo info, String type)
	{
		if( type != null )
		{
			final List<ModalHtmlEditorSettingsSection> subPages = subSettings.getAllImplementors(info);
			for( ModalHtmlEditorSettingsSection subPage : subPages )
			{
				SettingInfo settingInfo = subPage.getSettingInfo(info);
				if( settingInfo != null && settingInfo.getId().equals(type) )
				{
					return subPage;
				}
			}
		}
		return null;
	}

	public void returnToFrontPage(SectionInfo info)
	{
		final HtmlEditorSettingsFrontPageModel model = getModel(info);
		model.setLink(null);
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new HtmlEditorSettingsFrontPageModel();
	}

	public Table getSettingsTable()
	{
		return settingsTable;
	}

	public static class HtmlEditorSettingsFrontPageModel
	{
		@Bookmarked
		private String link;

		public String getLink()
		{
			return link;
		}

		public void setLink(String link)
		{
			this.link = link;
		}
	}
}
