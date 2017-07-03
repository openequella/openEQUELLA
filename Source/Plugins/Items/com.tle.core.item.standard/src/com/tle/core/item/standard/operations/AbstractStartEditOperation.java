package com.tle.core.item.standard.operations;

import javax.inject.Inject;

import com.tle.common.filesystem.handle.StagingFile;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.staging.service.StagingService;

public abstract class AbstractStartEditOperation extends StartLockOperation
{
	private final boolean modify;

	@Inject
	private StagingService stagingService;

	public AbstractStartEditOperation(boolean modify, boolean dontRelock)
	{
		super(dontRelock);
		this.modify = modify;
	}

	@Override
	public boolean execute()
	{
		super.execute();
		if( modify )
		{
			ItemFile file = itemFileService.getItemFile(getItem());
			StagingFile staging = stagingService.createStagingArea();
			fileSystemService.copy(file, staging);
			getItemPack().setStagingID(staging.getUuid());
		}
		return false;
	}
}
