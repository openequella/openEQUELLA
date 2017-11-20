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

import com.tle.beans.item.ItemStatus;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class RestoreSection extends GenericMinorActionSection
{
	@PlugKey("summary.sidebar.actions.restore.title")
	private static Label LINK_LABEL;
	@PlugKey("summary.sidebar.actions.restore.receipt")
	private static Label RECEIPT_LABEL;
	@Inject
	private ItemOperationFactory workflowFactory;

	@Override
	protected Label getLinkLabel()
	{
		return LINK_LABEL;
	}

	@Override
	@SuppressWarnings("nls")
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return itemInfo.hasPrivilege("DELETE_ITEM") && status.getStatusName().equals(ItemStatus.DELETED);
	}

	@Override
	protected void execute(SectionInfo info)
	{
		getItemInfo(info).modify(workflowFactory.restore());
		setReceipt(RECEIPT_LABEL);
	}

	@Override
	public String getLinkText()
	{
		return LINK_LABEL.getText();
	}
}
