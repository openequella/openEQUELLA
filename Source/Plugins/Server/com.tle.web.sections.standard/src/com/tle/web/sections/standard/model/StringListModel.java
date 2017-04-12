package com.tle.web.sections.standard.model;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;

@NonNullByDefault
public class StringListModel extends MutableListModel<String>
{

	@Override
	protected String convertStringToObject(SectionInfo info, String str)
	{
		return str;
	}

}
