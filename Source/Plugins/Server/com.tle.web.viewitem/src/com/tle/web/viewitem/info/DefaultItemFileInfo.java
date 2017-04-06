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
import com.tle.core.services.item.ItemService;
import com.tle.core.workflow.operations.StatusOperation;
import com.tle.core.workflow.operations.WorkflowFactory;
import com.tle.core.workflow.operations.WorkflowOperation;
import com.tle.web.viewable.ViewableItem;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
public class DefaultItemFileInfo implements ItemSectionInfo
{
	private ViewableItem<Item> viewableItem;
	private boolean updated;
	private Attachments attachments;
	@Inject
	private WorkflowFactory workflowFactory;

	private boolean purged;

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
