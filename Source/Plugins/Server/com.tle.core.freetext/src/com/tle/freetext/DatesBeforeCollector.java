package com.tle.freetext;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.tle.beans.Institution;

public class DatesBeforeCollector extends AbstractCompareDateCollector
{
	private long before;

	public DatesBeforeCollector(Map<Long, Institution> instMap, List<ItemIndexDelete> toDelete, Date before)
	{
		super(instMap, toDelete);
		this.before = before.getTime();
	}

	@Override
	public void compareDate(long itemId, long instId, long time)
	{
		if( time < before )
		{
			toDelete.add(new ItemIndexDelete(itemId, instMap.get(instId)));
		}
	}

	@Override
	public List<IndexedItem> getModifiedDocs()
	{
		return null;
	}

}
