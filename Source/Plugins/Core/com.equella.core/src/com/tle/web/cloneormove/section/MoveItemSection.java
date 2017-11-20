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

package com.tle.web.cloneormove.section;

import com.tle.beans.item.Item;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.web.cloneormove.model.RootCloneOrMoveModel;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.summary.sidebar.actions.GenericMinorActionWithPageSection;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class MoveItemSection extends GenericMinorActionWithPageSection
{
	@PlugKey("moveonly.title")
	private static Label MOVE_TITLE_LABEL;

	@TreeLookup
	private RootCloneOrMoveSection rootCloneOrMoveSection;

	@Override
	protected Label getLinkLabel()
	{
		return MOVE_TITLE_LABEL;
	}

	@Override
	protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status)
	{
		return itemInfo.hasPrivilege(CloneOrMoveSection.MOVE_ITEM) && !status.isLocked();
	}

	@Override
	public void execute(SectionInfo info)
	{
		final Item item = getItemInfo(info).getItem();

		rootCloneOrMoveSection.setCurrentItemdef(info, item.getItemDefinition().getUuid());

		RootCloneOrMoveModel model = rootCloneOrMoveSection.getModel(info);
		model.setUuid(item.getUuid());
		model.setVersion(item.getVersion());
		model.setIsMove(true);
		super.execute(info);
	}

	@Override
	protected SectionId getPageSection()
	{
		return rootCloneOrMoveSection;
	}

	@Override
	public String getLinkText()
	{
		return MOVE_TITLE_LABEL.getText();
	}
}
