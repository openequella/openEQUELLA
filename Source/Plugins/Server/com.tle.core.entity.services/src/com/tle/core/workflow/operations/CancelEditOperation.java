package com.tle.core.workflow.operations;

import javax.inject.Inject;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.services.LockingService;
import com.tle.core.services.StagingService;

@SuppressWarnings("nls")
public class CancelEditOperation extends AbstractWorkflowOperation
{
	@Inject
	private LockingService lockingService;
	@Inject
	private StagingService stagingService;

	private final String stagingId;
	private final boolean unlock;

	@AssistedInject
	protected CancelEditOperation(@Assisted("stagingId") @Nullable String stagingId, @Assisted("unlock") boolean unlock)
	{
		this.stagingId = stagingId;
		this.unlock = unlock;
	}

	@Override
	public boolean execute()
	{
		final ItemPack<Item> itemPack = getItemPack();
		if( itemPack != null )
		{
			itemPack.setStagingID(stagingId);
		}
		itemService.executeExtensionOperationsNow(params, "preCancel");

		if( stagingId != null )
		{
			stagingService.removeStagingArea(new StagingFile(stagingId), true);
		}

		final Item item = getItem();
		if( item != null && unlock )
		{
			lockingService.unlockItem(item, false);
		}
		return false;
	}
}
