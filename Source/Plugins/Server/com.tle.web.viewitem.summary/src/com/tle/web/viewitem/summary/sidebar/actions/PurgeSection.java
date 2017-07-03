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

import com.dytech.edge.web.WebConstants;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.selection.SelectionService;
import com.tle.web.viewurl.ItemSectionInfo;

@SuppressWarnings("nls")
@Bind
public class PurgeSection extends GenericMinorActionSection
{
	@PlugKey("summary.sidebar.actions.purge.title")
	private static Label LINK_LABEL;
	@PlugKey("summary.sidebar.actions.purge.confirm")
	private static Label CONFIRM_LABEL;
	@PlugKey("summary.sidebar.actions.purge.receipt")
	private static Label RECEIPT_LABEL;

	@Inject
	private SelectionService selectionService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private ItemOperationFactory workflowFactory;

	@Override
	protected Label getLinkLabel()
	{
		return LINK_LABEL;
	}

	@Override
	protected Label getConfirmation()
	{
		return CONFIRM_LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return itemInfo.hasPrivilege("PURGE_ITEM") && status.getStatusName().equals(ItemStatus.DELETED);
	}

	@Override
	protected void execute(SectionInfo info)
	{
		getItemInfo(info).modify(workflowFactory.purge(true));
		setReceipt(RECEIPT_LABEL);

		if( selectionService.getCurrentSession(info) == null )
		{
			info.forwardToUrl(institutionService.institutionalise(WebConstants.DEFAULT_HOME_PAGE));
		}
		else
		{
			selectionService.forwardToSelectable(info, null);
		}
	}

	@Override
	public String getLinkText()
	{
		return LINK_LABEL.getText();
	}

}
