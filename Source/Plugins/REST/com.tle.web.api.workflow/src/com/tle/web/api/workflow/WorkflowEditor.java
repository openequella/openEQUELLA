package com.tle.web.api.workflow;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.workflow.Workflow;
import com.tle.web.api.baseentity.serializer.BaseEntityEditor;
import com.tle.web.api.workflow.interfaces.beans.WorkflowBean;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface WorkflowEditor extends BaseEntityEditor<Workflow, WorkflowBean>
{
	// Nothing
}
