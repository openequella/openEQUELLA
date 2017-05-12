package com.tle.web.sections.header;

import java.util.Map;

public interface FormAction
{
	String getFormAction();

	Map<String, String[]> getHiddenState();
}
