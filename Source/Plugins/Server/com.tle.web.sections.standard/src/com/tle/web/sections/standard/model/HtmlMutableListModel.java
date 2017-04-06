package com.tle.web.sections.standard.model;

import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface HtmlMutableListModel<T>
{
	List<String> getValues(SectionInfo info);

	List<Option<T>> getOptions(SectionInfo info);

	void setValues(SectionInfo info, List<String> strings);
}
