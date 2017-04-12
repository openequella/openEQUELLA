package com.tle.web.workflow.tasks;

import com.tle.common.workflow.Workflow;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.TreeIndexed;

@TreeIndexed
public interface WorkflowSelection extends SectionId
{
	Workflow getWorkflow(SectionInfo info);

	void setWorkflow(SectionInfo info, Workflow workflow);
}
