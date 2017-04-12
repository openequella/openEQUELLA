package com.tle.core.qti.item.operation;

import javax.inject.Inject;

import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.core.filesystem.ItemFile;
import com.tle.core.guice.Bind;
import com.tle.core.workflow.operations.AbstractWorkflowOperation;

/**
 * @author Aaron
 */
@Bind
public class CreateTestEntitiesOperation extends AbstractWorkflowOperation
{
	@Inject
	private QtiAttachmentListener attachmentListener;

	@Override
	public boolean execute()
	{
		final Item item = getItem();
		FileHandle handle = getStaging();
		if( handle == null )
		{
			handle = new ItemFile(item);
		}
		attachmentListener.attachmentsChanged(null, item, handle);
		return false;
	}
}
