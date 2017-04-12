package com.tle.core.workflow.operations;

import javax.inject.Inject;

import com.tle.core.filesystem.ItemFile;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.services.StagingService;

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
			ItemFile file = new ItemFile(getItemKey());
			StagingFile staging = stagingService.createStagingArea();
			fileSystemService.copy(file, staging);
			getItemPack().setStagingID(staging.getUuid());
		}
		return false;
	}
}
