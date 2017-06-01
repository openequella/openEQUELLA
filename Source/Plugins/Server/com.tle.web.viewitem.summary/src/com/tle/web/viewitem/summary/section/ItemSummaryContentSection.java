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

package com.tle.web.viewitem.summary.section;

import javax.inject.Inject;

import com.tle.common.Check;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SimpleSectionId;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.HtmlRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.Link;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.renderers.DivRenderer;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;
import com.tle.web.template.section.HelpAndScreenOptionsSection;
import com.tle.web.template.section.event.BlueBarEvent;
import com.tle.web.template.section.event.BlueBarEventListener;
import com.tle.web.viewitem.summary.ItemSummaryContent;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.viewitem.summary.content.MainItemContentSection;
import com.tle.web.viewurl.ViewItemResource;

@SuppressWarnings("nls")
public class ItemSummaryContentSection
	extends
		AbstractPrototypeSection<ItemSummaryContentSection.ItemSummaryContentModel>
	implements
		ItemSummaryContent,
		HtmlRenderer,
		BlueBarEventListener
{
	public static final String SUMMARY_TAB_ID = "summary";
	public static final String DETAILS_TAB_ID = "details";

	@TreeLookup
	private MainItemContentSection defaultSection;
	@TreeLookup
	private ItemDetailsAndActionsSummarySection itemDetailsAndActionsSummarySection;
	@Inject
	protected SelectionService selectionService;
	@ViewFactory
	protected FreemarkerFactory view;
	@EventFactory
	private EventGenerator events;

	@Component
	@PlugKey("summary.content.tab.summary")
	private Link summaryTabLink;
	@Component
	@PlugKey("summary.content.tab.details")
	private Link detailsTabLink;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		SectionRenderable renderSection = renderSection(context, getSummaryId(context));
		SelectionSession currentSession = selectionService.getCurrentSession(context);

		if( currentSession != null && currentSession.getLayout() == Layout.COURSE )
		{
			summaryTabLink.setClickHandler(context, events.getNamedHandler("onTabChange", SUMMARY_TAB_ID));
			detailsTabLink.setClickHandler(context, events.getNamedHandler("onTabChange", DETAILS_TAB_ID));

			ItemSummaryContentModel model = getModel(context);
			if( model.getTabId() == null )
			{
				model.setTabId(SUMMARY_TAB_ID);
			}
			model.setRenderable(renderSection);

			HelpAndScreenOptionsSection.addHelp(context,
				view.createResult("viewitem/summary/help/itemselectionhelp.ftl", this));

			return view.createResult("viewitem/summary_and_details_tabs.ftl", context);
		}

		return new DivRenderer("area", renderSection);
	}

	@EventHandlerMethod
	public void onTabChange(SectionInfo info, String tabId)
	{
		if( tabId.equals(SUMMARY_TAB_ID) )
		{
			getModel(info).setTabId(SUMMARY_TAB_ID);
			setSummaryId(info, null);
		}
		else
		{
			getModel(info).setTabId(DETAILS_TAB_ID);
			setSummaryId(info, itemDetailsAndActionsSummarySection);
		}
	}

	private SectionId getSummaryId(RenderContext context)
	{
		final ItemSummaryContentModel model = getModel(context);
		// if the subsiduarySectionId has been set, it has priority (so as to
		// enable the showing of a DRMFilterSection, for example)
		if( model.getSubsiduarySectionId() != null )
		{
			return model.getSubsiduarySectionId();
		}

		String sid = model.getSummaryId();
		if( Check.isEmpty(sid) )
		{
			sid = defaultSection.getSectionId();
		}
		return new SimpleSectionId(sid);
	}

	public void setSummaryId(SectionInfo info, SectionId sid)
	{
		getModel(info).setSummaryId(sid == null ? null : sid.getSectionId());
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "is";
	}

	@Override
	public void addBlueBarResults(RenderContext context, BlueBarEvent event)
	{
		SectionId s = context.getSectionForId(getSummaryId(context));
		if( s instanceof AbstractContentSection<?> )
		{
			event.addHelp(((AbstractContentSection<?>) s).renderHelp(context));
		}
	}

	public void setSubsiduarySectionId(SectionInfo info, SectionId sectionId)
	{
		getModel(info).setSubsiduarySectionId(sectionId);
	}

	public void ensureTree(SectionInfo info, ViewItemResource resource)
	{
		defaultSection.ensureTree(info, resource);
	}

	@Override
	public Class<ItemSummaryContentModel> getModelClass()
	{
		return ItemSummaryContentModel.class;
	}

	public Link getSummaryTabLink()
	{
		return summaryTabLink;
	}

	public Link getDetailsTabLink()
	{
		return detailsTabLink;
	}

	public static class ItemSummaryContentModel
	{
		@Bookmarked
		private String summaryId;

		/**
		 * Other sections can set this value as an override. Where it is set it
		 * will have priority.
		 */
		private SectionId subsiduarySectionId;
		private SectionRenderable renderable;

		@Bookmarked
		private String tabId;

		public String getSummaryId()
		{
			return summaryId;
		}

		public void setSummaryId(String summaryId)
		{
			this.summaryId = summaryId;
		}

		public SectionId getSubsiduarySectionId()
		{
			return subsiduarySectionId;
		}

		public void setSubsiduarySectionId(SectionId subsiduarySectionId)
		{
			this.subsiduarySectionId = subsiduarySectionId;
		}

		public SectionRenderable getRenderable()
		{
			return renderable;
		}

		public void setRenderable(SectionRenderable renderable)
		{
			this.renderable = renderable;
		}

		public String getTabId()
		{
			return tabId;
		}

		public void setTabId(String tabId)
		{
			this.tabId = tabId;
		}
	}
}
