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

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.itemdef.SummarySectionsConfig;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.selection.section.RootSelectionSection.Layout;
import com.tle.web.viewitem.summary.content.HistoryContentSection;
import com.tle.web.viewitem.summary.content.TermsOfUseContentSection;
import com.tle.web.viewitem.summary.sidebar.ItemDetailsGroupSection;
import com.tle.web.viewitem.summary.sidebar.LockedByGroupSection;
import com.tle.web.viewitem.summary.sidebar.MinorActionsGroupSection;

/**
 * Used exclusively for the details when in a "structured" selection session
 * 
 * @author Aaron
 */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class ItemDetailsAndActionsSummarySection
	extends
		AbstractItemDetailsSection<ItemDetailsAndActionsSummarySection.ItemDetailsModel>
	implements
		DisplaySectionConfiguration
{
	@Inject
	private SelectionService selectionService;

	@EventFactory
	private EventGenerator events;

	@TreeLookup
	private HistoryContentSection historySection;
	@TreeLookup
	private TermsOfUseContentSection termsOfUseSection;
	@TreeLookup
	private MinorActionsGroupSection minorActionsGroupSection;
	@TreeLookup
	private LockedByGroupSection lockedByGroupSection;
	@TreeLookup
	private ItemDetailsGroupSection itemDetailsGroupSection;

	@Override
	public boolean canView(SectionInfo info)
	{
		final SelectionSession ss = selectionService.getCurrentSession(info);
		return (ss != null && ss.getLayout() == Layout.COURSE);
	}

	@Override
	protected String getTemplate(RenderEventContext context)
	{
		final ItemDetailsModel model = getModel(context);

		model.setMinorActions(renderSection(context, minorActionsGroupSection));
		model.setItemDetails(renderSection(context, itemDetailsGroupSection));

		final WorkflowStatus status = getItemInfo(context).getWorkflowStatus();

		if( getItemInfo(context).isEditing() || status.isLocked() )
		{
			model.setLockSection(renderSection(context, lockedByGroupSection));
		}

		return "viewitem/itemdetails.ftl";
	}

	@Override
	public void associateConfiguration(SummarySectionsConfig config)
	{
		// Nah
	}

	@EventHandlerMethod
	public void showHistory(SectionInfo info)
	{
		contentSection.setSummaryId(info, historySection);
	}

	@EventHandlerMethod
	public void showTermsOfUse(SectionInfo info)
	{
		contentSection.setSummaryId(info, termsOfUseSection);
	}

	@Override
	public ItemDetailsModel instantiateModel(SectionInfo info)
	{
		return new ItemDetailsModel();
	}

	public static class ItemDetailsModel extends AbstractItemDetailsSection.ItemDetailsModel
	{
		private SectionRenderable minorActions;
		private SectionRenderable lockSection;
		private SectionRenderable itemDetails;

		public SectionRenderable getMinorActions()
		{
			return minorActions;
		}

		public void setMinorActions(SectionRenderable minorActions)
		{
			this.minorActions = minorActions;
		}

		public SectionRenderable getLockSection()
		{
			return lockSection;
		}

		public void setLockSection(SectionRenderable lockSection)
		{
			this.lockSection = lockSection;
		}

		public SectionRenderable getItemDetails()
		{
			return itemDetails;
		}

		public void setItemDetails(SectionRenderable itemDetails)
		{
			this.itemDetails = itemDetails;
		}
	}
}
