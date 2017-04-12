package com.tle.core.workflow.operations;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemStatus;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.events.ItemDeletedEvent;
import com.tle.core.events.UnindexItemEvent;
import com.tle.core.events.WaitForItemIndexEvent;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.security.TLEAclManager;
import com.tle.core.security.impl.SecureItemStatus;
import com.tle.core.security.impl.SecureOnCall;
import com.tle.core.services.EventService;

@SecureItemStatus(ItemStatus.DELETED)
@SecureOnCall(priv = "PURGE_ITEM")
public class PurgeOperation extends AbstractWorkflowOperation
{
	@Inject
	private EventService eventService;
	@Inject
	private TLEAclManager aclManager;

	private final boolean wait;

	@AssistedInject
	protected PurgeOperation(@Assisted("wait") boolean wait)
	{
		this.wait = wait;
	}

	@Override
	public boolean execute()
	{
		ItemIdKey id = params.getItemIdKey();
		params.setUpdateSecurity(true);

		eventService.publishApplicationEvent(new ItemDeletedEvent(id));

		Item item = getItem();
		aclManager.deleteAllEntityChildren(Node.DYNAMIC_ITEM_METADATA, item.getId());
		itemService.delete(item);

		params.setItemPack(null);

		final ItemId itemId = item.getItemId();
		params.addAfterCommitHook(WorkflowParams.COMMIT_HOOK_PRIORITY_LOW, new Runnable()
		{
			@Override
			public void run()
			{
				fileSystemService.removeFile(new ItemFile(itemId));
			}
		});
		addAfterCommitEvent(new UnindexItemEvent(id, true));
		addAfterCommitEvent(new UnindexItemEvent(id, false));
		if( wait )
		{
			addAfterCommitEvent(new WaitForItemIndexEvent(id));
		}
		return false;
	}
}
