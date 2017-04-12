package com.tle.web.sections.standard.model;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface HtmlListModel<T>
{
	List<Option<T>> getOptions(SectionInfo info);

	@Nullable
	Option<T> getOption(SectionInfo info, String value);

	List<T> getValues(SectionInfo info, Collection<String> values);

	@Nullable
	T getValue(SectionInfo info, String value);

	Set<String> getMatchingValues(SectionInfo info, Set<String> values);

	@Nullable
	String getDefaultValue(SectionInfo info);

	String getStringValue(SectionInfo info, T value);

}
