package com.tle.web.sections.standard.event;

import com.tle.web.sections.SectionInfo;

public interface ValueSetListener<T>
{
	void valueSet(SectionInfo info, T value);
}
