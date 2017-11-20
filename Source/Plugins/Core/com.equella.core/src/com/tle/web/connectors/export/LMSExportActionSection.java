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

package com.tle.web.connectors.export;

import javax.inject.Inject;

import com.tle.beans.workflow.WorkflowStatus;
import com.tle.common.connectors.ConnectorConstants;
import com.tle.core.connectors.service.ConnectorService;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionWithPageSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class LMSExportActionSection extends GenericMinorActionWithPageSection
{
	@PlugKey("export.link.itemaction")
	private static Label LABEL_EXPORT_TO_SYSTEM_LINK;
	@TreeLookup
	private LMSExportSection exportSection;
	@Inject
	private ConnectorService connectorService;

	@Override
	protected Label getLinkLabel()
	{
		return LABEL_EXPORT_TO_SYSTEM_LINK;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		boolean allowPush = itemInfo.hasPrivilege(ConnectorConstants.PRIV_EXPORT_TO_LMS_ITEM);
		return allowPush && (connectorService.listExportable().size() > 0);
	}

	@Override
	protected SectionId getPageSection()
	{
		return exportSection;
	}

	@Override
	public String getLinkText()
	{
		return LABEL_EXPORT_TO_SYSTEM_LINK.getText();
	}
}
