/*
 * Copyright 2019 Apereo
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
		node.child(com.tle.web.viewitem.htmlfiveviewer.HtmlFiveViewerSection.class);
		node.child(com.tle.web.viewitem.flvviewer.FLVViewerSection.class);
		node.child(com.tle.web.viewitem.externallinkviewer.ExternalLinkViewerSection.class);
		node.child(com.tle.web.echo.viewer.EchoViewerSection.class);
		node.child(com.tle.web.copyright.section.ViewByRequestSection.class);
		node.child(com.tle.web.externaltools.viewer.ExternalToolViewerSection.class);
		node.child(com.tle.mypages.mypagesviewer.section.MyPagesViewerSection.class);
		node.child(com.tle.web.qti.viewer.QtiPlayViewerSection.class);
		node.child(com.tle.cla.web.viewitem.summary.CLAAgreementSection.class);
		node.child(com.tle.web.scorm.treeviewer.ScormTreeNavigationSection.class);
		node.child(com.tle.web.viewitem.largeimageviewer.LargeImageViewerSection.class);
		node.child(com.tle.cal.web.viewitem.summary.CALAgreementSection.class);
		node.child(com.tle.qti.QTIViewerSection.class);
		node.child(com.tle.web.viewitem.treeviewer.TreeNavigationSection.class);
		node.child(com.tle.web.viewitem.treeviewer.NewTreeNavigationSection.class);
		return node;
	}

	private NodeProvider summarySection()
	{
		NodeProvider node = node(SummarySection.class).placeHolder("SUMMARY_PAGE");
		node.child(summaryContent());
		node.child(summarySidebar());
		return node;
	}

	private NodeProvider mainContent()
	{
		NodeProvider node = node(MainItemContentSection.class).placeHolder("com.tle.web.viewitem.summary.SUMMARY_MAINCONTENT");
		node.child(com.tle.cal.web.viewitem.summary.CALSummarySection.class);
		node.child(com.tle.cla.web.viewitem.summary.CLASummarySection.class);
		return node;
	}

	private NodeProvider summaryContent()
	{
		NodeProvider node = node(ItemSummaryContentSection.class).placeHolder(
			"com.tle.web.viewitem.summary.SUMMARY_CONTENT");
		node.child(mainContent());
		node.child(TermsOfUseContentSection.class);
		node.child(VersionsContentSection.class);
		node.child(HistoryContentSection.class);
		node.child(node(ExportContentSection.class).placeHolder("com.tle.web.viewitem.summary.section.EXPORTERS"));
		node.child(ChangeOwnershipContentSection.class);

		node.child(com.tle.web.viewitem.sharing.ShareWithOthersContentSection.class);
		node.child(com.tle.web.cloneormove.section.RootCloneOrMoveSection.class);
		node.child(com.tle.cla.web.viewitem.summary.CLAActivateSection.class);
		node.child(com.tle.web.hierarchy.addkey.HierarchyTreeSection.class);

		node.child(com.tle.cal.web.viewitem.summary.CALActivateSection.class);
		node.child(com.tle.web.workflow.view.CurrentModerationContentSection.class);
		node.child(com.tle.web.activation.viewitem.summary.section.ShowActivationsSection.class);
		node.child(com.tle.web.activation.viewitem.summary.section.EditActivationSection.class);

		node.child(com.tle.web.connectors.export.LMSExportSection.class);
		node.child(com.tle.web.connectors.viewitem.FindUsesContentSection.class);
		return node;
	}

	private NodeProvider summarySidebar()
	{
		NodeProvider node = node(ItemSummarySidebarSection.class).placeHolder(
			"com.tle.web.viewitem.summary.SUMMARY_SIDEBAR");
		node.child(majorActions());
		node.child(ViewItemSelectionSummarySection.class);
		NodeProvider lockedBy = node(LockedByGroupSection.class).placeHolder("com.tle.web.viewitem.summary.sidebar.LOCKED_BY");
		lockedBy.child(com.tle.web.contribute.ResumeSection.class);
		lockedBy.child(com.tle.web.contribute.DiscardSection.class);
		node.child(lockedBy);
		node.child(itemDetails());
		node.child(MinorActionsGroupSection.class);
		node.child(com.tle.web.viewitem.summary.section.ItemDetailsAndActionsSummarySection.class);
		return node;
	}

	private NodeProvider itemDetails()
	{
		NodeProvider node = node(ItemDetailsGroupSection.class).placeHolder(
			"com.tle.web.viewitem.summary.sidebar.DETAILS_GROUP");
		node.child(HistoryLinkSection.class);
		node.child(TermsOfUseLinkSection.class);
		node.child(com.tle.web.activation.viewitem.sidebar.summary.ActivationsLinkSection.class);
		node.child(com.tle.web.connectors.viewitem.FindUsesLinkSection.class);
		return node;
	}

	private NodeProvider majorActions()
	{
		NodeProvider node = node(MajorActionsGroupSection.class).placeHolder(
			"com.tle.web.viewitem.summary.sidebar.MAJOR_ACTIONS");
		node.child(com.tle.web.searching.prevnext.SearchPrevNextSection.class);
		node.child(com.tle.web.viewitem.sharing.ShareWithOthersLinkSection.class);
		node.child(com.tle.web.favourites.actions.AddToFavouritesSection.class);
		node.child(com.tle.web.favourites.actions.RemoveFromFavouritesSection.class);
		node.child(com.tle.web.viewitem.moderation.ViewMetadataAction.class);
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
