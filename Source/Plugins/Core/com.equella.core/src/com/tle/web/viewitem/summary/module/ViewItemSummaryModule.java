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

package com.tle.web.viewitem.summary.module;

import com.google.inject.Scopes;
import com.google.inject.name.Names;
import com.tle.core.guice.PluginTrackerModule;
import com.tle.web.freemarker.BasicFreemarkerFactory;
import com.tle.web.sections.equella.guice.SectionsModule;
import com.tle.web.selection.section.SelectionSummarySection;
import com.tle.web.viewitem.AttachmentViewFilter;
import com.tle.web.viewitem.guice.ViewItemModule.ViewItemPropsModule;
import com.tle.web.viewitem.section.ConversionSection;
import com.tle.web.viewitem.section.LegacyUrlSection;
import com.tle.web.viewitem.section.RootItemFileSection;
import com.tle.web.viewitem.section.ViewAttachmentSection;
import com.tle.web.viewitem.section.ViewDefaultSection;
import com.tle.web.viewitem.summary.content.ChangeOwnershipContentSection;
import com.tle.web.viewitem.summary.content.ExportContentSection;
import com.tle.web.viewitem.summary.content.HistoryContentSection;
import com.tle.web.viewitem.summary.content.MainItemContentSection;
import com.tle.web.viewitem.summary.content.TermsOfUseContentSection;
import com.tle.web.viewitem.summary.content.VersionsContentSection;
import com.tle.web.viewitem.summary.filter.DRMFilterSection;
import com.tle.web.viewitem.summary.section.ItemSummaryContentSection;
import com.tle.web.viewitem.summary.section.ItemSummarySidebarSection;
import com.tle.web.viewitem.summary.section.SummarySection;
import com.tle.web.viewitem.summary.sidebar.ItemDetailsGroupSection;
import com.tle.web.viewitem.summary.sidebar.LockedByGroupSection;
import com.tle.web.viewitem.summary.sidebar.MajorActionsGroupSection;
import com.tle.web.viewitem.summary.sidebar.MinorActionsGroupSection;
import com.tle.web.viewitem.summary.sidebar.actions.SelectItemSummarySection;
import com.tle.web.viewitem.summary.sidebar.actions.UnselectItemSummarySection;
import com.tle.web.viewitem.summary.sidebar.summary.HistoryLinkSection;
import com.tle.web.viewitem.summary.sidebar.summary.TermsOfUseLinkSection;
import com.tle.web.viewitem.viewer.DirListViewer;
import com.tle.web.viewitem.viewer.WorkflowFlowchartSection;

@SuppressWarnings("nls")
public class ViewItemSummaryModule extends SectionsModule
{
	@Override
	protected void configure()
	{
		bind(Object.class).annotatedWith(Names.named("/viewitem/viewitem")).toProvider(viewItemTree());
		install(new ViewItemPropsModule());
		install(new TrackerModule());
	}

	private NodeProvider viewItemTree()
	{
		NodeProvider node = node(RootItemFileSection.class);
		node.child(LegacyUrlSection.class);
		node.child(summarySection());
		node.child(DirListViewer.class);
		node.child(WorkflowFlowchartSection.class);
		node.child(ConversionSection.class);
		node.child(ViewAttachmentSection.class);
		node.child(ViewDefaultSection.class);
		node.child(DRMFilterSection.class);
		return node;
	}

	private NodeProvider summarySection()
	{
		NodeProvider node = node(SummarySection.class).placeHolder("SUMMARY_PAGE");
		node.child(summaryContent());
		node.child(summarySidebar());
		return node;
	}

	private NodeProvider summaryContent()
	{
		NodeProvider node = node(ItemSummaryContentSection.class).placeHolder(
			"com.tle.web.viewitem.summary.SUMMARY_CONTENT");
		node.child(node(MainItemContentSection.class).placeHolder("com.tle.web.viewitem.summary.SUMMARY_MAINCONTENT"));
		node.child(TermsOfUseContentSection.class);
		node.child(VersionsContentSection.class);
		node.child(HistoryContentSection.class);
		node.child(node(ExportContentSection.class).placeHolder("com.tle.web.viewitem.summary.section.EXPORTERS"));
		node.child(ChangeOwnershipContentSection.class);
		return node;
	}

	private NodeProvider summarySidebar()
	{
		NodeProvider node = node(ItemSummarySidebarSection.class).placeHolder(
			"com.tle.web.viewitem.summary.SUMMARY_SIDEBAR");
		node.child(majorActions());
		node.child(ViewItemSelectionSummarySection.class);
		node.child(node(LockedByGroupSection.class).placeHolder("com.tle.web.viewitem.summary.sidebar.LOCKED_BY"));
		node.child(itemDetails());
		node.child(MinorActionsGroupSection.class);
		return node;
	}

	private NodeProvider itemDetails()
	{
		NodeProvider node = node(ItemDetailsGroupSection.class).placeHolder(
			"com.tle.web.viewitem.summary.sidebar.DETAILS_GROUP");
		node.child(HistoryLinkSection.class);
		node.child(TermsOfUseLinkSection.class);
		return node;
	}

	private NodeProvider majorActions()
	{
		NodeProvider node = node(MajorActionsGroupSection.class).placeHolder(
			"com.tle.web.viewitem.summary.sidebar.MAJOR_ACTIONS");
		node.child(SelectItemSummarySection.class);
		node.child(UnselectItemSummarySection.class);
		return node;
	}

	private static class TrackerModule extends PluginTrackerModule
	{
		@Override
		protected String getPluginId()
		{
			return "com.tle.web.viewitem.summary";
		}

		@Override
		protected void configure()
		{
			bindTracker(AttachmentViewFilter.class, "attachmentViewFilter", "class");
		}
	}

	public static class ViewItemSelectionSummarySection extends SelectionSummarySection
	{
		public ViewItemSelectionSummarySection()
		{
			setLayout("");
			setFollowWithHr(true);
		}

		@Override
		public String getDefaultPropertyName()
		{
			return "ss";
		}
	}
}
