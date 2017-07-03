package com.tle.core.item.standard.operations;

import javax.inject.Inject;

import com.tle.core.quota.service.QuotaService;

public class ItemFileSizeOperation extends AbstractStandardWorkflowOperation
{
	@Inject
	private QuotaService quotaService;

	@Override
	public boolean execute()
	{
		long file = quotaService.getFileSize(itemFileService.getItemFile(getItem()));
		if( file != getItem().getTotalFileSize() )
		{
			getItem().setTotalFileSize(file);
			return true;
		}
		return false;
	}
}
