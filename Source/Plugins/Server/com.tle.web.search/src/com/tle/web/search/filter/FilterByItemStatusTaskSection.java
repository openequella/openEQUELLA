package com.tle.web.search.filter;

import java.util.ArrayList;
import java.util.List;

import com.tle.beans.item.ItemStatus;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.events.RenderEventContext;

public class FilterByItemStatusTaskSection extends FilterByItemStatusSection
{
	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		getModel(context).setHideCheckBox(true);
		return super.renderHtml(context);
	}

	@Override
	protected List<ItemStatus> getStatusList()
	{
		List<ItemStatus> status = new ArrayList<ItemStatus>();
		for( ItemStatus i : ItemStatus.values() )
		{
			if( !i.equals(ItemStatus.PERSONAL) )
			{
				status.add(i);
			}
		}
		return status;
	}
}
