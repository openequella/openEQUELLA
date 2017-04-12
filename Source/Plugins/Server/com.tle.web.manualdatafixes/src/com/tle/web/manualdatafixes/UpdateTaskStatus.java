package com.tle.web.manualdatafixes;

import com.tle.web.sections.SectionInfo;

public interface UpdateTaskStatus
{
	String getAjaxId();

	boolean isFinished(SectionInfo info);
}
