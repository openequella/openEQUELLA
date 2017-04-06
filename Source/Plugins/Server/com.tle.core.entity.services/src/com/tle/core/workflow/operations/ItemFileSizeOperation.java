package com.tle.core.workflow.operations;

import javax.inject.Inject;

import com.tle.core.filesystem.ItemFile;
import com.tle.core.services.QuotaService;

public class ItemFileSizeOperation extends AbstractWorkflowOperation
{
	@Inject
	private QuotaService quotaService;

	@Override
	public boolean execute()
	{
		long file = quotaService.getFileSize(new ItemFile(getItem()));
		if( file != getItem().getTotalFileSize() )
		{
			getItem().setTotalFileSize(file);
			return true;
		}
		return false;
	}
}
