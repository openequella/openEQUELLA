package com.tle.core.item.standard.operations;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.item.Item;

public class SetItemThumbnailOperation extends AbstractStandardWorkflowOperation
{
	private final String thumb;

	@AssistedInject
	private SetItemThumbnailOperation(@Assisted String thumb)
	{
		this.thumb = thumb;
	}

	@Override
	public boolean execute()
	{
		Item item = getItem();
		item.setThumb(thumb);
		params.setUpdateSecurity(true);
		return true;
	}
}
