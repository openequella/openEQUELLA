package com.tle.web.workflow.tasks;

import com.tle.beans.item.ItemTaskId;
import com.tle.web.sections.SectionInfo;

public interface ModerationView
{
	SectionInfo getViewForward(SectionInfo info, ItemTaskId itemTaskId, String view);
}
