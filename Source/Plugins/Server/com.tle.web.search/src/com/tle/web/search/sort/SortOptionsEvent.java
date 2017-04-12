package com.tle.web.search.sort;

import java.util.EventListener;
import java.util.List;

import com.google.common.collect.Lists;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.AbstractSectionEvent;

public class SortOptionsEvent extends AbstractSectionEvent<SortOptionsListener>
{
	private List<Iterable<SortOption>> extraOptions = Lists.newArrayList();
	private AbstractSortOptionsSection section;

	public SortOptionsEvent(AbstractSortOptionsSection section)
	{
		this.section = section;
	}

	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return SortOptionsListener.class;
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, SortOptionsListener listener)
	{
		Iterable<SortOption> options = listener.addSortOptions(info, section);
		if( options != null )
		{
			extraOptions.add(options);
		}
	}

	public List<Iterable<SortOption>> getExtraOptions()
	{
		return extraOptions;
	}

	public void setExtraOptions(List<Iterable<SortOption>> extraOptions)
	{
		this.extraOptions = extraOptions;
	}

}