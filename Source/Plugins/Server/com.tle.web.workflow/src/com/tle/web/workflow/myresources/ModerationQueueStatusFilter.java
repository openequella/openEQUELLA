package com.tle.web.workflow.myresources;

import java.util.Arrays;
import java.util.List;

import com.tle.beans.item.ItemStatus;
import com.tle.core.guice.Bind;
import com.tle.web.search.filter.FilterByItemStatusSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.TreeLookup;

@Bind
public class ModerationQueueStatusFilter extends FilterByItemStatusSection
{
	@TreeLookup
	private FilterByItemStatusSection otherFilter;

	@Override
	protected List<ItemStatus> getStatusList()
	{
		return Arrays.asList(ItemStatus.MODERATING, ItemStatus.REVIEW, ItemStatus.REJECTED);
	}

	@SuppressWarnings("nls")
	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);
		itemStatus.setParameterId("mstatus");
		onlyInModeration.setParameterId("msmodonly");
	}

	@Override
	public Object instantiateModel(SectionInfo info)
	{
		return new Model(true);
	}

	public void enable(SectionInfo info)
	{
		getModel(info).setDisabled(false);
		otherFilter.disable(info);
	}
}
