package com.tle.freetext;

import com.tle.beans.Institution;

public class ItemIndexDelete
{
	private final long itemId;
	private final Institution institution;

	public ItemIndexDelete(long itemId, Institution inst)
	{
		this.itemId = itemId;
		this.institution = inst;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public long getItemId()
	{
		return itemId;
	}

}
