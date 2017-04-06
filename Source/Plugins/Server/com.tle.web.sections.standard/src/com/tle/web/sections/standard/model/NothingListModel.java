package com.tle.web.sections.standard.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.tle.web.sections.SectionInfo;

public class NothingListModel<T> implements HtmlListModel<T>
{

	@Override
	public Option<T> getOption(SectionInfo info, String value)
	{
		return null;
	}

	@Override
	public List<Option<T>> getOptions(SectionInfo info)
	{
		return Collections.emptyList();
	}

	@Override
	public List<T> getValues(SectionInfo info, Collection<String> values)
	{
		return Collections.emptyList();
	}

	@Override
	public T getValue(SectionInfo info, String value)
	{
		return null;
	}

	@Override
	public String getDefaultValue(SectionInfo info)
	{
		return null;
	}

	@Override
	public Set<String> getMatchingValues(SectionInfo info, Set<String> values)
	{
		return values;
	}

	@Override
	public String getStringValue(SectionInfo info, T value)
	{
		return null;
	}
}
