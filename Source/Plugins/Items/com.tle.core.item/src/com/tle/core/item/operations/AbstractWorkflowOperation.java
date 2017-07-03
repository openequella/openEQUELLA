package com.tle.core.item.operations;

import java.util.Collection;
import java.util.Date;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import com.dytech.devlib.PropBagEx;
import com.google.common.collect.Sets;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemKey;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.item.service.ItemFileService;
import com.tle.core.item.service.ItemService;
import com.tle.core.services.FileSystemService;
import com.tle.common.usermanagement.user.CurrentUser;

/**
 * @author Aaron
 *
 */
@NonNullByDefault
public abstract class AbstractWorkflowOperation implements WorkflowOperation
{
	@Inject
	protected ItemDefinitionService itemdefService;
	@Inject
	protected ItemService itemService;
	@Inject
	protected FileSystemService fileSystemService;
	@Inject
	protected ItemFileService itemFileService;

	protected ItemOperationParams params;

	private boolean injected;

	@Override
	public void setParams(ItemOperationParams params)
	{
		this.params = params;
	}

	@Nullable
	@Override
	public Item getItem()
	{
		final ItemPack<Item> pack = params.getItemPack();
		if( pack != null )
		{
			return pack.getItem();
		}
		return null;
	}

	@Nullable
	@Override
	public ItemPack<Item> getItemPack()
	{
		return params.getItemPack();
	}

	@Override
	public boolean isReadOnly()
	{
		return true;
	}

	@Override
	public boolean failedToAutowire()
	{
		return !injected;
	}

	@Override
	public boolean isDeleteLike()
	{
		return false;
	}

	protected String getUuid()
	{
		return getItem().getUuid();
	}

	protected int getVersion()
	{
		return getItem().getVersion();
	}

	protected ItemStatus getItemStatus()
	{
		return getItem().getStatus();
	}

	protected ItemDefinition getCollection()
	{
		return getItem().getItemDefinition();
	}

	protected Schema getSchema()
	{
		return getCollection().getSchema();
	}

	protected String getItemOwnerId()
	{
		return getItem().getOwner();
	}

	protected Collection<String> getAllOwnerIds()
	{
		Set<String> owners = Sets.newHashSet();
		owners.add(getItemOwnerId());
		owners.addAll(getItem().getCollaborators());
		return owners;
	}

	public Attachments getAttachments()
	{
		return new UnmodifiableAttachments(getItem());
	}

	protected boolean isOwner(String userid)
	{
		return getItem().getOwner().equals(userid);
	}

	protected void setOwner(String userid)
	{
		getItem().setOwner(userid);
	}

	protected String getUserId()
	{
		return CurrentUser.getUserID();
	}

	protected ItemKey getItemKey()
	{
		return params.getItemKey();
	}

	protected void setStopImmediately(boolean stop)
	{
		params.setStopImmediately(stop);
	}

	@Nullable
	protected StagingFile getStaging()
	{
		ItemPack<Item> itemPack = getItemPack();
		if( itemPack != null )
		{
			String s = itemPack.getStagingID();
			if( !Check.isEmpty(s) )
			{
				return new StagingFile(s);
			}
		}
		return null;
	}

	protected Date getDateModified()
	{
		Item item = getItem();
		if( item == null || item.getDateModified() == null )
		{
			return params.getDateNow();
		}
		return item.getDateModified();
	}

	protected void setVersion(int version)
	{
		getItem().setVersion(version);
	}

	protected void addAfterCommitEvent(ApplicationEvent<?> event)
	{
		params.getAfterCommitEvents().add(event);
	}

	protected ItemId getItemId()
	{
		return getItem().getItemId();
	}

	protected PropBagEx getItemXml()
	{
		return getItemPack().getXml();
	}

	protected ItemOperationParams getParams()
	{
		return params;
	}

	@PostConstruct
	protected void injected()
	{
		this.injected = true;
	}
}
