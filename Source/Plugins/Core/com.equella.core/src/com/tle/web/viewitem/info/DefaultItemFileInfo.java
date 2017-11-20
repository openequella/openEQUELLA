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

package com.tle.web.viewitem.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.item.standard.operations.workflow.StatusOperation;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class DefaultItemFileInfo implements ItemSectionInfo
{
	private ViewableItem<Item> viewableItem;
	private boolean updated;
	private Attachments attachments;
	private boolean purged;

	@Inject
	private ItemOperationFactory workflowFactory;
	@Inject
	private ItemService itemService;

	public DefaultItemFileInfo()
	{
		// nothing
	}

	@Override
	public ViewableItem<Item> getViewableItem()
	{
		return viewableItem;
	}

	@Override
	public Item getItem()
	{
		return viewableItem.getItem();
	}

	@Override
	public ItemDefinition getItemdef()
	{
		return viewableItem.getItem().getItemDefinition();
	}

	@Override
	public String getItemdir()
	{
		return viewableItem.getItemdir();
	}

	@Override
	public ItemKey getItemId()
	{
		return viewableItem.getItemId();
	}

	@Override
	public PropBagEx getItemxml()
	{
		return viewableItem.getItemxml();
	}

	@Nullable
	@Override
	public WorkflowStatus getWorkflowStatus()
	{
		return viewableItem.getWorkflowStatus();
	}

	@Override
	public Set<String> getPrivileges()
	{
		return viewableItem.getPrivileges();
	}

	@Override
	public boolean hasPrivilege(String privilege)
	{
		return getPrivileges().contains(privilege);
	}

	public String getReferrerUrl()
	{
		return null;
	}

	public boolean isUpdated()
	{
		return updated;
	}

	public void setViewableItem(ViewableItem viewableItem)
	{
		this.viewableItem = viewableItem;
	}

	@Override
	public void modify(WorkflowOperation... ops)
	{
		List<WorkflowOperation> workOps = new ArrayList<WorkflowOperation>();
		workOps.add(workflowFactory.startLock());
		workOps.addAll(Arrays.asList(ops));
		StatusOperation statop = workflowFactory.status();
		workOps.add(workflowFactory.save());
		workOps.add(statop);
		ItemPack pack = itemService.operation(viewableItem.getItemId(),
			workOps.toArray(new WorkflowOperation[workOps.size()]));
		viewableItem.update(pack, statop.getStatus());
		updated = true;
	}

	@Override
	public boolean isEditing()
	{
		return false;
	}

	@Override
	public void refreshItem(boolean modified)
	{
		updated |= modified;
		viewableItem.refresh();
	}

	@Override
	public Attachments getAttachments()
	{
		if( attachments == null )
		{
			attachments = new UnmodifiableAttachments(getItem());
		}
		return attachments;
	}

	public boolean isPurged()
	{
		return purged;
	}

	public void setPurged(boolean purged)
	{
		this.purged = purged;
	}
}
