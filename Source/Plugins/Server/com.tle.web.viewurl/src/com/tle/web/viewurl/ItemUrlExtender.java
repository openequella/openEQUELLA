package com.tle.web.viewurl;

import java.io.Serializable;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public interface ItemUrlExtender extends Serializable
{
	void execute(SectionInfo info);
}