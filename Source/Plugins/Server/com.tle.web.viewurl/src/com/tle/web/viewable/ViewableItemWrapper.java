package com.tle.web.viewable;

import java.util.Set;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.web.sections.Bookmark;

public abstract class ViewableItemWrapper implements ViewableItem<Item>
{
	protected ViewableItem<Item> inner;

	public ViewableItemWrapper()
	{
		// For serialization only...
	}

	public ViewableItemWrapper(ViewableItem<Item> def)
	{
		this.inner = def;
	}

	public void setInner(ViewableItem<Item> inner)
	{
		this.inner = inner;
	}

	@Override
	public Item getItem()
	{
		return inner.getItem();
	}

	@Override
	public PropBagEx getItemxml()
	{
		return inner.getItemxml();
	}

	@Override
	public FileHandle getFileHandle()
	{
		return inner.getFileHandle();
	}

	@Override
	public WorkflowStatus getWorkflowStatus()
	{
		return inner.getWorkflowStatus();
	}

	@Override
	public String getItemdir()
	{
		return inner.getItemdir();
	}

	@Override
	public ItemKey getItemId()
	{
		return inner.getItemId();
	}

	@Override
	public boolean isItemForReal()
	{
		return inner.isItemForReal();
	}

	@Override
	public void update(ItemPack pack, WorkflowStatus status)
	{
		inner.update(pack, status);
	}

	@Override
	public Set<String> getPrivileges()
	{
		return inner.getPrivileges();
	}

	@Override
	public boolean isDRMApplicable()
	{
		return inner.isDRMApplicable();
	}

	@Override
	public void refresh()
	{
		inner.refresh();
	}

	@Override
	public IAttachment getAttachmentByUuid(String uuid)
	{
		return inner.getAttachmentByUuid(uuid);
	}

	@Override
	public Bookmark createStableResourceUrl(String path)
	{
		return inner.createStableResourceUrl(path);
	}
}
