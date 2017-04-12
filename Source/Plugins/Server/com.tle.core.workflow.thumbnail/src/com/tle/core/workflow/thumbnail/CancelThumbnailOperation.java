package com.tle.core.workflow.thumbnail;

import javax.inject.Inject;

import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;
import com.tle.core.workflow.thumbnail.service.ThumbnailService;

/**
 * Invoked via preCancel plugin point extension in CancelOperation
 * @author Aaron
 *
 */
@Bind
public class CancelThumbnailOperation extends AbstractWorkflowOperation
{
	@Inject
	private ThumbnailService thumbService;

	@Override
	public boolean execute()
	{
		//discard all thumb requests from this staging
		final StagingFile staging = getStaging();
		if( staging != null )
		{
			thumbService.cancelRequests(getItemId(), staging);
		}
		return false;
	}
}
