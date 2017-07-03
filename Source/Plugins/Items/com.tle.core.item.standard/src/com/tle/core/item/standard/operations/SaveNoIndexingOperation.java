package com.tle.core.item.standard.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.annotation.Nullable;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.common.filesystem.handle.StagingFile;

public class SaveNoIndexingOperation extends SaveOperation
{
	private final String stagingID;

	@AssistedInject
	protected SaveNoIndexingOperation(@Assisted boolean noAutoArchive, @Assisted @Nullable String stagingID)
	{
		super(true);
		setNoAutoArchive(noAutoArchive);
		this.stagingID = stagingID;
	}

	@Override
	protected void addIndexingEvents(ItemIdKey newKey, Item item)
	{
		// none
	}

	@Override
	public boolean execute()
	{
		getItemPack().setStagingID(stagingID);
		return super.execute();
	}

	@Override
	protected StagingFile getStagingForCommit()
	{
		return null;
	}
}
